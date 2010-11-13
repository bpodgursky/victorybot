package gamesearch;

import heuristic.Heuristic;
import heuristic.NaiveHeuristic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ai.Bot;

import order.Order;
import order.Order.MovesValue;
import order.Order.OrderValue;
import order.builds.Build;
import order.builds.Remove;
import order.builds.Waive;
import order.retreats.Disband;
import order.retreats.Retreat;
import order.spring_fall.Hold;
import order.spring_fall.Move;
import order.spring_fall.SupportHold;
import order.spring_fall.SupportMove;
import representation.Country;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.constant.BoardConfiguration;
import state.constant.BoardConfiguration.TerritoryCoast;
import state.dynamic.BoardState;
import state.dynamic.BoardState.Phase;
import state.dynamic.BoardState.RetreatSituation;

public class MoveGeneration {

	private BoardConfiguration staticBoard;
	private Heuristic heuristic;
	
	private final Player ourPlayer;
	
	public MoveGeneration(BoardConfiguration sBoard, Player ourPlayer)
	{
		staticBoard = sBoard;
		this.ourPlayer = ourPlayer;
		heuristic = new NaiveHeuristic(sBoard);
	}
	
	private Random r = new Random();
	
	private static final int MOVES_PER_UNIT = 5;
	private static final int MAX_PLAYER_MOVES = 20;
	
	private List<Set<Order>> generateOrderSets(int num, int length, Map<TerritorySquare, List<OrderValue>> movesForAllUnits, Set<TerritorySquare> unit, BoardState dynamicState, Player player) throws Exception
	{
		StringBuilder str = new StringBuilder();
		TerritorySquare [] unitList = unit.toArray(new TerritorySquare[0]);
		
		int count = 0;
		
		while(num >= 1){
			count++;
			
			if(num%2 == 1){
				str.insert(0,"1");
			}
			else{
				str.insert(0,"0");
			}
			num = num / 2;
		}
		
		for(int i = count; i < length; i++){
			str.insert(0, "0");
		}
		
		char [] permute = str.toString().toCharArray();

		//Create vectors to store orders of units that will move and of units that we want to support other moves
		//Also create a HashMap between heuristic score of move and the order itself
		//Vector<Order> moveSet = new Vector<Order>();
		List<TerritorySquare> supportSet = new ArrayList<TerritorySquare>();
		
		Map<TerritorySquare, List<OrderValue>> movesPerUnit = new HashMap<TerritorySquare, List<OrderValue>>();
		
		count = 0;
		//Step through the array that gives us our permutations
		for(char c: permute) {
			TerritorySquare moveOrigin = unitList[count];
			
			//Means we will be giving a move order
			if(c == '1') {
				
				List<OrderValue> possibilities = movesForAllUnits.get(moveOrigin);
				//	otherwise we'll be looking at the same choices every turn
				Collections.shuffle(possibilities);
				
				List<OrderValue> topMoves = new ArrayList<OrderValue>();
				for(int i = 0; i < Math.min(MOVES_PER_UNIT, possibilities.size()); i++){
					topMoves.add(possibilities.get(i));
				}
				
				movesPerUnit.put(moveOrigin, topMoves);
			}
			//If not a direct order put in supportSet for later processing
			else {
				supportSet.add(moveOrigin);
			}
			count++;
		}
		
		List<Set<Order>> ordersToPopulate = new ArrayList<Set<Order>>();

		TerritorySquare[] relevantTerritories = movesPerUnit.keySet().toArray(new TerritorySquare[0]);
		
		enumerateMoves(dynamicState, player, relevantTerritories, new LinkedList<OrderValue>(), 0, movesPerUnit, supportSet, ordersToPopulate);
		
		return ordersToPopulate;
	}
	
	private void enumerateMoves(BoardState bst, Player p, TerritorySquare[] terrs, List<OrderValue> allOrders, int terrIndex, Map<TerritorySquare, List<OrderValue>> movesPerUnit, List<TerritorySquare> supportSet, List<Set<Order>> toPopulate) throws Exception{
		
		if(terrIndex == terrs.length){
			
			Set<TerritorySquare> supportSetCopy = new HashSet<TerritorySquare>(supportSet);
			Set<Order> finalMoveSet = new HashSet<Order>();
			HashMap<Double, Order> moveScoreMap = new HashMap<Double, Order>();
			
			//	added the rand because if two orders have the same score, 
			//	the second one will obliterate the first in the map
			for(OrderValue ov: allOrders){
				Double score = ov.score + r.nextDouble()/10000.0;
				moveScoreMap.put(score, ov.order);
			}
			
			//Sort heuristic scores and now lets step through remaining units and try to support our moves
			Double [] orderScore = (moveScoreMap.keySet().toArray(new Double[0]));
			Arrays.sort(orderScore);
			
			List<TerritorySquare> supportHoldable = new LinkedList<TerritorySquare>(supportSetCopy);
			
			for(Double heurVal: orderScore){
				
				Move supportedMove = (Move)moveScoreMap.get(heurVal);
				
				TerritorySquare suppToPoss = supportedMove.to;
				TerritorySquare supportFromPoss = supportedMove.from;
				
				for(TerritorySquare support: supportSetCopy.toArray(new TerritorySquare[0]))
				{
					
					//Do we border where a move is trying to go
					if(staticBoard.canSupportMove(bst, p, support,  supportFromPoss, suppToPoss)){
	
						Order ord = new SupportMove(bst, p, support, supportFromPoss, suppToPoss);
	
						finalMoveSet.add(ord);
						supportSetCopy.remove(support);
						
					}
				}
			}
			
			//	anything left in here either holds or supports holds
			for(TerritorySquare ts: supportSetCopy){

				//	find something to support hold on
				
				//	TODO this is not done intelligently.  rank the locations by 
				//	how much danger they are in, or enumerate the possibilities, or 
				//	something
				
				boolean foundSomething = false;
				for(TerritorySquare sq: supportHoldable){
					
					if(staticBoard.canSupportHold(bst, p, ts, sq)){
						finalMoveSet.add(new SupportHold(bst, p, ts, sq));
						
						foundSomething = true;
						break;
					}	
				}
				
				if(!foundSomething){
					finalMoveSet.add(new Hold(bst, p, ts));
				}	
			}

			for(OrderValue ov: allOrders) {
				finalMoveSet.add(ov.order);
			}
			
			if(finalMoveSet.size() != p.getOccupiedTerritories(bst).size()){
				
				for(Order ov: finalMoveSet){
					System.out.println("\t"+ov.toOrder(bst));
				}
				for(TerritorySquare terr: p.getOccupiedTerritories(bst)){
					System.out.println("\t"+terr.getName());
				}
				
				System.out.println("Support set: ");
				for(TerritorySquare terr: supportSet){
					System.out.println("\t"+terr.getName());
				}
				
				System.out.println("Moves per unit: ");
				for(TerritorySquare terr: movesPerUnit.keySet()){
					System.out.println("\t"+terr.getName());
				}
				
				throw new Exception("need "+p.getOccupiedTerritories(bst).size()+ " moves, got "+finalMoveSet.size());
			}
			
			toPopulate.add(finalMoveSet);
		}else{
			
			TerritorySquare thisOrigin = terrs[terrIndex];
			List<OrderValue> ordersForTerr = movesPerUnit.get(thisOrigin);
			
			for(OrderValue ov: ordersForTerr){
			
				allOrders.add(ov);
				
				enumerateMoves(bst, p, terrs, allOrders, terrIndex+1, movesPerUnit, supportSet, toPopulate);
				
				allOrders.remove(ov);
			}
		}
	}

	
	public MovesValue[] generateOrderSets(Player player, BoardState dynamicState) throws Exception
	{

		List<Collection<Order>> allCombinations = new LinkedList<Collection<Order>>();
		
		if(dynamicState.time.phase == Phase.SPR || dynamicState.time.phase == Phase.FAL){
			int unitCount = player.getNumberUnits(dynamicState);

			Set<TerritorySquare> unit = player.getOccupiedTerritories(dynamicState);
			
			Map<TerritorySquare, List<OrderValue>> orderPossibilities = staticBoard.getMovesForUnits(dynamicState, heuristic);
			
			//TODO how to avoid this.... 
			//	2) for each player, count only to the number of units that can affect you
			
			for(int i = 1; i < Math.pow(2.0, 
					Math.min(5, unitCount)); i++){
				allCombinations.addAll(generateOrderSets(i, unitCount, orderPossibilities, unit, dynamicState, player));
			}
			

		}else if(dynamicState.time.phase == Phase.SUM || dynamicState.time.phase == Phase.AUT){
			
			//	generate all combination of valid retreats for the player

			Set<RetreatSituation> playerRetreats = staticBoard.getRetreatsForPlayer(dynamicState, player);
			
			Map<RetreatSituation, List<Order>> retreatOptions = 
				new HashMap<RetreatSituation, List<Order>>();
			
			for(RetreatSituation rsit: playerRetreats){
				
				//	each of the places it can retreat to
				List<Order> unitOptions = new LinkedList<Order>();
				for(TerritoryCoast tcst: staticBoard.getRetreatsForUnit(dynamicState, rsit)){
					 unitOptions.add(new Retreat(dynamicState, player, rsit.from, tcst.sqr, tcst.coast));
				}
				
				//	and that it can disband 
				unitOptions.add(new Disband(dynamicState, player, rsit.from));
				
				retreatOptions.put(rsit, unitOptions);
			}

			enumerateRetreats(playerRetreats.toArray(new RetreatSituation[0]), 0, retreatOptions, new LinkedList<Order>(), allCombinations);
			
			//	so this is everything we can do with our retreat.  now we need to naively rank them: do so by
			//	seeing what would happen if every other retreat was a disband, and evaluating board states
			

		}
		else if(dynamicState.time.phase == Phase.WIN){
		
			//	generate all bcombinations of builds for the player
			
			//	basically, we want every combination of build locations and units to build there
			//	this should be small enough we can enumerate them... 
		
			int equalize = staticBoard.getRequiredBuilds(dynamicState, player);

			if(equalize > 0){
				enumerateBuilds(dynamicState, player, player.getHomeCenters(), equalize, new LinkedList<Order>(), allCombinations);
			}else if(equalize < 0){
				
				enumerateRemoves(dynamicState, player, player.getOccupiedTerritories(dynamicState), -equalize, new LinkedList<Order>(), allCombinations);
			}else{
				allCombinations.add(new LinkedList<Order>());
			}
		}
		
		if(allCombinations.size() == 0){
			throw new Exception("no orders generated for a player "+player.getName()+" in "+dynamicState.time);
		}
		
		//	want to order these moves by the naive quality the 
		//	board would have afterwards--basically, which of them
		//	is the opponent most likely to do
		//	the simplest way to calculate this is "what if everyone holds"
		
		Set<Order> otherOrders = new HashSet<Order>();
		for(Player p: staticBoard.getPlayers()){
			if(p == player) continue;
			
			otherOrders.addAll(staticBoard.generateDefaultOrdersFor(dynamicState, p));
		}
		
		List<MovesValue> valMoves = new LinkedList<MovesValue>();
		for(Collection<Order> ord: allCombinations){
			
			Set<Order> toSubmit = new HashSet<Order>(otherOrders);
			toSubmit.addAll(ord);
			
			BoardState stateAfterExecute = staticBoard.update(dynamicState.time.next(), dynamicState, toSubmit, false);
		
			valMoves.add(new MovesValue(ord, heuristic.boardScore(player, stateAfterExecute)));
		}
	
		MovesValue[] moves = valMoves.toArray(new MovesValue[0]);
		
		Arrays.sort(moves);
		
		MovesValue[] prunedMoves = new MovesValue[
		    Math.min(MAX_PLAYER_MOVES, moves.length)];
		for(int i = 0; i < MAX_PLAYER_MOVES && i < moves.length; i++){
			prunedMoves[i] = moves[i];
		}
		
		if(Bot.DEBUG){
			System.out.println("Reasonable sets of moves for player "+player.getName()+":");
			for(MovesValue mv: prunedMoves){
				System.out.println(mv.value+":");
				for(Order ord: mv.moves){
					System.out.println("\t\t"+ord.toOrder(dynamicState));
				}
			}
		}
		
		return prunedMoves;
	}
	
	private void enumerateRemoves(BoardState bst, Player p,
			Collection<TerritorySquare> occupied,
			int moreDisbands,
			List<Order> ordersSoFar,
			List<Collection<Order>> allDisbandCombos) throws Exception{
		
		//	no way to satisfy the disbands, return
		if(occupied.size() < moreDisbands) return;
		
		if(moreDisbands == 0){
			allDisbandCombos.add(ordersSoFar);
		}
		else if(!occupied.isEmpty()){
			
			TerritorySquare toLookAt = occupied.iterator().next();
			List<TerritorySquare> occupiedCopy = new LinkedList<TerritorySquare>(occupied);
			occupiedCopy.remove(toLookAt);
			
			//	remove it
			
			List<Order> ordersCopy = new LinkedList<Order>(ordersSoFar);
			ordersCopy.add(new Remove(bst, p, toLookAt));
			enumerateRemoves(bst, p, occupiedCopy, moreDisbands-1, ordersCopy, allDisbandCombos);
			
			//	or don't
			
			List<Order> ordersSame= new LinkedList<Order>(ordersSoFar);
			enumerateRemoves(bst, p, occupiedCopy, moreDisbands, ordersSame, allDisbandCombos);
		}
	}
	
	private void enumerateBuilds(BoardState bst, Player p,
			Collection<TerritorySquare> homeCenters, 
			int moreBuilds, 
			List<Order> ordersSoFar, 
			List<Collection<Order>> allCombinations) throws Exception{
		
		if(homeCenters.isEmpty()){
			
			for(int i = 0; i < moreBuilds; i++){
				ordersSoFar.add(new Waive(bst, p));
			}
			
			allCombinations.add(ordersSoFar);
			
		}else if(moreBuilds == 0){
			
			allCombinations.add(ordersSoFar);
			
		}else{
			TerritorySquare decide = homeCenters.iterator().next();
			
			List<TerritorySquare> remainingCenters = new LinkedList<TerritorySquare>(homeCenters);
			remainingCenters.remove(decide);
			
			//	build an army if can (can't if occupied or don't own it)
			if(staticBoard.canBuild(bst, p, new Unit(p, true), decide)){

				List<Order> ordersWithArmy = new LinkedList<Order>(ordersSoFar);
				ordersWithArmy.add(new Build(bst, p, new Unit(p, true), decide));
			
				enumerateBuilds(bst, p, remainingCenters, moreBuilds-1, ordersWithArmy, allCombinations);

			}

			//	fleet if can
			if(staticBoard.canBuild(bst, p, new Unit(p, false), decide)){

				List<Order> ordersWithFleet = new LinkedList<Order>(ordersSoFar);
				ordersWithFleet.add(new Build(bst, p, new Unit(p, false), decide));

				enumerateBuilds(bst, p, remainingCenters, moreBuilds-1, ordersWithFleet, allCombinations);
					
			}
			
			//	and nothing
			enumerateBuilds(bst, p, remainingCenters, moreBuilds, new LinkedList<Order>(ordersSoFar), allCombinations);
		}
		
	}
	
	private void enumerateRetreats(RetreatSituation[] allRetreats, int index, 
			Map<RetreatSituation, List<Order>> choices, List<Order> choicesMade, List<Collection<Order>> allPossibilities){
		
		if(index == allRetreats.length){
			allPossibilities.add(new LinkedList<Order>(choicesMade));
		}else{
			
			for(Order ord: choices.get(allRetreats[index])){
				
				choicesMade.add(ord);
				
				enumerateRetreats(allRetreats, index+1, choices, choicesMade, allPossibilities);
				
				choicesMade.remove(ord);
			}
		}
	}
	
	public static void main(String [] args) throws Exception
	{
		BoardConfiguration staticBoard = new BoardConfiguration();

		MoveGeneration gen = new MoveGeneration(staticBoard, null);
		BoardState bst = staticBoard.getInitialState();
		int count = 0;
		
		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		//	for each player, generate the a priori likely moves
		for(Player p: staticBoard.getPlayers()){
			orderSetsByPlayer.put(p, gen.generateOrderSets(p, bst));
		}
		
		
		//	try combinations of moves	
		
		
		for(MovesValue so: gen.generateOrderSets(staticBoard.getPlayer(Country.RUS), bst))
		{
			count++;
			
			System.out.println(Integer.toBinaryString(count));

			for(Order o: so.moves)
			{
				System.out.println(o.toOrder(bst));
			}
			System.out.println("");
		}
	}
	
}

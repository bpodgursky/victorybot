package heuristic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import order.Order;
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
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.constant.BoardConfiguration.TerritoryCoast;
import state.dynamic.BoardState;
import state.dynamic.BoardState.RetreatSituation;
import heuristic.Heuristic.MoveEnumeration;

public class NaiveMoveEnumeration extends MoveEnumeration {

	private static final int RAND_UNIT_GEN = 0;
	private static final int EXHAUSTIVE_UNIT_GEN = 1;
	
	private static final int MAX_PERMUTE_COUNT = 25;
	private static final double UNIT_MOVE_PROB = .5;

	private static final int MOVES_PER_UNIT = 5;
	
	private final Random r = new Random();


	private static final int RAND_MOVESETS_GENERATED = 3000;
	
	private static final int GENERATION_MODE = RAND_UNIT_GEN;
	
	public NaiveMoveEnumeration(Heuristic baseHeuristic) {
		super(baseHeuristic);
	}

	@Override
	public Collection<Collection<Order>> generateMoveSets(Player player, BoardState dynamicState) throws Exception{
		List<Collection<Order>> allCombinations = new LinkedList<Collection<Order>>();
		
		int unitCount = player.getNumberUnits(dynamicState);
		
		Set<TerritorySquare> unit = player.getOccupiedTerritories(dynamicState);
		
		Map<TerritorySquare, List<OrderValue>> orderPossibilities = heuristic.staticBoard.getMovesForUnits(dynamicState);
		Set<Long> seenSets = new HashSet<Long>();
		
		//TODO how to avoid this.... 
		//	2) for each player, count only to the number of units that can affect you
		
		if(GENERATION_MODE == EXHAUSTIVE_UNIT_GEN){
			int generated = 0;
			int maxPermute = (int)Math.pow(2.0, unitCount);			
			Set<Integer> generatedPermutations = new HashSet<Integer>();
			while(generated < .95*Math.min(maxPermute, MAX_PERMUTE_COUNT)){
				int toGenerate = r.nextInt(maxPermute);
				
				if(!generatedPermutations.contains(toGenerate)){
					allCombinations.addAll(generateOrderSets(r.nextInt(maxPermute), unitCount, orderPossibilities, unit, dynamicState, player));
					generated++;
				}
			}
		}else if(GENERATION_MODE == RAND_UNIT_GEN){
			int generated = 0;
			
			while(generated++ < RAND_MOVESETS_GENERATED){
				
				Collection<Order> proposed = generateAnOrderSet(orderPossibilities, dynamicState, player);
				long hash = hash(proposed);
				
				if(!seenSets.contains(hash)){
					allCombinations.add(proposed);
					seenSets.add(hash);
				}
			}
		}
		
		return allCombinations;
	}
	
	
	public Collection<Collection<Order>> generateRetreatSets(Player player, BoardState dynamicState) throws Exception{
		Collection<Collection<Order>> allCombinations = new LinkedList<Collection<Order>>();
		
		//	generate all combination of valid retreats for the player

		Set<RetreatSituation> playerRetreats = heuristic.staticBoard.getRetreatsForPlayer(dynamicState, player);
		
		Map<RetreatSituation, List<Order>> retreatOptions = 
			new HashMap<RetreatSituation, List<Order>>();
		
		for(RetreatSituation rsit: playerRetreats){
			
			//	each of the places it can retreat to
			List<Order> unitOptions = new LinkedList<Order>();
			for(TerritoryCoast tcst: heuristic.staticBoard.getRetreatsForUnit(dynamicState, rsit)){
				 unitOptions.add(new Retreat(dynamicState, player, rsit.from, tcst.sqr, tcst.coast));
			}
			
			//	and that it can disband 
			unitOptions.add(new Disband(dynamicState, player, rsit.from));
			
			retreatOptions.put(rsit, unitOptions);
		}

		enumerateRetreats(playerRetreats.toArray(new RetreatSituation[0]), 0, retreatOptions, new LinkedList<Order>(), allCombinations);
		
		//	so this is everything we can do with our retreat.  now we need to naively rank them: do so by
		//	seeing what would happen if every other retreat was a disband, and evaluating board states

		return allCombinations;
	}
	
	public Collection<Collection<Order>> generateBuildSets(Player player, BoardState bst) throws Exception{
		//	generate all bcombinations of builds for the player
		List<Collection<Order>> allCombinations = new LinkedList<Collection<Order>>();
		
		//	basically, we want every combination of build locations and units to build there
		//	this should be small enough we can enumerate them... 
	
		int equalize = heuristic.staticBoard.getRequiredBuilds(bst, player);

		if(equalize > 0){
			enumerateBuilds(bst, player, player.getHomeCenters(), equalize, new LinkedList<Order>(), allCombinations);
		}else if(equalize < 0){
			
			enumerateRemoves(bst, player, player.getOccupiedTerritories(bst), -equalize, new LinkedList<Order>(), allCombinations);
		}else{
			allCombinations.add(new LinkedList<Order>());
		}
		
		return allCombinations;
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
				
				for(TerritorySquare support: supportSet.toArray(new TerritorySquare[0]))
				{
					
					//Do we border where a move is trying to go
					if(heuristic.staticBoard.canSupportMove(bst, p, support,  supportFromPoss, suppToPoss)){
	
						Order ord = new SupportMove(bst, p, support, supportFromPoss, suppToPoss);
	
						finalMoveSet.add(ord);
						supportSetCopy.remove(support);
						
					}
				}
			}
			
			for(TerritorySquare ts:  supportSetCopy){
				supportHoldable.add(ts);
			}
			
			//	anything left in here either holds or supports holds
			for(TerritorySquare ts: supportSetCopy){

				//	find something to support hold on
				
				//	TODO this is not done intelligently.  rank the locations by 
				//	how much danger they are in, or enumerate the possibilities, or 
				//	something
				
				boolean foundSomething = false;
				for(TerritorySquare sq: supportHoldable){
					
					if(heuristic.staticBoard.canSupportHold(bst, p, ts, sq)){
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
			if(heuristic.staticBoard.canBuild(bst, p, new Unit(p, true), decide)){

				List<Order> ordersWithArmy = new LinkedList<Order>(ordersSoFar);
				ordersWithArmy.add(new Build(bst, p, new Unit(p, true), decide));
			
				enumerateBuilds(bst, p, remainingCenters, moreBuilds-1, ordersWithArmy, allCombinations);

			}

			//	fleet if can
			for(String coast: decide.getCoasts()){
				if(heuristic.staticBoard.canBuild(bst, p, new Unit(p, false), decide)){
	
					List<Order> ordersWithFleet = new LinkedList<Order>(ordersSoFar);
					ordersWithFleet.add(new Build(bst, p, new Unit(p, false), decide, coast));
	
					enumerateBuilds(bst, p, remainingCenters, moreBuilds-1, ordersWithFleet, allCombinations);
						
				}
			}
			
			//	and nothing
			enumerateBuilds(bst, p, remainingCenters, moreBuilds, new LinkedList<Order>(ordersSoFar), allCombinations);
		}
		
	}
	
	private void enumerateRetreats(RetreatSituation[] allRetreats, int index, 
			Map<RetreatSituation, List<Order>> choices, List<Order> choicesMade, Collection<Collection<Order>> allPossibilities){
		
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
	
	private long hash(Collection<Order> orders){
		
		long sum = 0;
		
		for(Order ord: orders){
			sum+=ord.hashCode2();
		}
		
		return sum;
	}
	
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

	private List<Order> generateAnOrderSet(Map<TerritorySquare, List<OrderValue>> movesForAllUnits, BoardState dynamicState, Player player) throws Exception
	{
		
		Set<TerritorySquare> occupiedTerritories = player.getOccupiedTerritories(dynamicState);
		
		Set<OrderValue> orders = new HashSet<OrderValue>();
		HashMap<Double, Order> moveScoreMap = new HashMap<Double, Order>();
		List<Order> finalOrders = new LinkedList<Order>();
		
		//Create vectors to store orders of units that will move and of units that we want to support other moves
		//Also create a HashMap between heuristic score of move and the order itself
		//Vector<Order> moveSet = new Vector<Order>();
		List<TerritorySquare> supportSet = new ArrayList<TerritorySquare>();
		
		//System.out.println("Combination:");
		for(TerritorySquare sqr: occupiedTerritories){ 
			
			//	Means we will be giving a move order
			if(r.nextDouble() < UNIT_MOVE_PROB){
				List<OrderValue> possibilities = movesForAllUnits.get(sqr);
			
				if(possibilities.size() == 0){
					throw new Exception("no possibilities for unit "+sqr.toString(dynamicState)+" player "+player);
				}
				
				//	choose a random territory it can move to
				OrderValue ov = possibilities.get(r.nextInt(possibilities.size()));
				
				orders.add(ov);
				
				//System.out.println(ov.order.toOrder(dynamicState));
			}else{
				supportSet.add(sqr);
			}
		}
		
		//	added the rand because if two orders have the same score, 
		//	the second one will obliterate the first in the map
		for(OrderValue ov: orders){
			Double score = ov.score + r.nextDouble()/10000.0;
			moveScoreMap.put(score, ov.order);
		}
		
		//Sort heuristic scores and now lets step through remaining units and try to support our moves
		Double [] orderScore = (moveScoreMap.keySet().toArray(new Double[0]));
		Arrays.sort(orderScore);
		
		List<TerritorySquare> supportHoldable = new LinkedList<TerritorySquare>(supportSet);
		
		for(Double heurVal: orderScore){
			
			Move supportedMove = (Move)moveScoreMap.get(heurVal);
			
			TerritorySquare suppToPoss = supportedMove.to;
			TerritorySquare supportFromPoss = supportedMove.from;
			
			for(TerritorySquare support: supportSet.toArray(new TerritorySquare[0]))
			{
				
				//Do we border where a move is trying to go
				if(heuristic.staticBoard.canSupportMove(dynamicState, player, support,  supportFromPoss, suppToPoss)){

					Order ord = new SupportMove(dynamicState, player, support, supportFromPoss, suppToPoss);

					finalOrders.add(ord);
					supportSet.remove(support);
					
				}
			}
		}
		
		for(TerritorySquare ts:  supportSet){
			supportHoldable.add(ts);
		}
		
		//	anything left in here either holds or supports holds
		for(TerritorySquare ts: supportSet){
			
			//	find something to support hold on
			
			//	TODO this is not done intelligently.  rank the locations by 
			//	how much danger they are in, or enumerate the possibilities, or 
			//	something
			
			boolean foundSomething = false;
			for(TerritorySquare sq: supportHoldable){
				
				if(heuristic.staticBoard.canSupportHold(dynamicState, player, ts, sq)){
					finalOrders.add(new SupportHold(dynamicState, player, ts, sq));
					
					foundSomething = true;
					break;
				}	
			}
			
			if(!foundSomething){
				finalOrders.add(new Hold(dynamicState, player, ts));
			}	
		}

		for(OrderValue ov: orders) {
			finalOrders.add(ov.order);
		}
		
		if(finalOrders.size() != player.getOccupiedTerritories(dynamicState).size()){
			
			for(Order ov: finalOrders){
				System.out.println("\t"+ov.toOrder(dynamicState));
			}
			for(TerritorySquare terr: player.getOccupiedTerritories(dynamicState)){
				System.out.println("\t"+terr.getName());
			}
			
			System.out.println("Support set: ");
			for(TerritorySquare terr: supportSet){
				System.out.println("\t"+terr.getName());
			}
			
			throw new Exception("need "+player.getOccupiedTerritories(dynamicState).size()+ " moves, got "+finalOrders.size());
		}

		return finalOrders;
	}




}

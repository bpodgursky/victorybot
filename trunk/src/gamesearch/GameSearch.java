package gamesearch;


import gamesearch.MoveGeneration.MovesValue;
import heuristic.Heuristic;
import heuristic.NaiveHeuristic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import order.Order;
import order.builds.Build;
import order.builds.Remove;
import order.builds.Waive;
import order.retreats.Disband;
import order.retreats.Retreat;
import order.spring_fall.Hold;
import order.spring_fall.SupportHold;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.constant.BoardConfiguration;
import state.constant.BoardConfiguration.TerritoryCoast;
import state.constant.BoardConfiguration.YearPhase;
import state.dynamic.BeliefState;
import state.dynamic.BoardState;
import state.dynamic.DiplomaticState;
import state.dynamic.BoardState.Phase;
import state.dynamic.BoardState.RetreatSituation;

//	gamesearch runs in a separate thread.  call methods on it to notify it of things--
// 	updated state, diplomatic changes, etc
public class GameSearch {

	private static final int MAX_PLAYER_MOVES = 50;
	
	//	keep this updated with the current best guess at orders
	private Collection<Order> currentOrders = new HashSet<Order>();
	
	//	use these as base for search, heuristic for search, and to inform
	//	move success probabilities, respectively
	private final BoardConfiguration boardConfiguration;
	private final DiplomaticState dipState;
	private final BeliefState beliefState;
	
	private BoardState boardState;
	
	private final Thread internalSearch;
	private final InternalSearch searcher;
	
	private final Player relevantPlayer;
	
	private boolean boardUpdate;
	private boolean dipUpdate;
	private boolean beliefUpdate;
	
	private boolean movesReady;
	
	private final Heuristic heuristic;
	
	private final MoveGeneration gen;
	
	public GameSearch(Player player, BoardConfiguration state, DiplomaticState dipState, BeliefState beliefState){

		this.relevantPlayer = player;
		
		this.gen = new MoveGeneration(state, relevantPlayer);
		
		this.searcher = new InternalSearch();
		this.internalSearch = new Thread(searcher);
		this.internalSearch.start();
		
		this.boardConfiguration = state;
		this.dipState = dipState;
		this.beliefState = beliefState;
		
		this.heuristic = new NaiveHeuristic(boardConfiguration);
		
		boardState = state.getInitialState();
	}
	
	//	methods to communicate to the search
	//	TODO probably should have more complex ways to communicate in here
	
	public void noteBoardUpdate(BoardState bst){
		
		this.boardState = bst;
		this.movesReady = false;
		this.boardUpdate = true;

		
		internalSearch.interrupt();
	}
	
	public void noteDiplomaticUpdate(){
		this.dipUpdate = true;
		
		internalSearch.interrupt();
	}
	
	public void noteBeliefUpdate(){
		
		this.currentOrders = new HashSet<Order>();
		this.movesReady = false;
		
		this.beliefUpdate = true;
		
		internalSearch.interrupt();
	}
	
	public boolean isReady(){
		return this.movesReady;
	}
	
	
	public Collection<Order> currentOrders(){
		return currentOrders;
	}
	
	//	base case of search.  Returns the best set of moves for us
	private Collection<Order> moveSearch(BoardState bst, YearPhase until) throws Exception{
		
		//	build sets of all moves for all relevant players
	
		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		
		Set<Player> relevantPlayers = boardConfiguration.getRelevantPlayers(bst, relevantPlayer);
		
		System.out.println("Building possible order sets for players: ");
		//	for each player, generate the a priori likely moves
		List<Player> otherPlayers = new LinkedList<Player>();
		for(Player p: boardConfiguration.getPlayers()){
			
			System.out.println("\t"+p.getName()+"...");
			if(this.boardUpdate){
				System.out.println("Took too long, quitting in generation...");
				return null;
			}
			
			if(relevantPlayers.contains(p)){
				orderSetsByPlayer.put(p, gen.generateOrderSets(p, bst));
			}else{
				
				MovesValue[] hold = new MovesValue[1];
				hold[0] = new MovesValue(boardConfiguration.generateDefaultOrdersFor(bst, p), -1);
				
				orderSetsByPlayer.put(p, hold);
			}
			if(p != this.relevantPlayer){
				otherPlayers.add(p);
			}
			
			System.out.println("\t"+p.getName()+" done");
		}
		
		System.out.println("Generated orders for each player:");
		for(Player p: orderSetsByPlayer.keySet()){
			System.out.println("\t"+orderSetsByPlayer.get(p).length);
		}
		
		System.out.println("Enumerating combinations for our "+ orderSetsByPlayer.get(relevantPlayer).length+ " moves..");
		Player[] playerArray = otherPlayers.toArray(new Player[0]);	
		MovesValue [] maxSetMoves = new MovesValue[orderSetsByPlayer.get(relevantPlayer).length];
		int count = 0;
		MovesValue bestMoveSoFar = null;
		
		for(MovesValue playerOrds: orderSetsByPlayer.get(relevantPlayer)){
			
			if(this.boardUpdate){
				System.out.println("Took too long, quitting...");
				return null;
			}
			
			List<Collection<Order>> orderList = new LinkedList<Collection<Order>>();
			orderList.add(playerOrds.moves);
		
			maxSetMoves[count] = new MovesValue(playerOrds.moves, min(bst, until, playerOrds.moves, orderSetsByPlayer, playerArray));
			if(bestMoveSoFar == null)
			{
				bestMoveSoFar = maxSetMoves[count];
				currentOrders = bestMoveSoFar.moves;
			}
			if(bestMoveSoFar.value < maxSetMoves[count].value)
			{
				bestMoveSoFar = maxSetMoves[count];
				currentOrders = bestMoveSoFar.moves;
			}
			count++;
			
			if(count % 1 == 0) System.out.println("\t"+count+" processed...");
		}
		System.out.println("Done enumerating");
		
		Arrays.sort(maxSetMoves);
		
		//	for each of our moves, find with min the worst outcome from that move.
		//	return the set of moves with the highest associated min
		if(maxSetMoves.length == 0){
			return new HashSet<Order>();
		}
			
		return maxSetMoves[0].moves;
		
	}
	
	//	for the moves that we have made (friendlyOrders), what is the worst possible board 
	//	that can result from it.  We want to return the quality of that board
	private double min(BoardState bst, YearPhase until, Collection<Order> friendlyOrders,
			Map<Player, MovesValue []> orderSetsByPlayer, Player [] playerArray) throws Exception{
		
		//System.out.println("\tmin entered until "+until+"...");
		//System.out.println("\tboard is "+bst.time+"...");
		if(bst.time.isAfter(until)) {
			//System.out.println("\tquitting");
			return heuristic.boardScore(relevantPlayer, bst);
		}
		
		//	recurse through all enemy combinations (cap for each player)	
		
		List<Collection<Order>> orderList = new LinkedList<Collection<Order>>();
		orderList.add(friendlyOrders);
		
		List<List<Collection<Order>>> allMoveSets = enumerateMoves(bst, orderList, orderSetsByPlayer, playerArray, 0);
		
		MovesValue [] moveScores = new MovesValue[allMoveSets.size()];
		int count = 0;
		for(List<Collection<Order>> fullMoveSet: allMoveSets)
		{
			Set<Order> toExecute = new HashSet<Order>();
			
			for(Collection<Order> execute: fullMoveSet){	
				toExecute.addAll(execute);
			}
			BoardState updatedState = boardConfiguration.update(bst.time.next(), bst, toExecute, false);
			moveScores[count] = new MovesValue(toExecute, max(updatedState, until));
		
			count++;
		}
		//System.out.println("\tdone...");
		//		base case of recursion, have a set of moves for each enemy player
		//			combine with the friendlyOrders above to make a full set of orders
		//			apply to gamestate, call max on this boardstate
		
		//	return the minimum board quality over all combinations
		Arrays.sort(moveScores);
		return moveScores[moveScores.length-1].value;
	}
	
	private double max(BoardState bst, YearPhase until) throws Exception{
		
		// if bst's date is after until, return the quality of the board

		if(bst.time.isAfter(until)) {
			return heuristic.boardScore(relevantPlayer, bst);
		}
		
		//	otherwise,
		
		//	build sets of all moves for all relevant players based on this bst

		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		
		Set<Player> relevantPlayers = boardConfiguration.getRelevantPlayers(bst, relevantPlayer);

		//	for each player, generate the a priori likely moves
		List<Player> otherPlayers = new LinkedList<Player>();
		for(Player p: boardConfiguration.getPlayers()){
			
			if(relevantPlayers.contains(p)){
				orderSetsByPlayer.put(p, gen.generateOrderSets(p, bst));
			}else{
				
				MovesValue[] hold = new MovesValue[1];
				hold[0] = new MovesValue(boardConfiguration.generateDefaultOrdersFor(bst, p), -1);
				
				orderSetsByPlayer.put(p, hold);
			}
			if(p != this.relevantPlayer){
				otherPlayers.add(p);
			}
		}
		
		//	for each of our moves, find with min the worst outcome from that move.
		//	return the maximum min found
		
		Player[] playerArray = otherPlayers.toArray(new Player[0]);	
		MovesValue [] maxSetMoves = new MovesValue[orderSetsByPlayer.get(relevantPlayer).length];
		int count = 0;
		
		//System.out.println("\tIn max, have "+maxSetMoves.length+" to look at... ");
		for(MovesValue playerOrds: orderSetsByPlayer.get(relevantPlayer)){
			
			List<Collection<Order>> orderList = new LinkedList<Collection<Order>>();
			orderList.add(playerOrds.moves);
			
			//int year = bst.time.year;
			//Phase phase = bst.time.phase == Phase.SPR ? Phase.SUM : Phase.WIN;
			//YearPhase until = new YearPhase(year, phase);
			maxSetMoves[count] = new MovesValue(playerOrds.moves, min(bst, until, playerOrds.moves, orderSetsByPlayer, playerArray));
			count++;
		}
		
		Arrays.sort(maxSetMoves);
		
		//	for each of our moves, find with min the worst outcome from that move.
		//	return the set of moves with the highest associated min
		return maxSetMoves[0].value;
		
		
	}
	
	//	how many moves to enumerate for each player.  hardcode for now
	private static final int MAX_ENUM = 4;
	
	private List<List<Collection<Order>>> enumerateMoves(BoardState bst, 
			List<Collection<Order>> allOrders, 
			Map<Player, MovesValue[]> playerOrders, 
			Player[] players, 
			int player) throws Exception{
		
		List<List<Collection<Order>>> playerEnumeration = new LinkedList<List<Collection<Order>>>();
		
		if(player == players.length){

			playerEnumeration.add(new LinkedList<Collection<Order>>(allOrders));
		}
		
		else if(players[player] == relevantPlayer){
			playerEnumeration.addAll(enumerateMoves(bst, allOrders, playerOrders, players, player+1));
		}
		else{
			
			MovesValue[] movesForPlayer = playerOrders.get(players[player]);
			
			for(int i = 0; i < movesForPlayer.length && i < MAX_ENUM; i++){			
				MovesValue mv = movesForPlayer[i]; 
				
				allOrders.add(mv.moves);
			
				playerEnumeration.addAll(enumerateMoves(bst, allOrders, playerOrders, players, player+1));
			
				allOrders.remove(mv.moves);
								
			}
		}
		return playerEnumeration;
	}
	
	Random r = new Random();
	
	
	private class InternalSearch implements Runnable{
		
		public final void run(){
			try{
				while(true){
					process();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void process() throws Exception{
			if(boardUpdate){
				boardUpdate = false;
				
				currentOrders = new HashSet<Order>();
				Set<Order> orders = new HashSet<Order>();
				
				//	movement turn
				if( boardState.time.phase == Phase.SPR || 
					boardState.time.phase == Phase.FAL){
					
					System.out.println("Starting to process movements...");
					
					//	put down something dumb at first so we have orders at least
					
					//	get all occupied territories
					Set<TerritorySquare> unitSquares = relevantPlayer.getOccupiedTerritories(boardState);

					
					Set<TerritorySquare> supported = new HashSet<TerritorySquare>();
					
					for(TerritorySquare ts: unitSquares){

						//	find something to support hold on
						
						Set<TerritorySquare> supportable = 
							boardConfiguration.getSupportableTerritories(boardState, relevantPlayer, ts, true);
						
						
						boolean foundOriginal = false;
						for(TerritorySquare neighbor: supportable){
							if(!supported.contains(neighbor)){

								orders.add(new SupportHold(boardState, relevantPlayer, ts, neighbor));
								supported.add(neighbor);
								
								foundOriginal = true;
								
								break;
							}
						}
						
						if(!foundOriginal && supportable.size() > 0){
							
							TerritorySquare target = supportable.iterator().next();
					
							orders.add(new SupportHold(boardState, relevantPlayer, ts, target));
						}else if(!foundOriginal){
						
							orders.add(new Hold(boardState, relevantPlayer, ts));
						}
					}
					
					System.out.println("Done with basic search, starting intelligent search...");
					
					currentOrders = orders;
					int year = boardState.time.year;
					
					//	too slow for now.  don't consider winter for now
					//Phase phase = boardState.time.phase == Phase.SPR ? Phase.SUM : Phase.WIN;
					Phase phase = boardState.time.phase == Phase.SPR ? Phase.SUM : Phase.AUT;
					
					YearPhase until = new YearPhase(year, phase);
					currentOrders = moveSearch(boardState, until);
					
				}
				
				//	retreat time
				else if(boardState.time.phase == Phase.SUM ||
						 boardState.time.phase == Phase.AUT){
					
					System.out.println("Starting to process retreat...");
					
					//	just a dumb first guess
					
					Collection<RetreatSituation> needRetreats = boardConfiguration.getRetreatsForPlayer(boardState, relevantPlayer);
					Set<TerritorySquare> takenRetreats = new HashSet<TerritorySquare>();
					
					for(RetreatSituation rsit: needRetreats){
						
						List<TerritoryCoast> options = boardConfiguration.getRetreatsForUnit(boardState, rsit);
						
						boolean foundRetreat = false;
						for(TerritoryCoast tcoast: options){
							
							if(!takenRetreats.contains(tcoast.sqr)){
								takenRetreats.add(tcoast.sqr);
								
								orders.add(new Retreat(boardState, relevantPlayer, rsit.from, tcoast.sqr, tcoast.coast));
								
								foundRetreat = true;
								
								break;
							}
						}
						
						if(!foundRetreat){
							orders.add(new Disband(boardState, relevantPlayer, rsit.from));
						}
					}
					
					currentOrders = orders;
					
					int year = boardState.time.year;
					
					Phase phase = boardState.time.phase == Phase.SUM ? Phase.FAL : Phase.WIN;
					
					YearPhase until = new YearPhase(year, phase);
					currentOrders = moveSearch(boardState, until);
					
					System.out.println("Retreat order submitted.");
				}
				
				//	build/disband time 
				else if(boardState.time.phase == Phase.WIN){
					
					System.out.println("Starting to process builds...");
					
					int equalize = boardConfiguration.getRequiredBuilds(boardState, relevantPlayer);
					
					if(equalize > 0){
						
						Collection<TerritorySquare> possibleBuilds = boardConfiguration.getPossibleBuilds(boardState, relevantPlayer);
					
						for(TerritorySquare poss: possibleBuilds){
							
							if(poss.hasAnySeaBorders()){
								
								if(r.nextBoolean()){
									orders.add(new Build(boardState, relevantPlayer, new Unit(relevantPlayer, true), poss));
								}else{
									orders.add(new Build(boardState, relevantPlayer, new Unit(relevantPlayer, false), poss, poss.getCoasts().iterator().next()));
								}
								
							}else{
								orders.add(new Build(boardState, relevantPlayer, new Unit(relevantPlayer, true), poss));
							}
								
							equalize--;
							
							if(equalize == 0) break;
						}
					}else if(equalize < 0){
						
						Collection<TerritorySquare> allUnits = relevantPlayer.getOccupiedTerritories(boardState);
						
						//	it's winter, so if we have fewer builds than units, we have a unit somewhere that is not a 
						//	supply center.  kill it
						
						for(TerritorySquare tsquare: allUnits){
							
							if(!tsquare.isSupplyCenter()){
								orders.add(new Remove(boardState, relevantPlayer, tsquare));
								
								equalize--;
								
								if(equalize == 0) break;
							}	
						}
					}
					
					//	we have to waive if we can't build more
					if(equalize > 0){
						for(int i = 0; i < equalize; i++){
							orders.add(new Waive(boardState, relevantPlayer));
						}
					}
					
					//TODO intelligent reasoning about what and where to build--compare quality
					//	of states
					
					currentOrders = orders;
					
					int year = boardState.time.year;
					
					Phase phase = Phase.WIN;
					
					YearPhase until = new YearPhase(year, phase);
					currentOrders = moveSearch(boardState, until);
					
					System.out.println("Build orders submitted");
				}
				
				if(!boardUpdate){
					System.out.println("Done with move search!");
					System.out.println("Moves will be: ");
					for(Order ord: currentOrders){
						System.out.println("\t"+ord.toOrder(boardState));
					}

					movesReady = true;
				}
				

			}
		}
	}
}

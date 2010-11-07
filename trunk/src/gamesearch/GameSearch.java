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
	private Set<Order> currentOrders = new HashSet<Order>();
	
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
	
	private final Heuristic heuristic;
	
	public GameSearch(Player player, BoardConfiguration state, DiplomaticState dipState, BeliefState beliefState){

		this.relevantPlayer = player;
		
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
		this.boardUpdate = true;
		
		internalSearch.interrupt();
	}
	
	public void noteDiplomaticUpdate(){
		this.dipUpdate = true;
		
		internalSearch.interrupt();
	}
	
	public void noteBeliefUpdate(){
		this.beliefUpdate = true;
		
		internalSearch.interrupt();
	}
	
	
	public Set<Order> currentOrders(){
		return currentOrders;
	}
	
	//	base case of search.  Returns the best set of moves for us
	private Set<Order> moveSearch(BoardState bst, YearPhase until) throws Exception{
		
		//	build sets of all moves for all relevant players
		
		MoveGeneration gen = new MoveGeneration(boardConfiguration);

		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		
		Set<Player> relevantPlayers = boardConfiguration.getRelevantPlayers(bst, relevantPlayer);
		
		System.out.println("This state, we "+relevantPlayer.getName()+" only care about players: ");
		System.out.println("\t"+relevantPlayers);
		
		//	for each player, generate the a priori likely moves
		List<Player> otherPlayers = new LinkedList<Player>();
		for(Player p: boardConfiguration.getPlayers()){
			
			if(relevantPlayers.contains(p)){
				orderSetsByPlayer.put(p, gen.generateOrderSets(p, bst));
			}else{
				
				MovesValue[] hold = new MovesValue[1];
				hold[0] = new MovesValue(boardConfiguration.generateHoldsFor(bst, p), -1);
				
				orderSetsByPlayer.put(p, hold);
			}
			if(p != this.relevantPlayer){
				otherPlayers.add(p);
			}
		}
		
		Player[] playerArray = otherPlayers.toArray(new Player[0]);	
		MovesValue [] maxSetMoves = new MovesValue[MAX_PLAYER_MOVES];
		int count = 0;
		MovesValue bestMoveSoFar = null;
		
		for(MovesValue playerOrds: orderSetsByPlayer.get(relevantPlayer)){
			
			System.out.println("Looking at "+playerOrds);
			
			List<Set<Order>> orderList = new LinkedList<Set<Order>>();
			orderList.add(playerOrds.moves);
			
			List<Double> scores = new ArrayList<Double>();

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
		}
		
		Arrays.sort(maxSetMoves);
		
		//	for each of our moves, find with min the worst outcome from that move.
		//	return the set of moves with the highest associated min
		return maxSetMoves[0].moves;
	}
	
	//	for the moves that we have made (friendlyOrders), what is the worst possible board 
	//	that can result from it.  We want to return the quality of that board
	private double min(BoardState bst, YearPhase until, Set<Order> friendlyOrders,
			Map<Player, MovesValue []> orderSetsByPlayer, Player [] playerArray) throws Exception{
		
		if(until.isAfter(bst.time))
		{
			return heuristic.boardScore(relevantPlayer, bst);
		}
		
		//	recurse through all enemy combinations (cap for each player)	
		
		
		List<Set<Order>> orderList = new LinkedList<Set<Order>>();
		orderList.add(friendlyOrders);
		
		List<List<Set<Order>>> allMoveSets = enumerateMoves(bst, orderList, orderSetsByPlayer, playerArray, 0);
		MovesValue [] moveScores = new MovesValue[allMoveSets.size()];
		int count = 0;
		for(List<Set<Order>> fullMoveSet: allMoveSets)
		{
			Set<Order> toExecute = new HashSet<Order>();
			
			for(Set<Order> execute: fullMoveSet){	
				toExecute.addAll(execute);
			}
			BoardState updatedState = boardConfiguration.update(bst.time, bst, toExecute, false);
			moveScores[count] = new MovesValue(toExecute, max(updatedState, updatedState.time));
		}
		//		base case of recursion, have a set of moves for each enemy player
		//			combine with the friendlyOrders above to make a full set of orders
		//			apply to gamestate, call max on this boardstate
		
		//	return the minimum board quality over all combinations
		Arrays.sort(moveScores);
		return moveScores[moveScores.length].value;
	}
	
	private double max(BoardState bst, YearPhase until) throws Exception{
		
		// if bst's date is after until, return the quality of the board
		if(until.isAfter(bst.time))
		{
			return heuristic.boardScore(relevantPlayer, bst);
		}
		
		
		
		
		//	otherwise,
		
		//	build sets of all moves for all relevant players based on this bst
		
		MoveGeneration gen = new MoveGeneration(boardConfiguration);

		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		
		Set<Player> relevantPlayers = boardConfiguration.getRelevantPlayers(bst, relevantPlayer);
		
		System.out.println("This state, we "+relevantPlayer.getName()+" only care about players: ");
		System.out.println("\t"+relevantPlayers);
		
		//	for each player, generate the a priori likely moves
		List<Player> otherPlayers = new LinkedList<Player>();
		for(Player p: boardConfiguration.getPlayers()){
			
			if(relevantPlayers.contains(p)){
				orderSetsByPlayer.put(p, gen.generateOrderSets(p, bst));
			}else{
				
				MovesValue[] hold = new MovesValue[1];
				hold[0] = new MovesValue(boardConfiguration.generateHoldsFor(bst, p), -1);
				
				orderSetsByPlayer.put(p, hold);
			}
			if(p != this.relevantPlayer){
				otherPlayers.add(p);
			}
		}
		
		//	for each of our moves, find with min the worst outcome from that move.
		//	return the maximum min found
		
		Player[] playerArray = otherPlayers.toArray(new Player[0]);	
		MovesValue [] maxSetMoves = new MovesValue[MAX_PLAYER_MOVES];
		int count = 0;
		
		for(MovesValue playerOrds: orderSetsByPlayer.get(relevantPlayer)){
			
			System.out.println("Looking at "+playerOrds);
			
			List<Set<Order>> orderList = new LinkedList<Set<Order>>();
			orderList.add(playerOrds.moves);
			
			List<Double> scores = new ArrayList<Double>();
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
	
	
	
	/*private Set<Order> moveSearch(BoardState bst) throws Exception{
		//	TODO this is temporarily just a one level search

		
		MoveGeneration gen = new MoveGeneration(boardConfiguration);

		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		
		Set<Player> relevantPlayers = boardConfiguration.getRelevantPlayers(bst, relevantPlayer);
		
		System.out.println("This state, we "+relevantPlayer.getName()+" only care about players: ");
		System.out.println("\t"+relevantPlayers);
		
		//	for each player, generate the a priori likely moves
		List<Player> otherPlayers = new LinkedList<Player>();
		for(Player p: boardConfiguration.getPlayers()){
			
			if(relevantPlayers.contains(p)){
				orderSetsByPlayer.put(p, gen.generateOrderSets(p, bst));
			}else{
				
				MovesValue[] hold = new MovesValue[1];
				hold[0] = new MovesValue(boardConfiguration.generateHoldsFor(bst, p), -1);
				
				orderSetsByPlayer.put(p, hold);
			}
			
			if(p != this.relevantPlayer){
				otherPlayers.add(p);
			}
		}
		
		System.out.println("Possible moves for players calculated: ");
		for(Player p: orderSetsByPlayer.keySet()){
			System.out.println("\t"+p.getName()+"\t"+orderSetsByPlayer.get(p).length);
		}
		
		//	try combinations of moves
		
		Player[] playerArray = otherPlayers.toArray(new Player[0]);		

		Set<Order> bestOrders = null;
		double bestScore = 0;
		
		System.out.println("Processing minimax...");
		for(MovesValue playerOrds: orderSetsByPlayer.get(relevantPlayer)){
			
			//	then we've taken too long.  get out of here and start over
			if(boardUpdate){
				return null;
			}
			
			
			List<Set<Order>> orderList = new LinkedList<Set<Order>>();
			orderList.add(playerOrds.moves);
			
			List<Double> scores = new ArrayList<Double>();
			
			enumerateMoves(bst, orderList, orderSetsByPlayer, playerArray, 0);
			
			//TODO hacky hacky hacky minimax hack make this whole algorithm clear
			Double minScore = scores.get(0);
			for(Double d: scores){
				if(d < minScore){
					minScore = d;
				}
			}
			
			if(bestOrders == null || minScore > bestScore){
				bestOrders = playerOrds.moves;
				bestScore = minScore;
				
				currentOrders = bestOrders;
			}
			
		}
		
		return bestOrders;
	}*/
	
	//	how many moves to enumerate for each player.  hardcode for now
	private static final int MAX_ENUM = 5;
	
	private List<List<Set<Order>>> enumerateMoves(BoardState bst, 
			List<Set<Order>> allOrders, 
			Map<Player, MovesValue[]> playerOrders, 
			Player[] players, 
			int player) throws Exception{
		
		List<List<Set<Order>>> playerEnumeration = new LinkedList<List<Set<Order>>>();
		
		if(player == players.length){
			// execute, evaluate quality
			
			//Set<Order> toExecute = new HashSet<Order>();
			
			//for(Set<Order> execute: allOrders){
				
			//	toExecute.addAll(execute);
			//}
			
			//BoardState executed = boardConfiguration.update(bst.time.next(), bst, toExecute, false);
			
			//double stateScore = heuristic.boardScore(relevantPlayer, executed);
			
			//scores.add(stateScore);
			playerEnumeration.add(allOrders);
		}
		
		else if(players[player] == relevantPlayer){
			enumerateMoves(bst, allOrders, playerOrders, players, player+1);
		}
		else{
			
			MovesValue[] movesForPlayer = playerOrders.get(players[player]);
			
			for(int i = 0; i < movesForPlayer.length && i < MAX_ENUM; i++){			
				MovesValue mv = movesForPlayer[i]; 
				
				allOrders.add(mv.moves);
			
				enumerateMoves(bst, allOrders, playerOrders, players, player+1);
			
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
							
							currentOrders = orders;
							
							YearPhase until;
							until = new YearPhase(boardState.time.year,
									boardState.time.phase == Phase.SPR ? Phase.SUM : Phase.WIN);
							
							moveSearch(boardState, until);
							
							System.out.println("Done with move search!");
							System.out.println("Moves will be: ");
							for(Order ord: currentOrders){
								System.out.println("\t"+ord.toOrder(boardState));
							}
						}
						
						
					}
					
					//TODO intelligent search
					
					currentOrders = orders;
					int year = boardState.time.year;
					Phase phase = boardState.time.phase == Phase.SPR ? Phase.SUM : Phase.WIN;
					YearPhase until = new YearPhase(year, phase);
					moveSearch(boardState, until);
					
					System.out.println("Done with move search!");
					System.out.println("Moves will be: ");
					for(Order ord: currentOrders){
						System.out.println("\t"+ord.toOrder(boardState));
					}
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
					
					//TODO intelligent search
					
					currentOrders = orders;
					
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
					
					//TODO intelligent reasoning about what and where to build--compare quality
					//	of states
					
					currentOrders = orders;
					
					System.out.println("Build orders submitted");
				}
			}
		}
	}
}

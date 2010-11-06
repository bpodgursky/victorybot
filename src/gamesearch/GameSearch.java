package gamesearch;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;

import gamesearch.MoveGeneration.MovesValue;
import heuristic.Heuristic;
import heuristic.NaiveHeuristic;

import order.Order;
import order.builds.Build;
import order.builds.Remove;
import order.retreats.Disband;
import order.retreats.Retreat;
import order.spring_fall.Hold;
import order.spring_fall.SupportHold;

import java.util.HashSet;

import representation.Country;
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
	}
	
	public void noteDiplomaticUpdate(){
		this.dipUpdate = true;
	}
	
	public void noteBeliefUpdate(){
		this.beliefUpdate = true;
	}
	
	
	public Set<Order> currentOrders(){
		return currentOrders;
	}
	
	
	private Set<Order> moveSearch(BoardState bst) throws Exception{
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
		
		System.out.println("Possible moves calculated: ");
		for(Player p: orderSetsByPlayer.keySet()){
			System.out.println("\t"+p.getName()+"\t"+orderSetsByPlayer.get(p).length);
		}
		
		//	try combinations of moves
		
		Player[] playerArray = otherPlayers.toArray(new Player[0]);		

		Set<Order> bestOrders = null;
		double bestScore = 0;
		
		for(MovesValue playerOrds: orderSetsByPlayer.get(relevantPlayer)){
			
			System.out.println("Looking at "+playerOrds);
			
			List<Set<Order>> orderList = new LinkedList<Set<Order>>();
			orderList.add(playerOrds.moves);
			
			List<Double> scores = new ArrayList<Double>();
			
			enumerateMoves(bst, orderList, orderSetsByPlayer, playerArray, 0, scores);
			
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
	}
	
	//	how many moves to enumerate for each player.  hardcode for now
	private static final int MAX_ENUM = 5;
	
	private void enumerateMoves(BoardState bst, List<Set<Order>> allOrders, Map<Player, MovesValue[]> playerOrders, Player[] players, int player, List<Double> scores) throws Exception{
		
		if(player == players.length){
			// execute, evaluate quality
			
			Set<Order> toExecute = new HashSet<Order>();
			
			for(Set<Order> execute: allOrders){
				
				toExecute.addAll(execute);
			}
			
			BoardState executed = boardConfiguration.update(bst.time.next(), bst, toExecute, false);
			
			double stateScore = heuristic.boardScore(relevantPlayer, executed);
			
			scores.add(stateScore);
		}
		
		else if(players[player] == relevantPlayer){
			enumerateMoves(bst, allOrders, playerOrders, players, player+1, scores);
		}
		else{
			
			MovesValue[] movesForPlayer = playerOrders.get(players[player]);
			
			for(int i = 0; i < movesForPlayer.length && i < MAX_ENUM; i++){			
				MovesValue mv = movesForPlayer[i]; 
				
				int initSize = allOrders.size();
				
				allOrders.add(mv.moves);
			
				enumerateMoves(bst, allOrders, playerOrders, players, player+1, scores);
			
				allOrders.remove(mv.moves);
				
				int endSize = allOrders.size();
				
				if(initSize != endSize) throw new Exception("orders size before "+initSize+" now "+endSize);
			}
		}
	}
	
	Random r = new Random();
	
	
	private class InternalSearch implements Runnable{
		
		//TODO this doesn't do anything especially useful yet
		//	it just puts out hold orders
		
		public final void run(){
			try {
				while(true){
					if(boardUpdate){
						boardUpdate = false;
						
						currentOrders = new HashSet<Order>();
						Set<Order> orders = new HashSet<Order>();
						
						//	movement turn
						if( boardState.time.phase == Phase.SPR || 
							boardState.time.phase == Phase.FAL){
							
							//	put down something dumb at first so we have orders at least
							
							//	get all occupied territories
							Set<TerritorySquare> unitSquares = relevantPlayer.getOccupiedTerritories(boardState);

							
							Set<TerritorySquare> supported = new HashSet<TerritorySquare>();
							
							for(TerritorySquare ts: unitSquares){

								//	find something to support hold on
								
								Set<TerritorySquare> supportable = 
									boardConfiguration.getSupportableTerritories(boardState, relevantPlayer, ts, true);
								
								for(TerritorySquare sqr: supportable){
									System.out.println("\t"+sqr.getName());
								}
								
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
							
							//TODO intelligent search
							
							currentOrders = orders;
							
							moveSearch(boardState);
							
							System.out.println("Done with move search!");
							System.out.println("Moves will be: ");
							for(Order ord: currentOrders){
								System.out.println("\t"+ord.toOrder(boardState));
							}
						}
						
						//	retreat time
						else if(boardState.time.phase == Phase.SUM ||
								 boardState.time.phase == Phase.AUT){
							
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
						}
						
						//	build/disband time 
						else if(boardState.time.phase == Phase.WIN){
							
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
							
						}
						
					
						
						
						
					}else{
						Thread.sleep(10);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

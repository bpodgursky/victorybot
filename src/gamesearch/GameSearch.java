package gamesearch;

import heuristic.FactorizedPruner;
import heuristic.Heuristic;
import heuristic.NaiveMoveEnumeration;
import heuristic.NaivePruner;
import heuristic.NaiveRelevance;
import heuristic.NaiveScorer;

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

import ai.Bot;

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
public abstract class GameSearch {

	//	keep this updated with the current best guess at orders
	protected Collection<Order> currentOrders = new HashSet<Order>();
	
	//	use these as base for search, heuristic for search, and to inform
	//	move success probabilities, respectively
	protected final BoardConfiguration boardConfiguration;
	protected final DiplomaticState dipState;
	protected final BeliefState beliefState;
	
	protected BoardState boardState;
	
	private final Thread internalSearch;
	private final InternalSearch searcher;
	
	protected Player relevantPlayer;
	
	protected boolean boardUpdate;
	protected boolean dipUpdate;
	protected boolean beliefUpdate;
	
	protected boolean movesReady;
	
	protected final Heuristic heuristic;
	
	protected final MoveGenerator gen;
	
	public GameSearch(Heuristic h, BoardConfiguration state, DiplomaticState dipState, BeliefState beliefState){
		
		this.boardConfiguration = state;
		this.dipState = dipState;
		this.beliefState = beliefState;
		
		
		this.heuristic = h;
		
//		this.heuristic = new Heuristic(boardConfiguration);
		
//		this.heuristic.setMovePruningHeuristic(new FactorizedPruner(heuristic));
//		this.heuristic.setOrderGenerationHeuristic(new NaiveMoveEnumeration(heuristic));
//		this.heuristic.setRelevanceHeuristic(new NaiveRelevance(heuristic));
//		this.heuristic.setScoreHeuristic(new NaiveScorer(heuristic));
		
		this.gen = new MoveGenerator(state, heuristic);
		
		this.searcher = new InternalSearch();
		this.internalSearch = new Thread(searcher);
		this.internalSearch.start();
		


		
		boardState = state.getInitialState();
	}
	
	public Player getPlayer(){
		return this.relevantPlayer;
	}
	
	protected abstract Collection<Order> moveSearch(BoardState bst, YearPhase until) throws Exception;
	
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

	
	public void setPlayer(Player p){
		this.relevantPlayer = p;
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
					
					if(Bot.LOGGING) System.out.println("Starting to process movements...");
					
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
					
					if(Bot.LOGGING) System.out.println("Done with basic search, starting intelligent search...");
					
					currentOrders = orders;
					int year = boardState.time.year;
					
					//	too slow for now.  don't consider winter for now
					//Phase phase = boardState.time.phase == Phase.SPR ? Phase.SUM : Phase.AUT;
					//Phase phase = boardState.time.phase == Phase.SPR ? Phase.SUM : Phase.AUT;
					Phase phase = boardState.time.phase == Phase.SPR ? Phase.SPR : Phase.FAL;
					
					YearPhase until = new YearPhase(year, phase);
					currentOrders = moveSearch(boardState, until);
					
				}
				
				//	retreat time
				else if(boardState.time.phase == Phase.SUM ||
						 boardState.time.phase == Phase.AUT){
					
					if(Bot.LOGGING) System.out.println("Starting to process retreat...");
					
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
					
					Phase phase = boardState.time.phase == Phase.SUM ? Phase.SUM : Phase.WIN;
					//Phase phase = boardState.time.phase == Phase.SUM ? Phase.FAL : Phase.SPR;
					
					YearPhase until = new YearPhase(year, phase);
					currentOrders = moveSearch(boardState, until);
					
					if(Bot.LOGGING) System.out.println("Retreat order submitted.");
				}
				
				//	build/disband time 
				else if(boardState.time.phase == Phase.WIN){
					
					if(Bot.LOGGING) System.out.println("Starting to process builds...");
					
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
										
					currentOrders = orders;
					
					YearPhase until = new YearPhase(boardState.time.year, Phase.WIN);
					//YearPhase until = new YearPhase(boardState.time.year+1, Phase.SPR);
					
					currentOrders = moveSearch(boardState, until);
					
					if(Bot.LOGGING) System.out.println("Build orders submitted");
				}
				
				if(!boardUpdate){
					if(Bot.LOGGING) {
						System.out.println("Done with move search!");
						System.out.println("Moves will be: ");
						for(Order ord: currentOrders){
							System.out.println("\t"+ord.toOrder(boardState));
						}
					}

					movesReady = true;
				}
				

			}
		}
	}
}

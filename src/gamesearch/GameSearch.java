package gamesearch;


import java.util.Collection;
import java.util.List;
import java.util.Set;

import order.Order;
import order.builds.Build;
import order.builds.Remove;
import order.retreats.Disband;
import order.retreats.Retreat;
import order.spring_fall.Hold;
import order.spring_fall.SupportHold;

import java.util.HashSet;

import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.constant.BoardConfiguration;
import state.constant.BoardConfiguration.TerritoryCoast;
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
	
	public GameSearch(Player player, BoardConfiguration state, DiplomaticState dipState, BeliefState beliefState){

		this.relevantPlayer = player;
		
		this.searcher = new InternalSearch();
		this.internalSearch = new Thread(searcher);
		this.internalSearch.start();
		
		this.boardConfiguration = state;
		this.dipState = dipState;
		this.beliefState = beliefState;
		
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
						if( boardState.currentPhase == Phase.SPR || 
							boardState.currentPhase == Phase.FAL){
							
							//	put down something dumb at first so we have orders at least
							
							//	get all occupied territories
							Set<TerritorySquare> unitSquares = relevantPlayer.getOccupiedTerritories(boardState);

							
							Set<TerritorySquare> supported = new HashSet<TerritorySquare>();
							
							for(TerritorySquare ts: unitSquares){
								
								System.out.println("Looking at "+ts.getName());
								
								//	find something to support hold on
								
								Set<TerritorySquare> supportable = 
									boardConfiguration.getSupportableTerritories(boardState, relevantPlayer, ts, true);
								
								for(TerritorySquare sqr: supportable){
									System.out.println("\t"+sqr.getName());
								}
								
								boolean foundOriginal = false;
								for(TerritorySquare neighbor: supportable){
									if(!supported.contains(neighbor)){
										
										System.out.println("Decided to support "+neighbor.getName());
										
										orders.add(new SupportHold(boardState, relevantPlayer, ts, neighbor));
										supported.add(neighbor);
										
										foundOriginal = true;
										
										break;
									}
								}
								
								if(!foundOriginal && supportable.size() > 0){
									
									TerritorySquare target = supportable.iterator().next();
									
									System.out.println("randomly picking "+target.getName());
									
									
									orders.add(new SupportHold(boardState, relevantPlayer, ts, target));
								}else if(!foundOriginal){
									
									System.out.println("giving up and holding");
									
									orders.add(new Hold(boardState, relevantPlayer, ts));
								}
								
							}
							
							//TODO intelligent search
							
							currentOrders = orders;
							
						}
						
						//	retreat time
						else if(boardState.currentPhase == Phase.SUM ||
								 boardState.currentPhase == Phase.AUT){
							
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
						else if(boardState.currentPhase == Phase.WIN){
							
							int equalize = boardConfiguration.getRequiredBuilds(boardState, relevantPlayer);
							
							if(equalize > 0){
								
								Collection<TerritorySquare> possibleBuilds = boardConfiguration.getPossibleBuilds(boardState, relevantPlayer);
							
								for(TerritorySquare poss: possibleBuilds){
									
									orders.add(new Build(boardState, relevantPlayer, new Unit(relevantPlayer, true), poss));
									
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
							
							//TODO intelligent reasoning about what and where to build
							
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

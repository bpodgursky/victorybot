package gamesearch;


import java.util.Set;

import order.Hold;
import order.Order;

import java.util.HashSet;

import representation.Player;
import representation.TerritorySquare;
import state.BeliefState;
import state.DiplomaticState;
import state.BoardState;
import state.BoardState.Phase;

//	gamesearch runs in a separate thread.  call methods on it to notify it of things--
// 	updated state, diplomatic changes, etc
public class GameSearch {

	//	keep this updated with the current best guess at orders
	private Set<Order> currentOrders = new HashSet<Order>();
	
	//	use these as base for search, heuristic for search, and to inform
	//	move success probabilities, respectively
	private final BoardState boardState;
	private final DiplomaticState dipState;
	private final BeliefState beliefState;
	
	private final Thread internalSearch;
	private final InternalSearch searcher;
	
	private final Player relevantPlayer;
	
	private boolean boardUpdate;
	private boolean dipUpdate;
	private boolean beliefUpdate;
	
	public GameSearch(Player player, BoardState state, DiplomaticState dipState, BeliefState beliefState){

		this.relevantPlayer = player;
		
		this.searcher = new InternalSearch();
		this.internalSearch = new Thread(searcher);
		this.internalSearch.start();
		
		this.boardState = state;
		this.dipState = dipState;
		this.beliefState = beliefState;
	}
	
	//	methods to communicate to the search
	//	TODO probably should have more complex ways to communicate in here
	
	public void noteBoardUpdate(){
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
						
						//	get all occupied territories
						
						Set<TerritorySquare> unitSquares = relevantPlayer.getOccupiedTerritories();
						Set<Order> orders = new HashSet<Order>();
						
						if(boardState.getCurrentPhase() == Phase.SPR || boardState.getCurrentPhase() == Phase.FAL){
							for(TerritorySquare ts: unitSquares){
								orders.add(new Hold(relevantPlayer, ts));
							}
							
							currentOrders = orders;
						}else{
							currentOrders = new HashSet<Order>();
						}
//						else if(boardState.getCurrentPhase() == Phase.WI){
//							
//						}
						
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

package gamesearch;


import java.util.Set;

import order.Order;
import order.spring_fall.Hold;

import java.util.HashSet;

import representation.Player;
import representation.TerritorySquare;
import state.constant.BoardConfiguration;
import state.dynamic.BeliefState;
import state.dynamic.BoardState;
import state.dynamic.DiplomaticState;
import state.dynamic.BoardState.Phase;

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
						
						//	get all occupied territories
						
						Set<TerritorySquare> unitSquares = relevantPlayer.getOccupiedTerritories(boardState);
						Set<Order> orders = new HashSet<Order>();
						
						if(boardState.currentPhase == Phase.SPR || boardState.currentPhase == Phase.FAL){
							for(TerritorySquare ts: unitSquares){
								orders.add(new Hold(boardState, relevantPlayer, ts));
							}
							
							currentOrders = orders;
						}else{
							currentOrders = new HashSet<Order>();
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

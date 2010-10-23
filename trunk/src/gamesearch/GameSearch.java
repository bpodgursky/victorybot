package gamesearch;


import java.util.Set;
import order.Order;

import java.util.HashSet;

import state.BeliefState;
import state.DiplomaticState;
import state.BoardState;

//	gamesearch runs in a separate thread.  call methods on it to notify it of things--
// 	updated state, diplomatic changes, etc
public class GameSearch {

	Set<Order> currentOrders = new HashSet<Order>();
	
	BoardState boardState;
	DiplomaticState dipState;
	BeliefState beliefState;
	
	Thread internalSearch;
	
	public GameSearch(BoardState state){
		
	}
	
	public Set<Order> currentOrders(){
		return currentOrders;
	}
	
}

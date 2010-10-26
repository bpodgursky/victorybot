package state;

import java.util.Set;

import order.Order;

public class BeliefState {
	
	//	TODO this is just a stub for now
	//	probably want to have actual classes for proposed things -- Alliance, Enemies, etc
	//	then it can calculate these on some heuristic even if it isn't explicitly stored
	public double getProbability(String fact){
		return .5;
	}
	
	//	TODO stub
	public void update(Set<Order> moves){
		
	}

}

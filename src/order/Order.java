package order;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import representation.Player;
import state.dynamic.BoardState;

public abstract class Order {
	
	//	succeeded, bounced, support was cut, dislodged convoying
	//	no such order, or action has not been executed
	//	fail is not include in the synatx, because they don't define a
	//	token for a hold or convoy that is dislodged.  Going to call
	//	those fail
	public enum Result{SUC, BNC, CUT, DSR, NSO, MAYBE, FAIL}; 
	
	public Result actionResult;
	
	//	it has to retreat, doesn't have to retreat, or 
	//	action hasn't been executed
	public enum RetreatState{RET, NO, NA, MAYBE}
	
	public RetreatState retreatState;
	
	public final Player player;
	
	//	for hashing.  is this a good idea?  it will be until this is 
	//	distributed and this count is no longer consistent.  Deal with that
	//	if this ever happens
	//private static AtomicLong idCount = new AtomicLong(0);
	//private final long id;
	
	public Order(Player player, Result result, RetreatState retreat){
		this.player = player;
		this.actionResult = result;
		this.retreatState = retreat;
		
//		id = idCount.incrementAndGet();
	}
	
	public abstract void execute();
	
	public abstract String toOrder(BoardState bst);
	
	public Result getResult(){
		return actionResult;
	}
	
	public String toString(BoardState bst){
		return this.toOrder(bst);
	}
	
//	//TODO lame lame lame
	public int hashCode2(){
		return actionResult.hashCode()+retreatState.hashCode()+player.hashCode2();
	}
//	
//	@Override
//	public boolean equals(Object other){
//		
//		if(other == null || !(other instanceof Order)){
//			return false;
//		}
//		
//		return id == ((Order)other).id;
//	}
	
	//for packaging orders

	//	so we can return the value associated with a set of moves
	public static class MovesValue implements Comparable<MovesValue>{
		
		public final double value;
		public final Collection<Order> moves;
		
		public MovesValue(Collection<Order> moves, double value){
			this.value = value;
			this.moves = moves;
		}

		@Override
		public int compareTo(MovesValue arg0) {
			return -Double.compare(value, arg0.value);
		}
	}
	
	public static class OrderValue{
		
		public final double score;
		public final Order order;
		
		public OrderValue(Order ord, double score){
			this.score = score;
			this.order = ord;
		}
	}
	
}

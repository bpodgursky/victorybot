package order;

import representation.Player;

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
	
	public Order(Player player, Result result, RetreatState retreat){
		this.player = player;
		this.actionResult = result;
		this.retreatState = retreat;
	}
	
//	public Order(Player player){
//		this.player = player;
//		this.actionResult = Result.NA;
//		this.retreatState = RetreatState.NA;
//	}
	
	public abstract void execute();
	
	public abstract String toOrder();
	
	public Result getResult(){
		return actionResult;
	}
	
	public String toString(){
		return this.toOrder();
	}
}

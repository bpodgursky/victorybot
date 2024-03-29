package order.spring_fall;

import ai.Bot;
import order.Order;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.dynamic.BoardState;


public class SupportHold extends Order{
	
	//	for now let these be full instantiated since it's easy to deal with.  as long as the 
	//	game search is only forward, there shouldn't be any need to leave values (like player or unit)
	//	unbound.  if we implement some kind of constraint satisfaction or POP search, will need to change
	//	this
	
	public final TerritorySquare supportFrom;
	public final TerritorySquare supportTo;
	
	public final Unit supporter;
	public final Unit supported;
	
	public SupportHold(BoardState bst, Player p, TerritorySquare supportFrom, TerritorySquare supportTo) throws Exception{
		this(bst, p, supportFrom, supportTo, Result.MAYBE, RetreatState.MAYBE);
	}

	public SupportHold(BoardState bst, Player p, TerritorySquare supportFrom, TerritorySquare supportTo, Result result, RetreatState retreat) throws Exception{
		super(p, result, retreat);
		
		if(supportFrom == null || supportTo == null){
			throw new Exception("null arguments");
		}

		if(Bot.ASSERTS){
			supportFrom.board.assertCanSupportHold(bst, p, supportFrom, supportTo);
		}
			
		this.supportFrom = supportFrom;
		this.supportTo = supportTo;
		
		this.supporter = supportFrom.getOccupier(bst);
		this.supported = supportTo.getOccupier(bst);
		
	}
	
	public String toString(){
		return "[support hold with "+supporter + " to "+supported+"]";
	}
	
	public String toOrder(BoardState bst){
		return "( ( "+supportFrom.getUnitString(bst)+" ) SUP ( "+supportTo.getUnitString(bst)+" ) )";
	} 

	public void execute() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int hashCode2(){
		return supportFrom.hashCode2()+supported.hashCode2()+supportTo.hashCode2()+supporter.hashCode2()+super.hashCode2();
	}
	
}

package order;

import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.constant.BoardConfiguration;


public class SupportHold extends Order{
	
	//	for now let these be full instantiated since it's easy to deal with.  as long as the 
	//	game search is only forward, there shouldn't be any need to leave values (like player or unit)
	//	unbound.  if we implement some kind of constraint satisfaction or POP search, will need to change
	//	this
	
	public final TerritorySquare supportFrom;
	public final TerritorySquare supportTo;
	
	public final Unit supporter;
	public final Unit supported;
	
	public SupportHold(Player p, TerritorySquare supportFrom, TerritorySquare supportTo) throws Exception{
		this(p, supportFrom, supportTo, Result.MAYBE, RetreatState.MAYBE);
	}

	public SupportHold(Player p, TerritorySquare supportFrom, TerritorySquare supportTo, Result result, RetreatState retreat) throws Exception{
		super(p, result, retreat);
		
		if(supportFrom == null || supportTo == null){
			throw new Exception("null arguments");
		}
		
//		if(!supportFrom.board.canSupportHold(p, supportFrom, supportTo)){
//			throw new Exception("cannot support hold from "+supportFrom+" to "+supportTo);
//		}
		
		supportFrom.board.assertCanSupportHold(p, supportFrom, supportTo);
		
		this.supportFrom = supportFrom;
		this.supportTo = supportTo;
		
		this.supporter = supportFrom.getOccupier();
		this.supported = supportTo.getOccupier();
		
	}
	
	public String toString(){
		return "[support hold with "+supporter + " to "+supported+"]";
	}
	
	public String toOrder(){
		return "( ( "+supportFrom.getUnitString()+" ) SUP ( "+supportTo.getUnitString()+" ) )";
	} 

	public void execute() {
		// TODO Auto-generated method stub
		
	}
}

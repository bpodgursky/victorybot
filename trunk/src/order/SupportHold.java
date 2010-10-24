package order;

import representation.TerritorySquare;
import representation.Unit;
import state.BoardState;


public class SupportHold extends Order{
	
	//	for now let these be full instantiated since it's easy to deal with.  as long as the 
	//	game search is only forward, there shouldn't be any need to leave values (like player or unit)
	//	unbound.  if we implement some kind of constraint satisfaction or POP search, will need to change
	//	this
	
	public final TerritorySquare supportFrom;
	public final TerritorySquare supportTo;
	
	public final Unit supporter;
	public final Unit supported;
	

	public SupportHold(TerritorySquare supportFrom, TerritorySquare supportTo) throws Exception{
		
		if(!BoardState.canSupportHold(supportFrom, supportTo)){
			throw new Exception("cannot support hold from "+supportFrom+" to "+supportTo);
		}
		
		this.supportFrom = supportFrom;
		this.supportTo = supportTo;
		
		this.supporter = supportFrom.getOccupier();
		this.supported = supportTo.getOccupier();
		
	}
	
	public String toString(){
		return "[support hold with "+supporter + " to "+supported+"]";
	}
}

package order;

import gamestate.TerritorySquare;
import gamestate.Unit;


public class SupportHold {
	
	//	for now let these be full instantiated since it's easy to deal with.  as long as the 
	//	game search is only forward, there shouldn't be any need to leave values (like player or unit)
	//	unbound.  if we implement some kind of constraint satisfaction or POP search, will need to change
	//	this
	
	public final TerritorySquare supportFrom;
	public final TerritorySquare supportTo;
	
	public final Unit supporter;
	public final Unit supported;
	
	public SupportHold(TerritorySquare supportFrom, Unit supporter, TerritorySquare supportTo, Unit supported){
		
		this.supportFrom = supportFrom;
		this.supportTo = supportTo;
		
		this.supporter = supporter;
		this.supported = supported;
		
	}
}

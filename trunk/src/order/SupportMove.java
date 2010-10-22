package order;

import gamestate.TerritorySquare;
import gamestate.Unit;

public class SupportMove {

	public final TerritorySquare supportFrom;
	public final TerritorySquare supportOrig;
	public final TerritorySquare supportInto;
	
	public final Unit supporter;
	public final Unit supported;
	
	public SupportMove(TerritorySquare supportFrom, Unit supporter, TerritorySquare supportOrig, Unit supported){
		
		this.supportFrom = supportFrom;
		//TODO the rest
	}
	
}

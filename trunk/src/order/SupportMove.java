package order;

import gamestate.GameState;
import gamestate.TerritorySquare;
import gamestate.Unit;

public class SupportMove {

	public final TerritorySquare supportFrom;
	public final TerritorySquare supportOrig;
	public final TerritorySquare supportInto;
	
	public final Unit supporter;
	public final Unit supported;
	
	public SupportMove(TerritorySquare supportFrom, TerritorySquare supportOrig, TerritorySquare supportInto) throws Exception{
		
		if(!GameState.canSupportMove(supportFrom, supportOrig, supportInto)){
			throw new Exception("cannot support with "+supportFrom+" from "+supportOrig+" to "+ supportInto);
		}
		
		this.supportFrom = supportFrom;
		this.supportOrig = supportOrig;
		this.supportInto = supportInto;
		
		this.supporter = supportFrom.getOccupier();
		this.supported = supportOrig.getOccupier();
	}
	
}

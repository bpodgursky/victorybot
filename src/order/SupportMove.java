package order;

import representation.TerritorySquare;
import representation.Unit;
import state.BoardState;

public class SupportMove extends Order{

	public final TerritorySquare supportFrom;
	public final TerritorySquare supportOrig;
	public final TerritorySquare supportInto;
	
	public final Unit supporter;
	public final Unit supported;
	
	public SupportMove(TerritorySquare supportFrom, TerritorySquare supportOrig, TerritorySquare supportInto) throws Exception{
		
		if(!BoardState.canSupportMove(supportFrom, supportOrig, supportInto)){
			throw new Exception("cannot support with "+supportFrom+" from "+supportOrig+" to "+ supportInto);
		}
		
		this.supportFrom = supportFrom;
		this.supportOrig = supportOrig;
		this.supportInto = supportInto;
		
		this.supporter = supportFrom.getOccupier();
		this.supported = supportOrig.getOccupier();
	}
	
	public String toString(){
		return "[support move with "+supportFrom+" from "+supportFrom+" to "+supportInto+"]";
	}
}

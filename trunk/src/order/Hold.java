package order;

import representation.TerritorySquare;
import representation.Unit;
import state.BoardState;

public class Hold extends Order{
	
	public final Unit holdingUnit;
	
	public final TerritorySquare holdingSquare;
	
	public Hold(TerritorySquare square) throws Exception{
		
		if(!BoardState.canHold(square)){
			throw new Exception("hold is not valid for "+square);
		}
		
		holdingSquare = square;
		holdingUnit = square.getOccupier();
		
	}


	public void execute() {
		System.out.println("Doing Hold!");
		
	}

}

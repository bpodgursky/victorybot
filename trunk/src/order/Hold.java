package order;

import representation.Player;
import representation.TerritorySquare;
import representation.Unit;

public class Hold extends Order{
	
	public final Unit holdingUnit;
	
	public final TerritorySquare holdingSquare;
	
	public Hold(Player p, TerritorySquare square) throws Exception{
		super(p);
		
		if(square == null){
			throw new Exception("null arguments");
		}
		
		if(!square.board.canHold(p, square)){
			throw new Exception("hold is not valid for "+square);
		}
		
		holdingSquare = square;
		holdingUnit = square.getOccupier();
		
	}


	public void execute() {
		System.out.println("Doing Hold!");
		
	}


	@Override
	public String toOrder() {
		return "( ( "+holdingSquare.getUnitString()+" ) HLD )";
	}
}

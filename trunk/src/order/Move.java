package order;

import gamestate.GameState;
import gamestate.TerritorySquare;
import gamestate.Unit;

public class Move {
	
	//	for now let these be full instantiated since it's easy to deal with.  as long as the 
	//	game search is only forward, there shouldn't be any need to leave values (like player or unit)
	//	unbound.  if we implement some kind of constraint satisfaction or POP search, will need to change
	//	this
	
	//not important to know which unit, only the player and type associated with it	
	public final Unit unit;
	
	public final TerritorySquare from;
	public final TerritorySquare to;
	
	public Move(TerritorySquare from, TerritorySquare to) throws Exception{
		this(from, to, "NA");
	}

	public Move( TerritorySquare from, TerritorySquare to, String destinationCoast) throws Exception{

		if(!GameState.canMove(from, to, destinationCoast)){
			throw new Exception("cannot move from "+ from+ " "+to+" on coast "+destinationCoast);
		}
		
		this.unit = from.getOccupier();
		this.from = from;
		this.to = to;
	}
}

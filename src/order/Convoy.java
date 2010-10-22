package order;

import gamestate.GameState;
import gamestate.TerritorySquare;
import gamestate.Unit;

public class Convoy {

	public final Unit convoyedUnit;
	public final Unit convoyingUnit;
	
	public final TerritorySquare convoyer;
	public final TerritorySquare from;
	public final TerritorySquare to;
	
	public Convoy(TerritorySquare convoyer, TerritorySquare from, TerritorySquare to) throws Exception{
	
		if(!GameState.canConvoy(convoyer, from, to)){
			throw new Exception("cannot convoy with "+convoyer+" from "+from+" to "+to);
		}
		
		convoyingUnit = convoyer.getOccupier();
		convoyedUnit = from.getOccupier();
		
		this.convoyer = convoyer;
		this.from = from;
		this.to = to;
		
	}
	
}

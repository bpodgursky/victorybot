package order;

import representation.TerritorySquare;
import representation.Unit;
import state.BoardState;

public class Convoy extends Order{

	public final Unit convoyedUnit;
	public final Unit convoyingUnit;
	
	public final TerritorySquare convoyer;
	public final TerritorySquare from;
	public final TerritorySquare to;
	
	public Convoy(TerritorySquare convoyer, TerritorySquare from, TerritorySquare to) throws Exception{
	
		if(!BoardState.canConvoy(convoyer, from, to)){
			throw new Exception("cannot convoy with "+convoyer+" from "+from+" to "+to);
		}
		
		convoyingUnit = convoyer.getOccupier();
		convoyedUnit = from.getOccupier();
		
		this.convoyer = convoyer;
		this.from = from;
		this.to = to;
		
	}
	
	public String toString(){
		return "[ convoy with "+convoyer+" from "+from+ " to "+to+"]";
	}


	public void execute() {
		// TODO Auto-generated method stub
		
	}
}

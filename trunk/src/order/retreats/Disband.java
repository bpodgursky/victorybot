package order.retreats;

import order.Order;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.dynamic.BoardState;

public class Disband extends Order{
	
	public final Unit disband;
	public final TerritorySquare disbandAt;
	
	public final String disbandCoast;
	
	public Disband(BoardState bst, Player p, TerritorySquare location) throws Exception{
		this(bst, p, location, "NA");
	}

	
	public Disband(BoardState bst, Player p, TerritorySquare location, String coast) throws Exception{
		super(p, Result.SUC, RetreatState.NA);
		
		if(location == null){
			throw new Exception("null arguments");
		}
		
//		if(!location.board.canDisband(p, location)){
//			throw new Exception("invalid disband");
//		}
		
		location.board.assertCanDisband(bst, p, location);
		
		this.disbandAt = location;
		this.disband = bst.getRetreatingUnit(location);
		this.disbandCoast = coast;
	}
	
	public void execute(){
	
	}

	@Override
	public String toOrder(BoardState bst) {
		return "( ( "+TerritorySquare.getUnitStringParen(player, disband, disbandAt.getName(), disbandCoast)+" ) DSB )";
	}
}
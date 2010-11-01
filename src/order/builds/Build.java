package order.builds;

import order.Order;
import order.Order.Result;
import order.Order.RetreatState;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.dynamic.BoardState;

public class Build extends Order{
	
	public final Unit build;
	
	public final TerritorySquare location;
	
	public final String coast;
	
	public Build(BoardState bst, Player p, Unit u, TerritorySquare location) throws Exception{
		this(bst, p, u, location, "NA");
	}
	
	public Build(BoardState bst, Player p, Unit u, TerritorySquare location, String coast) throws Exception{
		super(p, Result.SUC, RetreatState.NA);
		
		if(u == null || location == null || coast == null){
			throw new Exception("null arguments");
		}
		
		location.board.assertCanBuild(bst, p, u, location);
		
		this.build = u;
		this.location = location;
		this.coast = coast;
		
	}
	
	public String toOrder(BoardState bst){
		return "( ( "+TerritorySquare.getUnitString(player, build, location.getName(), coast)+" ) BLD )";
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
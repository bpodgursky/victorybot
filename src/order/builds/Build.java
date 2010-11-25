package order.builds;

import ai.Bot;
import order.Order;
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
		
		if(Bot.ASSERTS){
			location.board.assertCanBuild(bst, p, u, location);
		}
		
		this.build = u;
		this.location = location;
		this.coast = coast;
		
	}
	
	public String toOrder(BoardState bst){
		return "( ( "+TerritorySquare.getUnitStringParen(player, build, location.getName(), coast)+" ) BLD )";
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}
	
	public int hashCode2(){
		return build.hashCode2()+location.hashCode2()+coast.hashCode()+super.hashCode2();
	}

}
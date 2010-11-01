package order.retreats;

import order.Order;
import order.Order.Result;
import order.Order.RetreatState;
import representation.Player;
import representation.TerritorySquare;

import representation.Unit;

public class Retreat extends Order{
	
	public final TerritorySquare from;
	public final TerritorySquare to;
	
	public final Unit retreatingUnit;
	
	public final String destCoast;
	
	public Retreat(Player p, TerritorySquare from, TerritorySquare to) throws Exception{
		this(p, from, to, "NA");
	}
	
	public Retreat(Player p, TerritorySquare from, TerritorySquare to, Result result) throws Exception{
		this(p, from, to, "NA", result);
	}
	
	public Retreat(Player p, TerritorySquare from, TerritorySquare to, String coast) throws Exception{
		this(p, from, to, "NA", Result.MAYBE);
	}
	
	public Retreat(Player p, TerritorySquare from, TerritorySquare to, String destinationCoast, Result result) throws Exception{
		super(p, result, RetreatState.NA);
		
		if(from == null || to == null || destinationCoast == null){
			throw new Exception("null arguments");
		}
			
		
//		if(!from.board.canRetreat(p, from, to, destinationCoast)){
//			throw new Exception("invalid retreat");
//		}
		
		from.board.assertCanRetreat(p, from, to, destinationCoast);
		
		this.from = from;
		this.to = to;
		this.destCoast = destinationCoast;
		
		this.retreatingUnit = from.board.getRetreatingUnit(from);
	}
	
	public void execute(){
		
	}
	
	public String toOrder(){
		return "( ( "+TerritorySquare.getUnitString(player, retreatingUnit, from.getName(), destCoast)+" ) RTO "+
			TerritorySquare.getDestString(retreatingUnit, to.getName(), destCoast)+" )";
	}
}
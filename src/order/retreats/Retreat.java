package order.retreats;

import order.Order;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.dynamic.BoardState;

public class Retreat extends Order{
	
	public final TerritorySquare from;
	public final TerritorySquare to;
	
	public final Unit retreatingUnit;
	
	public final String destCoast;
	
	public Retreat(BoardState bst, Player p, TerritorySquare from, TerritorySquare to) throws Exception{
		this(bst, p, from, to, "NA");
	}
	
	public Retreat(BoardState bst, Player p, TerritorySquare from, TerritorySquare to, Result result) throws Exception{
		this(bst, p, from, to, "NA", result);
	}
	
	public Retreat(BoardState bst, Player p, TerritorySquare from, TerritorySquare to, String coast) throws Exception{
		this(bst, p, from, to, "NA", Result.MAYBE);
	}
	
	public Retreat(BoardState bst, Player p, TerritorySquare from, TerritorySquare to, String destinationCoast, Result result) throws Exception{
		super(p, result, RetreatState.NA);
		
		if(from == null || to == null || destinationCoast == null){
			throw new Exception("null arguments");
		}
			
		
//		if(!from.board.canRetreat(p, from, to, destinationCoast)){
//			throw new Exception("invalid retreat");
//		}
		
		from.board.assertCanRetreat(bst, p, from, to, destinationCoast);
		
		this.from = from;
		this.to = to;
		this.destCoast = destinationCoast;
		
		this.retreatingUnit = bst.getRetreatingUnit(from);
	}
	
	public void execute(){
		
	}
	
	public String toOrder(BoardState bst){
		return "( ( "+TerritorySquare.getUnitString(player, retreatingUnit, from.getName(), destCoast)+" ) RTO "+
			TerritorySquare.getDestString(retreatingUnit, to.getName(), destCoast)+" )";
	}
}

package order.spring_fall;

import order.Order;
import order.Order.Result;
import order.Order.RetreatState;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.BoardState;

public class Move extends Order{
	
	//	for now let these be full instantiated since it's easy to deal with.  as long as the 
	//	game search is only forward, there shouldn't be any need to leave values (like player or unit)
	//	unbound.  if we implement some kind of constraint satisfaction or POP search, will need to change
	//	this
	
	//not important to know which unit, only the player and type associated with it	
	public final Unit unit;
	
	public final TerritorySquare from;
	public final TerritorySquare to;
	
	public final String coast;
	
	
	public Move(Player p, TerritorySquare from, TerritorySquare to) throws Exception{
		this(p, from, to, "NA");
	}
	
	public Move(Player p, TerritorySquare from, TerritorySquare to, Result result, RetreatState retreat) throws Exception{
		this(p, from, to, "NA", result, retreat);
	}
	
	public Move(Player p, TerritorySquare from, TerritorySquare to, String destinationCoast) throws Exception{
		this(p, from, to, destinationCoast, Result.MAYBE, RetreatState.MAYBE);
	}

	public Move(Player p, TerritorySquare from, TerritorySquare to, String destinationCoast, Result result, RetreatState retreat) throws Exception{
		super(p, result, retreat);
		
		if(from == null || to == null || destinationCoast == null){
			throw new Exception("null arguments");
		}
//		if(!from.board.canMove(p, from, to, destinationCoast)){
//			throw new Exception("player "+p.getName()+"cannot move from "+ from+ " to "+to+" on coast "+destinationCoast);
//		}
		
		from.board.assertCanMove(p, from, to, destinationCoast);
		
		this.unit = from.getOccupier();
		this.from = from;
		this.to = to;
		
		this.coast = destinationCoast;
	}
	
//	public String toString(){
//		return "[ move from " +from+" to "+to+"]";
//	}

	public void execute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toOrder() {
		return "( ( "+from.getUnitString()+" ) MTO "+
			TerritorySquare.getDestString(unit, to.getName(), coast)+" )";
	}
}

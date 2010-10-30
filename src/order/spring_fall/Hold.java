package order.spring_fall;

import order.Order;
import order.Order.Result;
import order.Order.RetreatState;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;

public class Hold extends Order{
	
	public final Unit holdingUnit;
	
	public final TerritorySquare holdingSquare;
	
	public Hold(Player p, TerritorySquare square) throws Exception{
		this(p, square, Result.MAYBE, RetreatState.MAYBE);
	}
	
	public Hold(Player p, TerritorySquare square, Result result, RetreatState retreat) throws Exception{
		super(p, result, retreat);
		
		if(square == null){
			throw new Exception("null arguments");
		}
		
//		if(!square.board.canHold(p, square)){
//			throw new Exception("hold by "+p.getName()+" is not valid for "+square);
//		}
		
		square.board.assertCanHold(p, square);
		
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

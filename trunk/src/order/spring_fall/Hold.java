package order.spring_fall;

import ai.Bot;
import order.Order;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.dynamic.BoardState;

public class Hold extends Order{
	
	public final Unit holdingUnit;
	
	public final TerritorySquare holdingSquare;
	
	public Hold(BoardState bst, Player p, TerritorySquare square) throws Exception{
		this(bst, p, square, Result.MAYBE, RetreatState.MAYBE);
	}
	
	public Hold(BoardState bst, Player p, TerritorySquare square, Result result, RetreatState retreat) throws Exception{
		super(p, result, retreat);
		
		if(square == null){
			throw new Exception("null arguments");
		}
		
		if(Bot.ASSERTS){
			square.board.assertCanHold(bst, p, square);
		}
			
		holdingSquare = square;
		holdingUnit = square.getOccupier(bst);
		
	}


	public void execute() {
		System.out.println("Doing Hold!");
		
	}
	
	@Override
	public int hashCode2(){
		return holdingUnit.hashCode2()+holdingSquare.hashCode2()+super.hashCode2();
	}
	
	@Override
	public String toOrder(BoardState bst) {
		return "( ( "+holdingSquare.getUnitString(bst)+" ) HLD )";
	}
}

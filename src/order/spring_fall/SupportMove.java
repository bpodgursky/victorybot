package order.spring_fall;

import ai.Bot;
import order.Order;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.dynamic.BoardState;

public class SupportMove extends Order{

	public final TerritorySquare supportFrom;
	public final TerritorySquare supportOrig;
	public final TerritorySquare supportInto;
	
	public final Unit supporter;
	public final Unit supported;

	public SupportMove(BoardState bst, Player p, TerritorySquare supportFrom, TerritorySquare supportOrig, TerritorySquare supportInto) throws Exception{
		this(bst,  p, supportFrom, supportOrig, supportInto, Result.MAYBE, RetreatState.MAYBE);
	}
	
	public SupportMove(BoardState bst, Player p, TerritorySquare supportFrom, TerritorySquare supportOrig, TerritorySquare supportInto, Result result, RetreatState retreat) throws Exception{
		super(p, result, retreat);
		
		if(supportFrom == null || supportOrig == null || supportInto == null){
			throw new Exception("null arguments");
		}

		if(Bot.ASSERTS){		
			supportFrom.board.assertCanSupportMove(bst, p, supportFrom, supportOrig, supportInto);
		}
		
		this.supportFrom = supportFrom;
		this.supportOrig = supportOrig;
		this.supportInto = supportInto;
		
		this.supporter = supportFrom.getOccupier(bst);
		this.supported = supportOrig.getOccupier(bst);
	}
	
	public String toString(BoardState bst){
		return "[support move with "+supportFrom+" from "+supportFrom+" to "+supportInto+"]";
	}
	
	public String toOrder(BoardState bst){
		return "( ( "+supportFrom.getUnitString(bst)+" ) SUP ( "+supportOrig.getUnitString(bst)+" ) MTO "+supportInto.getName()+" )";
	}

	public void execute() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int hashCode2(){
		return supportFrom.hashCode2()+supportOrig.hashCode2()+supportInto.hashCode2()+supporter.hashCode2()+supported.hashCode2()+super.hashCode2();
	}
	
}

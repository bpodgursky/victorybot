package order.spring_fall;

import ai.Bot;
import order.Order;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.dynamic.BoardState;

//	for a fleet to support the convoy
public class Convoy extends Order{

	public final Unit convoyedUnit;
	public final Unit convoyingUnit;
	
	public final TerritorySquare convoyer;
	public final TerritorySquare from;
	public final TerritorySquare to;

	public Convoy(BoardState bst, Player p, TerritorySquare convoyer, TerritorySquare from, TerritorySquare to) throws Exception {
		this(bst, p, convoyer, from, to, Result.MAYBE, RetreatState.MAYBE);
	}
	
	public Convoy(BoardState bst, Player p, TerritorySquare convoyer, TerritorySquare from, TerritorySquare to, Result result, RetreatState retreat) throws Exception{
		super(p, result, retreat);
		
		if(convoyer == null || from == null || to == null){
			throw new Exception("null arguments");
		}

		if(Bot.ASSERTS){
			convoyer.board.assertCanAssistConvoy(bst, p, convoyer, from, to);
		}
		
		convoyingUnit = convoyer.getOccupier(bst);
		convoyedUnit = from.getOccupier(bst);
		
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

	@Override
	public String toOrder(BoardState bst) {
		return "( ( "+convoyer.getUnitString(bst)+" ) CVY ( "+
			from.getUnitString(bst)+" ) CTO "+to.getName()+" )";
	}
	
	
	@Override
	public int hashCode2(){
		return convoyedUnit.hashCode2()+convoyingUnit.hashCode2()+convoyer.hashCode2()+from.hashCode2()+to.hashCode2()+super.hashCode2();
	}
}

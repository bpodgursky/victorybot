package order;

import order.Order.Result;
import order.Order.RetreatState;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;

//	for a fleet to support the convoy
public class Convoy extends Order{

	public final Unit convoyedUnit;
	public final Unit convoyingUnit;
	
	public final TerritorySquare convoyer;
	public final TerritorySquare from;
	public final TerritorySquare to;

	public Convoy(Player p, TerritorySquare convoyer, TerritorySquare from, TerritorySquare to) throws Exception {
		this(p, convoyer, from, to, Result.MAYBE, RetreatState.MAYBE);
	}
	
	public Convoy(Player p, TerritorySquare convoyer, TerritorySquare from, TerritorySquare to, Result result, RetreatState retreat) throws Exception{
		super(p, result, retreat);
		
		if(convoyer == null || from == null || to == null){
			throw new Exception("null arguments");
		}
		
//		if(!convoyer.board.canAssistConvoy(p, convoyer, from, to)){
//			throw new Exception("cannot convoy with "+convoyer+" from "+from+" to "+to);
//		}
		
		convoyer.board.assertCanAssistConvoy(p, convoyer, from, to);
		
		convoyingUnit = convoyer.getOccupier();
		convoyedUnit = from.getOccupier();
		
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
	public String toOrder() {
		return "( ( "+convoyer.getUnitString()+" ) CVY ( "+
			from.getUnitString()+" ) CTO "+to.getName()+" )";
	}
}

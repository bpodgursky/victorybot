package order.builds;

import order.Order;
import representation.Player;
import representation.TerritorySquare;
import state.dynamic.BoardState;

public class Remove extends Order{

	public final TerritorySquare disbandLocation;

	
	public Remove(BoardState bst, Player p, TerritorySquare disbandAt) throws Exception{
		super(p, Result.SUC, RetreatState.NA);
		
		if(disbandAt == null){
			throw new Exception("null arguments");
		}
		
		disbandAt.board.assertCanRemove(bst, p, disbandAt);
		
		this.disbandLocation = disbandAt;
	}
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
	}

	@Override
	public String toOrder(BoardState bst) {
		return "( ( "+disbandLocation.getUnitString(bst)+" ) REM )";
	}

}

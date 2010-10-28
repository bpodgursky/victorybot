package order;

import representation.Player;
import representation.TerritorySquare;

public class Remove extends Order{

	public final TerritorySquare disbandLocation;

	
	public Remove(Player p, TerritorySquare disbandAt) throws Exception{
		super(p, Result.SUC, RetreatState.NA);
		
		if(disbandAt == null){
			throw new Exception("null arguments");
		}
		
//		if(disbandAt.board.canDisband(p, disbandAt)){
//			throw new Exception("invalid disband");
//		}
		
		disbandAt.board.assertCanRemove(p, disbandAt);
		
		this.disbandLocation = disbandAt;
	}
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
	}

	@Override
	public String toOrder() {
		return "( ( "+disbandLocation.getUnitString()+" ) DSB )";
	}

}

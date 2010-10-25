package order;

import representation.Player;
import representation.TerritorySquare;
import representation.Unit;

public class Disband extends Order{
	
	public final Unit disband;
	public final TerritorySquare disbandAt;
	
	public final String disbandCoast;
	
	public Disband(Player p, TerritorySquare location) throws Exception{
		this(p, location, "NA");
	}
	
	public Disband(Player p, TerritorySquare location, String coast) throws Exception{
		super(p);
		
		if(location == null){
			throw new Exception("null arguments");
		}
		
		if(!location.board.canDisband(p, location)){
			throw new Exception("invalid disband");
		}
		
		this.disbandAt = location;
		this.disband = location.board.getRetreatingUnit(location);
		this.disbandCoast = coast;
	}
	
	public void execute(){
	
	}

	@Override
	public String toOrder() {
		return "( ( "+TerritorySquare.getUnitString(player, disband, disbandAt.getName(), disbandCoast)+" ) DSB )";
	}
}
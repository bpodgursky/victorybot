package order;

import representation.Player;
import representation.TerritorySquare;
import representation.Unit;

public class Build extends Order{
	
	public final Unit build;
	
	public final TerritorySquare location;
	
	public final String coast;
	
	public Build(Player p, Unit u, TerritorySquare location) throws Exception{
		this(p, u, location, "NA");
	}
	
	public Build(Player p, Unit u, TerritorySquare location, String coast) throws Exception{
		super(p);
		
		if(u == null || location == null || coast == null){
			throw new Exception("null arguments");
		}
		
		if(!location.board.canBuild(p, u, location)){
			throw new Exception("invalid build");
		}
		
		this.build = u;
		this.location = location;
		this.coast = coast;
		
	}
	
	public String toOrder(){
		return "( ( "+TerritorySquare.getUnitString(player, build, location.getName(), coast)+" ) BLD )";
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
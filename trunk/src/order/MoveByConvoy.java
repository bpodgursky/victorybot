package order;

import java.util.LinkedList;
import java.util.List;

import representation.Player;
import representation.TerritorySquare;
import representation.Unit;

//	for the army to convoy
public class MoveByConvoy extends Order{

	public final Unit convoyedUnit;
	public final List<Unit> convoyingUnits = new LinkedList<Unit>();
	
	public final TerritorySquare convoyOrigin;
	public final TerritorySquare convoyDestination;
	
	public final List<TerritorySquare> transits;
	
	public MoveByConvoy(Player p, TerritorySquare origin, TerritorySquare destination, List<TerritorySquare> transits) throws Exception{
		super(p);		
		
		if(origin == null || destination == null || transits == null){
			throw new Exception("null arguments");
		}
		
		if(!origin.board.canConvoy(p, origin, destination, transits)){
			throw new Exception("invalid convoy");
		}
		
		this.convoyedUnit = origin.getOccupier();
		
		this.convoyOrigin = origin;
		this.convoyDestination = destination;
		this.transits = transits;
		
		for(TerritorySquare t: transits){
			convoyingUnits.add(t.getOccupier());
		}
		
	}

	public void execute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toOrder() {
		String str = "( ( "+convoyOrigin.getUnitString()+" ) CTO "+
		convoyDestination.getName()+" VIA (";
		
		for(int i = 0; i < transits.size(); i++){
			if(i == transits.size() -1){
				str+=transits.get(i).getName();
			}else{
				str+=transits.get(i).getName()+" ";
			}
		}
	
		str+=" ) )";
		
		return str;
	}
}

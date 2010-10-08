import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TerritorySquare {
	Set<TerritorySquare> borders = new HashSet<TerritorySquare>();
	
	public void borders(TerritorySquare other){
		borders.add(other);
	}
	
	boolean isSupplyCenter;
	boolean isLand;			//false if sea
	
	Player controller;		//null if none
	Player homeSupplyFor;	//null if none
	
	Unit occupier;			//null if none

	String name;

	public TerritorySquare(String name, 
			boolean isSupply, boolean isLand,
			Player controller, 
			Player homeSupplyFor){
		
		this.name = name;
		this.isLand = isLand;
		this.isSupplyCenter = isSupply;
		this.controller = controller;
		this.homeSupplyFor = homeSupplyFor;
	}
}


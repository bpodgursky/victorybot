import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TerritorySquare {
	
	//all borders
	Set<TerritorySquare> borders = new HashSet<TerritorySquare>();
	
	//all land borders (army can move between)
	Set<TerritorySquare> landBorders = new HashSet<TerritorySquare>();
	
	//only NC/SC or EC/WC for now
	Set<String> coasts = new HashSet<String>();
	
	//sea borders (fleet can move between) -- indexed by which coast
	Map<String, Set<TerritorySquare>> seaBorders = new HashMap<String, Set<TerritorySquare>>();
	
	//all sea borders
	Set<TerritorySquare> allSeaBorders = new HashSet<TerritorySquare>();

	//only call if there is only one coast
	public void borders(TerritorySquare other, boolean sharesCoast){
		borders.add(other);
		
		if(sharesCoast){
			seaBorders.get("DEFAULT").add(other);
			allSeaBorders.add(other);
		}
		
		if(other.isLand && this.isLand){
			landBorders.add(other);
		}
	}
	
	public void borders(TerritorySquare other, String borderCoastName){
		borders.add(other);
		
		seaBorders.get(borderCoastName).add(other);
		allSeaBorders.add(other);
	
		if(other.isLand){
			landBorders.add(other);
		}
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
		this(name, isSupply, isLand, controller, homeSupplyFor, null);
	}

	public TerritorySquare(String name, 
			boolean isSupply, boolean isLand,
			Player controller, 
			Player homeSupplyFor,
			Collection<String> coasts){
		
		this.name = name;
		this.isLand = isLand;
		this.isSupplyCenter = isSupply;
		this.controller = controller;
		this.homeSupplyFor = homeSupplyFor;
		
		if(coasts != null){
			this.coasts.addAll(coasts);
		}else{
			this.coasts.add("DEFAULT");
		}
		
		for(String s: this.coasts){
			this.seaBorders.put(s, new HashSet<TerritorySquare>());
		}
	}
}


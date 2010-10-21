import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TerritorySquare {
	
	//CONSTANT
	
	//all borders
	final Set<TerritorySquare> borders = new HashSet<TerritorySquare>();
	
	//all land borders (army can move between)
	final Set<TerritorySquare> landBorders = new HashSet<TerritorySquare>();
	
	//only NC/SC or EC/WC for now
	final Set<String> coasts = new HashSet<String>();
	
	//sea borders (fleet can move between) -- indexed by which coast
	final Map<String, Set<TerritorySquare>> seaBorders = new HashMap<String, Set<TerritorySquare>>();
	
	//all sea borders
	final Set<TerritorySquare> allSeaBorders = new HashSet<TerritorySquare>();

	final boolean isSupplyCenter;
	final boolean isLand;			//false if sea
	
	final Player homeSupplyFor;	//null if none
	
	final String name;
	
	//dynamic
	
	Player controller;		//null if none
	Unit occupier;			//null if none
	String occupiedCoast; 	//
	
	public void setController(Player p){
		controller = p;
	}
	
	public void setOccupier(Unit u, String coast) throws Exception{
		
		if(!coasts.contains(coast)){
			throw new Exception("Invalid coast!");
		}
		
		this.occupier = u;		
		this.occupiedCoast = coast;
	}
	
	public void setOccupier(Unit u) throws Exception{
		
		if(!u.army && this.coasts.size() > 1){
			throw new Exception("Must specify a coast!");
		}
		
		setOccupier(u, "NA");
	}
	
	//only call if there is only one coast
	public void borders(TerritorySquare other, boolean sharesCoast){
		borders.add(other);
		
		if(sharesCoast){
			seaBorders.get("NA").add(other);
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

	public TerritorySquare(String name, 
			boolean isSupply, boolean isLand,
			Player homeSupplyFor){
		this(name, isSupply, isLand, homeSupplyFor, null);
	}

	public TerritorySquare(String name, 
			boolean isSupply, boolean isLand,
			Player homeSupplyFor,
			Collection<String> coasts){
		
		this.name = name;
		this.isLand = isLand;
		this.isSupplyCenter = isSupply;
		this.homeSupplyFor = homeSupplyFor;
		
		if(coasts != null){
			this.coasts.addAll(coasts);
		}
		
		this.coasts.add("NA");
		
		for(String s: this.coasts){
			this.seaBorders.put(s, new HashSet<TerritorySquare>());
		}
	}
}


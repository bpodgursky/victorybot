package representation;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import state.constant.BoardConfiguration;
import state.dynamic.BoardState;



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
	
	//not 100% sure this needs to be here, but it's useful when assessing the validity of moves (by phase)
	public final BoardConfiguration board;
	
	//dynamic
	
	public void setController(BoardState bst, Player p){
		bst.setController(this, p);
	}
	
	public void setOccupier(BoardState bst, Unit u, String coast) throws Exception{
		
		if(!coasts.contains(coast)){
			throw new Exception("Invalid coast!");
		}
		
		bst.setOccupier(this, u);
		bst.setOccupiedCoast(this, coast);
	}
	
	public void setOccupier(BoardState bst, Unit u) throws Exception{
		
		if(u != null && !u.army && this.coasts.size() > 1){
			throw new Exception("Must specify a coast!");
		}
		
		setOccupier(bst, u, "NA");
	}
	
	//	only call if there is only one coast
	public void setBorders(TerritorySquare other, boolean sharesCoast){
		borders.add(other);
		
		if(sharesCoast){
			seaBorders.get("NA").add(other);
			allSeaBorders.add(other);
		}
		
		if(other.isLand && this.isLand){
			landBorders.add(other);
		}
	}
	
	public void setBorders(TerritorySquare other, String borderCoastName){
		borders.add(other);
		
		seaBorders.get(borderCoastName).add(other);
		seaBorders.get("NA").add(other);
		
		allSeaBorders.add(other);
	
		if(other.isLand){
			landBorders.add(other);
		}
	}
	
	public String getOccupiedCoast(BoardState bst){
		return bst.getOccupiedCoast(this);
	}
	
	public boolean isLandBorder(TerritorySquare other){
		return landBorders.contains(other);
	}
	
	public boolean isAnySeaBorder(TerritorySquare other){
		return this.allSeaBorders.contains(other);
	}
	
	public boolean hasAnySeaBorders(){
		return !allSeaBorders.isEmpty();
	}

	public boolean isSeaBorder(TerritorySquare other, String thisCoast, String otherCoast){
		
		return this.seaBorders.get(thisCoast).contains(other) &&
			  other.seaBorders.get(otherCoast).contains(this);
	}
	
	public TerritorySquare(String name, 
			boolean isSupply, boolean isLand,
			Player homeSupplyFor, BoardConfiguration state){
		this(name, isSupply, isLand, homeSupplyFor, null, state);
	}
	
	public Unit getOccupier(BoardState bst){
		return bst.getOccupier(this);
	}

	public TerritorySquare(String name, 
			boolean isSupply, boolean isLand,
			Player homeSupplyFor,
			Collection<String> coasts,
			BoardConfiguration state){
		
		this.name = name;
		this.isLand = isLand;
		this.isSupplyCenter = isSupply;
		this.homeSupplyFor = homeSupplyFor;
		this.board = state;
		
		if(coasts != null){
			this.coasts.addAll(coasts);

		}
		
		this.coasts.add("NA");
		
		for(String s: this.coasts){
			this.seaBorders.put(s, new HashSet<TerritorySquare>());
		}
	}
	
	public Player getHomePlayer(){
		return this.homeSupplyFor;
	}

	
	public boolean hasMultipleCoasts(){
		return this.coasts.size() > 1;
	}
	
	public boolean isSupplyCenter(){
		return this.isSupplyCenter;
	}
	
	public boolean isControlled(BoardState bst){
		return bst.getController(this) != null;
	}
	
	public Player getController(BoardState bst){
		return bst.getController(this);
	}
	
	public boolean hasCoast(String coast){
		return this.coasts.contains(coast);
	}
	
	public boolean isLand(){
		return this.isLand;
	}
	
	public Set<TerritorySquare> getBorders(){
		return new HashSet<TerritorySquare>(this.borders);
	}
	
	
	public String toString(BoardState bst){
		return "[Territory "+name+" controlled by "+ ((bst.getController(this)==null)?"":bst.getController(this).getName()) + " occupied by "+ bst.getOccupier(this)+" coast " + bst.getOccupiedCoast(this)+"]";
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getUnitString(BoardState bst){
		
		if(bst.getOccupier(this) == null){
			return "(unoccupied)";
		}else if(!bst.getOccupier(this).army && this.hasMultipleCoasts()){
			return bst.getOccupier(this)+ " ( "+this.name+" "+bst.getOccupiedCoast(this)+" )";
		}else{
			return bst.getOccupier(this)+ " "+this.name;
		}
	}
	
	public Set<String> getCoasts(){
		return coasts;
	}
	
	//for building orders
	
	public static String getDestString(Unit unit, String destination, String coast){
		if(unit.army || coast.equals("NA")){
			return destination;
		}else{
			return "("+destination+" "+coast+")";
		}
	}
	
	public static String getUnitString(Player pow, Unit unit, String square){
		return unit+" "+square;
	}
	
	public static String getUnitString(Player pow, Unit unit, String square, String coast){
		
		if(unit.army || coast.equals("NA")){
			return getUnitString(pow, unit, square);
		}
		
		return unit+" "+square+" "+coast;
	}
}


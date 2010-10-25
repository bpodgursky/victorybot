package representation;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import state.BoardState;



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
	public final BoardState board;
	
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
		
		if(u != null && !u.army && this.coasts.size() > 1){
			throw new Exception("Must specify a coast!");
		}
		
		setOccupier(u, "NA");
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
	
	public String getOccupiedCoast(){
		return this.occupiedCoast;
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
			Player homeSupplyFor, BoardState state){
		this(name, isSupply, isLand, homeSupplyFor, null, state);
	}
	
	public Unit getOccupier(){
		return this.occupier;
	}

	public TerritorySquare(String name, 
			boolean isSupply, boolean isLand,
			Player homeSupplyFor,
			Collection<String> coasts,
			BoardState state){
		
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
	
	public boolean isControlled(){
		return this.controller != null;
	}
	
	public Player getController(){
		return this.controller;
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
	
	
	public String toString(){
		return "[Territory "+name+" controlled by "+ ((controller==null)?"":controller.getName()) + " occupied by "+ this.occupier +" coast " + this.occupiedCoast+"]";
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getUnitString(){
		if(!this.occupier.army && this.hasMultipleCoasts()){
			return this.occupier+ " ( "+this.name+" "+this.getOccupiedCoast()+" )";
		}else{
			return this.occupier+ " "+this.name;
		}
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


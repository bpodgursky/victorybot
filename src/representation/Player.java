package representation;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import state.constant.BoardConfiguration;



public class Player {

	//constant
	
	public final BoardConfiguration board;
	public final Country power;  
	
	final Set<TerritorySquare> homeCenters = new HashSet<TerritorySquare>();

	
	//dynamic
	
	public Player(Country power, BoardConfiguration board){
		
		this.board = board;
		this.power = power;
		this.homeCenters.addAll(homeCenters);
	}
	
	public Set<TerritorySquare> getOccupiedTerritories(){
		return this.occupiedTerritories;
	}
	
	public int getNumberUnits(){
		return this.occupiedTerritories.size();
	}
	
	public Set<TerritorySquare> getControlledTerritories(){
		return this.supplyCenters;
	}
	
	public int getNumberSupplyCenters(){
		return this.supplyCenters.size();
	}
	
	public void setHomeSupply(Collection<TerritorySquare> homeSupply){
		this.homeCenters.addAll(homeSupply);
	}
	
	public void addSupply(TerritorySquare sqr) throws Exception{
		
		if(supplyCenters.contains(sqr)){
			throw new Exception("Already control this supply!");
		}
		
		this.supplyCenters.add(sqr);
	}
	
	public void removeSupply(TerritorySquare sqr) throws Exception{
		
		if(!supplyCenters.contains(sqr)){
			throw new Exception("Don't control this supply!");
		}
		
		this.supplyCenters.remove(sqr);
	}
	
	public void addOccupy(TerritorySquare sqr) throws Exception{
		
		if(occupiedTerritories.contains(sqr)){
			throw new Exception("Already occupy this!");
		}
		
		this.occupiedTerritories.add(sqr);
	}

	public void removeOccupy(TerritorySquare sqr) throws Exception{
		
		if(!occupiedTerritories.contains(sqr)){
			throw new Exception("Territory not occupied!");
		}
		
		this.occupiedTerritories.remove(sqr);
	}
	
	public String getName(){
		return this.power.toString();
	}
	
	public String toString(){
		String str = power+": ";
		
		str+="Controls [";
		for(TerritorySquare sqr: this.supplyCenters){
			str+=sqr.name+", ";
		}
		str+="] ";
		
		str+="Occupies [";
		for(TerritorySquare sqr: this.occupiedTerritories){
			str+=(sqr.occupier.army?"A":"F")+" "+sqr.name+", ";
		}
		str+="]";
		
		return str;
	}
}

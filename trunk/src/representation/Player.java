package representation;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;



public class Player {

	//constant
	
	final Country power;  
	final Set<TerritorySquare> homeCenters = new HashSet<TerritorySquare>();
	
	//dynamic
	
	//the set of territories you have units in 
	final Set<TerritorySquare> occupiedTerritories = new HashSet<TerritorySquare>();
	
	//the ones you control.  Note, the territory will have a unit in it, that will indicate
	//whether it is army or navy.
	final Set<TerritorySquare> supplyCenters = new HashSet<TerritorySquare>();

	
	public Player(Country power){
		
		this.power = power;
		this.homeCenters.addAll(homeCenters);
	}
	
	public Set<TerritorySquare> getOccupiedTerritories(){
		return this.occupiedTerritories;
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

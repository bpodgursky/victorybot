package representation;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import state.constant.BoardConfiguration;
import state.dynamic.BoardState;



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
	
	public int hashCode2(){
		return power.hashCode();
	}
	
	public Set<TerritorySquare> getOccupiedTerritories(BoardState bst){
		return bst.getOccupiedTerritories(this);
	}
	
	public int getNumberUnits(BoardState bst){
		return bst.getOccupiedTerritories(this).size();
	}
	
	public Set<TerritorySquare> getHomeCenters(){
		return homeCenters;
	}
	
	public Set<TerritorySquare> getControlledTerritories(BoardState bst){
		return bst.getSupplyCenters(this);
	}
	
	public int getNumberSupplyCenters(BoardState bst){
		return bst.getSupplyCenters(this).size();
	}
	
	public void setHomeSupply(Collection<TerritorySquare> homeSupply){
		this.homeCenters.addAll(homeSupply);
	}
	
	public void addSupply(BoardState bst, TerritorySquare sqr) throws Exception{
		
		if(bst.getSupplyCenters(this).contains(sqr)){
			throw new Exception("Already control this supply!");
		}
		
		bst.getSupplyCenters(this).add(sqr);
	}
	
	public void removeSupply(BoardState bst, TerritorySquare sqr) throws Exception{
		
		if(!bst.getSupplyCenters(this).contains(sqr)){
			throw new Exception("Don't control this supply!");
		}
		
		bst.getSupplyCenters(this).remove(sqr);
		
	}
	
	public void addOccupy(BoardState bst, TerritorySquare sqr) throws Exception{
		
		if(bst.getOccupiedTerritories(this).contains(sqr)){
			throw new Exception("Already occupy this!");
		}
		
		bst.getOccupiedTerritories(this).add(sqr);
	}

	public void removeOccupy(BoardState bst, TerritorySquare sqr) throws Exception{
		
		if(!bst.getOccupiedTerritories(this).contains(sqr)){
			throw new Exception("Territory "+sqr.getName()+" not occupied!");
		}
		
		bst.getOccupiedTerritories(this).remove(sqr);
	}
	
	public String getName(){
		return this.power.toString();
	}
	
	public String toString(){
		return this.power.toString();
	}
	
	public String toString(BoardState bst){
		String str = power+": ";
		
		str+="Controls [";
		for(TerritorySquare sqr: bst.getSupplyCenters(this)){
			str+=sqr.name+", ";
		}
		str+="] ";
		
		str+="Occupies [";
		for(TerritorySquare sqr: bst.getOccupiedTerritories(this)){
			str+=(bst.getOccupier(sqr).army?"A":"F")+" "+sqr.name+", ";
		}
		str+="]";
		
		return str;
	}
}

package state.dynamic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import order.Order;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.constant.BoardConfiguration;
import state.constant.BoardConfiguration.YearPhase;

//	the general contract: don't change data in the class except 
//	from within the update method in BoardConfiguration

public class BoardState {

	public final BoardConfiguration configuration;
	
	public final YearPhase time;
	
	public BoardState(YearPhase time, BoardConfiguration config){
//		
		this.time = time;
		this.configuration = config;
		
		for(Player p: config.getPlayers()){
			occupiedTerritories.put(p, new HashSet<TerritorySquare>());
		}
		
		for(Player p: config.getPlayers()){
			supplyCenters.put(p, new HashSet<TerritorySquare>());
		}
	}
	
	//dynamic data for a player
	
	/////////////////////////////////////////
	//	state data
	/////////////////////////////////////////
	
	//the set of territories you have units in 
	final Map<Player, Set<TerritorySquare>> occupiedTerritories = new HashMap<Player, Set<TerritorySquare>>();
	
	//the ones you control.  Note, the territory will have a unit in it, that will indicate
	//whether it is army or navy.
	final Map<Player, Set<TerritorySquare>> supplyCenters = new HashMap<Player, Set<TerritorySquare>>();

	//dynamic data for a territorysquare
	
	final Map<TerritorySquare, Player> controller = new HashMap<TerritorySquare, Player>();		//null if none
	final Map<TerritorySquare, Unit> occupier = new HashMap<TerritorySquare, Unit>();			//null if none
	final Map<TerritorySquare, String> occupiedCoast = new HashMap<TerritorySquare, String>(); 	//
	
	//spring = first moves
	//sum = retreats after first
	//fall = second moves
	//aut = retreats after fall
	//win = builds and disbands
	//public final Phase currentPhase;
	
	//public final int currentYear;
	
	MoveHistory history = new MoveHistory();
	
	final Map<TerritorySquare, RetreatSituation> retreats = new HashMap<TerritorySquare, RetreatSituation>();
	
	
	
	//////////////////////////////////////////////////////
	//	methods
	//////////////////////////////////////////////////////
	
	public Collection<RetreatSituation> getRetreats(){
		return retreats.values();
	}
	
	public void updateHistory(int year, Phase phase, Set<Order> orders){
		this.history.add(year, phase, orders);
	}
	
	public Set<TerritorySquare> getOccupiedTerritories(Player p){
		return occupiedTerritories.get(p);
	}
	
	public Player getController(TerritorySquare terr){
		return controller.get(terr);
	}
	
	public void setController(TerritorySquare terr, Player p){
		this.controller.put(terr, p);
	}
	
	public Unit getOccupier(TerritorySquare terr){
		return occupier.get(terr);
	}
	
	public void setOccupier(TerritorySquare terr, Unit u){
		this.occupier.put(terr, u);
	}
	
	public Set<TerritorySquare> getSupplyCenters(Player p){
		return supplyCenters.get(p);
	}
	
	public String getOccupiedCoast(TerritorySquare sqr){
		return this.occupiedCoast.get(sqr);
	}
	
	public void setOccupiedCoast(TerritorySquare terr, String coast){
		this.occupiedCoast.put(terr, coast);
	}
	
	//	a map of territories which have to retreat this turn.  A little awkward; the territory's state will already
	//	have been updated, so a new unit will be in the territory.  So this map is from the territory to the unit
	//	which needs to retreat from there.  To make things more annoying, we need to remember which coast to retreat
	//	from for the countries with multiple coasts.  This class encompasses all of that
	
	public class RetreatSituation{
	
		public RetreatSituation(Unit u, TerritorySquare sqr, String orig){
			this.retreating = u;
			this.from = sqr;
			this.originCoast = orig;
		}
		
		public final Unit retreating;
		public final TerritorySquare from;
		
		public final String originCoast;
	}
	
	//	only public for testing...
	public void setRetreatingUnit(Unit retreating, TerritorySquare sqr, String originCoast){
		retreats.put(sqr, new RetreatSituation(retreating, sqr, originCoast));
	}
	
	public void resolveRetreat(TerritorySquare from){
		retreats.remove(from);
	}
	
	public Unit getRetreatingUnit(TerritorySquare sq){
		return retreats.get(sq).retreating;
	}
	
	public boolean isValidRetreat(TerritorySquare from, TerritorySquare terr) throws Exception{
		return history.isValidRetreat(this, from, terr);
	}
	
	public enum Phase{SPR, SUM, FAL, AUT, WIN}
	

	public RetreatSituation getRetreatForTerritory(TerritorySquare terr){
		return retreats.get(terr);
	}
	
	public BoardState clone(YearPhase time){
		
		BoardState copy = new BoardState(time, this.configuration);
	
		for(Player p: this.occupiedTerritories.keySet()){
			copy.occupiedTerritories.put(p, new HashSet<TerritorySquare>(this.occupiedTerritories.get(p)));	
		}
		
		for(Player p: this.supplyCenters.keySet()){
			copy.supplyCenters.put(p, new HashSet<TerritorySquare>(this.supplyCenters.get(p)));	
		}

		copy.controller.putAll(this.controller);
		copy.occupier.putAll(this.occupier);
		copy.occupiedCoast.putAll(this.occupiedCoast);
		
		copy.retreats.putAll(this.retreats);
		
		copy.history = history.clone();
		
		return copy;
	}

}

package state.dynamic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import representation.Country;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;

public class BoardState {

	//dynamic data for a player
	
	//the set of territories you have units in 
	final Map<Player, Set<TerritorySquare>> occupiedTerritories = new HashMap<Player, Set<TerritorySquare>>();
	
	public Set<TerritorySquare> getOccupiedTerritories(Player p){
		return occupiedTerritories.get(p);
	}
	
	//the ones you control.  Note, the territory will have a unit in it, that will indicate
	//whether it is army or navy.
	final Map<Player, Set<TerritorySquare>> supplyCenters = new HashMap<Player, Set<TerritorySquare>>();
	
	public Set<TerritorySquare> getSupplyCenters(Player p){
		return supplyCenters.get(p);
	}
	
	//dynamic data for a territorysquare
	
	final Map<TerritorySquare, Player> controller = new HashMap<TerritorySquare, Player>();		//null if none
	final Map<TerritorySquare, Unit> occupier = new HashMap<TerritorySquare, Unit>();			//null if none
	final Map<TerritorySquare, String> occupiedCoast = new HashMap<TerritorySquare, String>(); 	//
	
	public Player getController(TerritorySquare terr){
		return controller.get(terr);
	}
	
	public Unit getOccupier(TerritorySquare terr){
		return occupier.get(terr);
	}
	
	//	state
	
	//	a map of territories which have to retreat this turn.  A little awkward; the territory's state will already
	//	have been updated, so a new unit will be in the territory.  So this map is from the territory to the unit
	//	which needs to retreat from there.  To make things more annoying, we need to remember which coast to retreat
	//	from for the countries with multiple coasts.  This class encompasses all of that
	
	class RetreatSituation{
	
		public RetreatSituation(Unit u, TerritorySquare sqr, String orig){
			this.retreating = u;
			this.from = sqr;
			this.originCoast = orig;
		}
		
		Unit retreating;
		TerritorySquare from;
		
		String originCoast;
	}
	
	
	public enum Phase{SPR, SUM, FAL, AUT, WIN}
	
	//spring = first moves
	//sum = retreats after first
	//fall = second moves
	//aut = retreats after fall
	//win = builds and disbands
	public final Phase currentPhase;
	
	public final int currentYear;
	
	final Map<TerritorySquare, RetreatSituation> retreats = new HashMap<TerritorySquare, RetreatSituation>();
	
	public RetreatSituation getRetreatForTerritory(TerritorySquare terr){
		return retreats.get(terr);
	}
	
	MoveHistory history = new MoveHistory();
}

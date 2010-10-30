package state;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import order.Order;
import order.Order.Result;
import order.Order.RetreatState;
import order.builds.Build;
import order.builds.Remove;
import order.builds.Waive;
import order.retreats.Disband;
import order.retreats.Retreat;
import order.spring_fall.Convoy;
import order.spring_fall.Hold;
import order.spring_fall.Move;
import order.spring_fall.MoveByConvoy;
import order.spring_fall.SupportHold;
import order.spring_fall.SupportMove;

import representation.Country;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;


public class BoardState {
	
	final static boolean SUPPLY = true;
	final static boolean NO_SUPPLY = false;
	
	final static boolean ARMY = true;
	final static boolean FLEET = false;
	
	final static boolean LAND = true;
	final static boolean SEA = false;
	
	
	public enum Phase{SPR, SUM, FAL, AUT, WIN}
	
	//spring = first moves
	//sum = retreats after first
	//fall = second moves
	//aut = retreats after fall
	//win = builds and disbands
	Phase currentPhase;
	
	public Phase getCurrentPhase(){
		return currentPhase;
	}
	
	int currentYear;
	
	//constant
	
	final Map<String, TerritorySquare> terrs = new HashMap<String, TerritorySquare>();
	final Map<Country, Player> activePlayers = new HashMap<Country, Player>();
	
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
	
	Map<TerritorySquare, RetreatSituation> retreats = new HashMap<TerritorySquare, RetreatSituation>();
	
	MoveHistory history = new MoveHistory();
	
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
	
	public BoardState() throws Exception{
		initialize();
	}
	
	public void setTime(String phase, int year){
		this.currentPhase = Phase.valueOf(phase);
		this.currentYear = year;
	}
	
	//these are public, but only really for testing.  
	
	//set the controller of a territory
	//once controlled, control is only lost by someone else taking control.  So don't
	//need a remove method
	public void setControl(Player p, TerritorySquare terr) throws Exception{
		
		if(!terr.isSupplyCenter()){
			throw new Exception("Territory "+terr.getName()+" not supply center");
		}
		
		if(terr.getController() == p){
			throw new Exception("Player "+p.getName()+" already controls center "+terr.getName());
		}
		
		if(terr.isControlled()){
			terr.getController().removeSupply(terr);
		}
		
		p.addSupply(terr);
		terr.setController(p);
	}
	
	public void setOccupier(Unit u, TerritorySquare terr, String coast) throws Exception{
		
		if(!terr.hasCoast(coast)){
			throw new Exception("Invalid coast "+coast+" for terr "+terr.getName());
		}
		
		if(u.army && !terr.isLand()){
			throw new Exception("Invalid occupier for terr "+terr.getName());
		}
		
		if(terr.getOccupier() != null){
			throw new Exception("territory "+terr.getName()+" already occupied");
		}
		
		u.belongsTo.addOccupy(terr);
		terr.setOccupier(u, coast);
	}
	
	public void setOccupier(Unit u, TerritorySquare terr) throws Exception{
		
		if(!u.army && terr.hasMultipleCoasts()){
			throw new Exception("Must specify a coast for "+terr.getName());
		}
		
		setOccupier(u, terr, "NA");
	}
	
	public Unit removeOccupier(TerritorySquare terr) throws Exception{
		
		Unit removed = terr.getOccupier();
		
		if(removed == null){
			throw new Exception("Territory "+terr.getName()+" not occupied!");
		}
		
		removed.belongsTo.removeOccupy(terr);
		
		terr.setOccupier(null);
		
		return removed;
	}
	
	
	private void initialize() throws Exception{
		
		currentPhase = Phase.SPR;
		
		Player eng = new Player(Country.ENG, this);
		Player fra = new Player(Country.FRA, this);
		Player ger = new Player(Country.GER, this);
		Player rus = new Player(Country.RUS, this);
		Player ita = new Player(Country.ITA, this);
		Player aus = new Player(Country.AUS, this);
		Player tur = new Player(Country.TUR, this);
		
		activePlayers.put(Country.ENG, eng);
		activePlayers.put(Country.FRA, fra);
		activePlayers.put(Country.GER, ger);
		activePlayers.put(Country.RUS, rus);
		activePlayers.put(Country.ITA, ita);
		activePlayers.put(Country.AUS, aus);
		activePlayers.put(Country.TUR, tur);
		
		//england
		terrs.put("EDI", new TerritorySquare("EDI", SUPPLY, LAND, eng, this));
		terrs.put("LVP", new TerritorySquare("LVP", SUPPLY, LAND, eng, this));
		terrs.put("LON", new TerritorySquare("LON", SUPPLY, LAND, eng, this));
		
		eng.setHomeSupply(Arrays.asList(get("EDI"), get("LVP"), get("LON")));
		
		//russia
		terrs.put("STP", new TerritorySquare("STP", SUPPLY, LAND, rus, Arrays.asList("NCS", "SCS"), this));
		terrs.put("MOS", new TerritorySquare("MOS", SUPPLY, LAND, rus, this));
		terrs.put("WAR", new TerritorySquare("WAR", SUPPLY, LAND, rus, this));
		terrs.put("SEV", new TerritorySquare("SEV", SUPPLY, LAND, rus, this));
		
		rus.setHomeSupply(Arrays.asList(get("STP"), get("MOS"), get("WAR"), get("SEV")));

		//france
		terrs.put("BRE", new TerritorySquare("BRE", SUPPLY, LAND, fra, this));
		terrs.put("PAR", new TerritorySquare("PAR", SUPPLY, LAND, fra, this));
		terrs.put("MAR", new TerritorySquare("MAR", SUPPLY, LAND, fra, this));

		fra.setHomeSupply(Arrays.asList(get("BRE"), get("PAR"), get("MAR")));
		
		//germany
		terrs.put("KIE", new TerritorySquare("KIE", SUPPLY, LAND, ger, this));
		terrs.put("BER", new TerritorySquare("BER", SUPPLY, LAND, ger, this));
		terrs.put("MUN", new TerritorySquare("MUN", SUPPLY, LAND, ger, this));

		ger.setHomeSupply(Arrays.asList(get("KIE"), get("BER"), get("MUN")));
		
		//italy
		terrs.put("ROM", new TerritorySquare("ROM", SUPPLY, LAND, ita, this));
		terrs.put("NAP", new TerritorySquare("NAP", SUPPLY, LAND, ita, this));
		terrs.put("VEN", new TerritorySquare("VEN", SUPPLY, LAND, ita, this));

		ita.setHomeSupply(Arrays.asList(get("ROM"), get("NAP"), get("VEN")));
		
		//austria
		terrs.put("VIE", new TerritorySquare("VIE", SUPPLY, LAND, aus, this));
		terrs.put("TRI", new TerritorySquare("TRI", SUPPLY, LAND, aus, this));
		terrs.put("BUD", new TerritorySquare("BUD", SUPPLY, LAND, aus, this));
		
		aus.setHomeSupply(Arrays.asList(get("VIE"), get("TRI"), get("BUD")));
		
		//turkey
		terrs.put("CON", new TerritorySquare("CON", SUPPLY, LAND, tur, this));
		terrs.put("ANK", new TerritorySquare("ANK", SUPPLY, LAND, tur, this));
		terrs.put("SMY", new TerritorySquare("SMY", SUPPLY, LAND, tur, this));

		tur.setHomeSupply(Arrays.asList(get("CON"), get("ANK"), get("SMY")));
		
		//other supply centers
		terrs.put("NWY", new TerritorySquare("NWY", SUPPLY, LAND, null, this));
		terrs.put("SWE", new TerritorySquare("SWE", SUPPLY, LAND, null, this));
		terrs.put("DEN", new TerritorySquare("DEN", SUPPLY, LAND, null, this));
		terrs.put("HOL", new TerritorySquare("HOL", SUPPLY, LAND, null, this));
		terrs.put("BEL", new TerritorySquare("BEL", SUPPLY, LAND, null, this));
		terrs.put("SPA", new TerritorySquare("SPA", SUPPLY, LAND, null, Arrays.asList("NCS", "SCS"), this));
		terrs.put("POR", new TerritorySquare("POR", SUPPLY, LAND, null, this));
		terrs.put("TUN", new TerritorySquare("TUN", SUPPLY, LAND, null, this));
		terrs.put("SER", new TerritorySquare("SER", SUPPLY, LAND, null, this));
		terrs.put("RUM", new TerritorySquare("RUM", SUPPLY, LAND, null, this));
		terrs.put("BUL", new TerritorySquare("BUL", SUPPLY, LAND, null, Arrays.asList("ECS", "SCS"), this));
		terrs.put("GRE", new TerritorySquare("GRE", SUPPLY, LAND, null, this));
		
		//other non supply center land
		terrs.put("FIN", new TerritorySquare("FIN",	NO_SUPPLY, LAND, null, this));
		terrs.put("LVN", new TerritorySquare("LVN",	NO_SUPPLY, LAND, null, this));		
		terrs.put("PRU", new TerritorySquare("PRU",	NO_SUPPLY, LAND, null, this));
		terrs.put("SIL", new TerritorySquare("SIL",	NO_SUPPLY, LAND, null, this));
		terrs.put("GAL", new TerritorySquare("GAL",	NO_SUPPLY, LAND, null, this));		
		terrs.put("UKR", new TerritorySquare("UKR",	NO_SUPPLY, LAND, null, this));
		terrs.put("BOH", new TerritorySquare("BOH",	NO_SUPPLY, LAND, null, this));
		terrs.put("RUH", new TerritorySquare("RUH",	NO_SUPPLY, LAND, null, this));
		terrs.put("BUR", new TerritorySquare("BUR",	NO_SUPPLY, LAND, null, this));
		terrs.put("GAS", new TerritorySquare("GAS",	NO_SUPPLY, LAND, null, this));
		terrs.put("NAF", new TerritorySquare("NAF",	NO_SUPPLY, LAND, null, this));
		terrs.put("PIE", new TerritorySquare("PIE",	NO_SUPPLY, LAND, null, this));
		terrs.put("TUS", new TerritorySquare("TUS",	NO_SUPPLY, LAND, null, this));
		terrs.put("APU", new TerritorySquare("APU",	NO_SUPPLY, LAND, null, this));
		terrs.put("ALB", new TerritorySquare("ALB",	NO_SUPPLY, LAND, null, this));
		terrs.put("ARM", new TerritorySquare("ARM",	NO_SUPPLY, LAND, null, this));
		terrs.put("SYR", new TerritorySquare("SYR",	NO_SUPPLY, LAND, null, this));
		terrs.put("CLY", new TerritorySquare("CLY",	NO_SUPPLY, LAND, null, this));
		terrs.put("YOR", new TerritorySquare("YOR",	NO_SUPPLY, LAND, null, this));
		terrs.put("WAL", new TerritorySquare("WAL",	NO_SUPPLY, LAND, null, this));
		terrs.put("PIC", new TerritorySquare("PIC", NO_SUPPLY, LAND, null, this));
		terrs.put("TYR", new TerritorySquare("TYR", NO_SUPPLY, LAND, null, this));
				
		//sea territories
		terrs.put("NAO", new TerritorySquare("NAO",	NO_SUPPLY, SEA, null, this));
		terrs.put("NWG", new TerritorySquare("NWG",	NO_SUPPLY, SEA, null, this));
		terrs.put("NTH", new TerritorySquare("NTH",	NO_SUPPLY, SEA, null, this));
		terrs.put("BAR", new TerritorySquare("BAR",	NO_SUPPLY, SEA, null, this));
		terrs.put("BAL", new TerritorySquare("BAL",	NO_SUPPLY, SEA, null, this));
		terrs.put("GOB", new TerritorySquare("GOB",	NO_SUPPLY, SEA, null, this));
		terrs.put("IRI", new TerritorySquare("IRI",	NO_SUPPLY, SEA, null, this));
		terrs.put("SKA", new TerritorySquare("SKA",	NO_SUPPLY, SEA, null, this));
		terrs.put("HEL", new TerritorySquare("HEL",	NO_SUPPLY, SEA, null, this));
		terrs.put("ECH", new TerritorySquare("ECH",	NO_SUPPLY, SEA, null, this));
		terrs.put("MAO", new TerritorySquare("MAO",	NO_SUPPLY, SEA, null, this));
		terrs.put("WES", new TerritorySquare("WES",	NO_SUPPLY, SEA, null, this));
		terrs.put("GOL", new TerritorySquare("GOL",	NO_SUPPLY, SEA, null, this));
		terrs.put("TYS", new TerritorySquare("TYS",	NO_SUPPLY, SEA, null, this));
		terrs.put("ION", new TerritorySquare("ION",	NO_SUPPLY, SEA, null, this));
		terrs.put("ADR", new TerritorySquare("ADR",	NO_SUPPLY, SEA, null, this));
		terrs.put("AEG", new TerritorySquare("AEG",	NO_SUPPLY, SEA, null, this));
		terrs.put("EAS", new TerritorySquare("EAS",	NO_SUPPLY, SEA, null, this));
		terrs.put("BLA", new TerritorySquare("BLA",	NO_SUPPLY, SEA, null, this));

		//ocean adjacencies
		
		border("NAO", "NWG", true);
		border("NAO", "CLY", true);
		border("NAO", "IRI", true);
		border("NAO", "MAO", true);
		
		border("NWG", "BAR", true);
		border("NWG", "NWY", true);
		border("NWG", "NTH", true);
		border("NWG", "EDI", true);
		border("NWG", "CLY", true);
		
		border("BAR", "STP", "NA", "NCS");
		border("BAR", "NWY", true);
		
		border("MAO", "IRI", true);
		border("MAO", "ECH", true);
		border("MAO", "BRE", true);
		border("MAO", "GAS", true);
		border("MAO", "SPA", "NA", "NCS");
		border("MAO", "SPA", "NA", "SCS");
		border("MAO", "POR", true);
		border("MAO", "WES", true);
		border("MAO", "NAF", true);
		
		border("IRI", "CLY", true);
		border("IRI", "LVP", true);
		border("IRI", "WAL", true);
		border("IRI", "ECH", true);
		
		border("NTH", "SKA", true);
		border("NTH", "NWY", true);
		border("NTH", "DEN", true);
		border("NTH", "HOL", true);
		border("NTH", "BEL", true);
		border("NTH", "LON", true);
		border("NTH", "YOR", true);
		border("NTH", "EDI", true);
		border("NTH", "HEL", true);
		border("NTH", "ECH", true);
		
		border("HEL", "DEN", true);
		border("HEL", "KIE", true);
		border("HEL", "HOL", true);
		
		border("SKA", "NWY", true);
		border("SKA", "SWE", true);
		border("SKA", "DEN", true);
		
		border("BAL", "SWE", true);
		border("BAL", "GOB", true);
		border("BAL", "LVN", true);
		border("BAL", "PRU", true);
		border("BAL", "BER", true);
		border("BAL", "KIE", true);
		border("BAL", "DEN", true);
		
		border("GOB", "SWE", true);
		border("GOB", "FIN", true);
		border("GOB", "STP", "NA", "SCS");
		border("GOB", "LVN", true);
		
		border("ECH", "WAL", true);
		border("ECH", "LON", true);
		border("ECH", "BEL", true);
		border("ECH", "PIC", true);
		border("ECH", "BRE", true);
		
		border("WES", "SPA", "NA", "SCS");
		border("WES", "GOL", true);
		border("WES", "TYS", true);
		border("WES", "TUN", true);
		border("WES", "NAF", true);
		
		border("GOL", "SPA", "NA", "SCS");
		border("GOL", "MAR", true);
		border("GOL", "PIE", true);
		border("GOL", "TUS", true);
		border("GOL", "TYS", true);
		
		border("TYS", "TUS", true);
		border("TYS", "ROM", true);
		border("TYS", "NAP", true);
		border("TYS", "ION", true);
		border("TYS", "TUN", true);
		
		border("ION", "NAP", true);
		border("ION", "ADR", true);
		border("ION", "ALB", true);
		border("ION", "GRE", true);
		border("ION", "AEG", true);
		border("ION", "EAS", true);
		border("ION", "TUN", true);
		
		border("ADR", "APU", true);
		border("ADR", "VEN", true);
		border("ADR", "TRI", true);
		border("ADR", "ALB", true);
		
		border("AEG", "GRE", true);
		border("AEG", "BUL", "NA", "SCS");
		border("AEG", "CON", true);
		border("AEG", "SMY", true);
		border("AEG", "EAS", true);
		border("AEG", "BLA", true);
		
		border("EAS", "SMY", true);
		border("EAS", "SYR", true);
		
		border("BLA", "BUL", "NA", "ECS");
		border("BLA", "RUM", true);
		border("BLA", "SEV", true);
		border("BLA", "ANK", true);
		border("BLA", "CON", true);
		border("BLA", "ARM", true);
		border("BLA", "CON", true);
		
		//england
		
		border("CLY", "EDI", true);
		border("CLY", "LVP", true);
		
		border("EDI", "YOR", true);
		border("EDI", "LVP", false);
		
		border("LVP", "YOR", false);
		border("LVP", "WAL", true);
		border("YOR", "LON", true);
		border("YOR", "WAL", false);
		
		border("WAL", "LON", true);
		
		//scandanavia
		
		border("NWY", "STP", "NA", "NCS");
		border("NWY", "FIN", false);
		border("NWY", "SWE", true);
		
		border("SWE", "FIN", true);
		border("SWE", "DEN", true);
		
		border("FIN", "STP", "NA", "SCS");
		
		border("DEN", "KIE", true);
		
		//Russia
		
		border("STP", "LVN", "SCS", "NA");
		border("STP", "MOS", false);
		
		border("LVN", "PRU", true);
		border("LVN", "WAR", false);
		border("LVN", "MOS", false);
		
		border("MOS", "WAR", false);
		border("MOS", "UKR", false);
		border("MOS", "SEV", false);
		
		border("WAR", "PRU", false);
		border("WAR", "SIL", false);
		border("WAR", "GAL", false);
		border("WAR", "UKR", false);
		
		border("UKR", "GAL", false);
		border("UKR", "RUM", false);
		border("UKR", "SEV", false);
		
		border("SEV", "RUM", true);
		border("SEV", "ARM", true);
		
		//germany
		
		border("KIE", "HOL", true);
		border("KIE", "RUH", false);
		border("KIE", "MUN", false);
		border("KIE", "BER", true);
		
		border("BER", "MUN", false);
		border("BER", "SIL", false);
		border("BER", "PRU", true);
		
		border("PRU", "SIL", false);
		
		border("RUH", "HOL", false);
		border("RUH", "BEL", false);
		border("RUH", "BUR", false);
		border("RUH", "MUN", false);
		
		border("MUN", "BUR", false);
		border("MUN", "TYR", false);
		border("MUN", "BOH", false);
		border("MUN", "SIL", false);
		
		border("SIL", "BOH", false);
		border("SIL", "GAL", false);

		//low countries

		border("HOL", "BEL", true);
		
		border("BEL", "PIC", true);
		border("BEL", "BUR", false);
		
		//france
		
		border("BRE", "GAS", true);
		border("BRE", "PAR", false);
		border("BRE", "PIC", true);
		
		border("PIC", "PAR", false);
		border("PIC", "BUR", false);
		
		border("GAS", "SPA", "NA", "NCS");
		border("GAS", "MAR", false);
		border("GAS", "BUR", false);
		border("GAS", "PAR", false);
		
		border("PAR", "BUR", false);
		
		border("MAR", "PIE", true);
		border("MAR", "BUR", false);
		border("MAR", "SPA", "NA", "SCS");
		
		//italy
		
		border("PIE", "VEN", false);
		border("PIE", "TUS", true);
		
		border("TUS", "ROM", true);
		border("TUS", "VEN", false);
		
		border("ROM", "VEN", false);
		border("ROM", "APU", false);
		border("ROM", "NAP", true);
		
		border("NAP", "APU", true);
		
		border("APU", "VEN", true);
		
		border("VEN", "TRI", true);
		border("VEN", "TYR", false);
		
		//austro hungary
		
		border("TYR", "BOH", false);
		border("TYR", "VIE", false);
		border("TYR", "TRI", false);
		border("TYR", "PIE", false);
		
		border("BOH", "GAL", false);
		border("BOH", "VIE", false);
		
		border("GAL", "RUM", false);
		border("GAL", "BUD", false);
		border("GAL", "VIE", false);
		
		border("VIE", "BUD", false);
		border("VIE", "TRI", false);
		
		border("BUD", "RUM", false);
		border("BUD", "SER", false);
		border("BUD", "TRI", false);
		
		border("TRI", "SER", false);
		border("TRI", "ALB", true);
		
		//turkey
		
		border("CON", "BUL", "NA", "SCS");
		border("CON", "BUL", "NA", "ECS");		
		border("CON", "ANK", true);
		border("CON", "SMY", true);
		
		border("ANK", "ARM", true);
		border("ANK", "SMY", false);
		
		border("SMY", "ARM", false);
		border("SMY", "SYR", true);
		
		border("SYR", "ARM", false);
		
		//balkans
		
		border("RUM", "SER", false);
		border("RUM", "BUL", "NA", "ECS");
		
		border("BUL", "SER", false);
		border("BUL", "GRE", "SCS", "NA");
		
		border("SER", "GRE", false);
		border("SER", "ALB", false);
		
		border("GRE", "ALB", true);
		
		//spain and africa

		border("SPA", "POR", "NCS", "NA");
		border("SPA", "POR", "SCS", "NA");
		
		border("NAF", "TUN", true);
		
		
		//set up units and control 
		
		setControl(eng, get("EDI"));
		setControl(eng, get("LVP"));
		setControl(eng, get("LON"));
		
		setOccupier(new Unit(eng, FLEET), get("EDI"));//EDI
		setOccupier(new Unit(eng, ARMY), get("LVP"));//LVP
		setOccupier(new Unit(eng, FLEET), get("LON"));
		
		setControl(rus, get("STP"));
		setControl(rus, get("MOS"));
		setControl(rus, get("WAR"));
		setControl(rus, get("SEV"));
		
		setOccupier(new Unit(rus, FLEET), get("STP"), "SCS");
		setOccupier(new Unit(rus, ARMY), get("MOS"));
		setOccupier(new Unit(rus, ARMY), get("WAR"));
		setOccupier(new Unit(rus, FLEET), get("SEV"));
		
		setControl(fra, get("BRE"));
		setControl(fra, get("PAR"));
		setControl(fra, get("MAR"));
		
		setOccupier(new Unit(fra, FLEET), get("BRE"));
		setOccupier(new Unit(fra, ARMY), get("PAR"));
		setOccupier(new Unit(fra, ARMY), get("MAR"));
		
		setControl(ger, get("KIE"));
		setControl(ger, get("BER"));
		setControl(ger, get("MUN"));
		
		setOccupier(new Unit(ger, FLEET), get("KIE"));
		setOccupier(new Unit(ger, ARMY), get("BER"));
		setOccupier(new Unit(ger, ARMY), get("MUN"));
		
		setControl(ita, get("ROM"));
		setControl(ita, get("NAP"));
		setControl(ita, get("VEN"));
		
		setOccupier(new Unit(ita, ARMY), get("ROM"));
		setOccupier(new Unit(ita, ARMY), get("VEN"));
		setOccupier(new Unit(ita, FLEET), get("NAP"));
		
		setControl(aus, get("VIE"));
		setControl(aus, get("TRI"));
		setControl(aus, get("BUD"));
		
		setOccupier(new Unit(aus, ARMY), get("VIE"));
		setOccupier(new Unit(aus, FLEET), get("TRI"));
		setOccupier(new Unit(aus, ARMY), get("BUD"));
		
		setControl(tur, get("CON"));
		setControl(tur, get("ANK"));
		setControl(tur, get("SMY"));
		
		setOccupier(new Unit(tur, ARMY), get("CON"));
		setOccupier(new Unit(tur, FLEET), get("ANK"));
		setOccupier(new Unit(tur, ARMY), get("SMY"));
		
	} 
	
	private String mapAsDotFile(){
	    String str = "digraph clusters {";
	    
	    
	    for(String s: this.terrs.keySet()){
	    	
	    	TerritorySquare sqr = get(s);
	    	for(TerritorySquare neighbor: sqr.getBorders()){
	    		
	    		//just to make sure there is only one of each border
	    		if(s.compareTo(neighbor.getName()) > 0){
	    			
	    			if(sqr.isLand() && neighbor.isLand())
	    				str+="\""+s+"\" -> \""+neighbor.getName()+"\"[dir=none weight=100];\n";
	    			else
	    				str+="\""+s+"\" -> \""+neighbor.getName()+"\"[dir=none weight=1];\n";
	    		}
	    	}
	    	
	    }
	    
	    return str+"}";
	}
	
	private void border(String t1, String t2, boolean shareCoast){
		TerritorySquare sq1 = get(t1);
		TerritorySquare sq2 = get(t2);
		
		sq1.setBorders(sq2, shareCoast);
		sq2.setBorders(sq1, shareCoast);
	}
	
	private void border(String t1, String t2, String sharedCoast1, String sharedCoast2){
		TerritorySquare sq1 = get(t1);
		TerritorySquare sq2 = get(t2);
		
		sq1.setBorders(sq2, sharedCoast1);
		sq2.setBorders(sq1, sharedCoast2);
	}
	
	//Holds the current location of all units
	
	public TerritorySquare get(String name){
		return terrs.get(name);
	}
	
	public void updateSupplyControl() throws Exception{
		
		for(Player p: this.activePlayers.values()){
			
			//for each of their units, set them as controlling the center (if it is one)
			
			Set<TerritorySquare> control = p.getControlledTerritories();
			for(TerritorySquare sq: p.getOccupiedTerritories()){
				if(sq.isSupplyCenter()){
					if(!control.contains(sq)){
						setControl(p, sq);
					}
				}
			}
		}
	}
	
	private void resolve(Set<Order> moves){
		//1) resolve moves
		
		//moves which want a location
		Map<TerritorySquare, Set<Order>> movesWantLocation = 
			new HashMap<TerritorySquare, Set<Order>>();
			
		//where moves come from
		Map<TerritorySquare, Order> moveOrigins = 
			new HashMap<TerritorySquare, Order>();
		
		//from the order to the orders which support it
		Map<Order, Set<Order>> supportMoves = 
			new HashMap<Order, Set<Order>>();
		
		//if a unit is holding or convoying,
		//it would like to stay where it is
		for(Order order: moves){
			
			if(order.getClass() == Move.class){
				Move mov = (Move)order;
				
				if(!movesWantLocation.containsKey(mov.to)){
					movesWantLocation.put(mov.to, new HashSet<Order>());
				}
				
				movesWantLocation.get(mov.to).add(order);
				moveOrigins.put(mov.from, mov);
				
			}else if(order.getClass() == Hold.class){
				Hold hol = (Hold)order;
				
				if(!movesWantLocation.containsKey(hol.holdingSquare)){
					movesWantLocation.put(hol.holdingSquare, new HashSet<Order>());
				}
				
				movesWantLocation.get(hol.holdingSquare).add(order);
				moveOrigins.put(hol.holdingSquare, hol);
				
			}else if(order.getClass() == SupportMove.class){
				SupportMove smov = (SupportMove)order;
				
				if(!movesWantLocation.containsKey(smov.supportFrom)){
					movesWantLocation.put(smov.supportFrom, new HashSet<Order>());
				}
				
				movesWantLocation.get(smov.supportFrom).add(order);
				moveOrigins.put(smov.supportFrom, smov);
				
			}else if(order.getClass() == SupportHold.class){
				SupportHold shol = (SupportHold)order;
				
				if(!movesWantLocation.containsKey(shol.supportFrom)){
					movesWantLocation.put(shol.supportFrom, new HashSet<Order>());
				}
				
				movesWantLocation.get(shol.supportFrom).add(order);
				moveOrigins.put(shol.supportFrom, shol);
				
			}else if(order.getClass() == Convoy.class){
				Convoy con = (Convoy)order;
				
				if(!movesWantLocation.containsKey(con.convoyer)){
					movesWantLocation.put(con.convoyer, new HashSet<Order>());
				}
				
				movesWantLocation.get(con.convoyer).add(order);
				moveOrigins.put(con.convoyer, con);
				
			}else if(order.getClass() == MoveByConvoy.class){
				MoveByConvoy mbc = (MoveByConvoy)order;
				
				if(!movesWantLocation.containsKey(mbc.convoyDestination)){
					movesWantLocation.put(mbc.convoyDestination, new HashSet<Order>());
				}
				
				movesWantLocation.get(mbc.convoyDestination).add(mbc);
				moveOrigins.put(mbc.convoyOrigin, mbc);
			}
		}
		
		//figure out what order each of the support moves is able to support
		for(Order order: moves){
			if(order.getClass() == SupportMove.class){
				SupportMove smov = (SupportMove)order;
				
				//find the order it is supporting
				Set<Order> movesToSquare =  movesWantLocation.get(smov.supportInto);
				
				for(Order potentialSupport: movesToSquare){
					
					//find if this order is either a move or a move by convoy
					TerritorySquare supportedFrom = null;
					if(potentialSupport.getClass() == Move.class){
						Move potentialMove = (Move)potentialSupport;
						
						supportedFrom = potentialMove.from;
						
					}else if(potentialSupport.getClass() == MoveByConvoy.class){
						MoveByConvoy potentialConvoy = (MoveByConvoy)potentialSupport;
					
						supportedFrom = potentialConvoy.convoyOrigin;
					}
						
					//if it is, we know where it's from now
					if(supportedFrom != null){
						
						if(supportedFrom == smov.supportFrom){
							//then this order is supporting this move
							
							//	make sure your support is not cut
							Set<Order> supportCutters = movesWantLocation.get(smov.supportFrom);
							
							boolean supportCut = false;
							for(Order potentialCut: supportCutters){
								if(potentialCut.getClass() == Move.class){
									Move moveCut = (Move)potentialCut;
									
									//the support is cut if someone who is not where you are
									//supporting into cuts it, and it is not the same player 
									//as the supporter
									if(moveCut.from != smov.supportInto &&
											moveCut.player != smov.player){
										supportCut = true;
									}
									
								}else if(potentialCut.getClass() == MoveByConvoy.class){
									MoveByConvoy mbcCut = (MoveByConvoy)potentialCut;
									
									//the support is cut if someone who is not where you are
									//supporting into cuts it, and it is not the same player 
									//as the supporter
									if(mbcCut.convoyOrigin != smov.supportInto &&
											mbcCut.player != smov.player){
										supportCut = true;
									}
								}
							}
							
							if(!supportCut){
								if(!supportMoves.containsKey(potentialSupport)){
									supportMoves.put(potentialSupport, new HashSet<Order>());
								}
								
								supportMoves.get(potentialSupport).add(order);
							}
						}
					}
				}
				
			}
			else if(order.getClass() == SupportHold.class){
				SupportHold shol = (SupportHold)order;
				
				//find the order it is supporting
				Set<Order> movesToSquare =  movesWantLocation.get(shol.supportTo);
				
				//you can support hold on anything that doesn't move
				for(Order potentialSupport: movesToSquare){
					
					//could support a hold, supporthold, supportmove, convoy
					
					TerritorySquare holdLocation = null;
					
					if(potentialSupport.getClass() == Hold.class){
						Hold hol = (Hold)potentialSupport;
						
						holdLocation = hol.holdingSquare;
						
					}else if(potentialSupport.getClass() == SupportHold.class){
						SupportHold shold = (SupportHold)potentialSupport;
						
						holdLocation = shold.supportTo;
						
					}else if(potentialSupport.getClass() == SupportMove.class){
						SupportMove smov = (SupportMove)potentialSupport;
						
						holdLocation = smov.supportFrom;
						
					}else if(potentialSupport.getClass() == Convoy.class){
						Convoy conv = (Convoy)potentialSupport;
						
						holdLocation = conv.convoyer;
						
					}
					
					///if the order was one of these type
					if(holdLocation != null){
						
						//and if it's the location that the support hold was supporting
						if(holdLocation == shol.supportTo){
							
							//then this is the order we are looking to support
							//	make sure your support is not cut
							Set<Order> supportCutters = movesWantLocation.get(shol.supportFrom);
							
							boolean supportCut = false;
							for(Order potentialCut: supportCutters){
								if(potentialCut.getClass() == Move.class){
									Move moveCut = (Move)potentialCut;
									
									//the support is cut if someone who is not where you are
									//supporting into cuts it, and it is not the same player 
									//as the supporter
									if(moveCut.player != shol.player){
										supportCut = true;
									}
									
								}else if(potentialCut.getClass() == MoveByConvoy.class){
									MoveByConvoy mbcCut = (MoveByConvoy)potentialCut;
									
									//the support is cut if someone who is not where you are
									//supporting into cuts it, and it is not the same player 
									//as the supporter
									if(mbcCut.player != shol.player){
										supportCut = true;
									}
								}
							}
							
							if(!supportCut){
								if(!supportMoves.containsKey(potentialSupport)){
									supportMoves.put(potentialSupport, new HashSet<Order>());
								}
								
								supportMoves.get(potentialSupport).add(order);
							}
						}
					}
				}
			}
		}
		
		//TODO resolve convoys -- aka see if any are dislodged or invalid.  fail moves 
		// which rely on them

		//now that the supports have been allocated, just iterate over the moves
		
		Set<Order> unresolvedMoves = new HashSet<Order>();
		
		for(Order ord: moves){
			if(ord.getClass() == Move.class){
				unresolvedMoves.add(ord);
			}else if(ord.getClass() == MoveByConvoy.class){
				unresolvedMoves.add(ord);
			}
		}
		
		
		Map<TerritorySquare, Order> mostSupportForTerritory = new HashMap<TerritorySquare, Order>();
		
		for(TerritorySquare terr: movesWantLocation.keySet()){
			
			Set<Order> competitors = movesWantLocation.get(terr);
			
			Order mostSupported = null;
			int mostSupports = 0;
			
			for(Order ord: competitors){
				
				
				
			}
		}
		
		
		while(!unresolvedMoves.isEmpty()){
			
			//loop through each
			for(Order ord: unresolvedMoves.toArray(new Order[0])){
				//	iteratively see if moves succeed.  for each unresolved move:
				

				TerritorySquare moveDestination = null;
				
				if(ord.getClass() == Move.class){
					moveDestination = ((Move)ord).to;
				}else if(ord.getClass() == MoveByConvoy.class){
					moveDestination = ((MoveByConvoy)ord).convoyDestination;
				}

				//		if the territory it moves into was originally unoccupied, or the move has
				//		2 or more support and the original occupier was a different power	
				if(moveDestination.getOccupier() == null || 
						supportMoves.get(ord).size() > 1 && 
						moveDestination.getOccupier().belongsTo != ord.player){
					
					Set<Order> competitors = movesWantLocation.get(moveDestination);
					
					//			if the move has more support than anyone else moving into it  
					if()

					//				set it as succeed
					//				set other moves into the territory as failing
					//			else
					//				set the successful move as succeeding
					//				set the other moves including this into it as failing					
				}
	
			

				//		else  
				//			if the unit originally in the territory has successfully moved out
				//				set it as succeed
				//			else if it is not moving out, or failed
				//				set it as fail				
				
			}
		}
	}
	
	
	public void update(Set<Order> moves) throws Exception{
		

		
		//	figure out which moves were successful
		
		//	for each territory, find out how many 
		
		
		//TODO for now just apply it if the results are set
		
		//process movements separately--slightly more complex resolutions
		Set<Order> successfulMoves = new HashSet<Order>();
		
		System.out.println("Successful orders to process:");
		for(Order ord: moves){
				
			if(ord.actionResult == Result.SUC){
				System.out.println(ord.toOrder()+ " "+ord.getResult());
				
				if(ord.getClass() == Build.class){
					Build b = (Build)ord;
					
					setOccupier(b.build, b.location, b.coast);
					
				}else if(ord.getClass() == Convoy.class){
					
					//nothing to do if a convoy succeeds
					
				}else if(ord.getClass() == Disband.class){
					Disband dsb = (Disband)ord;

					//removeOccupier(dsb.disbandAt);
					resolveRetreat(dsb.disbandAt);
					
				}else if(ord.getClass() == Hold.class){
					
					//nothing to do if a hold succeeds
					
				}else if(ord.getClass() == Move.class){
					
					successfulMoves.add(ord);
					
				}else if(ord.getClass() == MoveByConvoy.class){
					
					successfulMoves.add(ord);
					
				}else if(ord.getClass() == Remove.class){
					Remove rem = (Remove)ord;
					
					removeOccupier(rem.disbandLocation);
					
				}else if(ord.getClass() == Retreat.class){
					Retreat ret = (Retreat)ord;
					
					//	should be able to resolve the retreats here -- shouldn't ever have the issue
					//	of retreating somewhere someone else retreats from
					
					resolveRetreat(ret.from);
					setOccupier(ret.retreatingUnit, ret.to, ret.destCoast);
					
				}else if(ord.getClass() == SupportHold.class){
					
					//nothing to do if a support hold works
					
				}else if(ord.getClass() == SupportMove.class){
					
					//nothing to do if a support move works
					
				}else if(ord.getClass() == Waive.class){
					
					//nothing to do if a waive is successful
				}
			}
			
			if(ord.retreatState == RetreatState.RET){
				
				if(ord.getClass() == Convoy.class){
					Convoy c = (Convoy)ord;
					
					setRetreatingUnit(c.convoyingUnit, c.convoyer, "NA");
					this.removeOccupier(c.convoyer);
					
				}else if(ord.getClass() == Hold.class){
					Hold hold = (Hold)ord;
					
					setRetreatingUnit(hold.holdingUnit, hold.holdingSquare, hold.holdingSquare.getOccupiedCoast());
					this.removeOccupier(hold.holdingSquare);
					
				}else if(ord.getClass() == Move.class){
					Move mov = (Move)ord;
					
					setRetreatingUnit(mov.unit, mov.from, mov.from.getOccupiedCoast());
					this.removeOccupier(mov.from);
					
				}else if(ord.getClass() == MoveByConvoy.class){
					MoveByConvoy mbc = (MoveByConvoy)ord;
					
					setRetreatingUnit(mbc.convoyedUnit, mbc.convoyOrigin, "NA");
					this.removeOccupier(mbc.convoyOrigin);
					
				}else if(ord.getClass() == SupportHold.class){
					SupportHold shold = (SupportHold)ord;
					
					setRetreatingUnit(shold.supporter, shold.supportFrom, shold.supportFrom.getOccupiedCoast());
					this.removeOccupier(shold.supportFrom);
					
				}else if(ord.getClass() == SupportMove.class){
					SupportMove smove = (SupportMove)ord;
					
					setRetreatingUnit(smove.supporter, smove.supportFrom, smove.supportFrom.getOccupiedCoast());
					this.removeOccupier(smove.supportFrom);
				}else {
					//nothing else should have a retreat
					throw new Exception("should not be a retreat here");
				}
				

			}
		}			
		
		//	first map each move to where it will end up
		Map<TerritorySquare, Order> destinations = new HashMap<TerritorySquare, Order>();
		for(Order ord: successfulMoves){
			
			if(ord.getClass() == MoveByConvoy.class){
				MoveByConvoy mbc = (MoveByConvoy)ord;
				
				removeOccupier(mbc.convoyOrigin);
				destinations.put(mbc.convoyDestination, mbc);
				
			}else if(ord.getClass() == Move.class){
				Move mov = (Move)ord;
				
				removeOccupier(mov.from);
				destinations.put(mov.to, mov);
			}
		}
		
		for(TerritorySquare sq: destinations.keySet()){
			Order ord = destinations.get(sq);
			
			if(ord.getClass() == MoveByConvoy.class){
				MoveByConvoy mbc = (MoveByConvoy)ord;
				
				setOccupier(mbc.convoyedUnit, mbc.convoyDestination);
				
			}else if(ord.getClass() == Move.class){
				Move mov = (Move)ord;
				
				setOccupier(mov.unit, mov.to, mov.coast);
			}
		}
		
		history.add(currentYear, currentPhase, moves);
	}
	
	public String toString(){
		
		String str = "Game State: ";
		
		str+="\tPlayers:\n";
		
		for(Player p: this.activePlayers.values()){
			str+="\t"+p.toString()+"\n";
		}
		
		return str;
	}
	
	
	public Player getPlayer(Country power){
		return this.activePlayers.get(power);
	}
	
	//utility methods for game mechanics
	
	public void assertCanMove(Player p, TerritorySquare from, TerritorySquare to) throws Exception{
		assertCanMove(p, from, to, "NA");
	}
	
	public boolean canMove(Player p, TerritorySquare from, TerritorySquare to){
		return canMove(p, from, to, "NA");
	}
	
	public void assertCanMove(Player p, TerritorySquare from, TerritorySquare to, String destinationCoast) throws Exception{
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		//	make sure unit is there
		if(from.getOccupier() == null){
			throw new Exception("no occupier");
		}
		
		//	player correct
		if(from.getOccupier().belongsTo != p){
			throw new Exception("wrong occupying player: "+from.getOccupier());
		}
		
		if(from.getOccupier().army){
			
			//if the unit moving is an army, make sure it can do this
			if(!from.isLandBorder(to)){
				throw new Exception("no land border");
			}
		}else{
			String occupiedCoast = from.getOccupiedCoast();			
			
			//if it's a fleet, make sure the coasts match up
			if(!from.isSeaBorder(to, occupiedCoast, destinationCoast)){
				throw new Exception("no such sea border from "+from.getName()+" to "+to.getName());
			}
		}
	}
	
	public boolean canMove(Player p, TerritorySquare from, TerritorySquare to, String destinationCoast){
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			return false;
		}
		
		
		//	make sure unit is there
		if(from.getOccupier() == null){
			return false;
		}
		
		//	player correct
		if(from.getOccupier().belongsTo != p){
			return false;
		}

		
		if(from.getOccupier().army){
			
			//if the unit moving is an army, make sure it can do this
			if(!from.isLandBorder(to)){
				return false;
			}
		}else{
			String occupiedCoast = from.getOccupiedCoast();			
			
			//if it's a fleet, make sure the coasts match up
			if(!from.isSeaBorder(to, occupiedCoast, destinationCoast)){
				return false;
			}
		}
		
		return true;
	}
	
	
	public void assertCanSupportHold(Player p, TerritorySquare from, TerritorySquare to) throws Exception{
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		if(to.getOccupier() == null){
			throw new Exception("no occupier");
		}
		
		if(from.getOccupier().belongsTo != p){
			throw new Exception("wrong occupying country");
		}
		
		assertCanMove(p, from, to);	
	}
	
	public boolean canSupportHold(Player p, TerritorySquare from, TerritorySquare to){
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			return false;
		}
		
		if(to.getOccupier() == null){
			return false;
		}
		
		if(from.getOccupier().belongsTo != p){
			return false;
		}

		
		return canMove(p, from, to);	
	}
	
	public void assertCanSupportMove(Player p, TerritorySquare supporter, TerritorySquare from, TerritorySquare to) throws Exception{
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		
		if(supporter.getOccupier() == null){
			throw new Exception("no supporter");
		}
		
		if(supporter.getOccupier().belongsTo != p){
			throw new Exception("wrong occupying country");
		}
		
		if(from.getOccupier() == null){
			throw new Exception("no occupier");
		}
		
		assertCanMove(p, supporter, to);
		
		assertCanMove(p, from, to);
		
	}
	
	public boolean canSupportMove(Player p, TerritorySquare supporter, TerritorySquare from, TerritorySquare to){
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			return false;
		}
		
		if(supporter.getOccupier().belongsTo != p){
			return false;
		}
		
		if(supporter.getOccupier() == null){
			return false;
		}
		
		if(from.getOccupier() == null){
			return false;
		}
		
		if(!canMove(p, supporter, to)){
			return false;
		}
		
		if(!canMove(p, from, to)){
			return false;
		}
		
		return true;
	}
	
	public boolean assertCanAssistConvoy(Player p, TerritorySquare convoyer, TerritorySquare from, TerritorySquare to) throws Exception{
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		if(convoyer.getOccupier() == null){
			throw new Exception("empty convoyer");
		}
		
		if(from.getOccupier() == null){
			throw new Exception("empty occupier");
		}
		
		if(convoyer.getOccupier().belongsTo != p){
			throw new Exception("wrong occupying country");
		}
		
		Unit convoyingUnit = convoyer.getOccupier();
		Unit convoyedUnit = from.getOccupier();
		
		if(convoyer.isLand() || convoyingUnit.army || !convoyedUnit.army){
			throw new Exception("invalid type of unit being convoyed or convoying");
		}
		
		if(!from.hasAnySeaBorders() || !to.hasAnySeaBorders()){
			throw new Exception("convoy cannot succeed");
		}
		
		return true;
	}
	
	public boolean canAssistConvoy(Player p, TerritorySquare convoyer, TerritorySquare from, TerritorySquare to){
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			return false;
		}
		
		if(convoyer.getOccupier() == null || from.getOccupier() == null){
			return false;
		}
		
		if(convoyer.getOccupier().belongsTo != p){
			return false;
		}
		
		Unit convoyingUnit = convoyer.getOccupier();
		Unit convoyedUnit = from.getOccupier();
		
		if(convoyer.isLand() || convoyingUnit.army || !convoyedUnit.army){
			return false;
		}
		
		if(!from.hasAnySeaBorders() || !to.hasAnySeaBorders()){
			return false;
		}
		
		return true;
	}
	
	public void assertCanConvoy(Player p, TerritorySquare from, TerritorySquare to, List<TerritorySquare> transit) throws Exception{
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		//	units need to exist, territories have to be sea, only fleets convoy
		
		Unit movingUnit = from.getOccupier();
		
		if(movingUnit == null || !movingUnit.army){
			throw new Exception("this unit cannot convoy");
		}
		
		if(movingUnit.belongsTo != p){
			throw new Exception("wrong player");
		}
		
		for(TerritorySquare sqr: transit){
			if(sqr.getOccupier() == null || sqr.isLand() || sqr.getOccupier().army){
				throw new Exception("invalid transit ship");
			}
		}
		
		//	make sure that the path is contiguous
		
		TerritorySquare start = from;
		
		for(TerritorySquare t: transit){
			if(!t.isAnySeaBorder(start)){
				throw new Exception("transit not connected");
			}
			
			start = t;
		}
		
		if(!start.isAnySeaBorder(to)){
			throw new Exception("cannot get to destination--no border");
		}
	}
	
	public boolean canConvoy(Player p, TerritorySquare from, TerritorySquare to, List<TerritorySquare> transit){
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			return false;
		}
		
		//	units need to exist, territories have to be sea, only fleets convoy
		
		Unit movingUnit = from.getOccupier();
		
		if(movingUnit == null || !movingUnit.army){
			return false;
		}
		
		if(movingUnit.belongsTo != p){
			return false;
		}
		
		for(TerritorySquare sqr: transit){
			if(sqr.getOccupier() == null || sqr.isLand() || sqr.getOccupier().army){
				return false;
			}
		}
		
		//	make sure that the path is contiguous
		
		TerritorySquare start = from;
		
		for(TerritorySquare t: transit){
			if(!t.isAnySeaBorder(start)){
				return false;
			}
			
			start = t;
		}
		
		if(!start.isAnySeaBorder(to)){
			return false;
		}
		
		return true;
	}
	
	public boolean canHold(Player p, TerritorySquare holder){
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			return false;
		}
		
		if(holder.getOccupier() == null){
			return false;
		}
		
		if(holder.getOccupier().belongsTo != p){
			return false;
		}
		
		//	as long as there's a unit there it can hold...
		return true;
		
	}
	
	public void assertCanHold(Player p, TerritorySquare holder) throws Exception{
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		if(holder.getOccupier() == null){
			throw new Exception("null occupier");
		}
		
		if(holder.getOccupier().belongsTo != p){
			throw new Exception("holder belongs to wrong player");
		}
	}
	
	public void assertCanBuild(Player p, Unit u, TerritorySquare location) throws Exception{
		
		//	it must be winter
		if(this.currentPhase != Phase.WIN){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		//	location must be unoccupied
		if(location.getOccupier() != null){
			throw new Exception("occupier is null");
		}
		
		//	location must be controlled by this player
		if(location.getController() != p){
			throw new Exception("location has no controller");
		}

		//	location must be home territory of u.controller
		if(location.getHomePlayer() != u.belongsTo){
			throw new Exception("location is not a home supply center");
		}
		
		//	unit must belong to player
		if(u.belongsTo != p){
			throw new Exception("player does not control proposed unit");
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(u.belongsTo.getNumberUnits() >= u.belongsTo.getNumberSupplyCenters()){
			throw new Exception("player does not have any builds");
		}
		
		//	location must controlled by u.controller
		if(location.getController() != u.belongsTo){
			throw new Exception("player does not control center");
		}
	}
	
	public boolean canBuild(Player p, Unit u, TerritorySquare location){
		
		//	it must be winter
		if(this.currentPhase != Phase.WIN){
			return false;
		}
		
		//	location must be unoccupied
		if(location.getOccupier() != null){
			return false;
		}
		
		//	location must be controlled by this player
		if(location.getController() != p){
			return false;
		}

		//	location must be home territory of u.controller
		if(location.getHomePlayer() != u.belongsTo){
			return false;
		}
		
		if(u.belongsTo != p){
			return false;
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(u.belongsTo.getNumberUnits() >= u.belongsTo.getNumberSupplyCenters()){
			return false;
		}
		
		//	location must controlled by u.controller
		if(location.getController() != u.belongsTo){
			return false;
		}
		
		return true;
	}
	
	public boolean assertCanWaive(Player p) throws Exception{
		
		//	it must be winter
		if(this.currentPhase != Phase.WIN){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(p.getNumberUnits() >= p.getNumberSupplyCenters()){
			throw new Exception("no available builds to waive");
		}
		
		return true;
	}
	
	public boolean canWaive(Player p){
		
		//	it must be winter
		if(this.currentPhase != Phase.WIN){
			return false;
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(p.getNumberUnits() >= p.getNumberSupplyCenters()){
			return false;
		}
		
		return true;
	}
	
	public void assertCanDisband(Player p, TerritorySquare location) throws Exception{
		
		//	it must be retreat season
		if(this.currentPhase != Phase.AUT && this.currentPhase != Phase.SUM){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		//	there must be a pending retreat from this location
		if(!this.retreats.containsKey(location)){
			throw new Exception("no retreat for that location");
		}
		
		RetreatSituation rSit = this.retreats.get(location);
		
		if(rSit.retreating.belongsTo != p){
			throw new Exception("wrong player for retreat");
		}
		
		if(rSit.from != location){
			throw new Exception("wrong origin of retreat");
		}
	}
	public boolean canDisband(Player p, TerritorySquare location){
		
		//	it must be retreat season
		if(this.currentPhase != Phase.AUT && this.currentPhase != Phase.SUM){
			return false;
		}
		
		//	there must be a pending retreat from this location
		if(!this.retreats.containsKey(location)){
			return false;
		}
		
		RetreatSituation rSit = this.retreats.get(location);
		
		if(rSit.retreating.belongsTo != p){
			return false;
		}
		
		if(rSit.from != location){
			return false;
		}
		
		if(rSit.retreating.belongsTo != p){
			return false;
		}
		
		return true;
	}
	
	
	public void assertCanRetreat(Player p, TerritorySquare from, TerritorySquare to) throws Exception{
		assertCanRetreat(p, from, to, "NA");
	}
	
	public boolean canRetreat(Player p, TerritorySquare from, TerritorySquare to){
		return canRetreat(p, from, to, "NA");
	}
	
	public boolean assertCanRetreat(Player p, TerritorySquare from, TerritorySquare to, String destinationCoast) throws Exception{
		
		if(this.currentPhase != Phase.AUT && this.currentPhase != Phase.SUM){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		//	there must be a pending retreat from this location
		if(!this.retreats.containsKey(from)){
			throw new Exception("no retreat for that location");
		}
		
		RetreatSituation rSit = this.retreats.get(from);
		
		if(rSit.retreating.belongsTo != p){
			throw new Exception("wrong player for retreat");
		}
		
		if(rSit.from != from){
			throw new Exception("wrong origin of retreat");
		}

		
		if(rSit.retreating.army){
			
			//if the unit moving is an army, make sure it can do this
			if(!from.isLandBorder(to)){
				throw new Exception("no land border to retreat destination");
			}
		}else{
			String occupiedCoast = rSit.originCoast;//from.getOccupiedCoast();			
			
			//if it's a fleet, make sure the coasts match up
			if(!from.isSeaBorder(to, occupiedCoast, destinationCoast)){
				throw new Exception("no sea border from " +from.getName()+" to retreat destination "+to.getName());
			}
		}
		
		if(history.wasTerritoryContested(to)){
			throw new Exception("territory was contested on previous turn");
		}
		
		return true;
	}
	
	public boolean canRetreat(Player p, TerritorySquare from, TerritorySquare to, String destinationCoast){
		
		if(this.currentPhase != Phase.AUT && this.currentPhase != Phase.SUM){
			return false;
		}
		
		//	there must be a pending retreat from this location
		if(!this.retreats.containsKey(from)){
			return false;
		}
		
		RetreatSituation rSit = this.retreats.get(from);
		
		if(rSit.retreating.belongsTo != p){
			return false;
		}
		
		if(rSit.from != from){
			return false;
		}

		
		if(rSit.retreating.army){
			
			//if the unit moving is an army, make sure it can do this
			if(!from.isLandBorder(to)){
				return false;
			}
		}else{
			String occupiedCoast = rSit.originCoast;//from.getOccupiedCoast();			
			
			//if it's a fleet, make sure the coasts match up
			if(!from.isSeaBorder(to, occupiedCoast, destinationCoast)){
				return false;
			}
		}
		
		if(history.wasTerritoryContested(to)){
			return false;
		}
		
		return true;
	}
	
	public void assertCanRemove(Player p, TerritorySquare location) throws Exception{
		
		//	it must be winter
		if(this.currentPhase != Phase.WIN){
			throw new Exception("wrong season: "+currentPhase);
		}
		
		//	location must have a unit
		if(location.getOccupier() == null){
			throw new Exception("no occupier");
		}
		
		if(location.getOccupier().belongsTo != p){
			throw new Exception("wrong player");
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(location.getOccupier().belongsTo.getNumberUnits() < location.getOccupier().belongsTo.getNumberSupplyCenters()){
			throw new Exception("player does not have to remove units");
		}
	}
	public boolean canRemove(Player p, TerritorySquare location){
		
		//	it must be winter
		if(this.currentPhase != Phase.WIN){
			return false;
		}
		
		//	location must have a unit
		if(location.getOccupier() == null){
			return false;
		}
		
		if(location.getOccupier().belongsTo != p){
			return false;
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(location.getOccupier().belongsTo.getNumberUnits() < location.getOccupier().belongsTo.getNumberSupplyCenters()){
			return false;
		}
		
		return true;
	}

	
	public static void main(String[] args) throws Exception{
		
//		FileWriter fwriter = new FileWriter("map.gviz");
//		fwriter.write(new GameState().mapAsDotFile());
//		fwriter.close();
		
		BoardState g = new BoardState();
		System.out.println(g.toString());
	}	
}

package state;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import order.Build;
import order.Convoy;
import order.Disband;
import order.Hold;
import order.Move;
import order.MoveByConvoy;
import order.Order;
import order.OrderFactory;
import order.Remove;
import order.Retreat;
import order.SupportHold;
import order.SupportMove;
import order.Waive;
import order.Order.Result;
import order.Order.RetreatState;

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
	
	//TODO this is just here for testing...
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
			throw new Exception("Territory not supply center");
		}
		
		if(terr.getController() == p){
			throw new Exception("Player already controls this center!");
		}
		
		if(terr.isControlled()){
			terr.getController().removeSupply(terr);
		}
		
		p.addSupply(terr);
		terr.setController(p);
	}
	
	public void setOccupier(Unit u, TerritorySquare terr, String coast) throws Exception{
		
		if(!terr.hasCoast(coast)){
			throw new Exception("Invalid coast!");
		}
		
		if(u.army && !terr.isLand()){
			throw new Exception("Invalid occupier");
		}
		
		if(terr.getOccupier() != null){
			throw new Exception("territory "+terr.getName()+" already occupied");
		}
		
		u.belongsTo.addOccupy(terr);
		terr.setOccupier(u, coast);
	}
	
	public void setOccupier(Unit u, TerritorySquare terr) throws Exception{
		
		if(!u.army && terr.hasMultipleCoasts()){
			throw new Exception("Must specify a coast!");
		}
		
		setOccupier(u, terr, "NA");
	}
	
	public Unit removeOccupier(TerritorySquare terr) throws Exception{
		
		Unit removed = terr.getOccupier();
		
		if(removed == null){
			throw new Exception("Territory not occupied!");
		}
		
		removed.belongsTo.removeOccupy(terr);
		
		terr.setOccupier(null);
		
		return removed;
	}
	
	
	private void initialize() throws Exception{
		
		currentPhase = Phase.SPR;
		
		Player eng = new Player(Country.ENG);
		Player fra = new Player(Country.FRA);
		Player ger = new Player(Country.GER);
		Player rus = new Player(Country.RUS);
		Player ita = new Player(Country.ITA);
		Player aus = new Player(Country.AUS);
		Player tur = new Player(Country.TUR);
		
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
		terrs.put("BUL", new TerritorySquare("BUL", SUPPLY, LAND, null, Arrays.asList("ECS", "WCS"), this));
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
		terrs.put("TRL", new TerritorySquare("TRL", NO_SUPPLY, LAND, null, this));
				
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
		terrs.put("LYO", new TerritorySquare("LYO",	NO_SUPPLY, SEA, null, this));
		terrs.put("TYN", new TerritorySquare("TYN",	NO_SUPPLY, SEA, null, this));
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
		
		border("BAR", "STP", "NA", "NCS");
		border("BAR", "NWY", true);
		
		border("MAO", "IRI", true);
		border("MAO", "ECH", true);
		border("MAO", "BRE", true);
		border("MAO", "GAS", true);
		border("MAO", "SPA", "NA", "NCS");
		border("MAO", "SPA", "NA", "SCS");
		border("MAO", "POR", true);
		
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
		border("WES", "LYO", true);
		border("WES", "TYN", true);
		border("WES", "TUN", true);
		border("WES", "NAF", true);
		
		border("LYO", "SPA", "NA", "SCS");
		border("LYO", "MAR", true);
		border("LYO", "PIE", true);
		border("LYO", "TUS", true);
		border("LYO", "TYN", true);
		
		border("TYN", "TUS", true);
		border("TYN", "ROM", true);
		border("TYN", "NAP", true);
		border("TYN", "ION", true);
		border("TYN", "TUN", true);
		
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
		border("AEG", "BUL", "NA", "WCS");
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
		border("MUN", "TRL", false);
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
		border("VEN", "TRL", false);
		
		//austro hungary
		
		border("TRL", "BOH", false);
		border("TRL", "VIE", false);
		border("TRL", "TRI", false);
		
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
		
		border("CON", "BUL", "NA", "WCS");
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
		border("BUL", "GRE", "WCS", "NA");
		
		border("SER", "GRE", false);
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
	
	public void update(Set<Order> moves) throws Exception{
		
		//TODO for now just apply it if the results are set
		
		//TODO this code needs to be tested
		
		//process movements separately--slightly more complex resolutions
		Set<Order> successfulMoves = new HashSet<Order>();
		
		System.out.println("Processing orders: ");
		for(Order ord: moves){
				
			if(ord.actionResult == Result.SUC){
				System.out.println(ord.toOrder()+ " "+ord.getResult());
				
				if(ord.getClass() == Build.class){
					Build b = (Build)ord;
					
					setOccupier(b.build, b.location);
					
				}else if(ord.getClass() == Convoy.class){
					
					//nothing to do if a convoy succeeds
					
				}else if(ord.getClass() == Disband.class){
					Disband dsb = (Disband)ord;

					removeOccupier(dsb.disbandAt);
					
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
					setOccupier(ret.retreatingUnit, ret.to);
					
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
				
				setOccupier(mov.unit, mov.to);
			}
		}
		
		//TODO update history
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
	
	public boolean canMove(Player p, TerritorySquare from, TerritorySquare to){
		return canMove(p, from, to, "NA");
	}
	
	public boolean canMove(Player p, TerritorySquare from, TerritorySquare to, String destinationCoast){
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			return false;
		}
		
		//	player correct
		if(from.getOccupier().belongsTo != p){
			return false;
		}
		
		//	make sure unit is there
		if(from.getOccupier() == null){
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
	
	public boolean canSupportHold(Player p, TerritorySquare from, TerritorySquare to){
		
		if(this.currentPhase == Phase.WIN || this.currentPhase == Phase.SUM || this.currentPhase == Phase.AUT){
			return false;
		}
		
		if(from.getOccupier().belongsTo != p){
			return false;
		}
		
		if(to.getOccupier() == null){
			return false;
		}
		
		return canMove(p, from, to);	
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
	
	public boolean canRetreat(Player p, TerritorySquare from, TerritorySquare to){
		return canRetreat(p, from, to, "NA");
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

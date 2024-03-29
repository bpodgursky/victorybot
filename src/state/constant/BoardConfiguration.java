package state.constant;


import heuristic.Heuristic;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.Bot;

import order.Order;
import order.Order.OrderValue;
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
import state.dynamic.BoardState;
import state.dynamic.BoardState.Phase;
import state.dynamic.BoardState.RetreatSituation;


public class BoardConfiguration {
	
	final static boolean SUPPLY = true;
	final static boolean NO_SUPPLY = false;
	
	final static boolean ARMY = true;
	final static boolean FLEET = false;
	
	final static boolean LAND = true;
	final static boolean SEA = false;

	final BoardState initialState;
	
	//constant
	
	final Map<String, TerritorySquare> terrs = new HashMap<String, TerritorySquare>();
	final Map<Country, Player> activePlayers = new HashMap<Country, Player>();

	
	public BoardConfiguration() throws Exception{
		initialState = initialize();
	}
	
	public Collection<Player> getPlayers(){
		return activePlayers.values();
	}
	
	public BoardState getInitialState(){
		return initialState;
	}
	
	//these are public, but only really for testing.  
	
	//set the controller of a territory
	//once controlled, control is only lost by someone else taking control.  So don't
	//need a remove method
	public void setControl(BoardState bst, Player p, TerritorySquare terr) throws Exception{
		
		if(!terr.isSupplyCenter()){
			throw new Exception("Territory "+terr.getName()+" not supply center");
		}
		
		if(terr.getController(bst) == p){
			throw new Exception("Player "+p.getName()+" already controls center "+terr.getName());
		}
		
		if(terr.isControlled(bst)){
			terr.getController(bst).removeSupply(bst, terr);
		}
		
		p.addSupply(bst, terr);
		terr.setController(bst, p);
	}
	
	public void setOccupier(BoardState bst, Unit u, TerritorySquare terr, String coast) throws Exception{
		
		if(!terr.hasCoast(coast)){
			throw new Exception("Invalid coast "+coast+" for terr "+terr.getName());
		}
		
		if(u.army && !terr.isLand()){
			throw new Exception("Invalid occupier for terr "+terr.getName());
		}
		
		if(terr.getOccupier(bst) != null){
			throw new Exception("territory "+terr.getName()+" already occupied");
		}
		
		u.belongsTo.addOccupy(bst, terr);
		terr.setOccupier(bst, u, coast);
	}
	
	public void setOccupier(BoardState bst, Unit u, TerritorySquare terr) throws Exception{
		
		if(!u.army && terr.hasMultipleCoasts()){
			throw new Exception("Must specify a coast for "+terr.getName());
		}
		
		setOccupier(bst, u, terr, "NA");
	}
	
	public Unit removeOccupier(BoardState bst, TerritorySquare terr) throws Exception{
		
		Unit removed = terr.getOccupier(bst);
		
		if(removed == null){
			throw new Exception("Territory "+terr.getName()+" not occupied!");
		}
		
		removed.belongsTo.removeOccupy(bst, terr);
		
		terr.setOccupier(bst, null);
		
		return removed;
	}
	
	
	private BoardState initialize() throws Exception{
		
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
		
		BoardState bst = new BoardState(new YearPhase(1901, Phase.SPR), this);


		
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
		border("LVP", "NAO", true);
		
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
		
		//border("IRI", "CLY", true);
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
		border("ION", "APU", true);
		
		border("ADR", "APU", true);
		border("ADR", "VEN", true);
		border("ADR", "TRI", true);
		border("ADR", "ALB", true);
		
		border("AEG", "GRE", true);
		border("AEG", "BUL", "NA", "SCS");
		border("AEG", "CON", true);
		border("AEG", "SMY", true);
		border("AEG", "EAS", true);
		//border("AEG", "BLA", true);
		
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
		
		setControl(bst, eng, get("EDI"));
		setControl(bst, eng, get("LVP"));
		setControl(bst, eng, get("LON"));
		
		setOccupier(bst, new Unit(eng, FLEET), get("EDI"));//EDI
		setOccupier(bst, new Unit(eng, ARMY), get("LVP"));//LVP
		setOccupier(bst, new Unit(eng, FLEET), get("LON"));
		
		setControl(bst, rus, get("STP"));
		setControl(bst, rus, get("MOS"));
		setControl(bst, rus, get("WAR"));
		setControl(bst, rus, get("SEV"));
		
		setOccupier(bst, new Unit(rus, FLEET), get("STP"), "SCS");
		setOccupier(bst, new Unit(rus, ARMY), get("MOS"));
		setOccupier(bst, new Unit(rus, ARMY), get("WAR"));
		setOccupier(bst, new Unit(rus, FLEET), get("SEV"));
		
		setControl(bst, fra, get("BRE"));
		setControl(bst, fra, get("PAR"));
		setControl(bst, fra, get("MAR"));
		
		setOccupier(bst, new Unit(fra, FLEET), get("BRE"));
		setOccupier(bst, new Unit(fra, ARMY), get("PAR"));
		setOccupier(bst, new Unit(fra, ARMY), get("MAR"));
		
		setControl(bst, ger, get("KIE"));
		setControl(bst, ger, get("BER"));
		setControl(bst, ger, get("MUN"));
		
		setOccupier(bst, new Unit(ger, FLEET), get("KIE"));
		setOccupier(bst, new Unit(ger, ARMY), get("BER"));
		setOccupier(bst, new Unit(ger, ARMY), get("MUN"));
		
		setControl(bst, ita, get("ROM"));
		setControl(bst, ita, get("NAP"));
		setControl(bst, ita, get("VEN"));
		
		setOccupier(bst, new Unit(ita, ARMY), get("ROM"));
		setOccupier(bst, new Unit(ita, ARMY), get("VEN"));
		setOccupier(bst, new Unit(ita, FLEET), get("NAP"));
		
		setControl(bst, aus, get("VIE"));
		setControl(bst, aus, get("TRI"));
		setControl(bst, aus, get("BUD"));
		
		setOccupier(bst, new Unit(aus, ARMY), get("VIE"));
		setOccupier(bst, new Unit(aus, FLEET), get("TRI"));
		setOccupier(bst, new Unit(aus, ARMY), get("BUD"));
		
		setControl(bst, tur, get("CON"));
		setControl(bst, tur, get("ANK"));
		setControl(bst, tur, get("SMY"));
		
		setOccupier(bst, new Unit(tur, ARMY), get("CON"));
		setOccupier(bst, new Unit(tur, FLEET), get("ANK"));
		setOccupier(bst, new Unit(tur, ARMY), get("SMY"));
		
		return bst;
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
	
	public void updateSupplyControl(BoardState bst) throws Exception{
		
		for(Player p: this.activePlayers.values()){
			
			//for each of their units, set them as controlling the center (if it is one)
			
			Set<TerritorySquare> control = p.getControlledTerritories(bst);
			for(TerritorySquare sq: p.getOccupiedTerritories(bst)){
				if(sq.isSupplyCenter()){
					if(!control.contains(sq)){
						setControl(bst, p, sq);
					}
				}
			}
		}
	}
	
	private void calcMovesByDestination(Collection<Order> moves, 
			Map<TerritorySquare, Collection<Order>> movesWantLocation,
			Map<TerritorySquare, Order> moveOrigins){
		
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
	}
	
	private void calculateSupport(Collection<Order> moves,
			Map<TerritorySquare, Collection<Order>> movesWantLocation,
			Map<TerritorySquare, Order> moveOrigins,
			Map<Order, Collection<Order>> supportMoves,
			Map<Order, Order> supporters){
		
		for(Order order: moves){
			if(order.getClass() == SupportMove.class){
				SupportMove smov = (SupportMove)order;
				
				//find the order it is supporting
				Collection<Order> movesToSquare =  movesWantLocation.get(smov.supportInto);
				
				boolean foundOrder = false;
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
						
						if(supportedFrom == smov.supportOrig){
							//then this order is supporting this move
							
							foundOrder = true;
							
							//	make sure your support is not cut
							Collection<Order> supportCutters = movesWantLocation.get(smov.supportFrom);
							
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
										smov.actionResult = Result.CUT;
									}
									
								}else if(potentialCut.getClass() == MoveByConvoy.class){
									MoveByConvoy mbcCut = (MoveByConvoy)potentialCut;
									
									//the support is cut if someone who is not where you are
									//supporting into cuts it, and it is not the same player 
									//as the supporter
									if(mbcCut.convoyOrigin != smov.supportInto &&
											mbcCut.player != smov.player){
										
										if(!mbcCut.transits.contains(smov.supportInto)){
											supportCut = true;
											smov.actionResult = Result.CUT;
										}
									}
								}
							}
							
							if(!supportCut){
								if(!supportMoves.containsKey(potentialSupport)){
									supportMoves.put(potentialSupport, new HashSet<Order>());
								}
								
								supportMoves.get(potentialSupport).add(order);
								supporters.put(order, potentialSupport);
							}
						}
					}
				}
				
				if(!foundOrder){
					smov.actionResult = Result.NSO;
				}
				
			}
			else if(order.getClass() == SupportHold.class){
				SupportHold shol = (SupportHold)order;
				
				//find the order it is supporting
				Collection<Order> movesToSquare =  movesWantLocation.get(shol.supportTo);
				
				//TODO deal with this case
				if(movesToSquare == null) continue;
				
				boolean foundOrder = false;
				
				//you can support hold on anything that doesn't move
				for(Order potentialSupport: movesToSquare){
					
					//could support a hold, supporthold, supportmove, convoy
					
					TerritorySquare holdLocation = null;
					
					if(potentialSupport.getClass() == Hold.class){
						Hold hol = (Hold)potentialSupport;
						
						holdLocation = hol.holdingSquare;
						
					}else if(potentialSupport.getClass() == SupportHold.class){
						SupportHold shold = (SupportHold)potentialSupport;
						
						holdLocation = shold.supportFrom;
						
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
							
							foundOrder = true;
							
							
							//then this is the order we are looking to support
							//	make sure your support is not cut
							Collection<Order> supportCutters = movesWantLocation.get(shol.supportFrom);
							
							boolean supportCut = false;
							for(Order potentialCut: supportCutters){
								if(potentialCut.getClass() == Move.class){
									Move moveCut = (Move)potentialCut;
									
									//the support is cut if someone who is not where you are
									//supporting into cuts it, and it is not the same player 
									//as the supporter
									if(moveCut.player != shol.player){
										supportCut = true;
										shol.actionResult = Result.CUT;
									}
									
								}else if(potentialCut.getClass() == MoveByConvoy.class){
									MoveByConvoy mbcCut = (MoveByConvoy)potentialCut;
									
									//the support is cut if someone who is not where you are
									//supporting into cuts it, and it is not the same player 
									//as the supporter
									if(mbcCut.player != shol.player){
										
										supportCut = true;
										shol.actionResult = Result.CUT;
									}
								}
							}
							
							if(!supportCut){
								if(!supportMoves.containsKey(potentialSupport)){
									supportMoves.put(potentialSupport, new HashSet<Order>());
								}
								
								supportMoves.get(potentialSupport).add(order);
								supporters.put(order, potentialSupport);
							}
						}
					}
				}
				
				if(!foundOrder){
					shol.actionResult = Result.NSO;
				}
			}
		}
	}
	
	private void resolveDislodgedSupportMoves(Collection<Order> moves,
			Map<TerritorySquare, Collection<Order>> movesWantLocation,
			Map<TerritorySquare, Order> moveOrigins,
			Map<Order, Collection<Order>> supportMoves,
			Map<Order, Order> supporters){
		//	resolve dislodged support move orders
		
		//	for each support move order
		for(Order ord: moves){
			if(ord.getClass() == SupportMove.class){
				SupportMove smov = (SupportMove)ord;
				
				if(smov.actionResult == Result.CUT) continue;
				
				TerritorySquare supportingInto = smov.supportInto;
				Order fromInto = moveOrigins.get(supportingInto);
				
				if(	fromInto != null && 
					fromInto.player != smov.player &&
					fromInto.getClass() == Move.class){
					
					Move fromIntoMove = (Move)fromInto;
					
					//	this is the situation we need to figure out
					if(fromIntoMove.to == smov.supportFrom){
						
						int supportMoveSupport = 0;
						if(supportMoves.containsKey(smov)){
							supportMoveSupport = supportMoves.get(smov).size();
						}
						
						int adversaryMoveSupport = 0;
						if(supportMoves.containsKey(fromIntoMove)){
							adversaryMoveSupport = supportMoves.get(fromIntoMove).size();
						}
						
						Collection<Order> allCompetitors = movesWantLocation.get(smov.supportFrom);
						
						boolean anotherEnemy = false;
						int maxFriendlyIn = 0;
						
						for(Order attempt: allCompetitors){
							
							if(attempt.player != smov.player && attempt != fromIntoMove){
								anotherEnemy = true;
							}
							
							if(attempt.player == smov.player){
								
								int numSupports = 0;
								if(supportMoves.containsKey(attempt)){
									numSupports = supportMoves.get(attempt).size();
								}
								
								maxFriendlyIn = numSupports;
							}
						}
						
						if(anotherEnemy ||
								adversaryMoveSupport > Math.max(supportMoveSupport, maxFriendlyIn)){
							
							smov.actionResult = Result.CUT;
							
							supportMoves.get(supporters.get(smov)).remove(smov);
							supporters.remove(smov);
						}
					}
				}
				
			}
		}
	}
	
	private void findUnresolvedMoves(Collection<Order> moves, Collection<Order> unresolvedMoves){
		for(Order ord: moves){
			
			if(ord.actionResult != Result.MAYBE) continue;
			
			if(ord.getClass() == Move.class){
				unresolvedMoves.add(ord);
			}else if(ord.getClass() == MoveByConvoy.class){
				unresolvedMoves.add(ord);
			}
		}
	}
	
	//	consider a move obliterated if it went head to head with another unit and got
	//	dislodged.  It no longer blocks other units from moving into where it was trying
	//	to go
	private void resolveHeadOnHeadMoves(Collection<Order> unresolvedMoves,
			Map<TerritorySquare, Order> moveOrigins,
			Map<Order, Collection<Order>> supportMoves,
			Collection<Order> obliteratedMoves){
		
		//	resolve any moves trying to swap places
		for(Order ord: unresolvedMoves.toArray(new Order[0])){
			
			if(ord.getClass() == Move.class){
				Move mov = (Move)ord;
				
				if(!unresolvedMoves.contains(mov)) continue;
				
				TerritorySquare sqr = mov.to;
				Order moveFromDest = moveOrigins.get(sqr);
				
				if(moveFromDest == null) continue;
				
				if(moveFromDest.getClass() == Move.class){
					Move movFromDest = (Move)moveFromDest;
					
					TerritorySquare destFromDest = movFromDest.to;
					
					if(mov.from == destFromDest){
						
						//	if they're the same player, they fail
						if(mov.player == moveFromDest.player){
							
							mov.actionResult = Result.BNC;
							movFromDest.actionResult = Result.BNC;
							
							unresolvedMoves.remove(mov);
							unresolvedMoves.remove(moveFromDest);
							
						}else{
						
							int supports1 = 0;
							if(supportMoves.containsKey(ord)){
								supports1 = supportMoves.get(ord).size();
							}
							
							int supports2 = 0;
							if(supportMoves.containsKey(movFromDest)){
								supports2 = supportMoves.get(movFromDest).size();
							}
							
							if(supports1 == supports2){
								
								mov.actionResult = Result.BNC;
								movFromDest.actionResult = Result.BNC;
								
								unresolvedMoves.remove(mov);
								unresolvedMoves.remove(moveFromDest);
								
								obliteratedMoves.add(mov);
								obliteratedMoves.add(movFromDest);

							}else if(supports1 > supports2){
								
								//mov.actionResult = Result.SUC;
								movFromDest.actionResult = Result.BNC;
								
								//unresolvedMoves.remove(mov);
								unresolvedMoves.remove(moveFromDest);

								obliteratedMoves.add(movFromDest);
							
							}else if(supports2 > supports1){
								
								mov.actionResult = Result.BNC;
								//movFromDest.actionResult = Result.SUC;
								
								unresolvedMoves.remove(mov);
								//unresolvedMoves.remove(moveFromDest);

								obliteratedMoves.add(mov);
							}
						}
					}
				}
				
			}
		}
	}
	
	private void calculateMostSupportForTerritories(
			Map<TerritorySquare, Collection<Order>> movesWantLocation,
			Map<Order, Collection<Order>> supportMoves,
			Collection<Order> obliteratedMoves,
			Map<TerritorySquare, Order> mostSupportForTerritory){
		
		for(TerritorySquare terr: movesWantLocation.keySet()){
			
			Collection<Order> competitors = movesWantLocation.get(terr);
			
			Order mostSupported = null;
			int mostSupports = -1;
			int numWithSupportCount = 0;
			
			for(Order ord: competitors){
				
				//if(ord.actionResult == Result.BNC) continue;
				
				if(ord.actionResult == Result.DSR ||
						ord.actionResult == Result.NSO) continue;
				
				int supporterCount = 0;
				if(supportMoves.containsKey(ord)){
					supporterCount = supportMoves.get(ord).size();
				}
				
				if(supporterCount > mostSupports){
					mostSupports = supporterCount;
					
					//	if it's not already bounce from a head to head
					if(ord.actionResult != Result.BNC){
						mostSupported = ord;
					}
					
					numWithSupportCount = 1;
				}else if(supporterCount == mostSupports){
					numWithSupportCount++;
				}
			}
			
			if(numWithSupportCount == 1 && mostSupported != null){
				
				if(!obliteratedMoves.contains(mostSupported)){
					mostSupportForTerritory.put(terr, mostSupported);
				}
			}
		}
	}
	
	private void findConvoySets(Collection<Order> moves,
			Map<TerritorySquare, Order> moveOrigins,
			Map<Convoy, MoveByConvoy> convoyToRelyingCto,
			Map<MoveByConvoy, Collection<Convoy>> ctoToConvoys
			){
		for(Order ord: moves){
			
			//	for each convoying fleet
			if(ord.getClass() == Convoy.class){
				Convoy conv = (Convoy) ord;
				
				TerritorySquare from = conv.from;
				Order convoyed = moveOrigins.get(from);
				
				if(convoyed.getClass() == MoveByConvoy.class){
					MoveByConvoy mbc = (MoveByConvoy)convoyed;
					
					if(mbc.transits.contains(conv.convoyer) &&
						mbc.convoyDestination == conv.to){
						
						convoyToRelyingCto.put(conv, mbc);
						
						if(!ctoToConvoys.containsKey(mbc)){
							ctoToConvoys.put(mbc, new HashSet<Convoy>());
						}
						
						ctoToConvoys.get(mbc).add(conv);
						
					}else{
						conv.actionResult = Result.NSO;
					}
					
				}else{
					conv.actionResult = Result.NSO;
				}
							
			}
		}
		
		for(Order ord: moves){
			
			if(ord.getClass() == MoveByConvoy.class){
				MoveByConvoy mbc = (MoveByConvoy)ord;
				
				if(!ctoToConvoys.containsKey(mbc)){
					mbc.actionResult = Result.NSO;
				}
				
				for(TerritorySquare tst: mbc.transits){
					
					Order help = moveOrigins.get(tst);
					
					if(help == null || help.getClass() != Convoy.class){
						mbc.actionResult = Result.NSO;
					}else{
						Convoy helpCon = (Convoy)help;
						
						if(helpCon.from != mbc.convoyOrigin ||
								helpCon.to != mbc.convoyDestination){
							mbc.actionResult = Result.NSO;
						}
					}
				}
			}
		}
	}
	
	private void dislodgeConvoys(
			Map<TerritorySquare, Order> mostSupportForTerritory,
			Map<Convoy, MoveByConvoy> convoyToRelyingCto,
			Map<MoveByConvoy, Collection<Convoy>> ctoToConvoys){
		//	for each valid convoying fleet
		for(Convoy cnv: convoyToRelyingCto.keySet()){
			//		figure out if it is dislodged	:

			//		get unit with most support to territory
			Order mostSupport = mostSupportForTerritory.get(cnv.convoyer);
			
			if(mostSupport == null){
				//		if it is null, no dislodge
			}else if(mostSupport.player == cnv.player){
				//		if the unit is friendly, no dislodge	
			}else{
				
				cnv.actionResult = Result.FAIL;
				
				//			find the convoy and DSR it							
				convoyToRelyingCto.get(cnv).actionResult = Result.DSR;

			}
		}
	}
	
	private void resolveBattles(Collection<Order> unresolvedMoves, BoardState bst,
			Map<Order, Collection<Order>> supportMoves,
			Map<TerritorySquare, Order> mostSupportForTerritory,
			Map<TerritorySquare, Collection<Order>> movesWantLocation,
			Map<TerritorySquare, Order> moveOrigins) throws Exception{
		
		while(!unresolvedMoves.isEmpty()){
			
			int unresolvedBefore = unresolvedMoves.size();
			
			//loop through each
			for(Order ord: unresolvedMoves.toArray(new Order[0])){
				//	iteratively see if moves succeed.  for each unresolved move:
				
				//	could get modified within
				if(ord.actionResult != Result.MAYBE) continue;
				
				TerritorySquare moveDestination = null;
				
				if(ord.getClass() == Move.class){
					moveDestination = ((Move)ord).to;
				}else if(ord.getClass() == MoveByConvoy.class){
					moveDestination = ((MoveByConvoy)ord).convoyDestination;
				}

				//	if the territory it moves into was originally unoccupied, or the move has
				//	2 or more support and the original occupier was a different power	
				if(moveDestination.getOccupier(bst) == null || 
						(supportMoves.containsKey(ord) &&	//	no exceptions please 	
						supportMoves.get(ord).size() > 0)){ 	// if it has 1 or more support and it's a different person, it won't get held 
															//	up by blocked moves
						
					//	get the move with the most support into the destination territory
					//	(if there is such a move)
					Order successfulOrder = mostSupportForTerritory.get(moveDestination);
					Collection<Order> competitors = movesWantLocation.get(moveDestination);
					
					if(successfulOrder != null){
						
						if(successfulOrder.actionResult == Result.MAYBE &&
							(		moveDestination.getOccupier(bst) == null || 
									successfulOrder.player != moveDestination.getOccupier(bst).belongsTo)){
						
							//	set it as succeed if it's a move
							if( successfulOrder.getClass() == Move.class ||
								successfulOrder.getClass() == MoveByConvoy.class){
								
								successfulOrder.actionResult = Result.SUC;
								unresolvedMoves.remove(successfulOrder);
							}
						}else if(moveDestination.getOccupier(bst) != null && 
								successfulOrder.player == moveDestination.getOccupier(bst).belongsTo){
							
							Order friendlyAt = moveOrigins.get(moveDestination);
							
							if(friendlyAt == null){
								throw new Exception("friendlyAt null wtf");
							}
							
							if( friendlyAt.getClass() == Move.class || 
								friendlyAt.getClass() == MoveByConvoy.class){
								
								if(friendlyAt.actionResult == Result.SUC){
									
									successfulOrder.actionResult = Result.SUC;
									unresolvedMoves.remove(successfulOrder);
									
								}
								//	then you're stuck
								else if(friendlyAt.actionResult == Result.BNC ||
										friendlyAt.actionResult == Result.DSR){
									
									if(	successfulOrder.getClass() == Move.class ||
										successfulOrder.getClass() == MoveByConvoy.class){
										
										successfulOrder.actionResult = Result.BNC;
										unresolvedMoves.remove(successfulOrder);
									}
								}
							}else{
								
								if( successfulOrder.getClass() == Move.class ||
									successfulOrder.getClass() == MoveByConvoy.class){
									
									successfulOrder.actionResult = Result.BNC;
									unresolvedMoves.remove(successfulOrder);
								}	
							}
						}
						
						//	set other moves wanting the territory as failing
						//	if it was a hold or a support hold or a convoy, it's been 
						//	dislodged as well
						for(Order otherOrd: competitors){
							if(otherOrd != successfulOrder){
								
								if(otherOrd.actionResult == Result.MAYBE){
									
									if( otherOrd.getClass() == Move.class ||
										otherOrd.getClass() == MoveByConvoy.class){
										
										otherOrd.actionResult = Result.BNC;
										unresolvedMoves.remove(otherOrd);
									}
									
									//	should only be a convoy or hold here...
									else{
										
										if(successfulOrder.player != otherOrd.player){
											otherOrd.actionResult = Result.FAIL;
											unresolvedMoves.remove(otherOrd);
										}
									}
								}
							}
						}
						
					}
					
					//	else all moves into it fail 
					else{
						for(Order otherOrd: competitors){
							if( otherOrd.getClass() == Move.class || 
								otherOrd.getClass() == MoveByConvoy.class){
		
								if(otherOrd.actionResult == Result.MAYBE){
									
									if( otherOrd.getClass() == Move.class ||
										otherOrd.getClass() == MoveByConvoy.class){
										
										otherOrd.actionResult = Result.BNC;
										unresolvedMoves.remove(otherOrd);
									}
									else{
										
										otherOrd.actionResult = Result.FAIL;
										unresolvedMoves.remove(otherOrd);
										
									}
								}
							}
						}
					}
					
				}else{
					//		else if the unit originally in the territory has successfully moved out
					Order moveOut = moveOrigins.get(moveDestination);
					Order successfulOrder = mostSupportForTerritory.get(moveDestination);
					Collection<Order> competitors = movesWantLocation.get(moveDestination);
					
					if(successfulOrder == null){
						for(Order attempt: competitors){
							if(	attempt.getClass() != SupportHold.class &&
								attempt.getClass() != SupportMove.class &&
								attempt.getClass() != Hold.class &&
								attempt.getClass() != Convoy.class){
								
								attempt.actionResult = Result.BNC;
								unresolvedMoves.remove(attempt);	
							}
						}
					}
					
					else if((moveOut.getClass() == Move.class || 
							 moveOut.getClass() == MoveByConvoy.class) &&
							moveOut.actionResult == Result.SUC){

						//	set the designated move as a success
						successfulOrder.actionResult = Result.SUC;
						
						unresolvedMoves.remove(successfulOrder);
						
						for(Order otherOrd: competitors){
							if(otherOrd != successfulOrder){
								if( otherOrd.actionResult == Result.MAYBE){
									
									otherOrd.actionResult = Result.BNC;
									unresolvedMoves.remove(otherOrd);	
								}
							}
						}
					}
					
					//	in this case it wasn't trying to go anywhere, so this action
					//	is out of luck
					else if(!(moveOut.getClass() == Move.class || 
							  moveOut.getClass() == MoveByConvoy.class)){
						ord.actionResult = Result.BNC;
						unresolvedMoves.remove(ord);	
					}
					
					//	if the unit was trying to move out but failed
					else if((moveOut.actionResult == Result.BNC || 
							 moveOut.actionResult == Result.DSR) &&
							(moveOut.getClass() == Move.class || 
							 moveOut.getClass() == MoveByConvoy.class)){
						ord.actionResult = Result.BNC;
						unresolvedMoves.remove(ord);	
					}
				}
			}
			
			int unresolvedAfter = unresolvedMoves.size();
			
			if(!unresolvedMoves.isEmpty() && unresolvedBefore == unresolvedAfter){
				
				//	if there are two units moving to any location, fail those and see if anything propagates
				
				Map<TerritorySquare, Set<Order>> conflicts = new HashMap<TerritorySquare, Set<Order>>();
				
				boolean anythingBroken = false;
				for(Order ord: unresolvedMoves){
					
					TerritorySquare dest = null;
					if(ord.getClass() == Move.class){
						dest = ((Move)ord).to;
					}else if(ord.getClass() == MoveByConvoy.class){
						dest = ((MoveByConvoy)ord).convoyDestination;
					}
					
					if(!conflicts.containsKey(dest)){
						conflicts.put(dest, new HashSet<Order>());
					}
					
					conflicts.get(dest).add(ord);
				}
				
				for(TerritorySquare sqr: conflicts.keySet()){
					
					Set<Order> conflicters = conflicts.get(sqr);
					
					if(conflicters.size() > 1){
						for(Order ord: conflicters){
							ord.actionResult = Result.BNC;
							unresolvedMoves.remove(ord);
						}
						
						anythingBroken = true;
					}
				}
				
				if(!anythingBroken){
					//	then we have a cycle or two (or more)
					Set<Order> notInCycle = new HashSet<Order>();
					while(!unresolvedMoves.isEmpty()){

						//	1) grab a random unresolved move
						Order top = null;
						Iterator<Order> it = unresolvedMoves.iterator();
						do{
							top = it.next();
						}while(notInCycle.contains(top));
						
						Order next = top;
						Set<Order> cycle = new HashSet<Order>();
						//	2) get all orders in the ring
						do{
							
							if(cycle.contains(next) && next != top){
								notInCycle.add(top);
								break;
							}
							
							cycle.add(next);
							
							TerritorySquare dest = null;
							if(next.getClass() == Move.class){
								dest = ((Move)next).to;
							}else if(next.getClass() == MoveByConvoy.class){
								dest = ((MoveByConvoy)next).convoyDestination;
							}
							
							next = moveOrigins.get(dest);
							
						}while(next != top && next != null);
						
						if(cycle.size() > 2){
							for(Order ord: cycle){
								ord.actionResult = Result.SUC;
							}
						}else{
							for(Order ord: cycle){
								ord.actionResult = Result.BNC;
							}
						}
						
						unresolvedMoves.removeAll(cycle);
					}
				}
			}
		}
	}
	
	private void mapSuccessfulMoves(Collection<Order> moves, Map<TerritorySquare, Order> successfulMoves){
		for(Order order: moves){
			if(order.actionResult == Result.SUC){
				if(order.getClass() == Move.class){
					Move mov = (Move)order;
					
					successfulMoves.put(mov.to, mov);
	
				}else if(order.getClass() == MoveByConvoy.class){
					MoveByConvoy mbc = (MoveByConvoy)order;
	
					successfulMoves.put(mbc.convoyDestination, mbc);
				}
			}
		}
	}
	
	private void resolveRetreats(Collection<Order> moves){
		Map<TerritorySquare, Set<Order>> retreatTo = new HashMap<TerritorySquare, Set<Order>>();
		
		for(Order ord:moves){
			if(ord.getClass() == Retreat.class){
				Retreat ret = (Retreat)ord;
				
				if(!retreatTo.containsKey(ret.to)){
					retreatTo.put(ret.to, new HashSet<Order>());
				}

				retreatTo.get(ret.to).add(ret);
			
			}
		}
		
		for(TerritorySquare sqr: retreatTo.keySet()){
			
			Set<Order> retreating = retreatTo.get(sqr);
			
			if(retreating.size() > 1){
				for(Order ord: retreating){
					ord.actionResult = Result.BNC;
				}
			}else{
				for(Order ord: retreating){
					ord.actionResult = Result.SUC;
				}
			}
		}
	}
	
	private void setRetreats(Collection<Order> moves, Map<TerritorySquare, Order> successfulMoves){
		for(Order ord: moves){

			if(ord.actionResult == Result.MAYBE){
				
				ord.actionResult = Result.SUC;
			}
			
			//	if it was one of these, it can't retreat ever 
			if(	ord.getClass() == Build.class || 
				ord.getClass() == Remove.class || 
				ord.getClass() == Waive.class ||
				ord.getClass() == Disband.class ||
				ord.getClass() == Retreat.class){
				
				ord.retreatState = RetreatState.NA;
			}
			
			else if(ord.getClass() == Move.class){
				Move mov = (Move)ord;
				
				//	if the move failed
				if(mov.actionResult != Result.SUC){
					
					//	and something successfully moved into the square
					if(successfulMoves.get(mov.from) != null){
						mov.retreatState = RetreatState.RET;
					}else{
						mov.retreatState = RetreatState.NO;
					}
				}
			
			}
			else if(ord.getClass() == MoveByConvoy.class){
				MoveByConvoy mov = (MoveByConvoy)ord;
				
				//	if the move failed
				if(mov.actionResult != Result.SUC){
					
					//	and something successfully moved into the square
					if(successfulMoves.get(mov.convoyOrigin) != null){
						mov.retreatState = RetreatState.RET;
					}else{
						mov.retreatState = RetreatState.NO;
					}
				}
			}
			else if(ord.getClass() == Hold.class){
				Hold mov = (Hold)ord;
				
				//	and something successfully moved into the square
				if(successfulMoves.get(mov.holdingSquare) != null){
					mov.retreatState = RetreatState.RET;
				}else{
					mov.retreatState = RetreatState.NO;
				}
			}
			else if(ord.getClass() == SupportHold.class){
				SupportHold mov = (SupportHold)ord;
				
				//	and something successfully moved into the square
				if(successfulMoves.get(mov.supportFrom) != null){
					mov.retreatState = RetreatState.RET;
					
					//	if you're dislodged, your support is also cut, which takes precedence
					if(mov.actionResult == Result.FAIL){
						mov.actionResult = Result.CUT;
					}
					
				}else{
					mov.retreatState = RetreatState.NO;
				}
				
			}
			else if(ord.getClass() == SupportMove.class){
				SupportMove mov = (SupportMove)ord;
				
				//	and something successfully moved into the square
				if(successfulMoves.get(mov.supportFrom) != null){
					mov.retreatState = RetreatState.RET;
					
					//	if you're dislodged, your support is also cut, which takes precedence
					if(mov.actionResult == Result.FAIL){
						mov.actionResult = Result.CUT;
					}
					
				}else{
					mov.retreatState = RetreatState.NO;
				}

			}
			else if(ord.getClass() == Convoy.class){
				Convoy mov = (Convoy)ord;

				//	and something successfully moved into the square
				if(successfulMoves.get(mov.convoyer) != null){
					mov.retreatState = RetreatState.RET;
				}else{
					mov.retreatState = RetreatState.NO;
				}

			}
			
			if(ord.retreatState == RetreatState.MAYBE){
				ord.retreatState = RetreatState.NO;
			}
			
		}
	}
	
	private void printMaps(Set<Order> moves,
			Map<Order, Set<Order>> supportMoves,
			Map<TerritorySquare, Order> mostSupportForTerritory,
			Map<TerritorySquare, Set<Order>> movesWantLocation,
			Map<TerritorySquare, Order> moveOrigins,
			BoardState bst){
		
		System.out.println("Moves to location:");
		for(TerritorySquare terr: movesWantLocation.keySet()){
			System.out.println("\t"+terr.getName());
			for(Order ord: movesWantLocation.get(terr)){
				System.out.println("\t\t"+ord.toOrder(bst));
			}
		}

		System.out.println("Moves from location:");
		for(TerritorySquare terr: moveOrigins.keySet()){
			System.out.println("\t"+terr.getName()+"\t"+moveOrigins.get(terr).toOrder(bst));
		}

		System.out.println("Moves supporting moves:");
		for(Order ord: supportMoves.keySet()){
			System.out.println("\t"+ord.toOrder(bst));
			for(Order supp: supportMoves.get(ord)){
				System.out.println("\t\t"+supp.toOrder(bst));
			}
		}
		
		System.out.println("Most supports into territory:");
		for(TerritorySquare terr: mostSupportForTerritory.keySet()){
			System.out.println("\t"+terr.getName()+"\t"+mostSupportForTerritory.get(terr).toOrder(bst));
		}

		System.out.println("Resolved as: ");
		for(Order ord: moves){
			System.out.println("\t"+ord.toOrder(bst)+ " "+ord.actionResult+" "+ord.retreatState);
		}
	}
	
	//	check indicates whether this is an update from the server that we are just verifying
	//	(to test our own code)
	
	private void resolve(BoardState bst, Collection<Order> moves, boolean check) throws Exception{

		Map<Order, Result> actualResults = new HashMap<Order, Result>();
		Map<Order, RetreatState> actualRetreats = new HashMap<Order, RetreatState>();
		
		if(check){
			for(Order ord: moves){
				actualResults.put(ord, ord.actionResult);
				actualRetreats.put(ord, ord.retreatState);
				
				ord.actionResult = Result.MAYBE;
				ord.retreatState = RetreatState.MAYBE;
			}
		}
		
		//moves which want a location
		Map<TerritorySquare, Collection<Order>> movesWantLocation = 
			new HashMap<TerritorySquare, Collection<Order>>();
			
		//where moves come from
		Map<TerritorySquare, Order> moveOrigins = 
			new HashMap<TerritorySquare, Order>();
		
		//	populate moves want location and move origins
		calcMovesByDestination(moves, movesWantLocation, moveOrigins);
		
		//from the order to the orders which support it
		Map<Order, Collection<Order>> supportMoves = 
			new HashMap<Order, Collection<Order>>();
		
		//from the support order to the order it supports
		Map<Order, Order> supporters =
			new HashMap<Order, Order>();
		
		//	populate supportMoves and supporters
		calculateSupport(moves, movesWantLocation, moveOrigins, supportMoves, supporters);
		
		//	TODO you cannot support an attack against your own units.  Nothing 
		//	anywhere here deals correctly with you supporting an enemy against 
		//	your own units, because it's nasty as hell--that can still block
		//	a different unit moving it, but it can't assist in the dislodge of
		//	your unit

		//	if a unit is dislodged by the unit it is support moving into, the support is cut
		resolveDislodgedSupportMoves(moves, movesWantLocation, moveOrigins, supportMoves, supporters);
		
		Collection<Order> unresolvedMoves = new HashSet<Order>();
		
		//	populate the moves that have not been resolved yet
		findUnresolvedMoves(moves, unresolvedMoves);

		Collection<Order> obliteratedMoves = new HashSet<Order>();
		
		//	consider a move obliterated if it went head to head with another unit and got
		//	dislodged.  It no longer blocks other units from moving into where it was trying to go
		resolveHeadOnHeadMoves(unresolvedMoves, moveOrigins, supportMoves, obliteratedMoves);
		
		//	add only if there is a unique move with the most support
		Map<TerritorySquare, Order> mostSupportForTerritory = new HashMap<TerritorySquare, Order>();
		
		//	populate this map with the orders that have most support into a territory so far
		calculateMostSupportForTerritories(movesWantLocation, supportMoves, obliteratedMoves, mostSupportForTerritory);
		
		//	map from convoy to the unit that is moving
		Map<Convoy, MoveByConvoy> convoyToRelyingCto = new HashMap<Convoy, MoveByConvoy>(); 
		
		//	map from a convoying unit to all those supporting it
		Map<MoveByConvoy, Collection<Convoy>> ctoToConvoys = new HashMap<MoveByConvoy, Collection<Convoy>>();

		//	populate those two structures with all the consistent convoys
		findConvoySets(moves, moveOrigins, convoyToRelyingCto, ctoToConvoys);
		
		//	figure out which convoys are to be dislodged, and mark it as so
		dislodgeConvoys(mostSupportForTerritory, convoyToRelyingCto, ctoToConvoys);
		
		//	recalculate now that bad convoys are erased
		mostSupportForTerritory.clear();
		calculateMostSupportForTerritories(movesWantLocation, supportMoves, obliteratedMoves, mostSupportForTerritory);
		
		//	find the moves that are still unresolved
		unresolvedMoves.clear();
		findUnresolvedMoves(moves, unresolvedMoves);
		
		//	the meat of the resolution--resolving all standard land battles
		resolveBattles(unresolvedMoves, bst, supportMoves, mostSupportForTerritory, movesWantLocation, moveOrigins);
		
		//	build map of the units succesfully moving to each location
		Map<TerritorySquare, Order> successfulMoves = new HashMap<TerritorySquare, Order>();
		
		//	calculate destinations 
		mapSuccessfulMoves(moves, successfulMoves);
		
		//	resolve retreat orders, if this is that time of year
		resolveRetreats(moves);
		
		//	set the units that have to retreat.  Also set success on anything that hasn't 
		//	had anything marked so far (supports and convoys)
		setRetreats(moves, successfulMoves);
		
		//System.out.println("Time to resolve: "+(tEnd-tStart));
		


			

		//	check to see how disastrously we are off in our resolutions
		if(check){
			
			//	print data for debugging
			//	printMaps(moves, supportMoves, mostSupportForTerritory, movesWantLocation, moveOrigins, bst);
				
			for(Order ord: moves){
				
				Result calculatedResult = ord.actionResult;
				Result realResult = actualResults.get(ord);
				
				RetreatState calculatedRetreat = ord.retreatState;
				RetreatState realRetreat = actualRetreats.get(ord);
				
				if(Bot.LOGGING){
					if(calculatedResult != realResult){
						System.out.println("Found discrepency: ");
						System.err.println("\tresult of order "+ord.toOrder(bst)+" should have resolved as "+realResult+ " but was resolved as "+calculatedResult);
					}
					
					if(calculatedRetreat != realRetreat){
						System.out.println("Found discrepency: ");
						System.err.println("\tretreat for order "+ord.toOrder(bst)+" should have resolved as "+realRetreat+ " but was resolved as "+calculatedRetreat);
					}
				}
				
				//	if things were resolved differently we have to go with the server's version, after printing out 
				ord.actionResult = realResult;
				ord.retreatState = realRetreat;
			}
		}
	}
	
	//	a VERY rough pass through the moves.  Meant to quickly (and not really correctly) see which
	//	moves would succeed if nobody else did anything, so we don't have to update the whole board
	//	state or go through an expensive resolve call
	public void quickResolve(BoardState bst, Set<Order> orders, Player us)
	{
		Map<TerritorySquare, Move> needsSupport = new HashMap<TerritorySquare, Move>();
		Set<SupportMove> supports = new HashSet<SupportMove>();
		for(Order o: orders)
		{
			if(o.getClass() == Hold.class)
			{
				o.actionResult = Result.SUC;
			}
			
			if(o.getClass() == Move.class)
			{
				Move moveOrder = (Move)o;
				if(moveOrder.to.getOccupier(bst) == null)
				{
					moveOrder.actionResult = Result.SUC;
				}
				else if(moveOrder.to.getOccupier(bst).belongsTo == us)
				{
					//TODO If we need to
				}
				else
				{
					needsSupport.put(moveOrder.to, moveOrder);
					moveOrder.actionResult = Result.FAIL;
				}
			}
			
			if(o.getClass() == SupportMove.class)
			{
				SupportMove support = (SupportMove)o;
				supports.add(support);
			}
		}
		
		for(SupportMove sm: supports)
		{
			if(needsSupport.containsKey(sm.supportInto))
			{
				needsSupport.get(sm.supportInto).actionResult = Result.SUC;
				sm.actionResult = Result.SUC;
			}
			else
			{
				sm.actionResult = Result.FAIL;
			}
		}
	}
	
	public BoardState update(YearPhase time, BoardState orig, Collection<Order> moves, boolean fromServer) throws Exception{

		long tStart = System.nanoTime();
		
		//	figure out which moves were successful
		
		//TODO for now call resolve even if from server; in future skip this if the moves are already resolved
		resolve(orig, moves, fromServer);
		
//		long tAfterResolve = System.nanoTime();
		
		BoardState bst = orig.clone(time);
		
//		long tAfterClone = System.nanoTime();
		
		//process movements separately--slightly more complex resolutions
		Set<Order> successfulMoves = new HashSet<Order>();
		
		//	note that retreats are being processed, so supports might look wonky
		//	(with "unoccupied" where they are supporting)
		//System.out.println("Successful orders to process:");
		for(Order ord: moves){
				
			if(ord.actionResult == Result.SUC){
				//System.out.println(ord.toOrder(bst)+ " "+ord.getResult());
				
				if(ord.getClass() == Build.class){
					Build b = (Build)ord;
					
					setOccupier(bst, b.build, b.location, b.coast);
					
				}else if(ord.getClass() == Convoy.class){
					
					//nothing to do if a convoy succeeds
					
				}else if(ord.getClass() == Disband.class){
					Disband dsb = (Disband)ord;

					//removeOccupier(dsb.disbandAt);
					bst.resolveRetreat(dsb.disbandAt);
					
				}else if(ord.getClass() == Hold.class){
					
					//nothing to do if a hold succeeds
					
				}else if(ord.getClass() == Move.class){
					
					successfulMoves.add(ord);
					
				}else if(ord.getClass() == MoveByConvoy.class){
					
					successfulMoves.add(ord);
					
				}else if(ord.getClass() == Remove.class){
					Remove rem = (Remove)ord;
					
					removeOccupier(bst, rem.disbandLocation);
					
				}else if(ord.getClass() == Retreat.class){
					Retreat ret = (Retreat)ord;
					
					//	should be able to resolve the retreats here -- shouldn't ever have the issue
					//	of retreating somewhere someone else retreats from
					
					bst.resolveRetreat(ret.from);
					setOccupier(bst, ret.retreatingUnit, ret.to, ret.destCoast);
					
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
					
					bst.setRetreatingUnit(c.convoyingUnit, c.convoyer, "NA");
					this.removeOccupier(bst, c.convoyer);
					
				}else if(ord.getClass() == Hold.class){
					Hold hold = (Hold)ord;
					
					bst.setRetreatingUnit(hold.holdingUnit, hold.holdingSquare, hold.holdingSquare.getOccupiedCoast(bst));
					this.removeOccupier(bst, hold.holdingSquare);
					
				}else if(ord.getClass() == Move.class){
					Move mov = (Move)ord;
					
					bst.setRetreatingUnit(mov.unit, mov.from, mov.from.getOccupiedCoast(bst));
					this.removeOccupier(bst, mov.from);
					
				}else if(ord.getClass() == MoveByConvoy.class){
					MoveByConvoy mbc = (MoveByConvoy)ord;
					
					bst.setRetreatingUnit(mbc.convoyedUnit, mbc.convoyOrigin, "NA");
					this.removeOccupier(bst, mbc.convoyOrigin);
					
				}else if(ord.getClass() == SupportHold.class){
					SupportHold shold = (SupportHold)ord;
					
					bst.setRetreatingUnit(shold.supporter, shold.supportFrom, shold.supportFrom.getOccupiedCoast(bst));
					this.removeOccupier(bst, shold.supportFrom);
					
				}else if(ord.getClass() == SupportMove.class){
					SupportMove smove = (SupportMove)ord;
					
					bst.setRetreatingUnit(smove.supporter, smove.supportFrom, smove.supportFrom.getOccupiedCoast(bst));
					this.removeOccupier(bst, smove.supportFrom);
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
				
				removeOccupier(bst, mbc.convoyOrigin);
				destinations.put(mbc.convoyDestination, mbc);
				
			}else if(ord.getClass() == Move.class){
				Move mov = (Move)ord;
				
				removeOccupier(bst, mov.from);
				destinations.put(mov.to, mov);
			}
		}
		
		for(TerritorySquare sq: destinations.keySet()){
			Order ord = destinations.get(sq);
			
			if(ord.getClass() == MoveByConvoy.class){
				MoveByConvoy mbc = (MoveByConvoy)ord;
				
				setOccupier(bst, mbc.convoyedUnit, mbc.convoyDestination);
				
			}else if(ord.getClass() == Move.class){
				Move mov = (Move)ord;
				
				setOccupier(bst, mov.unit, mov.to, mov.coast);
			}
		}
		
		bst.updateHistory(orig.time.year, orig.time.phase, moves);
		
		if(time.phase == Phase.WIN){
			updateSupplyControl(bst);
		}
		
		//	even if winter didn't happen, update controls
		if(time.phase == Phase.SPR && orig.time.phase != Phase.WIN){
			updateSupplyControl(bst);
		}
		
//		long tAfterUpdate = System.nanoTime();
		
//		if(orig.time.phase == Phase.SPR || orig.time.phase == Phase.FAL){
//			runResolve+=((tAfterResolve- tStart));
//			runClone+=((tAfterClone-tAfterResolve));
//			runUpdate+=((tAfterUpdate-tAfterClone));
//		}
//		
//		if(count++%10000==0){
//			System.out.println("Resolve: "+runResolve/count);
//			System.out.println("Clone: "+runClone/count);
//			System.out.println("Update: "+runUpdate/count);
//		}
		
		return bst;
	}
	
	double count = 0;
//	
//	long runResolve = 0;
//	long runClone = 0;
//	long runUpdate = 0;
	
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
	
	public void assertCanMove(BoardState bst, Player p, TerritorySquare from, TerritorySquare to) throws Exception{
		assertCanMove(bst, p, from, to, "NA");
	}
	
	public boolean canMove(BoardState bst, Player p, TerritorySquare from, TerritorySquare to){
		return canMove(bst, p, from, to, "NA");
	}
	
	public void assertCanMove(BoardState bst, Player p, TerritorySquare from, TerritorySquare to, String destinationCoast) throws Exception{
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		//	make sure unit is there
		if(from.getOccupier(bst) == null){
			throw new Exception("no occupier");
		}
		
		//	player correct
		if(from.getOccupier(bst).belongsTo != p){
			throw new Exception("wrong occupying player: "+from.getOccupier(bst));
		}
		
//		if(!from.getOccupier(bst).army && to.hasMultipleCoasts() && destinationCoast.equals("NA")){
//			throw new Exception("need to specify a destination coast ");
//		}
		
		if(from.getOccupier(bst).army){
			
			//if the unit moving is an army, make sure it can do this
			if(!from.isLandBorder(to)){
				throw new Exception("no land border from "+from.getName()+" to "+to.getName());
			}
		}else{
			String occupiedCoast = from.getOccupiedCoast(bst);			
			
			//if it's a fleet, make sure the coasts match up
			if(!from.isSeaBorder(to, occupiedCoast, destinationCoast)){
				throw new Exception("no such sea border from "+from.getName()+" to "+to.getName());
			}
		}
	}
	
	public boolean canMove(BoardState bst, Player p, TerritorySquare from, TerritorySquare to, String destinationCoast){
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			return false;
		}
		
		
		//	make sure unit is there
		if(from.getOccupier(bst) == null){
			return false;
		}
		
		//	player correct
		if(from.getOccupier(bst).belongsTo != p){
			return false;
		}

		
		if(from.getOccupier(bst).army){
			
			//if the unit moving is an army, make sure it can do this
			if(!from.isLandBorder(to)){
				return false;
			}
		}else{
			String occupiedCoast = from.getOccupiedCoast(bst);			
			
			//if it's a fleet, make sure the coasts match up
			if(!from.isSeaBorder(to, occupiedCoast, destinationCoast)){
				return false;
			}
		}
		
		return true;
	}
	
	
	public void assertCanSupportHold(BoardState bst, Player p, TerritorySquare from, TerritorySquare to) throws Exception{
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		if(to.getOccupier(bst) == null){
			throw new Exception("no occupier");
		}
		
		if(from.getOccupier(bst).belongsTo != p){
			throw new Exception("wrong occupying country");
		}
		
		assertCanMove(bst, p, from, to);	
	}
	
	public boolean canSupportHold(BoardState bst, Player p, TerritorySquare from, TerritorySquare to){
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			return false;
		}
		
		if(to.getOccupier(bst) == null){
			return false;
		}
		
		if(from.getOccupier(bst).belongsTo != p){
			return false;
		}

		
		return canMove(bst, p, from, to);	
	}
	
	public void assertCanSupportMove(BoardState bst, Player p, TerritorySquare supporter, TerritorySquare from, TerritorySquare to) throws Exception{
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		
		if(supporter.getOccupier(bst) == null){
			throw new Exception("no supporter");
		}
		
		if(supporter.getOccupier(bst).belongsTo != p){
			throw new Exception("wrong occupying country");
		}
		
		if(from.getOccupier(bst) == null){
			throw new Exception("no occupier");
		}
		
		assertCanMove(bst, p, supporter, to);
		
		//	because it could be convoyed, can't check this
		//assertCanMove(bst, p, from, to);
		
	}
	
	public boolean canSupportMove(BoardState bst, Player p, TerritorySquare supporter, TerritorySquare from, TerritorySquare to){
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			return false;
		}
		
		if(supporter.getOccupier(bst).belongsTo != p){
			return false;
		}
		
		if(supporter.getOccupier(bst) == null){
			return false;
		}
		
		if(from.getOccupier(bst) == null){
			return false;
		}
		
		if(!canMove(bst, p, supporter, to)){
			return false;
		}
		
		if(!canMove(bst, p, from, to)){
			return false;
		}
		
		return true;
	}
	
	public boolean assertCanAssistConvoy(BoardState bst, Player p, TerritorySquare convoyer, TerritorySquare from, TerritorySquare to) throws Exception{
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		if(convoyer.getOccupier(bst) == null){
			throw new Exception("empty convoyer");
		}
		
		if(from.getOccupier(bst) == null){
			throw new Exception("empty occupier");
		}
		
		if(convoyer.getOccupier(bst).belongsTo != p){
			throw new Exception("wrong occupying country");
		}
		
		Unit convoyingUnit = convoyer.getOccupier(bst);
		Unit convoyedUnit = from.getOccupier(bst);
		
		if(convoyer.isLand() || convoyingUnit.army || !convoyedUnit.army){
			throw new Exception("invalid type of unit being convoyed or convoying");
		}
		
		if(!from.hasAnySeaBorders() || !to.hasAnySeaBorders()){
			throw new Exception("convoy cannot succeed");
		}
		
		return true;
	}
	
	public boolean canAssistConvoy(BoardState bst, Player p, TerritorySquare convoyer, TerritorySquare from, TerritorySquare to){
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			return false;
		}
		
		if(convoyer.getOccupier(bst) == null || from.getOccupier(bst) == null){
			return false;
		}
		
		if(convoyer.getOccupier(bst).belongsTo != p){
			return false;
		}
		
		Unit convoyingUnit = convoyer.getOccupier(bst);
		Unit convoyedUnit = from.getOccupier(bst);
		
		if(convoyer.isLand() || convoyingUnit.army || !convoyedUnit.army){
			return false;
		}
		
		if(!from.hasAnySeaBorders() || !to.hasAnySeaBorders()){
			return false;
		}
		
		return true;
	}
	
	public void assertCanConvoy(BoardState bst, Player p, TerritorySquare from, TerritorySquare to, List<TerritorySquare> transit) throws Exception{
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		//	units need to exist, territories have to be sea, only fleets convoy
		
		Unit movingUnit = from.getOccupier(bst);
		
		if(movingUnit == null || !movingUnit.army){
			throw new Exception("this unit cannot convoy");
		}
		
		if(movingUnit.belongsTo != p){
			throw new Exception("wrong player");
		}
		
		for(TerritorySquare sqr: transit){
			if(sqr.getOccupier(bst) == null || sqr.isLand() || sqr.getOccupier(bst).army){
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
	
	public boolean canConvoy(BoardState bst, Player p, TerritorySquare from, TerritorySquare to, List<TerritorySquare> transit){
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			return false;
		}
		
		//	units need to exist, territories have to be sea, only fleets convoy
		
		Unit movingUnit = from.getOccupier(bst);
		
		if(movingUnit == null || !movingUnit.army){
			return false;
		}
		
		if(movingUnit.belongsTo != p){
			return false;
		}
		
		for(TerritorySquare sqr: transit){
			if(sqr.getOccupier(bst) == null || sqr.isLand() || sqr.getOccupier(bst).army){
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
	
	public boolean canHold(BoardState bst, Player p, TerritorySquare holder){
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			return false;
		}
		
		if(holder.getOccupier(bst) == null){
			return false;
		}
		
		if(holder.getOccupier(bst).belongsTo != p){
			return false;
		}
		
		//	as long as there's a unit there it can hold...
		return true;
		
	}
	
	public void assertCanHold(BoardState bst, Player p, TerritorySquare holder) throws Exception{
		
		if(bst.time.phase == Phase.WIN || bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		if(holder.getOccupier(bst) == null){
			throw new Exception("null occupier");
		}
		
		if(holder.getOccupier(bst).belongsTo != p){
			throw new Exception("holder belongs to wrong player");
		}
	}
	
	public void assertCanBuild(BoardState bst, Player p, Unit u, TerritorySquare location) throws Exception{
		
		//	it must be winter
		if(bst.time.phase != Phase.WIN){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		//	location must be unoccupied
		if(location.getOccupier(bst) != null){
			throw new Exception("occupier is null");
		}
		
		//	location must be controlled by this player
		if(location.getController(bst) != p){
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
		if(u.belongsTo.getNumberUnits(bst) >= u.belongsTo.getNumberSupplyCenters(bst)){
			throw new Exception("player "+u.belongsTo+" does not have any builds");
		}
		
		//	location must controlled by u.controller
		if(location.getController(bst) != u.belongsTo){
			throw new Exception("player does not control center");
		}
	}
	
	public boolean canBuild(BoardState bst, Player p, TerritorySquare location){
		
		//	it must be winter
		if(bst.time.phase != Phase.WIN){
			return false;
		}
		
		//	location must be unoccupied
		if(location.getOccupier(bst) != null){
			return false;
		}
		
		//	location must be controlled by this player
		if(location.getController(bst) != p){
			return false;
		}

		//	location must be home territory of u.controller
		if(location.getHomePlayer() != p){
			return false;
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(p.getNumberUnits(bst) >= p.getNumberSupplyCenters(bst)){
			return false;
		}
		
		//	location must controlled by u.controller
		if(location.getController(bst) != p){
			return false;
		}
		
		return true;
	}
	
	public boolean canBuild(BoardState bst, Player p, Unit u, TerritorySquare location){
		
		//	it must be winter
		if(bst.time.phase != Phase.WIN){
			return false;
		}
		
		//	location must be unoccupied
		if(location.getOccupier(bst) != null){
			return false;
		}
		
		//	location must be controlled by this player
		if(location.getController(bst) != p){
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
		if(u.belongsTo.getNumberUnits(bst) >= u.belongsTo.getNumberSupplyCenters(bst)){
			return false;
		}
		
		//	location must controlled by u.controller
		if(location.getController(bst) != u.belongsTo){
			return false;
		}
		
		if(!u.army && !location.hasAnySeaBorders()){
			return false;
		}
		
		return true;
	}
	
	public boolean assertCanWaive(BoardState bst, Player p) throws Exception{
		
		//	it must be winter
		if(bst.time.phase != Phase.WIN){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(p.getNumberUnits(bst) >= p.getNumberSupplyCenters(bst)){
			throw new Exception("no available builds to waive");
		}
		
		return true;
	}
	
	public boolean canWaive(BoardState bst, Player p){
		
		//	it must be winter
		if(bst.time.phase != Phase.WIN){
			return false;
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(p.getNumberUnits(bst) >= p.getNumberSupplyCenters(bst)){
			return false;
		}
		
		return true;
	}
	
	public void assertCanDisband(BoardState bst, Player p, TerritorySquare location) throws Exception{
		
		//	it must be retreat season
		if(bst.time.phase != Phase.AUT && bst.time.phase != Phase.SUM){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		//	there must be a pending retreat from this location
		if(bst.getRetreatForTerritory(location) == null){
			throw new Exception("no retreat for that location");
		}
		
		RetreatSituation rSit = bst.getRetreatForTerritory(location);
		
		if(rSit.retreating.belongsTo != p){
			throw new Exception("wrong player for retreat");
		}
		
		if(rSit.from != location){
			throw new Exception("wrong origin of retreat");
		}
	}
	public boolean canDisband(BoardState bst, Player p, TerritorySquare location){
		
		//	it must be retreat season
		if(bst.time.phase != Phase.AUT && bst.time.phase != Phase.SUM){
			return false;
		}
		
		//	there must be a pending retreat from this location
		if(bst.getRetreatForTerritory(location) == null){
			return false;
		}
		
		RetreatSituation rSit = bst.getRetreatForTerritory(location);
		
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
	
	
	public void assertCanRetreat(BoardState bst, Player p, TerritorySquare from, TerritorySquare to) throws Exception{
		assertCanRetreat(bst, p, from, to, "NA");
	}
	
	public boolean canRetreat(BoardState bst, Player p, TerritorySquare from, TerritorySquare to) throws Exception{
		return canRetreat(bst, p, from, to, "NA");
	}
	
	public boolean assertCanRetreat(BoardState bst, Player p, TerritorySquare from, TerritorySquare to, String destinationCoast) throws Exception{
		
		if(bst.time.phase != Phase.AUT && bst.time.phase != Phase.SUM){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		//	there must be a pending retreat from this location
		if(bst.getRetreatForTerritory(from) == null){
			throw new Exception("no retreat for that location");
		}
		
		RetreatSituation rSit = bst.getRetreatForTerritory(from);
		
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
		
		if(!bst.isValidRetreat(from, to)){
			throw new Exception("territory was contested on previous turn");
		}
		
		return true;
	}
	
	public boolean canRetreat(BoardState bst, Player p, TerritorySquare from, TerritorySquare to, String destinationCoast) throws Exception{
		
		if(bst.time.phase != Phase.AUT && bst.time.phase != Phase.SUM){
			return false;
		}
		
		//	there must be a pending retreat from this location
		if(bst.getRetreatForTerritory(from) == null){
			return false;
		}
		
		RetreatSituation rSit = bst.getRetreatForTerritory(from);
		
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
		
		if(!bst.isValidRetreat(from, to)){
			return false;
		}
		
		return true;
	}
	
	public void assertCanRemove(BoardState bst, Player p, TerritorySquare location) throws Exception{
		
		//	it must be winter
		if(bst.time.phase != Phase.WIN){
			throw new Exception("wrong season: "+bst.time.phase);
		}
		
		//	location must have a unit
		if(location.getOccupier(bst) == null){
			throw new Exception("no occupier");
		}
		
		if(location.getOccupier(bst).belongsTo != p){
			throw new Exception("wrong player");
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(location.getOccupier(bst).belongsTo.getNumberUnits(bst) < location.getOccupier(bst).belongsTo.getNumberSupplyCenters(bst)){
			throw new Exception("player does not have to remove units");
		}
	}
	public boolean canRemove(BoardState bst, Player p, TerritorySquare location){
		
		//	it must be winter
		if(bst.time.phase != Phase.WIN){
			return false;
		}
		
		//	location must have a unit
		if(location.getOccupier(bst) == null){
			return false;
		}
		
		if(location.getOccupier(bst).belongsTo != p){
			return false;
		}
		
		//	controller must have spare builds--fewer occupied territories than controlled territories
		if(location.getOccupier(bst).belongsTo.getNumberUnits(bst) < location.getOccupier(bst).belongsTo.getNumberSupplyCenters(bst)){
			return false;
		}
		
		return true;
	}

	
	//	methods which can help in move generation
	//	TODO a lot of these methods may not be the most efficent ways to produce these stats.  Maybe
	//	compute after a board update and store them...
	//	TODO for now ignore selfOnly
	public Set<TerritorySquare> getSupportableTerritories(BoardState boardState, Player p, TerritorySquare location, boolean selfOnly){
		
		if(boardState.getSupportable(location) != null){
			return boardState.getSupportable(location);
		}
		
		Set<TerritorySquare> potentialSupports = new HashSet<TerritorySquare>();
		
		for(TerritorySquare neighbor: location.getBorders()){
			
			if(canSupportHold(boardState, p, location, neighbor)){
				
				if(selfOnly){
					if(neighbor.getOccupier(boardState).belongsTo == p){
						potentialSupports.add(neighbor);
					}
				}else{
					potentialSupports.add(neighbor);
				}
				
			}
		}
		
		boardState.setSupportable(location, potentialSupports);
		
		return potentialSupports;
	}
	
	public Set<RetreatSituation> getRetreatsForPlayer(BoardState boardState, Player p){
		
		Set<RetreatSituation> retreats = new HashSet<RetreatSituation>();
		
		for(RetreatSituation rsit: boardState.getRetreats()){
			
			if(rsit.retreating.belongsTo == p){
				retreats.add(rsit);
			}
		}
		
		return retreats;
	}
	
	public class TerritoryCoast{
		
		public final TerritorySquare sqr;
		public final String coast;
		
		public TerritoryCoast(TerritorySquare sqr, String coast){
			
			this.sqr = sqr;
			this.coast = coast;
			
		}
	}
	
	//	map from the square to the coast
	public List<TerritoryCoast> getRetreatsForUnit(BoardState boardState, RetreatSituation rsit) throws Exception{
		
		if(boardState.getRetreatsForUnit(rsit) != null){
			return boardState.getRetreatsForUnit(rsit);
		}
		
		List<TerritoryCoast> options = new LinkedList<TerritoryCoast>();
		TerritorySquare from = rsit.from;

		for(TerritorySquare tsquare: from.getBorders()){
			
			for(String s: tsquare.getCoasts()){
				if(canRetreat(boardState, rsit.retreating.belongsTo, from, tsquare, s)){
					options.add(new TerritoryCoast(tsquare, s));
				}
			}
		}
		
		boardState.setRetreatsForUnit(rsit, options);
		
		return options;
	}
	
	public Map<TerritorySquare, List<OrderValue>> getMovesForUnits(BoardState dynamicState) throws Exception{
		
		if(dynamicState.getMovesForUnits() != null){
			return dynamicState.getMovesForUnits();
		}
		
		Map<TerritorySquare, List<OrderValue>> orderMap = new HashMap<TerritorySquare, List<OrderValue>>();
		
		for(Player p: getPlayers()){
			for(TerritorySquare sqr: dynamicState.getOccupiedTerritories(p)){
				
				List<TerritoryCoast> possibleMoves = getMovesForUnit(dynamicState, sqr);
				List<OrderValue> orders = new LinkedList<OrderValue>();

				for(TerritoryCoast tcoast: possibleMoves){

					Move move = new Move(dynamicState, p, sqr, tcoast.sqr, tcoast.coast);
					orders.add(new OrderValue(move, 1));			
				
				}

				orderMap.put(sqr, orders);
				
			}
		}
		
		if(Bot.DEBUG){
			System.out.println("All moves for units: ");
			for(TerritorySquare sqr: orderMap.keySet()){
				System.out.println("\t"+sqr.getName());
				for(OrderValue ov: orderMap.get(sqr)){
					System.out.println("\t"+ov.score+"\t"+ov.order.toOrder(dynamicState));
				}
			}
		}
		
		dynamicState.setMoveForUnits(orderMap);
		
		return orderMap;
	}
	
	private List<TerritoryCoast> getMovesForUnit(BoardState boardState, TerritorySquare from){
		
		Unit occupier = from.getOccupier(boardState);
		
		List<TerritoryCoast> options = new LinkedList<TerritoryCoast>();

		//TODO cache a list of borders for a unit and for a fleet for each coast in the territory square
		for(TerritorySquare tsquare: from.getBorders()){
			
			if(!occupier.army){
				if(tsquare.hasMultipleCoasts()){
					for(String s: tsquare.getCoasts()){
						if(s.equals("NA")) continue;
						
						if(canMove(boardState, occupier.belongsTo, from, tsquare, s)){
							options.add(new TerritoryCoast(tsquare, s));
						}
					}
				}else{
					
					// only 1
					for(String s: tsquare.getCoasts()){
						if(canMove(boardState, occupier.belongsTo, from, tsquare, s)){
							options.add(new TerritoryCoast(tsquare, s));
						}
					}
				}
			}else{
				
				// only 1
				if(canMove(boardState, occupier.belongsTo, from, tsquare, "NA")){
					options.add(new TerritoryCoast(tsquare, "NA"));
				}
			}
		}
		
		return options;
		
	}
	
	public int getRequiredBuilds(BoardState boardState, Player p){
		
		int control = p.getControlledTerritories(boardState).size();
		int occupy = p.getOccupiedTerritories(boardState).size();
		
		return control-occupy;
	}
	
	public Collection<TerritorySquare> getPossibleBuilds(BoardState boardState, Player p){
		
		List<TerritorySquare> possibilities = new LinkedList<TerritorySquare>();
		
		for(TerritorySquare tsquare: p.getHomeCenters()){
			if(canBuild(boardState, p, tsquare)){
				possibilities.add(tsquare);
			}
		}
		
		return possibilities;
	}
	
	public static class YearPhase{
		
		public final int year;
		public final Phase phase;
		
		public YearPhase(int year, Phase phase){
			
			this.year = year;
			this.phase = phase;
			
		}
		
		private static int phaseNum(Phase p){
			if(p == Phase.SPR) return 0;
			else if(p == Phase.SUM) return 1;
			else if(p == Phase.FAL) return 2;
			else if(p == Phase.AUT) return 3;
			else if(p == Phase.WIN) return 4;
			
			return -1;
		}
		
		public int movesUntil(YearPhase until){
			
			int thisPhase = phaseNum(phase);
			int thatPhase = phaseNum(until.phase);
			
			return (5*(until.year-year))+(thatPhase-thisPhase)+1;
		}
		
		public boolean isAfter(YearPhase yp)
		{
			if(this.year > yp.year)
			{
				return true;
			}
			else if(this.year == yp.year && this.phase.compareTo(yp.phase) > 0)
			{
				return true;
			}
			else
				return false;
		}
		
		public YearPhase next(){
			
			if(phase == Phase.WIN){
				return new YearPhase(year+1, Phase.SPR);
			}
			else if(phase == Phase.SPR){
				return new YearPhase(year, Phase.SUM);
			}
			else if(phase == Phase.SUM){
				return new YearPhase(year, Phase.FAL);
			}
			else if(phase == Phase.FAL){
				return new YearPhase(year, Phase.AUT);
			}
			else {
				return new YearPhase(year, Phase.WIN);
			}
		}
		
		public String toString(){
			return year+" "+phase.toString();
		}
	}
	
	//	generate whatever makes sense for the season.  Waive, retreats, holds.
	//	so game search can get an a priori estimate of move quality.
	//	can't completely punt for disbands, so choose randomly
	public Set<Order> generateDefaultOrdersFor(BoardState bst, Player p) throws Exception{
		
		if(bst.getDefaultOrders(p) != null){
			return bst.getDefaultOrders(p);
		}
		
		Set<Order> toReturn = null;
		if(bst.time.phase == Phase.SPR || bst.time.phase == Phase.FAL){
			toReturn = generateHoldsFor(bst, p);
		}else if(bst.time.phase == Phase.SUM || bst.time.phase == Phase.AUT){
			toReturn = generateDisbandsFor(bst, p);
		}else{
			toReturn = generateBuildsFor(bst, p);
		}
		
		bst.setDefaultOrders(p, toReturn);
		
		return toReturn;
	}
	
	private Set<Order> generateBuildsFor(BoardState bst, Player p) throws Exception{
		
		Set<Order> orders = new HashSet<Order>();
		
		int required =  this.getRequiredBuilds(bst, p);
		
		TerritorySquare[] allUnits = bst.getOccupiedTerritories(p).toArray(new TerritorySquare[0]);
		int destroyedIndex = 0;
		
		while(required != 0){
			
			if(required > 0){
				orders.add(new Waive(bst, p));
				required--;
				
			}else if(required < 0){
				orders.add(new Remove(bst, p, allUnits[destroyedIndex++]));
				required++;
			}
		}
		
		return orders;
	}
	
	private Set<Order> generateHoldsFor(BoardState bst, Player p) throws Exception{
		
		Set<Order> holds = new HashSet<Order>();
		for(TerritorySquare tsquare: p.getOccupiedTerritories(bst)){
			holds.add(new Hold(bst, p, tsquare));
		}
		
		return holds;
	}
	
	private Set<Order> generateDisbandsFor(BoardState bst, Player p) throws Exception{
		
		Set<Order> disbands = new HashSet<Order>();
		
		for(RetreatSituation ret: getRetreatsForPlayer(bst, p)){
			disbands.add(new Disband(bst, p, ret.from, ret.originCoast));
		}
		
		return disbands;
	}
	
	public static void main(String[] args) throws Exception{
		
		FileWriter fwriter = new FileWriter("map.gviz");
		fwriter.write(new BoardConfiguration().mapAsDotFile());
		fwriter.close();
		
		BoardConfiguration g = new BoardConfiguration();
		System.out.println(g.toString());
	}	
}

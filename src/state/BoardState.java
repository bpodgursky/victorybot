package state;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	//constant
	
	final Map<String, TerritorySquare> terrs = new HashMap<String, TerritorySquare>();
	final Set<Player> activePlayers = new HashSet<Player>();
	
	public BoardState() throws Exception{
		initialize();
	}
	
	//set the controller of a territory
	//once controlled, control is only lost by someone else taking control.  So don't
	//need a remove method
	private void setControl(Player p, TerritorySquare terr) throws Exception{
		
		if(!terr.isSupplyCenter()){
			throw new Exception("Territory not supply center");
		}
		
		if(terr.isControlled()){
			terr.getController().removeSupply(terr);
		}
		
		p.addSupply(terr);
		terr.setController(p);
	}
	
	private void setOccupier(Unit u, TerritorySquare terr, String coast) throws Exception{
		
		if(!terr.hasCoast(coast)){
			throw new Exception("Invalid coast!");
		}
		
		if(u.army && !terr.isLand()){
			throw new Exception("Invalid occupier");
		}
		
		u.belongsTo.addOccupy(terr);
		terr.setOccupier(u, coast);
	}
	
	private void setOccupier(Unit u, TerritorySquare terr) throws Exception{
		
		if(!u.army && terr.hasMultipleCoasts()){
			throw new Exception("Must specify a coast!");
		}
		
		setOccupier(u, terr, "NA");
	}
	
	private Unit removeOccupier(TerritorySquare terr) throws Exception{
		
		Unit removed = terr.getOccupier();
		
		if(removed == null){
			throw new Exception("Territory not occupied!");
		}
		
		removed.belongsTo.removeOccupy(terr);
		
		terr.setOccupier(null);
		
		return removed;
	}
	
	
	private void initialize() throws Exception{
		
		Player eng = new Player(Country.ENG);
		Player fra = new Player(Country.FRA);
		Player ger = new Player(Country.GER);
		Player rus = new Player(Country.RUS);
		Player ita = new Player(Country.ITA);
		Player aus = new Player(Country.AUS);
		Player tur = new Player(Country.TUR);
		
		activePlayers.add(eng);
		activePlayers.add(fra);
		activePlayers.add(ger);
		activePlayers.add(rus);
		activePlayers.add(ita);
		activePlayers.add(aus);
		activePlayers.add(tur);
		
		//england
		terrs.put("EDI", new TerritorySquare("EDI", SUPPLY, LAND, eng));
		terrs.put("LVP", new TerritorySquare("LVP", SUPPLY, LAND, eng));
		terrs.put("LON", new TerritorySquare("LON", SUPPLY, LAND, eng));
		
		eng.setHomeSupply(Arrays.asList(get("EDI"), get("LVP"), get("LON")));
		
		//russia
		terrs.put("STP", new TerritorySquare("STP", SUPPLY, LAND, rus, Arrays.asList("NC", "SC")));
		terrs.put("MOS", new TerritorySquare("MOS", SUPPLY, LAND, rus));
		terrs.put("WAR", new TerritorySquare("WAR", SUPPLY, LAND, rus));
		terrs.put("SEV", new TerritorySquare("SEV", SUPPLY, LAND, rus));
		
		rus.setHomeSupply(Arrays.asList(get("STP"), get("MOS"), get("WAR"), get("SEV")));

		
		//france
		terrs.put("BRE", new TerritorySquare("BRE", SUPPLY, LAND, fra));
		terrs.put("PAR", new TerritorySquare("PAR", SUPPLY, LAND, fra));
		terrs.put("MAR", new TerritorySquare("MAR", SUPPLY, LAND, fra));

		
		//germany
		terrs.put("KIE", new TerritorySquare("KIE", SUPPLY, LAND, ger));
		terrs.put("BER", new TerritorySquare("BER", SUPPLY, LAND, ger));
		terrs.put("MUN", new TerritorySquare("MUN", SUPPLY, LAND, ger));

		
		//italy
		terrs.put("ROM", new TerritorySquare("ROM", SUPPLY, LAND, ita));
		terrs.put("NAP", new TerritorySquare("NAP", SUPPLY, LAND, ita));
		terrs.put("VEN", new TerritorySquare("VEN", SUPPLY, LAND, ita));

		
		//austria
		terrs.put("VIE", new TerritorySquare("VIE", SUPPLY, LAND, aus));
		terrs.put("TRI", new TerritorySquare("TRI", SUPPLY, LAND, aus));
		terrs.put("BUD", new TerritorySquare("BUD", SUPPLY, LAND, aus));

		//turkey
		terrs.put("CON", new TerritorySquare("CON", SUPPLY, LAND, tur));
		terrs.put("ANK", new TerritorySquare("ANK", SUPPLY, LAND, tur));
		terrs.put("SMY", new TerritorySquare("SMY", SUPPLY, LAND, tur));

		
		//other supply centers
		terrs.put("NWY", new TerritorySquare("NWY", SUPPLY, LAND, null));
		terrs.put("SWE", new TerritorySquare("SWE", SUPPLY, LAND, null));
		terrs.put("DEN", new TerritorySquare("DEN", SUPPLY, LAND, null));
		terrs.put("HOL", new TerritorySquare("HOL", SUPPLY, LAND, null));
		terrs.put("BEL", new TerritorySquare("BEL", SUPPLY, LAND, null));
		terrs.put("SPA", new TerritorySquare("SPA", SUPPLY, LAND, null, Arrays.asList("NC", "SC")));
		terrs.put("POR", new TerritorySquare("POR", SUPPLY, LAND, null));
		terrs.put("TUN", new TerritorySquare("TUN", SUPPLY, LAND, null));
		terrs.put("SER", new TerritorySquare("SER", SUPPLY, LAND, null));
		terrs.put("RUM", new TerritorySquare("RUM", SUPPLY, LAND, null));
		terrs.put("BUL", new TerritorySquare("BUL", SUPPLY, LAND, null, Arrays.asList("EC", "WC")));
		terrs.put("GRE", new TerritorySquare("GRE", SUPPLY, LAND, null));
			
		//other non supply center land
		terrs.put("FIN", new TerritorySquare("FIN",	NO_SUPPLY, LAND, null));
		terrs.put("LVN", new TerritorySquare("LVN",	NO_SUPPLY, LAND, null));		
		terrs.put("PRU", new TerritorySquare("PRU",	NO_SUPPLY, LAND, null));
		terrs.put("SIL", new TerritorySquare("SIL",	NO_SUPPLY, LAND, null));
		terrs.put("GAL", new TerritorySquare("GAL",	NO_SUPPLY, LAND, null));		
		terrs.put("UKR", new TerritorySquare("UKR",	NO_SUPPLY, LAND, null));
		terrs.put("BOH", new TerritorySquare("BOH",	NO_SUPPLY, LAND, null));
		terrs.put("RUH", new TerritorySquare("RUH",	NO_SUPPLY, LAND, null));
		terrs.put("BUR", new TerritorySquare("BUR",	NO_SUPPLY, LAND, null));
		terrs.put("GAS", new TerritorySquare("GAS",	NO_SUPPLY, LAND, null));
		terrs.put("NAF", new TerritorySquare("NAF",	NO_SUPPLY, LAND, null));
		terrs.put("PIE", new TerritorySquare("PIE",	NO_SUPPLY, LAND, null));
		terrs.put("TUS", new TerritorySquare("TUS",	NO_SUPPLY, LAND, null));
		terrs.put("APU", new TerritorySquare("APU",	NO_SUPPLY, LAND, null));
		terrs.put("ALB", new TerritorySquare("ALB",	NO_SUPPLY, LAND, null));
		terrs.put("ARM", new TerritorySquare("ARM",	NO_SUPPLY, LAND, null));
		terrs.put("SYR", new TerritorySquare("SYR",	NO_SUPPLY, LAND, null));
		terrs.put("CLY", new TerritorySquare("CLY",	NO_SUPPLY, LAND, null));
		terrs.put("YOR", new TerritorySquare("YOR",	NO_SUPPLY, LAND, null));
		terrs.put("WAL", new TerritorySquare("WAL",	NO_SUPPLY, LAND, null));
		terrs.put("PIC", new TerritorySquare("PIC", NO_SUPPLY, LAND, null));
		terrs.put("TRL", new TerritorySquare("TRL", NO_SUPPLY, LAND, null));
				
		//sea territories
		terrs.put("NAO", new TerritorySquare("NAO",	NO_SUPPLY, SEA, null));
		terrs.put("NRG", new TerritorySquare("NRG",	NO_SUPPLY, SEA, null));
		terrs.put("NTH", new TerritorySquare("NTH",	NO_SUPPLY, SEA, null));
		terrs.put("BAR", new TerritorySquare("BAR",	NO_SUPPLY, SEA, null));
		terrs.put("BAL", new TerritorySquare("BAL",	NO_SUPPLY, SEA, null));
		terrs.put("BOT", new TerritorySquare("BOT",	NO_SUPPLY, SEA, null));
		terrs.put("IRI", new TerritorySquare("IRI",	NO_SUPPLY, SEA, null));
		terrs.put("SKA", new TerritorySquare("SKA",	NO_SUPPLY, SEA, null));
		terrs.put("HEL", new TerritorySquare("HEL",	NO_SUPPLY, SEA, null));
		terrs.put("ENG", new TerritorySquare("ENG",	NO_SUPPLY, SEA, null));
		terrs.put("MID", new TerritorySquare("MID",	NO_SUPPLY, SEA, null));
		terrs.put("WES", new TerritorySquare("WES",	NO_SUPPLY, SEA, null));
		terrs.put("LYO", new TerritorySquare("LYO",	NO_SUPPLY, SEA, null));
		terrs.put("TYN", new TerritorySquare("TYN",	NO_SUPPLY, SEA, null));
		terrs.put("ION", new TerritorySquare("ION",	NO_SUPPLY, SEA, null));
		terrs.put("ADR", new TerritorySquare("ADR",	NO_SUPPLY, SEA, null));
		terrs.put("AEG", new TerritorySquare("AEG",	NO_SUPPLY, SEA, null));
		terrs.put("EAS", new TerritorySquare("EAS",	NO_SUPPLY, SEA, null));
		terrs.put("BLA", new TerritorySquare("BLA",	NO_SUPPLY, SEA, null));

		//ocean adjacencies
		
		border("NAO", "NRG", true);
		border("NAO", "CLY", true);
		border("NAO", "IRI", true);
		border("NAO", "MID", true);
		
		border("NRG", "BAR", true);
		border("NRG", "NWY", true);
		border("NRG", "NTH", true);
		border("NRG", "EDI", true);
		
		border("BAR", "STP", "NA", "NC");
		border("BAR", "NWY", true);
		
		border("MID", "IRI", true);
		border("MID", "ENG", true);
		border("MID", "BRE", true);
		border("MID", "GAS", true);
		border("MID", "SPA", "NA", "NC");
		border("MID", "POR", true);
		
		border("IRI", "CLY", true);
		border("IRI", "LVP", true);
		border("IRI", "WAL", true);
		border("IRI", "ENG", true);
		
		border("NTH", "SKA", true);
		border("NTH", "NWY", true);
		border("NTH", "DEN", true);
		border("NTH", "HOL", true);
		border("NTH", "BEL", true);
		border("NTH", "LON", true);
		border("NTH", "YOR", true);
		border("NTH", "EDI", true);
		border("NTH", "HEL", true);
		border("NTH", "ENG", true);
		
		border("SKA", "NWY", true);
		border("SKA", "SWE", true);
		border("SKA", "DEN", true);
		
		border("BAL", "SWE", true);
		border("BAL", "BOT", true);
		border("BAL", "LVN", true);
		border("BAL", "PRU", true);
		border("BAL", "BER", true);
		border("BAL", "KIE", true);
		border("BAL", "DEN", true);
		
		border("BOT", "SWE", true);
		border("BOT", "FIN", true);
		border("BOT", "STP", "NA", "SC");
		border("BOT", "LVN", true);
		
		border("ENG", "WAL", true);
		border("ENG", "LON", true);
		border("ENG", "BEL", true);
		border("ENG", "PIC", true);
		border("ENG", "BRE", true);
		
		border("WES", "SPA", "NA", "SC");
		border("WES", "LYO", true);
		border("WES", "TYN", true);
		border("WES", "TUN", true);
		border("WES", "NAF", true);
		
		border("LYO", "SPA", "NA", "SC");
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
		border("AEG", "BUL", "NA", "WC");
		border("AEG", "CON", true);
		border("AEG", "SMY", true);
		border("AEG", "EAS", true);
		border("AEG", "BLA", true);
		
		border("EAS", "SMY", true);
		border("EAS", "SYR", true);
		
		border("BLA", "BUL", "NA", "EC");
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
		
		border("NWY", "STP", "NA", "NC");
		border("NWY", "FIN", false);
		border("NWY", "SWE", true);
		
		border("SWE", "FIN", true);
		border("SWE", "DEN", true);
		
		border("FIN", "STP", "NA", "SC");
		
		border("DEN", "KIE", true);
		
		//Russia
		
		border("STP", "LVN", "SC", "NA");
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
		
		border("GAS", "SPA", "NA", "NC");
		border("GAS", "MAR", false);
		border("GAS", "BUR", false);
		
		border("PAR", "BUR", false);
		
		border("MAR", "PIE", true);
		border("MAR", "BUR", false);
		border("MAR", "SPA", "NA", "SC");
		
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
		
		border("CON", "BUL", "NA", "WC");
		border("CON", "BUL", "NA", "EC");		
		border("CON", "ANK", true);
		border("CON", "SMY", true);
		
		border("ANK", "ARM", true);
		border("ANK", "SMY", false);
		
		border("SMY", "ARM", false);
		border("SMY", "SYR", true);
		
		border("SYR", "ARM", false);
		
		//balkans
		
		border("RUM", "SER", false);
		border("RUM", "BUL", "NA", "EC");
		
		border("BUL", "SER", false);
		border("BUL", "GRE", "WC", "NA");
		
		border("SER", "GRE", false);
		border("GRE", "ALB", true);
		
		//spain and africa

		border("SPA", "POR", "NC", "NA");
		border("SPA", "POR", "SC", "NA");
		
		border("NAF", "TUN", true);
		
		
		//set up units and control 
		
		setControl(eng, get("EDI"));
		setControl(eng, get("LVP"));
		setControl(eng, get("LON"));
		
		setOccupier(new Unit(eng, FLEET), get("EDI"));
		setOccupier(new Unit(eng, ARMY), get("LVP"));
		setOccupier(new Unit(eng, FLEET), get("LON"));
		
		setControl(rus, get("STP"));
		setControl(rus, get("MOS"));
		setControl(rus, get("WAR"));
		setControl(rus, get("SEV"));
		
		setOccupier(new Unit(rus, FLEET), get("STP"), "SC");
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
	
	public void placeUnit(Unit u, TerritorySquare location) throws Exception{
		location.setOccupier(u);
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
	
	private TerritorySquare get(String name){
		return terrs.get(name);
	}
	
	public void update(String moves){
		System.out.println(moves);
	}
	
	public String toString(){
		
		String str = "Game State: ";
		
		str+="\tPlayers:\n";
		
		for(Player p: this.activePlayers){
			str+="\t"+p.toString()+"\n";
		}
		
		return str;
	}
	
	//utility methods for game mechanics
	
	public static boolean canMove(TerritorySquare from, TerritorySquare to){
		return canMove(from, to, "NA");
	}
	
	public static boolean canMove(TerritorySquare from, TerritorySquare to, String destinationCoast){
		//make sure unit is there
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
	
	public static boolean canSupportHold(TerritorySquare from, TerritorySquare to){
		
		if(to.getOccupier() == null){
			return false;
		}
		
		return canMove(from, to);	
	}
	
	public static boolean canSupportMove(TerritorySquare supporter, TerritorySquare from, TerritorySquare to){
		
		if(supporter.getOccupier() == null){
			return false;
		}
		
		if(from.getOccupier() == null){
			return false;
		}
		
		if(!canMove(supporter, to)){
			return false;
		}
		
		if(!canMove(to, from)){
			return false;
		}
		
		return true;
	}
	
	public static boolean canConvoy(TerritorySquare convoyer, TerritorySquare from, TerritorySquare to){
		
		if(convoyer.getOccupier() == null || from.getOccupier() == null){
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
	
	public static boolean canHold(TerritorySquare holder){
		
		if(holder.getOccupier() == null){
			return false;
		}
		
		//	as long as there's a unit there it can hold...
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

import java.util.HashMap;
import java.util.Map;


public class GameState {
	Map<String, TerritorySquare> terrs = new HashMap<String, TerritorySquare>();
	
	{
		//england
		terrs.put("EDI", new TerritorySquare("EDI", true, true, Player.ENG, Player.ENG));
		terrs.put("LVP", new TerritorySquare("LVP", true, true, Player.ENG, Player.ENG));
		terrs.put("LON", new TerritorySquare("LON", true, true, Player.ENG, Player.ENG));
		
		//russia
		terrs.put("STP", new TerritorySquare("STP", true, true, Player.RUS, Player.RUS));
		terrs.put("MOS", new TerritorySquare("MOS", true, true, Player.RUS, Player.RUS));
		terrs.put("WAR", new TerritorySquare("WAR", true, true, Player.RUS, Player.RUS));
		terrs.put("SEV", new TerritorySquare("SEV", true, true, Player.RUS, Player.RUS));
		
		//france
		terrs.put("BRE", new TerritorySquare("BRE", true, true, Player.FRA, Player.FRA));
		terrs.put("PAR", new TerritorySquare("PAR", true, true, Player.FRA, Player.FRA));
		terrs.put("MAR", new TerritorySquare("MAR", true, true, Player.FRA, Player.FRA));
		
		//germany
		terrs.put("KIE", new TerritorySquare("KIE", true, true, Player.GER, Player.GER));
		terrs.put("BER", new TerritorySquare("BER", true, true, Player.GER, Player.GER));
		terrs.put("MUN", new TerritorySquare("MUN", true, true, Player.GER, Player.GER));
		
		//italy
		terrs.put("ROM", new TerritorySquare("ROM", true, true, Player.ITA, Player.ITA));
		terrs.put("NAP", new TerritorySquare("NAP", true, true, Player.ITA, Player.ITA));
		terrs.put("VEN", new TerritorySquare("VEN", true, true, Player.ITA, Player.ITA));
		
		//austria
		terrs.put("VIE", new TerritorySquare("VIE", true, true, Player.AUS, Player.AUS));
		terrs.put("TRI", new TerritorySquare("TRI", true, true, Player.AUS, Player.AUS));
		terrs.put("BUD", new TerritorySquare("BUD", true, true, Player.AUS, Player.AUS));
		
		//turkey
		terrs.put("CON", new TerritorySquare("CON", true, true, Player.TUR, Player.TUR));
		terrs.put("ANK", new TerritorySquare("ANK", true, true, Player.TUR, Player.TUR));
		terrs.put("SMY", new TerritorySquare("SMY", true, true, Player.TUR, Player.TUR));
		
		
		//other supply centers
		terrs.put("NWY", new TerritorySquare("NWY", true, true, null, null));
		terrs.put("SWE", new TerritorySquare("SWE", true, true, null, null));
		terrs.put("DEN", new TerritorySquare("DEN", true, true, null, null));
		terrs.put("HOL", new TerritorySquare("HOL", true, true, null, null));
		terrs.put("BEL", new TerritorySquare("BEL", true, true, null, null));
		terrs.put("SPA", new TerritorySquare("SPA", true, true, null, null));
		terrs.put("POR", new TerritorySquare("POR", true, true, null, null));
		terrs.put("TUN", new TerritorySquare("TUN", true, true, null, null));
		terrs.put("SER", new TerritorySquare("SER", true, true, null, null));
		terrs.put("RUM", new TerritorySquare("RUM", true, true, null, null));
		terrs.put("BUL", new TerritorySquare("BUL", true, true, null, null));
		terrs.put("GRE", new TerritorySquare("GRE", true, true, null, null));
			
		//other non supply center land
		terrs.put("FIN", new TerritorySquare("FIN",	false, true, null, null));
		terrs.put("LVN", new TerritorySquare("LVN",	false, true, null, null));		
		terrs.put("PRU", new TerritorySquare("PRU",	false, true, null, null));
		terrs.put("SIL", new TerritorySquare("SIL",	false, true, null, null));
		terrs.put("GAL", new TerritorySquare("GAL",	false, true, null, null));		
		terrs.put("UKR", new TerritorySquare("UKR",	false, true, null, null));
		terrs.put("BOH", new TerritorySquare("BOH",	false, true, null, null));
		terrs.put("RUH", new TerritorySquare("RUH",	false, true, null, null));
		terrs.put("BUR", new TerritorySquare("BUR",	false, true, null, null));
		terrs.put("GAS", new TerritorySquare("GAS",	false, true, null, null));
		terrs.put("NAF", new TerritorySquare("NAF",	false, true, null, null));
		terrs.put("PIE", new TerritorySquare("PIE",	false, true, null, null));
		terrs.put("TUS", new TerritorySquare("TUS",	false, true, null, null));
		terrs.put("APU", new TerritorySquare("APU",	false, true, null, null));
		terrs.put("ALB", new TerritorySquare("ALB",	false, true, null, null));
		terrs.put("ARM", new TerritorySquare("ARM",	false, true, null, null));
		terrs.put("SYR", new TerritorySquare("SYR",	false, true, null, null));
		terrs.put("CLY", new TerritorySquare("CLY",	false, true, null, null));
		terrs.put("YOR", new TerritorySquare("YOR",	false, true, null, null));
		terrs.put("WAL", new TerritorySquare("WAL",	false, true, null, null));

		
		//sea territories
		terrs.put("NAO", new TerritorySquare("NAO",	false, false, null, null));
		terrs.put("NRG", new TerritorySquare("NRG",	false, false, null, null));
		terrs.put("NTH", new TerritorySquare("NTH",	false, false, null, null));
		terrs.put("BAR", new TerritorySquare("BAR",	false, false, null, null));
		terrs.put("BAL", new TerritorySquare("BAL",	false, false, null, null));
		terrs.put("BOT", new TerritorySquare("BOT",	false, false, null, null));
		terrs.put("IRI", new TerritorySquare("IRI",	false, false, null, null));
		terrs.put("SKA", new TerritorySquare("SKA",	false, false, null, null));
		terrs.put("HEL", new TerritorySquare("HEL",	false, false, null, null));
		terrs.put("ENG", new TerritorySquare("ENG",	false, false, null, null));
		terrs.put("MID", new TerritorySquare("MID",	false, false, null, null));
		terrs.put("WES", new TerritorySquare("WES",	false, false, null, null));
		terrs.put("LYO", new TerritorySquare("LYO",	false, false, null, null));
		terrs.put("TYN", new TerritorySquare("TYN",	false, false, null, null));
		terrs.put("ION", new TerritorySquare("ION",	false, false, null, null));
		terrs.put("ADR", new TerritorySquare("ADR",	false, false, null, null));
		terrs.put("AEG", new TerritorySquare("AEG",	false, false, null, null));
		terrs.put("EAS", new TerritorySquare("EAS",	false, false, null, null));
		terrs.put("BLA", new TerritorySquare("BLA",	false, false, null, null));

		//ocean adjacencies
		
		border("NAO", "NRG", true);
		border("NAO", "CLY", true);
		border("NAO", "IRI", true);
		border("NAO", "MID", true);
		
		border("NRG", "BAR", true);
		border("NRG", "NWY", true);
		border("NRG", "NTH", true);
		border("NRG", "EDI", true);
		
		border("BAR", "STP", true);
		border("BAR", "NWY", true);
		
		border("MID", "IRI", true);
		border("MID", "ENG", true);
		border("MID", "BRE", true);
		border("MID", "GAS", true);
		border("MID", "SPA", true);
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
		border("BOT", "STP", true);
		border("BOT", "LVN", true);
		
		border("ENG", "WAL", true);
		border("ENG", "LON", true);
		border("ENG", "BEL", true);
		border("ENG", "PIC", true);
		border("ENG", "BRE", true);
		
		border("WES", "SPA", true);
		border("WES", "LYO", true);
		border("WES", "TYN", true);
		border("WES", "TUN", true);
		border("WES", "NAF", true);
		
		border("LYO", "SPA", true);
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
		border("AEG", "BUL", true);
		border("AEG", "CON", true);
		border("AEG", "SMY", true);
		border("AEG", "EAS", true);
		border("AEG", "BLA", true);
		
		border("EAS", "SMY", true);
		border("EAS", "SYR", true);
		
		border("BLA", "BUL", true);
		border("BLA", "RUM", true);
		border("BLA", "SEV", true);
		border("BLA", "ANK", true);
		border("BLA", "CON", true);
		
		//england
		
		border("CLY", "EDI");
		border("CLY", "LVP");
		
		border("EDI", "YOR");
		border("EDI", "LVP");
		
		border("LVP", "YOR");
		border("LVP", "WAL");
		border("YOR", "LON");
		border("YOR", "WAL");
		
		border("WAL", "LON");
		
		//other
		
		border("NOR", "STP");
		border("NOR", "FIN");
		
		
	}
	
	private void border(String t1, String t2, boolean shareCoast){
		TerritorySquare sq1 = get(t1);
		TerritorySquare sq2 = get(t2);
		
		sq1.borders(sq2);
		sq2.borders(sq1);
	}
	
	//Holds the current location of all units
	
	private TerritorySquare get(String name){
		return terrs.get(name);
	}
	
	void update(String moves){
		
	}
	
}

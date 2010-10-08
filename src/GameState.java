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
		
		border("NAO", "NRG");
		border("NAO", "CLY");
		border("NAO", "IRI");
		border("NAO", "MID");
		
		border("NRG", "BAR");
		border("NRG", "NWY");
		border("NRG", "NTH");
		border("NRG", "EDI");
		
		border("BAR", "STP");
		border("BAR", "NWY");
		
		border("MID", "IRI");
		border("MID", "ENG");
		border("MID", "BRE");
		border("MID", "GAS");
		border("MID", "SPA");
		border("MID", "POR");
		
		border("IRI", "CLY");
		border("IRI", "LVP");
		border("IRI", "WAL");
		border("IRI", "ENG");
		
		border("NTH", "SKA");
		border("NTH", "NWY");
		border("NTH", "DEN");
		border("NTH", "HOL");
		border("NTH", "BEL");
		border("NTH", "LON");
		border("NTH", "YOR");
		border("NTH", "EDI");
		border("NTH", "HEL");
		border("NTH", "ENG");
		
		border("SKA", "NWY");
		border("SKA", "SWE");
		border("SKA", "DEN");
		
		border("BAL", "SWE");
		border("BAL", "BOT");
		border("BAL", "LVN");
		border("BAL", "PRU");
		border("BAL", "BER");
		border("BAL", "KIE");
		border("BAL", "DEN");
		
		border("BOT", "SWE");
		border("BOT", "FIN");
		border("BOT", "STP");
		border("BOT", "LVN");
		
		border("ENG", "WAL");
		border("ENG", "LON");
		border("ENG", "BEL");
		border("ENG", "PIC");
		border("ENG", "BRE");
		
		border("MID", "BRE");
		border("MID", "GAS");
		border("MID", "SPA");
		border("MID", "POR");
		border("MID", "NAF");
		
		border("WES", "SPA");
		border("WES", "LYO");
		border("WES", "TYN");
		border("WES", "TUN");
		border("WES", "NAF");
		
		border("LYO", "SPA");
		border("LYO", "MAR");
		border("LYO", "PIE");
		border("LYO", "TUS");
		border("LYO", "TYN");
		
		border("TYN", "TUS");
		border("TYN", "ROM");
		border("TYN", "NAP");
		border("TYN", "ION");
		border("TYN", "TUN");
		
		border("ION", "NAP");
		border("ION", "ADR");
		border("ION", "ALB");
		border("ION", "GRE");
		border("ION", "AEG");
		border("ION", "EAS");
		border("ION", "TUN");
		
		border("ADR", "APU");
		border("ADR", "VEN");
		border("ADR", "TRI");
		border("ADR", "ALB");
		
		border("AEG", "GRE");
		border("AEG", "BUL");
		border("AEG", "CON");
		border("AEG", "SMY");
		border("AEG", "EAS");
		border("AEG", "BLA");
		
		border("EAS", "SMY");
		border("EAS", "SYR");
		
		border("BLA", "BUL");
		border("BLA", "RUM");
		border("BLA", "SEV");
		border("BLA", "ANK");
		border("BLA", "CON");
		
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

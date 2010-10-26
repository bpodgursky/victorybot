package order;

import java.util.Arrays;

import representation.Country;
import representation.Player;
import representation.Unit;
import state.BoardState;
import state.BoardState.Phase;

public class TestOrders {
	
	public void testPrint() throws Exception{
	
		BoardState bst = new BoardState();
		
		Player rus = bst.getPlayer(Country.RUS);
		
		bst.removeOccupier(bst.get("STP"));

		Unit nwy = new Unit(rus, true);
		Unit nrg = new Unit(rus, false);
		
		bst.setOccupier(nwy, bst.get("NWY"));
		bst.setOccupier(nrg, bst.get("BAR"));
		
		Convoy cnv = new Convoy(rus, bst.get("BAR"), bst.get("NWY"), bst.get("STP"));
		Move mov = new Move(rus, bst.get("BAR"), bst.get("NRG"));
		MoveByConvoy mbc = new MoveByConvoy(rus, bst.get("NWY"), bst.get("STP"), Arrays.asList(bst.get("BAR")));
		SupportHold hld = new SupportHold(rus, bst.get("BAR"), bst.get("NWY"));
		SupportMove smv = new SupportMove(rus, bst.get("BAR"), bst.get("NWY"), bst.get("STP"));
		Move mov2 = new Move(rus, bst.get("BAR"), bst.get("STP"), "NCS");
		
		System.out.println(cnv.toOrder());
		System.out.println(mov.toOrder());
		System.out.println(mbc.toOrder());
		System.out.println(hld.toOrder());
		System.out.println(smv.toOrder());
		System.out.println(mov2.toOrder());
		
		//	change seasons
		bst.setTime("WIN", 1901);
		
		//	test removing units 
		Remove rem = new Remove(rus, bst.get("BAR"));
	
		System.out.println(rem.toOrder());
		
		bst.removeOccupier(bst.get("NWY"));
		bst.removeOccupier(bst.get("BAR"));
		
		//	test building or not building new ones
		Build bld = new Build(rus, new Unit(rus, false), bst.get("STP"), "NC");
		Waive wve = new Waive(rus);

		System.out.println(bld.toOrder());
		System.out.println(wve.toOrder());
		
		//	change to retreat season
		bst.setTime("AUT", 1902);
		bst.setRetreatingUnit(new Unit(rus, false), bst.get("NTH"), "NA");
		
		Retreat ret = new Retreat(rus, bst.get("NTH"), bst.get("YOR"));
		Disband dsb = new Disband(rus, bst.get("NTH"));
		
		System.out.println(ret.toOrder());
		System.out.println(dsb.toOrder());
	}
	
	public static void main(String[] args) throws Exception{
		new TestOrders().testPrint();
	}
}

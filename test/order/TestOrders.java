//package order;
//
//import java.util.Arrays;
//
//import order.builds.Build;
//import order.builds.Remove;
//import order.builds.Waive;
//import order.retreats.Disband;
//import order.retreats.Retreat;
//import order.spring_fall.Convoy;
//import order.spring_fall.Move;
//import order.spring_fall.MoveByConvoy;
//import order.spring_fall.SupportHold;
//import order.spring_fall.SupportMove;
//
//import representation.Country;
//import representation.Player;
//import representation.Unit;
//import state.constant.BoardConfiguration;
//import state.dynamic.BoardState;
//
//public class TestOrders {
//	
//	public void testPrint() throws Exception{
//	
//		BoardConfiguration bcfg = new BoardConfiguration();
//		BoardState bst = bcfg.getInitialState();
//		
//		Player rus = bcfg.getPlayer(Country.RUS);
//		
//		bcfg.removeOccupier(bst, bcfg.get("STP"));
//
//		Unit nwy = new Unit(rus, true);
//		Unit nrg = new Unit(rus, false);
//		
//		bcfg.setOccupier(bst, nwy, bcfg.get("NWY"));
//		bcfg.setOccupier(bst, nrg, bcfg.get("BAR"));
//		
//		Convoy cnv = new Convoy(bst, rus, bcfg.get("BAR"), bcfg.get("NWY"), bcfg.get("STP"));
//		Move mov = new Move(bst, rus, bcfg.get("BAR"), bcfg.get("NRG"));
//		MoveByConvoy mbc = new MoveByConvoy(bst, rus, bcfg.get("NWY"), bcfg.get("STP"), Arrays.asList(bcfg.get("BAR")));
//		SupportHold hld = new SupportHold(bst, rus, bcfg.get("BAR"), bcfg.get("NWY"));
//		SupportMove smv = new SupportMove(bst, rus, bcfg.get("BAR"), bcfg.get("NWY"), bcfg.get("STP"));
//		Move mov2 = new Move(bst, rus, bcfg.get("BAR"), bcfg.get("STP"), "NCS");
//		
//		System.out.println(cnv.toOrder(bst));
//		System.out.println(mov.toOrder(bst));
//		System.out.println(mbc.toOrder(bst));
//		System.out.println(hld.toOrder(bst));
//		System.out.println(smv.toOrder(bst));
//		System.out.println(mov2.toOrder(bst));
//		
//		//	change seasons
//		bst.setTime("WIN", 1901);
//		
//		//	test removing units 
//		Remove rem = new Remove(rus, bst.get("BAR"));
//	
//		System.out.println(rem.toOrder());
//		
//		bst.removeOccupier(bst.get("NWY"));
//		bst.removeOccupier(bst.get("BAR"));
//		
//		//	test building or not building new ones
//		Build bld = new Build(rus, new Unit(rus, false), bst.get("STP"), "NC");
//		Waive wve = new Waive(rus);
//
//		System.out.println(bld.toOrder());
//		System.out.println(wve.toOrder());
//		
//		//	change to retreat season
//		bst.setTime("AUT", 1902);
//		bst.setRetreatingUnit(new Unit(rus, false), bst.get("NTH"), "NA");
//		
//		Retreat ret = new Retreat(rus, bst.get("NTH"), bst.get("YOR"));
//		Disband dsb = new Disband(rus, bst.get("NTH"));
//		
//		System.out.println(ret.toOrder());
//		System.out.println(dsb.toOrder());
//	}
//	
//	public static void main(String[] args) throws Exception{
//		new TestOrders().testPrint();
//	}
//}

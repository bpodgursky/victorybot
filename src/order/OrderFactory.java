package order;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import representation.Country;
import representation.TerritorySquare;
import state.BoardState;

public class OrderFactory {

	//public final TerritorySquare unitLoc;

	
	public final BoardState state;
	
	public OrderFactory(BoardState state){
		this.state = state;
//		turn = move.substring(move.indexOf("(")+2, move.indexOf(")"));
//		System.out.println(turn);
//		move = move.substring(move.indexOf(")")+5, move.length());
//		unitLoc = terrs.get(move.substring(0, 2));
//		System.out.println(unitLoc.toString());
//		move = move.substring(move.indexOf(")")+1, move.length());
//		order = move.substring(0, 2);
//		System.out.println(order);
//		orderLoc = terrs.get(move.substring(3, 5));
//		System.out.println(orderLoc.toString());
//		move = move.substring(move.indexOf("("), move.length());
//		result = move.substring(0, 2);
//		System.out.println(result);
	}
	
	public Order buildOrder(String[] orderArray){

		//	TODO for now tack this together, but do we want to deal with tokens 
		//	separately?
		String order = "";
		for(String s: orderArray){
			order+=s;
		}
		
		List<String>  contentTokens = new LinkedList<String>();
		
		String result;
		String turn = "asdf";
		String orderType = "";
				
		order = order.substring(3).trim();
		System.out.println(order);
		int parenCount = 0;
		System.out.println(order.charAt(0));
		if(order.charAt(0) == '(')
		{
			System.out.println("Inside IF");
			int i = 1;
			parenCount++;
			StringBuilder unit = new StringBuilder();
			while(order.charAt(i+1) != ')' && parenCount != 0)
			{
				System.out.println(i);
				if(order.charAt(i) == '(')
				{
					parenCount++;
					i++;
				}
				if(order.charAt(i) == ')')
				{
					parenCount--;
				}
				unit.append(order.charAt(i));
				i++;
			}
			System.out.println(unit.toString().trim());
			contentTokens.add(unit.toString().trim());
			order = order.substring(i+2).trim();
			orderType = order.substring(0,3);
			order = order.substring(4);
			System.out.println(order);
			if(order.length() == 1)
			{
				
			}else if(order.charAt(0) == '(')
			{
				
			}
			else
			{
				contentTokens.add(order.substring(0,3));
			}
		}
		else
		{
			
		}
		
		//1) turn order string into these token parts
		
		//2) switch on order type, content tokens parsed depending on 
		//	order type
		
		Order resultOrder = null;
		try
		{
			Order newOrder = null;
			if(orderType.equals("HLD")){
				String [] unitTokens = contentTokens.get(0).split(" ");
				Country c = Country.valueOf(unitTokens[0]);
				TerritorySquare from = state.get(unitTokens[2]);
				newOrder = new Hold(state.getPlayer(c), from);
				System.out.println(newOrder.toOrder());
				return newOrder;
			}else if(orderType.equals("MTO")){
				String [] unitTokens = contentTokens.get(0).split(" ");
				Country c = Country.valueOf(unitTokens[0]);
					
				TerritorySquare from = state.get(unitTokens[2]);
				TerritorySquare to = state.get(contentTokens.get(1));
				
				newOrder = new Move(state.getPlayer(c), from, to);
				System.out.println(newOrder.toOrder());
				return newOrder;
			}else if(orderType.equals("SUPMTO")){
				//TODO
			}else if(orderType.equals("CVYCTO")){
				//TODO
			}else if(orderType.equals("CTOVIA")){
				//TODO
			}else if(orderType.equals("RTO")){
				//TODO
			}else if(orderType.equals("DSB")){
				//TODO
			}else if(orderType.equals("BLD")){
				//TODO
			}else if(orderType.equals("REM")){
				//TODO
			}else if(orderType.equals("WVE")){
				//TODO
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		
		return resultOrder;
	}
	
	public static void main(String[] args)
	{
		System.out.println("Why?");
		BoardState temp = null;
		try {
			System.out.println("Inside");
			temp = new BoardState();
			OrderFactory test = new OrderFactory(temp);
			System.out.println("Before Order");
			Order testOrder = test.buildOrder(new String[]{"ORD ( ( ENG FLT LON ) HLD )"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

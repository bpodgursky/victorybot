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
	
	public Order buildOrder(String[] message){

		//	TODO for now tack this together, but do we want to deal with tokens 
		//	separately?
		String order = "";
		for(int i = 0; i < message.length; i++){
			String s = message[i];
			
			if(i != message.length -1){
				order+=s+" ";
			}else{
				order+=s;
			}
		}
		
		System.out.println("Order: "+order);
		
		List<LinkedList<String>>  contentTokens = new LinkedList<LinkedList<String>>();
		
		String result;
		String turn = message[2] + " " + message[3];
		String orderType = "";
				
		order = order.substring(3).trim();
		//System.out.println(order);
		int parenCount = 0;
		//System.out.println(order.charAt(0));
		if(message[5].equals("("))
		{
			//System.out.println("Inside IF");
			int i = 6;
			parenCount++;
			LinkedList<String> unit = new LinkedList<String>();
			while(i < message.length)
			{
				if(message[i].equals("("))
				{
					parenCount++;
					i++;
				}
				if(message[i].equals(")") && parenCount == 2)
				{
					contentTokens.add(unit);
					System.out.println(unit.toString());
					System.out.println(contentTokens.toString());
					parenCount--;
					i++;
					unit = new LinkedList<String>();
				}
				if(message[i].equals(")"))
				{
					if(parenCount == 1)
					{
						break;
					}
					parenCount--;
				}
				
				unit.add(message[i]);
				i++;
			}
			contentTokens.add(unit);
			if(contentTokens.get(0).get(contentTokens.get(0).size()-1).equals(")"))
			{
				contentTokens.get(0).remove(contentTokens.get(0).size()-1);
			}
			orderType = contentTokens.toString();
			System.out.println(orderType);
			System.out.println(contentTokens.get(1).toString());
			orderType = contentTokens.get(1).get(0);
			//System.out.println(unit.toString().trim());
			//contentTokens.add(unit.toString().trim());
			//order = order.substring(i+2).trim();
			//orderType = order.substring(0,3);
			//order = order.substring(4);
			//System.out.println(order);
			if(order.length() == 1)
			{
				
			}else if(order.charAt(0) == '(')
			{
				
			}
			else
			{
				//contentTokens.add(order.substring(0,3));
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
				Country c = Country.valueOf(contentTokens.get(0).get(0));
				TerritorySquare from = state.get(contentTokens.get(0).get(2));
				newOrder = new Hold(state.getPlayer(c), from);
				System.out.println(newOrder.toOrder());
				return newOrder;
			}else if(orderType.equals("MTO")){
				Country c = Country.valueOf(contentTokens.get(0).get(0));
					
				TerritorySquare from = state.get(contentTokens.get(0).get(2));
				TerritorySquare to = state.get(contentTokens.get(1).get(0));
				
				newOrder = new Move(state.getPlayer(c), from, to);
				//System.out.println(newOrder.toOrder());
				return newOrder;
			}else if(orderType.equals("SUP")){
				//TODO
			}else if(orderType.equals("CVY")){
				//TODO
			}else if(orderType.equals("CTO")){
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
			Order testOrder = test.buildOrder(new String[]{"ORD", "(", "SPR", "1901", ")", "(", "(", "RUS",
					"FLT", "(", "STP", "SCS", ")", ")",
					"HLD", ")"
					//"CTO", "NAO", "VIA", "(", "ENC", "ION", ")", ")"
					,"(", "SUC", ")"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package order;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import order.Order.Result;
import order.Order.RetreatState;

import representation.Country;
import representation.TerritorySquare;
import representation.Unit;
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
	
	public Order buildOrder(String[] message) throws Exception{

		System.out.println(Arrays.toString(message));
		
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
		
//		System.out.println("Order: "+order);
		
		List<LinkedList<String>>  contentTokens = new LinkedList<LinkedList<String>>();
		
//		String result;
//		String turn = message[2] + " " + message[3];
		String orderType = null;
				
		order = order.substring(3).trim();
		//System.out.println(order);
		int parenCount = 0;
		
		Result result = null;
		RetreatState retreat = null;
		
		//System.out.println(order.charAt(0));
		if(message[5].equals("("))
		{
			//System.out.println("Inside IF");
			int i = 6;
			parenCount++;
			LinkedList<String> unit = new LinkedList<String>();
			
			boolean seenDeeperParens = false;
			while(i < message.length)
			{
				if(message[i].equals("("))
				{
					parenCount++;
					i++;
				}
				else if(message[i].equals(")") && parenCount == 2)
				{					
					seenDeeperParens = true;
					contentTokens.add(unit);
					//System.out.println(unit.toString());
					//System.out.println(contentTokens.toString());
					parenCount--;
					i++;
					unit = new LinkedList<String>();
					
					if(orderType == null){
						orderType = message[i];
						i++;
					}
					
				}
				else if(message[i].equals(")"))
				{
					if(parenCount == 1)
					{
//						System.out.println("at "+i);
//						System.out.println("in "+Arrays.toString(message));
//
//						System.out.println("i+3 is "+message[i+3]);
//						System.out.println("i+2 is "+message[i+2]);
//						System.out.println("i+1 is "+message[i+1]);
//						System.out.println("i+0 is "+message[i]);
						
						//it's a waive
						if(orderType == null){
							
							orderType = "WVE";
							result = Result.SUC;
							retreat = RetreatState.NA;
						}
						//In the case of a unit which was convoying or holding, RET may be the only token.
						else if(orderType.equals("HLD") || orderType.equals("CVY")){
							
							if(!message[i+3].equals(")")){
								
								//	then there are two tokens
								
								result = Result.valueOf(message[i+2]);
								retreat = RetreatState.valueOf(message[i+3]);
								
							}else{
								
								//	otherwise we have to see what it is
								//	(because this syntax sucks)
								
								if(message[i+2].equals("RET")){
									retreat = RetreatState.RET;
									result = Result.FAIL;
								}else{
									retreat = RetreatState.NO;
									result = Result.SUC;
								}
							}
							
						}else{
						
							result = Result.valueOf(message[i+2]);
							
							if(!message[i+3].equals(")")){
								retreat = RetreatState.RET;
							}else{
								retreat = RetreatState.NO;
							}
						}
						
						break;
					}
					parenCount--;
					i++;
				}else{
					unit.add(message[i]);
					i++;
				}
			}
			if(unit.size() != 0)
			{
				contentTokens.add(unit);
			}
			if(contentTokens.get(0).get(contentTokens.get(0).size()-1).equals(")"))
			{
				contentTokens.get(0).remove(contentTokens.get(0).size()-1);
			}
			//orderType = contentTokens.toString();
			//System.out.println(orderType);
			if(!seenDeeperParens && contentTokens.size() == 1)
			{
				orderType = contentTokens.get(0).get(1);
			}
//			else
//			{
//				orderType = contentTokens.get(1).get(0);
//			}
			//System.out.println(unit.toString().trim());
			//contentTokens.add(unit.toString().trim());
			//order = order.substring(i+2).trim();
			//orderType = order.substring(0,3);
			//order = order.substring(4);
			//System.out.println(order);
//			if(order.length() == 1)
//			{
//				
//			}else if(order.charAt(0) == '(')
//			{
//				
//			}
//			else
//			{
//				//contentTokens.add(order.substring(0,3));
//			}
		}
		else
		{
			
		}
		
//		System.out.println("Order parts: ");
//		for(List<String> part: contentTokens){
//			System.out.println(part);
//		}
//		System.out.println("Order type: "+orderType);
//		System.out.println("Result: "+result);
		
		//1) turn order string into these token parts
		
		//2) switch on order type, content tokens parsed depending on 
		//	order type
	
		Order newOrder = null;
		
		Country c = Country.valueOf(contentTokens.get(0).get(0));
		
//		System.out.println("Country: "+c);
		
		if(orderType.equals("HLD")){

			TerritorySquare from = state.get(contentTokens.get(0).get(2));
			newOrder = new Hold(state.getPlayer(c), from, result, retreat);
			//System.out.println(newOrder.toOrder());
			//return newOrder;
		}else if(orderType.equals("MTO")){
				
			TerritorySquare from = state.get(contentTokens.get(0).get(2));
			
			TerritorySquare to = state.get(contentTokens.get(1).get(0));
			
			String coast = "NA";
			
			if(contentTokens.get(1).size() > 1){
				coast = contentTokens.get(1).get(1);
			}
			
			System.out.println("Country: "+contentTokens.get(0).get(0));
			System.out.println("Player: "+state.getPlayer(c));
			System.out.println("From: "+contentTokens.get(0).get(2));
			System.out.println("To: "+contentTokens.get(1).get(0));
			System.out.println("coast: "+coast);
			System.out.println("terr1: "+from);
			System.out.println("terr2: "+to);
			
			newOrder = new Move(state.getPlayer(c), from, to, coast, result, retreat);
			//System.out.println(newOrder.toOrder());
			//return newOrder;
		}else if(orderType.equals("SUP")){
			
			TerritorySquare from = state.get(contentTokens.get(0).get(2));
			TerritorySquare supporter = state.get(contentTokens.get(1).get(2));
			if(contentTokens.size() == 3)
			{
				TerritorySquare to = state.get(contentTokens.get(2).get(1));
				newOrder = new SupportMove(state.getPlayer(c), from, supporter, to, result, retreat);
			}
			else
			{
				newOrder = new SupportHold(state.getPlayer(c), from, supporter, result, retreat);
			}
			//System.out.println(newOrder.toOrder());
			//return newOrder;
		}else if(orderType.equals("CVY")){
			
			TerritorySquare from = state.get(contentTokens.get(0).get(2));
			TerritorySquare convoy = state.get(contentTokens.get(1).get(2));
			TerritorySquare to = state.get(contentTokens.get(2).get(1));
			newOrder = new Convoy(state.getPlayer(c), from, convoy, to, result, retreat);
			//System.out.println(newOrder.toOrder());
		}else if(orderType.equals("CTO")){
			
			List<TerritorySquare> via = new LinkedList<TerritorySquare>();
			TerritorySquare from = state.get(contentTokens.get(0).get(2));
			TerritorySquare to = state.get(contentTokens.get(1).get(1));
			for(int i = 3; i < contentTokens.get(1).size(); i++)
			{
				TerritorySquare viaTerr = state.get(contentTokens.get(1).get(i));
				via.add(viaTerr);
			}
			newOrder = new MoveByConvoy(state.getPlayer(c), from, to, via, result, retreat);
			//System.out.println(newOrder.toOrder());
		}else if(orderType.equals("RTO")){
			
			TerritorySquare from = state.get(contentTokens.get(0).get(2));
			TerritorySquare to = state.get(contentTokens.get(1).get(0));
			
			String rToCoast = "NA";
			if(contentTokens.get(1).size() > 1){
				rToCoast = contentTokens.get(1).get(1);
			}
			
			newOrder = new Retreat(state.getPlayer(c), from, to, rToCoast, result);
			//System.out.println(newOrder.toOrder());
		}else if(orderType.equals("DSB")){
			
			TerritorySquare from = state.get(contentTokens.get(0).get(2));
			newOrder = new Disband(state.getPlayer(c), from);
			//System.out.println(newOrder.toOrder());
		}else if(orderType.equals("BLD")){
			
			TerritorySquare from = state.get(contentTokens.get(0).get(2));
			Unit u = null;
			if(contentTokens.get(0).get(1).equals("AMY"))
			{
				u = new Unit(state.getPlayer(c), true);
			}
			else
			{
				u = new Unit(state.getPlayer(c), false);
			}
			newOrder = new Build(state.getPlayer(c), u, from);
		}else if(orderType.equals("REM")){
			
			TerritorySquare from = state.get(contentTokens.get(0).get(2));
			newOrder = new Remove(state.getPlayer(c), from);
		}else if(orderType.equals("WVE")){
			
			newOrder = new Waive(state.getPlayer(c));
		}else{
			throw new Exception("unknown type "+orderType);
		}

		return newOrder;
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
			Order testOrder = test.buildOrder(new String[]{"ORD", "(", "SPR", "1901", ")", "(", "ENG",
					"WVE", ")",//, "VIA",
					//"(", "NTH", ")", ")"
					//"CTO", "NAO", "VIA", "(", "ENC", "ION", ")", ")"
					"(", "SUC", ")"});
			
			System.out.println(testOrder+"\n");
			
			Order testMove2 = test.buildOrder("ORD ( SPR 1901 ) ( ( RUS FLT STP SCS ) MTO FIN ) ( SUC )".split(" "));

			System.out.println(testMove2+"\n");
			
			Order testMove = test.buildOrder("ORD ( SPR 1901 ) ( ( AUS AMY BUD ) MTO SER ) ( SUC )".split(" "));

			System.out.println(testMove+"\n");
			
			Order testHold = test.buildOrder("ORD ( SPR 1901 ) ( ( RUS AMY WAR ) HLD ) ( SUC )".split(" "));
			
			System.out.println(testHold+"\n");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package order;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	
	public Order buildOrder(String order){

		List<String>  contentTokens = new LinkedList<String>();
		
		String result;
		String turn = "asdf";
		String orderType;
				
		//1) turn order string into these token parts
		
		//2) switch on order type, content tokens parsed depending on 
		//	order type
		
		Order resultOrder = null;
		
		if(turn.equals("HLD")){
			//TODO 
		}else if(turn.equals("MTO")){
			//TODO
		}else if(turn.equals("SUPMTO")){
			//TODO
		}else if(turn.equals("CVYCTO")){
			//TODO
		}else if(turn.equals("CTOVIA")){
			//TODO
		}else if(turn.equals("RTO")){
			//TODO
		}else if(turn.equals("DSB")){
			//TODO
		}else if(turn.equals("BLD")){
			//TODO
		}else if(turn.equals("REM")){
			//TODO
		}else if(turn.equals("WVE")){
			//TODO
		}
		
		return resultOrder;
	}
}

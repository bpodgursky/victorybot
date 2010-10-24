package order;

import java.util.Map;

import representation.TerritorySquare;

public class OrderToken {

	public final String turn;
	public final TerritorySquare unitLoc;
	public final String order;
	public final TerritorySquare orderLoc;
	public final String result;
	
	public OrderToken(String move, Map<String, TerritorySquare> terrs)
	{
		turn = move.substring(move.indexOf("(")+2, move.indexOf(")"));
		System.out.println(turn);
		move = move.substring(move.indexOf(")")+5, move.length());
		unitLoc = terrs.get(move.substring(0, 2));
		System.out.println(unitLoc.toString());
		move = move.substring(move.indexOf(")")+1, move.length());
		order = move.substring(0, 2);
		System.out.println(order);
		orderLoc = terrs.get(move.substring(3, 5));
		System.out.println(orderLoc.toString());
		move = move.substring(move.indexOf("("), move.length());
		result = move.substring(0, 2);
		System.out.println(result);
	}
}

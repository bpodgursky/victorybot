package heuristic;

import java.util.Set;

import order.Order;
import order.spring_fall.Move;
import representation.Player;
import state.constant.BoardConfiguration;
import state.dynamic.BoardState;

public abstract class Heuristic {

	protected BoardConfiguration staticBoard;
	
	public Heuristic(BoardConfiguration sBoard)
	{
		staticBoard = sBoard;
	}
	
	//Returns a double that is the score of a board for a player
	public abstract double boardScore(Player player, BoardState dynamicBoard);

	//Returns a double that is the score of a single order in some board configuration
	public abstract double orderScore(BoardState dynamicBoard, Set<Order> successfulOrders, Player player);
}

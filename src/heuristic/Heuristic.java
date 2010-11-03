package heuristic;

import order.Order;
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
	public abstract double orderScore(Order ord, BoardState dynamicBoard);
}

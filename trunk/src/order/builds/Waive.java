package order.builds;

import order.Order;
import order.Order.Result;
import order.Order.RetreatState;
import representation.Player;
import state.dynamic.BoardState;

public class Waive extends Order{

	public Waive(BoardState bst, Player player) throws Exception{
		super(player, Result.SUC, RetreatState.NA);
		
		player.board.assertCanWaive(bst, player);
	}

	@Override
	public void execute() {
	}

	@Override
	public String toOrder(BoardState bst) {
		return "( "+player.getName()+" WVE )";
	}
}

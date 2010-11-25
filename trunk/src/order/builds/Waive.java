package order.builds;

import ai.Bot;
import order.Order;
import representation.Player;
import state.dynamic.BoardState;

public class Waive extends Order{

	public Waive(BoardState bst, Player player) throws Exception{
		super(player, Result.SUC, RetreatState.NA);
		
		if(Bot.ASSERTS){
			player.board.assertCanWaive(bst, player);
		}
	}

	@Override
	public void execute() {
	}

	@Override
	public String toOrder(BoardState bst) {
		return "( "+player.getName()+" WVE )";
	}
	
	public int hashCode2(){
		return super.hashCode2();
	}
}

package order;

import representation.Player;

public class Waive extends Order{

	public Waive(Player player) throws Exception{
		super(player, Result.SUC, RetreatState.NA);
		
		player.board.assertCanWaive(player);
	}

	@Override
	public void execute() {
	}

	@Override
	public String toOrder() {
		return "( "+player.getName()+" WVE )";
	}
}

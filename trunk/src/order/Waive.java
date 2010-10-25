package order;

import representation.Player;

public class Waive extends Order{

	public Waive(Player player) {
		super(player);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toOrder() {
		return "( "+player.getName()+" WVE )";
	}
}

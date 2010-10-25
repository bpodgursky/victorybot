package order;

import representation.Player;

public abstract class Order {
	
	public final Player player;
	
	public Order(Player player){
		this.player = player;
	}
	
	public abstract void execute();
	
	public abstract String toOrder();
}

package state.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import order.Order;
import order.spring_fall.Hold;
import order.spring_fall.Move;

import representation.Player;
import representation.TerritorySquare;

import state.dynamic.BoardState.Phase;

//	a history of all moves
public class MoveHistory {

	List<OrderSet> moveHistory = new ArrayList<OrderSet>();
	
	public MoveHistory clone(){
		MoveHistory copy = new MoveHistory();
		
		copy.moveHistory.addAll(moveHistory);
		
		return copy;
	}
	
	public void add(int year, Phase phase, Set<Order>  orders){
		moveHistory.add(new OrderSet(year, phase, orders));
	}
	
	//	return whether territory was contested on the last movement turn
	public boolean wasTerritoryContested(TerritorySquare square){
		
		if(moveHistory.size() == 0) return false;
		
		for(int i = moveHistory.size()-1; i >= 0; i--){
			OrderSet moves = moveHistory.get(i);
			
			if(moves.phase == Phase.SPR || moves.phase == Phase.FAL){
				if(!moves.ordersTo.containsKey(square)) return false;
			
				return !moves.ordersTo.get(square).isEmpty();
			}else{
				continue;
			}
		}
		
		//	won't actually get here
		return false;
	}
	
	public boolean isValidRetreat(BoardState bst, TerritorySquare square){
		
		if(moveHistory.size() == 0) return true;
		
		for(int i = moveHistory.size()-1; i >= 0; i--){
			OrderSet moves = moveHistory.get(i);
			
			if(moves.phase == Phase.SPR || moves.phase == Phase.FAL){
				if(!moves.ordersTo.containsKey(square)) return true;
			
				//	if it's not empty now, it's not a valid retreat
				if(square.getOccupier(bst) != null) return false;
				
				//	if two or more units contested it, it's an invalid retreat (standoff)
				Set<Order> contestants = moves.ordersTo.get(square);
				
				if(contestants.size() >= 2) return false;
				
				return true;
			}else{
				continue;
			}
		}
		
		//	won't actually get here
		return false;
	}
}

class OrderSet{
	
	final Set<Order> allOrders = new HashSet<Order>();
	
	final Map<Player, Set<Order>> orders = new HashMap<Player, Set<Order>>();
	final Map<TerritorySquare, Set<Order>> ordersTo = new HashMap<TerritorySquare, Set<Order>>();
	
	final int year;
	final Phase phase;

	public OrderSet(int year, Phase phase, Set<Order>  orders){
				
		this.year = year;
		this.phase = phase;
		
		this.allOrders.addAll(orders); 
		
		//	mark where the orders were to
		for(Order ord: orders){
			if(ord.getClass() == Move.class){
				Move move = (Move)ord;
				
				if(!ordersTo.containsKey(move.to)){
					ordersTo.put(move.to, new HashSet<Order>());
				}
				
				ordersTo.get(move.to).add(move);
			}else if (ord.getClass() == Hold.class){
				Hold hold = (Hold)ord;
				
				if(!ordersTo.containsKey(hold)){
					ordersTo.put(hold.holdingSquare, new HashSet<Order>());
				}
				
				ordersTo.get(hold.holdingSquare).add(hold);
			}
		}
		
		//	and which players made them
		for(Order ord: orders){
			
			if(!this.orders.containsKey(ord.player)){
				this.orders.put(ord.player, new HashSet<Order>());
			}
			
			this.orders.get(ord.player).add(ord);
		}
		
	}
	
}
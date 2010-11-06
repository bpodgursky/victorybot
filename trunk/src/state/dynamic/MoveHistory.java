package state.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import order.Order;
import order.builds.Build;
import order.builds.Remove;
import order.retreats.Disband;
import order.retreats.Retreat;
import order.spring_fall.Convoy;
import order.spring_fall.Hold;
import order.spring_fall.Move;
import order.spring_fall.MoveByConvoy;
import order.spring_fall.SupportHold;
import order.spring_fall.SupportMove;
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
	
	public boolean isValidRetreat(BoardState bst, TerritorySquare from, TerritorySquare square) throws Exception{
		
		if(moveHistory.size() == 0) return true;
		
		for(int i = moveHistory.size()-1; i >= 0; i--){
			OrderSet moves = moveHistory.get(i);
			
			if(moves.phase == Phase.SPR || moves.phase == Phase.FAL){
			
				//	if it's not empty now, it's not a valid retreat
				if(square.getOccupier(bst) != null) return false;
				
				//	if units contested it, it's an invalid retreat (standoff)
				//	only 1 if there was a head to head battle
				if(moves.ordersTo.containsKey(square) &&
						moves.ordersTo.get(square).size() > 1) return false;
				
				//	can't retreat where your attacker came from
				if(moves.ordersFrom.get(square) != null){
					Order moveFrom = moves.ordersFrom.get(square);
					
					if(moveFrom.getClass() == Move.class){
						Move move = (Move)moveFrom;
						
						if(move.to == from) return false;
					}
				}
				
				return true;
				
			}else{
				continue;
			}
		}
		
		//	won't actually get here
		throw new Exception("history incomplete");
	}
}

class OrderSet{
	
	final Set<Order> allOrders = new HashSet<Order>();
	
	final Map<Player, Set<Order>> orders = new HashMap<Player, Set<Order>>();
	
	//TODO for now only move and movebyconvoy to 
	final Map<TerritorySquare, Set<Order>> ordersTo = new HashMap<TerritorySquare, Set<Order>>();
	final Map<TerritorySquare, Order> ordersFrom = new HashMap<TerritorySquare, Order>();
	
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
			}else if (ord.getClass() == MoveByConvoy.class){
				MoveByConvoy mbc = (MoveByConvoy)ord;
				
				if(!ordersTo.containsKey(mbc.convoyDestination)){
					ordersTo.put(mbc.convoyDestination, new HashSet<Order>());
				}
				
				ordersTo.get(mbc.convoyDestination).add(mbc);
			}
		}
		
		for(Order ord: orders){
			if(ord.getClass() == Build.class){
				Build build = (Build)ord;
				
				ordersFrom.put(build.location, build);
				
			}else if (ord.getClass() == Hold.class){
				Hold hold = (Hold)ord;
				
				ordersFrom.put(hold.holdingSquare, hold);
				
			}else if (ord.getClass() == Remove.class){
				Remove remove = (Remove)ord;
				
				ordersFrom.put(remove.disbandLocation, remove);
				
			}else if (ord.getClass() == Disband.class){
				Disband disband = (Disband)ord;
				
				ordersFrom.put(disband.disbandAt, disband);
				
			}else if (ord.getClass() == Retreat.class){
				Retreat retreat = (Retreat)ord;
				
				//	to or from?
				ordersFrom.put(retreat.from, retreat);
				
			}else if (ord.getClass() == Convoy.class){
				Convoy convoy = (Convoy)ord;
				
				ordersFrom.put(convoy.convoyer, convoy);
				
			}else if (ord.getClass() == Move.class){
				Move move = (Move)ord;
				
				ordersFrom.put(move.from, move);
				
			}else if (ord.getClass() == MoveByConvoy.class){
				MoveByConvoy move = (MoveByConvoy)ord;
				
				ordersFrom.put(move.convoyOrigin, move);
				
			}else if (ord.getClass() == SupportHold.class){
				SupportHold shold = (SupportHold)ord;
				
				ordersFrom.put(shold.supportFrom, shold);
				
			}else if (ord.getClass() == SupportMove.class){
				SupportMove smove = (SupportMove)ord;
				
				ordersFrom.put(smove.supportFrom, smove);
				
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
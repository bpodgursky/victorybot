package heuristic;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import order.Order;
import order.Order.MovesValue;
import order.Order.Result;
import order.Order.RetreatState;
import representation.Player;
import state.dynamic.BoardState;
import state.dynamic.BoardState.Phase;
import heuristic.Heuristic.MovePruningHeuristic;

public class NaivePruner extends MovePruningHeuristic{

	private static final int MOVES_TO_ENUMERATE = 20;
	private static final int BUILDS_TO_ENUMERATE = 3;
	private static final int RETREATS_TO_ENUMERATE = 3;
	

	private static final int FULL_RESOLVE = 0;
	private static final int QUICK_RESOLVE = 1;

	
	//	config
	
	//	parameters

	private static final int ORDERSET_RANK_MODE = 0;
	
	public NaivePruner(Heuristic baseHeuristic) {
		super(baseHeuristic);
	}

	@Override
	public MovesValue[] getPrunedMoves(Player player, List<Collection<Order>> allCombinations, BoardState dynamicState) throws Exception{
		//	want to order these moves by the naive quality the 
		//	board would have afterwards--basically, which of them
		//	is the opponent most likely to do
		//	the simplest way to calculate this is "what if everyone holds"
		
		int ordersToEnumerate = 0;
		if(dynamicState.time.phase == Phase.SPR || dynamicState.time.phase == Phase.FAL){
			ordersToEnumerate = MOVES_TO_ENUMERATE;
		}else if(dynamicState.time.phase == Phase.SUM || dynamicState.time.phase == Phase.AUT){
			ordersToEnumerate = RETREATS_TO_ENUMERATE;
		}else{
			ordersToEnumerate = BUILDS_TO_ENUMERATE;
		}
		
		Set<Order> otherOrders = new HashSet<Order>();
		for(Player p: heuristic.staticBoard.getPlayers()){
			if(p == player) continue;
			
			otherOrders.addAll(heuristic.staticBoard.generateDefaultOrdersFor(dynamicState, p));
		}
		
		List<MovesValue> valMoves = new LinkedList<MovesValue>();
		
		for(Collection<Order> ords: allCombinations){
			
			Set<Order> toSubmit = new HashSet<Order>(otherOrders);
			toSubmit.addAll(ords);
			
			if(ORDERSET_RANK_MODE == QUICK_RESOLVE){
				heuristic.staticBoard.quickResolve(dynamicState, toSubmit, player);
				Set<Order> sucMoves = new HashSet<Order>();
				
				for(Order o: toSubmit){
					if(o.actionResult == Result.SUC){
						sucMoves.add(o);
					}
				}
				
				valMoves.add(new MovesValue(ords, heuristic.scorer.orderScore(dynamicState, ords, player)));
				
				for(Order ord: ords){
					ord.actionResult = Result.MAYBE;
					ord.retreatState = RetreatState.MAYBE;
				}
				
			}else if(ORDERSET_RANK_MODE == FULL_RESOLVE){
				BoardState stateAfterExecute = heuristic.staticBoard.update(dynamicState.time.next(), dynamicState, toSubmit, false);
				
				valMoves.add(new MovesValue(ords, heuristic.scorer.boardScore(player, stateAfterExecute)));
			}
		}
	
		MovesValue[] moves = valMoves.toArray(new MovesValue[0]);
		Arrays.sort(moves);
		
		MovesValue[] prunedMoves = new MovesValue[
		    Math.min(ordersToEnumerate, moves.length)];
		
		for(int i = 0; i < ordersToEnumerate && i < moves.length; i++){
			prunedMoves[i] = moves[i];
		}
		
		return prunedMoves;
	}

	
	
}

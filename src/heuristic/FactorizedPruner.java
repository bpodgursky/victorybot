package heuristic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.Bot;

import order.Order;
import order.Order.MovesValue;
import representation.Player;
import representation.TerritorySquare;
import state.dynamic.BoardState;
import state.dynamic.BoardState.Phase;
import heuristic.Heuristic.MovePruningHeuristic;
import java.util.Arrays;

public class FactorizedPruner extends MovePruningHeuristic{

	//TODO diff for builds and retreats
	//private static final int MAX_NAIVE_MOVES_PER_PLAYER = 25;
	

	private static final int MOVES_TO_ENUMERATE = 30;
	private static final int BUILDS_TO_ENUMERATE = 3;
	private static final int RETREATS_TO_ENUMERATE = 3;
	
	private static final boolean DEBUG = false;
	
	public FactorizedPruner(Heuristic baseHeuristic) {
		super(baseHeuristic);
	}

	@Override
	public MovesValue[] getPrunedMoves(Player player,
			List<Collection<Order>> allCombinations, BoardState dynamicState)
			throws Exception {
		
		//	if you don't have a choice, whatever
		if(allCombinations.size() == 1){
			return new MovesValue[]{new MovesValue(allCombinations.get(0), 0)};
		}
		
		int ordersToEnumerate = 0;
		if(dynamicState.time.phase == Phase.SPR || dynamicState.time.phase == Phase.FAL){
			ordersToEnumerate = MOVES_TO_ENUMERATE;
		}else if(dynamicState.time.phase == Phase.SUM || dynamicState.time.phase == Phase.AUT){
			ordersToEnumerate = RETREATS_TO_ENUMERATE;
		}else{
			ordersToEnumerate = BUILDS_TO_ENUMERATE;
		}
		
		if(DEBUG) System.out.println("Factorized pruning for player "+player.getName());
		//TODO cache this between calls to players
		Map<Player, List<MovesValue>> naiveMovesForPlayers = new HashMap<Player, List<MovesValue>>();
		
		if(DEBUG) System.out.println("\tgetting naive moves for each player...");
		

		Collection<Player> relevantOthers = heuristic.relevance.getRelevantPlayers(dynamicState, player);
		
		for(Player next: heuristic.staticBoard.getPlayers()){
			
			if(relevantOthers.contains(next)){
			
				if(DEBUG) System.out.println("\t\tgetting naive moves for "+next.getName()+"...");
				
				List<Collection<Order>> movesForPlayer = heuristic.orderGenerator.generateOrderSets(next, dynamicState);
				
				BoardState lonelyBst = dynamicState.clone(dynamicState.time);
		
				Collection<Order> defaultOtherPlayerOrders = new LinkedList<Order>();
				
				//	get a board that is empty except for this player's units
				for(Player p: heuristic.staticBoard.getPlayers()){
					if(p == next) continue;
					
					for(TerritorySquare sqr: new ArrayList<TerritorySquare>(p.getOccupiedTerritories(lonelyBst))){
						heuristic.staticBoard.removeOccupier(lonelyBst, sqr);
					}
					
					//	still need to waive some stuff or generate empty sets
					defaultOtherPlayerOrders.addAll(heuristic.staticBoard.generateDefaultOrdersFor(lonelyBst, p));
					
				}
				
				List<MovesValue> moveValues = new ArrayList<MovesValue>();
				
				for(Collection<Order> proposedOrders: movesForPlayer){
					
					Collection<Order> fullSet = new LinkedList<Order>(defaultOtherPlayerOrders);
					fullSet.addAll(proposedOrders);
					
					BoardState updatedState = heuristic.staticBoard.update(lonelyBst.time.next(), lonelyBst, fullSet, false);
				
					moveValues.add(new MovesValue(proposedOrders, heuristic.scorer.boardScore(next, updatedState)));
				}
				
				Collections.sort(moveValues);
				
				List<MovesValue> prunedNaive = new ArrayList<MovesValue>();
				for(int i = 0; i < moveValues.size() && i < ordersToEnumerate; i++){
					prunedNaive.add(moveValues.get(i));
				}
				
				naiveMovesForPlayers.put(next, prunedNaive);
			}else{
			
				naiveMovesForPlayers.put(next, Arrays.asList(
						new MovesValue(heuristic.staticBoard.generateDefaultOrdersFor(dynamicState, next), 0)));
			}
		}
		
		Map<Player, Collection<Order>> defaultOrders = new HashMap<Player, Collection<Order>>();
		
		for(Player p: heuristic.staticBoard.getPlayers()){
			defaultOrders.put(p, heuristic.staticBoard.generateDefaultOrdersFor(dynamicState, p));
		}
		
		List<MovesValue> smarterScores = new ArrayList<MovesValue>();
		
		//	for each of this player's moves
		if(DEBUG) System.out.println("\tlooking at each of "+naiveMovesForPlayers.get(player).size()+" moves..."); int i =0;
		
		
		for(MovesValue mv: naiveMovesForPlayers.get(player)){
			
			if(DEBUG) if(i++ % 10 == 0) System.out.println("\t"+i+"...");
			
			double sumOfWorst = 0;
			
			//	for each other player
			for(Player other: heuristic.staticBoard.getPlayers()){
				if(other == player) continue;
				
				//	assume that nobody moves except that player and you
				Collection<Order> theseDefaultOrders = new LinkedList<Order>();
				for(Player rest: heuristic.staticBoard.getPlayers()){
					if(rest == other || rest == player) continue; 
				
					theseDefaultOrders.addAll(defaultOrders.get(rest));
				}
				
				//TODO expect instead?
				Double minAcross = null;
				//	do a [min expect] against the top ranked of their moves, store that for the move+opponent	
				for(MovesValue theirMv: naiveMovesForPlayers.get(other)){
					
					Collection<Order> toExecute = new ArrayList<Order>(theseDefaultOrders);
					toExecute.addAll(theirMv.moves);
					toExecute.addAll(mv.moves);
					
					BoardState stateAfter = heuristic.staticBoard.update(dynamicState.time.next(), dynamicState, toExecute, false);
					
					double value = heuristic.scorer.boardScore(player, stateAfter);
					
					if(minAcross == null || value < minAcross){
						minAcross = value;
					}
				}
				
				sumOfWorst+=minAcross;

			}
			
			//	set the move's score across all players
			//	couple of ways to do it:
			//		1) min across player outcomes
			//		2) average across player outcomes
			//		3) sum across player outcomes
			//			using this one for now
			//	TODO use a weight by probability if we get that
			smarterScores.add(new MovesValue(mv.moves, sumOfWorst));
		}
		
		//		  rank the moves by these new scores
		Collections.sort(smarterScores);
		
		return smarterScores.toArray(new MovesValue[0]);
	}
	
}

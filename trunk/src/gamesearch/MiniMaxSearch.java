package gamesearch;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import order.Order;
import order.Order.MovesValue;
import representation.Player;
import state.constant.BoardConfiguration;
import state.constant.BoardConfiguration.YearPhase;
import state.dynamic.BeliefState;
import state.dynamic.BoardState;
import state.dynamic.DiplomaticState;

public class MiniMaxSearch extends GameSearch{
	
	private static final int MAX_TOTAL_ENUM = 25000;
	private int maxEnumTmp;
	
	public MiniMaxSearch(Player player, BoardConfiguration state, DiplomaticState dipState, BeliefState beliefState){
		super(player, state, dipState, beliefState);
	}
	
	//	base case of search.  Returns the best set of moves for us
	protected Collection<Order> moveSearch(BoardState bst, YearPhase until) throws Exception{
		
		//	build sets of all moves for all relevant players
	
		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		
		Collection<Player> relevantPlayers = heuristic.relevance.getRelevantPlayers(bst, relevantPlayer);
		
		System.out.println("Building possible order sets for players: ");
		//	for each player, generate the a priori likely moves
		List<Player> otherPlayers = new LinkedList<Player>();
		for(Player p: boardConfiguration.getPlayers()){
			
			System.out.println("\t"+p.getName()+"...");
			if(this.boardUpdate){
				System.out.println("Took too long, quitting in generation...");
				return null;
			}
			
			if(relevantPlayers.contains(p)){
				orderSetsByPlayer.put(p, gen.generateOrderSets(p, bst));
			}else{
				
				MovesValue[] hold = new MovesValue[1];
				hold[0] = new MovesValue(boardConfiguration.generateDefaultOrdersFor(bst, p), -1);
				
				orderSetsByPlayer.put(p, hold);
			}
			if(p != this.relevantPlayer){
				otherPlayers.add(p);
			}
			
			System.out.println("\t"+p.getName()+" done");
		}
		
		
		int relevantPlayerCount = relevantPlayers.size();	
		int movesUntil = bst.time.movesUntil(until);
		maxEnumTmp = (int)Math.pow(MAX_TOTAL_ENUM, 1.0/(movesUntil*relevantPlayerCount));
		
		System.out.println("Generated orders for each player:");
		for(Player p: orderSetsByPlayer.keySet()){
			System.out.println("\t"+orderSetsByPlayer.get(p).length);
		}
		
		System.out.println("Enumerating combinations for our "+ orderSetsByPlayer.get(relevantPlayer).length+ " moves..");
		Player[] playerArray = otherPlayers.toArray(new Player[0]);	
		MovesValue [] maxSetMoves = new MovesValue[orderSetsByPlayer.get(relevantPlayer).length];
		int count = 0;
		MovesValue bestMoveSoFar = null;
		
		for(MovesValue playerOrds: orderSetsByPlayer.get(relevantPlayer)){
			
			List<Collection<Order>> orderList = new LinkedList<Collection<Order>>();
			orderList.add(playerOrds.moves);
		
			maxSetMoves[count] = new MovesValue(playerOrds.moves, min(bst, until, playerOrds.moves, orderSetsByPlayer, playerArray));
			
			
			if(this.boardUpdate){
				System.out.println("Took too long, quitting...");
				return null;
			}
			
			if(bestMoveSoFar == null)
			{
				bestMoveSoFar = maxSetMoves[count];
				currentOrders = bestMoveSoFar.moves;
			}
			if(bestMoveSoFar.value < maxSetMoves[count].value)
			{
				bestMoveSoFar = maxSetMoves[count];
				currentOrders = bestMoveSoFar.moves;
			}
			count++;
			
			System.out.println(maxSetMoves[count-1].value);
			for(Order ord: playerOrds.moves){
				System.out.println("\t"+ord.toOrder(boardState));
			}
		
		}
		System.out.println("Done enumerating");
		
		Arrays.sort(maxSetMoves);
		
		//	for each of our moves, find with min the worst outcome from that move.
		//	return the set of moves with the highest associated min
		if(maxSetMoves.length == 0){
			return new HashSet<Order>();
		}
			
		return maxSetMoves[0].moves;
		
	}
	
	//	for the moves that we have made (friendlyOrders), what is the worst possible board 
	//	that can result from it.  We want to return the quality of that board
	private double min(BoardState bst, YearPhase until, Collection<Order> friendlyOrders,
			Map<Player, MovesValue []> orderSetsByPlayer, Player [] playerArray) throws Exception{
		
		//System.out.println("\tmin entered until "+until+"...");
		//System.out.println("\tboard is "+bst.time+"...");
		if(bst.time.isAfter(until)) {
			//System.out.println("\tquitting");
			return heuristic.scorer.boardScore(relevantPlayer, bst);
		}
		
		//	recurse through all enemy combinations (cap for each player)	
		
		List<Collection<Order>> orderList = new LinkedList<Collection<Order>>();
		orderList.add(friendlyOrders);
		
		List<List<Collection<Order>>> allMoveSets = enumerateMoves(bst, orderList, orderSetsByPlayer, playerArray, 0);
		
		if(boardUpdate){
			System.out.println("Took too long, quitting...");
			return -1;
		}
		
		MovesValue [] moveScores = new MovesValue[allMoveSets.size()];
		int count = 0;
		for(List<Collection<Order>> fullMoveSet: allMoveSets)
		{
			
			if(boardUpdate){
				System.out.println("Took too long, quitting...");
				return -1;
			}
			
			Set<Order> toExecute = new HashSet<Order>();
			
			for(Collection<Order> execute: fullMoveSet){	
				toExecute.addAll(execute);
			}
			BoardState updatedState = boardConfiguration.update(bst.time.next(), bst, toExecute, false);
			moveScores[count] = new MovesValue(toExecute, max(updatedState, until));
		
			count++;
		}
		
		//		base case of recursion, have a set of moves for each enemy player
		//			combine with the friendlyOrders above to make a full set of orders
		//			apply to gamestate, call max on this boardstate
		
		//	return the minimum board quality over all combinations
		Arrays.sort(moveScores);
		return moveScores[moveScores.length-1].value;
	}
	
	private double max(BoardState bst, YearPhase until) throws Exception{
		
		// if bst's date is after until, return the quality of the board

		if(bst.time.isAfter(until)) {
			return heuristic.scorer.boardScore(relevantPlayer, bst);
		}
		
		//	otherwise,
		
		//	build sets of all moves for all relevant players based on this bst

		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		
		Collection<Player> relevantPlayers = heuristic.relevance.getRelevantPlayers(bst, relevantPlayer);

		//	for each player, generate the a priori likely moves
		List<Player> otherPlayers = new LinkedList<Player>();
		for(Player p: boardConfiguration.getPlayers()){
			
			if(relevantPlayers.contains(p)){
				orderSetsByPlayer.put(p, gen.generateOrderSets(p, bst));
			}else{
				
				MovesValue[] hold = new MovesValue[1];
				hold[0] = new MovesValue(boardConfiguration.generateDefaultOrdersFor(bst, p), -1);
				
				orderSetsByPlayer.put(p, hold);
			}
			if(p != this.relevantPlayer){
				otherPlayers.add(p);
			}
		}
		
		//	for each of our moves, find with min the worst outcome from that move.
		//	return the maximum min found
		
		Player[] playerArray = otherPlayers.toArray(new Player[0]);	
		MovesValue [] maxSetMoves = new MovesValue[orderSetsByPlayer.get(relevantPlayer).length];
		int count = 0;
		
		//System.out.println("\tIn max, have "+maxSetMoves.length+" to look at... ");
		for(MovesValue playerOrds: orderSetsByPlayer.get(relevantPlayer)){
			
			List<Collection<Order>> orderList = new LinkedList<Collection<Order>>();
			orderList.add(playerOrds.moves);
			
			//int year = bst.time.year;
			//Phase phase = bst.time.phase == Phase.SPR ? Phase.SUM : Phase.WIN;
			//YearPhase until = new YearPhase(year, phase);
			maxSetMoves[count] = new MovesValue(playerOrds.moves, min(bst, until, playerOrds.moves, orderSetsByPlayer, playerArray));
			count++;
		}
		
		Arrays.sort(maxSetMoves);
		
		//	for each of our moves, find with min the worst outcome from that move.
		//	return the set of moves with the highest associated min
		return maxSetMoves[0].value;
		
		
	}
	
	private List<List<Collection<Order>>> enumerateMoves(BoardState bst, 
			List<Collection<Order>> allOrders, 
			Map<Player, MovesValue[]> playerOrders, 
			Player[] players, 
			int player) throws Exception{
		
		
		if(this.boardUpdate){
			System.out.println("Took too long, quitting...");
			return new LinkedList<List<Collection<Order>>>();
		}
		
		List<List<Collection<Order>>> playerEnumeration = new LinkedList<List<Collection<Order>>>();
		
		if(player == players.length){

			playerEnumeration.add(new LinkedList<Collection<Order>>(allOrders));
		}
		
		else if(players[player] == relevantPlayer){
			playerEnumeration.addAll(enumerateMoves(bst, allOrders, playerOrders, players, player+1));
		}
		else{
			
			MovesValue[] movesForPlayer = playerOrders.get(players[player]);
			
			for(int i = 0; i < movesForPlayer.length && i < maxEnumTmp; i++){			
				MovesValue mv = movesForPlayer[i]; 
				
				allOrders.add(mv.moves);
			
				playerEnumeration.addAll(enumerateMoves(bst, allOrders, playerOrders, players, player+1));
			
				allOrders.remove(mv.moves);
								
			}
		}
		return playerEnumeration;
	}
	
	
}

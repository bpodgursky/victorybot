package gamesearch;

import heuristic.NaiveHeuristic;

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

public class ExpectiMaxSearch extends GameSearch{
	
	public ExpectiMaxSearch(Player player, BoardConfiguration state, DiplomaticState dipState, BeliefState beliefState){
		super(player, state, dipState, beliefState);
	}
	
//	base case of search.  Returns the best set of moves for us
	protected Collection<Order> moveSearch(BoardState bst, YearPhase until) throws Exception{
		
		//	build sets of all moves for all relevant players
	
		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		
		Set<Player> relevantPlayers = boardConfiguration.getRelevantPlayers(bst, relevantPlayer);
		
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
		
			maxSetMoves[count] = new MovesValue(playerOrds.moves, expect(bst, until, playerOrds.moves, orderSetsByPlayer, playerArray));
			
			
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
			
			//if(count % 1 == 0) System.out.println("\t"+count+" processed...");
		
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
	private double expect(BoardState bst, YearPhase until, Collection<Order> friendlyOrders,
			Map<Player, MovesValue []> orderSetsByPlayer, Player [] playerArray) throws Exception{
		
		//System.out.println("\tmin entered until "+until+"...");
		//System.out.println("\tboard is "+bst.time+"...");
		if(bst.time.isAfter(until)) {
			//System.out.println("\tquitting");
			return heuristic.boardScore(relevantPlayer, bst);
		}
		
		//	recurse through all enemy combinations (cap for each player)	
		
		List<Collection<Order>> orderList = new LinkedList<Collection<Order>>();
		orderList.add(friendlyOrders);
		
		Map<List<Collection<Order>>, Double> moveProbabilities = new HashMap<List<Collection<Order>>, Double>();
		enumerateMoves(bst, orderList, 1.0, orderSetsByPlayer, playerArray, 0, moveProbabilities);
		
		if(boardUpdate){
			System.out.println("Took too long, quitting...");
			return -1;
		}
		
		double utilitySum = 0;
		double sumProb = 0;
		
		for(List<Collection<Order>> fullMoveSet: moveProbabilities.keySet())
		{
			double probability = moveProbabilities.get(fullMoveSet);
			
			if(boardUpdate){
				System.out.println("Took too long, quitting...");
				return -1;
			}
			
			Set<Order> toExecute = new HashSet<Order>();
			
			for(Collection<Order> execute: fullMoveSet){	
				toExecute.addAll(execute);
			}
			BoardState updatedState = boardConfiguration.update(bst.time.next(), bst, toExecute, false);
			
			utilitySum += probability*max(updatedState, until);
			sumProb+=probability;
		}
		
		//System.out.println("Prob sums to: "+sumProb);

		return utilitySum;
	}
	
	private double max(BoardState bst, YearPhase until) throws Exception{
		
		// if bst's date is after until, return the quality of the board

		if(bst.time.isAfter(until)) {
			return heuristic.boardScore(relevantPlayer, bst);
		}
		
		//	otherwise,
		
		//	build sets of all moves for all relevant players based on this bst

		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		
		Set<Player> relevantPlayers = boardConfiguration.getRelevantPlayers(bst, relevantPlayer);

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
			maxSetMoves[count] = new MovesValue(playerOrds.moves, expect(bst, until, playerOrds.moves, orderSetsByPlayer, playerArray));
			count++;
		}
		
		Arrays.sort(maxSetMoves);
		
		//	for each of our moves, find with min the worst outcome from that move.
		//	return the set of moves with the highest associated min
		return maxSetMoves[0].value;
		
		
	}
	
	//	how many moves to enumerate for each player.  hardcode for now
	private static final int MAX_ENUM = 4;
	
	private void enumerateMoves(BoardState bst, 
			List<Collection<Order>> allOrders, 
			double ordersetLikelihood,
			Map<Player, MovesValue[]> playerOrders, 
			Player[] players, 
			int player,
			Map<List<Collection<Order>>, Double> moveAndLikelihood) throws Exception{
		
		if(boardUpdate){
			System.out.println("Took too long, quitting...");
			return;
		}
		
		if(player == players.length){

			moveAndLikelihood.put(new LinkedList<Collection<Order>>(allOrders), ordersetLikelihood);
		}
		
		else if(players[player] == relevantPlayer){
			enumerateMoves(bst, allOrders, ordersetLikelihood, playerOrders, players, player+1, moveAndLikelihood);
		}
		else{
			
			MovesValue[] movesForPlayer = playerOrders.get(players[player]);
			
			int toExpand = Math.min(movesForPlayer.length, MAX_ENUM);
			for(int i = 0; i < toExpand; i++){			
				MovesValue mv = movesForPlayer[i]; 
				
				//TODO get this from the belief state
				//TODO maybe a function of the perceived quality to the player?
				double likelihood = ordersetLikelihood*1.0/toExpand;//ordersetLikelihood*Math.pow(.5, i+1);
				
				allOrders.add(mv.moves);
			
				enumerateMoves(bst, allOrders, likelihood, playerOrders, players, player+1, moveAndLikelihood);
			
				allOrders.remove(mv.moves);
								
			}
		}
	}
}

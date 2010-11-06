package gamesearch;

import heuristic.Heuristic;
import heuristic.NaiveHeuristic;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import order.Order;
import order.spring_fall.Hold;
import order.spring_fall.Move;
import order.spring_fall.SupportMove;

import representation.Country;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.constant.BoardConfiguration;
import state.constant.BoardConfiguration.TerritoryCoast;
import state.dynamic.BoardState;
import state.dynamic.BoardState.Phase;

public class MoveGeneration {

	private BoardConfiguration staticBoard;
	private Heuristic heuristic;
	
	public MoveGeneration(BoardConfiguration sBoard)
	{
		staticBoard = sBoard;
		heuristic = new NaiveHeuristic(sBoard);
	}
	
	private Random r = new Random();
	
	private static final int MOVES_PER_UNIT = 3;
	private static final int MAX_PLAYER_MOVES = 50;
	
	public static class OrderValue{
		
		public final double score;
		public final Order order;
		
		public OrderValue(Order ord, double score){
			this.score = score;
			this.order = ord;
		}
	}
	
	private List<Set<Order>> generateOrderSets(int num, int length, Set<TerritorySquare> unit, BoardState dynamicState, Player player) throws Exception
	{
		StringBuilder str = new StringBuilder();
		TerritorySquare [] unitList = unit.toArray(new TerritorySquare[0]);
		
		int count = 0;
		
		while(num >= 1)
		{
			count++;
			if(num%2 == 1)
			{
				str.insert(0,"1");
			}
			else
			{
				str.insert(0,"0");
			}
			num = num / 2;
		}
		
		for(int i = count; i < length; i++)
		{
			str.insert(0, "0");
		}
		
		char [] permute = str.toString().toCharArray();

		//Create vectors to store orders of units that will move and of units that we want to support other moves
		//Also create a HashMap between heuristic score of move and the order itself
		//Vector<Order> moveSet = new Vector<Order>();
		List<TerritorySquare> supportSet = new ArrayList<TerritorySquare>();
		
		Map<TerritorySquare, OrderValue[]> movesPerUnit = new HashMap<TerritorySquare, OrderValue[]>();
		
		count = 0;
		//Step through the array that gives us our permutations
		for(char c: permute)
		{
			TerritorySquare moveOrigin = unitList[count];
			
			//Means we will be giving a move order
			if(c == '1')
			{
				
				List<TerritoryCoast> possibleMoves = staticBoard.getMovesForUnit(dynamicState, moveOrigin);
				List<OrderValue> orders = new LinkedList<OrderValue>();
				
				int pMoveCount = 0;
				for(TerritoryCoast tcoast: possibleMoves){

					Move move = new Move(dynamicState, player, moveOrigin, tcoast.sqr, tcoast.coast);
					orders.add(new OrderValue(move, heuristic.orderScore(move, dynamicState)));			
				
					
					if(pMoveCount++ >= MOVES_PER_UNIT) break;
				}

				OrderValue[] ovArray = orders.toArray(new OrderValue[orders.size()]);
				
				Arrays.sort(ovArray, new Comparator<OrderValue>(){
					public int compare(OrderValue a, OrderValue b){
						return -Double.compare(a.score, b.score);
					}
				});
				
				movesPerUnit.put(moveOrigin, ovArray);
			}
			//If not a direct order put in supportSet for later processing
			else
			{
				supportSet.add(moveOrigin);
			}
			count++;
		}
		
		List<Set<Order>> ordersToPopulate = new ArrayList<Set<Order>>();

		TerritorySquare[] relevantTerritories = movesPerUnit.keySet().toArray(new TerritorySquare[0]);
		
		enumerateMoves(dynamicState, player, relevantTerritories, new LinkedList<OrderValue>(), 0, movesPerUnit, supportSet, ordersToPopulate);
		
		return ordersToPopulate;
	}
	
	private void enumerateMoves(BoardState bst, Player p, TerritorySquare[] terrs, List<OrderValue> allOrders, int terrIndex, Map<TerritorySquare, OrderValue[]> movesPerUnit, List<TerritorySquare> supportSet, List<Set<Order>> toPopulate) throws Exception{
		
		if(terrIndex == terrs.length){
			
			Set<TerritorySquare> supportSetCopy = new HashSet<TerritorySquare>(supportSet);
			Set<Order> finalMoveSet = new HashSet<Order>();
			HashMap<Double, Order> moveScoreMap = new HashMap<Double, Order>();
			
			//	added the rand because if two orders have the same score, 
			//	the second one will obliterate the first in the map
			for(OrderValue ov: allOrders){
				Double score = ov.score + r.nextDouble()/10000.0;
				moveScoreMap.put(score, ov.order);
			}
			
			//Sort heuristic scores and now lets step through remaining units and try to support our moves
			Double [] orderScore = (moveScoreMap.keySet().toArray(new Double[0]));
			Arrays.sort(orderScore);
	
			for(Double heurVal: orderScore)
			{
				
				Move supportedMove = (Move)moveScoreMap.get(heurVal);
				
				TerritorySquare suppToPoss = supportedMove.to;
				TerritorySquare supportFromPoss = supportedMove.from;
				
				for(TerritorySquare support: supportSetCopy.toArray(new TerritorySquare[0]))
				{
					
					//Do we border where a move is trying to go
					if(staticBoard.canSupportMove(bst, p, support,  supportFromPoss, suppToPoss)){
	
						Order ord = new SupportMove(bst, p, support, supportFromPoss, suppToPoss);
	
						finalMoveSet.add(ord);
						supportSetCopy.remove(support);
						
					}
				}
			}
			
			for(TerritorySquare ts: supportSetCopy){
				
				//TODO 	for now just hold if can't support a move.  In future,
				//	should support holds too 
				finalMoveSet.add(new Hold(bst, p, ts));	
			}

			for(OrderValue ov: allOrders)
			{
				finalMoveSet.add(ov.order);
			}
			
			if(finalMoveSet.size() != p.getOccupiedTerritories(bst).size()){
				
				for(Order ov: finalMoveSet){
					System.out.println("\t"+ov.toOrder(bst));
				}
				for(TerritorySquare terr: p.getOccupiedTerritories(bst)){
					System.out.println("\t"+terr.getName());
				}
				
				System.out.println("Support set: ");
				for(TerritorySquare terr: supportSet){
					System.out.println("\t"+terr.getName());
				}
				
				System.out.println("Moves per unit: ");
				for(TerritorySquare terr: movesPerUnit.keySet()){
					System.out.println("\t"+terr.getName());
				}
				
				throw new Exception("need "+p.getOccupiedTerritories(bst).size()+ " moves, got "+finalMoveSet.size());
			}
			
			toPopulate.add(finalMoveSet);
		}else{
			
			TerritorySquare thisOrigin = terrs[terrIndex];
			OrderValue[] ordersForTerr = movesPerUnit.get(thisOrigin);
			
			for(OrderValue ov: ordersForTerr){
			
				allOrders.add(ov);
				
				enumerateMoves(bst, p, terrs, allOrders, terrIndex+1, movesPerUnit, supportSet, toPopulate);
				
				allOrders.remove(ov);
			}
		}
	}
	
	//	so we can return the value associated with a set of moves
	public static class MovesValue{
		
		public final double value;
		public final Set<Order> moves;
		
		public MovesValue(Set<Order> moves, double value){
			this.value = value;
			this.moves = moves;
		}
	}
	
	public MovesValue[] generateOrderSets(Player player, BoardState dynamicState) throws Exception
	{
		int unitCount = player.getNumberUnits(dynamicState);

		List<Set<Order>> unitMasks = new LinkedList<Set<Order>>();
		Set<TerritorySquare> unit = player.getOccupiedTerritories(dynamicState);
		for(int i = 1; i < Math.pow(2.0,(double)unitCount); i++)
		{
			unitMasks.addAll(generateOrderSets(i, unitCount, unit, dynamicState, player));
		}
		
		//	want to order these moves by the naive quality the 
		//	board would have afterwards--basically, which of them
		//	is the opponent most likely to do
		//	the simplest way to calculate this is "what if everyone holds"
		
		Set<Order> otherOrders = new HashSet<Order>();
		for(Player p: this.staticBoard.getPlayers()){
			if(p == player) continue;
			
			otherOrders.addAll(staticBoard.generateHoldsFor(dynamicState, p));
		}
		
		List<MovesValue> valMoves = new LinkedList<MovesValue>();
		for(Set<Order> ord: unitMasks){
			
			Set<Order> toSubmit = new HashSet<Order>(otherOrders);
			toSubmit.addAll(ord);
			
			BoardState stateAfterExecute = staticBoard.update(dynamicState.time.next(), dynamicState, toSubmit, false);
		
			valMoves.add(new MovesValue(ord, heuristic.boardScore(player, stateAfterExecute)));
		}
		
		MovesValue[] moves = valMoves.toArray(new MovesValue[0]);
		
		Arrays.sort(moves, new Comparator<MovesValue>(){
			public int compare(MovesValue a, MovesValue b){
				return -Double.compare(a.value, b.value);
			}
		});
		
		MovesValue[] prunedMoves = new MovesValue[MAX_PLAYER_MOVES];
		for(int i = 0; i < MAX_PLAYER_MOVES && i < moves.length; i++){
			prunedMoves[i] = moves[i];
		}
		
		return prunedMoves;
	}
	
	public static void main(String [] args) throws Exception
	{
		BoardConfiguration staticBoard = new BoardConfiguration();

		MoveGeneration gen = new MoveGeneration(staticBoard);
		BoardState bst = staticBoard.getInitialState();
		int count = 0;
		
		Map<Player, MovesValue[]> orderSetsByPlayer =
			new HashMap<Player, MovesValue[]>();
		
		//	for each player, generate the a priori likely moves
		for(Player p: staticBoard.getPlayers()){
			orderSetsByPlayer.put(p, gen.generateOrderSets(p, bst));
		}
		
		
		//	try combinations of moves	
		
		
		for(MovesValue so: gen.generateOrderSets(staticBoard.getPlayer(Country.RUS), bst))
		{
			count++;
			
			System.out.println(Integer.toBinaryString(count));

			for(Order o: so.moves)
			{
				System.out.println(o.toOrder(bst));
			}
			System.out.println("");
		}
	}
	
}

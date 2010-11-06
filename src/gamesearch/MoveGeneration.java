package gamesearch;

import heuristic.Heuristic;
import heuristic.NaiveHeuristic;

import java.math.BigInteger;
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
	
	private Set<Order> generateOrderSet(int num, int length, Set<TerritorySquare> unit, BoardState dynamicState, Player player) throws Exception
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
		Vector<Order> moveSet = new Vector<Order>();
		Vector<Order> supportOrder = new Vector<Order>();
		Vector<TerritorySquare> supportSet = new Vector<TerritorySquare>();
		HashMap<Double, Order> moveScoreMap = new HashMap<Double, Order>();
		
		count = 0;
		Order ord = null;
		//Step through the array that gives us our permutations
		for(char c: permute)
		{
			TerritorySquare moveOrigin = unitList[count];
			
			//Means we will be giving a move order
			if(c == '1')
			{
				
				List<TerritoryCoast> possibleMoves = staticBoard.getMovesForUnit(dynamicState, moveOrigin);
				
				//TODO right now just a random territory listed.  Need to make it do all territories.
				TerritoryCoast chosenDestination = possibleMoves.get(r.nextInt(possibleMoves.size()));
				
				ord = new Move(dynamicState, player, moveOrigin,
						chosenDestination.sqr, chosenDestination.coast);
				
				//Add order to correct lists
				moveSet.add(ord);
				
				//TODO added the rand because if two orders have the same score, 
				//	the second one will obliterate the first in the map
				Double score = new Double(heuristic.orderScore(ord, dynamicState)) + r.nextDouble()/10000.0;
				moveScoreMap.put(score, ord);
				
			}
			//If not a direct order put in supportSet for later processing
			else
			{
				supportSet.add(moveOrigin);
			}
			count++;
		}
		
		//Sort heuristic scores and now lets step through remaining units and try to support our moves
		Double [] orderScore = (moveScoreMap.keySet().toArray(new Double[0]));
		Arrays.sort(orderScore);

		for(Double heurVal: orderScore)
		{
			
			Move supportedMove = (Move)moveScoreMap.get(heurVal);
			
			TerritorySquare suppToPoss = supportedMove.to;
			TerritorySquare supportFromPoss = supportedMove.from;
			
			for(TerritorySquare support: supportSet.toArray(new TerritorySquare[0]))
			{
				
				//Do we border where a move is trying to go
				if(staticBoard.canSupportMove(dynamicState, player, support,  supportFromPoss, suppToPoss)){

					ord = new SupportMove(dynamicState, player, support, supportFromPoss, suppToPoss);

					supportOrder.add(ord);
					supportSet.remove(support);
					
				}
			}
		}
		
		Set<Order> finalMoveSet = new HashSet<Order>();
		
		for(TerritorySquare ts: supportSet){
			
			//TODO 	for now just hold if can't support a move.  In future,
			//	should support holds too 
			finalMoveSet.add(new Hold(dynamicState, player, ts));	
		}
		
		for(Order ord1: supportOrder)
		{
			finalMoveSet.add(ord1);
		}
		
		for(Order ord2: moveSet)
		{
			finalMoveSet.add(ord2);
		}
		
		return finalMoveSet;
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
			unitMasks.add(generateOrderSet(i, unitCount, unit, dynamicState, player));
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
		
		Arrays.sort(moves, new Comparator(){
			public int compare(Object a, Object b){
				
				double valA = ((MovesValue)a).value;
				double valB = ((MovesValue)b).value;
				
				return -Double.compare(valA, valB);
			}
			
		});
		
		return moves;
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

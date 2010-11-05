package gamesearch;

import heuristic.Heuristic;
import heuristic.NaiveHeuristic;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import order.Order;
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
		System.out.println(str.toString());
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
				TerritoryCoast chosenDestination = possibleMoves.get(r.nextInt(possibleMoves.size()-1));
				
				ord = new Move(dynamicState, player, moveOrigin,
						chosenDestination.sqr, chosenDestination.coast);
				
				//Add order to correct lists
				moveSet.add(ord);
				
				//TODO added the rand because if two orders have the same score, 
				//	the second one will obliterate the first in the map
				Double score = new Double(heuristic.orderScore(ord, dynamicState)) + new Random().nextDouble()/10000.0;
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
		Vector<TerritorySquare> supportSetCopy = new Vector<TerritorySquare>(supportSet);
		
		for(Double heurVal: orderScore)
		{
			
			Move supportedMove = (Move)moveScoreMap.get(heurVal);
			
			TerritorySquare suppToPoss = supportedMove.to;
			TerritorySquare supportFromPoss = supportedMove.from;
			
			for(TerritorySquare support: supportSetCopy)
			{
				
				//Do we border where a move is trying to go
				if(staticBoard.canSupportMove(dynamicState, player, support,  supportFromPoss, suppToPoss)){

					ord = new SupportMove(dynamicState, player, support, supportFromPoss, suppToPoss);

					supportOrder.add(ord);
					supportSet.remove(support);
					
				}
			}
			supportSetCopy = supportSet;
			supportSet = new Vector<TerritorySquare>(supportSetCopy);
		}
		Set<Order> finalMoveSet = new HashSet<Order>();
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
	
	public List<Set<Order>> generateOrderSets(Player player, BoardState dynamicState) throws Exception
	{
		int unitCount = player.getNumberUnits(dynamicState);
		System.out.println(unitCount);
		List<Set<Order>> unitMasks = new LinkedList<Set<Order>>();
		Set<TerritorySquare> unit = player.getOccupiedTerritories(dynamicState);
		for(int i = 1; i < Math.pow(2.0,(double)unitCount); i++)
		{
			System.out.println(i);
			unitMasks.add(generateOrderSet(i, unitCount, unit, dynamicState, player));
		}
		return unitMasks;
	}
	
	public static void main(String [] args) throws Exception
	{
		BoardConfiguration staticBoard = null;
		try
		{
			staticBoard = new BoardConfiguration();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		MoveGeneration gen = new MoveGeneration(staticBoard);
		BoardState bst = staticBoard.getInitialState();
		int count = 0;
		for(Set<Order> so: gen.generateOrderSets(staticBoard.getPlayer(Country.RUS), bst))
		{
			count++;
			
			System.out.println(Integer.toBinaryString(count));

			for(Order o: so)
			{
				System.out.println(o.toOrder(bst));
			}
			System.out.println("");
		}
	}
	
}

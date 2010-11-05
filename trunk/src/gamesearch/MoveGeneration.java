package gamesearch;

import heuristic.Heuristic;
import heuristic.NaiveHeuristic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
	
	private Set<Order> generateOrderSet(int num, int length, Set<TerritorySquare> unit, BoardState dynamicState, Player player)
	{
		StringBuilder str = new StringBuilder();
		Object [] unitList = unit.toArray();
		
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
			//Means we will be giving a move order
			if(c == '1')
			{
				try
				{
					TerritorySquare moveTo = null;
					//TODO right now just selecting the first territory listed.  Need to make it do all territories.
					//If it is not an army we have to do some extra work
					if(!((TerritorySquare)unitList[count]).getOccupier(dynamicState).army)
					{
						Iterator<TerritorySquare> territories = ((TerritorySquare)unitList[count]).getBorders().iterator();
						while(territories.hasNext())
						{
							//Come up with the coast string for the territory
							TerritorySquare tmp = territories.next();
							String coast1 = ((TerritorySquare)unitList[count]).getUnitString(dynamicState);
							if(coast1.contains("NCS"))
							{
								coast1 = "NCS";
							}
							else if(coast1.contains("SCS"))
							{
								coast1 = "SCS";
							}
							else
							{
								coast1 = "NA";
							}
							
							String coast2 = tmp.getUnitString(dynamicState);
							if(coast2.contains("NCS"))
							{
								coast2 = "NCS";
							}
							else if(coast2.contains("SCS"))
							{
								coast2 = "SCS";
							}
							else
							{
								coast2 = "NA";
							}
							System.out.println(coast1);
							System.out.println(coast2);
							System.out.println(((TerritorySquare)unitList[count]));
							System.out.println(tmp);
							//Check to see if they share a sea border.  If not do not create that move
							if(((TerritorySquare)unitList[count]).isSeaBorder(tmp,
									coast1, coast2))
							{
								ord = new Move(dynamicState, player, ((TerritorySquare)unitList[count]), tmp);
							}
						}
					}
					else
					{
						ord = new Move(dynamicState, player, ((TerritorySquare)unitList[count]),
							((TerritorySquare)unitList[count]).getBorders().iterator().next());
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				//Add order to correct lists
				moveSet.add(ord);
				Double score = new Double(heuristic.orderScore(ord, dynamicState));
				moveScoreMap.put(score, ord);
			}
			//If not a direct order put in supportSet for later processing
			else
			{
				supportSet.add(((TerritorySquare)unitList[count]));
			}
			count++;
		}
		//Sort heuristic scores and now lets step through remaining units and try to support our moves
		System.out.println(moveSet.firstElement());
		Object [] orderScore = (moveScoreMap.keySet().toArray());
		Arrays.sort(orderScore);
		Vector<TerritorySquare> supportSetCopy = new Vector<TerritorySquare>(supportSet);
		for(Object heurVal: orderScore)
		{
			for(TerritorySquare support: supportSetCopy)
			{
				//Do we border where a move is trying to go
				if(support.getBorders().contains(((Move)moveScoreMap.get((Double)heurVal)).to))
				{
					ord = null;
					try
					{
						ord = new SupportMove(dynamicState, player, support, support, ((Move)moveScoreMap.get((Double)heurVal)).to);
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
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
	
	public Set<Set<Order>> generateOrderSets(Player player, BoardState dynamicState)
	{
		int unitCount = player.getNumberUnits(dynamicState);
		System.out.println(unitCount);
		Set<Set<Order>> unitMasks = new HashSet<Set<Order>>();
		Set<TerritorySquare> unit = player.getOccupiedTerritories(dynamicState);
		for(int i = 1; i < Math.pow(2.0,(double)unitCount); i++)
		{
			System.out.println(i);
			unitMasks.add(generateOrderSet(i, unitCount, unit, dynamicState, player));
		}
		return unitMasks;
	}
	
	public static void main(String [] args)
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
			System.out.println(count);
			count++;
			for(Order o: so)
			{
				System.out.println(o.toOrder(bst));
			}
			System.out.println("");
		}
	}
	
}

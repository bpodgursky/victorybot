package heuristic;

import java.util.Collection;
import java.util.Random;

import order.Order;
import order.spring_fall.Move;

import representation.Player;
import state.constant.BoardConfiguration;
import state.dynamic.BoardState;

public class NaiveHeuristic extends Heuristic {

	BoardConfiguration staticBoard;
	
	public NaiveHeuristic(BoardConfiguration staticBoard)
	{
		super(staticBoard);
		this.staticBoard = staticBoard;
	}
	
	public double orderScore(Order ord, BoardState bst)
	{
		if(ord.getClass() == Move.class)
		{
			Move move = (Move)ord;
			if(move.to.isSupplyCenter())
			{
				return 2.0;
			}
			else
			{
				Random rand = new Random();
				return rand.nextDouble();
			}
		}
		
		return 0.0;
	}
	
	public double boardScore(Player player, BoardState dynamicBoard)
	{
		int mySupplyCenterCount = player.getNumberSupplyCenters(dynamicBoard);
		Collection<Player> players = staticBoard.getPlayers();
		players.remove(player);
		int opponentSupplyCenterMax = ((Player) (players.toArray())[0]).getNumberSupplyCenters(dynamicBoard);
		for(Player opPlayer: players)
		{
			int tmpCount = opPlayer.getNumberSupplyCenters(dynamicBoard);
			if(tmpCount > opponentSupplyCenterMax)
			{
				opponentSupplyCenterMax = tmpCount;
			}
		}
		
		double score = mySupplyCenterCount - opponentSupplyCenterMax;
		return score;
	}
	
	public static String makeBinary(int num, int power)
	{
		StringBuilder str = new StringBuilder();
		if(num == 0)
		{
			return "0";
		}
		
		int count = 0;
		
		while(num >= 1)
		{
			count++;
			if(num%2 == 1)
			{
				str.append("1");
			}
			else
			{
				str.append("0");
			}
			num = num / 2;
		}
		
		for(int i = count; i < power; i++)
		{
			str.insert(0, "0");
		}
		
		return str.toString();
	}
	
	public static void main(String [] args)
	{
		for(int i = 0; i < Math.pow(2, 7); i++)
		{
			System.out.println(NaiveHeuristic.makeBinary(i, 7));
		}
	}
}

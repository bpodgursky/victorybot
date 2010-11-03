package gamesearch;

import java.util.HashSet;
import java.util.Set;

import representation.Player;
import representation.Unit;
import state.constant.BoardConfiguration;
import state.dynamic.BoardState;

public class MoveGeneration {

	private BoardConfiguration staticBoard;
	
	public MoveGeneration(BoardConfiguration sBoard)
	{
		staticBoard = sBoard;
	}
	
	private char [] makeBinary(int num, int length)
	{
		StringBuilder str = new StringBuilder();
		if(num == 0)
		{
			return "0".toCharArray();
		}
		
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
		
		return str.toString().toCharArray();
	}
	
	public Set<Set<Unit>> generateOrderSets(Player player, BoardState dynamicState)
	{
		int unitCount = player.getNumberUnits(dynamicState);
		Set<char []> unitMasks = new HashSet<char []>();
		for(int i = 1; i < unitCount; i++)
		{
			unitMasks.add(makeBinary(i, unitCount));
		}
		return null;
	}
	
}

package gamesearch;

import heuristic.Heuristic;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ai.Bot;


import order.Order;
import order.Order.OrderValue;
import order.Order.RetreatState;
import order.Order.MovesValue;
import order.Order.Result;
import order.builds.Build;
import order.builds.Remove;
import order.builds.Waive;
import order.retreats.Disband;
import order.retreats.Retreat;
import order.spring_fall.Hold;
import order.spring_fall.Move;
import order.spring_fall.SupportHold;
import order.spring_fall.SupportMove;
import representation.Country;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.constant.BoardConfiguration;
import state.constant.BoardConfiguration.TerritoryCoast;
import state.dynamic.BoardState;
import state.dynamic.BoardState.Phase;
import state.dynamic.BoardState.RetreatSituation;

public class MoveGenerator {

	private Heuristic heuristic;
		
	public MoveGenerator(BoardConfiguration sBoard, Heuristic h)
	{
		heuristic = h;
	}
	
	public MovesValue[] generateOrderSets(Player player, BoardState dynamicState) throws Exception
	{

		List<Collection<Order>> allCombinations = heuristic.orderGenerator.generateOrderSets(player, dynamicState);
		
		if(allCombinations.size() == 0){
			throw new Exception("no orders generated for a player "+player.getName()+" in "+dynamicState.time);
		}
		
		MovesValue[] prunedMoves = heuristic.movePruning.getPrunedMoves(player, allCombinations, dynamicState);
		
		if(Bot.DEBUG){
			
			if(Bot.LOGGING) System.out.println("Reasonable sets of moves for player "+player.getName()+":");
			for(MovesValue mv: prunedMoves){
				if(Bot.LOGGING) System.out.println(mv.value);
				for(Order ord: mv.moves){
					if(Bot.LOGGING) System.out.println("\t\t"+ord.toOrder(dynamicState)+"\t"+ord);
				}
			}
		}
		
		return prunedMoves;
	}
	
	double resolveTime = 0;
	double count = 0;

}

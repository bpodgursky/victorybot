package heuristic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import order.Order;
import order.Order.MovesValue;
import order.spring_fall.Move;
import representation.Player;
import state.constant.BoardConfiguration;
import state.dynamic.BoardState;
import state.dynamic.BoardState.Phase;

public class Heuristic {
	
	public final BoardConfiguration staticBoard;
	
	public static abstract class RelevanceHeuristic{
		protected final Heuristic heuristic;
		
		public RelevanceHeuristic(Heuristic baseHeuristic){
			this.heuristic = baseHeuristic;
		}
		
		//	which players are relevant
		public abstract Collection<Player> getRelevantPlayers(BoardState bst, Player p) throws Exception;
	}
	
	public static abstract class MoveEnumeration{
		protected final Heuristic heuristic;
		
		public MoveEnumeration(Heuristic baseHeuristic){
			this.heuristic = baseHeuristic;
		}
		
		public List<Collection<Order>> generateOrderSets(Player player, BoardState dynamicState) throws Exception{
			List<Collection<Order>> allCombinations = new LinkedList<Collection<Order>>();
			
			if(dynamicState.time.phase == Phase.SPR || dynamicState.time.phase == Phase.FAL){
				allCombinations.addAll(heuristic.orderGenerator.generateMoveSets(player, dynamicState));
			}
			else if(dynamicState.time.phase == Phase.SUM || dynamicState.time.phase == Phase.AUT){
				allCombinations.addAll(heuristic.orderGenerator.generateRetreatSets(player, dynamicState));
			}
			else if(dynamicState.time.phase == Phase.WIN){
				allCombinations.addAll(heuristic.orderGenerator.generateBuildSets(player, dynamicState));
			}
			
			return allCombinations;
		}
	
		//	Generates a set of moves for a player
		protected abstract Collection<Collection<Order>> generateMoveSets(Player player, BoardState dynamicState) throws Exception;
		
		protected abstract Collection<Collection<Order>> generateRetreatSets(Player p, BoardState dynamicState) throws Exception;
		
		protected abstract Collection<Collection<Order>> generateBuildSets(Player p, BoardState dynamicState) throws Exception;
	}
	
	public static abstract class MovePruningHeuristic{
		protected final Heuristic heuristic;
		
		public MovePruningHeuristic(Heuristic baseHeuristic){
			this.heuristic = baseHeuristic;
		}

		public abstract MovesValue[] getPrunedMoves(Player player, List<Collection<Order>> allCombinations, BoardState dynamicState) throws Exception;	
	}
	
	public static abstract class ScoreHeuristic{
		protected final Heuristic heuristic;
		
		public ScoreHeuristic(Heuristic baseHeuristic){
			this.heuristic = baseHeuristic;
		}
		
		//Returns a double that is the score of a board for a player
		public abstract double boardScore(Player player, BoardState dynamicBoard) throws Exception;

		//Returns a double that is the score of a single order in some board configuration
		public abstract double orderScore(BoardState dynamicBoard, Collection<Order> successfulOrders, Player player) throws Exception;

	}
	
	public RelevanceHeuristic relevance;
	public MoveEnumeration orderGenerator;
	public MovePruningHeuristic movePruning;
	public ScoreHeuristic scorer;
	
	public void setRelevanceHeuristic(RelevanceHeuristic relevance){
		this.relevance = relevance;
	}
	
	public void setOrderGenerationHeuristic(MoveEnumeration orderGenerator){
		this.orderGenerator = orderGenerator;
	}
	
	public void setMovePruningHeuristic(MovePruningHeuristic movePruning){
		this.movePruning = movePruning;
	}
	
	public void setScoreHeuristic(ScoreHeuristic scorer){
		this.scorer = scorer;
	}
	
	public Heuristic(BoardConfiguration config){
		this.staticBoard = config;
	}
}

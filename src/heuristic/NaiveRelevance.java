package heuristic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import order.Order;
import order.spring_fall.Convoy;
import order.spring_fall.Hold;
import order.spring_fall.Move;
import order.spring_fall.MoveByConvoy;
import order.spring_fall.SupportHold;
import order.spring_fall.SupportMove;

import representation.Player;
import representation.TerritorySquare;
import state.dynamic.BoardState;
import state.dynamic.MoveHistory.OrderSet;
import heuristic.Heuristic.RelevanceHeuristic;

public class NaiveRelevance extends RelevanceHeuristic{

	public NaiveRelevance(Heuristic baseHeuristic) {
		super(baseHeuristic);
	}

	private static final int SOMEWHAT_RELEVANT = 0;
	private static final int VERY_RELEVANT = 1;
	
	private static final int RELEVANCE_SEARCH = SOMEWHAT_RELEVANT;
	
	@Override
	public Collection<Player> getRelevantPlayers(BoardState bst, Player p){
		
		if(RELEVANCE_SEARCH == SOMEWHAT_RELEVANT){
			return getAllRelevantPlayers(bst, p);
		}else if(RELEVANCE_SEARCH == VERY_RELEVANT){
			return getVeryRelevantPlayers(bst, p);
		}
		
		//	it's whining
		return null;
	}
	
	//	a relevant player is defined as a player whose units could take your 
	//	supply centers, or which conflicted with you last turn
	private Collection<Player> getVeryRelevantPlayers(BoardState bst, Player p){
		
		if(bst.getRelevantPlayers(p) != null){
			return bst.getRelevantPlayers(p);
		}
		
		Set<Player> foundPlayers = new HashSet<Player>();
		
		foundPlayers.add(p);
		
		//	those in squares bordering your possessions
		for(TerritorySquare tsquare: p.getControlledTerritories(bst)){
			//relevantTerritories.add(tsquare);
			for(TerritorySquare sqr:tsquare.getBorders()){
				if(sqr.getOccupier(bst) != null){
					foundPlayers.add(sqr.getOccupier(bst).belongsTo);
				}
			}
		}
		
		OrderSet lastMoves = bst.getHistory().getLastMoves();
		
		Set<Order> ordersForPlayer = lastMoves.getOrdersForPlayer(p);
		
		//	if this is null, it's the first turn.  Fall back to this one
		if(ordersForPlayer == null){
			return getAllRelevantPlayers(bst, p);
		}
		
		Set<TerritorySquare> lastContested = new HashSet<TerritorySquare>();
		for(Order ord: ordersForPlayer){
			if(ord.getClass() == Move.class){
				lastContested.add(((Move)ord).to);
			}else if(ord.getClass() == Hold.class){
				lastContested.add(((Hold)ord).holdingSquare);
			}else if(ord.getClass() == SupportHold.class){
				lastContested.add(((SupportHold)ord).supportFrom);
			}else if(ord.getClass() == SupportMove.class){
				lastContested.add(((SupportMove)ord).supportFrom);
			}else if(ord.getClass() == Convoy.class){
				lastContested.add(((Convoy)ord).convoyer);
			}else if(ord.getClass() == MoveByConvoy.class){
				lastContested.add(((MoveByConvoy)ord).convoyDestination);
			}
		}
		
		for(TerritorySquare sqr: lastContested){
			Set<Order> movesTo = lastMoves.getOrdersTo(sqr);
			
			if(movesTo != null){
				for(Order ord: movesTo){
					foundPlayers.add(ord.player);
				}
			}
		}
		
		return foundPlayers;
	}
	
	//	a relevant player is defined as a player whose units could tactically
	//	affect the success of yours in a turn.  That is, they border any of the 
	//	territories you border
	private Collection<Player> getAllRelevantPlayers(BoardState bst, Player p){
		
		if(bst.getRelevantPlayers(p) != null){
			return bst.getRelevantPlayers(p);
		}
		
		Set<Player> foundPlayers = new HashSet<Player>();
		Set<TerritorySquare> relevantTerritories = new HashSet<TerritorySquare>();
		
		foundPlayers.add(p);
		
		//	first layer: those in squares bordering your possessions
		for(TerritorySquare tsquare: p.getControlledTerritories(bst)){
			relevantTerritories.add(tsquare);
			relevantTerritories.addAll(tsquare.getBorders());
		}
		for(TerritorySquare tsquare: p.getOccupiedTerritories(bst)){
			relevantTerritories.add(tsquare);
			relevantTerritories.addAll(tsquare.getBorders());
		}
		
		//	need to worry about one more layer out, because those will be
		//	moving into your squares
		
		Set<TerritorySquare> affectingTerritories = new HashSet<TerritorySquare>();
		
		for(TerritorySquare tsquare: relevantTerritories){
			
			affectingTerritories.add(tsquare);
			affectingTerritories.addAll(tsquare.getBorders());
		}
		
		//	in theory we eventually need to worry about one more layer out,
		//	because these could cut the support of those moving into these...
		//	only really relevant when we have allies
		
		for(TerritorySquare tsquare: affectingTerritories){
			
			if(tsquare.getOccupier(bst) != null){
				foundPlayers.add(tsquare.getOccupier(bst).belongsTo);
			}
			
			if(tsquare.getController(bst) != null){
				foundPlayers.add(tsquare.getController(bst));
			}
		}
		
		bst.setRelevantPlayers(p, foundPlayers);
		
		return foundPlayers;
	}
	
}

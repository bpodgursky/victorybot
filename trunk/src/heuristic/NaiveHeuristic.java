package heuristic;

import java.util.Collection;
import java.util.HashSet;

import order.Order;
import order.spring_fall.Convoy;
import order.spring_fall.Hold;
import order.spring_fall.Move;
import order.spring_fall.MoveByConvoy;
import order.spring_fall.SupportHold;
import order.spring_fall.SupportMove;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
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
		Player orderFor = ord.player;
		
		if(ord.getClass() == Move.class){
			Move mov = (Move)ord;
			
			//	moves are cool
			if(mov.to.isSupplyCenter()){
				
				//	they're really cool if they're to a supply center
				//	we don't control or don't occupy
				if(mov.to.getController(bst) != orderFor ||
						mov.to.getOccupier(bst) != null &&
						mov.to.getOccupier(bst).belongsTo != orderFor){
				
					return 3.0;
					
				}
				
				else{
					
					//	defense?  who cares about that?
					for(TerritorySquare neighbor: mov.to.getBorders()){
						if(neighbor.getOccupier(bst) != null &&
								neighbor.getOccupier(bst).belongsTo != orderFor){
							
							return .9;
						}
					}
					
					//	meh otherwise
					return 1.0;
				}
			}else{
				
				//TODO be smarter here
				
				return 1.0;
			}
			
		}else if(ord.getClass() == MoveByConvoy.class){
			MoveByConvoy mov = (MoveByConvoy)ord;
			
			//	moves are cool
			if(mov.convoyDestination.isSupplyCenter()){
				
				//	they're really cool if they're to a supply center
				//	we don't control or don't occupy
				if(mov.convoyDestination.getController(bst) != orderFor ||
						mov.convoyDestination.getOccupier(bst) != null &&
						mov.convoyDestination.getOccupier(bst).belongsTo != orderFor){
				
					return 3.0;
					
				}
				
				else{
					
					//	defense is irrelevant				
					for(TerritorySquare neighbor: mov.convoyDestination.getBorders()){
						if(neighbor.getOccupier(bst) != null &&
								neighbor.getOccupier(bst).belongsTo != orderFor){
							
							return .9;
						}
					}
					
					//	meh otherwise
					return 1.0;
				}
			}else{
				
				//TODO be smarter here
				
				return 1.0;
			}	
		}else if(ord.getClass() == Hold.class){
			
			//	holds are pretty lame in general
			
			return 1.0;
			
		}else if(ord.getClass() == SupportHold.class){
			
			//	support holds are ok
			
			return 1.5;
			
		}else if(ord.getClass() == SupportMove.class){
			
			//	progress!
			
			return 2.0;
			
		}else if(ord.getClass() == Convoy.class){
			
			//	not that we can really pull these off yet
		
			return 1.0;
		}

		return 0;
	}
	
	//	naive heuristic for board quality
	public double boardScore(Player player, BoardState dynamicBoard)
	{
		
		double myScore = playerScore(player, dynamicBoard);
		
		Collection<Player> players = new HashSet<Player>(staticBoard.getPlayers());
		players.remove(player);
		
		double sumOtherScores = 0;
		
		for(Player p: staticBoard.getPlayers()){
			if(p != player){
				sumOtherScores+=playerScore(p, dynamicBoard);
			}
		}
		
		//	return the difference between your score and the other players' average
		double score = myScore - sumOtherScores/(staticBoard.getPlayers().size()-1);
		
		return score;
	}
	
	private double playerScore(Player player, BoardState dynamicBoard){

		//	count the supply centers we expect this gives us
		double mySupplyCenterCount = player.getNumberSupplyCenters(dynamicBoard);

		//	add in ones you're on that won't control quite yet
		for(TerritorySquare sqr:player.getOccupiedTerritories(dynamicBoard)){
			if(sqr.isSupplyCenter() && sqr.getController(dynamicBoard) != player){
				mySupplyCenterCount++;
			}
		}

		//	and subtract the ones that others are on
		for(TerritorySquare sqr:player.getControlledTerritories(dynamicBoard)){
			if(sqr.getOccupier(dynamicBoard) != null && sqr.getOccupier(dynamicBoard).belongsTo != player){
				mySupplyCenterCount--;
			}
		}
		
		//	get an idea of how close we are to taking other centers
		double threateningSupplyCenterScore = 0;
		
		//	look everywhere we have units
		for(TerritorySquare sqr: player.getOccupiedTerritories(dynamicBoard)){
			
			//	look at all their neighbors
			for(TerritorySquare neighbor: sqr.getBorders()){
				
				//	if you don't control it yet
				if(neighbor.isSupplyCenter() && neighbor.getController(dynamicBoard) != player){
					
					double threatenScore = 0;
					
					//	if nobody is there, it's worth a lot potentially
					if(neighbor.getOccupier(dynamicBoard) == null){
						threatenScore++;
					}
					
					//slightly less if it's not open
					else if(neighbor.getOccupier(dynamicBoard).belongsTo != player){
						threatenScore+=.5;
					}
					
					//	if it's our home supply center, this is very important
					if(neighbor.getHomePlayer() == player){
						threatenScore*=2;
					}
					
					threateningSupplyCenterScore+=threatenScore;
				}
			}
		}
		
		//	and then see how close we are to losing centers
		double threatenedSupplyCenterScore = 0;
		
		//	look everywhere we control
		for(TerritorySquare sqr: player.getControlledTerritories(dynamicBoard)){
			
			//	look at all their neighbors
			for(TerritorySquare neighbor: sqr.getBorders()){
				
				//	if someone else occupies it
				Unit force = neighbor.getOccupier(dynamicBoard);
				
				if(force != null && force.belongsTo != player){
					
					double threatenScore = 0;
					
					//	if the square is unoccupied, that is bad
					if(sqr.getOccupier(dynamicBoard) == null){
						threatenScore--;
					}
					
					//slightly less if it's not open
					else if(sqr.getOccupier(dynamicBoard).belongsTo == player){
						threatenScore-=.5;
					}
					
					//	if it's our home supply center, this is very bad
					if(neighbor.getHomePlayer() == player){
						threatenScore*=2;
					}
					
					threateningSupplyCenterScore+=threatenScore;
				}
			}
		}
		
		double lostHomeSupplyPenalty = 0;
		
		//	add a penalty for losing our own home supply centers
		for(TerritorySquare homeSupply: player.getHomeCenters()){
			if(homeSupply.getController(dynamicBoard) != player){
				lostHomeSupplyPenalty--;
			}
		}
		
		//	add a bonus for coming out of this with more units (will probably be represented elsewhere,
		//	but oh well.)  Hopefully this will reward plans that don't have retreats or disbands
		double liveUnitBonus = player.getOccupiedTerritories(dynamicBoard).size();
		
		//	take the expected supply center count, and discounted other positions
		return mySupplyCenterCount + 
			.25 * (threateningSupplyCenterScore + threatenedSupplyCenterScore) +
			lostHomeSupplyPenalty +
			liveUnitBonus;
	}
	
	public static void main(String [] args)
	{
		
	}
}

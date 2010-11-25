package heuristic;

import java.util.Collection;

import order.Order;
import order.spring_fall.Move;
import representation.Player;
import representation.TerritorySquare;
import representation.Unit;
import state.dynamic.BoardState;
import heuristic.Heuristic.ScoreHeuristic;

public class NaiveScorer extends ScoreHeuristic{

	public NaiveScorer(Heuristic baseHeuristic) {
		super(baseHeuristic);
	}

	@Override
	public double orderScore(BoardState dynamicBoard, Collection<Order> successfulOrders, Player player)
	{
		
		//	count the supply centers we expect this gives us
		double mySupplyCenterCount = getExpectedSupplyCenters(player, dynamicBoard);
		
		//	and the ones we expect to move into 
		for(Order o: successfulOrders)
		{
			if(o.getClass() == Move.class)
			{
				Move move = (Move)o;
				if(move.to.isSupplyCenter() && move.to.getController(dynamicBoard) != player)
				{
					mySupplyCenterCount++;
				}
			}
		}

		//	get an idea of how close we are to taking other centers
		double threateningSupplyCenterScore = getThreateningSupplyCenterScore(player, dynamicBoard);
		
		//	and losing them
		double threatenedSupplyCenterScore = getThreatenedSupplyCenterScore(player, dynamicBoard);

		double lostHomeSupplyPenalty = getLostHomeSupplyPenalty(player, dynamicBoard);
		
		//	add a bonus for coming out of this with more units (will probably be represented elsewhere,
		//	but oh well.)  Hopefully this will reward plans that don't have retreats or disbands
		double liveUnitBonus = player.getOccupiedTerritories(dynamicBoard).size();
		
		//	take the expected supply center count, and discounted other positions
		return mySupplyCenterCount + 
			.25 * (threateningSupplyCenterScore + threatenedSupplyCenterScore) +
			lostHomeSupplyPenalty +
			liveUnitBonus;
	}
	
	//	naive heuristic for board quality
	public double boardScore(Player player, BoardState dynamicBoard){
		return playerScore(player, dynamicBoard);
	}
	
	private double playerScore(Player player, BoardState dynamicBoard){

		//	count the supply centers we expect this gives us
		double mySupplyCenterCount = getExpectedSupplyCenters(player, dynamicBoard);
		
//		//	TODO this is a bit of a hack to make it play turkey and england better
//		double fleets = 0;
//		double armies = 0;
//		
//		for(TerritorySquare sqr:player.getOccupiedTerritories(dynamicBoard)){
//			if(!sqr.getOccupier(dynamicBoard).army){
//				fleets++;
//			}else{
//				armies++;
//			}
//		}
		
		//	get an idea of how close we are to taking other centers
		double threateningSupplyCenterScore = getThreateningSupplyCenterScore(player, dynamicBoard);
		
		//	and then see how close we are to losing centers
		double threatenedSupplyCenterScore = getThreatenedSupplyCenterScore(player, dynamicBoard);
		
		//	add a penalty for losing our own home supply centers		
		double lostHomeSupplyPenalty = getLostHomeSupplyPenalty(player, dynamicBoard);
		
		//	add a bonus for coming out of this with more units (will probably be represented elsewhere,
		//	but oh well.)  Hopefully this will reward plans that don't have retreats or disbands
		double liveUnitBonus = player.getOccupiedTerritories(dynamicBoard).size();
		
		//	take the expected supply center count, and discounted other positions
		return mySupplyCenterCount + 
			.05 * (threateningSupplyCenterScore + threatenedSupplyCenterScore) +
			lostHomeSupplyPenalty +
			liveUnitBonus;
			//+fleetBonus;
	}
	
	private double getExpectedSupplyCenters(Player player, BoardState dynamicBoard){
		
		double expectedSupply = player.getNumberSupplyCenters(dynamicBoard);

		//	add in ones you're on that won't control quite yet
		for(TerritorySquare sqr:player.getOccupiedTerritories(dynamicBoard)){
			if(sqr.isSupplyCenter() && sqr.getController(dynamicBoard) != player){
				expectedSupply++;
			}
		}

		//	and subtract the ones that others are on
		for(TerritorySquare sqr:player.getControlledTerritories(dynamicBoard)){
			if(sqr.getOccupier(dynamicBoard) != null && sqr.getOccupier(dynamicBoard).belongsTo != player){
				expectedSupply--;
			}
		}
		
		return expectedSupply;
	}
	
	private double getLostHomeSupplyPenalty(Player player, BoardState bst){
		
		double lostHomeSupplyPenalty = 0;
		
		for(TerritorySquare homeSupply: player.getHomeCenters()){
			if(homeSupply.getController(bst) != player){
				lostHomeSupplyPenalty--;
			}
		}
		
		return lostHomeSupplyPenalty;
	}
	
	private double getThreatenedSupplyCenterScore(Player player, BoardState bst){
		
		double threatenedSupplyCenterScore = 0;
		
		//	look everywhere we control
		for(TerritorySquare sqr: player.getControlledTerritories(bst)){
			
			//	look at all their neighbors
			for(TerritorySquare neighbor: sqr.getBorders()){
				
				//	if someone else occupies it
				Unit force = neighbor.getOccupier(bst);
				
				if(force != null && force.belongsTo != player){
					
					double threatenScore = 0;
					
					//	if the square is unoccupied, that is bad
					if(sqr.getOccupier(bst) == null){
						threatenScore--;
					}
					
					//slightly less if it's not open
					else if(sqr.getOccupier(bst).belongsTo == player){
						threatenScore-=.5;
					}
					
					//	if it's our home supply center, this is very bad
					if(neighbor.getHomePlayer() == player){
						threatenScore*=2;
					}
					
					threatenedSupplyCenterScore+=threatenScore;
				}
			}
		}
		
		return threatenedSupplyCenterScore;
	}
	
	private double getThreateningSupplyCenterScore(Player player, BoardState bst){
		//	look everywhere we have units
		
		double threateningSupplyCenterScore = 0;
		for(TerritorySquare sqr: player.getOccupiedTerritories(bst)){
			
			//	look at all their neighbors
			for(TerritorySquare neighbor: sqr.getBorders()){
				
				//	if you don't control it yet
				if(neighbor.isSupplyCenter() && neighbor.getController(bst) != player){
					
					double threatenScore = 0;
					
					//	if nobody is there, it's worth a lot potentially
					if(neighbor.getOccupier(bst) == null){
						threatenScore++;
					}
					
					//slightly less if it's not open
					else if(neighbor.getOccupier(bst).belongsTo != player){
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
		
		return threateningSupplyCenterScore;
	}

}

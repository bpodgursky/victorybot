
public class Unit {
	
	final Player belongsTo;
	final boolean army; //false is fleet

	public Unit(Player belongsTo, boolean isArmy){
		
		this.belongsTo = belongsTo;
		this.army = isArmy;
		
	}
}

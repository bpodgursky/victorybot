package representation;



public class Unit {
	
	public final Player belongsTo;
	public final boolean army; //false is fleet

	public Unit(Player belongsTo, boolean isArmy){
		
		this.belongsTo = belongsTo;
		this.army = isArmy;
	}
	
//	public String toString(){
//		return (army)?"A ":"F "+belongsTo.getName();
//	}
	
	public String toString(){
		return belongsTo.getName()+" "+(army?"AMY":"FLT");
	}
}

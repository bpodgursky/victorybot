package representation;



public class Unit {
	
	public final Player belongsTo;
	public final boolean army; //false is fleet

	public Unit(Player belongsTo, boolean isArmy){
		
		this.belongsTo = belongsTo;
		this.army = isArmy;
	}
	
	public String toString(){
		return belongsTo.getName()+" "+(army?"AMY":"FLT");
	}
	
	public int hashCode2(){
		return belongsTo.hashCode()+(army?1:0);
	}
}

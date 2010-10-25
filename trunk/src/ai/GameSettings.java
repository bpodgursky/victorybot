package ai;

import representation.Country;

public class GameSettings {

	public final Country power;
	public final String password;
	
	public final int lvl;
	public final int mtl;
	public final int rtl;
	public final int btl ;
	
	public final boolean dsd;
	public final boolean aoa;
	
	public GameSettings(Country power, String password, int lvl, int mtl, int rtl, int btl, boolean dsd, boolean aoa){
		
		this.power = power;
		this.password = password;
		
		this.lvl = lvl;
		this.mtl = mtl*1000;
		this.rtl = rtl*1000;
		this.btl = btl*1000;
		
		this.dsd = dsd;
		this.aoa = aoa;

	}
	
	public String toString(){
		String str = "Settings:\n";
		str+=" power = "+power+"\n";
		str+=" pass = "+password+"\n";
		str+=" lvl = "+lvl+"\n";
		str+=" mtl = "+mtl+"\n";
		str+=" rtl = "+rtl+"\n";
		str+=" btl = "+btl+"\n";
		str+=" dsd = "+dsd+"\n";
		str+=" aoa = "+aoa+"\n";
		
		return str;
	}
	
}

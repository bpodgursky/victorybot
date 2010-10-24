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
		this.mtl = mtl;
		this.rtl = rtl;
		this.btl = btl;
		
		this.dsd = dsd;
		this.aoa = aoa;

	}
	
	
}

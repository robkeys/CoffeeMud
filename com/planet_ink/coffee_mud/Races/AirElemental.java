package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class AirElemental extends StdRace
{
	public String ID(){	return "AirElemental"; }
	public String name(){ return "Air Elemental"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 400;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Air Elemental";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+100);
		affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+100);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a swirling gust");
			naturalWeapon.setWeaponType(Weapon.TYPE_GASSING);
		}
		return naturalWeapon;
	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is almost blown away!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is swirling alot and is massively dissipated.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is swirling alot and is heavily dissipated.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is swirling alot and dissipating more and more.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is swirling and starting to dissipate.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is swirling!.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is starting to swirl.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is showing some swirls.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is showing small swirls.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition.^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("a pile of leaves",EnvResource.RESOURCE_GREENS));
			}
		}
		return resources;
	}
}

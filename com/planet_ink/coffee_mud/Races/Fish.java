package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fish extends StdRace
{
	public String ID(){	return "Fish"; }
	public String name(){ return "Fish"; }
	public int shortestMale(){return 2;}
	public int shortestFemale(){return 2;}
	public int heightVariance(){return 3;}
	public int lightestWeight(){return 5;}
	public int weightVariance(){return 15;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_EYES;}
	public String racialCategory(){return "Amphibian";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,0 ,1 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,2 ,1 ,0 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,3);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
		affectableStats.setStat(CharStats.DEXTERITY,7);
	}
	public String arriveStr()
	{
		return "swims in";
	}
	public String leaveStr()
	{
		return "swims";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("nasty stingers");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}

	public boolean isWaterRoom(Room R)
	{
		if(R==null) return false;
		if((R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		||(R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
		||(R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			return true;
		return false;
	}

	public boolean okAffect(Environmental affected, Affect msg)
	{
		if((msg.targetMinor()==Affect.TYP_ENTER)
		&&(msg.amISource((MOB)affected))
		&&(msg.target() instanceof Room)
		&&(msg.tool() instanceof Exit)
		&&(!isWaterRoom((Room)msg.target())))
		{
			((MOB)affected).tell("That way looks too dry.");
			return false;
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		MOB mob=(MOB)affected;
		if(mob.location()!=null)
	    {
			if(isWaterRoom(mob.location()))
			{
				if((affectableStats.sensesMask()&EnvStats.CAN_NOT_BREATHE)==EnvStats.CAN_NOT_BREATHE)
					affectableStats.setSensesMask(affectableStats.sensesMask()-EnvStats.CAN_NOT_BREATHE);
			}
			else
			{
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_BREATHE);
				if((affectableStats.disposition()&EnvStats.IS_SWIMMING)>0)
					affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_SWIMMING);
			}
		}
	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is facing a cold death!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous bloody wounds and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody wounds and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is cut and bruised heavily.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some minor cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few bruises and scratched scales.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a few small bruises.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase(),EnvResource.RESOURCE_FISH));
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" hide",EnvResource.RESOURCE_HIDE));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}

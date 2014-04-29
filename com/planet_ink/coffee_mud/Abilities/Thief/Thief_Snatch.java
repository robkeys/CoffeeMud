package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("rawtypes")
public class Thief_Snatch extends ThiefSkill
{
	@Override public String ID() { return "Thief_Snatch"; }
	@Override public String name(){ return "Weapon Snatch";}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;}
	private static final String[] triggerStrings = {"SNATCH"};
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int usageType(){return USAGE_MOVEMENT;}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			Item weapon=mob.fetchWieldedItem();
			if(weapon==null)
				return Ability.QUALITY_INDIFFERENT;
			if(mob.freeWearPositions(Wearable.WORN_HELD,(short)0,(short)0)>0)
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				MOB targetM=(MOB)target;
				Item hisItem=targetM.fetchWieldedItem();
				if((hisItem==null)
				||(!(hisItem instanceof Weapon))
				||((((Weapon)hisItem).weaponClassification()==Weapon.CLASS_NATURAL)))
					return Ability.QUALITY_INDIFFERENT;
				if(hisItem.rawLogicalAnd())
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=(auto&&(givenTarget instanceof MOB))?(MOB)givenTarget:mob.getVictim();
		if((!mob.isInCombat())||(target==null))
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to disarm!");
			return false;
		}
		Item weapon=mob.fetchWieldedItem();
		if(weapon==null)
		{
			mob.tell("You need a weapon to disarm someone!");
			return false;
		}
		else
		if(mob.freeWearPositions(Wearable.WORN_HELD,(short)0,(short)0)>0)
		{
			mob.tell("Your other hand needs to be free to do a weapon snatch.");
			return false;
		}

		Item hisItem=target.fetchWieldedItem();
		if((hisItem==null)
		||(!(hisItem instanceof Weapon))
		||((((Weapon)hisItem).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			mob.tell(target.charStats().HeShe()+" is not wielding a weapon!");
			return false;
		}
		else
		if(hisItem.rawLogicalAnd())
		{
			mob.tell("You can't snatch a two-handed weapon!");
			return false;
		}
		Weapon hisWeapon=(Weapon)hisItem;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(getXLEVELLevel(mob)*2));
		if(levelDiff>0)
			levelDiff=levelDiff*6;
		else
			levelDiff=0;
		boolean hit=(auto)||CMLib.combat().rollToHit(mob,target);
		boolean success=proficiencyCheck(mob,-levelDiff,auto)&&(hit);
		if((success)
		&&((hisWeapon.rawProperLocationBitmap()==Wearable.WORN_WIELD)
			||(hisWeapon.rawProperLocationBitmap()==Wearable.WORN_WIELD+Wearable.WORN_HELD)))
		{
			CMMsg msg=CMClass.getMsg(target,hisWeapon,null,CMMsg.MSG_DROP,null);
			CMMsg msg2=CMClass.getMsg(mob,null,this,CMMsg.MSG_THIEF_ACT,null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(target,msg);
				mob.location().send(mob,msg2);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<S-NAME> disarm(s) <T-NAMESELF>!");
				if(mob.location().isContent(hisWeapon))
				{
					CMLib.commands().postGet(mob,null,hisWeapon,true);
					if(mob.isMine(hisWeapon))
					{
						msg=CMClass.getMsg(mob,hisWeapon,null,CMMsg.MSG_HOLD,"<S-NAME> snatch(es) the <T-NAME> out of mid-air!");
						if(mob.location().okMessage(mob,msg))
							mob.location().send(mob,msg);
					}
				}
			}
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to disarm <T-NAMESELF> and fail(s)!");
		return success;
	}
}

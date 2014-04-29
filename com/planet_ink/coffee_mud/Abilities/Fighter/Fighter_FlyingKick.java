package com.planet_ink.coffee_mud.Abilities.Fighter;
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
public class Fighter_FlyingKick extends FighterSkill
{
	@Override public String ID() { return "Fighter_FlyingKick"; }
	@Override public String name(){ return "Flying Kick";}
	private static final String[] triggerStrings = {"FLYINGKICK","FLYKICK"};
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return Ability.CAN_MOBS;}
	@Override public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_KICKING;}
	@Override public int minRange(){return 1;}
	@Override public int maxRange(){return adjustedMaxInvokerRange(5);}
	@Override public int usageType(){return USAGE_MOVEMENT;}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()==0))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.charStats().getBodyPart(Race.BODY_LEG)<=1)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()==0))
		{
			mob.tell("You are too close to do a flying kick!");
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_LEG)<=1)
		{
			mob.tell("You need at least two legs to do this.");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,(mob.charStats().getStat(CharStats.STAT_DEXTERITY)-target.charStats().getStat(CharStats.STAT_DEXTERITY))*2,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			int topDamage=adjustedLevel(mob,asLevel)+20;
			int damage=CMLib.dice().roll(1,topDamage,0);
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()>0)
					damage = (int)Math.round(CMath.div(damage,2.0));
				CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,"^F^<FIGHT^><S-NAME> <DAMAGE> <T-NAME> with a flying KICK!^</FIGHT^>^?"+CMLib.protocol().msp("bashed1.wav",30));
				if(mob.getVictim()==target)
				{
					mob.setAtRange(0);
					target.setAtRange(0);
				}
				if(mob.getVictim()==null) mob.setVictim(null); // correct range
				if(target.getVictim()==null) target.setVictim(null); // correct range
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> fail(s) to land a flying kick on <T-NAMESELF>.");

		// return whether it worked
		return success;
	}
}

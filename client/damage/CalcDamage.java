package client.damage;

import client.MapleCharacter;
import client.SkillFactory;
import handling.channel.handler.AttackInfo;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import server.SecondaryStatEffect;
import server.life.MapleMonster;
import tools.AttackPair;
import tools.Pair;

public class CalcDamage {
   CRand32 rndGenForCharacter = new CRand32();
   int invalidCount = 0;
   private int numRand = 11;

   public void SetSeed(int seed1, int seed2, int seed3) {
      this.rndGenForCharacter.Seed(seed1, seed2, seed3);
   }

   public List<Pair<Long, Boolean>> PDamage(MapleCharacter chr, AttackInfo attack) {
      List<Pair<Long, Boolean>> realDamageList = new ArrayList();
      Iterator var4 = attack.allDamage.iterator();

      while(var4.hasNext()) {
         AttackPair eachMob = (AttackPair)var4.next();
         MapleMonster monster = chr.getMap().getMonsterByOid(eachMob.objectId);
         long[] rand = new long[this.numRand];

         for(int i = 0; i < this.numRand; ++i) {
            rand[i] = this.rndGenForCharacter.Random();
         }

         byte index = 0;
         Iterator var9 = eachMob.attack.iterator();

         while(var9.hasNext()) {
            Pair<Long, Boolean> att = (Pair)var9.next();
            double realDamage = 0.0D;
            boolean critical = false;
            ++index;
            ++index;
            long var10000 = rand[index % this.numRand];
            long maxDamage = 38L;
            long minDamage = 8L;
            ++index;
            double adjustedRandomDamage = this.RandomInRange(rand[index % this.numRand], maxDamage, minDamage);
            realDamage += adjustedRandomDamage;
            if (monster == null) {
               chr.dropMessageGM(6, "monster null");
            } else if (monster.getStats() == null) {
               chr.dropMessageGM(6, "stat null");
            } else {
               SecondaryStatEffect skillEffect = null;
               if (attack.skill > 0) {
                  skillEffect = SkillFactory.getSkill(attack.skill).getEffect(chr.getTotalSkillLevel(attack.skill));
               }

               if (skillEffect != null) {
                  chr.dropMessageGM(6, "skillDamage : " + skillEffect.getDamage());
                  realDamage = realDamage * (double)skillEffect.getDamage() / 100.0D;
               }

               ++index;
               if (this.RandomInRange(rand[index % this.numRand], 100L, 0L) < (double)chr.getStat().critical_rate) {
                  critical = true;
                  int maxCritDamage = chr.getStat().critical_damage;
                  ++index;
                  int criticalDamageRate = (int)this.RandomInRange(rand[index % this.numRand], (long)maxCritDamage, (long)maxCritDamage);
                  realDamage += (double)criticalDamageRate / 100.0D * (double)((int)realDamage);
               }

               realDamageList.add(new Pair((long)realDamage, critical));
            }
         }
      }

      return realDamageList;
   }

   public double RandomInRange(long randomNum, long maxDamage, long minDamage) {
      BigInteger ECX = new BigInteger(randomNum.makeConcatWithConstants<invokedynamic>(randomNum));
      BigInteger EAX = new BigInteger("1801439851");
      BigInteger multipled = ECX.multiply(EAX);
      long highBit = multipled.shiftRight(32).longValue();
      long rightShift = highBit >>> 22;
      double newRandNum = (double)randomNum - (double)rightShift * 1.0E7D;
      double value;
      if (minDamage != maxDamage) {
         if (minDamage > maxDamage) {
            long temp = maxDamage;
            maxDamage = minDamage;
            minDamage = temp;
         }

         value = (double)(maxDamage - minDamage) * newRandNum / 9999999.0D + (double)minDamage;
      } else {
         value = (double)maxDamage;
      }

      return value;
   }
}

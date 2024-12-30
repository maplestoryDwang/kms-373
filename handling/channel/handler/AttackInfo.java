package handling.channel.handler;

import client.MapleCharacter;
import client.Skill;
import client.SkillFactory;
import constants.GameConstants;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import server.SecondaryStatEffect;
import tools.AttackPair;
import tools.Pair;

public class AttackInfo {
   public int skill;
   public int charge = 0;
   public int lastAttackTickCount;
   public List<AttackPair> allDamage;
   public Point position;
   public Point chain;
   public Point plusPosition = new Point();
   public Point plusPosition2;
   public Point plusPosition3;
   public Point attackPosition;
   public Point attackPosition2;
   public Point attackPosition3;
   public Rectangle acrossPosition;
   public int display = 0;
   public int facingleft = 0;
   public int count = 0;
   public int subAttackType;
   public int subAttackUnk;
   public byte hits;
   public byte targets;
   public byte tbyte = 0;
   public byte speed = 0;
   public byte animation;
   public byte plusPos;
   public short AOE;
   public short slot;
   public short csstar;
   public boolean real = true;
   public boolean across = false;
   public boolean Aiming = false;
   public byte attacktype = 0;
   public boolean isLink = false;
   public byte isBuckShot = 0;
   public byte isShadowPartner = 0;
   public byte nMoveAction = -1;
   public byte rlType;
   public byte bShowFixedDamage = 0;
   public int item;
   public int skilllevel = 0;
   public int asist;
   public int summonattack;
   public List<Point> mistPoints = new ArrayList();
   public List<Pair<Integer, Integer>> attackObjects = new ArrayList();

   public final SecondaryStatEffect getAttackEffect(MapleCharacter chr, int skillLevel, Skill skill_) {
      if (GameConstants.isLinkedSkill(this.skill)) {
         Skill skillLink = SkillFactory.getSkill(GameConstants.getLinkedSkill(this.skill));
         return skillLink.getEffect(skillLevel);
      } else {
         return skill_.getEffect(skillLevel);
      }
   }
}

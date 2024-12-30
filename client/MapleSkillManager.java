package client;

public class MapleSkillManager {
   public static boolean isKhaliVoydSkills(int skillId) {
      switch(skillId) {
      case 154121003:
      case 154121009:
      case 154121011:
         return true;
      default:
         return false;
      }
   }

   public static boolean isKhaliHexSkills(int skillId) {
      switch(skillId) {
      case 154111006:
      case 154120031:
      case 154120032:
      case 154120033:
      case 154121001:
      case 154121002:
      case 400041082:
      case 400041083:
         return true;
      default:
         return false;
      }
   }

   public static boolean isKhaliAttackSkills(int skillId) {
      switch(skillId) {
      case 154001000:
      case 154101000:
      case 154111002:
      case 154121000:
         return true;
      default:
         return false;
      }
   }

   public static boolean isUnstableMemorizeSkills(int skillId) {
      switch(skillId) {
      case 2111013:
      case 2121003:
      case 2121004:
      case 2121006:
      case 2121007:
      case 2121011:
      case 2211011:
      case 2221004:
      case 2221006:
      case 2221007:
      case 2221011:
      case 2311011:
      case 2311012:
      case 2321001:
      case 2321004:
      case 2321007:
      case 2321008:
         return true;
      default:
         return false;
      }
   }
}

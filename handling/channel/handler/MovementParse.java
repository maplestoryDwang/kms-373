package handling.channel.handler;

import client.MapleCharacter;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import server.maps.AnimatedMapleMapObject;
import server.movement.AbsoluteLifeMovement;
import server.movement.AranMovement;
import server.movement.ChairMovement;
import server.movement.ChangeEquipSpecialAwesome;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import server.movement.RelativeLifeMovement;
import server.movement.SunknownMovement;
import server.movement.TunknownMovement;
import server.movement.UnknownMovement;
import server.movement.UnknownMovement2;
import server.movement.UnknownMovement3;
import server.movement.UnknownMovement4;
import tools.data.LittleEndianAccessor;

public class MovementParse {
   public static List<LifeMovementFragment> parseMovement(LittleEndianAccessor lea, int kind) {
      return parseMovement(lea, kind, (MapleCharacter)null);
   }

   public static List<LifeMovementFragment> parseMovement(LittleEndianAccessor rh, int kind, MapleCharacter chr) {
      List<LifeMovementFragment> res = new ArrayList();
      byte numCommands = rh.readByte();
      rh.readByte();

      for(byte i = 0; i < numCommands; ++i) {
         byte command = rh.readByte();
         short nAttr = 0;
         short xpos;
         short xmod;
         short ymod;
         short ywobble;
         short duration;
         byte unk;
         short yoffset;
         byte unk;
         byte newstate;
         int nnow;
         byte newstate;
         byte unk;
         AranMovement um;
         byte newstate;
         UnknownMovement3 um;
         byte newstate;
         short xoffset;
         byte unk;
         switch(command) {
         case 0:
         case 8:
         case 15:
         case 17:
         case 19:
         case 69:
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 91:
         case 105:
            xpos = rh.readShort();
            xmod = rh.readShort();
            ymod = rh.readShort();
            ywobble = rh.readShort();
            duration = rh.readShort();
            if (command == 15 || command == 17) {
               nAttr = rh.readShort();
            }

            xoffset = rh.readShort();
            yoffset = rh.readShort();
            short v307 = rh.readShort();
            byte newstate = rh.readByte();
            short duration = rh.readShort();
            byte unk = rh.readByte();
            AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, xmod), duration, newstate, duration, unk);
            alm.setV307(v307);
            alm.setnAttr(nAttr);
            alm.setPixelsPerSecond(new Point(ymod, ywobble));
            alm.setOffset(new Point(xoffset, yoffset));
            res.add(alm);
            break;
         case 1:
         case 2:
         case 18:
         case 21:
         case 22:
         case 24:
         case 60:
         case 62:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 96:
            Point v307 = null;
            xmod = rh.readShort();
            ymod = rh.readShort();
            if (command == 21 || command == 22) {
               nAttr = rh.readShort();
            }

            if (command == 60) {
               v307 = rh.readPos();
            }

            newstate = rh.readByte();
            duration = rh.readShort();
            unk = rh.readByte();
            RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xmod, ymod), duration, newstate, unk);
            rlm.setAttr(nAttr);
            rlm.setV307(v307);
            res.add(rlm);
            break;
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 9:
         case 10:
         case 11:
         case 13:
         case 26:
         case 27:
         case 52:
         case 53:
         case 54:
         case 80:
         case 81:
         case 82:
         case 84:
         case 86:
            xpos = rh.readShort();
            xmod = rh.readShort();
            ymod = rh.readShort();
            int now = rh.readInt();
            newstate = rh.readByte();
            xoffset = rh.readShort();
            unk = rh.readByte();
            ChairMovement cm = new ChairMovement(command, new Point(xpos, xmod), xoffset, newstate, ymod, unk);
            cm.setUnk(now);
            res.add(cm);
            break;
         case 12:
            res.add(new ChangeEquipSpecialAwesome(command, rh.readByte()));
            break;
         case 14:
         case 16:
            xpos = rh.readShort();
            xmod = rh.readShort();
            nAttr = rh.readShort();
            unk = rh.readByte();
            ywobble = rh.readShort();
            newstate = rh.readByte();
            SunknownMovement sum = new SunknownMovement(command, new Point(xpos, xmod), ywobble, unk, newstate);
            sum.setAttr(nAttr);
            res.add(sum);
            break;
         case 20:
         case 25:
         case 77:
         case 79:
         default:
            if (command != 78 && command != 80) {
               newstate = rh.readByte();
               xmod = rh.readShort();
               unk = rh.readByte();
               um = new AranMovement(command, new Point(0, 0), newstate, xmod, unk);
               res.add(um);
               break;
            }

            xpos = rh.readShort();
            xmod = rh.readShort();
            ymod = rh.readShort();
            ywobble = rh.readShort();
            duration = rh.readShort();
            xoffset = rh.readShort();
            yoffset = rh.readShort();
            UnknownMovement4 alm = new UnknownMovement4(command, new Point(xpos, xmod), 0, 0, duration, (byte)0);
            alm.setPixelsPerSecond(new Point(ymod, ywobble));
            alm.setOffset(new Point(xoffset, yoffset));
            res.add(alm);
            break;
         case 23:
         case 99:
         case 100:
            xpos = rh.readShort();
            xmod = rh.readShort();
            ymod = rh.readShort();
            ywobble = rh.readShort();
            newstate = rh.readByte();
            xoffset = rh.readShort();
            unk = rh.readByte();
            TunknownMovement tum = new TunknownMovement(command, new Point(xpos, xmod), xoffset, newstate, unk);
            tum.setOffset(new Point(ymod, ywobble));
            res.add(tum);
            break;
         case 28:
            nnow = rh.readInt();
            newstate = rh.readByte();
            ymod = rh.readShort();
            newstate = rh.readByte();
            um = new UnknownMovement3(command, new Point(0, 0), ymod, newstate, (short)0, newstate);
            um.setUnow(nnow);
            res.add(um);
            break;
         case 29:
         case 41:
            nnow = rh.readInt();
            newstate = rh.readByte();
            ymod = rh.readShort();
            newstate = rh.readByte();
            um = new UnknownMovement3(command, new Point(0, 0), ymod, newstate, (short)0, newstate);
            um.setUnow(nnow);
            res.add(um);
            break;
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 50:
         case 51:
         case 55:
         case 57:
         case 58:
         case 59:
         case 61:
         case 75:
         case 76:
         case 78:
         case 83:
         case 85:
         case 87:
         case 88:
         case 89:
         case 90:
         case 92:
         case 93:
         case 94:
         case 95:
         case 97:
         case 98:
         case 101:
         case 102:
         case 103:
         case 104:
            newstate = rh.readByte();
            xmod = rh.readShort();
            unk = rh.readByte();
            um = new AranMovement(command, new Point(0, 0), xmod, newstate, unk);
            res.add(um);
            break;
         case 49:
            xpos = rh.readShort();
            xmod = rh.readShort();
            ymod = rh.readShort();
            ywobble = rh.readShort();
            duration = rh.readShort();
            unk = rh.readByte();
            yoffset = rh.readShort();
            unk = rh.readByte();
            UnknownMovement2 um = new UnknownMovement2(command, new Point(xpos, xmod), yoffset, unk, unk);
            um.setPixelsPerSecond(new Point(ymod, ywobble));
            um.setXOffset(duration);
            res.add(um);
            break;
         case 56:
         case 68:
            xpos = rh.readShort();
            xmod = rh.readShort();
            ymod = rh.readShort();
            ywobble = rh.readShort();
            duration = rh.readShort();
            unk = rh.readByte();
            yoffset = rh.readShort();
            unk = rh.readByte();
            UnknownMovement um = new UnknownMovement(command, new Point(xpos, xmod), yoffset, unk, duration, unk);
            um.setPixelsPerSecond(new Point(ymod, ywobble));
            res.add(um);
         }
      }

      if (numCommands != res.size()) {
         return null;
      } else {
         return res;
      }
   }

   public static void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target, int yoffset) {
      if (movement != null) {
         Iterator var3 = movement.iterator();

         while(var3.hasNext()) {
            LifeMovementFragment move = (LifeMovementFragment)var3.next();
            if (move instanceof LifeMovement) {
               if (move instanceof AbsoluteLifeMovement) {
                  Point position2;
                  Point position = position2 = ((LifeMovement)move).getPosition();
                  position2.y += yoffset;
                  target.setPosition(position);
               }

               target.setStance(((LifeMovement)move).getNewstate());
            }
         }

      }
   }
}

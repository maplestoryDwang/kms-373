package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.world.World;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartyOperation;
import java.util.ArrayList;
import java.util.Iterator;
import server.maps.Event_DojoAgent;
import server.maps.FieldLimitType;
import server.quest.MapleQuest;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

public class PartyHandler {
   public static final void DenyPartyRequest(LittleEndianAccessor slea, MapleClient c) {
      int action = slea.readByte();
      int type = slea.readInt();
      MapleCharacter party_reder = c.getChannelServer().getPlayerStorage().getCharacterById(slea.readInt());
      if (c.getPlayer().getParty() == null && c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(122901)) == null) {
         MapleParty party = World.Party.getParty(party_reder.getParty().getId());
         if (party != null) {
            if (action == 40) {
               if (type == 5) {
                  if (party.getMembers().size() < 6) {
                     System.out.println("파티");
                     c.getPlayer().setParty(party);
                     World.Party.updateParty(party_reder.getParty().getId(), PartyOperation.JOIN, new MaplePartyCharacter(c.getPlayer()));
                     c.getPlayer().receivePartyMemberHP();
                     c.getPlayer().updatePartyMemberHP();
                  } else {
                     c.getPlayer().dropMessage(5, c.getPlayer().getName() + "님이 파티를 수락하셨습니다.");
                  }
               }

               if (type == 4 && party_reder != null) {
                  party_reder.dropMessage(5, c.getPlayer().getName() + "님이 파티 초대를 거절하셨습니다.");
               }
            }
         } else {
            c.getPlayer().dropMessage(5, "가입하려는 파티가 존재하지 않습니다.");
         }
      } else {
         c.getPlayer().dropMessage(5, "이미 파티에 가입되어 있어 파티에 가입할 수 없습니다.");
      }

   }

   public static final void PartyOperation(LittleEndianAccessor slea, MapleClient c) {
      int operation = slea.readByte();
      MapleParty party = c.getPlayer().getParty();
      MaplePartyCharacter partyplayer = new MaplePartyCharacter(c.getPlayer());
      switch(operation) {
      case 1:
         String titlename = slea.readMapleAsciiString();
         byte visible = slea.readByte();
         byte drop = slea.readByte();
         if (party == null) {
            party = World.Party.createParty(partyplayer);
            party.setVisible(visible);
            party.setPartyTitle(titlename);
            party.setPartyDrop(drop);
            c.getPlayer().setParty(party);
            c.getSession().writeAndFlush(CWvsContext.PartyPacket.partyCreated(party, c.getPlayer()));
         } else if (partyplayer.equals(party.getLeader()) && party.getMembers().size() == 1) {
            c.getSession().writeAndFlush(CWvsContext.PartyPacket.partyCreated(party, c.getPlayer()));
         } else {
            c.getPlayer().dropMessage(5, "이미 파티에 가입되어 있어 파티를 만들 수 없습니다.");
         }

         return;
      case 2:
         if (party != null) {
            party.getVisible();
            if (partyplayer.equals(party.getLeader())) {
               if (GameConstants.isDojo(c.getPlayer().getMapId())) {
                  Event_DojoAgent.failed(c.getPlayer());
               }

               World.Party.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
               if (c.getPlayer().getEventInstance() != null) {
                  c.getPlayer().getEventInstance().disbandParty();
               }
            } else {
               if (GameConstants.isDojo(c.getPlayer().getMapId())) {
                  Event_DojoAgent.failed(c.getPlayer());
               }

               World.Party.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
               if (c.getPlayer().getEventInstance() != null) {
                  c.getPlayer().getEventInstance().leftParty(c.getPlayer());
               }
            }

            c.getPlayer().setParty((MapleParty)null);
            c.getPlayer().setBlessingAnsanble((byte)1);
         }

         return;
      case 3:
         String theName;
         int theCh;
         MapleCharacter invited;
         if (party == null) {
            party = World.Party.createParty(partyplayer);
            party.setPartyTitle(c.getPlayer().getName() + "님의 파티");
            c.getPlayer().setParty(party);
            party.setPartyTitle(party.getPatryTitle());
            c.getSession().writeAndFlush(CWvsContext.PartyPacket.partyCreated(party, c.getPlayer()));
            theName = slea.readMapleAsciiString();
            theCh = World.Find.findChannel(theName);
            if (theCh > 0) {
               invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterByName(theName);
               if (invited != null && invited.getParty() == null && invited.getQuestNoAdd(MapleQuest.getInstance(122901)) == null) {
                  if (party.getMembers().size() < 6) {
                     c.getPlayer().dropMessage(1, invited.getName() + "님을 파티에 초대했습니다.");
                     invited.getClient().getSession().writeAndFlush(CWvsContext.PartyPacket.partyInvite(c.getPlayer()));
                  } else {
                     c.getPlayer().dropMessage(5, "이미 파티원이 최대로 가득 찬 상태입니다.");
                  }
               } else {
                  c.getPlayer().dropMessage(5, "이미 파티에 가입되어 있는 대상입니다.");
               }
            } else {
               c.getPlayer().dropMessage(5, "대상을 찾지 못했습니다.");
            }
         } else {
            theName = slea.readMapleAsciiString();
            theCh = World.Find.findChannel(theName);
            if (theCh > 0) {
               invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterByName(theName);
               if (invited != null && invited.getParty() == null && invited.getQuestNoAdd(MapleQuest.getInstance(122901)) == null) {
                  if (party.getMembers().size() < 6) {
                     c.getPlayer().dropMessage(1, invited.getName() + "님을 파티에 초대했습니다.");
                     invited.getClient().getSession().writeAndFlush(CWvsContext.PartyPacket.partyInvite(c.getPlayer()));
                  } else {
                     c.getPlayer().dropMessage(5, "이미 파티원이 최대로 가득 찬 상태입니다.");
                  }
               } else {
                  c.getPlayer().dropMessage(5, "이미 파티에 가입되어 있는 대상입니다.");
               }
            } else {
               c.getPlayer().dropMessage(5, "대상을 찾지 못했습니다.");
            }
         }

         return;
      case 4:
      case 5:
      case 7:
      case 8:
      case 9:
      case 10:
      case 11:
      default:
         System.out.println("Unhandled Party function." + operation);
         return;
      case 6:
         if (party != null) {
            MaplePartyCharacter newleader = party.getMemberById(slea.readInt());
            if (newleader != null && partyplayer.equals(party.getLeader())) {
               World.Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newleader);
            }
         }

         return;
      case 12:
         String newTitle = slea.readMapleAsciiString();
         byte visible__ = slea.readByte();
         byte drop__ = slea.readByte();
         if (newTitle.length() < 0) {
            c.getPlayer().dropMessage(1, "한 글자 이상의 변경할 파티명을 입력해주십시오.");
         } else {
            party.setVisible(visible__);
            party.setPartyDrop(drop__);
            party.setPartyTitle(newTitle);
            ArrayList partymembers;
            if (party == null) {
               partymembers = new ArrayList();
            } else {
               partymembers = new ArrayList(party.getMembers());
            }

            while(partymembers.size() < c.getPlayer().getParty().getMembers().size()) {
               partymembers.add(new MaplePartyCharacter());
            }

            Iterator var12 = partymembers.iterator();

            while(var12.hasNext()) {
               MaplePartyCharacter partychar = (MaplePartyCharacter)var12.next();
               partychar.getPlayer().getClient().getSession().writeAndFlush(CWvsContext.PartyPacket.updateParty(c.getChannel(), party, PartyOperation.CHANGE_PARTY_TITLE, partyplayer, c.getPlayer()));
            }

         }
      }
   }

   public static final void AllowPartyInvite(LittleEndianAccessor slea, MapleClient c) {
      if (slea.readByte() > 0) {
         c.getPlayer().getQuest_Map().remove(MapleQuest.getInstance(122901));
      } else {
         c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122901));
      }

   }

   public static final void MemberSearch(LittleEndianAccessor slea, MapleClient c) {
      if (FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) {
         c.getPlayer().dropMessage(5, "You may not do party search here.");
      } else {
         c.getSession().writeAndFlush(CWvsContext.PartyPacket.showMemberSearch(c.getPlayer().getMap().getCharactersThreadsafe()));
      }
   }
}

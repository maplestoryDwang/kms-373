package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.PlatformerRecord;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.Item;
import handling.channel.ChannelServer;
import io.netty.channel.Channel;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import scripting.EventManager;
import scripting.NPCScriptManager;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.Timer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.OverrideMonsterStats;
import server.polofritto.BountyHunting;
import server.polofritto.DefenseTowerWave;
import server.polofritto.FrittoEagle;
import server.polofritto.FrittoEgg;
import server.quest.MapleQuest;
import server.quest.party.MapleNettPyramid;
import tools.FileoutputUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.SLFCGPacket;

public class MapScriptMethods {
   private static final Point witchTowerPos = new Point(-60, 184);
   private static final String[] mulungEffects = new String[]{"무릉도장에 도전한 것을 후회하게 해주겠다! 어서 들어와봐!", "기다리고 있었다! 용기가 남았다면 들어와 보시지!", "배짱 하나는 두둑하군! 현명함과 무모함을 혼동하지말라고!", "무릉도장에 도전하다니 용기가 가상하군!", "패배의 길을 걷고싶다면 들어오라고!"};

   public static void startScript_FirstUser(MapleClient c, String scriptName) {
      if (c.getPlayer() != null) {
         c.getPlayer().dropMessageGM(-8, "펄스트 : " + scriptName);
         MapleNettPyramid mnp;
         Iterator pos3;
         int mobId;
         int i;
         MapleMonster shammos;
         int averageLevel;
         int size;
         Iterator var13;
         MapleMonster m;
         EventManager em;
         MapleCharacter pl;
         Iterator var21;
         switch(MapScriptMethods.onFirstUserEnter.fromString(scriptName)) {
         case firstenter_bossBlackMage:
            Timer.EventTimer.getInstance().schedule(() -> {
               if (c.getPlayer().getMapId() == 450013100) {
                  c.getPlayer().getMap().broadcastMessage(CField.enforceMSG("검은 마법사와 대적하기 위해서는 그를 호위하는 창조와 파괴의 기사들을 물리쳐야 한다.", 265, 8000));
               } else if (c.getPlayer().getMapId() == 450013300) {
                  c.getPlayer().getMap().broadcastMessage(CField.enforceMSG("드디어 검은 마법사의 앞에 바로 섰다. 모든 힘을 다해 그를 물리치자.", 265, 8000));
               } else if (c.getPlayer().getMapId() == 450013500) {
                  c.getPlayer().getMap().broadcastMessage(CField.enforceMSG("저 모습은 마치 신의 권능이라도 얻은 것 같다. 설사 상대가 신이라고 할지라도 모두를 위해 여기서 저지해야 한다.", 265, 8000));
               }

               if (c.getPlayer().getMapId() == 450013100) {
                  c.getPlayer().getMap().broadcastMessage(CField.ImageTalkNpc(0, 4000, "이 지역에서 발생되는 공격은 창조나 파괴의 저주를 거는 것 같다. 만약 두 저주가 동시에 걸린다면 #b큰 피해#k를 입으니 조심하자"));
               } else if (c.getPlayer().getMapId() == 450013300) {
                  c.getPlayer().getMap().broadcastMessage(CField.ImageTalkNpc(0, 4000, "이 지역에서 발생되는 공격은 창조나 파괴의 저주를 거는 것 같다. 만약 두 저주가 동시에 걸린다면 #b큰 피해#k를 입으니 조심하자"));
               } else if (c.getPlayer().getMapId() == 450013500) {
                  c.getPlayer().getMap().broadcastMessage(CField.ImageTalkNpc(3003902, 4000, "#face1#가자. 나는 복수를, 너는 세계를 지키는 거야."));
               } else if (c.getPlayer().getMapId() == 450013500) {
                  c.getPlayer().getMap().broadcastMessage(CField.ImageTalkNpc(0, 4000, "아무 것도 없는 공간…… 나 혼자 남은 것인가……"));
               } else if (c.getPlayer().getMapId() == 450013750) {
                  c.getPlayer().getMap().broadcastMessage(CField.ImageTalkNpc(0, 5000, "창세의 알을 파괴하여 기나긴 싸움을 마무리 하자."));
               }

            }, 1000L);
         case PinkBeen_before:
         case dusk_onFirstUserEnter:
         case boss_Ravana_mirror:
         case boss_Ravana:
         case cygnus_Summon:
         case MalayBoss_Int:
         case storymap_scenario:
         case VanLeon_Before:
         case dojang_Msg:
         case balog_summon:
         case easy_balog_summon:
            break;
         case onRewordMap:
            reloadWitchTower(c);
            break;
         case moonrabbit_mapEnter:
            c.getPlayer().getMap().startMapEffect("Gather the Primrose Seeds around the moon and protect the Moon Bunny!", 5120016);
            break;
         case Fenter_450004250:
            ArrayList<String> tags = new ArrayList<String>() {
               {
                  for(int i = 1; i <= 4; ++i) {
                     this.add("except" + i);
                  }

               }
            };
            c.getSession().writeAndFlush(MobPacket.BossLucid.setStainedGlassOnOff(true, tags));
            break;
         case StageMsg_goddess:
            switch(c.getPlayer().getMapId()) {
            case 920010000:
               c.getPlayer().getMap().startMapEffect("Please save me by collecting Cloud Pieces!", 5120019);
               return;
            case 920010100:
               c.getPlayer().getMap().startMapEffect("Bring all the pieces here to save Minerva!", 5120019);
               return;
            case 920010200:
               c.getPlayer().getMap().startMapEffect("Destroy the monsters and gather Statue Pieces!", 5120019);
               return;
            case 920010300:
               c.getPlayer().getMap().startMapEffect("Destroy the monsters in each room and gather Statue Pieces!", 5120019);
               return;
            case 920010400:
               c.getPlayer().getMap().startMapEffect("Play the correct LP of the day!", 5120019);
               return;
            case 920010500:
               c.getPlayer().getMap().startMapEffect("Find the correct combination!", 5120019);
               return;
            case 920010600:
               c.getPlayer().getMap().startMapEffect("Destroy the monsters and gather Statue Pieces!", 5120019);
               return;
            case 920010700:
               c.getPlayer().getMap().startMapEffect("Get the right combination once you get to the top!", 5120019);
               return;
            case 920010800:
               c.getPlayer().getMap().startMapEffect("Summon and defeat Papa Pixie!", 5120019);
               return;
            default:
               return;
            }
         case StageMsg_crack:
            if (c.getPlayer().getMapId() >= 922010401 && c.getPlayer().getMapId() <= 922010405) {
               NPCScriptManager.getInstance().start(c.getPlayer().getClient(), 2007, "RP_stage2");
            }

            switch(c.getPlayer().getMapId()) {
            case 922010100:
               c.getPlayer().getMap().startMapEffect("차원의 라츠와 차원의 블랙라츠를 모두 해치우고 차원의 통행증 20장을 모아라!", 5120018);
               return;
            case 922010600:
               c.getPlayer().getMap().startMapEffect("숨겨진 상자의 암호를 풀고 꼭대기로 올라가라.", 5120018);
               return;
            case 922010700:
               c.getPlayer().getMap().startMapEffect("이 곳에 있는 롬바드를 모두 물리치자!", 5120018);
               c.getPlayer().getMap().setRPTicket(0);
               c.getPlayer().getMap().resetFully();
               return;
            case 922010800:
               c.getPlayer().getMap().startSimpleMapEffect("문제를 듣고 정답에 맞는 상자 위로 올라가라!", 5120018);
               return;
            case 922010900:
               c.getPlayer().getMap().startSimpleMapEffect("알리샤르를 물리쳐 주세요!", 5120018);
               c.getPlayer().getMap().spawnMonsterWithEffectBelow(MapleLifeFactory.getMonster(9300012), new Point(704, 184), 15);
               return;
            default:
               return;
            }
         case StageMsg_together:
            switch(c.getPlayer().getMapId()) {
            case 103000800:
               c.getPlayer().getMap().startMapEffect("Solve the question and gather the amount of passes!", 5120017);
               return;
            case 103000801:
               c.getPlayer().getMap().startMapEffect("Get on the ropes and unveil the correct combination!", 5120017);
               return;
            case 103000802:
               c.getPlayer().getMap().startMapEffect("Get on the platforms and unveil the correct combination!", 5120017);
               return;
            case 103000803:
               c.getPlayer().getMap().startMapEffect("Get on the barrels and unveil the correct combination!", 5120017);
               return;
            case 103000804:
               c.getPlayer().getMap().startMapEffect("Defeat King Slime and his minions!", 5120017);
               return;
            default:
               return;
            }
         case will_phase1:
            c.getPlayer().getMap().broadcastMessage(CField.enforceMSG("전혀 다른 2개의 공간에 있는 윌을 동시에 공격해야 해요. 달빛을 모아서 사용하면 다른 쪽으로 이동이 가능할 것 같아요.", 245, 7000));
            break;
         case will_phase2:
            c.getPlayer().getMap().broadcastMessage(CField.enforceMSG("거울에 비친 진짜 모습을 조심하세요. 달빛을 모아서 사용하면 치유 불가 저주를 잠시 멈출 수 있을 것 같아요.", 245, 7000));
            var21 = c.getPlayer().getMap().getAllMonster().iterator();

            do {
               if (!var21.hasNext()) {
                  return;
               }

               m = (MapleMonster)var21.next();
            } while(m.getId() != 8880342 && m.getId() != 8880302);

            c.getPlayer().getMap().broadcastMessage(MobPacket.showBossHP(m));
            c.getPlayer().getMap().broadcastMessage(MobPacket.BossWill.setWillHp(m.getWillHplist()));
            break;
         case will_phase3:
            c.getPlayer().getMap().broadcastMessage(CField.enforceMSG("윌이 진심이 된 것 같군요. 달빛을 모아서 사용하면 거미줄을 태워버릴 수 있을 것 같아요.", 245, 7000));
            var21 = c.getPlayer().getMap().getAllMonster().iterator();

            do {
               if (!var21.hasNext()) {
                  return;
               }

               m = (MapleMonster)var21.next();
            } while(m.getId() != 8880342 && m.getId() != 8880302);

            c.getPlayer().getMap().broadcastMessage(MobPacket.showBossHP(m));
            break;
         case JinHillah_onFirstUserEnter:
            c.getPlayer().getMap().broadcastMessage(CField.enforceMSG("영혼이 타오르는 양초를 힐라가 일정 시간마다 베어 없앨 것이다. 영혼을 빼앗기지 않게 조심하자.", 254, 8000));
            SkillFactory.getSkill(80002543).getEffect(1).applyTo(c.getPlayer(), false);
            break;
         case StageMsg_romio:
            switch(c.getPlayer().getMapId()) {
            case 926100000:
               c.getPlayer().getMap().startMapEffect("Please find the hidden door by investigating the Lab!", 5120021);
               return;
            case 926100001:
               c.getPlayer().getMap().startMapEffect("Find  your way through this darkness!", 5120021);
               return;
            case 926100100:
               c.getPlayer().getMap().startMapEffect("Fill the beakers to power the energy!", 5120021);
               return;
            case 926100200:
               c.getPlayer().getMap().startMapEffect("Get the files for the experiment through each door!", 5120021);
               return;
            case 926100203:
               c.getPlayer().getMap().startMapEffect("Please defeat all the monsters!", 5120021);
               return;
            case 926100300:
               c.getPlayer().getMap().startMapEffect("Find your way through the Lab!", 5120021);
               return;
            case 926100401:
               c.getPlayer().getMap().startMapEffect("Please, protect my love!", 5120021);
               return;
            default:
               return;
            }
         case StageMsg_juliet:
            switch(c.getPlayer().getMapId()) {
            case 926110000:
               c.getPlayer().getMap().startMapEffect("Please find the hidden door by investigating the Lab!", 5120022);
               return;
            case 926110001:
               c.getPlayer().getMap().startMapEffect("Find  your way through this darkness!", 5120022);
               return;
            case 926110100:
               c.getPlayer().getMap().startMapEffect("Fill the beakers to power the energy!", 5120022);
               return;
            case 926110200:
               c.getPlayer().getMap().startMapEffect("Get the files for the experiment through each door!", 5120022);
               return;
            case 926110203:
               c.getPlayer().getMap().startMapEffect("Please defeat all the monsters!", 5120022);
               return;
            case 926110300:
               c.getPlayer().getMap().startMapEffect("Find your way through the Lab!", 5120022);
               return;
            case 926110401:
               c.getPlayer().getMap().startMapEffect("Please, protect my love!", 5120022);
               return;
            default:
               return;
            }
         case party6weatherMsg:
            switch(c.getPlayer().getMapId()) {
            case 930000000:
               c.getPlayer().getMap().startMapEffect("중앙의 포탈을 타고 입장해. 지금 너에게 변신 마법을 걸게.", 5120023);
               return;
            case 930000010:
               c.getPlayer().getMap().startMapEffect("본인이 누군지 헷갈리지 않도록 자신의 모습을 확인해!", 5120023);
               return;
            case 930000100:
               c.getPlayer().getMap().startMapEffect("트리로드 때문에 숲이 독에 오염되었어. 트리로드를 모두 없애줘!", 5120023);
               return;
            case 930000200:
               c.getPlayer().getMap().startMapEffect("웅덩이에서 독을 희석된 독으로 바꾸고, 희석된 독으로 가시덤불을 없애!", 5120023);
               return;
            case 930000300:
               c.getPlayer().getMap().startMapEffect("다들 어디 가버린거야? 포탈을 타고 내가 있는 곳까지 와!", 5120023);
               return;
            case 930000400:
               c.getPlayer().getMap().startMapEffect("중독된 스프라이트를 물리치고 몬스터 구슬을 모아와줘!", 5120023);
               return;
            case 930000500:
               c.getPlayer().getMap().startMapEffect("괴인의 책상 앞에 있는 상자들을 열고 보라색 마력석을 가져와!", 5120023);
               return;
            case 930000600:
               c.getPlayer().getMap().startMapEffect("보라색 마력석을 가지고 괴인의 책상을 클릭해 봐!", 5120023);
               return;
            default:
               return;
            }
         case WUK_StageEnter:
            switch(c.getPlayer().getMapId()) {
            case 933011000:
               c.getPlayer().getMap().startMapEffect("파티원들은 클로토를 찾아가서 그녀가 말한 개수만큼 리케이터를 물리치고 쿠폰을 모아라!", 5120017);
               return;
            case 933012000:
               c.getPlayer().getMap().startMapEffect("다음 단계로 가는 문을 열 수 있는 줄 3개를 찾아서 매달려라!", 5120017);
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(15));
               return;
            case 933013000:
               c.getPlayer().getMap().startMapEffect("다음 단계로 가는 문을 열 수 있는 발판 3개를 찾아라!", 5120017);
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(30));
               return;
            case 933014000:
               em = c.getChannelServer().getEventSM().getEventManager("KerningPQ");
               em.setProperty("stage4r", "0");
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(50));
               int randomNum = false;
               size = (int)Math.floor(Math.random() * 200.0D + 500.0D);
               String to = Integer.toString(size);
               c.getPlayer().getEventInstance().setProperty("stage4M", to);
               c.getPlayer().getMap().setKerningPQ(0);
               c.getPlayer().getMap().startMapEffect("기호를 활성화시키고 숫자를 주워 다음 숫자를 완성하라!" + size, 5120017);
               return;
            case 933015000:
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(75));
               c.getPlayer().getMap().startMapEffect("킹슬라임을 해치워라!!", 5120017);
               return;
            default:
               return;
            }
         case prisonBreak_mapEnter:
            switch(c.getPlayer().getMapId()) {
            case 921160100:
               c.getPlayer().getMap().startMapEffect("쉿! 조용히 장애물들을 피해서 탑을 벗어나셔야 합니다.", 5120053);
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(0));
               return;
            case 921160200:
               c.getPlayer().getMap().startMapEffect("경비병들을 모두 물리치셔야 해요. 그렇지 않으면 그들이 다른 경비병까지 불러올꺼에요.", 5120053);
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(10));
               return;
            case 921160300:
               c.getPlayer().getMap().startMapEffect("감옥으로의 접근을 막기 위해 그들이 미로를 만들어 놨어요. 공중감옥으로 통하는 문을 찾으세요!", 5120053);
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(20));
               return;
            case 921160400:
               c.getPlayer().getMap().startMapEffect("문을 지키고 있는 경비병들을 모두 처치하세요!", 5120053);
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(30));
               return;
            case 921160500:
               c.getPlayer().getMap().startMapEffect("이것이 마지막 장애물이군요. 장애물을 통과해 공중 감옥으로 와주세요.", 5120053);
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(50));
               return;
            case 921160600:
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(60));
               c.getPlayer().getMap().startMapEffect("경비병을 처치하고 감옥 열쇠를 되찾아 문을 열어주세요.", 5120053);
               return;
            case 921160700:
               c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(70));
               c.getPlayer().getMap().startMapEffect("교도관을 물리치고 우리에게 자유를 되찾아주세요!!!", 5120053);
               c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300454), new Point(-954, -181));
               return;
            default:
               return;
            }
         case StageMsg_davy:
            switch(c.getPlayer().getMapId()) {
            case 925100000:
               c.getPlayer().getMap().startMapEffect("Defeat the monsters outside of the ship to advance!", 5120020);
               break;
            case 925100100:
               c.getPlayer().getMap().startMapEffect("We must prove ourselves! Get me Pirate Medals!", 5120020);
               break;
            case 925100200:
               c.getPlayer().getMap().startMapEffect("Defeat the guards here to pass!", 5120020);
               break;
            case 925100300:
               c.getPlayer().getMap().startMapEffect("Eliminate the guards here to pass!", 5120020);
               break;
            case 925100400:
               c.getPlayer().getMap().startMapEffect("Lock the doors! Seal the root of the Ship's power!", 5120020);
               break;
            case 925100500:
               c.getPlayer().getMap().startMapEffect("Destroy the Lord Pirate!", 5120020);
            }

            em = c.getChannelServer().getEventSM().getEventManager("Pirate");
            if (c.getPlayer().getMapId() == 925100500 && em != null && em.getProperty("stage5") != null) {
               averageLevel = Randomizer.nextBoolean() ? 9300107 : 9300119;
               size = Integer.parseInt(em.getProperty("stage5"));
               switch(size) {
               case 1:
                  averageLevel = Randomizer.nextBoolean() ? 9300119 : 9300105;
                  break;
               case 2:
                  averageLevel = Randomizer.nextBoolean() ? 9300106 : 9300105;
               }

               MapleMonster shammos = MapleLifeFactory.getMonster(averageLevel);
               if (c.getPlayer().getEventInstance() != null) {
                  c.getPlayer().getEventInstance().registerMonster(shammos);
               }

               c.getPlayer().getMap().spawnMonsterOnGroundBelow(shammos, new Point(411, 236));
            }
            break;
         case astaroth_summon:
            c.getPlayer().getMap().resetFully();
            c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9400633), new Point(600, -26));
            break;
         case killing_BonusSetting:
            c.getPlayer().getMap().resetFully();
            c.getSession().writeAndFlush(CField.showEffect("killing/bonus/bonus"));
            c.getSession().writeAndFlush(CField.showEffect("killing/bonus/stage"));
            shammos = null;
            mnp = null;
            pos3 = null;
            int spawnPer = false;
            int mobId = false;
            Point pos1;
            Point pos3;
            byte spawnPer;
            Point pos2;
            if (c.getPlayer().getMapId() >= 910320010 && c.getPlayer().getMapId() <= 910320029) {
               pos1 = new Point(121, 218);
               pos2 = new Point(396, 43);
               pos3 = new Point(-63, 43);
               mobId = 9700020;
               spawnPer = 10;
            } else if (c.getPlayer().getMapId() >= 926010010 && c.getPlayer().getMapId() <= 926010029) {
               pos1 = new Point(0, 88);
               pos2 = new Point(-326, -115);
               pos3 = new Point(361, -115);
               mobId = 9700019;
               spawnPer = 10;
            } else if (c.getPlayer().getMapId() >= 926010030 && c.getPlayer().getMapId() <= 926010049) {
               pos1 = new Point(0, 88);
               pos2 = new Point(-326, -115);
               pos3 = new Point(361, -115);
               mobId = 9700019;
               spawnPer = 15;
            } else if (c.getPlayer().getMapId() >= 926010050 && c.getPlayer().getMapId() <= 926010069) {
               pos1 = new Point(0, 88);
               pos2 = new Point(-326, -115);
               pos3 = new Point(361, -115);
               mobId = 9700019;
               spawnPer = 20;
            } else {
               if (c.getPlayer().getMapId() < 926010070 || c.getPlayer().getMapId() > 926010089) {
                  break;
               }

               pos1 = new Point(0, 88);
               pos2 = new Point(-326, -115);
               pos3 = new Point(361, -115);
               mobId = 9700029;
               spawnPer = 20;
            }

            for(int i = 0; i < spawnPer; ++i) {
               c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new Point(pos1));
               c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new Point(pos2));
               c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new Point(pos3));
            }

            return;
         case mPark_summonBoss:
            if (c.getPlayer().getEventInstance() != null && c.getPlayer().getEventInstance().getProperty("boss") != null && c.getPlayer().getEventInstance().getProperty("boss").equals("0")) {
               for(i = 9800119; i < 9800125; ++i) {
                  m = MapleLifeFactory.getMonster(i);
                  c.getPlayer().getEventInstance().registerMonster(m);
                  c.getPlayer().getMap().spawnMonsterOnGroundBelow(m, new Point(c.getPlayer().getMap().getPortal(2).getPosition()));
               }
            }
            break;
         case hontale_boss1:
         case hontale_boss2:
            c.getPlayer().getMap().killAllMonsters(false);
            break;
         case shammos_Fenter:
            if (c.getPlayer().getMapId() >= 921120005 && c.getPlayer().getMapId() < 921120500) {
               shammos = MapleLifeFactory.getMonster(9300275);
               if (c.getPlayer().getEventInstance() != null) {
                  averageLevel = 0;
                  size = 0;

                  for(var13 = c.getPlayer().getEventInstance().getPlayers().iterator(); var13.hasNext(); ++size) {
                     pl = (MapleCharacter)var13.next();
                     averageLevel += pl.getLevel();
                  }

                  if (size <= 0) {
                     return;
                  }

                  shammos.changeLevel(averageLevel /= size);
                  c.getPlayer().getEventInstance().registerMonster(shammos);
                  if (c.getPlayer().getEventInstance().getProperty("HP") == null) {
                     c.getPlayer().getEventInstance().setProperty("HP", averageLevel + "000");
                  }

                  shammos.setHp(Long.parseLong(c.getPlayer().getEventInstance().getProperty("HP")));
               }

               c.getPlayer().getMap().spawnMonsterWithEffectBelow(shammos, new Point(c.getPlayer().getMap().getPortal(0).getPosition()), 12);
               shammos.switchController(c.getPlayer(), false);
            }
            break;
         case iceman_FEnter:
            if (c.getPlayer().getMapId() >= 932000100 && c.getPlayer().getMapId() < 932000300) {
               shammos = MapleLifeFactory.getMonster(9300438);
               if (c.getPlayer().getEventInstance() != null) {
                  averageLevel = 0;
                  size = 0;

                  for(var13 = c.getPlayer().getEventInstance().getPlayers().iterator(); var13.hasNext(); ++size) {
                     pl = (MapleCharacter)var13.next();
                     averageLevel += pl.getLevel();
                  }

                  if (size <= 0) {
                     return;
                  }

                  shammos.changeLevel(averageLevel /= size);
                  c.getPlayer().getEventInstance().registerMonster(shammos);
                  if (c.getPlayer().getEventInstance().getProperty("HP") == null) {
                     c.getPlayer().getEventInstance().setProperty("HP", averageLevel + "000");
                  }

                  shammos.setHp(Long.parseLong(c.getPlayer().getEventInstance().getProperty("HP")));
               }

               c.getPlayer().getMap().spawnMonsterWithEffectBelow(shammos, new Point(c.getPlayer().getMap().getPortal(0).getPosition()), 12);
               shammos.switchController(c.getPlayer(), false);
            }
            break;
         case PRaid_D_Fenter:
            switch(c.getPlayer().getMapId() % 10) {
            case 0:
               c.getPlayer().getMap().startMapEffect("몬스터를 모두 퇴치해라!", 5120033);
               return;
            case 1:
               c.getPlayer().getMap().startMapEffect("상자를 부수고, 나오는 몬스터를 모두 퇴치해라!", 5120033);
               return;
            case 2:
               c.getPlayer().getMap().startMapEffect("일등항해사를 퇴치해라!", 5120033);
               return;
            case 3:
               c.getPlayer().getMap().startMapEffect("몬스터를 모두 퇴치해라!", 5120033);
               return;
            case 4:
               c.getPlayer().getMap().startMapEffect("몬스터를 모두 퇴치하고, 점프대를 작동시켜서 건너편으로 건너가라!", 5120033);
               return;
            default:
               return;
            }
         case PRaid_B_Fenter:
            c.getPlayer().getMap().startMapEffect("상대편보다 먼저 몬스터를 퇴치하라!", 5120033);
            break;
         case Polo_Defence:
            c.getPlayer().getMap().resetFully();
            c.getPlayer().setBountyHunting(new BountyHunting(1));
            c.getPlayer().getBountyHunting().start(c);
            break;
         case summon_pepeking:
            c.getPlayer().getMap().resetFully();
            i = Randomizer.nextInt(10);
            averageLevel = 100100;
            averageLevel = i >= 4 ? 3300007 : (i >= 1 ? 3300006 : 3300005);
            c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(averageLevel), c.getPlayer().getPosition());
            break;
         case Xerxes_summon:
            c.getPlayer().getMap().resetFully();
            c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(6160003), c.getPlayer().getPosition());
            break;
         case shammos_FStart:
            c.getPlayer().getMap().startMapEffect("Defeat the monsters!", 5120035);
            break;
         case kenta_mapEnter:
            switch(c.getPlayer().getMapId() / 100 % 10) {
            case 1:
               c.getPlayer().getMap().startMapEffect("Eliminate all the monsters!", 5120052);
               return;
            case 2:
               c.getPlayer().getMap().startMapEffect("Get me 20 Air Bubbles for me to survive!", 5120052);
               return;
            case 3:
               c.getPlayer().getMap().startMapEffect("Help! Make sure I live for three minutes!", 5120052);
               return;
            case 4:
               c.getPlayer().getMap().startMapEffect("Eliminate the two Pianus!", 5120052);
               return;
            default:
               return;
            }
         case iceman_Boss:
            c.getPlayer().getMap().startMapEffect("You will perish!", 5120050);
            break;
         case Visitor_Cube_poison:
            c.getPlayer().getMap().startMapEffect("Eliminate all the monsters!", 5120039);
            break;
         case Visitor_Cube_Hunting_Enter_First:
            c.getPlayer().getMap().startMapEffect("Eliminate all the Visitors!", 5120039);
            break;
         case VisitorCubePhase00_Start:
            c.getPlayer().getMap().startMapEffect("Eliminate all the flying monsters!", 5120039);
            break;
         case visitorCube_addmobEnter:
            c.getPlayer().getMap().startMapEffect("Eliminate all the monsters by moving around the map!", 5120039);
            break;
         case Visitor_Cube_PickAnswer_Enter_First_1:
            c.getPlayer().getMap().startMapEffect("One of the aliens must have a clue to the way out.", 5120039);
            break;
         case visitorCube_medicroom_Enter:
            c.getPlayer().getMap().startMapEffect("Eliminate all of the Unjust Visitors!", 5120039);
            break;
         case visitorCube_iceyunna_Enter:
            c.getPlayer().getMap().startMapEffect("Eliminate all of the Speedy Visitors!", 5120039);
            break;
         case Visitor_Cube_AreaCheck_Enter_First:
            c.getPlayer().getMap().startMapEffect("The switch at the top of the room requires a heavy weight.", 5120039);
            break;
         case visitorCube_boomboom_Enter:
            c.getPlayer().getMap().startMapEffect("The enemy is powerful! Watch out!", 5120039);
            break;
         case visitorCube_boomboom2_Enter:
            c.getPlayer().getMap().startMapEffect("This Visitor is strong! Be careful!", 5120039);
            break;
         case CubeBossbang_Enter:
            c.getPlayer().getMap().startMapEffect("This is it! Give it your best shot!", 5120039);
            break;
         case metro_firstSetting:
         case killing_MapSetting:
         case Sky_TrapFEnter:
         case balog_bonusSetting:
            c.getPlayer().getMap().resetFully();
            break;
         case pyramidWeather:
            if (c.getPlayer().isLeader()) {
               boolean hard = c.getPlayer().nettDifficult == 2;
               mnp = MapleNettPyramid.getInfo(c.getPlayer(), hard);
               if (mnp != null) {
                  pos3 = mnp.getMembers().iterator();

                  while(pos3.hasNext()) {
                     MapleNettPyramid.MapleNettPyramidMember pm = (MapleNettPyramid.MapleNettPyramidMember)pos3.next();
                     if (pm.getChr() != null) {
                        mobId = pm.getChr().getV("NettPyramid") == null ? 0 : Integer.parseInt(pm.getChr().getV("NettPyramid"));
                        pm.getChr().addKV("NettPyramid", String.valueOf(mobId + 1));
                        pm.getChr().setNettPyramid(mnp);
                     } else {
                        c.getPlayer().dropMessage(5, "오류가 발생했습니다.");
                     }
                  }

                  mnp.firstNettPyramid(c.getPlayer());
               }
            }
            break;
         default:
            FileoutputUtil.log("Log_Script_Except.rtf", "Unhandled script : " + scriptName + ", type : onFirstUserEnter - MAPID " + c.getPlayer().getMapId());
         }

      }
   }

   public static void startScript_User(final MapleClient c, String scriptName) {
      if (c.getPlayer() != null) {
         String data = "";
         if (c.getPlayer().isGM()) {
            c.getPlayer().dropMessage(6, "startScript_User : " + scriptName);
         }

         MapleMapFactory mf;
         int l;
         MapleMap mapp;
         HashMap sa;
         int var10001;
         int q;
         int i;
         Channel var40;
         label503:
         switch(MapScriptMethods.onUserEnter.fromString(scriptName)) {
         case dojang_QcheckSet:
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().start(c, 2007, "dojo_exit");
            break;
         case dojang_1st:
            c.getPlayer().getMap().startMapEffect("제한시간은 15분, 최대한 신속하게 몬스터를 쓰러트리고 다음 층으로 올라가면 돼!", 5120024, 8000);
            break;
         case cannon_tuto_direction:
            showIntro(c, "Effect/Direction4.img/cannonshooter/Scene00");
            showIntro(c, "Effect/Direction4.img/cannonshooter/out00");
            break;
         case dunkel_timeRecord:
            c.getPlayer().getMap().broadcastMessage(CField.enforceMSG("친위대장 듄켈 : 나와 나의 군단이 있는 이상 위대하신 분께는 손끝 하나 대지 못한다!", 272, 5000));
            break;
         case cannon_tuto_direction1:
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(true));
            c.getSession().writeAndFlush(CField.UIPacket.IntroLock(true));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction4.img/effect/cannonshooter/balloon/0", 5000, 0, 0, 1, 0));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction4.img/effect/cannonshooter/balloon/1", 5000, 0, 0, 1, 0));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction4.img/effect/cannonshooter/balloon/2", 5000, 0, 0, 1, 0));
            c.getSession().writeAndFlush(CField.EffectPacket.showWZEffect("Effect/Direction4.img/cannonshooter/face04"));
            c.getSession().writeAndFlush(CField.EffectPacket.showWZEffect("Effect/Direction4.img/cannonshooter/out01"));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 5000));
            break;
         case cannon_tuto_direction2:
            showIntro(c, "Effect/Direction4.img/cannonshooter/Scene01");
            showIntro(c, "Effect/Direction4.img/cannonshooter/out02");
            break;
         case cygnusTest:
            var10001 = c.getPlayer().getMapId() == 913040006 ? 9 : c.getPlayer().getMapId() - 913040000;
            showIntro(c, "Effect/Direction.img/cygnus/Scene" + var10001);
            break;
         case Polo_Wave:
            c.getPlayer().getMap().resetFully();
            c.getPlayer().setDefenseTowerWave(new DefenseTowerWave(1, 20));
            c.getPlayer().getDefenseTowerWave().start(c);
            break;
         case Fritto_Eagle_Enter:
            c.getPlayer().getMap().resetFully();
            c.getPlayer().setFrittoEagle(new FrittoEagle(0, 20));
            c.getPlayer().getFrittoEagle().start(c);
            break;
         case Fritto_Egg_Enter:
            c.getPlayer().getMap().resetFully();
            c.getPlayer().setFrittoEgg(new FrittoEgg(0));
            c.getPlayer().getFrittoEgg().start(c);
            break;
         case magnus_enter_HP:
            if (c.getPlayer().getMap().getNumMonsters() == 0) {
               MapleMonster magnus = MapleLifeFactory.getMonster(c.getPlayer().getMapId() == 401060300 ? 8880010 : (c.getPlayer().getMapId() == 401060200 ? 8880002 : 8880000));
               magnus.setCustomInfo(magnus.getId(), 0, Randomizer.rand(20000, 40000));
               c.getPlayer().getMap().spawnMonsterOnGroundBelow(magnus, new Point(2800, -1347));
            }
            break;
         case bhb2_scEnterHp:
         case bhb3_scEnterHp:
            if (c.getPlayer().getMap().getNumMonsters() == 0) {
               c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(c.getPlayer().getMapId() == 350060190 ? 8950101 : (c.getPlayer().getMapId() == 350060210 ? 8950102 : (c.getPlayer().getMapId() == 350060200 ? 8950002 : 8950001))), c.getPlayer().getTruePosition());
            }
            break;
         case Akayrum_ExpeditionEnter:
            if (c.getPlayer().getMap().getNumMonsters() == 0) {
               c.getPlayer().getMap().broadcastMessage(CField.EffectPacket.showEffect(c.getPlayer(), 0, 0, 34, 0, 0, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0), false, c.getPlayer().getTruePosition(), "Voice.img/akayrum/2", (Item)null));
               c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("용기와 만용을 구분하지 못하는 자들이여. 목숨이 아깝지 않다면 내게 덤비도록. 후후.", 5120056, true));
               c.getPlayer().getMap().spawnNpc(2144010, new Point(320, -181));
            }
            break;
         case cygnusJobTutorial:
            var10001 = c.getPlayer().getMapId();
            showIntro(c, "Effect/Direction.img/cygnusJobTutorial/Scene" + (var10001 - 913040100));
            break;
         case shammos_Enter:
            if (c.getPlayer().getEventInstance() != null && c.getPlayer().getMapId() == 921120500) {
               NPCScriptManager.getInstance().dispose(c);
               c.removeClickedNPC();
               NPCScriptManager.getInstance().start(c, 2022006);
            }
            break;
         case iceman_Enter:
            if (c.getPlayer().getEventInstance() != null && c.getPlayer().getMapId() == 932000300) {
               NPCScriptManager.getInstance().dispose(c);
               c.removeClickedNPC();
               NPCScriptManager.getInstance().start(c, 2159020);
            }
            break;
         case start_itemTake:
            EventManager em = c.getChannelServer().getEventSM().getEventManager("OrbisPQ");
            if (em != null && em.getProperty("pre").equals("0")) {
               NPCScriptManager.getInstance().dispose(c);
               c.removeClickedNPC();
               NPCScriptManager.getInstance().start(c, 2013001);
            }
            break;
         case TD_neo_BossEnter:
         case findvioleta:
            c.getPlayer().getMap().resetFully();
            break;
         case StageMsg_crack:
            if (c.getPlayer().getMapId() == 922010400) {
               mf = c.getChannelServer().getMapFactory();
               q = 0;

               for(i = 0; i < 5; ++i) {
                  q += mf.getMap(922010401 + i).getNumMonsters();
               }

               if (q > 0) {
                  c.getPlayer().dropMessage(-1, "There are still " + q + " monsters remaining.");
               }
            } else if (c.getPlayer().getMapId() >= 922010401 && c.getPlayer().getMapId() <= 922010405) {
               if (c.getPlayer().getMap().getNumMonsters() > 0) {
                  c.getPlayer().dropMessage(-1, "There are still some monsters remaining in this map.");
               } else {
                  c.getPlayer().dropMessage(-1, "There are no monsters remaining in this map.");
               }
            }
            break;
         case q31102e:
            if (c.getPlayer().getQuestStatus(31102) == 1) {
               MapleQuest.getInstance(31102).forceComplete(c.getPlayer(), 2140000);
            }
            break;
         case q31103s:
            if (c.getPlayer().getQuestStatus(31103) == 0) {
               MapleQuest.getInstance(31103).forceComplete(c.getPlayer(), 2142003);
            }
            break;
         case Resi_tutor20:
            c.getSession().writeAndFlush(CField.MapEff("resistance/tutorialGuide"));
            break;
         case Resi_tutor30:
            c.getSession().writeAndFlush(CField.EffectPacket.showWZEffect("Effect/OnUserEff.img/guideEffect/resistanceTutorial/userTalk"));
            break;
         case Resi_tutor40:
            NPCScriptManager.getInstance().dispose(c);
            c.removeClickedNPC();
            NPCScriptManager.getInstance().start(c, 2159012);
            break;
         case Resi_tutor50:
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
            c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().dispose(c);
            c.removeClickedNPC();
            NPCScriptManager.getInstance().start(c, 2159006);
            break;
         case Resi_tutor70:
            showIntro(c, "Effect/Direction4.img/Resistance/TalkJ");
            break;
         case prisonBreak_1stageEnter:
         case shammos_Start:
         case moonrabbit_takeawayitem:
         case TCMobrevive:
         case cygnus_ExpeditionEnter:
         case knights_Summon:
         case VanLeon_ExpeditionEnter:
         case Resi_tutor10:
         case Resi_tutor60:
         case Resi_tutor50_1:
         case sealGarden:
         case in_secretroom:
         case TD_MC_gasi2:
         case TD_MC_keycheck:
         case pepeking_effect:
         case userInBattleSquare:
         case summonSchiller:
         case VisitorleaveDirectionMode:
         case visitorPT_Enter:
         case VisitorCubePhase00_Enter:
         case visitor_ReviveMap:
         case PRaid_D_Enter:
         case PRaid_B_Enter:
         case PRaid_WinEnter:
         case PRaid_FailEnter:
         case PRaid_Revive:
         case metro_firstSetting:
         case blackSDI:
         case summonIceWall:
         case onSDI:
         case enterBlackfrog:
         case Sky_Quest:
         case dollCave00:
         case dollCave01:
         case dollCave02:
         case shammos_Base:
         case shammos_Result:
         case Sky_BossEnter:
         case Sky_GateMapEnter:
         case balog_dateSet:
         case balog_buff:
         case outCase:
         case Sky_StageEnter:
         case evanTogether:
         case merStandAlone:
         case EntereurelTW:
         case aranTutorAlone:
         case evanAlone:
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            break;
         case merOutStandAlone:
            if (c.getPlayer().getQuestStatus(24001) == 1) {
               MapleQuest.getInstance(24001).forceComplete(c.getPlayer(), 0);
               c.getPlayer().dropMessage(5, "Quest complete.");
            }
            break;
         case merTutorSleep00:
            showIntro(c, "Effect/Direction5.img/mersedesTutorial/Scene0");
            sa = new HashMap();
            sa.put(SkillFactory.getSkill(20021181), new SkillEntry(-1, (byte)0, -1L));
            sa.put(SkillFactory.getSkill(20021166), new SkillEntry(-1, (byte)0, -1L));
            sa.put(SkillFactory.getSkill(20020109), new SkillEntry(1, (byte)1, -1L));
            sa.put(SkillFactory.getSkill(20021110), new SkillEntry(1, (byte)1, -1L));
            sa.put(SkillFactory.getSkill(20020111), new SkillEntry(1, (byte)1, -1L));
            sa.put(SkillFactory.getSkill(20020112), new SkillEntry(1, (byte)1, -1L));
            c.getPlayer().changeSkillsLevel(sa);
            break;
         case merTutorSleep01:
            while(c.getPlayer().getLevel() < 10) {
               c.getPlayer().levelUp();
            }

            c.getPlayer().changeJob(2300);
            showIntro(c, "Effect/Direction5.img/mersedesTutorial/Scene1");
            break;
         case merTutorSleep02:
            c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(0));
            break;
         case merTutorDrecotion00:
            c.getSession().writeAndFlush(CField.UIPacket.playMovie("Mercedes.avi", true));
            sa = new HashMap();
            sa.put(SkillFactory.getSkill(20021181), new SkillEntry(1, (byte)1, -1L));
            sa.put(SkillFactory.getSkill(20021166), new SkillEntry(1, (byte)1, -1L));
            c.getPlayer().changeSkillsLevel(sa);
            break;
         case merTutorDrecotion10:
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/mercedesInIce/merBalloon/6", 2000, 0, -100, 1, 0));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 2000));
            c.getPlayer().setDirection(0);
            break;
         case merTutorDrecotion20:
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/mercedesInIce/merBalloon/9", 2000, 0, -100, 1, 0));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 2000));
            c.getPlayer().setDirection(0);
            break;
         case ds_tuto_ani:
            c.getSession().writeAndFlush(CField.UIPacket.playMovie("DemonSlayer1.avi", true));
            break;
         case Resi_tutor80:
         case startEreb:
         case mirrorCave:
         case babyPigMap:
         case evanleaveD:
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
            c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            break;
         case dojang_Msg:
            c.getPlayer().getMap().startMapEffect(mulungEffects[Randomizer.nextInt(mulungEffects.length)], 5120024);
            break;
         case undomorphdarco:
         case reundodraco:
            c.getPlayer().cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(2210016));
            break;
         case goAdventure:
            showIntro(c, "Effect/Direction3.img/goAdventure/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case crash_Dragon:
            showIntro(c, "Effect/Direction4.img/crash/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case getDragonEgg:
            showIntro(c, "Effect/Direction4.img/getDragonEgg/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case meetWithDragon:
            showIntro(c, "Effect/Direction4.img/meetWithDragon/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case PromiseDragon:
            showIntro(c, "Effect/Direction4.img/PromiseDragon/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case evanPromotion:
            switch(c.getPlayer().getMapId()) {
            case 900090000:
               data = "Effect/Direction4.img/promotion/Scene0" + (c.getPlayer().getGender() == 0 ? "0" : "1");
               break;
            case 900090001:
               data = "Effect/Direction4.img/promotion/Scene1";
               break;
            case 900090002:
               data = "Effect/Direction4.img/promotion/Scene2" + (c.getPlayer().getGender() == 0 ? "0" : "1");
               break;
            case 900090003:
               data = "Effect/Direction4.img/promotion/Scene3";
               break;
            case 900090004:
               c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
               c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               mapp = c.getChannelServer().getMapFactory().getMap(900010000);
               c.getPlayer().changeMap(mapp, mapp.getPortal(0));
               return;
            }

            showIntro(c, data);
            break;
         case will_phase2_everyone:
            if (!c.getPlayer().getBuffedValue(80002404)) {
               SkillFactory.getSkill(80002404).getEffect(1).applyTo(c.getPlayer(), false);
            }
            break;
         case will_phase3_everyone:
            c.getSession().writeAndFlush(MobPacket.BossWill.willThirdOne());
            break;
         case JinHillah_onUserEnter:
            c.getSession().writeAndFlush(CField.JinHillah(0, c.getPlayer(), c.getPlayer().getMap()));
            c.getSession().writeAndFlush(CField.JinHillah(1, c.getPlayer(), c.getPlayer().getMap()));
            if (c.getPlayer().getMap().getReqTouched() > 0) {
               c.getSession().writeAndFlush(CField.JinHillah(6, c.getPlayer(), c.getPlayer().getMap()));
               c.getSession().writeAndFlush(CField.JinHillah(7, c.getPlayer(), c.getPlayer().getMap()));
            }

            if (c.getPlayer().getMap().getSandGlassTime() > 0L) {
               c.getSession().writeAndFlush(CField.JinHillah(4, c.getPlayer(), c.getPlayer().getMap()));
            }

            if (c.getPlayer().liveCounts() > 0) {
               c.getSession().writeAndFlush(CField.JinHillah(3, c.getPlayer(), c.getPlayer().getMap()));
               c.getPlayer().getMap().broadcastMessage(CField.JinHillah(10, c.getPlayer(), c.getPlayer().getMap()));
            }
            break;
         case mPark_stageEff:
            c.getPlayer().dropMessage(-1, "필드 내의 모든 몬스터를 제거해야 다음 스테이지로 이동하실 수 있습니다.");
            switch(c.getPlayer().getMapId() % 1000 / 100) {
            case 0:
            case 1:
            case 2:
            case 3:
               c.getSession().writeAndFlush(CField.showEffect("monsterPark/stageEff/stage"));
               var40 = c.getSession();
               var10001 = c.getPlayer().getMapId() % 1000 / 100;
               var40.writeAndFlush(CField.showEffect("monsterPark/stageEff/number/" + (var10001 + 1)));
               break label503;
            case 4:
               if (c.getPlayer().getMapId() / 1000000 == 952) {
                  c.getSession().writeAndFlush(CField.showEffect("monsterPark/stageEff/final"));
               } else {
                  c.getSession().writeAndFlush(CField.showEffect("monsterPark/stageEff/stage"));
                  c.getSession().writeAndFlush(CField.showEffect("monsterPark/stageEff/number/5"));
               }
               break label503;
            case 5:
               c.getSession().writeAndFlush(CField.showEffect("monsterPark/stageEff/final"));
            default:
               break label503;
            }
         case mPark_Enter:
            if (c.getPlayer().getMapId() == 951000000) {
               return;
            }
            break;
         case TD_MC_title:
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
            c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            c.getSession().writeAndFlush(CField.MapEff("temaD/enter/mushCatle"));
            break;
         case TD_NC_title:
            switch(c.getPlayer().getMapId() / 100 % 10) {
            case 0:
               c.getSession().writeAndFlush(CField.MapEff("temaD/enter/teraForest"));
               break label503;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
               var40 = c.getSession();
               var10001 = c.getPlayer().getMapId() / 100;
               var40.writeAndFlush(CField.MapEff("temaD/enter/neoCity" + var10001 % 10));
            default:
               break label503;
            }
         case explorationPoint:
            if (c.getPlayer().getMapId() == 104000000) {
               c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
               c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               c.getSession().writeAndFlush(CField.MapNameDisplay(c.getPlayer().getMapId()));
            }

            MapleQuest.MedalQuest m = null;
            MapleQuest.MedalQuest[] var22 = MapleQuest.MedalQuest.values();
            i = var22.length;

            int number;
            for(number = 0; number < i; ++number) {
               MapleQuest.MedalQuest mq = var22[number];
               int[] var36 = mq.maps;
               l = var36.length;

               for(int var38 = 0; var38 < l; ++var38) {
                  int j = var36[var38];
                  if (c.getPlayer().getMapId() == j) {
                     m = mq;
                     break;
                  }
               }
            }

            if (m != null && c.getPlayer().getLevel() >= m.level && c.getPlayer().getQuestStatus(m.questid) != 2) {
               if (c.getPlayer().getQuestStatus(m.lquestid) != 1) {
                  MapleQuest.getInstance(m.lquestid).forceStart(c.getPlayer(), 0, "0");
               }

               if (c.getPlayer().getQuestStatus(m.questid) != 1) {
                  MapleQuest.getInstance(m.questid).forceStart(c.getPlayer(), 0, (String)null);
                  StringBuilder sb = new StringBuilder("enter=");

                  for(i = 0; i < m.maps.length; ++i) {
                     sb.append("0");
                  }

                  c.getPlayer().updateInfoQuest(m.questid - 2005, sb.toString());
                  MapleQuest.getInstance(m.questid - 1995).forceStart(c.getPlayer(), 0, "0");
               }

               String quest = c.getPlayer().getInfoQuest(m.questid - 2005);
               if (quest.length() != m.maps.length + 6) {
                  StringBuilder sb2 = new StringBuilder("enter=");

                  for(number = 0; number < m.maps.length; ++number) {
                     sb2.append("0");
                  }

                  quest = sb2.toString();
                  c.getPlayer().updateInfoQuest(m.questid - 2005, quest);
               }

               MapleQuestStatus stat = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(m.questid - 1995));
               if (stat.getCustomData() == null) {
                  stat.setCustomData("0");
               }

               number = Integer.parseInt(stat.getCustomData());
               StringBuilder sb3 = new StringBuilder("enter=");
               boolean changedd = false;

               for(l = 0; l < m.maps.length; ++l) {
                  boolean changed = false;
                  if (c.getPlayer().getMapId() == m.maps[l] && quest.substring(l + 6, l + 7).equals("0")) {
                     sb3.append("1");
                     changed = true;
                     changedd = true;
                  }

                  if (!changed) {
                     sb3.append(quest.substring(l + 6, l + 7));
                  }
               }

               if (changedd) {
                  ++number;
                  c.getPlayer().updateInfoQuest(m.questid - 2005, sb3.toString());
                  MapleQuest.getInstance(m.questid - 1995).forceStart(c.getPlayer(), 0, String.valueOf(number));
                  c.getPlayer().dropMessage(-1, number + "/" + m.maps.length + "개 탐험");
                  c.getPlayer().dropMessage(-1, "칭호 - " + m.questname + " 탐험가 도전 중");
                  c.getSession().writeAndFlush(CWvsContext.showQuestMsg("", "칭호 - " + m.questname + " 탐험가 도전 중. " + number + "/" + m.maps.length + "개 지역 완료"));
               }
            }
            break;
         case go10000:
         case go1020000:
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
            c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         case go20000:
         case go30000:
         case go40000:
         case go50000:
         case go1000000:
         case go2000000:
         case go1010000:
         case go1010100:
         case go1010200:
         case go1010300:
         case go1010400:
            c.getSession().writeAndFlush(CField.MapNameDisplay(c.getPlayer().getMapId()));
            break;
         case ds_tuto_ill0:
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 6300));
            showIntro(c, "Effect/Direction6.img/DemonTutorial/SceneLogo");
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
                  c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  MapleMap mapto = c.getChannelServer().getMapFactory().getMap(927000000);
                  c.getPlayer().changeMap(mapto, mapto.getPortal(0));
               }
            }, 6300L);
            break;
         case ds_tuto_home_before:
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 30));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 0));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 90));
            c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text11"));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 4000));
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  MapScriptMethods.showIntro(c, "Effect/Direction6.img/DemonTutorial/Scene2");
               }
            }, 1000L);
            break;
         case ds_tuto_1_0:
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 30));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 0));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(4, 2159310));
                  NPCScriptManager.getInstance().start(c, 2159310);
               }
            }, 1000L);
            break;
         case ds_tuto_4_0:
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(true));
            c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 0));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(4, 2159344));
            NPCScriptManager.getInstance().start(c, 2159344);
            break;
         case cannon_tuto_01:
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(true));
            c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(110), 1, (byte)1);
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 0));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(4, 1096000));
            NPCScriptManager.getInstance().dispose(c);
            NPCScriptManager.getInstance().start(c, 1096000);
            break;
         case ds_tuto_5_0:
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(true));
            c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 0));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(4, 2159314));
            NPCScriptManager.getInstance().dispose(c);
            NPCScriptManager.getInstance().start(c, 2159314);
            break;
         case ds_tuto_3_0:
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 30));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text12"));
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 0));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(4, 2159311));
                  NPCScriptManager.getInstance().dispose(c);
                  NPCScriptManager.getInstance().start(c, 2159311);
               }
            }, 1000L);
            break;
         case ds_tuto_3_1:
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(true));
            c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            if (!c.getPlayer().getMap().containsNPC(2159340)) {
               c.getPlayer().getMap().spawnNpc(2159340, new Point(175, 0));
               c.getPlayer().getMap().spawnNpc(2159341, new Point(300, 0));
               c.getPlayer().getMap().spawnNpc(2159342, new Point(600, 0));
            }

            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/tuto/balloonMsg2/0", 2000, 0, -100, 1, 0));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/tuto/balloonMsg1/3", 2000, 0, -100, 1, 0));
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 0));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(4, 2159340));
                  NPCScriptManager.getInstance().dispose(c);
                  NPCScriptManager.getInstance().start(c, 2159340);
               }
            }, 1000L);
            break;
         case ds_tuto_2_before:
            c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 30));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 0));
                  c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text13"));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 500));
               }
            }, 1000L);
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text14"));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 4000));
               }
            }, 1500L);
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  MapleMap mapto = c.getChannelServer().getMapFactory().getMap(927000020);
                  c.getPlayer().changeMap(mapto, mapto.getPortal(0));
                  c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(0));
                  MapleQuest.getInstance(23204).forceStart(c.getPlayer(), 0, (String)null);
                  MapleQuest.getInstance(23205).forceComplete(c.getPlayer(), 0);
                  Map<Skill, SkillEntry> sa = new HashMap();
                  sa.put(SkillFactory.getSkill(30011170), new SkillEntry(1, (byte)1, -1L));
                  sa.put(SkillFactory.getSkill(30011169), new SkillEntry(1, (byte)1, -1L));
                  sa.put(SkillFactory.getSkill(30011168), new SkillEntry(1, (byte)1, -1L));
                  sa.put(SkillFactory.getSkill(30011167), new SkillEntry(1, (byte)1, -1L));
                  sa.put(SkillFactory.getSkill(30010166), new SkillEntry(1, (byte)1, -1L));
                  c.getPlayer().changeSkillsLevel(sa);
               }
            }, 5500L);
            break;
         case ds_tuto_1_before:
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 1));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 30));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 0));
                  c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text8"));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 500));
               }
            }, 1000L);
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text9"));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 3000));
               }
            }, 1500L);
            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  MapleMap mapto = c.getChannelServer().getMapFactory().getMap(927000010);
                  c.getPlayer().changeMap(mapto, mapto.getPortal(0));
               }
            }, 4500L);
            break;
         case ds_tuto_0_0:
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
            c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(1));
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(true));
            sa = new HashMap();
            sa.put(SkillFactory.getSkill(30011109), new SkillEntry(1, (byte)1, -1L));
            sa.put(SkillFactory.getSkill(30010110), new SkillEntry(1, (byte)1, -1L));
            sa.put(SkillFactory.getSkill(30010111), new SkillEntry(1, (byte)1, -1L));
            sa.put(SkillFactory.getSkill(30010185), new SkillEntry(1, (byte)1, -1L));
            c.getPlayer().changeSkillsLevel(sa);
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 0));
            c.getSession().writeAndFlush(CField.showEffect("demonSlayer/back"));
            c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text0"));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 500));
            c.getPlayer().setDirection(0);
            if (!c.getPlayer().getMap().containsNPC(2159307)) {
               c.getPlayer().getMap().spawnNpc(2159307, new Point(1305, 50));
            }
            break;
         case ds_tuto_2_prep:
            if (!c.getPlayer().getMap().containsNPC(2159309)) {
               c.getPlayer().getMap().spawnNpc(2159309, new Point(550, 50));
            }
            break;
         case goArcher:
            showIntro(c, "Effect/Direction3.img/archer/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case goPirate:
            showIntro(c, "Effect/Direction3.img/pirate/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case goRogue:
            showIntro(c, "Effect/Direction3.img/rogue/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case goMagician:
            showIntro(c, "Effect/Direction3.img/magician/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case goSwordman:
            showIntro(c, "Effect/Direction3.img/swordman/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case goLith:
            showIntro(c, "Effect/Direction3.img/goLith/Scene" + (c.getPlayer().getGender() == 0 ? "0" : "1"));
            break;
         case TD_MC_Openning:
            showIntro(c, "Effect/Direction2.img/open");
            break;
         case TD_MC_gasi:
            showIntro(c, "Effect/Direction2.img/gasi");
            break;
         case aranDirection:
            switch(c.getPlayer().getMapId()) {
            case 914090010:
               data = "Effect/Direction1.img/aranTutorial/Scene0";
               break;
            case 914090011:
               data = "Effect/Direction1.img/aranTutorial/Scene1" + (c.getPlayer().getGender() == 0 ? "0" : "1");
               break;
            case 914090012:
               data = "Effect/Direction1.img/aranTutorial/Scene2" + (c.getPlayer().getGender() == 0 ? "0" : "1");
               break;
            case 914090013:
               data = "Effect/Direction1.img/aranTutorial/Scene3";
               break;
            case 914090100:
               data = "Effect/Direction1.img/aranTutorial/HandedPoleArm" + (c.getPlayer().getGender() == 0 ? "0" : "1");
               break;
            case 914090200:
               data = "Effect/Direction1.img/aranTutorial/Maha";
            }

            showIntro(c, data);
            break;
         case iceCave:
            sa = new HashMap();
            sa.put(SkillFactory.getSkill(20000014), new SkillEntry(-1, (byte)0, -1L));
            sa.put(SkillFactory.getSkill(20000015), new SkillEntry(-1, (byte)0, -1L));
            sa.put(SkillFactory.getSkill(20000016), new SkillEntry(-1, (byte)0, -1L));
            sa.put(SkillFactory.getSkill(20000017), new SkillEntry(-1, (byte)0, -1L));
            sa.put(SkillFactory.getSkill(20000018), new SkillEntry(-1, (byte)0, -1L));
            c.getPlayer().changeSkillsLevel(sa);
            c.getSession().writeAndFlush(CField.EffectPacket.showWZEffect("Effect/Direction1.img/aranTutorial/ClickLirin"));
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
            c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            break;
         case rienArrow:
            if (c.getPlayer().getInfoQuest(21019).equals("miss=o;helper=clear")) {
               c.getPlayer().updateInfoQuest(21019, "miss=o;arr=o;helper=clear");
               c.getSession().writeAndFlush(CField.EffectPacket.showWZEffect("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3"));
            }
            break;
         case rien:
            if (c.getPlayer().getQuestStatus(21101) == 2 && c.getPlayer().getInfoQuest(21019).equals("miss=o;arr=o;helper=clear")) {
               c.getPlayer().updateInfoQuest(21019, "miss=o;arr=o;ck=1;helper=clear");
            }

            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
            c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
            break;
         case check_count:
            if (c.getPlayer().getMapId() == 950101010 && (!c.getPlayer().haveItem(4001433, 20) || c.getPlayer().getLevel() < 50)) {
               mapp = c.getChannelServer().getMapFactory().getMap(950101100);
               c.getPlayer().changeMap(mapp, mapp.getPortal(0));
            }
         case Massacre_first:
         case miniGameVS_Start:
            break;
         case Massacre_result:
            c.getSession().writeAndFlush(CField.showEffect("killing/fail"));
            break;
         case enter_hungryMuto:
         case enter_hungryMutoEasy:
         case enter_hungryMutoHard:
            c.getSession().writeAndFlush(CField.environmentChange("event/start", 19));
            c.getSession().writeAndFlush(CField.environmentChange("Dojang/clear", 5));
            c.getSession().writeAndFlush(CField.environmentChange("Map/Effect3.img/hungryMutoMsg/msg1", 16));
            break;
         case enter_450002024:
            if (c.getPlayer().getMap().getNPCById(3003160) == null) {
            }
            break;
         case PTtutor000:
            try {
               c.getSession().writeAndFlush(CField.UIPacket.playMovie("phantom_memory.avi", true));
               c.getSession().writeAndFlush(CField.showEffect("phantom/mapname1"));
               c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(1));
               c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 1));
               c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction6.img/effect/tuto/balloonMsg0/10", 0, 0, -110, 1, 0));
               Thread.sleep(1300L);
            } catch (InterruptedException var12) {
            }

            c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(false));
            c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(0));
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            break;
         case enter_993014200:
         case enter_993018200:
         case enter_993021200:
         case enter_993029200:
            int count = c.getPlayer().getLinkMobCount();
            c.getSession().writeAndFlush(SLFCGPacket.FrozenLinkMobCount(count));
            Iterator var17 = c.getPlayer().getMap().getAllMonstersThreadsafe().iterator();

            while(var17.hasNext()) {
               MapleMapObject monstermo = (MapleMapObject)var17.next();
               MapleMonster monster = (MapleMonster)monstermo;
               if (monster.getOwner() == c.getPlayer().getId()) {
                  monster.setHp(0L);
                  c.getPlayer().getMap().broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 1));
                  c.getPlayer().getMap().removeMapObject(monster);
                  monster.killed();
               }
            }

            if (c.getPlayer().getV("linkMob") != null && count > 0) {
               int[] smobid = new int[]{9010152, 9010153, 9010154, 9010155, 9010156, 9010157, 9010158, 9010159, 9010160, 9010161, 9010162, 9010163, 9010164, 9010165, 9010166, 9010167, 9010168, 9010169, 9010170, 9010171, 9010172, 9010173, 9010174, 9010175, 9010176, 9010177, 9010178, 9010179, 9010180, 9010181};
               int[] smobx = new int[]{1736, 1872, 1944, 2074, 2154, 2237, 2368, 2435, 2567, 2647, 2750};
               int[] smoby = new int[]{399, 132, -81};

               for(int i2 = 0; i2 < smobx.length; ++i2) {
                  for(int g = 0; g < smoby.length; ++g) {
                     l = smobid[Integer.parseInt(c.getPlayer().getV("linkMob"))];
                     MapleMonster mob = MapleLifeFactory.getMonster(l);
                     mob.setOwner(c.getPlayer().getId());
                     c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, new Point(smobx[i2], smoby[g]));
                  }
               }
            }
         case enter_993001000:
            if (c.getPlayer().getKeyValue(18838, "stage") == -1L) {
               c.getPlayer().setKeyValue(18838, "count", "99");
               c.getPlayer().setKeyValue(18838, "stageT", "0");
               c.getPlayer().setKeyValue(18838, "hack", "0");
               c.getPlayer().setKeyValue(18838, "stage", "0");
               c.getPlayer().setKeyValue(18838, "mode", "0");
            }

            List<PlatformerRecord> records = c.getPlayer().getPlatformerRecords();
            MapleCharacter var10000 = c.getPlayer();
            MapleCharacter var10003 = c.getPlayer();
            var10000.setKeyValue(18838, "count", var10003.getKeyValue(18838, "count").makeConcatWithConstants<invokedynamic>(var10003.getKeyValue(18838, "count")));
            c.getPlayer().setKeyValue(18838, "stage", records.size().makeConcatWithConstants<invokedynamic>(records.size()));
            q = 0;

            while(true) {
               if (q >= records.size()) {
                  break label503;
               }

               PlatformerRecord rec = (PlatformerRecord)records.get(q);
               c.getPlayer().setKeyValue(18839 + q, "isClear", "1");
               c.getPlayer().setKeyValue(18839 + q, "br", rec.getClearTime().makeConcatWithConstants<invokedynamic>(rec.getClearTime()));
               c.getPlayer().setKeyValue(18839 + q, "cs", rec.getStars().makeConcatWithConstants<invokedynamic>(rec.getStars()));
               ++q;
            }
         case enter_910143000:
            MapleItemInformationProvider.getInstance().getItemEffect(2210217).applyTo(c.getPlayer(), true);
            break;
         case fireWolf_Enter:
            if (c.getPlayer().getMap().getAllMonster().size() == 0) {
               mf = ChannelServer.getInstance(c.getChannel()).getMapFactory();
               MapleMap map = mf.getMap(993000500);
               map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9101078), new Point(25, 353));
            }

            c.getPlayer().setFWolfDamage(0L);
            c.getPlayer().setFWolfKiller(false);
            c.getSession().writeAndFlush(CField.startMapEffect("불꽃늑대를 처치할 용사가 늘었군. 어서 녀석을 공격해! 머무를 수 있는 시간은 30초 뿐이야!", 5120159, true));
            c.getSession().writeAndFlush(CField.getClock(30));
            Timer.MapTimer.getInstance().schedule(() -> {
               if (c.getPlayer().getMapId() == 993000500) {
                  c.getPlayer().warp(993000600);
               }

            }, 30000L);
            break;
         case enter_993192002:
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().start(c, 2007, "BloomingForest_0");
            break;
         case enter_993192003:
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().start(c, 2007, "BloomingForest_1");
            break;
         case enter_993192004:
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().start(c, 2007, "BloomingForest_RedFlower");
            break;
         case enter_993192005:
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().start(c, 2007, "BloomingForest_BlueFlower");
            break;
         case enter_993192006:
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().start(c, 2007, "BloomingForest_YellowFlower");
            break;
         case enter_993192007:
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().start(c, 2007, "BloomingForest_2");
            break;
         case BloomingRace_reset:
            c.send(SLFCGPacket.ContentsWaiting(c.getPlayer(), 0, 11, 5, 1, 25));
            if (c.getPlayer().getMap().getCustomTime(c.getPlayer().getMap().getId()) != null) {
               c.send(CField.getClock(c.getPlayer().getMap().getCustomTime(c.getPlayer().getMap().getId()) / 1000));
            }
            break;
         case enter_993194001:
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().start(c, 2007, "MapleLive_0");
            break;
         case enter_993194002:
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().start(c, 2007, "MapleLive_1");
            break;
         default:
            FileoutputUtil.log("Log_Script_Except.rtf", "Unhandled script : " + scriptName + ", type : onUserEnter - MAPID " + c.getPlayer().getMapId());
         }

         byte var35 = -1;
         switch(scriptName.hashCode()) {
         case -920219021:
            if (scriptName.equals("180onUser")) {
               var35 = 0;
            }
         default:
            switch(var35) {
            case 0:
               if (!c.getPlayer().isGM()) {
                  c.getPlayer().warp(100000000);
                  c.getPlayer().ban("운영자맵 침입", true, true, true);
                  c.disconnect(true, false);
               }
            default:
            }
         }
      }
   }

   private static void showIntro(MapleClient c, String data) {
      c.getSession().writeAndFlush(CField.EffectPacket.showWZEffect(data));
   }

   private static void sendDojoClock(MapleClient c) {
      c.getSession().writeAndFlush(CField.getDojoClock(900, (int)((System.currentTimeMillis() - (long)c.getPlayer().getDojoStartTime() - c.getPlayer().getDojoCoolTime()) / 1000L)));
   }

   private static void sendDojoStart(MapleClient c, int stage) {
      c.getSession().writeAndFlush(CField.environmentChange("Dojang/start", 5));
      c.getSession().writeAndFlush(CField.environmentChange("dojang/start/stage", 19));
      c.getSession().writeAndFlush(CField.environmentChange("dojang/start/number/" + stage, 19));
      c.getSession().writeAndFlush(CField.getDojoClockStop(false, 900));
      c.getSession().writeAndFlush(CField.trembleEffect(0, 1));
   }

   private static void handlePinkBeanStart(MapleClient c) {
      MapleMap map = c.getPlayer().getMap();
      if (!map.containsNPC(2141000)) {
         map.spawnNpc(2141000, new Point(-190, -42));
      }

   }

   private static void reloadWitchTower(MapleClient c) {
      MapleMap map = c.getPlayer().getMap();
      map.killAllMonsters(false);
      int level = c.getPlayer().getLevel();
      int mob;
      if (level <= 10) {
         mob = 9300367;
      } else if (level <= 20) {
         mob = 9300368;
      } else if (level <= 30) {
         mob = 9300369;
      } else if (level <= 40) {
         mob = 9300370;
      } else if (level <= 50) {
         mob = 9300371;
      } else if (level <= 60) {
         mob = 9300372;
      } else if (level <= 70) {
         mob = 9300373;
      } else if (level <= 80) {
         mob = 9300374;
      } else if (level <= 90) {
         mob = 9300375;
      } else if (level <= 100) {
         mob = 9300376;
      } else {
         mob = 9300377;
      }

      MapleMonster theMob = MapleLifeFactory.getMonster(mob);
      OverrideMonsterStats oms = new OverrideMonsterStats();
      oms.setOMp(theMob.getMobMaxMp());
      oms.setOExp(theMob.getMobExp());
      oms.setOHp((long)Math.ceil((double)(theMob.getMobMaxHp() * (long)level) / 5.0D));
      theMob.setOverrideStats(oms);
      map.spawnMonsterOnGroundBelow(theMob, witchTowerPos);
   }

   public static void startDirectionInfo(MapleCharacter chr, boolean start) {
      final MapleClient c = chr.getClient();
      MapleNodes.DirectionInfo di = chr.getMap().getDirectionInfo(start ? 0 : chr.getDirection());
      if (di != null && di.eventQ.size() > 0) {
         if (start) {
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(true));
            c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 4));
         } else {
            Iterator var7 = di.eventQ.iterator();

            while(var7.hasNext()) {
               String s = (String)var7.next();
               switch(MapScriptMethods.directionInfo.fromString(s)) {
               case merTutorDrecotion01:
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/mercedesInIce/merBalloon/0", 2000, 0, -100, 1, 0));
               case merTutorDrecotion02:
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/mercedesInIce/merBalloon/1", 2000, 0, -100, 1, 0));
               case merTutorDrecotion03:
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 2));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/mercedesInIce/merBalloon/2", 2000, 0, -100, 1, 0));
               case merTutorDrecotion04:
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 2));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/mercedesInIce/merBalloon/3", 2000, 0, -100, 1, 0));
               case merTutorDrecotion05:
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 2));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/mercedesInIce/merBalloon/4", 2000, 0, -100, 1, 0));
                  Timer.EventTimer.getInstance().schedule(new Runnable() {
                     public void run() {
                        c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 2));
                        c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
                        c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/mercedesInIce/merBalloon/5", 2000, 0, -100, 1, 0));
                     }
                  }, 2000L);
                  Timer.EventTimer.getInstance().schedule(new Runnable() {
                     public void run() {
                        c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(0));
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     }
                  }, 4000L);
               case merTutorDrecotion12:
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 2));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo("Effect/Direction5.img/effect/mercedesInIce/merBalloon/8", 2000, 0, -100, 1, 0));
                  c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(0));
               case merTutorDrecotion21:
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 1));
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
                  MapleMap mapto = c.getChannelServer().getMapFactory().getMap(910150005);
                  c.getPlayer().changeMap(mapto, mapto.getPortal(0));
               case ds_tuto_0_2:
                  c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text1"));
               case ds_tuto_0_1:
                  c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(3, 2));
               case ds_tuto_0_3:
                  c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text2"));
                  Timer.EventTimer.getInstance().schedule(new Runnable() {
                     public void run() {
                        c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 4000));
                        c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text3"));
                     }
                  }, 2000L);
                  Timer.EventTimer.getInstance().schedule(new Runnable() {
                     public void run() {
                        c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 500));
                        c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text4"));
                     }
                  }, 6000L);
                  Timer.EventTimer.getInstance().schedule(new Runnable() {
                     public void run() {
                        c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 4000));
                        c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text5"));
                     }
                  }, 6500L);
                  Timer.EventTimer.getInstance().schedule(new Runnable() {
                     public void run() {
                        c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 500));
                        c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text6"));
                     }
                  }, 10500L);
                  Timer.EventTimer.getInstance().schedule(new Runnable() {
                     public void run() {
                        c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 4000));
                        c.getSession().writeAndFlush(CField.showEffect("demonSlayer/text7"));
                     }
                  }, 11000L);
                  Timer.EventTimer.getInstance().schedule(new Runnable() {
                     public void run() {
                        c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(4, 2159307));
                        NPCScriptManager.getInstance().dispose(c);
                        NPCScriptManager.getInstance().start(c, 2159307);
                     }
                  }, 15000L);
               }
            }
         }

         c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 2000));
         chr.setDirection(chr.getDirection() + 1);
         if (chr.getMap().getDirectionInfo(chr.getDirection()) == null) {
            chr.setDirection(-1);
         }
      } else if (start) {
         switch(chr.getMapId()) {
         case 931050300:
            while(chr.getLevel() < 10) {
               chr.levelUp();
            }

            MapleMap mapto = c.getChannelServer().getMapFactory().getMap(931050000);
            chr.changeMap(mapto, mapto.getPortal(0));
         }
      }

   }

   private static enum onFirstUserEnter {
      dojang_Eff,
      dojang_Msg,
      PinkBeen_before,
      onRewordMap,
      StageMsg_together,
      StageMsg_crack,
      StageMsg_davy,
      StageMsg_goddess,
      party6weatherMsg,
      StageMsg_juliet,
      StageMsg_romio,
      will_phase1,
      will_phase2,
      will_phase3,
      WUK_StageEnter,
      JinHillah_onFirstUserEnter,
      moonrabbit_mapEnter,
      Fenter_450004250,
      Fenter_450004150,
      astaroth_summon,
      boss_Ravana,
      boss_Ravana_mirror,
      killing_BonusSetting,
      killing_MapSetting,
      metro_firstSetting,
      balog_bonusSetting,
      balog_summon,
      easy_balog_summon,
      Sky_TrapFEnter,
      pyramidWeather,
      shammos_Fenter,
      PRaid_D_Fenter,
      PRaid_B_Fenter,
      summon_pepeking,
      Xerxes_summon,
      VanLeon_Before,
      cygnus_Summon,
      storymap_scenario,
      shammos_FStart,
      kenta_mapEnter,
      iceman_FEnter,
      iceman_Boss,
      Polo_Defence,
      prisonBreak_mapEnter,
      Visitor_Cube_poison,
      Visitor_Cube_Hunting_Enter_First,
      VisitorCubePhase00_Start,
      visitorCube_addmobEnter,
      Visitor_Cube_PickAnswer_Enter_First_1,
      visitorCube_medicroom_Enter,
      visitorCube_iceyunna_Enter,
      Visitor_Cube_AreaCheck_Enter_First,
      visitorCube_boomboom_Enter,
      visitorCube_boomboom2_Enter,
      CubeBossbang_Enter,
      MalayBoss_Int,
      mPark_summonBoss,
      hontale_boss1,
      hontale_boss2,
      queen_summon0,
      pierre_Summon1,
      pierre_Summon,
      banban_Summon,
      firstenter_bossBlackMage,
      dusk_onFirstUserEnter,
      NULL;

      private static MapScriptMethods.onFirstUserEnter fromString(String Str) {
         try {
            return valueOf(Str);
         } catch (IllegalArgumentException var2) {
            return NULL;
         }
      }
   }

   private static enum onUserEnter {
      babyPigMap,
      crash_Dragon,
      evanleaveD,
      getDragonEgg,
      meetWithDragon,
      go1010100,
      go1010200,
      go1010300,
      go1010400,
      will_phase1_everyone,
      will_phase2_everyone,
      will_phase3_everyone,
      dunkel_timeRecord,
      dunkel_boss,
      JinHillah_onUserEnter,
      evanPromotion,
      PromiseDragon,
      evanTogether,
      incubation_dragon,
      TD_MC_Openning,
      TD_MC_gasi,
      TD_MC_title,
      magnus_enter_HP,
      Akayrum_ExpeditionEnter,
      bhb2_scEnterHp,
      bhb3_scEnterHp,
      cygnusJobTutorial,
      cygnusTest,
      Polo_Wave,
      Fritto_Eagle_Enter,
      Fritto_Egg_Enter,
      Fritto_Dancing_Enter,
      startEreb,
      enter_450004150,
      PinkBeenJob_Event,
      dojang_Msg,
      dojang_1st,
      reundodraco,
      undomorphdarco,
      explorationPoint,
      goAdventure,
      go10000,
      go20000,
      go30000,
      go40000,
      go50000,
      go1000000,
      go1010000,
      go1020000,
      go2000000,
      goArcher,
      goPirate,
      goRogue,
      goMagician,
      goSwordman,
      goLith,
      iceCave,
      mirrorCave,
      aranDirection,
      rienArrow,
      rien,
      check_count,
      Massacre_first,
      Massacre_result,
      aranTutorAlone,
      evanAlone,
      dojang_QcheckSet,
      Sky_StageEnter,
      outCase,
      balog_buff,
      balog_dateSet,
      Sky_BossEnter,
      Sky_GateMapEnter,
      shammos_Enter,
      shammos_Result,
      shammos_Base,
      dollCave00,
      dollCave01,
      dollCave02,
      Sky_Quest,
      enterBlackfrog,
      onSDI,
      blackSDI,
      summonIceWall,
      metro_firstSetting,
      start_itemTake,
      findvioleta,
      pepeking_effect,
      TD_MC_keycheck,
      TD_MC_gasi2,
      in_secretroom,
      sealGarden,
      TD_NC_title,
      TD_neo_BossEnter,
      PRaid_D_Enter,
      PRaid_B_Enter,
      PRaid_Revive,
      PRaid_W_Enter,
      PRaid_WinEnter,
      PRaid_FailEnter,
      Resi_tutor10,
      Resi_tutor20,
      Resi_tutor30,
      Resi_tutor40,
      Resi_tutor50,
      Resi_tutor60,
      Resi_tutor70,
      Resi_tutor80,
      Resi_tutor50_1,
      summonSchiller,
      q31102e,
      q31103s,
      jail,
      VanLeon_ExpeditionEnter,
      cygnus_ExpeditionEnter,
      knights_Summon,
      TCMobrevive,
      mPark_stageEff,
      mPark_Enter,
      moonrabbit_takeawayitem,
      StageMsg_crack,
      shammos_Start,
      iceman_Enter,
      prisonBreak_1stageEnter,
      VisitorleaveDirectionMode,
      visitorPT_Enter,
      VisitorCubePhase00_Enter,
      visitor_ReviveMap,
      cannon_tuto_01,
      cannon_tuto_direction,
      cannon_tuto_direction1,
      cannon_tuto_direction2,
      userInBattleSquare,
      merTutorDrecotion00,
      merTutorDrecotion10,
      merTutorDrecotion20,
      merStandAlone,
      merOutStandAlone,
      merTutorSleep00,
      merTutorSleep01,
      merTutorSleep02,
      EntereurelTW,
      ds_tuto_ill0,
      ds_tuto_0_0,
      ds_tuto_1_0,
      ds_tuto_3_0,
      ds_tuto_3_1,
      ds_tuto_4_0,
      ds_tuto_5_0,
      ds_tuto_2_prep,
      ds_tuto_1_before,
      ds_tuto_2_before,
      ds_tuto_home_before,
      ds_tuto_ani,
      PTtutor000,
      enter_993014200,
      enter_993018200,
      enter_993021200,
      enter_993029200,
      enter_hungryMuto,
      enter_hungryMutoEasy,
      enter_hungryMutoHard,
      enter_450002024,
      enter_993001000,
      enter_pfTutorialStage,
      enter_910143000,
      enter_450004200,
      enter_993192002,
      enter_993192003,
      enter_993192004,
      enter_993192005,
      enter_993192006,
      enter_993192007,
      BloomingRace_reset,
      enter_993194001,
      enter_993194002,
      miniGameVS_Start,
      fireWolf_Enter,
      NULL;

      private static MapScriptMethods.onUserEnter fromString(String Str) {
         try {
            return valueOf(Str);
         } catch (IllegalArgumentException var2) {
            return NULL;
         }
      }
   }

   private static enum directionInfo {
      merTutorDrecotion01,
      merTutorDrecotion02,
      merTutorDrecotion03,
      merTutorDrecotion04,
      merTutorDrecotion05,
      merTutorDrecotion12,
      merTutorDrecotion21,
      ds_tuto_0_1,
      ds_tuto_0_2,
      ds_tuto_0_3,
      NULL;

      private static MapScriptMethods.directionInfo fromString(String Str) {
         try {
            return valueOf(Str);
         } catch (IllegalArgumentException var2) {
            return NULL;
         }
      }
   }
}

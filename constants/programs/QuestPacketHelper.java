package constants.programs;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager.LookAndFeelInfo;
import tools.HexTool;
import tools.data.ByteArrayByteStream;
import tools.data.LittleEndianAccessor;

public class QuestPacketHelper extends JFrame {
   private List<String> list = new ArrayList();
   private JCheckBox BOSS_ENV1;
   private JCheckBox BlackLabel;
   private JCheckBox MoveAction;
   private JCheckBox NPC_MOTION;
   private JCheckBox NpcAction;
   private JCheckBox NpcSpecialAction;
   private JCheckBox NpcSpecialAction2;
   private JCheckBox NpcSpecialAction4;
   private JTextPane Packettext;
   private JCheckBox PlaySound;
   private JCheckBox ShowEffect;
   private JCheckBox SpawnDirectonInfo;
   private JCheckBox SpawnNPC;
   private JCheckBox UserInGameDirectionEvent;
   private JCheckBox cm;
   private JButton jButton1;
   private JButton jButton2;
   private JLabel jLabel1;
   private JList<String> jList1;
   private JScrollPane jScrollPane1;
   private JScrollPane jScrollPane4;
   private JCheckBox qm;

   public QuestPacketHelper() {
      this.initComponents();
   }

   private void initComponents() {
      this.jButton1 = new JButton();
      this.jLabel1 = new JLabel();
      this.UserInGameDirectionEvent = new JCheckBox();
      this.BOSS_ENV1 = new JCheckBox();
      this.jScrollPane4 = new JScrollPane();
      this.Packettext = new JTextPane();
      this.jScrollPane1 = new JScrollPane();
      this.jList1 = new JList();
      this.jButton2 = new JButton();
      this.cm = new JCheckBox();
      this.qm = new JCheckBox();
      this.BlackLabel = new JCheckBox();
      this.ShowEffect = new JCheckBox();
      this.SpawnNPC = new JCheckBox();
      this.NpcSpecialAction = new JCheckBox();
      this.NpcSpecialAction2 = new JCheckBox();
      this.SpawnDirectonInfo = new JCheckBox();
      this.MoveAction = new JCheckBox();
      this.PlaySound = new JCheckBox();
      this.NpcAction = new JCheckBox();
      this.NpcSpecialAction4 = new JCheckBox();
      this.NPC_MOTION = new JCheckBox();
      this.setDefaultCloseOperation(3);
      this.setTitle("QuestPacketHelper");
      this.jButton1.setText("변환");
      this.jButton1.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            QuestPacketHelper.this.jButton1ActionPerformed(evt);
         }
      });
      this.jLabel1.setText("퀘스트");
      this.UserInGameDirectionEvent.setText("UserInGameDirectionEvent");
      this.UserInGameDirectionEvent.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            QuestPacketHelper.this.UserInGameDirectionEventActionPerformed(evt);
         }
      });
      this.BOSS_ENV1.setText("BOSS_ENV");
      this.jScrollPane4.setViewportView(this.Packettext);
      this.jScrollPane1.setViewportView(this.jList1);
      this.jButton2.setText("청소");
      this.jButton2.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            QuestPacketHelper.this.jButton2ActionPerformed(evt);
         }
      });
      this.cm.setText("cm");
      this.qm.setText("qm");
      this.BlackLabel.setText("BlackLabel");
      this.ShowEffect.setText("ShowEffect");
      this.SpawnNPC.setText("SpawnNPC");
      this.NpcSpecialAction.setText("SAction");
      this.NpcSpecialAction2.setText("SAction2");
      this.SpawnDirectonInfo.setText("SpawnDirectonInfo");
      this.MoveAction.setText("MoveAction");
      this.PlaySound.setText("PlaySound");
      this.NpcAction.setText("NPCAction");
      this.NpcSpecialAction4.setText("SAction4");
      this.NPC_MOTION.setText("NPCMOTION");
      GroupLayout layout = new GroupLayout(this.getContentPane());
      this.getContentPane().setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(this.jScrollPane1, -2, 635, -2).addContainerGap(-1, 32767)).addGroup(Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(110, 110, 110).addComponent(this.jScrollPane4, -2, 433, -2)).addGroup(layout.createSequentialGroup().addGap(302, 302, 302).addComponent(this.jLabel1, -2, 40, -2)).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(200, 200, 200).addComponent(this.jButton1, -2, 237, -2)).addGroup(layout.createSequentialGroup().addGap(40, 40, 40).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(this.cm).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.qm)).addGroup(layout.createSequentialGroup().addComponent(this.BOSS_ENV1).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.BlackLabel).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.ShowEffect).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.SpawnNPC).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.NpcSpecialAction, -2, 72, -2)).addGroup(layout.createSequentialGroup().addComponent(this.UserInGameDirectionEvent).addGap(17, 17, 17).addComponent(this.SpawnDirectonInfo).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.PlaySound))))).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.jButton2).addGroup(layout.createSequentialGroup().addComponent(this.NpcAction).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.MoveAction)).addGroup(layout.createSequentialGroup().addComponent(this.NpcSpecialAction2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.NpcSpecialAction4).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.NPC_MOTION))))).addGap(76, 76, 76)));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.jLabel1, -2, 20, -2).addGap(32, 32, 32).addComponent(this.jScrollPane4, -2, 99, -2).addGap(18, 18, 18).addComponent(this.jScrollPane1, -2, 204, -2).addGap(18, 18, 18).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.BOSS_ENV1).addComponent(this.BlackLabel).addComponent(this.ShowEffect).addComponent(this.SpawnNPC).addComponent(this.NpcSpecialAction).addComponent(this.NpcSpecialAction2).addComponent(this.NpcSpecialAction4).addComponent(this.NPC_MOTION)).addPreferredGap(ComponentPlacement.RELATED, 47, 32767).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.UserInGameDirectionEvent).addComponent(this.SpawnDirectonInfo).addComponent(this.PlaySound).addComponent(this.NpcAction).addComponent(this.MoveAction)).addGap(18, 18, 18).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.cm).addComponent(this.qm)).addGap(21, 21, 21)).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.jButton1, -2, 50, -2).addComponent(this.jButton2))).addGap(32, 32, 32)));
      this.pack();
   }

   private void jButton1ActionPerformed(ActionEvent evt) {
      byte[] data = HexTool.getByteArrayFromHexString(this.Packettext.getText());
      LittleEndianAccessor slea = new LittleEndianAccessor(new ByteArrayByteStream(data));
      String ab = "";
      if (this.cm.isSelected() || this.qm.isSelected()) {
         if (this.cm.isSelected()) {
            ab = ab + "cm";
         } else if (this.qm.isSelected()) {
            ab = ab + "qm";
         }

         String a;
         if (this.BOSS_ENV1.isSelected()) {
            slea.readByte();
            a = ab + ".getClient().send(SLFCGPacket.MakeBlind(";
            a = a + slea.readByte() + ", " + slea.readShort() + ", " + slea.readShort() + ", " + slea.readShort() + ", " + slea.readShort() + ", " + slea.readInt() + ", " + slea.readInt() + "));";
            this.list.add(a);
            this.jList1.setListData((String[])this.list.toArray(new String[0]));
         } else {
            String ani2;
            if (this.UserInGameDirectionEvent.isSelected()) {
               String a = ab + ".getClient().send(SLFCGPacket.InGameDirectionEvent(";
               ani2 = "\"\"";
               int type = slea.readByte();
               switch(type) {
               case 0:
                  a = a + ani2 + ", " + type + ", " + type + ", " + slea.readInt() + ", " + slea.readInt();
                  break;
               case 1:
                  int time = slea.readInt();
                  this.list.add("statusplus(" + time + ");");
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
                  return;
               case 2:
                  String var10000 = slea.readMapleAsciiString();
                  ani2 = "\"" + var10000 + "\"";
                  a = a + ani2 + ", " + type + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt();
                  slea.skip(1);
                  a = a + ", " + slea.readInt();
                  boolean active = slea.readByte() > 0;
                  if (active) {
                     a = a + ", 1, " + slea.readInt() + ", " + slea.readByte() + ", " + slea.readByte();
                  } else {
                     a = a + ", 0, 0, 0, 0";
                  }

                  a = a + ", " + slea.readByte();
                  break;
               case 3:
               case 6:
               case 11:
               case 18:
               case 22:
                  a = a + ani2 + ", " + type + ", " + slea.readInt();
                  break;
               case 4:
                  ani2 = "\"" + slea.readMapleAsciiString() + "\"";
                  a = a + ani2 + ", " + type + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt();
                  break;
               case 5:
                  int type1 = slea.readByte();
                  a = a + ani2 + ", " + type + ", " + type1 + ", " + slea.readInt();
                  int type2 = slea.readByte();
                  a = a + ", " + type2;
                  if (type1 > 0 && type2 > 0) {
                     a = a + ", " + slea.readInt() + ", " + slea.readInt();
                  }
                  break;
               case 7:
                  slea.skip(1);
                  a = a + ani2 + ", " + type + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt();
               case 8:
               case 9:
               case 15:
               case 16:
               case 21:
               default:
                  break;
               case 10:
               case 19:
               case 20:
                  a = a + ani2 + ", " + type + ", " + slea.readByte();
                  break;
               case 12:
                  ani2 = "\"" + slea.readMapleAsciiString() + "\"";
                  a = a + ani2 + ", " + type + ", " + slea.readByte();
                  break;
               case 13:
                  ani2 = "\"" + slea.readMapleAsciiString() + "\"";
                  a = a + ani2 + ", " + type + ", " + slea.readByte() + ", " + slea.readShort() + ", " + slea.readInt() + ", " + slea.readInt();
                  break;
               case 14:
                  a = a + ani2 + ", " + type + ", " + slea.readByte() + ", " + slea.readInt();
                  break;
               case 17:
                  a = a + ani2 + ", " + type + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readByte();
                  break;
               case 23:
                  ani2 = "\"" + slea.readMapleAsciiString() + "\"";
                  a = a + ani2;
               }

               a = a + "));";
               this.list.add(a);
               this.jList1.setListData((String[])this.list.toArray(new String[0]));
            } else if (this.BlackLabel.isSelected()) {
               a = ab + ".getClient().send(SLFCGPacket.BlackLabel(";
               slea.readByte();
               a = a + "\"" + slea.readMapleAsciiString() + "\", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt();
               a = a + "));";
               this.list.add(a);
               this.jList1.setListData((String[])this.list.toArray(new String[0]));
            } else {
               int type2;
               if (this.ShowEffect.isSelected()) {
                  a = ab + ".getClient().send(SLFCGPacket.ShowEffectChatNpc(";
                  slea.readByte();
                  a = a + slea.readByte() + ", ";
                  int unk1 = slea.readInt();
                  type2 = slea.readInt();
                  a = a + "\"" + slea.readMapleAsciiString() + "\", " + unk1 + ", " + type2 + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt();
                  a = a + "));";
                  this.list.add(a);
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
               } else if (this.SpawnNPC.isSelected()) {
                  a = ab + ".spawnNpc2(";
                  slea.readByte();
                  slea.readInt();
                  a = a + slea.readInt() + ", new Point(" + slea.readShort() + ", " + slea.readShort() + "));";
                  this.list.add(a);
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
               } else if (this.NpcSpecialAction.isSelected()) {
                  a = ab + ".getClient().send(SLFCGPacket.SetNpcSpecialAction(oid, ";
                  slea.skip(4);
                  a = a + "\"" + slea.readMapleAsciiString() + "\", " + slea.readInt() + ", " + (slea.readByte() == 1 ? "true" : "false") + "));";
                  this.list.add(a);
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
               } else if (this.NpcSpecialAction2.isSelected()) {
                  a = ab + ".getClient().send(SLFCGPacket.SetNpcSpecialAction2(oid, ";
                  slea.skip(4);
                  a = a + slea.readInt() + ", " + slea.readInt() + "));";
                  this.list.add(a);
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
               } else if (this.SpawnDirectonInfo.isSelected()) {
                  a = ab + ".getClient().send(SLFCGPacket.SpawnDirectionObject(";
                  a = a + slea.readShort() + ", " + slea.readShort() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + "));";
                  this.list.add(a);
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
               } else if (this.MoveAction.isSelected()) {
                  a = ab + ".getClient().send(SLFCGPacket.getNpcMoveAction(oid, ";
                  slea.skip(4);
                  a = a + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + "));";
                  this.list.add(a);
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
               } else if (this.PlaySound.isSelected()) {
                  a = ab + ".getClient().send(SLFCGPacket.PlayAmientSound(";
                  a = a + "\"" + slea.readMapleAsciiString() + "\", " + slea.readInt() + ", " + slea.readInt() + "));";
                  this.list.add(a);
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
               } else if (this.NpcAction.isSelected()) {
                  a = ab + ".NpcAction(" + slea.readInt() + ", \"" + slea + "\");";
                  this.list.add(a);
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
               } else if (this.NpcSpecialAction4.isSelected()) {
                  a = ab + ".getClient().send(SLFCGPacket.SetNpcSpecialAction4(oid, ";
                  slea.skip(4);
                  int type1 = slea.readByte();
                  type2 = slea.readInt();
                  String ani = slea.readMapleAsciiString();
                  int type3 = slea.readInt();
                  ani2 = slea.readMapleAsciiString();
                  int type4 = slea.readInt();
                  a = a + "\"" + ani + "\", \"" + ani2 + "\", " + type1 + ", " + type2 + ", " + type3 + ", " + type4 + "));";
                  this.list.add(a);
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
               } else if (this.NPC_MOTION.isSelected()) {
                  a = ab + ".getClient().send(SLFCGPacket.SetNpcMotion(" + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + ", " + slea.readInt() + "));";
                  this.list.add(a);
                  this.jList1.setListData((String[])this.list.toArray(new String[0]));
               }
            }
         }

      }
   }

   private void UserInGameDirectionEventActionPerformed(ActionEvent evt) {
   }

   private void jButton2ActionPerformed(ActionEvent evt) {
      this.list.clear();
      this.jList1.setListData((String[])this.list.toArray(new String[0]));
   }

   public static void main(String[] args) {
      try {
         LookAndFeelInfo[] var1 = UIManager.getInstalledLookAndFeels();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            LookAndFeelInfo info = var1[var3];
            if ("Nimbus".equals(info.getName())) {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (ClassNotFoundException var5) {
         Logger.getLogger(QuestPacketHelper.class.getName()).log(Level.SEVERE, (String)null, var5);
      } catch (InstantiationException var6) {
         Logger.getLogger(QuestPacketHelper.class.getName()).log(Level.SEVERE, (String)null, var6);
      } catch (IllegalAccessException var7) {
         Logger.getLogger(QuestPacketHelper.class.getName()).log(Level.SEVERE, (String)null, var7);
      } catch (UnsupportedLookAndFeelException var8) {
         Logger.getLogger(QuestPacketHelper.class.getName()).log(Level.SEVERE, (String)null, var8);
      }

      EventQueue.invokeLater(new Runnable() {
         public void run() {
            (new QuestPacketHelper()).setVisible(true);
         }
      });
   }
}

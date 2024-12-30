package provider;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MapleData implements MapleDataEntity, Iterable<MapleData> {
   private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
   private Node node;
   private File imageDataDir;

   private MapleData(Node node) {
      this.node = node;
   }

   public MapleData(FileInputStream fis, File imageDataDir) {
      try {
         this.node = documentBuilderFactory.newDocumentBuilder().parse(fis).getFirstChild();
      } catch (ParserConfigurationException var4) {
         throw new RuntimeException(var4);
      } catch (SAXException var5) {
         throw new RuntimeException(var5);
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      }

      this.imageDataDir = imageDataDir;
   }

   public MapleData getChildByPath(String path) {
      String[] segments = path.split("/");
      if (segments[0].equals("..")) {
         return ((MapleData)this.getParent()).getChildByPath(path.substring(path.indexOf("/") + 1));
      } else {
         Node myNode = this.node;

         for(int x = 0; x < segments.length; ++x) {
            NodeList childNodes = myNode.getChildNodes();
            boolean foundChild = false;

            for(int i = 0; i < childNodes.getLength(); ++i) {
               Node childNode = childNodes.item(i);
               if (childNode != null && childNode.getAttributes() != null && childNode.getAttributes().getNamedItem("name") != null && childNode.getNodeType() == 1 && childNode.getAttributes().getNamedItem("name").getNodeValue().equals(segments[x])) {
                  myNode = childNode;
                  foundChild = true;
                  break;
               }
            }

            if (!foundChild) {
               return null;
            }
         }

         MapleData ret = new MapleData(myNode);
         File var10003 = this.imageDataDir;
         String var10004 = this.getName();
         ret.imageDataDir = (new File(var10003, var10004 + "/" + path)).getParentFile();
         return ret;
      }
   }

   public List<MapleData> getChildren() {
      List<MapleData> ret = new ArrayList();
      NodeList childNodes = this.node.getChildNodes();

      for(int i = 0; i < childNodes.getLength(); ++i) {
         Node childNode = childNodes.item(i);
         if (childNode != null && childNode.getNodeType() == 1) {
            MapleData child = new MapleData(childNode);
            child.imageDataDir = new File(this.imageDataDir, this.getName());
            ret.add(child);
         }
      }

      return ret;
   }

   public Object getData() {
      NamedNodeMap attributes = this.node.getAttributes();
      MapleDataType type = this.getType();
      switch(type) {
      case DOUBLE:
         return Double.parseDouble(attributes.getNamedItem("value").getNodeValue());
      case FLOAT:
         return Float.parseFloat(attributes.getNamedItem("value").getNodeValue());
      case INT:
         return Integer.parseInt(attributes.getNamedItem("value").getNodeValue());
      case SHORT:
         return Short.parseShort(attributes.getNamedItem("value").getNodeValue());
      case LONG:
         return Long.parseLong(attributes.getNamedItem("value").getNodeValue());
      case STRING:
      case UOL:
         return attributes.getNamedItem("value").getNodeValue();
      case VECTOR:
         return new Point(Integer.parseInt(attributes.getNamedItem("x").getNodeValue()), Integer.parseInt(attributes.getNamedItem("y").getNodeValue()));
      case CANVAS:
         return new MapleCanvas(Integer.parseInt(attributes.getNamedItem("width").getNodeValue()), Integer.parseInt(attributes.getNamedItem("height").getNodeValue()), new File(this.imageDataDir, this.getName() + ".png"));
      default:
         return null;
      }
   }

   public final MapleDataType getType() {
      String nodeName = this.node.getNodeName();
      if (nodeName.equals("imgdir")) {
         return MapleDataType.PROPERTY;
      } else if (nodeName.equals("canvas")) {
         return MapleDataType.CANVAS;
      } else if (nodeName.equals("convex")) {
         return MapleDataType.CONVEX;
      } else if (nodeName.equals("sound")) {
         return MapleDataType.SOUND;
      } else if (nodeName.equals("uol")) {
         return MapleDataType.UOL;
      } else if (nodeName.equals("double")) {
         return MapleDataType.DOUBLE;
      } else if (nodeName.equals("float")) {
         return MapleDataType.FLOAT;
      } else if (nodeName.equals("int")) {
         return MapleDataType.INT;
      } else if (nodeName.equals("short")) {
         return MapleDataType.SHORT;
      } else if (nodeName.equals("long")) {
         return MapleDataType.LONG;
      } else if (nodeName.equals("string")) {
         return MapleDataType.STRING;
      } else if (nodeName.equals("vector")) {
         return MapleDataType.VECTOR;
      } else {
         return nodeName.equals("null") ? MapleDataType.IMG_0x00 : null;
      }
   }

   public MapleDataEntity getParent() {
      Node parentNode = this.node.getParentNode();
      if (parentNode.getNodeType() == 9) {
         return null;
      } else {
         MapleData parentData = new MapleData(parentNode);
         parentData.imageDataDir = this.imageDataDir.getParentFile();
         return parentData;
      }
   }

   public String getName() {
      return this.node.getAttributes().getNamedItem("name").getNodeValue();
   }

   public Iterator<MapleData> iterator() {
      return this.getChildren().iterator();
   }
}

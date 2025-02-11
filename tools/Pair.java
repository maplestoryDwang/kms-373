package tools;

import java.io.Serializable;

public class Pair<E, F> implements Serializable {
   private static final long serialVersionUID = 9179541993413738569L;
   public E left;
   public F right;

   public Pair(E left, F right) {
      this.left = left;
      this.right = right;
   }

   public Pair(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public E getLeft() {
      return this.left;
   }

   public F getRight() {
      return this.right;
   }

   public String toString() {
      String var10000 = this.left.toString();
      return var10000 + ":" + this.right.toString();
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.left == null ? 0 : this.left.hashCode());
      result = 31 * result + (this.right == null ? 0 : this.right.hashCode());
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         Pair other = (Pair)obj;
         if (this.left == null) {
            if (other.left != null) {
               return false;
            }
         } else if (!this.left.equals(other.left)) {
            return false;
         }

         if (this.right == null) {
            if (other.right != null) {
               return false;
            }
         } else if (!this.right.equals(other.right)) {
            return false;
         }

         return true;
      }
   }
}

package scripting;

final class a implements Runnable {
   private int cx;
   private int cz;
   private AbstractPlayerInteraction a;

   a(AbstractPlayerInteraction a, int cx, int cz) {
      this.a = a;
      this.cx = cx;
      this.cz = cz;
   }

   public final void run() {
      if (this.a.getPlayer() != null && this.a.getPlayer().getMapId() == this.cx) {
         this.a.warp(this.cz, 0);
      }

   }
}

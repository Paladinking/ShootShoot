package game.items;

import game.events.SoundPlayed;
import game.listeners.GameObjectHandler;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public abstract class Item {

     protected final int maxDelay;

     private final int soundEffect;

     private int delay;

     protected Item(int maxDelay, int soundEffect){
          this.maxDelay = maxDelay;
          this.soundEffect = soundEffect;
          this.delay = 0;
     }

     public void tryUse(GameObjectHandler handler, TileMap tileMap, Vector2d source, Point destination, int radius){
          if (isReady()) {
               use(handler, tileMap, source, destination, radius);
               handler.playSound(soundEffect);
               handler.createEvent(new SoundPlayed(soundEffect));
               setDelay();
          }
     }

     protected abstract void use(GameObjectHandler handler, TileMap tileMap, Vector2d source, Point destination, int radius);

     protected void setDelay(){
          this.delay = maxDelay;
     }

     public boolean isReady(){
          return delay == 0;
     }

     public void tick(){
          if (delay > 0) delay--;
     }

     public double getDelayFraction(){
          return ((double) delay) / maxDelay;
     }
}

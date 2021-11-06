package game.items.weaponds;

import game.listeners.GameEventHandler;
import game.sound.Sound;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public abstract class Weapon {

     protected final int maxDelay;

     private final Sound soundEffect;

     private int delay;

     protected Weapon(int maxDelay, Sound soundEffect){
          this.maxDelay = maxDelay;
          this.soundEffect = soundEffect;
          this.delay = 0;
     }

     public void tryUse(GameEventHandler handler, TileMap tileMap, Vector2d source, Point destination){
          if (isReady()) {
               use(handler, tileMap, source, destination);
               soundEffect.play();
               setDelay();
          }
     }

     protected abstract void use(GameEventHandler handler, TileMap tileMap, Vector2d source, Point destination);

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

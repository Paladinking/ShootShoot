package game.items.weaponds;

import game.events.GameEvent;
import game.listeners.GameEventHandler;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public abstract class Weapon {

     protected final int maxDelay, radius, projectileSpeed;

     private int delay;

     protected Weapon(int maxDelay, int radius, int bulletSpeed){
          this.radius = radius;
          this.maxDelay = maxDelay;
          this.projectileSpeed = bulletSpeed;
          this.delay = 0;
     }


     protected void createProjectile(GameEventHandler handler, TileMap tileMap, Vector2d source, Vector2d projectileVector, int type){
          Vector2d bulletPos = new Vector2d(source);
          bulletPos.add(new Vector2d(projectileVector.x * (radius + 1), projectileVector.y * (radius + 1)));
          if (tileMap.isOpen(bulletPos)) {
               projectileVector.scale(projectileSpeed);
               handler.addEvent(new GameEvent.ProjectileCreated(bulletPos.x, bulletPos.y, projectileVector.x, projectileVector.y, type));
          }
     }


     public abstract void use(GameEventHandler handler, TileMap tileMap, Vector2d source, Point destination);

     protected void setDelay(){
          this.delay = maxDelay;
     }

     public boolean isReady(){
          return delay == 0;
     }

     public void tick(){
          if (delay > 0) delay--;
     }

     public double getShootDelayFraction(){
          return ((double) delay) / maxDelay;
     }
}

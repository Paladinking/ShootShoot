package game.listeners;

import javax.vecmath.Vector2d;

public interface PlayerListener {

    void playerFiredShot(Vector2d pos, Vector2d velocity);

    void playerMoved(Vector2d newPos);
}

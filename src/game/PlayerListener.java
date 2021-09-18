package game;

import javax.vecmath.Vector2d;

public interface PlayerListener {

    void firedShot(Vector2d pos, Vector2d velocity);
}

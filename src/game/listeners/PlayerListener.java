package game.listeners;

import game.entities.LocalPlayer;
import game.items.weaponds.Weapon;

import javax.vecmath.Vector2d;

public interface PlayerListener {

    void playerUsedWeapon(Weapon weapon, LocalPlayer player);

    void playerMoved(Vector2d newPos);
}

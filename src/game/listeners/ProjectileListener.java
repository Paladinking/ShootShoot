package game.listeners;

import game.entities.LocalPlayer;
import game.events.GameEvent;

public interface ProjectileListener {

    void hurtPlayer(LocalPlayer player, int amount);

    void createEvent(GameEvent event);
}

package game.listeners;

import game.entities.LocalPlayer;
import game.events.GameEvent;

public interface GameObjectHandler {

    void hurtPlayer(LocalPlayer player, int amount);

    void createEvent(GameEvent event);

    void addEvent(GameEvent event);

    void playSound(int sound);

    LocalPlayer getPlayer();
}

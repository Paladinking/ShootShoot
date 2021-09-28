package game.listeners;

import game.events.GameEvent;
import game.events.ServerEvent;

public interface GameEventHandler {

    void addEvent(GameEvent event);

    default void execImmediate(ServerEvent event){

    }
}

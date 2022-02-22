package fuzs.deathfinder.api.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface ScreenOpenCallback {
    Event<ScreenOpenCallback> EVENT = EventFactory.createArrayBacked(ScreenOpenCallback.class, listeners -> (Screen newScreen) -> {
        for (ScreenOpenCallback event : listeners) {
            if (!event.onScreenOpen(newScreen)) {
                return false;
            }
        }
        return true;
    });

    boolean onScreenOpen(Screen newScreen);
}

package fuzs.deathfinder.client;

import fuzs.deathfinder.api.client.event.ScreenOpenCallback;
import fuzs.deathfinder.client.handler.DeathCommandHandler;
import fuzs.deathfinder.client.handler.DeathScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.DeathScreen;

public class DeathFinderClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        onConstructMod();
    }

    public static void onConstructMod() {
        registerHandlers();
    }

    private static void registerHandlers() {
        final DeathScreenHandler deathScreenHandler = new DeathScreenHandler();
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof DeathScreen) {
                ScreenEvents.afterRender(screen).register(deathScreenHandler::onDrawScreen);
            }
        });
        ScreenOpenCallback.EVENT.register(deathScreenHandler::onScreenOpen);
        final DeathCommandHandler deathCommandHandler = new DeathCommandHandler();
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ChatScreen) {
                ScreenMouseEvents.allowMouseClick(screen).register(deathCommandHandler::onMouseClicked$Pre);
            }
        });
    }
}

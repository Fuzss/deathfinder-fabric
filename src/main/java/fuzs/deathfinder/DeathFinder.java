package fuzs.deathfinder;

import fuzs.deathfinder.api.event.LivingDeathCallback;
import fuzs.deathfinder.config.ClientConfig;
import fuzs.deathfinder.config.ServerConfig;
import fuzs.deathfinder.handler.DeathMessageHandler;
import fuzs.deathfinder.network.client.message.C2SDeathPointTeleportMessage;
import fuzs.deathfinder.registry.ModRegistry;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.ConfigHolderImpl;
import fuzs.puzzleslib.network.MessageDirection;
import fuzs.puzzleslib.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeathFinder implements ModInitializer {
    public static final String MOD_ID = "deathfinder";
    public static final String MOD_NAME = "Death Finder";
    public static final Logger LOGGER = LogManager.getLogger(DeathFinder.MOD_NAME);

    public static final NetworkHandler NETWORK = NetworkHandler.of(MOD_ID);
    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<ClientConfig, ServerConfig> CONFIG = ConfigHolder.of(() -> new ClientConfig(), () -> new ServerConfig());

    @Override
    public void onInitialize() {
        onConstructMod();
    }

    public static void onConstructMod() {
        ((ConfigHolderImpl<?, ?>) CONFIG).addConfigs(MOD_ID);
        registerHandlers();
        registerMessages();
        ModRegistry.touch();
    }

    private static void registerHandlers() {
        final DeathMessageHandler deathMessageHandler = new DeathMessageHandler();
        LivingDeathCallback.EVENT.register((LivingEntity entity, DamageSource source) -> {
            deathMessageHandler.onLivingDeath(entity, source);
            return true;
        });
    }

    private static void registerMessages() {
        NETWORK.register(C2SDeathPointTeleportMessage.class, C2SDeathPointTeleportMessage::new, MessageDirection.TO_SERVER);
    }
}

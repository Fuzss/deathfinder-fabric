package fuzs.deathfinder.registry;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import fuzs.deathfinder.DeathFinder;
import fuzs.deathfinder.capability.PlayerDeathTracker;
import fuzs.deathfinder.capability.PlayerDeathTrackerImpl;
import fuzs.puzzleslib.capability.CapabilityController;
import fuzs.puzzleslib.capability.data.PlayerRespawnStrategy;

public class ModRegistry {
    private static final CapabilityController CAPABILITIES = CapabilityController.of(DeathFinder.MOD_ID);
    public static final ComponentKey<PlayerDeathTracker> PLAYER_DEATH_TRACKER_CAPABILITY = CAPABILITIES.registerPlayerCapability("death_tracker", PlayerDeathTracker.class, player -> new PlayerDeathTrackerImpl(), PlayerRespawnStrategy.ALWAYS_COPY);

    public static void touch() {

    }
}

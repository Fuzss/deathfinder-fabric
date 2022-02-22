package fuzs.deathfinder.core.capability.data;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * modes determining how capability data should be handled when the player entity is recreated, which will usually happen when returning from the end dimension and when respawning
 * this is basically the same class as in {@see <a href="https://github.com/OnyxStudios/Cardinal-Components-API">https://github.com/OnyxStudios/Cardinal-Components-API</a>} for the Fabric mod loader
 */
public class PlayerRespawnStrategy {
    private static final Map<PlayerRespawnStrategy, RespawnCopyStrategy<Component>> TO_COMPONENT_STRATEGY = new HashMap<>() {{
        this.put(ALWAYS_COPY, RespawnCopyStrategy.ALWAYS_COPY);
        this.put(INVENTORY, RespawnCopyStrategy.INVENTORY);
        this.put(LOSSLESS, RespawnCopyStrategy.LOSSLESS_ONLY);
        this.put(NEVER, RespawnCopyStrategy.NEVER_COPY);
    }};
    /**
     * always copy data when recreating player
     */
    public static final PlayerRespawnStrategy ALWAYS_COPY = new PlayerRespawnStrategy();
    /**
     * copy data when inventory contents are copied
     */
    public static final PlayerRespawnStrategy INVENTORY = new PlayerRespawnStrategy();
    /**
     * copy data when returning from end, but never after dying
     */
    public static final PlayerRespawnStrategy LOSSLESS = new PlayerRespawnStrategy();
    /**
     * never copy data
     */
    public static final PlayerRespawnStrategy NEVER = new PlayerRespawnStrategy();

    private PlayerRespawnStrategy() {
    }

    /**
     * simple method for converting to api equivalent, much more complex on Forge
     */
    public final RespawnCopyStrategy<Component> toComponentStrategy() {
        return Optional.ofNullable(TO_COMPONENT_STRATEGY.get(this))
                .orElseThrow(() -> new IllegalStateException("Invalid respawn strategy"));
    }
}

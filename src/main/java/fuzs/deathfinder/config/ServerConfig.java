package fuzs.deathfinder.config;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.annotation.Config;
import fuzs.puzzleslib.config.serialization.EntryCollectionBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Set;

public class ServerConfig extends AbstractConfig {
    @Config
    public ComponentsConfig components = new ComponentsConfig();
    @Config
    public MessagesConfig messages = new MessagesConfig();

    public ServerConfig() {
        super("");
    }

    public enum TeleportRestriction {
        NO_ONE, OPERATORS_ONLY, EVERYONE
    }

    @Override
    protected void afterConfigReload() {
        this.messages.afterConfigReload();
    }

    public static class ComponentsConfig extends AbstractConfig {
        @Config(description = "Add position component to death messages.")
        public boolean positionComponent = true;
        @Config(description = "Add dimension component to death messages.")
        public boolean dimensionComponent = true;
        @Config(description = "Add distance component to death messages.")
        public boolean distanceComponent = true;
        @Config(description = {"Who should be allowed to click the position component to teleport there.", "Normal player can only teleport to their own death position once in a given time frame, if enabled.", "Operators can teleport to any death position without limitations, if enabled."})
        public TeleportRestriction allowTeleporting = TeleportRestriction.OPERATORS_ONLY;
        @Config(description = {"Amount of seconds in which teleporting to the last death point is possible.", "Set to -1 to remove time limit."})
        @Config.IntRange(min = -1)
        public int teleportInterval = 300;

        public ComponentsConfig() {
            super("death_message_components");
        }
    }

    public static class MessagesConfig extends AbstractConfig {
        @Config(description = "Show death message for players.")
        public boolean playerDeaths = true;
        @Config(description = "Show death message for tamed entities.")
        public boolean petDeaths = true;
        @Config(description = "Show death message for villagers.")
        public boolean villagerDeaths = true;
        @Config(description = "Show death message for named entities.")
        public boolean namedEntityDeaths = true;
        @Config(description = "Show death message for all entities.")
        public boolean allDeaths = false;
        @Config(name = "death_message_blacklist", description = {"Entities to be excluded when \"all_deaths\" is enabled.", EntryCollectionBuilder.CONFIG_DESCRIPTION})
        private List<String> deathMessageBlacklistRaw = EntryCollectionBuilder.getKeyList(Registry.ENTITY_TYPE, EntityType.BAT, EntityType.GLOW_SQUID);
        @Config(name = "death_message_whitelist", description = {"The only entities to be included when \"all_deaths\" is enabled. Takes precedence over blacklist.", EntryCollectionBuilder.CONFIG_DESCRIPTION})
        private List<String> deathMessageWhitelistRaw = Lists.newArrayList();

        public Set<EntityType<?>> deathMessageBlacklist;
        public Set<EntityType<?>> deathMessageWhitelist;

        public MessagesConfig() {
            super("death_messages");
        }

        @Override
        protected void afterConfigReload() {
            this.deathMessageBlacklist = EntryCollectionBuilder.of(Registry.ENTITY_TYPE).buildSet(this.deathMessageBlacklistRaw);
            this.deathMessageWhitelist = EntryCollectionBuilder.of(Registry.ENTITY_TYPE).buildSet(this.deathMessageWhitelistRaw);
        }
    }
}

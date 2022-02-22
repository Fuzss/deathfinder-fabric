package fuzs.deathfinder.core.capability;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import fuzs.deathfinder.core.capability.data.CapabilityComponent;
import fuzs.deathfinder.core.capability.data.CapabilityFactory;
import fuzs.deathfinder.core.capability.data.ItemCapabilityComponent;
import fuzs.deathfinder.core.capability.data.PlayerRespawnStrategy;
import fuzs.puzzleslib.PuzzlesLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * helper object for registering and attaching mod capabilities, needs to be extended by every mod individually
 * this basically is the same as {@link fuzs.puzzleslib.registry.RegistryManager}
 */
public class CapabilityController implements ItemComponentInitializer, EntityComponentInitializer, BlockComponentInitializer, WorldComponentInitializer, ChunkComponentInitializer {
    /**
     * capability controllers are stored for each mod separately to avoid concurrency issues, might not be need though
     */
    private static final Map<String, CapabilityController> MOD_TO_CAPABILITIES = Maps.newConcurrentMap();

    /**
     * namespace for this instance
     */
    private final String namespace;
    /**
     * internal storage for registering capability entries
     */
    private final Multimap<Class<?>, ComponentFactoryRegistration<?>> typeToRegistration = ArrayListMultimap.create();

    /**
     * invoked by cardinal components entry point via reflection
     * this should better be a separate class, but kept in here to remain consistent with Forge and doesn't matter anyways
     */
    @Deprecated
    public CapabilityController() {
        this("_internal");
    }

    /**
     * private constructor
     * @param namespace namespace for this instance
     */
    private CapabilityController(String namespace) {
        this.namespace = namespace;
    }

    /**
     * register capability to {@link ItemStack} objects
     * @param capabilityKey path for internal name of this capability, will be used for serialization
     * @param capabilityType interface for this capability
     * @param capabilityFactory capability factory
     * @param item item to apply to
     * @param <C> capability type
     * @return capability instance from capability manager
     */
    public <C extends ItemCapabilityComponent> ComponentKey<C> registerItemCapability(String capabilityKey, Class<C> capabilityType, CapabilityFactory<ItemStack, C> capabilityFactory, Item item) {
        return this.registerCapability(ItemStack.class, capabilityKey, capabilityType, componentKey -> (ItemComponentFactoryRegistry registry) -> registry.register(item, componentKey, capabilityFactory));
    }

    public <C extends ItemCapabilityComponent> ComponentKey<C> registerItemCapability(String capabilityKey, Class<C> capabilityType, CapabilityFactory<ItemStack, C> capabilityFactory, Predicate<Item> itemFilter) {
        return this.registerCapability(ItemStack.class, capabilityKey, capabilityType, componentKey -> (ItemComponentFactoryRegistry registry) -> registry.register(itemFilter, componentKey, capabilityFactory));
    }

    public <T extends Entity, C extends CapabilityComponent> ComponentKey<C> registerEntityCapability(String capabilityKey, Class<C> capabilityType, CapabilityFactory<T, C> capabilityFactory, Class<T> entityType) {
        return this.registerCapability(Entity.class, capabilityKey, capabilityType, componentKey -> (EntityComponentFactoryRegistry registry) -> registry.registerFor(entityType, componentKey, capabilityFactory));
    }

    public <C extends CapabilityComponent> ComponentKey<C> registerPlayerCapability(String capabilityKey, Class<C> capabilityType, CapabilityFactory<Player, C> capabilityFactory, PlayerRespawnStrategy respawnStrategy) {
        return this.registerCapability(Entity.class, capabilityKey, capabilityType, componentKey -> (EntityComponentFactoryRegistry registry) -> registry.registerForPlayers(componentKey, capabilityFactory, respawnStrategy.toComponentStrategy()));
    }

    public <T extends BlockEntity, C extends CapabilityComponent> ComponentKey<C> registerBlockEntityCapability(String capabilityKey, Class<C> capabilityType, CapabilityFactory<T, C> capabilityFactory, Class<T> blockEntityType) {
        return this.registerCapability(BlockEntity.class, capabilityKey, capabilityType, componentKey -> (BlockComponentFactoryRegistry registry) -> registry.registerFor(blockEntityType, componentKey, capabilityFactory));
    }

    public <C extends CapabilityComponent> ComponentKey<C> registerLevelChunkCapability(String capabilityKey, Class<C> capabilityType, CapabilityFactory<ChunkAccess, C> capabilityFactory) {
        return this.registerCapability(LevelChunk.class, capabilityKey, capabilityType, componentKey -> (ChunkComponentFactoryRegistry registry) -> registry.register(componentKey, capabilityFactory));
    }

    public <C extends CapabilityComponent> ComponentKey<C> registerLevelCapability(String capabilityKey, Class<C> capabilityType, CapabilityFactory<Level, C> capabilityFactory) {
        return this.registerCapability(Level.class, capabilityKey, capabilityType, componentKey -> (WorldComponentFactoryRegistry registry) -> registry.register(componentKey, capabilityFactory));
    }

    /**
     * register capabilities for a given object type
     * @param objectType type of object to attach to, only works for generic supertypes
     * @param capabilityKey path for internal name of this capability, will be used for serialization
     * @param capabilityType interface for this capability
     * @param factoryRegistration capability factory
     * @param <C> capability type
     * @return capability instance from capability manager
     */
    private <C extends CapabilityComponent> ComponentKey<C> registerCapability(Class<?> objectType, String capabilityKey, Class<C> capabilityType, Function<ComponentKey<C>, ComponentFactoryRegistration<?>> factoryRegistration) {
        final ComponentKey<C> componentKey = ComponentRegistryV3.INSTANCE.getOrCreate(this.locate(capabilityKey), capabilityType);
        this.typeToRegistration.put(objectType, factoryRegistration.apply(componentKey));
        return componentKey;
    }

    /**
     * @param path path for location
     * @return resource location for {@link #namespace}
     */
    private ResourceLocation locate(String path) {
        if (StringUtils.isEmpty(path)) throw new IllegalArgumentException("Can't register object without name");
        return new ResourceLocation(this.namespace, path);
    }

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
        registerComponentFactories(ItemStack.class, registry);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registerComponentFactories(Entity.class, registry);
    }

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registerComponentFactories(BlockEntity.class, registry);
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registerComponentFactories(LevelChunk.class, registry);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registerComponentFactories(Level.class, registry);
    }

    private static <T> void registerComponentFactories(Class<?> baseType, T registry) {
        for (CapabilityController controller : MOD_TO_CAPABILITIES.values()) {
            for (ComponentFactoryRegistration<?> factoryRegistration : controller.typeToRegistration.get(baseType)) {
                ((ComponentFactoryRegistration<T>) factoryRegistration).registerComponentFactories(registry);
            }
        }
    }

    /**
     * creates a new capability controller for <code>namespace</code> or returns an existing one
     * @param namespace namespace used for registration
     * @return new mod specific capability controller
     */
    public static synchronized CapabilityController of(String namespace) {
        return MOD_TO_CAPABILITIES.computeIfAbsent(namespace, key -> {
            final CapabilityController manager = new CapabilityController(namespace);
            PuzzlesLib.LOGGER.info("Creating capability controller for mod id {}", namespace);
            return manager;
        });
    }

    @FunctionalInterface
    private interface ComponentFactoryRegistration<T> {
        void registerComponentFactories(T registry);
    }
}

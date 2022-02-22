package fuzs.deathfinder.core.capability;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import fuzs.deathfinder.core.capability.data.CapabilityComponent;
import fuzs.deathfinder.core.capability.data.CapabilityFactory;
import fuzs.deathfinder.core.capability.data.PlayerRespawnStrategy;
import fuzs.puzzleslib.PuzzlesLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

/**
 * helper object for registering and attaching mod capabilities, needs to be extended by every mod individually
 * this basically is the same as {@link fuzs.puzzleslib.registry.RegistryManager}
 */
public class CapabilityController implements EntityComponentInitializer, BlockComponentInitializer {
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
    private final Multimap<Class<?>, CapabilityData<?, ? extends CapabilityComponent>> typeToData = ArrayListMultimap.create();

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
     * forge event
     */
//    private void onRegisterCapabilities(final RegisterCapabilitiesEvent evt) {
//        for (DefaultCapabilityData data : this.typeToData.values()) {
//            evt.register(data.capabilityType());
//        }
//    }
//
//    @Deprecated
//    @SubscribeEvent
//    public void onAttachCapabilities(final AttachCapabilitiesEvent<?> evt) {
//        for (DefaultCapabilityData data : this.typeToData.get((Class<?>) evt.getGenericType())) {
//            if (data.filter().test(evt.getObject())) {
//                evt.addCapability(data.location(), data.capabilityFactory().get());
//            }
//        }
//    }
//
//    @Deprecated
//    @SubscribeEvent
//    public void onPlayerClone(final PlayerEvent.Clone evt) {
//        if (this.respawnStrategies.isEmpty()) return;
//        // we have to revive caps and then invalidate them again since 1.17+
//        evt.getOriginal().reviveCaps();
//        for (Map.Entry<Capability<? extends CapabilityComponent>, PlayerRespawnStrategy> entry : this.respawnStrategies.entrySet()) {
//            evt.getOriginal().getCapability(entry.getKey()).ifPresent(oldCapability -> {
//                evt.getPlayer().getCapability(entry.getKey()).ifPresent(newCapability -> {
//                    entry.getValue().copy(oldCapability, newCapability, !evt.isWasDeath(), evt.getPlayer().level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY));
//                });
//            });
//        }
//        evt.getOriginal().invalidateCaps();
//    }
//
//    /**
//     * register capabilities for a given object type
//     * @param objectType type of object to attach to, only works for generic supertypes
//     * @param path path for internal name of this capability, will be used for serialization
//     * @param type interface for this capability
//     * @param factory capability factory
//     * @param filter filter for <code>objectType</code>
//     * @param token capability token required to get capability instance from capability manager
//     * @param <T> capability type
//     * @return capability instance from capability manager
//     */
//    @SuppressWarnings("unchecked")
//    private <T extends CapabilityComponent> Capability<T> registerCapability(Class<? extends ICapabilityProvider> objectType, String path, Class<T> type, Supplier<T> factory, Predicate<Object> filter, CapabilityToken<T> token) {
//        final Capability<T> capability = CapabilityManager.get(token);
//        this.typeToData.put(objectType, new DefaultCapabilityData(this.locate(path), (Class<CapabilityComponent>) type, () -> new CapabilityDispatcher<>(capability, factory.get()), filter));
//        return capability;
//    }
//
//    /**
//     * register capability to {@link ItemStack} objects
//     * @param path path for internal name of this capability, will be used for serialization
//     * @param type interface for this capability
//     * @param factory capability factory
//     * @param filter filter for <code>objectType</code>
//     * @param token capability token required to get capability instance from capability manager
//     * @param <T> capability type
//     * @return capability instance from capability manager
//     */
//    public <T extends CapabilityComponent> Capability<T> registerItemStackCapability(String path, Class<T> type, Supplier<T> factory, Predicate<Object> filter, CapabilityToken<T> token) {
//        return this.registerCapability(ItemStack.class, path, type, factory, filter, token);
//    }

    /**
     * register capability to {@link Entity} objects
     * @param path path for internal name of this capability, will be used for serialization
     * @param capabilityType interface for this capability
     * @param capabilityFactory capability factory
     * @param clazzFilter filter for <code>objectType</code>
     * @param <T> capability type
     * @return capability instance from capability manager
     */
    public <T extends CapabilityComponent> ComponentKey<T> registerEntityCapability(String path, Class<T> capabilityType, CapabilityFactory<Entity, T> capabilityFactory, Predicate<Class<Entity>> clazzFilter) {
        final ComponentKey<T> componentKey = ComponentRegistryV3.INSTANCE.getOrCreate(this.locate(path), capabilityType);
        this.typeToData.put(Entity.class, new DefaultCapabilityData<>(componentKey, capabilityType, capabilityFactory, clazzFilter));
        return componentKey;
    }

    /**
     * register capability to {@link Entity} objects
     * @param path path for internal name of this capability, will be used for serialization
     * @param type interface for this capability
     * @param factory capability factory
     * @param filter filter for <code>objectType</code>
     * @param respawnStrategy how data should be copied when the player object is recreated
     * @param token capability token required to get capability instance from capability manager
     * @param <T> capability type
     * @return capability instance from capability manager
     */
    public <T extends CapabilityComponent> ComponentKey<T> registerPlayerCapability(String path, Class<T> capabilityType, CapabilityFactory<Player, T> capabilityFactory, Predicate<Class<Player>> clazzFilter, PlayerRespawnStrategy respawnStrategy) {
        final ComponentKey<T> componentKey = ComponentRegistryV3.INSTANCE.getOrCreate(this.locate(path), capabilityType);
        this.typeToData.put(Player.class, new PlayerCapabilityData<>(componentKey, capabilityType, capabilityFactory, clazzFilter, respawnStrategy));
        return componentKey;
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        for (CapabilityController controller : MOD_TO_CAPABILITIES.values()) {
            registerEntityComponentFactories(controller, registry);
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerEntityComponentFactories(CapabilityController controller, EntityComponentFactoryRegistry registry) {
        for (DefaultCapabilityData<Entity, CapabilityComponent> data : (Collection<DefaultCapabilityData<Entity, CapabilityComponent>>) (Collection<?>) controller.typeToData.get(Entity.class)) {
            registry.beginRegistration(Entity.class, data.capabilityKey())
                    .filter((Predicate<Class<? extends Entity>>) (Predicate<?>) data.filter())
                    .impl(data.capabilityType())
                    .end(data.capabilityFactory());
        }
        for (CapabilityData<Player, CapabilityComponent> data : (Collection<CapabilityData<Player, CapabilityComponent>>) (Collection<?>) controller.typeToData.get(Player.class)) {
            registry.beginRegistration(Player.class, data.capabilityKey())
                    .filter((Predicate<Class<? extends Player>>) (Predicate<?>) data.filter())
                    .impl(data.capabilityType())
                    .respawnStrategy(((PlayerCapabilityData<?, ?>) data).respawnStrategy().toComponentStrategy())
                    .end(data.capabilityFactory());
        }
    }

    /**
     * register capability to {@link BlockEntity} objects
     * @param path path for internal name of this capability, will be used for serialization
     * @param type interface for this capability
     * @param factory capability factory
     * @param filter filter for <code>objectType</code>
     * @param token capability token required to get capability instance from capability manager
     * @param <T> capability type
     * @return capability instance from capability manager
     */
//    public <T extends CapabilityComponent> Capability<T> registerBlockEntityCapability(String path, Class<T> type, Supplier<T> factory, Predicate<Object> filter, CapabilityToken<T> token) {
//        return this.registerCapability(BlockEntity.class, path, type, factory, filter, token);
//    }
//
//    /**
//     * register capability to {@link Level} objects
//     * @param path path for internal name of this capability, will be used for serialization
//     * @param type interface for this capability
//     * @param factory capability factory
//     * @param filter filter for <code>objectType</code>
//     * @param token capability token required to get capability instance from capability manager
//     * @param <T> capability type
//     * @return capability instance from capability manager
//     */
//    public <T extends CapabilityComponent> Capability<T> registerLevelCapability(String path, Class<T> type, Supplier<T> factory, Predicate<Object> filter, CapabilityToken<T> token) {
//        return this.registerCapability(Level.class, path, type, factory, filter, token);
//    }
//
//    /**
//     * register capability to {@link LevelChunk} objects
//     * @param path path for internal name of this capability, will be used for serialization
//     * @param type interface for this capability
//     * @param factory capability factory
//     * @param filter filter for <code>objectType</code>
//     * @param token capability token required to get capability instance from capability manager
//     * @param <T> capability type
//     * @return capability instance from capability manager
//     */
//    public <T extends CapabilityComponent> Capability<T> registerLevelChunkCapability(String path, Class<T> type, Supplier<T> factory, Predicate<Object> filter, CapabilityToken<T> token) {
//        return this.registerCapability(LevelChunk.class, path, type, factory, filter, token);
//    }

    /**
     * @param path path for location
     * @return resource location for {@link #namespace}
     */
    private ResourceLocation locate(String path) {
        if (StringUtils.isEmpty(path)) throw new IllegalArgumentException("Can't register object without name");
        return new ResourceLocation(this.namespace, path);
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

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {

    }

    /**
     * just a data class for all the things we need when registering capabilities...
     */
    private static record DefaultCapabilityData<T, C extends CapabilityComponent>(ComponentKey<C> capabilityKey, Class<C> capabilityType, CapabilityFactory<T, C> capabilityFactory, Predicate<Class<T>> filter) implements CapabilityData<T, C> {
        @Override
        public @Nullable PlayerRespawnStrategy respawnStrategy() {
            return null;
        }
    }

    private static record PlayerCapabilityData<T, C extends CapabilityComponent>(ComponentKey<C> capabilityKey, Class<C> capabilityType, CapabilityFactory<T, C> capabilityFactory, Predicate<Class<T>> filter, PlayerRespawnStrategy respawnStrategy) implements CapabilityData<T, C> {
    }

    private interface CapabilityData<T, C extends CapabilityComponent> {
        ComponentKey<C> capabilityKey();

        Class<C> capabilityType();

        CapabilityFactory<T, C> capabilityFactory();

        Predicate<Class<T>> filter();

        @Nullable
        PlayerRespawnStrategy respawnStrategy();
    }
}

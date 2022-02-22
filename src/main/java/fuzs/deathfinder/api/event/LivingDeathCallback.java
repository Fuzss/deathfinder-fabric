package fuzs.deathfinder.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface LivingDeathCallback {
    Event<LivingDeathCallback> EVENT = EventFactory.createArrayBacked(LivingDeathCallback.class, listeners -> (LivingEntity entity, DamageSource source) -> {
        for (LivingDeathCallback event : listeners) {
            if (!event.onLivingDeath(entity, source)) {
                return false;
            }
        }
        return true;
    });

    boolean onLivingDeath(LivingEntity entity, DamageSource source);
}

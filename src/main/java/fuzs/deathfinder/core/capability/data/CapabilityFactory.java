package fuzs.deathfinder.core.capability.data;

import dev.onyxstudios.cca.api.v3.component.ComponentFactory;

@FunctionalInterface
public interface CapabilityFactory<T, C extends CapabilityComponent> extends ComponentFactory<T, C> {
    C create(T t);

    @Override
    default C createComponent(T t) {
        return this.create(t);
    }
}

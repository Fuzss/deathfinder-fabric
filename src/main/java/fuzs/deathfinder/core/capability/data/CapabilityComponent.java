package fuzs.deathfinder.core.capability.data;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.CompoundTag;

public interface CapabilityComponent extends ComponentV3 {
    void write(CompoundTag tag);

    void read(CompoundTag tag);

    @Override
    default void writeToNbt(CompoundTag tag) {
        this.write(tag);
    }

    @Override
    default void readFromNbt(CompoundTag tag) {
        this.read(tag);
    }
}

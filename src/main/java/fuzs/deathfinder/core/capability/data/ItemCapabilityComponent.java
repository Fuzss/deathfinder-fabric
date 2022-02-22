package fuzs.deathfinder.core.capability.data;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.world.item.ItemStack;

public abstract class ItemCapabilityComponent extends ItemComponent implements CapabilityComponent {
    public ItemCapabilityComponent(ItemStack stack) {
        super(stack);
    }

    public ItemCapabilityComponent(ItemStack stack, ComponentKey<?> key) {
        super(stack, key);
    }
}

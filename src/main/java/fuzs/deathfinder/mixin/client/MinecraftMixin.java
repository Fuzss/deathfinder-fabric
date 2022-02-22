package fuzs.deathfinder.mixin.client;

import fuzs.deathfinder.api.client.event.ScreenOpenCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    public void setScreen$head(Screen screen, CallbackInfo callbackInfo) {
        if (!ScreenOpenCallback.EVENT.invoker().onScreenOpen(screen)) callbackInfo.cancel();
    }
}

package fuzs.deathfinder.mixin;

import com.mojang.authlib.GameProfile;
import fuzs.deathfinder.api.event.LivingDeathCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level p_36114_, BlockPos p_36115_, float p_36116_, GameProfile p_36117_) {
        super(p_36114_, p_36115_, p_36116_, p_36117_);
    }

    @ModifyVariable(method = "die", at = @At("STORE"), ordinal = 0)
    public boolean die$showDeathMessages(boolean showDeathMessages) {
        return false;
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    public void die(DamageSource damageSource, CallbackInfo callbackInfo) {
        if (!LivingDeathCallback.EVENT.invoker().onLivingDeath(this, damageSource)) callbackInfo.cancel();
    }
}

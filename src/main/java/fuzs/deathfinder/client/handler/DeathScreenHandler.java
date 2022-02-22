package fuzs.deathfinder.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.deathfinder.DeathFinder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class DeathScreenHandler {
    private final Minecraft minecraft = Minecraft.getInstance();
    private BlockPos lastPlayerPosition = BlockPos.ZERO;

    public void onDrawScreen(Screen deathScreen, PoseStack matrices, int mouseX, int mouseY, float tickDelta) {
        if (!DeathFinder.CONFIG.client().deathScreenCoordinates) return;
        if (deathScreen instanceof DeathScreen && this.lastPlayerPosition != BlockPos.ZERO) {
            Component component = new TranslatableComponent("death.screen.position", new TextComponent(String.valueOf(this.lastPlayerPosition.getX())).withStyle(ChatFormatting.WHITE), new TextComponent(String.valueOf(this.lastPlayerPosition.getY())).withStyle(ChatFormatting.WHITE), new TextComponent(String.valueOf(this.lastPlayerPosition.getZ())).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GOLD);
            GuiComponent.drawCenteredString(matrices, this.minecraft.font, component, deathScreen.width / 2, 115, 16777215);
        }
    }

    public boolean onScreenOpen(Screen newScreen) {
        if (newScreen instanceof DeathScreen) {
            // when canceling death message on server, death screen package is still sent (arrives after ours though)
            // so we intercept it here and keep our screen
            if (this.minecraft.screen instanceof DeathScreen) {
                return false;
            } else {
                this.lastPlayerPosition = this.minecraft.player.blockPosition();
            }
        }
        return true;
    }
}

package net.charonus.modellers_dream.screen.custom.DispatcherTable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.charonus.modellers_dream.ModellersDream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DispatcherScreen extends AbstractContainerScreen<DispatcherMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModellersDream.MOD_ID, "textures/gui/dispatcher.png");

    private IconButton confirmButton;

    public DispatcherScreen(DispatcherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 226;
        this.imageHeight = 220;
        this.titleLabelX = 4;
        this.titleLabelY = 4;
        // Suppress vanilla's automatic "Inventory" label since this menu has no player-inv slots
        this.inventoryLabelX = Integer.MIN_VALUE;
        this.inventoryLabelY = Integer.MIN_VALUE;
    }

    @Override
    protected void init() {
        super.init();
        confirmButton = new IconButton(leftPos + imageWidth - 26, topPos + imageHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onConfirm);
        addRenderableWidget(confirmButton);
    }

    private void onConfirm() {
        // TODO: send a real C2S packet once dispatch state exists on the menu/BE
        this.onClose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
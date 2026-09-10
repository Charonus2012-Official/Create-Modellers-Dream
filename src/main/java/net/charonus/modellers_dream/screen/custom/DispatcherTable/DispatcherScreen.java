package net.charonus.modellers_dream.screen.custom.DispatcherTable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.charonus.modellers_dream.ModellersDream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class DispatcherScreen extends AbstractContainerScreen<DispatcherMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModellersDream.MOD_ID, "textures/gui/dispatcher.png");

    private int trainScroll;
    private int stationScroll;
    private int selectedTrain = -1;
    private int selectedStation = -1;
    private static final int LIST_ROWS = 6;

    public DispatcherScreen(DispatcherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 226;
        this.imageHeight = 220;
        this.titleLabelX = 4;
        this.titleLabelY = 3;
        // Suppress vanilla's automatic "Inventory" label since this menu has no player-inv slots
        this.inventoryLabelX = Integer.MIN_VALUE;
        this.inventoryLabelY = Integer.MIN_VALUE;
    }

    @Override
    protected void init() {
        super.init();

        IconButton appendRoute = new IconButton(leftPos + 200, topPos + 137, AllIcons.I_ADD);
        appendRoute.withCallback(() -> sendButton(DispatcherMenu.APPEND_BUTTON));
        appendRoute.setToolTip(Component.literal("Append Route"));
        addRenderableWidget(appendRoute);


        IconButton directRoute = new IconButton(leftPos + 200, topPos + 168, AllIcons.I_CONFIRM);
        directRoute.withCallback(() -> sendButton(DispatcherMenu.DIRECT_BUTTON));
        directRoute.setToolTip(Component.literal("Direct Route"));
        addRenderableWidget(directRoute);


        IconButton OK = new IconButton(leftPos + 200, topPos + 195, AllIcons.I_CONFIRM);
        OK.withCallback(this::onClose);
        addRenderableWidget(OK);
    }

    private void sendButton(int buttonId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x4A4A4A, false);
        guiGraphics.drawString(this.font, "Trains", 6, 15, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Stations", 116, 15, 0xFFFFFF, false);
        renderList(guiGraphics, menu.blockEntity.getAvailableTrains(), trainScroll, selectedTrain, 6, 30, mouseX, mouseY);
        renderList(guiGraphics, menu.blockEntity.getAvailableStations(), stationScroll, selectedStation, 116, 30, mouseX, mouseY);

        String train = selectedTrain >= 0 && selectedTrain < menu.blockEntity.getAvailableTrains().size()
                ? menu.blockEntity.getAvailableTrains().get(selectedTrain) : "Select a train";
        String station = selectedStation >= 0 && selectedStation < menu.blockEntity.getAvailableStations().size()
                ? menu.blockEntity.getAvailableStations().get(selectedStation) : "Select a station";
        guiGraphics.drawString(this.font, "Train Name", 8, 136, 0xE0E0E0, false);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(train, 175), 8, 149, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "To", 8, 167, 0xE0E0E0, false);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(station, 175), 8, 178, 0xFFFFFF, false);
    }

    private void renderList(GuiGraphics graphics, List<String> values, int scroll, int selected, int x, int y,
                            int mouseX, int mouseY) {
        for (int row = 0; row < LIST_ROWS; row++) {
            int index = scroll + row;
            if (index >= values.size()) {
                break;
            }
            int rowY = topPos + y + row * 16;
            boolean hovered = mouseX >= leftPos + x && mouseX < leftPos + x + 100
                    && mouseY >= rowY && mouseY < rowY + 14;
            int color = index == selected ? 0xFFE26A : hovered ? 0xFFFFFF : 0xB8B8B8;
            graphics.drawString(this.font, this.font.plainSubstrByWidth(values.get(index), 94), x, y + row * 16, color, false);
        }
        if (values.size() > LIST_ROWS) {
            int trackTop = y + 1;
            int trackHeight = LIST_ROWS * 16 - 2;
            int thumbHeight = Math.max(8, trackHeight * LIST_ROWS / values.size());
            int maxScroll = values.size() - LIST_ROWS;
            int thumbY = trackTop + (trackHeight - thumbHeight) * scroll / maxScroll;
            graphics.fill(x + 100, trackTop, x + 102, trackTop + trackHeight, 0xFF4A4A4A);
            graphics.fill(x + 100, thumbY, x + 102, thumbY + thumbHeight, 0xFFCFCFCF);
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && clickList(mouseX, mouseY, true)) {
            return true;
        }
        if (button == 0 && clickList(mouseX, mouseY, false)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickList(double mouseX, double mouseY, boolean trains) {
        int x = leftPos + (trains ? 6 : 116);
        int y = topPos + 30;
        if (mouseX < x || mouseX >= x + 100 || mouseY < y || mouseY >= y + LIST_ROWS * 16) {
            return false;
        }
        int row = ((int) mouseY - y) / 16;
        int index = (trains ? trainScroll : stationScroll) + row;
        List<String> values = trains ? menu.blockEntity.getAvailableTrains() : menu.blockEntity.getAvailableStations();
        if (index >= values.size()) {
            return false;
        }
        if (trains) {
            selectedTrain = index;
            sendButton(DispatcherMenu.TRAIN_BUTTON_BASE + index);
        } else {
            selectedStation = index;
            sendButton(DispatcherMenu.STATION_BUTTON_BASE + index);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= leftPos + 6 && mouseX < leftPos + 108) {
            trainScroll = scroll(trainScroll, menu.blockEntity.getAvailableTrains().size(), scrollY);
            return true;
        }
        if (mouseX >= leftPos + 116 && mouseX < leftPos + 218) {
            stationScroll = scroll(stationScroll, menu.blockEntity.getAvailableStations().size(), scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private static int scroll(int current, int size, double amount) {
        int max = Math.max(0, size - LIST_ROWS);
        return Math.max(0, Math.min(max, current - (int) Math.signum(amount)));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}

package net.charonus.modellers_dream.screen.custom.DispatcherTable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.network.SetDispatchDelayPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class DispatcherScreen extends AbstractContainerScreen<DispatcherMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModellersDream.MOD_ID, "textures/gui/dispatcher.png");

    private static final int OFFSET_X = 1; // for modifying textures

    private int trainScroll;
    private int stationScroll;
    private int selectedTrain = -1;
    private int selectedStation = -1;
    private static final int LIST_ROWS = 6;

    private static final String[] UNIT_LABELS = {"Ticks", "Seconds", "Minutes"};
    private static final int[] UNIT_TICKS_PER_STEP = {1, 20, 1200};
    private static final int DELAY_ROW_X = 8 + OFFSET_X;
    private static final int DELAY_ROW_Y = 196;
    private static final int VALUE_BOX_W = 30;
    private static final int UNIT_BOX_W = 60;
    private static final int DELAY_BOX_H = 16;
    private static final int DELAY_BOX_GAP = 3;
    // unitIndex 1 (Seconds) + delayValue 5 matches DispatcherMenu.DEFAULT_DELAY_TICKS (100 ticks = 5s)
    private int unitIndex = 1;
    private int delayValue = 5;
    private ScrollInput valueScrollInput;
    private ScrollInput unitScrollInput;

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

        IconButton appendRoute = new IconButton(leftPos + 200 + OFFSET_X, topPos + 137, AllIcons.I_ADD);
        appendRoute.withCallback(() -> sendButton(DispatcherMenu.APPEND_BUTTON));
        appendRoute.setToolTip(Component.literal("Append Route"));
        addRenderableWidget(appendRoute);


        IconButton directRoute = new IconButton(leftPos + 200 + OFFSET_X, topPos + 168, AllIcons.I_CONFIRM);
        directRoute.withCallback(() -> sendButton(DispatcherMenu.DIRECT_BUTTON));
        directRoute.setToolTip(Component.literal("Direct Route"));
        addRenderableWidget(directRoute);


        IconButton OK = new IconButton(leftPos + 200 + OFFSET_X, topPos + 195, AllIcons.I_CONFIRM);
        OK.withCallback(this::onClose);
        addRenderableWidget(OK);

        int valueX = leftPos + DELAY_ROW_X + OFFSET_X;
        int unitX = valueX + VALUE_BOX_W + DELAY_BOX_GAP;
        int boxY = topPos + DELAY_ROW_Y;

        // max is exclusive in ScrollInput's clampState(), hence +1 to actually reach it
        valueScrollInput = new ScrollInput(valueX, boxY, VALUE_BOX_W, DELAY_BOX_H)
                .withRange(0, maxValueForUnit() + 1)
                .titled(Component.literal("Wait on Arrival Time"))
                .calling(state -> {
                    delayValue = state;
                    sendDelayUpdate();
                });
        valueScrollInput.setState(delayValue);
        addRenderableWidget(valueScrollInput);

        unitScrollInput = new SelectionScrollInput(unitX, boxY, UNIT_BOX_W, DELAY_BOX_H)
                .forOptions(List.of(Component.literal("Ticks"), Component.literal("Seconds"), Component.literal("Minutes")))
                .titled(Component.literal("Wait on Arrival Unit"))
                .calling(state -> {
                    unitIndex = state;
                    int max = maxValueForUnit();
                    if (delayValue > max) {
                        delayValue = max;
                    }
                    valueScrollInput.withRange(0, max + 1);
                    valueScrollInput.setState(delayValue);
                    sendDelayUpdate();
                });
        unitScrollInput.setState(unitIndex);
        addRenderableWidget(unitScrollInput);
    }

    private int maxValueForUnit() {
        return DispatcherMenu.MAX_DELAY_TICKS / UNIT_TICKS_PER_STEP[unitIndex];
    }

    private void sendDelayUpdate() {
        int ticks = Math.min(DispatcherMenu.MAX_DELAY_TICKS, delayValue * UNIT_TICKS_PER_STEP[unitIndex]);
        PacketDistributor.sendToServer(new SetDispatchDelayPacket(menu.containerId, ticks));
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
        guiGraphics.drawString(this.font, "Trains", 6 + OFFSET_X, 15, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Stations", 116 + OFFSET_X, 15, 0xFFFFFF, false);
        renderList(guiGraphics, menu.blockEntity.getAvailableTrains(), trainScroll, selectedTrain, 6 + OFFSET_X, 30, mouseX, mouseY);
        renderList(guiGraphics, menu.blockEntity.getAvailableStations(), stationScroll, selectedStation, 116 + OFFSET_X, 30, mouseX, mouseY);

        String train = selectedTrain >= 0 && selectedTrain < menu.blockEntity.getAvailableTrains().size()
                ? menu.blockEntity.getAvailableTrains().get(selectedTrain) : "Select a train";
        String station = selectedStation >= 0 && selectedStation < menu.blockEntity.getAvailableStations().size()
                ? menu.blockEntity.getAvailableStations().get(selectedStation) : "Select a station";
        guiGraphics.drawString(this.font, "Train Name", 8 + OFFSET_X, 136, 0xE0E0E0, false);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(train, 175), 8 + OFFSET_X, 149, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "To", 8 + OFFSET_X, 167, 0xE0E0E0, false);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(station, 175), 8 + OFFSET_X, 178, 0xFFFFFF, false);
        renderDelayBox(guiGraphics, mouseX, mouseY);
    }

    private void renderDelayBox(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // guiGraphics.drawString(this.font, "Delay", DELAY_ROW_X, DELAY_ROW_Y - 10, 0xE0E0E0, false);

        int valueX = DELAY_ROW_X;
        int unitX = valueX + VALUE_BOX_W + DELAY_BOX_GAP;
        drawScrollBox(guiGraphics, mouseX, mouseY, valueX, VALUE_BOX_W, String.valueOf(delayValue));
        drawScrollBox(guiGraphics, mouseX, mouseY, unitX, UNIT_BOX_W, UNIT_LABELS[unitIndex]);
    }

    private void drawScrollBox(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int w, String text) {
        // boolean hovered = mouseX >= leftPos + x && mouseX < leftPos + x + w
        //         && mouseY >= topPos + DELAY_ROW_Y && mouseY < topPos + DELAY_ROW_Y + DELAY_BOX_H;
        // guiGraphics.fill(x, DELAY_ROW_Y, x + w, DELAY_ROW_Y + DELAY_BOX_H, 0xFF6A6A6A);
        // guiGraphics.fill(x + 1, DELAY_ROW_Y + 1, x + w - 1, DELAY_ROW_Y + DELAY_BOX_H - 1,
        //         hovered ? 0xFF4A4A4A : 0xFF2B2B2B);
        int textWidth = this.font.width(text);
        guiGraphics.drawString(this.font, text, x + (w - textWidth) / 2, DELAY_ROW_Y + 4, 0xFFFFFF, false);
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
        boolean inListRow = mouseY >= topPos + 30 && mouseY < topPos + 30 + LIST_ROWS * 16;
        if (inListRow && mouseX >= leftPos + 6 && mouseX < leftPos + 108) {
            trainScroll = scroll(trainScroll, menu.blockEntity.getAvailableTrains().size(), scrollY);
            return true;
        }
        if (inListRow && mouseX >= leftPos + 116 && mouseX < leftPos + 218) {
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

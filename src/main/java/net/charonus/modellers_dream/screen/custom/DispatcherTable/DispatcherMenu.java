package net.charonus.modellers_dream.screen.custom.DispatcherTable;

import net.charonus.modellers_dream.block.ModBlocks;
import net.charonus.modellers_dream.block.entity.DispatcherTable.DispatcherTableBlockEntity;
import net.charonus.modellers_dream.dispatch.DispatchManager;
import net.charonus.modellers_dream.screen.ModMenuTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DispatcherMenu extends AbstractContainerMenu {
    public static final int TRAIN_BUTTON_BASE = 100;
    public static final int STATION_BUTTON_BASE = 1000;
    public static final int DIRECT_BUTTON = 2000;
    public static final int APPEND_BUTTON = 2001;
    // Delay no longer rides clickMenuButton's buttonId (see SetDispatchDelayPacket) -
    // that field is a legacy fixed-width one and isn't safe for an arbitrary tick count.
    public static final int MAX_DELAY_TICKS = 72000; // 60 minute cap - adjust if stations need longer holds
    public static final int DEFAULT_DELAY_TICKS = 100;
    public final DispatcherTableBlockEntity blockEntity;
    private final Level level;
    private int selectedTrain = -1;
    private int selectedStation = -1;
    private int delayTicks = DEFAULT_DELAY_TICKS;

    public int getDelayTicks() {
        return delayTicks;
    }

    /** Called by DispatchDelayPacketHandler; never trust the client value without clamping. */
    public void setDelayTicks(int ticks) {
        this.delayTicks = Math.clamp(ticks, 0, MAX_DELAY_TICKS);
    }

    public DispatcherMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public DispatcherMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ModMenuTypes.DISPATCHER_MENU.get(), containerId);
        this.blockEntity = ((DispatcherTableBlockEntity) blockEntity);
        this.level = inv.player.level();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.DISPATCHER_TABLE.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!stillValid(player)) {
            return false;
        }
        if (buttonId >= TRAIN_BUTTON_BASE && buttonId < STATION_BUTTON_BASE) {
            int index = buttonId - TRAIN_BUTTON_BASE;
            if (index < blockEntity.getAvailableTrains().size()) {
                selectedTrain = index;
                return true;
            }
            return false;
        }
        if (buttonId >= STATION_BUTTON_BASE && buttonId < DIRECT_BUTTON) {
            int index = buttonId - STATION_BUTTON_BASE;
            if (index < blockEntity.getAvailableStations().size()) {
                selectedStation = index;
                return true;
            }
            return false;
        }
        if (buttonId != DIRECT_BUTTON && buttonId != APPEND_BUTTON
                || selectedTrain < 0 || selectedStation < 0
                || selectedTrain >= blockEntity.getAvailableTrains().size()
                || selectedStation >= blockEntity.getAvailableStations().size()) {
            return false;
        }

        Train train = DispatchManager.findTrainByName(blockEntity.getAvailableTrains().get(selectedTrain));
        if (train == null || !DispatchManager.hasConductor(train)) {
            return false;
        }
        if (!DispatchManager.hasSchedule(train)) {
            player.displayClientMessage(
                    Component.literal("Train '" + train.name.getString()
                                    + "' has no schedule. Give the conductor a train schedule first.")
                            .withStyle(ChatFormatting.RED),
                    false);
            return false;
        }

        String station = blockEntity.getAvailableStations().get(selectedStation);

        if (buttonId == DIRECT_BUTTON) {
            DispatchManager.setDestination(train, station, delayTicks);
        } else {
            if (!DispatchManager.appendDestination(train, station, delayTicks)) {
                player.displayClientMessage(
                        Component.literal("Train '" + train.name.getString() + "' has too many stations queued")
                                .withStyle(ChatFormatting.RED),
                        false
                );
                return false;
            }
        }

        player.displayClientMessage(
                Component.literal("Dispatching '" + train.name.getString() + "' to '" + station + "'")
                        .withStyle(ChatFormatting.GREEN),
                false
        );


        return true;
    }

}

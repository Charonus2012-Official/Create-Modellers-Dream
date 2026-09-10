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
    public final DispatcherTableBlockEntity blockEntity;
    private final Level level;
    private int selectedTrain = -1;
    private int selectedStation = -1;

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
        player.displayClientMessage(
                Component.literal("Dispatching '" + train.name.getString() + "' to '" + station + "'")
                        .withStyle(ChatFormatting.GREEN),
                false
        );

        if (buttonId == DIRECT_BUTTON) {
            DispatchManager.setDestination(train, station);
        } else {
            DispatchManager.appendDestination(train, station);
        }
        return true;
    }

}

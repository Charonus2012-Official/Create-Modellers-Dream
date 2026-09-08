package net.charonus.modellers_dream.screen.custom.DispatcherTable;

import net.charonus.modellers_dream.block.ModBlocks;
import net.charonus.modellers_dream.block.entity.DispatcherTable.DispatcherTableBlockEntity;
import net.charonus.modellers_dream.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DispatcherMenu extends AbstractContainerMenu {
    public final DispatcherTableBlockEntity blockEntity;
    private final Level level;

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


}

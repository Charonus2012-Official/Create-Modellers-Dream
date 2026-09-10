package net.charonus.modellers_dream.item.custom;

import net.charonus.modellers_dream.block.entity.TrackConnector.TrackConnectorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DispatcherTableBlockItem extends BlockItem {
    public DispatcherTableBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player != null && player.isShiftKeyDown()) {
            stack.remove(DataComponents.CUSTOM_DATA);
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("modellers_dream.dispatcher_table.cleared"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (be instanceof TrackConnectorBlockEntity connector) {
            if (level.isClientSide) return InteractionResult.SUCCESS;

            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.put("LinkedConnector", NbtUtils.writeBlockPos(pos));
            tag.putString("LinkedConnectorDimension", level.dimension().location().toString());
            tag.putUUID("NetworkId", connector.getNetworkId());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            if (player != null) {
                player.displayClientMessage(Component.translatable("modellers_dream.dispatcher_table.linked"), true);
            }
            return InteractionResult.SUCCESS;
        }

        // Check for network data before allowing placement
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.copyTag().hasUUID("NetworkId")
                || !data.copyTag().contains("LinkedConnectorDimension")) {
            if (level.isClientSide && player != null) {
                player.displayClientMessage(Component.translatable("modellers_dream.dispatcher_table.missing_link").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            stack.remove(DataComponents.CUSTOM_DATA);
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("modellers_dream.dispatcher_table.cleared"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return super.use(level, player, hand);
    }
}

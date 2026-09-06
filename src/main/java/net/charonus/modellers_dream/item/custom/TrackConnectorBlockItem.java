package net.charonus.modellers_dream.item.custom;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.track.ITrackBlock;

import net.charonus.modellers_dream.block.entity.TrackConnector.TrackConnectorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TrackConnectorBlockItem extends BlockItem {

    private static final String LANG_PREFIX = "modellers_dream.track_connector.";

    public TrackConnectorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        // Shift-click cancels an in-progress selection
        if (player.isShiftKeyDown() && stack.has(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            clearSelection(stack);
            player.displayClientMessage(msg("clear"), true);
            AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, .5f);
            return InteractionResult.SUCCESS;
        }

        // First click: player targeted a rail -> remember its position + facing
        if (state.getBlock() instanceof ITrackBlock track) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;

            boolean front = isFacingTrackFront(track, level, pos, state, player.getLookAngle());

            stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS, pos);
            stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, front);

            player.displayClientMessage(msg("set"), true);
            AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, 1);
            return InteractionResult.SUCCESS;
        }

        // Clicked something that isn't a track, and nothing selected yet
        if (!stack.has(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
            player.displayClientMessage(msg("missing").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        // A track is already selected -> this is the 2nd (placement) click.
        BlockPos selectedPos = stack.get(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
        boolean front = stack.getOrDefault(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, false);

        if (TrackConnectorBlockEntity.isOccupied(level, selectedPos)) {
            player.displayClientMessage(msg("occupied").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        InteractionResult useOn = super.useOn(pContext);

        if (level.isClientSide || useOn == InteractionResult.FAIL)
            return useOn;

        clearSelection(stack);
        ItemStack itemInHand = player.getItemInHand(pContext.getHand());
        if (!itemInHand.isEmpty())
            clearSelection(itemInHand);


        return useOn;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean updated = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide && stack.has(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
            BlockPos targetPos = stack.get(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
            boolean front = stack.getOrDefault(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, false);
            if (level.getBlockEntity(pos) instanceof TrackConnectorBlockEntity be) {
                be.setTarget(targetPos, front);
                return true;
            }
        }
        return updated;
    }

    /**
     * Reimplementation of {@code ITrackBlock.getNearestTrackAxis(...)}'s direction math,
     * without needing the Catnip Pair type the original returns.
     */
    private static boolean isFacingTrackFront(ITrackBlock track, Level level, BlockPos pos, BlockState state,
                                              Vec3 lookVec) {
        Vec3 best = null;
        double bestDiff = Double.MAX_VALUE;

        for (Vec3 axis : track.getTrackAxes(level, pos, state)) {
            for (int opposite : new int[] { 1, -1 }) {
                double distanceTo = axis.normalize()
                        .distanceTo(lookVec.scale(opposite));
                if (distanceTo > bestDiff)
                    continue;
                bestDiff = distanceTo;
                best = axis;
            }
        }

        return lookVec.dot(best.multiply(1, 0, 1)
                .normalize()) < 0;
    }

    private static void clearSelection(ItemStack stack) {
        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
    }

    private static MutableComponent msg(String key) {
        return Component.translatable(LANG_PREFIX + key);
    }
}

package net.charonus.modellers_dream.block.custom.TrackConnector;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;

import net.charonus.modellers_dream.item.custom.TrackConnectorBlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

@EventBusSubscriber(Dist.CLIENT)
public class TrackConnectorOverlayRenderer {

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_PARTICLES)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof TrackConnectorBlockItem))
            return;

        ClientLevel level = mc.level;
        BlockPos pos;
        AxisDirection direction;

        if (stack.has(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
            // Already selected - lock onto that position, ignore where we're looking
            pos = stack.get(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
            boolean front = stack.getOrDefault(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, false);
            direction = front ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;

        } else {
            // Nothing selected yet - follow the live raycast
            HitResult hit = mc.hitResult;
            if (hit == null || hit.getType() != HitResult.Type.BLOCK)
                return;

            pos = ((BlockHitResult) hit).getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof ITrackBlock track))
                return;

            boolean front = isFacingTrackFront(track, level, pos, state, player.getLookAngle());
            direction = front ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        }

        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack ms = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        int light = LevelRenderer.getLightColor(level, pos);

        ms.pushPose();
        ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        TrackTargetingBehaviour.render(level, pos, direction, null, ms, buffer, light,
                OverlayTexture.NO_OVERLAY, RenderedTrackOverlayType.OBSERVER, 1 + 1 / 16f);
        ms.popPose();

        buffer.endBatch();
        RenderSystem.enableCull();
    }

    private static boolean isFacingTrackFront(ITrackBlock track, ClientLevel level, BlockPos pos, BlockState state,
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
}

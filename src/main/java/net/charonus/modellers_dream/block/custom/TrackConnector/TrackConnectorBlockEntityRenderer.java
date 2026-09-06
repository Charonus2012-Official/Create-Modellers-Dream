package net.charonus.modellers_dream.block.custom.TrackConnector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.charonus.modellers_dream.block.entity.TrackConnector.TrackConnectorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;

public class TrackConnectorBlockEntityRenderer implements BlockEntityRenderer<TrackConnectorBlockEntity> {
    public TrackConnectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TrackConnectorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockPos target = be.getTargetTrack();
        if (target == null)
            return;

        BlockPos pos = be.getBlockPos();
        boolean front = be.getTargetDirection();
        AxisDirection direction = front ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;

        ms.pushPose();
        ms.translate(target.getX() - pos.getX(), target.getY() - pos.getY(), target.getZ() - pos.getZ());
        TrackTargetingBehaviour.render(be.getLevel(), target, direction, null, ms, buffer, light,
                OverlayTexture.NO_OVERLAY, RenderedTrackOverlayType.OBSERVER, 1 + 1 / 16f);
        ms.popPose();
    }
}

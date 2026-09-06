package net.charonus.modellers_dream.block.custom.DispatcherTable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.charonus.modellers_dream.item.custom.DispatcherTableBlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

@EventBusSubscriber(value = Dist.CLIENT)
public class DispatcherTableOverlayRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_PARTICLES)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof DispatcherTableBlockItem)) {
            stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof DispatcherTableBlockItem))
                return;
        }

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.copyTag().contains("LinkedConnector"))
            return;

        BlockPos pos = NbtUtils.readBlockPos(data.copyTag(), "LinkedConnector").orElse(null);
        if (pos == null)
            return;

        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack ms = event.getPoseStack();

        ms.pushPose();
        ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(ms, buffer, 0, 0, 0, 1, 1, 1, 0.2f, 0.8f, 1f, 1f);

        ms.popPose();
    }
}

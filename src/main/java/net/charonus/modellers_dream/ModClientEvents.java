package net.charonus.modellers_dream;

import net.charonus.modellers_dream.block.custom.TrackConnector.TrackConnectorBlockEntityRenderer;
import net.charonus.modellers_dream.block.entity.ModBlockEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ModClientEvents {

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TRACK_CONNECTOR_BE.get(), TrackConnectorBlockEntityRenderer::new);
    }
}

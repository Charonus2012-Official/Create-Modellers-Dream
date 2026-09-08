package net.charonus.modellers_dream;

import net.charonus.modellers_dream.block.custom.TrackConnector.TrackConnectorBlockEntityRenderer;
import net.charonus.modellers_dream.block.entity.ModBlockEntities;
import net.charonus.modellers_dream.screen.ModMenuTypes;
import net.charonus.modellers_dream.screen.custom.DispatcherTable.DispatcherScreen;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ModClientEvents {
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TRACK_CONNECTOR_BE.get(), TrackConnectorBlockEntityRenderer::new);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.DISPATCHER_MENU.get(), DispatcherScreen::new);
    }
}

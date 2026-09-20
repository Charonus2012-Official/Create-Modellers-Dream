package net.charonus.modellers_dream.network;

import net.charonus.modellers_dream.screen.custom.DispatcherTable.DispatcherMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DispatchDelayPacketHandler {

    public static void handle(SetDispatchDelayPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        // Guard against a stale packet for a menu the player has since closed or switched
        if (serverPlayer.containerMenu.containerId != packet.containerId()) {
            return;
        }
        if (serverPlayer.containerMenu instanceof DispatcherMenu menu) {
            menu.setDelayTicks(packet.ticks());
        }
    }
}

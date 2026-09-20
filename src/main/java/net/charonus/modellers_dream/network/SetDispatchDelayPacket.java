package net.charonus.modellers_dream.network;

import io.netty.buffer.ByteBuf;
import net.charonus.modellers_dream.ModellersDream;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server: "the delay scroller in the open DispatcherMenu (containerId) now reads
 * this many ticks." Sent on every scroll change, not deferred to screen close, because
 * Direct/Append can be clicked while the screen is still open and must see the current value.
 *
 * This exists instead of routing through AbstractContainerMenu#clickMenuButton because
 * ServerboundContainerButtonClickPacket's buttonId is a legacy field that isn't safe to
 * carry an arbitrary tick count in - see DispatcherMenu for the button-range approach that
 * remains fine for the small fixed sets (trains/stations) it's still used for.
 */
public record SetDispatchDelayPacket(int containerId, int ticks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetDispatchDelayPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModellersDream.MOD_ID, "set_dispatch_delay"));

    public static final StreamCodec<ByteBuf, SetDispatchDelayPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetDispatchDelayPacket::containerId,
            ByteBufCodecs.VAR_INT, SetDispatchDelayPacket::ticks,
            SetDispatchDelayPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

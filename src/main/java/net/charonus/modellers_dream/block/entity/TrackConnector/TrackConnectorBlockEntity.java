package net.charonus.modellers_dream.block.entity.TrackConnector;

import net.charonus.modellers_dream.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrackConnectorBlockEntity extends BlockEntity {
    public static final Set<TrackConnectorBlockEntity> CONNECTORS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private BlockPos targetOffset;
    private boolean targetDirection;
    private UUID networkId;

    public TrackConnectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TRACK_CONNECTOR_BE.get(), pos, blockState);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide) {
            CONNECTORS.add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            CONNECTORS.remove(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            CONNECTORS.remove(this);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (targetOffset != null) {
            tag.put("TargetOffset", NbtUtils.writeBlockPos(targetOffset));
            tag.putBoolean("TargetDirection", targetDirection);
        }
        if (networkId != null) {
            tag.putUUID("NetworkId", networkId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("TargetOffset")) {
            targetOffset = NbtUtils.readBlockPos(tag, "TargetOffset").orElse(null);
            targetDirection = tag.getBoolean("TargetDirection");
        }
        if (tag.hasUUID("NetworkId")) {
            networkId = tag.getUUID("NetworkId");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static boolean isOccupied(Level level, BlockPos targetTrack) {
        for (TrackConnectorBlockEntity connector : CONNECTORS) {
            if (connector.level == level && targetTrack.equals(connector.getTargetTrack())) {
                return true;
            }
        }
        return false;
    }

    public void setTarget(BlockPos targetPos, boolean direction) {
        this.targetOffset = targetPos.subtract(worldPosition);
        this.targetDirection = direction;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public BlockPos getTargetTrack() {
        return targetOffset == null ? null : worldPosition.offset(targetOffset.getX(), targetOffset.getY(), targetOffset.getZ());
    }

    public boolean getTargetDirection() {
        return targetDirection;
    }

    public UUID getNetworkId() {
        if (networkId == null) {
            networkId = UUID.randomUUID();
            setChanged();
        }
        return networkId;
    }
}

package net.charonus.modellers_dream.block.entity.DispatcherTable;

import net.charonus.modellers_dream.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class DispatcherTableBlockEntity extends BlockEntity {
    private UUID networkId;
    private BlockPos linkedConnectorPos;

    public DispatcherTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISPATCHER_TABLE_BE.get(), pos, state);
    }

    public void setNetworkId(UUID networkId) {
        this.networkId = networkId;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setLinkedConnectorPos(BlockPos linkedConnectorPos) {
        this.linkedConnectorPos = linkedConnectorPos;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public BlockPos getLinkedConnectorPos() {
        return linkedConnectorPos;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (networkId != null) {
            tag.putUUID("NetworkId", networkId);
        }
        if (linkedConnectorPos != null) {
            tag.put("LinkedConnector", NbtUtils.writeBlockPos(linkedConnectorPos));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("NetworkId")) {
            networkId = tag.getUUID("NetworkId");
        }
        if (tag.contains("LinkedConnector")) {
            linkedConnectorPos = NbtUtils.readBlockPos(tag, "LinkedConnector").orElse(null);
        }
    }
}

package net.charonus.modellers_dream.block.entity.DispatcherTable;

import net.charonus.modellers_dream.block.entity.ModBlockEntities;
import net.charonus.modellers_dream.block.entity.TrackConnector.TrackConnectorBlockEntity;
import net.charonus.modellers_dream.block.entity.TrackConnector.TrackConnectorQuery;
import net.charonus.modellers_dream.screen.custom.DispatcherTable.DispatcherMenu;
import net.charonus.modellers_dream.ModellersDream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class DispatcherTableBlockEntity extends BlockEntity implements MenuProvider {
    private UUID networkId;
    private BlockPos linkedConnectorPos;
    private ResourceKey<net.minecraft.world.level.Level> linkedConnectorDimension;
    private List<String> availableTrains = List.of();
    private List<String> availableStations = List.of();

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

    public void setLinkedConnectorDimension(ResourceKey<net.minecraft.world.level.Level> linkedConnectorDimension) {
        this.linkedConnectorDimension = linkedConnectorDimension;
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

    public ResourceKey<net.minecraft.world.level.Level> getLinkedConnectorDimension() {
        return linkedConnectorDimension;
    }

    public List<String> getAvailableTrains() {
        return availableTrains;
    }

    public List<String> getAvailableStations() {
        return availableStations;
    }

    /** Refreshes the small client-facing list cache when this table is opened. */
    public void refreshAvailableNetworkData() {
        TrackConnectorQuery.QueryResult result = queryLinkedNetwork();
        availableTrains = result.trains.stream().map(train -> train.name).sorted().toList();
        availableStations = result.stations.stream().map(station -> station.name).sorted().toList();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Resolves the connector at its saved position and dimension, then verifies that
     * its persistent link UUID still matches this table before querying Create's live graph.
     */
    public TrackConnectorQuery.QueryResult queryLinkedNetwork() {
        if (!(level instanceof ServerLevel tableLevel)
                || linkedConnectorPos == null
                || linkedConnectorDimension == null
                || networkId == null) {
            return TrackConnectorQuery.QueryResult.empty();
        }

        ServerLevel connectorLevel = tableLevel.getServer().getLevel(linkedConnectorDimension);
        if (connectorLevel == null
                || !(connectorLevel.getBlockEntity(linkedConnectorPos) instanceof TrackConnectorBlockEntity connector)
                || !networkId.equals(connector.getNetworkId())) {
            return TrackConnectorQuery.QueryResult.empty();
        }

        return TrackConnectorQuery.query(connector, connectorLevel);
    }

    /** Temporary diagnostics until the menu receives synced query data. */
    public void logLinkedNetwork() {
        if (!(level instanceof ServerLevel tableLevel)) {
            ModellersDream.LOGGER.info("Dispatcher Table at {} cannot query: not on the server", worldPosition);
            return;
        }
        if (linkedConnectorPos == null || linkedConnectorDimension == null || networkId == null) {
            ModellersDream.LOGGER.info("Dispatcher Table at {} has an incomplete link: position={}, dimension={}, networkId={}",
                    worldPosition, linkedConnectorPos, linkedConnectorDimension, networkId);
            return;
        }

        ServerLevel connectorLevel = tableLevel.getServer().getLevel(linkedConnectorDimension);
        if (connectorLevel == null) {
            ModellersDream.LOGGER.info("Dispatcher Table at {} cannot find linked dimension {}", worldPosition,
                    linkedConnectorDimension.location());
            return;
        }
        if (!(connectorLevel.getBlockEntity(linkedConnectorPos) instanceof TrackConnectorBlockEntity connector)) {
            ModellersDream.LOGGER.info("Dispatcher Table at {} cannot find a Track Connector at {} in {}",
                    worldPosition, linkedConnectorPos, linkedConnectorDimension.location());
            return;
        }
        if (!networkId.equals(connector.getNetworkId())) {
            ModellersDream.LOGGER.info("Dispatcher Table at {} has a UUID mismatch: table={}, connector={}",
                    worldPosition, networkId, connector.getNetworkId());
            return;
        }
        if (connector.getTargetTrack() == null) {
            ModellersDream.LOGGER.info("Dispatcher Table at {} found its connector, but it has no selected target track",
                    worldPosition);
            return;
        }
        if (TrackConnectorQuery.resolveGraph(connectorLevel, connector.getTargetTrack()) == null) {
            ModellersDream.LOGGER.info("Dispatcher Table at {} found connector {} and target rail {}, but Create has no graph for that rail",
                    worldPosition, linkedConnectorPos, connector.getTargetTrack());
            return;
        }

        TrackConnectorQuery.QueryResult result = TrackConnectorQuery.query(connector, connectorLevel);
        ModellersDream.LOGGER.info("Dispatcher Table at {} resolved graph {} ({} trains, {} stations)",
                worldPosition, result.graphId, result.trains.size(), result.stations.size());
        result.trains.forEach(train -> ModellersDream.LOGGER.info("  Train: {}", train.name));
        result.stations.forEach(station -> ModellersDream.LOGGER.info("  Station: {}", station.name));
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
        if (linkedConnectorDimension != null) {
            tag.putString("LinkedConnectorDimension", linkedConnectorDimension.location().toString());
        }
        writeStringList(tag, "AvailableTrains", availableTrains);
        writeStringList(tag, "AvailableStations", availableStations);
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
        if (tag.contains("LinkedConnectorDimension")) {
            ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString("LinkedConnectorDimension"));
            if (dimensionId != null) {
                linkedConnectorDimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
            }
        }
        availableTrains = readStringList(tag, "AvailableTrains");
        availableStations = readStringList(tag, "AvailableStations");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Dispatcher Table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DispatcherMenu(i, inventory, this);
    }

    private static void writeStringList(CompoundTag tag, String key, List<String> values) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        values.forEach(value -> list.add(net.minecraft.nbt.StringTag.valueOf(value)));
        tag.put(key, list);
    }

    private static List<String> readStringList(CompoundTag tag, String key) {
        if (!tag.contains(key, net.minecraft.nbt.Tag.TAG_LIST)) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (net.minecraft.nbt.Tag value : tag.getList(key, net.minecraft.nbt.Tag.TAG_STRING)) {
            values.add(value.getAsString());
        }
        return List.copyOf(values);
    }
}

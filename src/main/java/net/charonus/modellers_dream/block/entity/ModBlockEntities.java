package net.charonus.modellers_dream.block.entity;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.block.ModBlocks;
import net.charonus.modellers_dream.block.custom.TrackConnector.TrackConnectorBlockEntityRenderer;
import net.charonus.modellers_dream.block.entity.DispatcherTable.DispatcherTableBlockEntity;
import net.charonus.modellers_dream.block.entity.TrackConnector.TrackConnectorBlockEntity;

public class ModBlockEntities {

    public static final BlockEntityEntry<TrackConnectorBlockEntity> TRACK_CONNECTOR_BE = ModellersDream.REGISTRATE
            .blockEntity("track_connector_be", TrackConnectorBlockEntity::new)
            .validBlock(ModBlocks.TRACK_CONNECTOR::get)
            .renderer(() -> TrackConnectorBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<DispatcherTableBlockEntity> DISPATCHER_TABLE_BE = ModellersDream.REGISTRATE
            .blockEntity("dispatcher_table_be", DispatcherTableBlockEntity::new)
            .validBlock(ModBlocks.DISPATCHER_TABLE::get)
            .register();

    public static void register() {}
}

package net.charonus.modellers_dream.block.entity;

import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.block.ModBlocks;
import net.charonus.modellers_dream.block.entity.DispatcherTable.DispatcherTableBlockEntity;
import net.charonus.modellers_dream.block.entity.TrackConnector.TrackConnectorBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModellersDream.MOD_ID);

    public static final Supplier<BlockEntityType<TrackConnectorBlockEntity>> TRACK_CONNECTOR_BE =
            BLOCK_ENTITIES.register("track_connector_be", () -> BlockEntityType.Builder.of(
                    TrackConnectorBlockEntity::new, ModBlocks.TRACK_CONNECTOR.get()).build(null)
            );

    public static final Supplier<BlockEntityType<DispatcherTableBlockEntity>> DISPATCHER_TABLE_BE =
            BLOCK_ENTITIES.register("dispatcher_table_be", () -> BlockEntityType.Builder.of(
                    DispatcherTableBlockEntity::new, ModBlocks.DISPATCHER_TABLE.get()).build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

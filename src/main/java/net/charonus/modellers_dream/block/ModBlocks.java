package net.charonus.modellers_dream.block;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.block.custom.DispatcherTable.DispatcherTableBlock;
import net.charonus.modellers_dream.block.custom.TrackConnector.TrackConnectorBlock;
import net.charonus.modellers_dream.item.custom.DispatcherTableBlockItem;
import net.charonus.modellers_dream.item.custom.TrackConnectorBlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class ModBlocks {

    public static final BlockEntry<DispatcherTableBlock> DISPATCHER_TABLE= ModellersDream.REGISTRATE
            .block("dispatcher_table", DispatcherTableBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion().sound(SoundType.WOOD))
            .blockstate(NonNullBiConsumer.noop())
            .transform(axeOrPickaxe())
            .item(DispatcherTableBlockItem::new)
            .build()
            .register();

    public static final BlockEntry<TrackConnectorBlock> TRACK_CONNECTOR = ModellersDream.REGISTRATE
            .block("track_connector", TrackConnectorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.PODZOL).noOcclusion().sound(SoundType.NETHERITE_BLOCK))
            .blockstate(NonNullBiConsumer.noop())
            .transform(pickaxeOnly())
            .item(TrackConnectorBlockItem::new)
            .build()
            .register();

    public static void register() {}
}

package net.charonus.modellers_dream.block;

import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.block.custom.DispatcherTable.DispatcherTableBlock;
import net.charonus.modellers_dream.block.custom.TrackConnector.TrackConnectorBlock;
import net.charonus.modellers_dream.item.ModItems;
import net.charonus.modellers_dream.item.custom.DispatcherTableBlockItem;
import net.charonus.modellers_dream.item.custom.TrackConnectorBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(ModellersDream.MOD_ID);

    // public static final DeferredBlock<Block> BLOCK = registerBlock("block_id",
    //         () -> new Block(BlockBehaviour.Properties.of()
    //                 .strength(4f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

    public static final DeferredBlock<Block> DISPATCHER_TABLE = registerBlock("dispatcher_table",
            () -> new DispatcherTableBlock(BlockBehaviour.Properties.of().noOcclusion().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD)),
            DispatcherTableBlockItem::new);

    public static final DeferredBlock<Block> TRACK_CONNECTOR = registerBlock("track_connector",
            () -> new TrackConnectorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK)
                    .mapColor(MapColor.PODZOL)
                    .noOcclusion()
                    .sound(SoundType.NETHERITE_BLOCK))
    , TrackConnectorBlockItem::new);

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block, BiFunction<Block, Item.Properties, ? extends BlockItem> blockItem) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, blockItem);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block,
                                                             BiFunction<Block, Item.Properties, ? extends BlockItem> itemFactory) {
        ModItems.ITEMS.register(name, () -> itemFactory.apply(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

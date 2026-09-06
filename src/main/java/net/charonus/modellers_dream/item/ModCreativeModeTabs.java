package net.charonus.modellers_dream.item;

import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModellersDream.MOD_ID);

    public static final Supplier<CreativeModeTab> MODELLERS_DREAM_TAB = CREATIVE_MODE_TAB.register("modellers_dream_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.DISPATCHER_TABLE))
                    .title(Component.translatable("creativetab.modellers_dream.modellers_dream_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.DISPATCHER_TABLE);
                        output.accept(ModBlocks.TRACK_CONNECTOR);
                        output.accept(ModItems.TRACK_COMMUNICATOR);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}

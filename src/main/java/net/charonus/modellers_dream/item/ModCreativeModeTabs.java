package net.charonus.modellers_dream.item;

import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModellersDream.MOD_ID);

    public static final ResourceKey<CreativeModeTab> MODELLERS_DREAM_TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                    ResourceLocation.fromNamespaceAndPath(ModellersDream.MOD_ID, "modellers_dream_tab"));

    public static final Supplier<CreativeModeTab> MODELLERS_DREAM_TAB = CREATIVE_MODE_TAB.register("modellers_dream_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.DISPATCHER_TABLE))
                    .title(Component.translatable("creativetab.modellers_dream.modellers_dream_tab"))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}

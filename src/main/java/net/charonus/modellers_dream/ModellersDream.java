package net.charonus.modellers_dream;

import com.tterrag.registrate.Registrate;
import net.charonus.modellers_dream.block.ModBlocks;
import net.charonus.modellers_dream.block.entity.ModBlockEntities;
import net.charonus.modellers_dream.command.DispatchCommand;
import net.charonus.modellers_dream.compat.computercraft.CCCompatRegistry;
import net.charonus.modellers_dream.item.ModCreativeModeTabs;
import net.charonus.modellers_dream.item.ModItems;
import net.charonus.modellers_dream.network.DispatchDelayPacketHandler;
import net.charonus.modellers_dream.network.SetDispatchDelayPacket;
import net.charonus.modellers_dream.screen.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(ModellersDream.MOD_ID)
public class ModellersDream {
    public static final String MOD_ID = "modellers_dream";

    public static final Registrate REGISTRATE = Registrate.create(MOD_ID)
            .defaultCreativeTab(ModCreativeModeTabs.MODELLERS_DREAM_TAB_KEY);

    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ModellersDream(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);


        new LangProvider();


        ModBlocks.register();
        ModItems.register();


        ModBlockEntities.register();
        ModMenuTypes.register();


        ModCreativeModeTabs.register(modEventBus);

        if (ModList.get().isLoaded("computercraft")) {
            CCCompatRegistry.register(modEventBus);
        }

        if (FMLEnvironment.dist == Dist.CLIENT) {
        }

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(SetDispatchDelayPacket.TYPE, SetDispatchDelayPacket.STREAM_CODEC, DispatchDelayPacketHandler::handle);
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        DispatchCommand.register(event.getDispatcher());
    }
}

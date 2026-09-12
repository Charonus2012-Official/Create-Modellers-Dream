package net.charonus.modellers_dream.compat.computercraft;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.charonus.modellers_dream.block.entity.DispatcherTable.DispatcherTableBlockEntity;
import net.charonus.modellers_dream.block.entity.ModBlockEntities;

public class CCCompatRegistry {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterCapabilitiesEvent.class, CCCompatRegistry::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                PeripheralCapability.get(),
                ModBlockEntities.DISPATCHER_TABLE_BE.get(),
                (dispatcher, side) -> new DispatcherPeripheral(dispatcher)
        );
    }
}

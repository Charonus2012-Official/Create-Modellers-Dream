package net.charonus.modellers_dream.item;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.item.custom.TrackCommunicatorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModellersDream.MOD_ID);

    public static final DeferredItem<Item> TRACK_COMMUNICATOR = ITEMS.register("track_communicator",
            () -> new TrackCommunicatorItem(new Item.Properties()));

    public static final DeferredItem<Item> INCOMPLETE_TRACK_COMMUNICATOR = ITEMS.register("incomplete_track_communicator",
            () -> new SequencedAssemblyItem(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

package net.charonus.modellers_dream.item;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.charonus.modellers_dream.ModellersDream;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final ItemEntry<Item> TRACK_COMMUNICATOR = ingredient("track_communicator");

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_TRACK_COMMUNICATOR = sequencedIngredient("incomplete_track_communicator");



    private static ItemEntry<Item> ingredient(String name) {
		return ModellersDream.REGISTRATE.item(name, Item::new)
			.register();
	}

    private static ItemEntry<SequencedAssemblyItem> sequencedIngredient(String name) {
		return ModellersDream.REGISTRATE.item(name, SequencedAssemblyItem::new)
			.register();
	}

    public static void register() {}
}

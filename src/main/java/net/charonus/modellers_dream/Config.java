package net.charonus.modellers_dream;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();


    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MAX_PENDING_STATIONS;

    static {
        BUILDER.comment("Dispatcher behavior limits");
        BUILDER.push("dispatcher");

        MAX_PENDING_STATIONS = BUILDER
                .comment(
                        "Maximum number of stations that can be queued per train.",
                        "Prevents spam and runaway schedules.",
                        "Default: 16"
                )
                .defineInRange("max_pending_stations", 16, 1, 256);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}

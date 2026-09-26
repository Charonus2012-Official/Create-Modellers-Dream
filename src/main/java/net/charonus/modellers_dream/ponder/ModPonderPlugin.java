package net.charonus.modellers_dream.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.block.ModBlocks;
import net.charonus.modellers_dream.ponder.TrackNetwork.TrackNetworkScenes;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModPonderPlugin implements PonderPlugin {
    @Override
    public @NotNull String getModId() {
        return ModellersDream.MOD_ID;
    }


    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        TrackNetworkScenes.register(helper);
        helper.forComponents(AllItems.SCHEDULE.getId())
                .addStoryBoard("deployer_schedule/deploying", DeployerScheduleScenes::deployerSchedule);
    }
}

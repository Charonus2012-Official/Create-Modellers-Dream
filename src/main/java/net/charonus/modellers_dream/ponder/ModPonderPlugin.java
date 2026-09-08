package net.charonus.modellers_dream.ponder;

import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.ponder.TrackNetwork.TrackNetworkScenes;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class ModPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return ModellersDream.MOD_ID;
    }


    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        TrackNetworkScenes.register(helper);
    }
}

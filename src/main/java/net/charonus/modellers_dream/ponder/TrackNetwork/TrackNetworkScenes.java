package net.charonus.modellers_dream.ponder.TrackNetwork;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.charonus.modellers_dream.block.ModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrackNetworkScenes {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ModBlocks.TRACK_CONNECTOR.getId(), ModBlocks.DISPATCHER_TABLE.getId())
                .addStoryBoard("network/setup", TrackNetworkScenes::settingUp);
    }

    public static void settingUp(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("track_network_setup", "Setting up the Track Network");

        Selection connector = util.select().position(7, 1, 2);
        Vec3 connectorVec = util.vector().topOf(7, 1, 2);

        Selection dispatcher = util.select().position(3, 1, 2);
        Vec3 dispatcherVec = util.vector().topOf(3, 1, 2);


        scene.configureBasePlate(0,0,9);
        scene.showBasePlate();



        for (int i = 8; i >= 0; i--) {
			scene.world().showSection(util.select().position(i, 1, 6), Direction.DOWN);
			scene.idle(1);
		}
        scene.idle(20);


        Vec3 target = util.vector().topOf(7, 0, 6);
        AABB bb = new AABB(target, target).move(0, 2 / 16f, 0);

        scene.overlay().showControls(target, Pointing.DOWN, 40).rightClick()
                        .withItem(ModBlocks.TRACK_CONNECTOR.toStack());
        scene.idle(6);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.inflate(.45f, 1 / 16f, .45f), 60);
		scene.idle(10);

        scene.overlay().showText(50)
                .pointAt(target)
                .placeNearTarget()
                .colored(PonderPalette.GREEN)
                .text("Select a Train Track and place the Connector nearby");


        scene.idle(20);
        scene.world().showSection(connector, Direction.DOWN);

        scene.idle(100);


        scene.overlay().showText(40)
                .text("Now we need to add a Dispatcher Table")
                .attachKeyFrame();

        scene.idle(30);

        scene.overlay().showControls(connectorVec, Pointing.DOWN, 40).rightClick()
                .withItem(ModBlocks.DISPATCHER_TABLE.toStack());
        scene.idle(6);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), connector, 60);
        scene.idle(10);

        scene.overlay().showText(80)
                .pointAt(connectorVec)
                .placeNearTarget()
                .colored(PonderPalette.BLUE)
                .text("Right-Click the Track Connector with the Dispatcher Table to connect to the network, then place it somewhere");
        scene.idle(90);
        scene.world().showSection(dispatcher, Direction.DOWN);
        scene.idle(70);

        scene.overlay().showControls(dispatcherVec, Pointing.DOWN, 50).rightClick();
        scene.idle(10);
        scene.overlay().showText(50)
                .text("Right-Click to open the Dispatcher Table GUI and go dispatch some trains!")
                .pointAt(dispatcherVec)
                .placeNearTarget()
                .colored(PonderPalette.GREEN);

        scene.idle(80);

        scene.overlay().showText(80)
                .attachKeyFrame()
                .text("To dispatch trains you also need to put an empty train schedule to the driver!")
                .colored(PonderPalette.RED);
        scene.idle(80);
    }
}

package net.charonus.modellers_dream.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DeployerScheduleScenes {

    public static void deployerSchedule(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("deployer_schedule", "Deploying Schedules to Stations");
        scene.configureBasePlate(0, 0, 8);
        scene.scaleSceneView(.85f);
        scene.showBasePlate();

        // --- Adjust these grid coordinates to match your recorded schematic ---
        BlockPos stationPos = util.grid().at(4, 1, 3);
        BlockPos deployerPos = util.grid().at(4, 1, 1);
        Selection everythingButTrain = util.select().layer(0).add(util.select().layer(1));
        Selection train = util.select().fromTo(4, 2, 5, 6, 3, 5);
        BlockPos controls1 = util.grid().at(4, 3, 5);
        BlockPos controls2 = util.grid().at(6, 3, 5);

        scene.world().showSection(everythingButTrain, Direction.UP);
        // --- Spin the shafts/cogs powering the Deployer (adjust selection if only part of the base plate should turn) ---
        scene.world().setKineticSpeed(everythingButTrain, 16);
        scene.idle(10);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(deployerPos))
                .placeNearTarget()
                .text("A Deployer holding a Schedule can deliver it straight to a Station");
        scene.idle(80);

        // --- Give the Deployer a Schedule item (visual only, matches DeployerScenes' pattern) ---
        ItemStack schedule = AllItems.SCHEDULE.asStack();
        Vec3 frontVec = util.vector().blockSurface(deployerPos, Direction.WEST)
                .add(0, 0, .125);

        scene.overlay().showControls(frontVec, Pointing.DOWN, 40).rightClick()
                .withItem(schedule);
        scene.idle(7);
        scene.world().modifyBlockEntityNBT(util.select().position(deployerPos), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", schedule.saveOptional(scene.world().getHolderLookupProvider())));
        scene.idle(15);

        // --- No train present yet: Deployer does nothing ---
        scene.overlay().showText(70)
                .pointAt(util.vector().topOf(deployerPos))
                .placeNearTarget()
                .colored(PonderPalette.RED)
                .text("With no Train at the Station, the Deployer simply does nothing");
        scene.idle(10);
        scene.world().moveDeployer(deployerPos, 1, 25);
        scene.idle(26);
        scene.world().moveDeployer(deployerPos, -1, 25);
        scene.idle(60);

        // --- Train is already present ---
        scene.addKeyframe();

        scene.world().toggleControls(controls1);
        scene.world().toggleControls(controls2);

        ElementLink<WorldSectionElement> trainElement = scene.world().showIndependentSection(train, Direction.DOWN);
        Vec3 birbTarget = util.vector().centerOf(4, 4, 4);
        ElementLink<ParrotElement> birb = scene.special().createBirb(birbTarget, ParrotPose.FacePointOfInterestPose::new);
        scene.idle(20);

        scene.world().animateTrainStation(stationPos, true);
        scene.effects().indicateSuccess(stationPos);
        scene.idle(15);

        scene.overlay().showText(60)
                .pointAt(util.vector().topOf(stationPos))
                .placeNearTarget()
                .attachKeyFrame()
                .text("Now that a Train is present...");
        scene.idle(50);

        // --- Deployer fires, schedule is consumed and applied ---
        scene.world().moveDeployer(deployerPos, 1, 25);
        scene.idle(20);

        AABB stationBB = new AABB(stationPos);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, stationBB, stationBB.inflate(.3f), 30);
        scene.idle(6);

        scene.world().modifyBlockEntityNBT(util.select().position(deployerPos), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", ItemStack.EMPTY.saveOptional(scene.world().getHolderLookupProvider())));
        scene.world().moveDeployer(deployerPos, -1, 25);
        scene.special().conductorBirb(birb, true);
        scene.idle(10);

        scene.overlay().showText(90)
                .pointAt(util.vector().topOf(stationPos))
                .placeNearTarget()
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .text("...the Schedule is handed directly to the Train's Conductor, and consumed");
        scene.idle(90);

        // --- Train departs, now scheduled — drives off the baseplate and disappears ---
        scene.world().animateTrainStation(stationPos, false);
        scene.world().moveSection(trainElement, util.vector().of(-10, 0, 0), 40);
        scene.special().moveParrot(birb, util.vector().of(-10, 0, 0), 40);
        scene.world().animateBogey(util.grid().at(5, 2, 5), -4f, 40);
        scene.idle(40);
        scene.world().hideIndependentSection(trainElement, Direction.DOWN);
        scene.special().hideElement(birb, Direction.UP);

        scene.idle(20);
    }

}

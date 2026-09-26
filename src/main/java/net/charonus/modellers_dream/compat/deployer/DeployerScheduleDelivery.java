package net.charonus.modellers_dream.compat.deployer;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleItem;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import net.charonus.modellers_dream.ModellersDream;
import org.slf4j.Logger;

import java.util.List;

/**
 * Lets a Create Deployer holding a Schedule item deliver that schedule
 * directly to whatever train is currently present at a Station, by
 * targeting the Station block from the side. No DispatchManager involved
 * — this talks straight to Create's Train / ScheduleRuntime.
 *
 * Two entry points are needed: the Deployer's raytrace can resolve to
 * either the Station block itself (RightClickBlock) OR an entity
 * occupying that same space — e.g. a train's SeatEntity — in which case
 * Create's own DeployerHandler takes the entity branch instead
 * (EntityInteract) and never reaches the block-click code at all.
 *
 * Behavior:
 *  - No train at the station           -> no-op
 *  - Train present, no conductor        -> no-op (matches vanilla's silent
 *                                          DestinationInstruction failure)
 *  - Train present, already scheduled  -> no-op (for now)
 *  - Train present, conductor present,
 *    no schedule yet                   -> schedule applied, item consumed
 */
@EventBusSubscriber(modid = ModellersDream.MOD_ID)
public class DeployerScheduleDelivery {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onDeployerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof DeployerFakePlayer))
            return;

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ScheduleItem))
            return;

        LOGGER.debug("[ModellersDream] Deployer hit BLOCK at {} holding schedule item", event.getPos());

        if (tryDeliver(event.getLevel(), event.getPos(), stack)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onDeployerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof DeployerFakePlayer))
            return;

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ScheduleItem))
            return;

        LOGGER.debug("[ModellersDream] Deployer hit ENTITY {} at {} holding schedule item",
                event.getTarget().getClass().getSimpleName(), event.getPos());

        if (tryDeliver(event.getLevel(), event.getPos(), stack)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    /**
     * @return true if a schedule was successfully applied and the item shrunk
     */
    private static boolean tryDeliver(Level level, BlockPos pos, ItemStack stack) {
        if (level.isClientSide)
            return false;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof StationBlockEntity stationBE)) {
            LOGGER.debug("[ModellersDream] No StationBlockEntity at {}", pos);
            return false;
        }

        GlobalStation station = stationBE.getStation();
        if (station == null) {
            LOGGER.debug("[ModellersDream] Station at {} has no resolved GlobalStation", pos);
            return false;
        }

        Train train = station.getPresentTrain();
        if (train == null) {
            LOGGER.debug("[ModellersDream] No train present at station {}", pos);
            return false;
        }

        if (!train.hasForwardConductor() && !train.hasBackwardConductor()) {
            LOGGER.debug("[ModellersDream] Train at {} has no conductor present, refusing", pos);
            return false;
        }

        if (train.runtime.getSchedule() != null) {
            LOGGER.debug("[ModellersDream] Train at {} already has a schedule, refusing", pos);
            return false;
        }

        Schedule schedule = ScheduleItem.getSchedule(level.registryAccess(), stack);
        if (schedule == null || schedule.entries.isEmpty()) {
            LOGGER.debug("[ModellersDream] Held item has an empty schedule");
            schedule = new Schedule(List.of(), false, 0);
        }

        train.runtime.setSchedule(schedule, false);
        stack.shrink(1);

        LOGGER.info("[ModellersDream] Delivered schedule to train at station {}", pos);
        return true;
    }

}

package net.charonus.modellers_dream.mixin;

import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.display.GlobalTrainDisplayData.TrainDeparturePrediction;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(value = ScheduleRuntime.class, remap = false)
public class ScheduleRuntimeMixin {

    /*
     * Create's setSchedule() calculates:
     *
     * Mth.clamp(savedProgress, 0, entries.size() - 1)
     *
     * For an empty schedule that becomes -1.
     *
     * We turn an empty schedule into a harmless completed/paused schedule.
     */
    @Inject(
            method = "setSchedule",
            at = @At("TAIL")
    )
    private void modellersDream$fixEmptySchedule(
            Schedule schedule,
            boolean auto,
            CallbackInfo ci
    ) {
        if (schedule == null || !schedule.entries.isEmpty())
            return;

        ScheduleRuntime runtime = (ScheduleRuntime) (Object) this;

        runtime.currentEntry = 0;
        runtime.paused = true;
        runtime.completed = true;

        // An empty schedule cannot meaningfully cycle.
        schedule.cyclic = false;
    }

    /*
     * Create's getWaitingStatus() directly does:
     *
     * schedule.entries.get(currentEntry)
     *
     * which crashes when entries is empty.
     */
    @Inject(
            method = "getWaitingStatus",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modellersDream$emptyWaitingStatus(
            net.minecraft.world.level.Level level,
            CallbackInfoReturnable<MutableComponent> cir
    ) {
        ScheduleRuntime runtime = (ScheduleRuntime) (Object) this;

        if (runtime.schedule != null && runtime.schedule.entries.isEmpty()) {
            cir.setReturnValue(Component.empty());
        }
    }

    /*
     * Create's prediction code also assumes there is at least one entry.
     *
     * For an empty schedule, there is simply nothing to predict.
     */
    @Inject(
            method = "submitPredictions",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modellersDream$emptyPredictions(
            CallbackInfoReturnable<Collection<TrainDeparturePrediction>> cir
    ) {
        ScheduleRuntime runtime = (ScheduleRuntime) (Object) this;

        if (runtime.schedule != null && runtime.schedule.entries.isEmpty()) {
            cir.setReturnValue(java.util.Collections.emptyList());
        }
    }
    @Inject(
            method = "destinationReached",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modellersDream$handleEmptySchedule(CallbackInfo ci) {

        ScheduleRuntime runtime = (ScheduleRuntime) (Object) this;

        // Normal schedules should use Create's original logic.
        if (runtime.schedule == null || !runtime.schedule.entries.isEmpty())
            return;

        /*
         * Empty schedule:
         *
         * There is no current entry, so there is nothing to process.
         * Mark the schedule as completed and prevent Create from
         * executing the original destinationReached() code.
         */

        runtime.state = ScheduleRuntime.State.POST_TRANSIT;
        runtime.currentEntry = 0;
        runtime.paused = true;
        runtime.completed = true;

        runtime.conditionProgress.clear();
        runtime.conditionContext.clear();

        runtime.displayLinkUpdateRequested = true;

        // Skip the original method completely.
        ci.cancel();
    }
}

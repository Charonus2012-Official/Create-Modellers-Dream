package net.charonus.modellers_dream.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ScheduleItem.class, remap = false)
public class ScheduleItemMixin {

    // An empty Schedule Item normally has no TRAIN_SCHEDULE component,
    // causing Create's getSchedule() to return null.
    @ModifyExpressionValue(
            method = "handScheduleTo",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/trains/schedule/ScheduleItem;getSchedule(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/world/item/ItemStack;)Lcom/simibubi/create/content/trains/schedule/Schedule;"
            )
    )
    private Schedule modellersDream$createEmptySchedule(Schedule original) {
        return original != null ? original : new Schedule();
    }

    // Prevent Create from rejecting the empty schedule.
    @ModifyExpressionValue(
            method = "handScheduleTo",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;isEmpty()Z"
            )
    )
    private boolean modellersDream$allowEmptySchedule(boolean original) {
        return false;
    }
}

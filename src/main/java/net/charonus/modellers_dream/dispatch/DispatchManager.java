package net.charonus.modellers_dream.dispatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleEntry;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.trains.schedule.condition.ScheduledDelay;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;

/**
 * Owns every train's Modellers Dream dispatch queue and is the only place that
 * touches Train.runtime directly. The /dispatch command should just call into this.
 *
 * Two Create-runtime quirks drove this design (see ScheduleRuntime source):
 *
 *  - An entry needs at least one (possibly empty) condition column, or
 *    ScheduleRuntime.tickConditions() never advances currentEntry past it -
 *    the train arrives and just sits there forever. An empty column passes
 *    instantly, which is what we want (no artificial wait).
 *
 *  - ScheduleRuntime.setSchedule() calls reset(), which zeroes currentEntry.
 *    Calling it on a train that's already mid-schedule replays every leg it
 *    already completed. So we only ever call setSchedule() to start fresh
 *    (no schedule yet, or a previous one-shot that already completed);
 *    otherwise we append directly to the live Schedule's entry list, which
 *    Create leaves public and mutable for exactly this kind of use.
 */
public final class DispatchManager {

    private static final Map<UUID, List<String>> pendingStations = new HashMap<>();

    private DispatchManager() {}

    // ---------------------------------------------------------------
    // Lookup
    // ---------------------------------------------------------------

    public static List<Train> getAllTrains() {
        List<Train> trains = new ArrayList<>(Create.RAILWAYS.trains.values());
        trains.sort(Comparator.comparing(t -> t.name.getString()));
        return trains;
    }

    public static Train findTrainByName(String name) {
        return Create.RAILWAYS.trains.values()
                .stream()
                .filter(t -> t.name.getString().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static boolean hasConductor(Train train) {
        return train.hasForwardConductor() || train.hasBackwardConductor();
    }

    /** True if the train has a schedule that hasn't finished running yet - content doesn't matter. */
    public static boolean hasActiveSchedule(Train train) {
        ScheduleRuntime runtime = train.runtime;
        return runtime.schedule != null && !runtime.completed;
    }

    /** True if the conductor of the train has any schedule at all. */
    public static boolean hasSchedule(Train train) {
        return train.runtime.schedule != null;
    }

    // ---------------------------------------------------------------
    // Queue read + sync
    // ---------------------------------------------------------------

    /**
     * Drops stations from our queue that Create's runtime has already driven past.
     * Only meaningful for schedules we built (destination-only, non-cyclic, entries
     * 1:1 with our queue) - hand-crafted player schedules aren't tracked here.
     */
    private static void sync(Train train) {
        List<String> queue = pendingStations.get(train.id);
        if (queue == null)
            return;

        ScheduleRuntime runtime = train.runtime;
        if (runtime.schedule == null || runtime.completed) {
            pendingStations.remove(train.id);
            return;
        }

        // Determine how many entries have been passed relative to original queue size.
        // runtime.schedule.entries.size() contains total entries remaining + completed.
        int totalEntries = runtime.schedule.entries.size();
        int remainingEntries = totalEntries - runtime.currentEntry;

        // Adjust queue size to match remaining entries in Create's live schedule
        while (queue.size() > remainingEntries && !queue.isEmpty()) {
            queue.remove(0);
        }

        if (queue.isEmpty())
            pendingStations.remove(train.id);
    }

    public static List<String> getPendingStations(Train train) {
        sync(train);
        return pendingStations.getOrDefault(train.id, List.of());
    }

    // ---------------------------------------------------------------
    // Dispatch operations
    // ---------------------------------------------------------------

    /** Wipes any existing schedule and dispatches the train to a single station. */
    public static void setDestination(Train train, String stationName) {
        Schedule schedule = new Schedule(new ArrayList<>(List.of(buildEntry(stationName))), false, 0);
        train.runtime.setSchedule(schedule, false);

        List<String> queue = new ArrayList<>();
        queue.add(stationName);
        pendingStations.put(train.id, queue);
    }

    /**
     * Adds a station to the end of the train's run.
     * Falls back to setDestination() when there's nothing live to extend
     * (no schedule yet, or the previous one already completed) - extending a
     * finished schedule's entry list in place would replay its old stops,
     * since currentEntry gets zeroed once a non-cyclic schedule completes.
     */
    public static void appendDestination(Train train, String stationName) {
        if (!hasActiveSchedule(train)) {
            setDestination(train, stationName);
            return;
        }

        train.runtime.schedule.entries.add(buildEntry(stationName));
        train.runtime.predictionTicks.add(-1); // TBD sentinel - keeps predictionTicks in lockstep with entries,
        // or ScheduleRuntime.predictForEntry() index-out-of-bounds crashes
        // on the very next tick (this is what crashed your server)
        pendingStations.computeIfAbsent(train.id, id -> new ArrayList<>()).add(stationName);
    }

    public static void clear(Train train) {
        ScheduleRuntime runtime = train.runtime;
        if (runtime.schedule != null) {
            runtime.schedule.entries.clear();
            runtime.predictionTicks.clear();
            runtime.currentEntry = 0;
        }
        pendingStations.remove(train.id);
    }

    private static ScheduleEntry buildEntry(String stationName) {
        DestinationInstruction destination = new DestinationInstruction();
        destination.getData().putString("Text", stationName);

        ScheduledDelay delay = new ScheduledDelay();

        List<List<ScheduleWaitCondition>> conditions = new ArrayList<>();
        conditions.add(new ArrayList<>(List.of(delay))); // one empty column = passes instantly, no artificial wait
        return new ScheduleEntry(destination, conditions);
    }
}

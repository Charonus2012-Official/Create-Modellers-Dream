package net.charonus.modellers_dream.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleEntry;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * MVP test command for "Create: Modellers Dream".
 *
 * Usage:
 *   /dispatch list
 *   /dispatch <trainName> <stationName>
 *
 * This proves out the whole dispatch chain end-to-end:
 *   1. find a Train by its in-game name
 *   2. confirm it has a conductor (required for automated schedules)
 *   3. build a one-entry Schedule targeting a station name
 *   4. hand it to Train.runtime -> Create's own pathfinding/signals take over
 *
 * Register this in your mod's RegisterCommandsEvent listener, e.g.:
 *   DispatchTestCommand.register(event.getDispatcher());
 */
public class DispatchTestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dispatch")
                        .requires(source -> source.hasPermission(2)) // op only, this is a debug command
                        .then(Commands.literal("list")
                                .executes(DispatchTestCommand::listTrains))
                        .then(Commands.argument("trainName", StringArgumentType.string())
                                .then(Commands.argument("stationName", StringArgumentType.string())
                                        .executes(DispatchTestCommand::dispatchTrain)))
        );
    }

    private static int listTrains(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        List<Train> trains = new ArrayList<>(Create.RAILWAYS.trains.values());
        trains.sort(Comparator.comparing(t -> t.name.getString()));

        if (trains.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No trains currently exist in this world."), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Trains (" + trains.size() + "):"), false);
        for (Train train : trains) {
            boolean hasConductor = train.hasForwardConductor() || train.hasBackwardConductor();
            String status = train.runtime.schedule != null ? "scheduled" : "idle";
            source.sendSuccess(() -> Component.literal(" - " + train.name.getString()
                    + "  [conductor: " + hasConductor + "]"
                    + "  [" + status + "]"), false);
        }
        return trains.size();
    }

    private static int dispatchTrain(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String trainName = StringArgumentType.getString(ctx, "trainName");
        String stationName = StringArgumentType.getString(ctx, "stationName");

        // 1. Find the train by name (case-insensitive exact match for the test command;
        //    swap for UUID lookup once you have a real selection UI)
        Train train = Create.RAILWAYS.trains.values()
                .stream()
                .filter(t -> t.name.getString().equalsIgnoreCase(trainName))
                .findFirst()
                .orElse(null);

        if (train == null) {
            source.sendFailure(Component.literal("No train named '" + trainName + "' found. Try /dispatch list"));
            return 0;
        }

        // 2. A train needs a conductor seated to run an automated schedule at all -
        //    Create's DestinationInstruction silently fails without one.
        if (!train.hasForwardConductor() && !train.hasBackwardConductor()) {
            source.sendFailure(Component.literal(
                    "Train '" + trainName + "' has no conductor seated - it can't run an automatic schedule. "
                            + "Place a conductor (villager/mob in the driver's seat) first."));
            return 0;
        }

        // 3. Build a one-entry, non-cyclic schedule targeting the destination station.
        //    The DestinationInstruction "Text" field is matched as a glob/regex against
        //    GlobalStation.name, so exact station names work directly.
        DestinationInstruction destination = new DestinationInstruction();
        destination.getData().putString("Text", stationName);

        ScheduleEntry entry = new ScheduleEntry(destination, new ArrayList<>()); // no wait conditions - go immediately
        Schedule schedule = new Schedule(List.of(entry), false, 0); // cyclic=false: one-shot dispatch

        // 4. Hand control to Create's own schedule runtime. From this point on Create
        //    handles pathfinding, movement, and signal compliance every tick - we don't
        //    touch any of that.
        train.runtime.setSchedule(schedule, false);

        source.sendSuccess(() -> Component.literal(
                "Dispatched '" + trainName + "' -> '" + stationName + "'."), true);
        return 1;
    }
}

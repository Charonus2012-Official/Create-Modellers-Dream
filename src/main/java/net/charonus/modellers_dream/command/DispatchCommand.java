package net.charonus.modellers_dream.command;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import com.simibubi.create.content.trains.entity.Train;

import net.charonus.modellers_dream.dispatch.DispatchManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * /dispatch list [<trainName>]   - all trains, or one train's pending stations
 * /dispatch set <trainName> <stationName>    - replace the train's run with one stop
 * /dispatch append <trainName> <stationName> - queue another stop after the current run
 *
 * All actual schedule manipulation lives in DispatchManager; this class is just
 * argument parsing and player-facing messages.
 */
public class DispatchCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dispatch")
                        .requires(source -> source.hasPermission(2)) // op only, this is a debug command
                        .then(Commands.literal("list")
                                .executes(DispatchCommand::listAllTrains)
                                .then(Commands.argument("trainName", StringArgumentType.string())
                                        .executes(DispatchCommand::listStations)))
                        .then(Commands.literal("set")
                                .then(Commands.argument("trainName", StringArgumentType.string())
                                        .then(Commands.argument("stationName", StringArgumentType.string())
                                                .executes(ctx -> dispatchAction(ctx, DispatchManager::setDestination)))))
                        .then(Commands.literal("append")
                                .then(Commands.argument("trainName", StringArgumentType.string())
                                        .then(Commands.argument("stationName", StringArgumentType.string())
                                                .executes(ctx -> dispatchAction(ctx, DispatchManager::appendDestination)))))

                        .then(Commands.literal("clear")
                                .then(Commands.argument("trainName", StringArgumentType.string())
                                        .executes(DispatchCommand::clearSchedule)))
        );
    }

    private static int listAllTrains(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        List<Train> trains = DispatchManager.getAllTrains();

        if (trains.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No trains currently exist in this world."), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Trains (" + trains.size() + "):"), false);
        for (Train train : trains) {
            boolean hasConductor = DispatchManager.hasConductor(train);
            boolean active = DispatchManager.hasActiveSchedule(train);
            source.sendSuccess(() -> Component.literal(" - " + train.name.getString()
                    + "  [conductor: " + hasConductor + "]"
                    + "  [" + (active ? "scheduled" : "idle") + "]"), false);
        }
        return trains.size();
    }

    private static int listStations(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String trainName = StringArgumentType.getString(ctx, "trainName");

        Train train = DispatchManager.findTrainByName(trainName);
        if (train == null) {
            source.sendFailure(Component.literal("No train named '" + trainName + "' found. Try /dispatch list"));
            return 0;
        }

        List<String> stations = DispatchManager.getPendingStations(train);
        if (stations.isEmpty()) {
            source.sendSuccess(() -> Component.literal("'" + trainName + "' has no stations queued."), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("'" + trainName + "' queue (" + stations.size() + "):"), false);
        for (int i = 0; i < stations.size(); i++) {
            int index = i + 1;
            String station = stations.get(i);
            source.sendSuccess(() -> Component.literal(" " + index + ". " + station), false);
        }
        return stations.size();
    }

    private interface DispatchOp {
        void apply(Train train, String stationName);
    }

    private static int dispatchAction(CommandContext<CommandSourceStack> ctx, DispatchOp op) {
        CommandSourceStack source = ctx.getSource();
        String trainName = StringArgumentType.getString(ctx, "trainName");
        String stationName = StringArgumentType.getString(ctx, "stationName");

        Train train = DispatchManager.findTrainByName(trainName);
        if (train == null) {
            source.sendFailure(Component.literal("No train named '" + trainName + "' found. Try /dispatch list"));
            return 0;
        }


        if (!DispatchManager.hasConductor(train)) {
            source.sendFailure(Component.literal(
                    "Train '" + trainName + "' has no conductor seated - it can't run an automatic schedule. "
                            + "Place a conductor (villager/mob in the driver's seat) first."));
            return 0;
        }

        if (!DispatchManager.hasSchedule(train)) {
            source.sendFailure(Component.literal(
                    "Cannot dispatch train that doesn't have a train schedule, give the conductor a train schedule to proceed"
            ));
            return 0;
        }

        op.apply(train, stationName);

        source.sendSuccess(() -> Component.literal(
                "'" + trainName + "' -> '" + stationName + "'."), true);
        return 1;
    }

    private static int clearSchedule(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String trainName = StringArgumentType.getString(ctx, "trainName");

        Train train = DispatchManager.findTrainByName(trainName);

        if (train == null) {
            source.sendFailure(Component.literal("No train named '" + trainName + "' found. Try /dispatch list"));
            return 0;
        }

        if (!DispatchManager.hasActiveSchedule(train)) {
            source.sendFailure(Component.literal("Train has no active route"));
            return 0;
        }

        DispatchManager.clear(train);
        source.sendSuccess(() -> Component.literal("Cleared schedule of '" + trainName + "'"), true);

        return 1;
    }
}

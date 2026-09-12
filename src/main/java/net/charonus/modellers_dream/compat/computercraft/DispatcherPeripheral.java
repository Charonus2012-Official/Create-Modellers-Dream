package net.charonus.modellers_dream.compat.computercraft;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.charonus.modellers_dream.block.entity.DispatcherTable.DispatcherTableBlockEntity;
import net.charonus.modellers_dream.dispatch.DispatchManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DispatcherPeripheral implements IPeripheral {
    private final DispatcherTableBlockEntity dispatcher;

    public DispatcherPeripheral(DispatcherTableBlockEntity dispatcher) {
        this.dispatcher = dispatcher;
        if (dispatcher.getLevel() != null && dispatcher.getLevel().isClientSide) {
            System.out.println("WARNING: Peripheral created on client side!");
        }
    }

    @Override
    public @NotNull String getType() {
        return "dispatcher";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof DispatcherPeripheral otherPeripheral
                && otherPeripheral.dispatcher == this.dispatcher;
    }

    @LuaFunction(mainThread = true)
    public final List<String> listTrains() {
        dispatcher.logLinkedNetwork();
        var network = dispatcher.queryLinkedNetwork();

        if (network == null) {
            System.out.println("Network is null!");
            return List.of();
        }
        return network.trains.stream()
                .map(train -> train.name)
                .toList();
    }

    @LuaFunction(mainThread = true)
    public final List<String> listStations() {
        dispatcher.logLinkedNetwork();
        var network = dispatcher.queryLinkedNetwork();

        if (network == null) {
            System.out.println("Network is null!");
            return List.of();
        }

        return network.stations.stream()
                .map(station -> station.name)
                .toList();
    }

    @LuaFunction(mainThread = true)
    public final boolean appendRoute(String trainName, String stationName) {
        dispatcher.logLinkedNetwork();
        var network = dispatcher.queryLinkedNetwork();

        if (network == null) {
            System.out.println("Network is null!");
            return false;
        }

        var train = network.trains.stream()
                .filter(t -> t.name.equals(trainName))
                .findFirst()
                .orElse(null);

        var station = network.stations.stream()
                .filter(s -> s.name.equals(stationName))
                .findFirst()
                .orElse(null);

        if (train == null || station == null) {
            System.out.println("Train or station not found!");
            return false;
        }
        if (!DispatchManager.hasSchedule(train.train)) {
            return false;
        }

        DispatchManager.appendDestination(train.train, station.name);

        return true;
    }


    @LuaFunction(mainThread = true)
    public final boolean directRoute(String trainName, String stationName) {
        dispatcher.logLinkedNetwork();
        var network = dispatcher.queryLinkedNetwork();

        if (network == null) {
            System.out.println("Network is null!");
            return false;
        }

        var train = network.trains.stream()
                .filter(t -> t.name.equals(trainName))
                .findFirst()
                .orElse(null);

        var station = network.stations.stream()
                .filter(s -> s.name.equals(stationName))
                .findFirst()
                .orElse(null);

        if (train == null || station == null) {
            System.out.println("Train or station not found!");
            return false;
        }
        if (!DispatchManager.hasSchedule(train.train)) {
            return false;
        }

        DispatchManager.setDestination(train.train, station.name);

        return true;
    }

    @LuaFunction(mainThread = true)
    public final boolean clearSchedule(String trainName) {
        dispatcher.logLinkedNetwork();
        var network = dispatcher.queryLinkedNetwork();

        if (network == null) {
            System.out.println("Network is null!");
            return false;
        }

        var train = network.trains.stream()
                .filter(t -> t.name.equals(trainName))
                .findFirst()
                .orElse(null);

        if (train == null) {
            return false;
        }

        if (!DispatchManager.hasConductor(train.train) || !DispatchManager.hasSchedule(train.train)) {
            return false;
        }

        DispatchManager.clear(train.train);

        return true;
    }

    @LuaFunction(mainThread = true)
    public final List<String> listPendingStations(String trainName) {
        dispatcher.logLinkedNetwork();
        var network = dispatcher.queryLinkedNetwork();

        if (network == null) {
            System.out.println("Network is null!");
            return List.of();
        }

        var train = network.trains.stream()
                .filter(t -> t.name.equals(trainName))
                .findFirst()
                .orElse(null);

        if (train == null) {
            return List.of();
        }

        if (!DispatchManager.hasConductor(train.train) || !DispatchManager.hasSchedule(train.train)) {
            return List.of();
        }

        return DispatchManager.getPendingStations(train.train);
    }
}

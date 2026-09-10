package net.charonus.modellers_dream.block.entity.TrackConnector;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.graph.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.track.ITrackBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TrackConnectorQuery {

    public static class TrainData {
        public final String name;
        public final Train train;

        public TrainData(String name, Train train) {
            this.name = name;
            this.train = train;
        }
    }

    public static class StationData {
        public final String name;
        public final GlobalStation station;

        public StationData(String name, GlobalStation station) {
            this.name = name;
            this.station = station;
        }
    }

    public static class QueryResult {
        public final UUID graphId;
        public final List<TrainData> trains;
        public final List<StationData> stations;
        public final boolean isValid;

        public QueryResult(UUID graphId, List<TrainData> trains, List<StationData> stations, boolean isValid) {
            this.graphId = graphId;
            this.trains = trains;
            this.stations = stations;
            this.isValid = isValid;
        }

        public static QueryResult empty() {
            return new QueryResult(null, Collections.emptyList(), Collections.emptyList(), false);
        }
    }

    /**
     * Resolve the TrackGraph that the connector's target track currently belongs to.
     * Never cached - Create's graphs merge/split live, so this re-resolves every call.
     *
     * Real API confirmed against Create mc1.21.1/dev source:
     * - Create.RAILWAYS.sided(level).getGraph(level, TrackNodeLocation) is the only lookup,
     *   it does NOT accept a BlockPos directly.
     * - A normal rail segment is not necessarily a stored graph node. Create compresses straight
     *   sections, so we must walk connected track locations until reaching one of its graph nodes.
     *   This mirrors TrackPropagator's graph discovery traversal.
     */
    public static TrackGraph resolveGraph(Level level, BlockPos trackPos) {
        BlockState state = level.getBlockState(trackPos);
        if (!(state.getBlock() instanceof ITrackBlock track)) {
            return null;
        }

        ArrayDeque<TrackNodeLocation> frontier = new ArrayDeque<>();
        Set<TrackNodeLocation> visited = new HashSet<>();
        for (DiscoveredLocation initial : track.getConnected(level, trackPos, state, false, null)) {
            if (visited.add(initial)) {
                frontier.add(initial);
            }
        }

        // Create uses the same limit while propagating graph changes, preventing a malformed
        // track layout from making a GUI open scan an unbounded network.
        int remainingSearches = 1000;
        while (!frontier.isEmpty() && remainingSearches-- > 0) {
            TrackNodeLocation location = frontier.removeFirst();
            TrackGraph graph = Create.RAILWAYS.sided(level).getGraph(level, location);
            if (graph != null) {
                return graph;
            }

            for (DiscoveredLocation next : ITrackBlock.walkConnectedTracks(level, location, false)) {
                if (visited.add(next)) {
                    frontier.addLast(next);
                }
            }
        }
        return null;
    }

    /**
     * Query trains and stations reachable from a Track Connector's target track.
     */
    public static QueryResult query(TrackConnectorBlockEntity connector, Level level) {
        if (connector == null || connector.getTargetTrack() == null) {
            return QueryResult.empty();
        }

        TrackGraph graph = resolveGraph(level, connector.getTargetTrack());
        if (graph == null) {
            return QueryResult.empty();
        }

        List<TrainData> trains = queryTrainsOnGraph(level, graph.id);
        List<StationData> stations = queryStationsOnGraph(graph);

        return new QueryResult(graph.id, trains, stations, true);
    }

    /**
     * All trains currently on a specific graph ID.
     * Trains are stored directly on GlobalRailwayManager.trains (Map<UUID, Train>),
     * each Train has a `graph` field with its current TrackGraph.
     */
    public static List<TrainData> queryTrainsOnGraph(Level level, UUID graphId) {
        return Create.RAILWAYS.sided(level).trains.values().stream()
                .filter(train -> train.graph != null && train.graph.id.equals(graphId))
                .map(train -> new TrainData(train.name.getString(), train))
                .collect(Collectors.toList());
    }

    /**
     * All stations on a specific graph.
     * graph.getPoints(EdgePointType<T>) returns Collection<T> directly - no casting needed.
     * GlobalStation.name is a plain public String field.
     */
    public static List<StationData> queryStationsOnGraph(TrackGraph graph) {
        return graph.getPoints(EdgePointType.STATION).stream()
                .map(station -> new StationData(station.name, station))
                .collect(Collectors.toList());
    }
}

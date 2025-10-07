// TrainGraph.java
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Weighted train graph using full TrainConnection.
 * - Vertices: cities
 * - Edges: TrainConnection between departure city to arrival city
 * - Weight: tripDuration from departure city to arrival city
 *
 * Path totalDuration is computed using a real timeline:
 *   - start at first edge's departure time (day 0)
 *   - for each next edge, wait until its next available departure time
 *     (same-day or rolled to a future day if needed), then add its tripDuration
 *   - total = finalArrival - firstDeparture
 */
public class TrainGraph {

    // Adjacency: departure -> (arrival -> list of TrainConnection edges)
    private final Map<String, Map<String, List<TrainConnection>>> graph = new HashMap<>();

    public TrainGraph(List<TrainConnection> connections) {
        for (TrainConnection tc : connections) {
            addEdge(tc);
        }
    }

    /** Add a single directed edge */
    private void addEdge(TrainConnection tc) {
        graph.computeIfAbsent(tc.departureCity, k -> new HashMap<>())
             .computeIfAbsent(tc.arrivalCity, k -> new ArrayList<>())
             .add(tc);
    }

    /** All cities that appear as departure or arrival. */
    public Set<String> getAllCities() {
        Set<String> cities = new HashSet<>(graph.keySet());
        for (Map<String, List<TrainConnection>> m : graph.values()) {
            cities.addAll(m.keySet());
        }
        return cities;
    }

    /** Direct connections list from -> to (may be empty). */
    public List<TrainConnection> getConnections(String from, String to) {
        Map<String, List<TrainConnection>> m = graph.get(from);
        if (m == null) return Collections.emptyList();
        List<TrainConnection> lst = m.get(to);
        return (lst == null) ? Collections.emptyList() : Collections.unmodifiableList(lst);
    }

    /** All outgoing connections from a city. */
    public List<TrainConnection> getConnectionsFrom(String from) {
        Map<String, List<TrainConnection>> m = graph.get(from);
        if (m == null) return Collections.emptyList();
        List<TrainConnection> all = new ArrayList<>();
        for (List<TrainConnection> lst : m.values()) all.addAll(lst);
        return Collections.unmodifiableList(all);
    }

    /** Is there at least one direct edge from -> to? */
    public boolean hasDirect(String from, String to) {
        Map<String, List<TrainConnection>> m = graph.get(from);
        return m != null && m.containsKey(to) && !m.get(to).isEmpty();
    }

    /** Count distinct directed edges. */
    public int edgeCount() {
        int sum = 0;
        for (Map<String, List<TrainConnection>> m : graph.values()) {
            for (List<TrainConnection> lst : m.values()) sum += lst.size();
        }
        return sum;
    }

    // ---------- Path enumeration (≤ 2 intermediates => ≤ 3 edges) ----------

    /** Path record with edges and total duration precomputed (with realistic waits). */
    public static class PathResult {
        public final String from;
        public final String to;
        public final List<TrainConnection> edges;
        public final Duration totalDuration;

        public PathResult(String from, String to, List<TrainConnection> edges) {
            this.from = from;
            this.to = to;
            this.edges = Collections.unmodifiableList(new ArrayList<>(edges));
            this.totalDuration = computeTotalWithWaits(this.edges);
        }

        /** # of intermediate cities = edges - 1 (0..2) */
        public int intermediates() { return Math.max(0, edges.size() - 1); }

        private static Duration computeTotalWithWaits(List<TrainConnection> edges) {
            if (edges.isEmpty()) return Duration.ZERO;

            long startAbs = minutesOfDay(edges.get(0).departureTime);
            long curAbs = startAbs;

            for (TrainConnection e : edges) {
                int depMin = minutesOfDay(e.departureTime);
                long depAbs = alignToNextOrSame(curAbs, depMin);
                long travel = e.tripDuration.toMinutes();
                curAbs = depAbs + travel;
            }
            long totalMins = Math.max(0, curAbs - startAbs);
            return Duration.ofMinutes(totalMins);
        }

        private static int minutesOfDay(LocalTime t) {
            return t.getHour() * 60 + t.getMinute();
        }

        private static long alignToNextOrSame(long currentAbs, int targetMinOfDay) {
            long day = currentAbs / 1440; // minutes in a day
            long candidate = day * 1440 + targetMinOfDay;
            while (candidate < currentAbs) candidate += 1440;
            return candidate;
        }

        @Override
        public String toString() {
            String hops = edges.stream()
                    .map((TrainConnection e) -> {
                        long mins = e.tripDuration.toMinutes();
                        long h = mins / 60, m = mins % 60;
                        return String.format(
                                "%s→%s[%s %s→%s %dh%02dm €%d/€%d]",
                                e.departureCity, e.arrivalCity,
                                e.getRouteID(), e.departureTime, e.arrivalTime,
                                h, m,
                                e.firstClassRate, e.secondClassRate);
                    })
                    .collect(Collectors.joining("  "));

            long totalMins = totalDuration.toMinutes();
            long th = totalMins / 60, tm = totalMins % 60;

            int totalFirst = 0, totalSecond = 0;
            for (TrainConnection e : edges) {
                totalFirst += e.firstClassRate;
                totalSecond += e.secondClassRate;
            }

            return String.format(
                    "%s ⇒ %s | edges=%d, intermed=%d, total=%dh %02dm | total €%d/€%d | %s",
                    from, to, edges.size(), intermediates(), th, tm, totalFirst, totalSecond, hops);
        }
    }

    /** Enumerate ALL simple paths between ALL pairs (from -> to) with ≤ 3 edges. */
    public Map<String, Map<String, List<PathResult>>> allPathsUpToTwoIntermediates() {
        final int MAX_EDGES = 3;
        Map<String, Map<String, List<PathResult>>> result = new HashMap<>();
        for (String source : getAllCities()) {
            Deque<TrainConnection> path = new ArrayDeque<>();
            Set<String> visited = new HashSet<>();
            visited.add(source);
            dfsCollect(source, source, MAX_EDGES, visited, path, result);
        }
        return result;
    }

    /** Enumerate ALL simple paths (1–3 edges) that satisfy a given Filter. */
    public Map<String, Map<String, List<PathResult>>> allPathsUpToTwoIntermediates(
            Predicate<TrainConnection> edgeFilter) {
        final int MAX_EDGES = 3;
        Map<String, Map<String, List<PathResult>>> result = new HashMap<>();
        for (String source : getAllCities()) {
            Deque<TrainConnection> path = new ArrayDeque<>();
            Set<String> visited = new HashSet<>();
            visited.add(source);
            dfsCollectFiltered(source, source, MAX_EDGES, visited, path, edgeFilter, result);
        }
        return result;
    }

    /** Paths between specific cities (1–3 edges) that satisfy a given Filter. */
    public List<PathResult> pathsUpToTwoIntermediates(String from, String to, Predicate<TrainConnection> edgeFilter, Set<String> depDays, Set<String> typesTrain) {
        final int MAX_EDGES = 3;
        List<PathResult> out = new ArrayList<>();
        Deque<TrainConnection> path = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        visited.add(from);

        Map<String, Map<String, List<PathResult>>> sink = new HashMap<>();
        dfsCollectFiltered(from, from, MAX_EDGES, visited, path, edgeFilter, sink);

        Map<String, List<PathResult>> m = sink.get(from);
        if (m != null) {
            List<PathResult> lst = m.get(to);
            if (lst != null) out.addAll(lst);
        }
        
        return this.filterPathsUpToTwoIntermediates(out, depDays, typesTrain);
    }

    private List<PathResult> filterPathsUpToTwoIntermediates(List<PathResult> toFilter, Set<String> depDays, Set<String> typesTrain){
        List<PathResult> out = new ArrayList<>();
        for (PathResult result : toFilter){
            List<TrainConnection> egdesList = result.edges;
            TrainConnection tc = egdesList.get(0);
            String s = tc.daysOfOperation;
            String [] days = s.split(",");
            boolean goodDay = false;
            boolean goodType = true;
            if(s.equals("Daily") || depDays.isEmpty()){
                goodDay = true;
            }
            else{
                for(String day: days){
                    if(depDays.contains(day)){
                        goodDay =true;
                        break;
                    }
                }
            }
            for(TrainConnection tempTC : egdesList){
                String tempType = tempTC.trainType;
                if(!typesTrain.contains(tempType)){
                    goodType=false;
                }  
            }
            if(typesTrain.isEmpty()){
                goodType = true;
            }
            if(goodType && goodDay){
                out.add(result);
            }
        }
        return out;
    }

    private void dfsCollectFiltered(String origin,
                                    String currentCity,
                                    int edgesRemaining,
                                    Set<String> visitedCities,
                                    Deque<TrainConnection> path,
                                    Predicate<TrainConnection> edgeFilter,
                                    Map<String, Map<String, List<PathResult>>> sink) {
        if (edgesRemaining == 0) return;

        Map<String, List<TrainConnection>> adj = graph.get(currentCity);
        if (adj == null) return;

        for (Map.Entry<String, List<TrainConnection>> e : adj.entrySet()) {
            String nextCity = e.getKey();
            if (visitedCities.contains(nextCity)) continue;

            for (TrainConnection edge : e.getValue()) {
                if (!edgeFilter.test(edge)) continue;

                path.addLast(edge);
                visitedCities.add(nextCity);

                sink.computeIfAbsent(origin, k -> new HashMap<>())
                    .computeIfAbsent(nextCity, k -> new ArrayList<>())
                    .add(new PathResult(origin, nextCity, new ArrayList<>(path)));

                dfsCollectFiltered(origin, nextCity, edgesRemaining - 1, visitedCities, path, edgeFilter, sink);

                visitedCities.remove(nextCity);
                path.removeLast();
            }
        }
    }

    private void dfsCollect(String origin,
                            String currentCity,
                            int edgesRemaining,
                            Set<String> visitedCities,
                            Deque<TrainConnection> path,
                            Map<String, Map<String, List<PathResult>>> sink) {
        if (edgesRemaining == 0) return;

        Map<String, List<TrainConnection>> m = graph.get(currentCity);
        if (m == null) return;

        for (Map.Entry<String, List<TrainConnection>> entry : m.entrySet()) {
            String nextCity = entry.getKey();
            if (visitedCities.contains(nextCity)) continue;

            for (TrainConnection edge : entry.getValue()) {
                path.addLast(edge);
                visitedCities.add(nextCity);

                sink.computeIfAbsent(origin, k -> new HashMap<>())
                    .computeIfAbsent(nextCity, k -> new ArrayList<>())
                    .add(new PathResult(origin, nextCity, new ArrayList<>(path)));

                dfsCollect(origin, nextCity, edgesRemaining - 1, visitedCities, path, sink);

                visitedCities.remove(nextCity);
                path.removeLast();
            }
        }
    }

    /** Best (shortest duration) path per pair within the same bound. */
    public Map<String, Map<String, PathResult>> fastestPathPerPairUpToTwoIntermediates() {
        Map<String, Map<String, List<PathResult>>> all = allPathsUpToTwoIntermediates();
        Map<String, Map<String, PathResult>> best = new HashMap<>();
        for (Map.Entry<String, Map<String, List<PathResult>>> eFrom : all.entrySet()) {
            String from = eFrom.getKey();
            for (Map.Entry<String, List<PathResult>> eTo : eFrom.getValue().entrySet()) {
                String to = eTo.getKey();
                PathResult min = eTo.getValue().stream()
                        .min(Comparator.comparing(p -> p.totalDuration))
                        .orElse(null);
                if (min != null) {
                    best.computeIfAbsent(from, k -> new HashMap<>()).put(to, min);
                }
            }
        }
        return best;
    }

    // ---------- String rendering ----------

    private int cityCount() { return getAllCities().size(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("TrainGraph: %d cities, %d direct connections%n",
                cityCount(), edgeCount()));

        for (Map.Entry<String, Map<String, List<TrainConnection>>> fromEntry : graph.entrySet()) {
            for (Map.Entry<String, List<TrainConnection>> toEntry : fromEntry.getValue().entrySet()) {
                for (TrainConnection tc : toEntry.getValue()) {
                    long mins = tc.tripDuration.toMinutes();
                    long h = mins / 60, m = mins % 60;
                    sb.append(String.format(
                            "%s -> %s | %s | Days:%s | %dh %02dm | €%d/€%d | route=%s%n",
                            tc.departureCity,
                            tc.arrivalCity,
                            tc.trainType,
                            tc.daysOfOperation,
                            h, m,
                            tc.firstClassRate, tc.secondClassRate,
                            tc.getRouteID()));
                }
            }
        }
        return sb.toString();
    }
}

import java.util.List;
import java.util.function.Predicate;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class driver {
    public static void main(String[] args) {
        // 1) Load CSV (pass a path as arg0, or it defaults to this file)
        String csv = (args.length > 0) ? args[0] : "eu_rail_network.csv";
        TrainConnection.loadTrainConnectionsFromCSV(csv);

        // Build the graph from all connections
        TrainGraph g = new TrainGraph(TrainConnection.trainConnections); // directed

        // Print the whole graph (direct connections only)
        System.out.println(g);

        // All paths Paris -> London (≤ 2 intermediates, i.e., up to 3 edges)
        List<TrainGraph.PathResult> allPaths = g.pathsUpToTwoIntermediates("Paris", "London", e -> true);
        System.out.printf("%nAll paths Paris -> London (≤2 intermediates): %d%n", allPaths.size());
        allPaths.forEach(System.out::println);

        // Example filtered search based on other params:
        // Weekend only, train type RJX, first-class ≤ 120€, depart after 12:00
        Predicate<TrainConnection> weekend = DayFilters.runsOnAny(DayFilters.set(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        Predicate<TrainConnection> onlyRJX = e -> "RJX".equalsIgnoreCase(e.trainType);
        Predicate<TrainConnection> cheapFirst = e -> e.firstClassRate <= 120;
        Predicate<TrainConnection> afternoon = e -> e.departureTime.isAfter(LocalTime.NOON);

        Predicate<TrainConnection> filter = weekend.and(onlyRJX).and(cheapFirst).and(afternoon);

        List<TrainGraph.PathResult> filtered = g.pathsUpToTwoIntermediates("Paris", "London", filter);
        System.out.printf("%nFiltered paths Paris -> London (weekend, RJX, 1st<=120€, depart after noon): %d%n",
                filtered.size());
        filtered.forEach(System.out::println);

        // examples on how to use the predicate for searshes
        // Weekend only (uses DayFilters from earlier)
        weekend = DayFilters.runsOnAny(DayFilters.set(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        // Train type: exactly RJX
        Predicate<TrainConnection> rjxOnly = e -> "RJX".equalsIgnoreCase(e.trainType);

        // First-class price cap
        Predicate<TrainConnection> firstClassAtMost = e -> e.firstClassRate <= 120;

        // Depart after noon
        Predicate<TrainConnection> departAfterNoon = e -> e.departureTime.isAfter(LocalTime.NOON);

        // Combine (all must hold)
        Predicate<TrainConnection> weekendRjxCheapAfterNoon = weekend.and(rjxOnly).and(firstClassAtMost)
                .and(departAfterNoon);

        // OR example: allow multiple train types
        Predicate<TrainConnection> typeIn = e -> {
            String t = e.trainType.toLowerCase();
            return t.equals("rjx") || t.equals("tgv") || t.equals("ice");
        };

        // Time window: depart between 08:00 and 12:00 inclusive
        LocalTime t1 = LocalTime.of(8, 0), t2 = LocalTime.of(12, 0);
        Predicate<TrainConnection> departBetweenMorning = e -> !e.departureTime.isBefore(t1)
                && !e.departureTime.isAfter(t2);

        // Second-class price cap
        Predicate<TrainConnection> secondClassAtMost60 = e -> e.secondClassRate <= 60;

        // Exclude a type (e.g., Nightjet)
        Predicate<TrainConnection> notNightjet = e -> !"nightjet".equalsIgnoreCase(e.trainType);

        // All paths Paris -> London with the combined filter (≤2 intermediates)
        filtered = g.pathsUpToTwoIntermediates("Paris", "London", weekendRjxCheapAfterNoon);
        filtered.forEach(System.out::println);
    }
}

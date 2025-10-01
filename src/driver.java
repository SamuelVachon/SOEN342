import java.util.List;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

public class driver {
    public static void main(String[] args) {
        // 1) Load CSV
        String csv = (args.length > 0) ? args[0] : "eu_rail_network.csv";
        TrainConnection.loadTrainConnectionsFromCSV(csv);

        // 2) Build graph from all loaded connections
        TrainGraph g = new TrainGraph(TrainConnection.trainConnections); // directed

        // 3) Print the whole graph (direct connections only)
        System.out.println(g);

        // 4) All paths Paris -> London (≤ 2 intermediates)
        List<TrainGraph.PathResult> allPaths = g.pathsUpToTwoIntermediates("Paris", "London", e -> true);
        System.out.printf("%nAll paths Paris -> London (≤2 intermediates): %d%n", allPaths.size());
        allPaths.forEach(System.out::println);

        // 5) Example filtered search:
        // Weekend only + train type RJX + first-class ≤ 120€ + depart after 12:00
        Predicate<TrainConnection> weekend = e -> {
            EnumSet<DayOfWeek> ops = DayFilters.parse(e.daysOfOperation);
            return ops.contains(DayOfWeek.SATURDAY) || ops.contains(DayOfWeek.SUNDAY);
        };
        Predicate<TrainConnection> onlyRJX = e -> e.trainType != null && e.trainType.equalsIgnoreCase("RJX");
        Predicate<TrainConnection> firstClassAtMost120 = e -> e.firstClassRate <= 120;
        Predicate<TrainConnection> departAfterNoon = e -> e.departureTime != null
                && e.departureTime.isAfter(LocalTime.NOON);

        Predicate<TrainConnection> filter = weekend.and(onlyRJX).and(firstClassAtMost120).and(departAfterNoon);

        List<TrainGraph.PathResult> filtered = g.pathsUpToTwoIntermediates("Paris", "London", filter);

        System.out.printf("%nFiltered paths Paris -> London "
                + "(weekend, RJX, 1st<=120€, depart>12:00): %d%n",
                filtered.size());
        filtered.forEach(System.out::println);

        // 1) Max total duration (e.g., ≤ 6h)
        Duration maxDur = Duration.ofHours(6);
        List<TrainGraph.PathResult> fastOnly = allPaths.stream()
                .filter(p -> p.totalDuration.compareTo(maxDur) <= 0)
                .collect(Collectors.toList());
        System.out.println("\nPaths with total duration ≤ 6h: " + fastOnly.size());
        fastOnly.stream().limit(5).forEach(System.out::println);

        // 2) Duration within a range (e.g., 3h–8h inclusive)
        Duration minDur = Duration.ofHours(3);
        Duration maxDur2 = Duration.ofHours(8);
        List<TrainGraph.PathResult> midRange = allPaths.stream()
                .filter(p -> p.totalDuration.compareTo(minDur) >= 0
                        && p.totalDuration.compareTo(maxDur2) <= 0)
                .collect(Collectors.toList());
        System.out.println("\nPaths with total duration in [3h, 8h]: " + midRange.size());
        midRange.stream().limit(5).forEach(System.out::println);

        // 3) Cap on total SECOND-class cost (e.g., ≤ €150)
        int secondCap = 150;
        List<TrainGraph.PathResult> cheapSecond = allPaths.stream()
                .filter(p -> totalSecondCost(p) <= secondCap)
                .collect(Collectors.toList());
        System.out.println("\nPaths with total second-class cost ≤ €150: " + cheapSecond.size());
        cheapSecond.stream().limit(5).forEach(System.out::println);

        // 4) Cap on total FIRST-class cost (e.g., ≤ €250)
        int firstCap = 250;
        List<TrainGraph.PathResult> cheapFirst = allPaths.stream()
                .filter(p -> totalFirstCost(p) <= firstCap)
                .collect(Collectors.toList());
        System.out.println("\nPaths with total first-class cost ≤ €250: " + cheapFirst.size());
        cheapFirst.stream().limit(5).forEach(System.out::println);

        // 5) Top-K cheapest by SECOND-class cost (sorted)
        int K = 5;
        List<TrainGraph.PathResult> topKSecond = allPaths.stream()
                .sorted(Comparator.comparingInt(driver::totalSecondCost))
                .limit(K)
                .collect(Collectors.toList());
        System.out.println("\nTop " + K + " cheapest (second-class total):");
        topKSecond.forEach(p -> System.out.printf("€%d | %s%n", totalSecondCost(p), p));

        // 6) Cheapest SECOND-class, tie-break by duration
        List<TrainGraph.PathResult> cheapestBySecondThenTime = allPaths.stream()
                .sorted(Comparator
                        .comparingInt(driver::totalSecondCost)
                        .thenComparing(p -> p.totalDuration)) // shorter wins on ties
                .limit(5)
                .collect(Collectors.toList());
        System.out.println("\nCheapest by second-class, tie-break by duration:");
        cheapestBySecondThenTime.forEach(p -> System.out.printf("€%d | %dh%02dm | %s%n",
                totalSecondCost(p),
                p.totalDuration.toHours(),
                p.totalDuration.toMinutesPart(),
                p));

        // 7) Combine edge-level filter + path-level constraint:
        // Example: Weekend RJX only (edge-level) AND total second-class ≤ €150
        // (path-level).
        java.util.function.Predicate<TrainConnection> weekendRJX = e -> {
            java.util.EnumSet<java.time.DayOfWeek> ops = DayFilters.parse(e.daysOfOperation);
            return (ops.contains(java.time.DayOfWeek.SATURDAY) || ops.contains(java.time.DayOfWeek.SUNDAY))
                    && e.trainType != null && e.trainType.equalsIgnoreCase("RJX");
        };

        List<TrainGraph.PathResult> weekendRJXPaths = g.pathsUpToTwoIntermediates("Paris", "London", weekendRJX);

        List<TrainGraph.PathResult> weekendRJXUnder150 = weekendRJXPaths.stream()
                .filter(p -> totalSecondCost(p) <= 150)
                .collect(Collectors.toList());

        System.out.println("\nWeekend RJX paths with total second-class ≤ €150: " + weekendRJXUnder150.size());
        weekendRJXUnder150.forEach(System.out::println);

    }

    static int totalFirstCost(TrainGraph.PathResult p) {
        return p.edges.stream().mapToInt(tc -> tc.firstClassRate).sum();
    }

    static int totalSecondCost(TrainGraph.PathResult p) {
        return p.edges.stream().mapToInt(tc -> tc.secondClassRate).sum();
    }
}

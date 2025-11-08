import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

/**
 * Test script to validate layover time constraints and day-of-week functionality.
 */
public class TestLayoverAndDays {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("LAYOVER AND DAY-OF-WEEK VALIDATION TEST SUITE");
        System.out.println("==============================================\n");

        // Run all test scenarios
        testScenario1_ShortLayover();
        testScenario2_LongLayover();
        testScenario3_OvernightLayover();
        testScenario4_MultiDayJourney();
        testScenario5_WeekendWrap();
        testScenario6_LayoverFiltering();

        System.out.println("\n==============================================");
        System.out.println("ALL TESTS COMPLETED");
        System.out.println("==============================================");
    }

    /**
     * Scenario 1: Short layover (30 minutes) - should pass with max layover > 30 min
     */
    private static void testScenario1_ShortLayover() {
        System.out.println("TEST 1: Short Layover (30 minutes)");
        System.out.println("-----------------------------------");

        List<TrainConnection> connections = new ArrayList<>();

        // Paris -> Lyon: Dep 08:00, Arr 10:00 (Daily)
        TrainConnection tc1 = createConnection("TGV001", "Paris", "Lyon",
            "08:00", "10:00", 0, "TGV", "Daily", 50, 30);

        // Lyon -> Marseille: Dep 10:30, Arr 12:00 (Daily)
        TrainConnection tc2 = createConnection("TGV002", "Lyon", "Marseille",
            "10:30", "12:00", 0, "TGV", "Daily", 40, 25);

        connections.add(tc1);
        connections.add(tc2);

        TrainGraph g = new TrainGraph(connections);
        List<TrainGraph.PathResult> paths = g.pathsUpToTwoIntermediates("Paris", "Marseille", tc -> true);

        if (!paths.isEmpty()) {
            System.out.println("✓ Path found:");
            System.out.println(paths.get(0));
            System.out.println("Expected: 30-minute layover in Lyon");
            System.out.println("Expected: All on same day (Day 0)");
        } else {
            System.out.println("✗ FAILED: No path found");
        }
        System.out.println();
    }

    /**
     * Scenario 2: Long layover (5 hours) - should be filtered if max < 300 min
     */
    private static void testScenario2_LongLayover() {
        System.out.println("TEST 2: Long Layover (5 hours)");
        System.out.println("-------------------------------");

        List<TrainConnection> connections = new ArrayList<>();

        // Berlin -> Munich: Dep 08:00, Arr 12:00 (Daily)
        TrainConnection tc1 = createConnection("ICE001", "Berlin", "Munich",
            "08:00", "12:00", 0, "ICE", "Daily", 80, 60);

        // Munich -> Vienna: Dep 17:00, Arr 21:00 (Daily)
        TrainConnection tc2 = createConnection("RJX001", "Munich", "Vienna",
            "17:00", "21:00", 0, "RJX", "Daily", 90, 70);

        connections.add(tc1);
        connections.add(tc2);

        TrainGraph g = new TrainGraph(connections);
        List<TrainGraph.PathResult> paths = g.pathsUpToTwoIntermediates("Berlin", "Vienna", tc -> true);

        if (!paths.isEmpty()) {
            System.out.println("✓ Path found:");
            System.out.println(paths.get(0));
            System.out.println("Expected: 5-hour (300 minute) layover in Munich");
            System.out.println("Expected: All on same day (Day 0)");
        } else {
            System.out.println("✗ FAILED: No path found");
        }
        System.out.println();
    }

    /**
     * Scenario 3: Overnight layover - crosses midnight
     */
    private static void testScenario3_OvernightLayover() {
        System.out.println("TEST 3: Overnight Layover (crossing midnight)");
        System.out.println("----------------------------------------------");

        List<TrainConnection> connections = new ArrayList<>();

        // Madrid -> Barcelona: Dep 20:00, Arr 23:30 (Daily)
        TrainConnection tc1 = createConnection("AVE001", "Madrid", "Barcelona",
            "20:00", "23:30", 0, "AVE", "Daily", 60, 40);

        // Barcelona -> Paris: Dep 08:00, Arr 14:00 (Daily)
        TrainConnection tc2 = createConnection("TGV100", "Barcelona", "Paris",
            "08:00", "14:00", 0, "TGV", "Daily", 100, 80);

        connections.add(tc1);
        connections.add(tc2);

        TrainGraph g = new TrainGraph(connections);
        List<TrainGraph.PathResult> paths = g.pathsUpToTwoIntermediates("Madrid", "Paris", tc -> true);

        if (!paths.isEmpty()) {
            System.out.println("✓ Path found:");
            System.out.println(paths.get(0));
            System.out.println("Expected: 8.5-hour overnight layover in Barcelona");
            System.out.println("Expected: Departure Day 0 (e.g., Mon), Arrival Day 1 (e.g., Tue)");
        } else {
            System.out.println("✗ FAILED: No path found");
        }
        System.out.println();
    }

    /**
     * Scenario 4: Multi-day journey - train arrives next day
     */
    private static void testScenario4_MultiDayJourney() {
        System.out.println("TEST 4: Multi-Day Journey (overnight train)");
        System.out.println("--------------------------------------------");

        List<TrainConnection> connections = new ArrayList<>();

        // Lisbon -> Madrid: Dep 22:00, Arr 08:00+1 (Daily)
        TrainConnection tc1 = createConnection("NIGHT001", "Lisbon", "Madrid",
            "22:00", "08:00", 1, "NightTrain", "Daily", 70, 50);

        // Madrid -> Barcelona: Dep 10:00, Arr 13:00 (Daily)
        TrainConnection tc2 = createConnection("AVE002", "Madrid", "Barcelona",
            "10:00", "13:00", 0, "AVE", "Daily", 60, 40);

        connections.add(tc1);
        connections.add(tc2);

        TrainGraph g = new TrainGraph(connections);
        List<TrainGraph.PathResult> paths = g.pathsUpToTwoIntermediates("Lisbon", "Barcelona", tc -> true);

        if (!paths.isEmpty()) {
            System.out.println("✓ Path found:");
            System.out.println(paths.get(0));
            System.out.println("Expected: Departure Day 0 (e.g., Thu 22:00)");
            System.out.println("Expected: Arrive Madrid Day 1 (e.g., Fri 08:00)");
            System.out.println("Expected: 2-hour layover in Madrid");
            System.out.println("Expected: Final arrival Day 1 (e.g., Fri 13:00)");
        } else {
            System.out.println("✗ FAILED: No path found");
        }
        System.out.println();
    }

    /**
     * Scenario 5: Week wrap-around (Saturday -> Sunday -> Monday)
     */
    private static void testScenario5_WeekendWrap() {
        System.out.println("TEST 5: Weekend Wrap-around");
        System.out.println("----------------------------");

        List<TrainConnection> connections = new ArrayList<>();

        // Rome -> Florence: Dep 20:00, Arr 02:00+1 (Sat,Sun,Mon)
        TrainConnection tc1 = createConnection("FB001", "Rome", "Florence",
            "20:00", "02:00", 1, "FrecciaRossa", "Sat,Sun,Mon", 50, 35);

        // Florence -> Milan: Dep 08:00, Arr 11:00 (Daily)
        TrainConnection tc2 = createConnection("FB002", "Florence", "Milan",
            "08:00", "11:00", 0, "FrecciaRossa", "Daily", 55, 40);

        connections.add(tc1);
        connections.add(tc2);

        TrainGraph g = new TrainGraph(connections);
        List<TrainGraph.PathResult> paths = g.pathsUpToTwoIntermediates("Rome", "Milan", tc -> true);

        if (!paths.isEmpty()) {
            System.out.println("✓ Path found:");
            System.out.println(paths.get(0));
            System.out.println("Expected: If departing Sat, arrive Florence Sun, depart Florence Sun");
            System.out.println("Expected: Shows day progression through weekend");
        } else {
            System.out.println("✗ FAILED: No path found");
        }
        System.out.println();
    }

    /**
     * Scenario 6: Test layover filtering functionality
     */
    private static void testScenario6_LayoverFiltering() {
        System.out.println("TEST 6: Layover Filtering");
        System.out.println("--------------------------");

        List<TrainConnection> connections = new ArrayList<>();

        // Create multiple paths with different layover times

        // Path 1: Short layover (1 hour)
        TrainConnection tc1a = createConnection("IC001", "CityA", "CityB",
            "08:00", "10:00", 0, "IC", "Daily", 30, 20);
        TrainConnection tc1b = createConnection("IC002", "CityB", "CityC",
            "11:00", "13:00", 0, "IC", "Daily", 30, 20);

        // Path 2: Long layover (6 hours) - via different intermediate
        TrainConnection tc2a = createConnection("IC003", "CityA", "CityD",
            "08:00", "10:00", 0, "IC", "Daily", 30, 20);
        TrainConnection tc2b = createConnection("IC004", "CityD", "CityC",
            "16:00", "18:00", 0, "IC", "Daily", 30, 20);

        // Direct path
        TrainConnection tc3 = createConnection("IC005", "CityA", "CityC",
            "09:00", "15:00", 0, "IC", "Daily", 50, 35);

        connections.add(tc1a);
        connections.add(tc1b);
        connections.add(tc2a);
        connections.add(tc2b);
        connections.add(tc3);

        TrainGraph g = new TrainGraph(connections);
        List<TrainGraph.PathResult> allPaths = g.pathsUpToTwoIntermediates("CityA", "CityC", tc -> true);

        System.out.println("All paths found: " + allPaths.size());
        for (int i = 0; i < allPaths.size(); i++) {
            System.out.println("\nPath " + (i + 1) + ":");
            System.out.println(allPaths.get(i));
        }

        // Simulate filtering with max layover = 120 minutes (2 hours)
        int maxLayover = 120;
        List<TrainGraph.PathResult> filteredPaths = new ArrayList<>();
        for (TrainGraph.PathResult path : allPaths) {
            if (!hasExcessiveLayover(path, maxLayover)) {
                filteredPaths.add(path);
            }
        }

        System.out.println("\n\nAfter filtering (max layover: " + maxLayover + " min):");
        System.out.println("Filtered paths: " + filteredPaths.size());
        for (int i = 0; i < filteredPaths.size(); i++) {
            System.out.println("\nFiltered Path " + (i + 1) + ":");
            System.out.println(filteredPaths.get(i));
        }

        System.out.println("\nExpected: Path via CityD (6-hour layover) should be filtered out");
        System.out.println("Expected: Path via CityB (1-hour layover) should remain");
        System.out.println();
    }

    // Helper methods

    private static TrainConnection createConnection(String routeID, String depCity, String arrCity,
                                                   String depTime, String arrTime, int dayOffset,
                                                   String trainType, String daysOfOperation,
                                                   int firstClass, int secondClass) {
        TrainConnection tc = new TrainConnection();

        // Use reflection to set private routeID field
        try {
            java.lang.reflect.Field field = TrainConnection.class.getDeclaredField("routeID");
            field.setAccessible(true);
            field.set(tc, routeID);
        } catch (Exception e) {
            System.err.println("Error setting routeID: " + e.getMessage());
        }

        tc.departureCity = depCity;
        tc.arrivalCity = arrCity;
        tc.departureTime = LocalTime.parse(depTime);
        tc.arrivalTime = LocalTime.parse(arrTime);
        tc.arrivalDayOffset = dayOffset;
        tc.trainType = trainType;
        tc.daysOfOperation = daysOfOperation;
        tc.firstClassRate = firstClass;
        tc.secondClassRate = secondClass;

        // Calculate duration
        long depMinutes = tc.departureTime.toSecondOfDay() / 60;
        long arrMinutes = tc.arrivalTime.toSecondOfDay() / 60 + dayOffset * 24 * 60L;
        tc.tripDuration = Duration.ofMinutes(arrMinutes - depMinutes);

        return tc;
    }

    private static boolean hasExcessiveLayover(TrainGraph.PathResult path, int maxLayoverMinutes) {
        List<TrainConnection> edges = path.edges;
        if (edges.size() <= 1) return false;

        long curAbs = minutesOfDay(edges.get(0).departureTime);

        for (int i = 0; i < edges.size(); i++) {
            TrainConnection currentTrain = edges.get(i);
            int depMin = minutesOfDay(currentTrain.departureTime);
            long depAbs = alignToNextOrSame(curAbs, depMin);
            long travel = currentTrain.tripDuration.toMinutes();
            long arrivalAbs = depAbs + travel;

            if (i < edges.size() - 1) {
                TrainConnection nextTrain = edges.get(i + 1);
                int nextDepMin = minutesOfDay(nextTrain.departureTime);
                long nextDepAbs = alignToNextOrSame(arrivalAbs, nextDepMin);

                long layoverMinutes = nextDepAbs - arrivalAbs;
                if (layoverMinutes > maxLayoverMinutes) {
                    return true;
                }
            }

            curAbs = arrivalAbs;
        }
        return false;
    }

    private static int minutesOfDay(LocalTime t) {
        return t.getHour() * 60 + t.getMinute();
    }

    private static long alignToNextOrSame(long currentAbs, int targetMinOfDay) {
        long day = currentAbs / 1440;
        long candidate = day * 1440 + targetMinOfDay;
        while (candidate < currentAbs) candidate += 1440;
        return candidate;
    }
}

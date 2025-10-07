import java.util.*;
import java.util.function.Predicate;

public class driver {

    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        // Loading CSV
        String csv = (args.length > 0) ? args[0] : "eu_rail_network.csv";
        TrainConnection.loadTrainConnectionsFromCSV(csv);

        // Building the graph
        TrainGraph g = new TrainGraph(TrainConnection.trainConnections);

        // Main loop
        while (true) {
            System.out.println("\n=== RAIL PLANNER ===");
            System.out.println("1) List available cities");
            System.out.println("2) Plan a trip (≤ 2 connections)");
            System.out.println("3) Quit");
            System.out.print("Choose: ");

            int choice = readInt();
                switch (choice) {
                case 1 -> listCities(g);
                case 2 -> planTripUI(g);  
                case 3 -> {
                        System.out.println("Goodbye!");
                        return;
                }
                default -> System.out.println("Invalid choice.");
                }

        }
    }

    // UI Methods

        private static void listCities(TrainGraph g) {
        List<String> a = new ArrayList<>(g.getAllCities());
        Collections.sort(a);
        int w = a.stream().mapToInt(String::length).max().orElse(10) + 2;
        int cols = Math.max(1, Math.min(8, 120 / w));         
        int rows = (int)Math.ceil(a.size() / (double)cols);
        System.out.println("\nAvailable cities (" + a.size() + "):");
        for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                int i = c * rows + r;
                if (i < a.size()) System.out.printf("%-" + w + "s", a.get(i));
                }
                System.out.println();
        }
        }

    private static void planTripUI(TrainGraph g) {
        Set<String> cities = g.getAllCities();
        if (cities.isEmpty()) {
            System.out.println("Error. Couldn't load CSV.");
            return;
        }

        System.out.println("\nType the FROM city (case-sensitive). Type '?' to list cities.");
        String from = promptCity(cities, "FROM", g);

        System.out.println("\nType the TO city (case-sensitive). Type '?' to list cities.");
        String to = promptCity(cities, "TO", g);

        Predicate<TrainConnection> any = e -> true;

        List<TrainGraph.PathResult> base = g.pathsUpToTwoIntermediates(from, to, any);
        if (base == null || base.isEmpty()) {
            System.out.println("\nNo trips found with ≤ 2 connections from " + from + " to " + to + ".");
            return;
        }

        boolean firstClass = false; // second class is default
        List<TrainGraph.PathResult> current = new ArrayList<>(base);

        //Inner User LOOP 
        while (true) {
    System.out.println("\n=== Trips " + from + " → " + to + " (≤2 connections) ===");
    printPaths(current, firstClass);

    System.out.println("\nOptions:");
    System.out.println("1) Sort by total PRICE (" + (firstClass ? "FIRST" : "SECOND") + "-class)");
    System.out.println("2) Sort by total DURATION");
    System.out.println("3) Toggle price class (now " + (firstClass ? "FIRST" : "SECOND") + ")");
    System.out.println("4) Back to main menu");
    System.out.print("Choose: ");

    int op = readInt();
    switch (op) {
        case 1 -> {
        final boolean fc = firstClass;
        Comparator<TrainGraph.PathResult> byPriceThenTime = (p1, p2) -> {
                int price1 = p1.edges.stream().mapToInt(tc -> fc ? tc.firstClassRate : tc.secondClassRate).sum();
                int price2 = p2.edges.stream().mapToInt(tc -> fc ? tc.firstClassRate : tc.secondClassRate).sum();
                if (price1 != price2) return Integer.compare(price1, price2);
                return p1.totalDuration.compareTo(p2.totalDuration);
        };
        current.sort(byPriceThenTime);
}

        
        case 3 -> firstClass = !firstClass;
        case 4 -> { return; }
        default -> System.out.println("Invalid choice.");
    }
}

    }

    //Printing 


private static void printPaths(List<TrainGraph.PathResult> paths, boolean firstClass) {
    int i = 1;
    for (TrainGraph.PathResult p : paths) {
        System.out.printf("%d) %s%n", i++, p);  
    }
}




    private static String promptCity(Set<String> cities, String label, TrainGraph g) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            if (s.equals("?")) {
                listCities(g);
                continue;
            }
            if (!cities.contains(s)) {
                System.out.println("Not found. Type '?' to list cities");
                continue;
            }
            return s;
        }
    }

    private static int readInt() {
        while (true) {
            String s = in.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.print("Enter a number:  ");
            }
        }



    }
}

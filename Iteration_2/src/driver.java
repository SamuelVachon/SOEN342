import java.time.DayOfWeek;
import java.util.*;
import java.util.function.Predicate;

public class driver {
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        String csv = (args.length > 0) ? args[0] : "eu_rail_network.csv";
        TrainConnection.loadTrainConnectionsFromCSV(csv);
        TrainGraph g = new TrainGraph(TrainConnection.trainConnections);
        while (true) {
            System.out.println("\n=== RAIL PLANNER ===");
            System.out.println("1) List available cities");
            System.out.println("2) Plan a trip (≤ 2 connections)");
            System.out.println("3) Quit");
            System.out.print("Choose: ");
            int choice = readInt();
            switch (choice) {
                case 1:
                    listCities(g);
                    break;
                case 2:
                    planTripUI(g);
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void listCities(TrainGraph g) {
        ArrayList<String> cities = new ArrayList<>(g.getAllCities());
        Collections.sort(cities);

        int colWidth = cities.stream().mapToInt(String::length).max().orElse(10) + 2;
        int cols = Math.max(1, Math.min(8, 120 / colWidth));
        int rows = (int) Math.ceil((double) cities.size() / (double) cols);

        System.out.println("\nAvailable cities (" + cities.size() + "):");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = c * rows + r;
                if (idx < cities.size()) {
                    System.out.printf("%-" + colWidth + "s", cities.get(idx));
                }
            }
            System.out.println();
        }
    }

    private static void planTripUI(TrainGraph g) 
    {
        CustomerCatalog customerCatalog = new CustomerCatalog();

        Set<String> all = g.getAllCities();
        if (all.isEmpty()) {
            System.out.println("Error. Couldn't load CSV.");
            return;
        }

        System.out.println("\nType the FROM city (case-sensitive). Type '?' to list cities.");
        String from = promptCity(all, "FROM", g);

        System.out.println("\nType the TO city (case-sensitive). Type '?' to list cities.");
        String to = promptCity(all, "TO", g);

        System.out.println("\nType the type of trains to travel on (case-sensitive).");
        System.out.println("Type 'all' for any type, or '?' to list available types. Example: TGV,ICE,RJX");
        Set<String> typesSelected = promptTypeTrains();           // empty set => no type restriction

        System.out.println("\nType the departure day(s).");
        System.out.println("Type 'all' for any day. Example: Mon,Tue or Fri-Sun");
        EnumSet<DayOfWeek> daysSelected = promptDays();           // empty set => no day restriction

        // Build one predicate that enforces both day and train-type constraints
        Predicate<TrainConnection> predicate = (tc) -> {
            boolean typeOK = typesSelected.isEmpty() || typesSelected.contains(tc.trainType);
            boolean dayOK  = daysSelected.isEmpty() || runsOnAny(tc, daysSelected);
            return typeOK && dayOK;
        };

        List<TrainGraph.PathResult> paths = g.pathsUpToTwoIntermediates(from, to, predicate);

        if (paths == null || paths.isEmpty()) {
            System.out.println("\nNo trips found with ≤ 2 connections from " + from + " to " + to + ".");
            return;
        }

        boolean useFirstClass = false; // default sort by 2nd-class price
        List<TrainGraph.PathResult> shown = new ArrayList<>(paths);
        boolean UserisBooking = false;
        while (true) {
            System.out.println("\n=== Trips " + from + " → " + to + " (≤2 connections) ===");
            printPaths(shown, useFirstClass);

            System.out.println("\nOptions:");
            System.out.println("1) Sort by total PRICE (" + (useFirstClass ? "FIRST" : "SECOND") + "-class)");
            System.out.println("2) Sort by total DURATION");
            System.out.println("3) Toggle price class (now " + (useFirstClass ? "FIRST" : "SECOND") + ")");
            System.out.println("4) Book a trip");
            System.out.println("5) View bookings (Existing customers only)");
            System.out.println("6) Back to main menu");
            System.out.print("Choose: ");

            int opt = readInt();
            switch (opt) {
                case 1: {
                    final boolean sortFirstClass = useFirstClass; // capture as effectively final
                    Comparator<TrainGraph.PathResult> cmp = Comparator
                            .comparingInt((TrainGraph.PathResult p) -> sumPrice(p, sortFirstClass))
                            .thenComparing((TrainGraph.PathResult p) -> p.totalDuration);
                    shown.sort(cmp);
                    break;
                }
                case 2:
                    shown.sort(Comparator.comparing((TrainGraph.PathResult pr) -> pr.totalDuration));
                    break;
                case 3:
                    useFirstClass = !useFirstClass;
                    break;
                case 4: {
                        System.out.print("\nSelect the trip number to book (1-" + shown.size() + "): ");
                        int sel = in.nextInt();
                        if (sel < 1 || sel > shown.size()) {
                            System.out.println("Invalid selection.");
                            break;
                        }
                        TrainGraph.PathResult chosenPath = shown.get(sel - 1);
                        bookTrip(customerCatalog, chosenPath);
                        break;
                     }
                 case 5: 
                    System.out.println("\nExisting Customers' Bookings:");
                    System.out.println("==============================");
                    System.out.println("Please Enter your Name:");
                    String name = in.nextLine();
                    System.out.println("Please Enter your ID:");
                    String id = in.nextLine();
                    System.out.println("/n/n");
                    CustomerCatalog.Customer existingCustomer = customerCatalog.find(id, name);
                    if (existingCustomer != null) {
                        customerCatalog.viewTrip(existingCustomer); 
                    } else {
                        System.out.println("Customer not found.");
                    }
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    private static void bookTrip(CustomerCatalog customerCatalog, TrainGraph.PathResult chosenPath){
        System.out.print("How many travellers? ");
        int numTravellers = readInt();

    ArrayList<CustomerCatalog.Customer> allCustomers = new ArrayList<>();
    for (int i = 0; i < numTravellers; i++) {
        System.out.println("\nTraveller " + (i + 1) + ":");
        System.out.print("Name: ");
        String name = in.nextLine();
        System.out.print("ID: ");
        String id = in.nextLine();
        System.out.print("Age: ");
        int age = readInt();
        

        CustomerCatalog.Customer customerTemp =customerCatalog.find(id, name);
        if (customerTemp == null) {
            customerTemp = customerCatalog.add(name, id, age);
        }
        allCustomers.add(customerTemp);
    }

        customerCatalog.bookTrip(allCustomers, chosenPath);

        for (CustomerCatalog.Customer c : allCustomers) {
                customerCatalog.viewTrip(c);
        }

        System.out.println("\n Trip booked successfully!");
        
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private static void printPaths(List<TrainGraph.PathResult> list, boolean firstClass) {
        int i = 1;
        for (TrainGraph.PathResult pr : list) {
            System.out.printf("%d) %s%n", i++, pr);
        }
    }

    private static String promptCity(Set<String> cities, String label, TrainGraph g) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            if (s.equals("?")) {
                listCities(g);
            } else if (cities.contains(s)) {
                return s;
            } else {
                System.out.println("Not found. Type '?' to list cities");
            }
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

    private static Set<String> promptTypeTrains() {
        Set<String> available = TrainConnection.getTrainTypes();
        Set<String> chosen = new HashSet<>();
        boolean needInput = true;

        while (needInput) {
            System.out.print("Type of train: ");
            String s = in.nextLine().trim();
            if (s.equals("?")) {
                for (String t : available) System.out.print(t + (", "));
                System.out.println();
                continue;
            }
            if (s.equalsIgnoreCase("all")) {
                chosen.clear(); // empty means "no restriction"
                break;
            }

            String[] toks = s.split(",");
            chosen.clear();
            needInput = false;
            for (String tok : toks) {
                String t = tok.trim();
                if (!available.contains(t)) {
                    System.out.println(t + " is not a type of train available. Enter '?' to list available types.");
                    needInput = true;
                    chosen.clear();
                    break;
                }
                chosen.add(t);
            }
        }
        return chosen;
    }

    private static EnumSet<DayOfWeek> promptDays() {
        while (true) {
            System.out.print("Departure days: ");
            String s = in.nextLine().trim();
            if (s.equalsIgnoreCase("all")) {
                return EnumSet.noneOf(DayOfWeek.class); // empty => no restriction
            }
            try {
                return parseDaysSpec(s);
            } catch (IllegalArgumentException ex) {
                System.out.println("Use comma-separated days like 'Mon,Wed' or ranges like 'Fri-Sun', or 'all'.");
            }
        }
    }

    // ---------- Price helper ----------
    private static int sumPrice(TrainGraph.PathResult p, boolean firstClass) {
        int sum = 0;
        for (TrainConnection e : p.edges) {
            sum += firstClass ? e.firstClassRate : e.secondClassRate;
        }
        return sum;
    }

    // ---------- Day filtering helpers ----------
    private static boolean runsOnAny(TrainConnection tc, EnumSet<DayOfWeek> selected) {
        if (selected == null || selected.isEmpty()) return true; // no restriction
        EnumSet<DayOfWeek> op = operatingDays(tc.daysOfOperation);
        for (DayOfWeek d : selected) if (op.contains(d)) return true;
        return false;
    }

    private static EnumSet<DayOfWeek> operatingDays(String spec) {
        String s = spec.replace("\"", "").trim();
        if (s.equalsIgnoreCase("Daily")) return EnumSet.allOf(DayOfWeek.class);

        if (s.contains(",") && !s.contains("-")) { // "Mon,Wed,Fri"
            EnumSet<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
            for (String tok : s.split(",")) set.add(tokenToDay(tok.trim()));
            return set;
        }
        if (s.contains("-")) { // "Fri-Sun" (wrap OK)
            return daysRange(s);
        }
        // Single day like "Mon"
        return EnumSet.of(tokenToDay(s));
    }

    private static EnumSet<DayOfWeek> daysRange(String range) {
        String[] parts = range.split("-");
        DayOfWeek start = tokenToDay(parts[0].trim());
        DayOfWeek end   = tokenToDay(parts[1].trim());
        EnumSet<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
        int i = start.getValue(); // 1..7
        while (true) {
            set.add(DayOfWeek.of(i));
            if (i == end.getValue()) break;
            i = i % 7 + 1; // wrap
        }
        return set;
    }

    private static DayOfWeek tokenToDay(String t) {
        String k = t.substring(0, Math.min(3, t.length())).toLowerCase(Locale.ROOT);
        switch (k) {
            case "mon": return DayOfWeek.MONDAY;
            case "tue": return DayOfWeek.TUESDAY;
            case "wed": return DayOfWeek.WEDNESDAY;
            case "thu": return DayOfWeek.THURSDAY;
            case "fri": return DayOfWeek.FRIDAY;
            case "sat": return DayOfWeek.SATURDAY;
            case "sun": return DayOfWeek.SUNDAY;
            default: throw new IllegalArgumentException("Bad day token: " + t);
        }
    }

    private static EnumSet<DayOfWeek> parseDaysSpec(String s) {
        s = s.replace("\"", "").trim();
        if (s.isEmpty()) return EnumSet.noneOf(DayOfWeek.class);

        // support "Mon,Wed" (commas), "Fri-Sun" (range), or combo like "Mon,Thu-Fri"
        EnumSet<DayOfWeek> out = EnumSet.noneOf(DayOfWeek.class);
        for (String part : s.split(",")) {
            String p = part.trim();
            if (p.isEmpty()) continue;
            if (p.contains("-")) {
                out.addAll(daysRange(p));
            } else {
                out.add(tokenToDay(p));
            }
        }
        return out;
        
    }

    
}

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.function.Predicate;

public class driver {
    
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        // Initialize counters from database before anything else
        Trip.initializeCounter();
        Reservation.initializeCounter();

        loader();
    String csv = (args.length > 0) ? args[0] : "Iteration_3/eu_rail_network.csv";
        TrainConnection.loadTrainConnectionsFromCSV(csv);
        TrainGraph g = new TrainGraph(TrainConnection.trainConnections);
            CustomerCatalog customerCatalog = new CustomerCatalog();
        while (true) {
            System.out.println("\n=== RAIL PLANNER ===");
            System.out.println("1) List available cities");
            System.out.println("2) Plan a trip (≤ 2 connections)");
            System.out.println("3) View bookings (Existing customers only)");
            System.out.println("4) Quit");
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
                System.out.println("\nExisting Customers' Bookings:");
                System.out.println("==============================");
                System.out.println("Please Enter your Last Name:");
                String lastName = in.nextLine();
                System.out.println("Please Enter your ID:");
                String id = in.nextLine();
                customerCatalog.viewTripFromDB(lastName, id);
                break;


                case 4:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    
    public static void loader() {
            CustomerCatalog customerCatalog = new CustomerCatalog();

            String sql =
            "SELECT c.first_name, c.last_name, c.age, c.identifier, t.route " +
            "FROM Customer c " +
            "JOIN Trip t ON c.customer_id = t.customer_id";


            try (Connection conn = DBManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    int age = rs.getInt("age");
                    String identifier = rs.getString("identifier");
                    String routeString = rs.getString("route");

                    // Create or find the customer
                    CustomerCatalog.Customer customer = customerCatalog.find(identifier, firstName, lastName);
                    if (customer == null)
                        customer = customerCatalog.add(firstName, lastName, identifier, age);

                    // Build edges from the route string
                    List<TrainConnection> edges = new ArrayList<>();
                    if (routeString != null && !routeString.isEmpty()) {
                        for (String id : routeString.split("\\|")) {
                            for (TrainConnection tc : TrainConnection.trainConnections) {
                                if (tc.getRouteID().equals(id.trim())) {
                                    edges.add(tc);
                                    break;
                                }
                            }
                        }
                    }

                    // Build PathResult and Trip, then attach to customer
                    if (!edges.isEmpty()) {
                        TrainGraph.PathResult path = new TrainGraph.PathResult(
                                edges.get(0).departureCity,
                                edges.get(edges.size() - 1).arrivalCity,
                                edges
                        );

                        ArrayList<CustomerCatalog.Customer> list = new ArrayList<>();
                        list.add(customer);

                        Trip trip = list.get(0).bookTrip(list, path);
                        trip.setFromPreviousSession(true); // Mark as loaded from database
                        for (CustomerCatalog.Customer c : list) {
                            c.addTrip(trip);
                        }
                    }
                }

                System.out.println("Trips successfully rebuilt and assigned to customers!");

            } catch (SQLException e) {
                System.out.println("Error rebuilding trips: " + e.getMessage());
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

        System.out.println("\nWhat is the maximum layover time (in minutes) you're willing to wait at a connection?");
        System.out.println("Type '0' for no limit. Example: 120 (for 2 hours)");
        int maxLayoverMinutes = promptMaxLayover();

        // Build one predicate that enforces both day and train-type constraints
        Predicate<TrainConnection> predicate = (tc) -> {
            boolean typeOK = typesSelected.isEmpty() || typesSelected.contains(tc.trainType);
            boolean dayOK  = daysSelected.isEmpty() || runsOnAny(tc, daysSelected);
            return typeOK && dayOK;
        };

        List<TrainGraph.PathResult> paths = g.pathsUpToTwoIntermediates(from, to, predicate);

        // Filter paths where all trains share at least one common operating day
        paths.removeIf(p -> !allTrainsShareCommonDay(p.edges));

        // Filter paths based on maximum layover time
        if (maxLayoverMinutes > 0) {
            paths.removeIf(p -> hasExcessiveLayover(p, maxLayoverMinutes));
        }

        if (paths == null || paths.isEmpty()) {
            System.out.println("\nNo trips found with ≤ 2 connections from " + from + " to " + to + ".");
            return;
        }


        boolean useFirstClass = false; // default sort by 2nd-class price
        List<TrainGraph.PathResult> shown = new ArrayList<>(paths);
        while (true) {
            System.out.println("\n=== Trips " + from + " → " + to + " (≤2 connections) ===");
            printPaths(shown, useFirstClass);

            System.out.println("\nOptions:");
            System.out.println("1) Sort by total PRICE (" + (useFirstClass ? "FIRST" : "SECOND") + "-class)");
            System.out.println("2) Sort by total DURATION");
            System.out.println("3) Toggle price class (now " + (useFirstClass ? "FIRST" : "SECOND") + ")");
            System.out.println("4) Book a trip");
            System.out.println("5) Back to main menu");
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
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
     private static void bookTrip(CustomerCatalog customerCatalog, TrainGraph.PathResult chosenPath) {
    System.out.print("How many travellers? ");
    int numTravellers = readInt();

    ArrayList<CustomerCatalog.Customer> allCustomers = new ArrayList<>();
    for (int i = 0; i < numTravellers; i++) {
        System.out.println("\nTraveller " + (i + 1) + ":");

        CustomerCatalog.Customer customerTemp = null;
        boolean customerConfirmed = false;

        while (!customerConfirmed) {
            System.out.print("ID: ");
            String id = in.nextLine();

            // Check if customer exists in DB
            customerTemp = customerCatalog.findCustomerByIdFromDB(id);

            if (customerTemp != null) {
                // Customer found, display info and ask for confirmation
                System.out.println("\nCustomer found:");
                System.out.println("Name: " + customerTemp.getFullName());
                System.out.println("Age: " + customerTemp.getAge());
                System.out.println("ID: " + customerTemp.getId());
                System.out.print("\nIs this the correct customer? (yes/no): ");
                String confirmation = in.nextLine().trim().toLowerCase();

                if (confirmation.equals("yes") || confirmation.equals("y")) {
                    customerConfirmed = true;
                } else {
                    System.out.println("\nThat was not the correct customer. Please try again.");
                    // Loop back to ask for ID again, maintaining order: ID, First Name, Last Name, Age
                }
            } else {
                // Customer not found, ask for full information
                System.out.println("Customer not found. Please enter the information:");
                System.out.print("First Name: ");
                String firstName = in.nextLine();
                System.out.print("Last Name: ");
                String lastName = in.nextLine();
                System.out.print("Age: ");
                int age = readInt();

                customerTemp = customerCatalog.add(firstName, lastName, id, age);
                customerCatalog.saveCustomerToDB(customerTemp);
                customerConfirmed = true;
            }
        }

        allCustomers.add(customerTemp);
    }

    // Create the trip in memory
    Trip trip = allCustomers.get(0).bookTrip(allCustomers, chosenPath);
    for (CustomerCatalog.Customer c : allCustomers) {
        c.addTrip(trip);
    }

    // Save the trip to DB
    customerCatalog.saveTripToDB(trip, allCustomers.get(0), chosenPath);

    // Save reservations for each passenger
    for (CustomerCatalog.Customer c : allCustomers) {
        Reservation reservation = new Reservation(c, chosenPath);
        customerCatalog.saveReservationToDB(reservation, 1); // use real trip_id later
    }

    for (CustomerCatalog.Customer c : allCustomers) {
        customerCatalog.viewTrip(c);
    }

    System.out.println("\nTrip booked and saved successfully!");
    System.out.println("Press Enter to go back to menu...");
    in.nextLine();
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

    private static int promptMaxLayover() {
        while (true) {
            System.out.print("Maximum layover (minutes): ");
            String s = in.nextLine().trim();
            try {
                int minutes = Integer.parseInt(s);
                if (minutes < 0) {
                    System.out.println("Please enter a non-negative number (0 for no limit).");
                    continue;
                }
                return minutes;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
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
 

    private static boolean allTrainsShareCommonDay(List<TrainConnection> edges) {
    EnumSet<DayOfWeek> common = EnumSet.allOf(DayOfWeek.class);
    for (TrainConnection tc : edges) {
        common.retainAll(tc.operatingDaysSet());
        if (common.isEmpty()) return false;
    }
    return true;
}

    /**
     * Checks if any layover in the path exceeds the maximum allowed time.
     * Returns true if any single layover is too long.
     */
    private static boolean hasExcessiveLayover(TrainGraph.PathResult path, int maxLayoverMinutes) {
        List<TrainConnection> edges = path.edges;
        if (edges.size() <= 1) return false; // Direct route, no layover

        long curAbs = minutesOfDay(edges.get(0).departureTime);

        for (int i = 0; i < edges.size(); i++) {
            TrainConnection currentTrain = edges.get(i);
            int depMin = minutesOfDay(currentTrain.departureTime);
            long depAbs = alignToNextOrSame(curAbs, depMin);
            long travel = currentTrain.tripDuration.toMinutes();
            long arrivalAbs = depAbs + travel;

            // If there's a next train, check the layover time
            if (i < edges.size() - 1) {
                TrainConnection nextTrain = edges.get(i + 1);
                int nextDepMin = minutesOfDay(nextTrain.departureTime);
                long nextDepAbs = alignToNextOrSame(arrivalAbs, nextDepMin);

                long layoverMinutes = nextDepAbs - arrivalAbs;
                if (layoverMinutes > maxLayoverMinutes) {
                    return true; // This layover is too long
                }
            }

            curAbs = arrivalAbs;
        }
        return false;
    }

    private static int minutesOfDay(java.time.LocalTime t) {
        return t.getHour() * 60 + t.getMinute();
    }

    private static long alignToNextOrSame(long currentAbs, int targetMinOfDay) {
        long day = currentAbs / 1440; // minutes in a day
        long candidate = day * 1440 + targetMinOfDay;
        while (candidate < currentAbs) candidate += 1440;
        return candidate;
    }

}

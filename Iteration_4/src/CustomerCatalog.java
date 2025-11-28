import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerCatalog{
    private  static ArrayList<Customer> customers = new ArrayList<>();

    public CustomerCatalog(){

    }

    public Trip bookTrip(ArrayList<Customer> clients, TrainGraph.PathResult pathResult){
        Trip trip = clients.get(0).bookTrip(clients, pathResult);
        for (int i=0;i<clients.size();i++){
            clients.get(i).addTrip(trip);
        }
        return trip;
    }

    public Customer add(String firstName, String lastName, String id, int age){
        CustomerCatalog.Customer newCustomer = new Customer(firstName, lastName, id, age);
        customers.add(newCustomer);
        return newCustomer;
    }

    public Customer find(String id, String firstName, String lastName){
        for (CustomerCatalog.Customer customer: customers){
            if (customer.getId().equals(id) &&
                customer.getFirstName().equals(firstName) &&
                customer.getLastName().equals(lastName)){
                return customer;
            }
        }
        return null;
    }

    public void viewTrip(Customer customer){
        System.out.println(customer.toString());
    }

    public ArrayList<Customer> getCustomers(){
        return CustomerCatalog.customers;
    }

    public class Customer{
        String firstName;
        String lastName;
        String id;
        int age;
        private ArrayList<Trip> trips;

        public Customer(String firstName, String lastName, String id, int age){
            this.firstName = firstName;
            this.lastName = lastName;
            this.id = id;
            this.age = age;
            this.trips = new ArrayList<>();
        }

        public Trip bookTrip(ArrayList<CustomerCatalog.Customer> clients, TrainGraph.PathResult pathResult){
            Trip trip = new Trip(clients,pathResult);
            return trip;
        }

        public void addTrip(Trip trip){
            this.trips.add(trip);
        }

        // Getters and setters
        public String getFirstName(){
            return this.firstName;
        }

        public void setFirstName(String firstName){
            this.firstName = firstName;
        }

        public String getLastName(){
            return this.lastName;
        }

        public void setLastName(String lastName){
            this.lastName = lastName;
        }

        public String getFullName(){
            return this.firstName + " " + this.lastName;
        }

        public String getId(){
            return this.id;
        }

        public void setId(String id){
            this.id = id;
        }

        public int getAge(){
            return this.age;
        }

        public void setAge(int age){
            this.age = age;
        }

        public ArrayList<Trip> getTrips(){
            return this.trips;
        }

        @Override
        public String toString(){
            String string =
            "Customer Name: " + this.firstName + " " + this.lastName + ", " +
            "ID: " + this.id + ", " +
            "Age: " + this.age;

            // Separate trips into two categories
            ArrayList<Trip> tripHistory = new ArrayList<>();
            ArrayList<Trip> currentTrips = new ArrayList<>();

            for (Trip trip: this.trips){
                if (trip.isFromPreviousSession()) {
                    tripHistory.add(trip);
                } else {
                    currentTrips.add(trip);
                }
            }

            // Display Trip History section
            if (!tripHistory.isEmpty()) {
                string += "\n\n=== TRIP HISTORY ===";
                for (Trip trip: tripHistory){
                    string += "\n" + trip.toString();
                }
            }

            // Display Current Trips section
            if (!currentTrips.isEmpty()) {
                string += "\n\n=== CURRENT TRIPS ===";
                for (Trip trip: currentTrips){
                    string += "\n" + trip.toString();
                }
            }

            if (tripHistory.isEmpty() && currentTrips.isEmpty()) {
                string += "\nNo trips found.";
            }

            return string;
        }
    


}

// ====================== DATABASE METHODS ======================
    public void saveCustomerToDB(Customer customer) {
        String sql = "INSERT INTO Customer (first_name, last_name, age, identifier) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setInt(3, customer.getAge());
            stmt.setString(4, customer.getId());
            stmt.executeUpdate();
            System.out.println("Customer saved to DB: " + customer.getFullName());
        } catch (SQLException e) {
            System.out.println(" Could not save customer: " + e.getMessage());
        }
    }

    public void saveReservationToDB(Reservation r, int tripId) {
        String sql = "INSERT INTO Reservation (trip_id, passenger_first_name, passenger_last_name, passenger_age, passenger_id, ticket_number)"
                   + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            CustomerCatalog.Customer c = r.getCustomer();
            stmt.setInt(1, tripId);
            stmt.setString(2, c.getFirstName());
            stmt.setString(3, c.getLastName());
            stmt.setInt(4, c.getAge());
            stmt.setString(5, c.getId());
            stmt.setString(6, "TICKET-" + r.getId());
            stmt.executeUpdate();
            System.out.println(" Reservation saved for passenger " + c.getFullName());
        } catch (SQLException e) {
            System.out.println(" Could not save reservation: " + e.getMessage());
        }
    }

    public void saveTripToDB(Trip trip, Customer customer, TrainGraph.PathResult path) {
        String sql = "INSERT INTO Trip (customer_id, origin, destination, path_description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Find customer_id from DB
            String lookup = "SELECT customer_id FROM Customer WHERE identifier=? LIMIT 1";
            try (PreparedStatement findStmt = conn.prepareStatement(lookup)) {
                findStmt.setString(1, customer.getId());
                ResultSet rs = findStmt.executeQuery();
                if (rs.next()) {
                    int customerId = rs.getInt("customer_id");

                    stmt.setInt(1, customerId);
                    stmt.setString(2, path.edges.get(0).departureCity);
                    stmt.setString(3, path.edges.get(path.edges.size() - 1).arrivalCity);
                    stmt.setString(4, path.toString());
                    stmt.executeUpdate();

                    System.out.println(" Trip saved to DB for " + customer.getFullName());
                } else {
                    System.out.println(" Could not find customer ID for " + customer.getFullName());
                }
            }
        } catch (SQLException e) {
            System.out.println(" Could not save trip: " + e.getMessage());
        }
    }

    public Customer findCustomerByIdFromDB(String identifier) {
        String sql = "SELECT first_name, last_name, age, identifier FROM Customer WHERE identifier=? LIMIT 1";
        try (Connection conn = DBManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, identifier);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                int age = rs.getInt("age");
                String id = rs.getString("identifier");

                // Check if customer already exists in memory
                Customer existing = find(id, firstName, lastName);
                if (existing != null) {
                    return existing;
                }

                // Create new customer object from DB data
                Customer customer = new Customer(firstName, lastName, id, age);
                customers.add(customer);
                return customer;
            }
        } catch (SQLException e) {
            System.out.println("Error finding customer: " + e.getMessage());
        }
        return null;
    }

            public void viewTripFromDB(String lastName, String identifier) {
            // Find customer in memory to check for in-memory trips
            Customer customerInMemory = null;
            for (Customer c : customers) {
                if (c.getLastName().equals(lastName) && c.getId().equals(identifier)) {
                    customerInMemory = c;
                    break;
                }
            }

            // Query database for all trips
            String sql = """
                SELECT
                    c.first_name,
                    c.last_name,
                    c.identifier,
                    c.age,
                    t.trip_id,
                    t.origin,
                    t.destination,
                    t.path_description
                FROM Customer c
                JOIN Trip t ON c.customer_id = t.customer_id
                WHERE c.last_name = ? AND c.identifier = ?;
            """;

            try (Connection conn = DBManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, lastName);
                ps.setString(2, identifier);

                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> historyTrips = new ArrayList<>();
                    ArrayList<String> currentTrips = new ArrayList<>();
                    String customerInfo = "";
                    boolean found = false;

                    // Collect all trips from database
                    while (rs.next()) {
                        if (!found) {
                            String firstName = rs.getString("first_name");
                            String lastNameDB = rs.getString("last_name");
                            String id = rs.getString("identifier");
                            int age = rs.getInt("age");
                            customerInfo = "Customer Name: " + firstName + " " + lastNameDB + ", ID: " + id + ", Age: " + age;
                        }
                        found = true;

                        int tripId = rs.getInt("trip_id");
                        String tripInfo = "------------------------------\n" +
                                        "Trip ID: " + tripId + "\n" +
                                        "From: " + rs.getString("origin") + "\n" +
                                        "To: " + rs.getString("destination") + "\n" +
                                        "Path: " + rs.getString("path_description");

                        // All trips from database are considered history
                        historyTrips.add(tripInfo);
                    }

                    // Add current trips from memory that might not be in DB query results yet
                    if (customerInMemory != null) {
                        for (Trip trip : customerInMemory.getTrips()) {
                            if (!trip.isFromPreviousSession()) {
                                currentTrips.add(trip.toString());
                            }
                        }
                    }

                    // Display results
                    if (!found && (customerInMemory == null || customerInMemory.getTrips().isEmpty())) {
                        System.out.println("No trips found for this customer.");
                        return;
                    }

                    System.out.println(customerInfo);

                    // Display Trip History
                    if (!historyTrips.isEmpty()) {
                        System.out.println("\n=== TRIP HISTORY ===");
                        for (String trip : historyTrips) {
                            System.out.println(trip);
                        }
                    }

                    // Display Current Trips
                    if (!currentTrips.isEmpty()) {
                        System.out.println("\n=== CURRENT TRIPS ===");
                        for (String trip : currentTrips) {
                            System.out.println(trip);
                        }
                    } else if (found) {
                        System.out.println("\n=== CURRENT TRIPS ===");
                        System.out.println("No current trips in this session.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }



    } 

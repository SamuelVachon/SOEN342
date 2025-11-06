import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerCatalog{
    private  static ArrayList<Customer> customers = new ArrayList<>();

    public CustomerCatalog(){

    }

    public void bookTrip(ArrayList<Customer> clients, TrainGraph.PathResult pathResult){
        Trip trip = clients.get(0).bookTrip(clients, pathResult);
        for (int i=0;i<clients.size();i++){
            clients.get(i).addTrip(trip);
        }
    }

    public Customer add(String name, String id, int age){
        CustomerCatalog.Customer newCustomer = new Customer(name,id,age);
        customers.add(newCustomer);
        return newCustomer;
    }

    public Customer find(String id, String name){
        for (CustomerCatalog.Customer customer: customers){
            if (customer.getId().equals(id) && customer.getName().equals(name)){
                return customer;
            }
        }
        return null;
    }

    public void viewTrip(Customer customer){
        System.out.println(customer.toString());
    }

    public ArrayList<Customer> getCustomers(){
        return this.customers;
    }

    public class Customer{
        String name;
        String id;
        int age;
        private ArrayList<Trip> trips;

        public Customer(String name, String id, int age){
            this.name = name;
            this.id =id;
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
        public String getName(){
            return this.name;
        }

        public void setName(String name){
            this.name = name;
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
            "Customer Name: " + this.name + ", " +
            "ID: " + this.id + ", " +
            "Age: " + this.age;
            for (Trip trip: this.trips){
                string += "\n" + trip.toString();
            }
            return string;
        }
    


}

// ====================== DATABASE METHODS ======================
    public void saveCustomerToDB(Customer customer) {
        String sql = "INSERT INTO Customer (name, age, identifier) VALUES (?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setInt(2, customer.getAge());
            stmt.setString(3, customer.getId());
            stmt.executeUpdate();
            System.out.println("Customer saved to DB: " + customer.getName());
        } catch (SQLException e) {
            System.out.println(" Could not save customer: " + e.getMessage());
        }
    }

    public void saveReservationToDB(Reservation r, int tripId) {
        String sql = "INSERT INTO Reservation (trip_id, passenger_name, passenger_age, passenger_id, ticket_number)"
                   + " VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            CustomerCatalog.Customer c = r.getCustomer();
            stmt.setInt(1, tripId);
            stmt.setString(2, c.getName());
            stmt.setInt(3, c.getAge());
            stmt.setString(4, c.getId());
            stmt.setString(5, "TICKET-" + r.getId());
            stmt.executeUpdate();
            System.out.println(" Reservation saved for passenger " + c.getName());
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

                    System.out.println(" Trip saved to DB for " + customer.getName());
                } else {
                    System.out.println(" Could not find customer ID for " + customer.getName());
                }
            }
        } catch (SQLException e) {
            System.out.println(" Could not save trip: " + e.getMessage());
        }
    }

    private int getCustomerIdFromDB(String identifier) {
        String sql = "SELECT customer_id FROM Customer WHERE identifier=? LIMIT 1";
        try (Connection conn = DBManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, identifier);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("customer_id");
        } catch (SQLException e) {
            System.out.println("Could not find customer_id: " + e.getMessage());
        }
        return -1;
    }

    } 

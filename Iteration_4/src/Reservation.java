import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Reservation{
    private static int counter = 0;
    CustomerCatalog.Customer customer;
    int id;
    TrainGraph.PathResult pathResult;

    public Reservation(CustomerCatalog.Customer client, TrainGraph.PathResult pathResult){
        this.id = Reservation.counter++;
        this.customer = client;
        this.pathResult = pathResult;
    }

    // Initialize counter based on max ID in database
    public static void initializeCounter() {
        String sql = "SELECT MAX(reservation_id) as max_id FROM Reservation";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                counter = maxId + 1; // Start from next available ID
                System.out.println("Reservation counter initialized to: " + counter);
            }
        } catch (SQLException e) {
            System.out.println("Could not initialize Reservation counter: " + e.getMessage());
            counter = 0; // Fallback to 0 if query fails
        }
    }

    // Getters and setters
    public CustomerCatalog.Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(CustomerCatalog.Customer customer) {
        this.customer = customer;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TrainGraph.PathResult getPathResult() {
        return this.pathResult;
    }

    public void setPathResult(TrainGraph.PathResult pathResult) {
        this.pathResult = pathResult;
    }

    @Override
    public String toString() {
        return
        "Reservation ID: " + this.id + "\n" +
        "Customer Name: " + this.customer.getFullName() + "\n" +
        "Path: " + this.pathResult.toString();
    }
}
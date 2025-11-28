import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Trip{
    int id;
    private static int counter = 0;
    private ArrayList<Reservation> reservations;
    private boolean isFromPreviousSession;

    public Trip(ArrayList<CustomerCatalog.Customer> clients, TrainGraph.PathResult pathResult){
        id = Trip.counter++;
        reservations = new ArrayList<>();
        for (CustomerCatalog.Customer client: clients){
            reservations.add(new Reservation(client, pathResult));
        }
        this.isFromPreviousSession = false; // Default to current session
    }

    // Initialize counter based on max ID in database
    public static void initializeCounter() {
        String sql = "SELECT MAX(trip_id) as max_id FROM Trip";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                counter = maxId + 1; // Start from next available ID
                System.out.println("Trip counter initialized to: " + counter);
            }
        } catch (SQLException e) {
            System.out.println("Could not initialize Trip counter: " + e.getMessage());
            counter = 0; // Fallback to 0 if query fails
        }
    }

    // Getters and setters
    public ArrayList<Reservation> getReservations() {
        return this.reservations;
    }

    public int getId() {
        return this.id;
    }

    public boolean isFromPreviousSession() {
        return this.isFromPreviousSession;
    }

    public void setFromPreviousSession(boolean isFromPreviousSession) {
        this.isFromPreviousSession = isFromPreviousSession;
    }

    @Override
    public String toString() {
        String string = "[" + this.id + "]\n" + "Trip Reservations:\n";
        for(Reservation res: this.reservations){
            string += res.toString() + "\n";
        }
        return string;
    }
    
}
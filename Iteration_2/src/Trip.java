import java.util.*;

public class Trip{
    private ArrayList<Reservation> reservations;

    public Trip(ArrayList<CustomerCatalog.Customer> clients, TrainGraph.PathResult pathResult){
        reservations = new ArrayList<>();
        for (CustomerCatalog.Customer client: clients){
            reservations.add(new Reservation(client, pathResult));
        }
    }

    // Getter
    public ArrayList<Reservation> getReservations() {
        return this.reservations;
    }

    @Override
    public String toString() {
        String string = "Trip Reservations:\n";
        for(Reservation res: this.reservations){
            string += res.toString() + "\n";
        }
        return string;
    }
    
}
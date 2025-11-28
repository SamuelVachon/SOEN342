import java.util.*;

public class Trip{
    int id;
    private static int counter = 0;
    private ArrayList<Reservation> reservations;

    public Trip(ArrayList<CustomerCatalog.Customer> clients, TrainGraph.PathResult pathResult){
        id = Trip.counter++;
        reservations = new ArrayList<>();
        for (CustomerCatalog.Customer client: clients){
            reservations.add(new Reservation(client, pathResult));
        }
    }

    // Getter
    public ArrayList<Reservation> getReservations() {
        return this.reservations;
    }

    public int getId() {
        return this.id;
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
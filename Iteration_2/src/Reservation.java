import java.util.*;

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
        "Customer Name: " + this.customer.getName() + "\n" +
        "Path: " + this.pathResult.toString();
    }
}
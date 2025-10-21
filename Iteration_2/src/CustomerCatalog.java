import java.util.*;

public class CustomerCatalog{
    public static ArrayList<Customer> customers = new ArrayList<>();

    public CustomerCatalog(){

    }

    public void bookTrip(ArrayList<Customer> clients, TrainGraph.PathResult pathResult){
        Trip trip = clients.get(0).bookTrip(clients, pathResult);
        for (int i=1;i<clients.size();i++){
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

    public viewTrip(Customer customer){
        System.out.println(customer.toString());
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
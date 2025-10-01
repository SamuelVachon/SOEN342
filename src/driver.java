
import java.util.*;

public class driver {
    public static void main(String[] args) {
        TrainConnection.loadTrainConnectionsFromCSV("./eu_rail_network.csv");
        for (TrainConnection tc : TrainConnection.trainConnections) {
            System.out.println(tc);
        }
        System.out.println(TrainConnection.trainConnections.size());
    }
}

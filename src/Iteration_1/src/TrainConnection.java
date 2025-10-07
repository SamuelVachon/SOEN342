import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TrainConnection {
    public static ArrayList<TrainConnection> trainConnections = new ArrayList<>(100);

    private String routeID;
    public String departureCity;
    public String arrivalCity;
    public LocalTime departureTime;
    public LocalTime arrivalTime;
    public int arrivalDayOffset;
    public String trainType;
    public String daysOfOperation;
    public int firstClassRate;
    public int secondClassRate;
    public Duration tripDuration;

    public String getRouteID() { return routeID; }

    public TrainConnection() {
    }

    public static void loadTrainConnectionsFromCSV(String filePath) {
        trainConnections.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        try (BufferedReader br = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {

            String line = br.readLine(); // skip header, we don't want it
            while ((line = br.readLine()) != null) {
                ArrayList<String> cols = parseCsvLine(line);
                if (cols.size() < 9)
                    continue;

                TrainConnection tc = new TrainConnection();
                tc.routeID = cols.get(0).trim();
                tc.departureCity = cols.get(1).trim();
                tc.arrivalCity = cols.get(2).trim();
                tc.departureTime = LocalTime.parse(cleanTime(cols.get(3)), formatter);
                tc.arrivalTime = LocalTime.parse(cleanTime(cols.get(4)), formatter);
                tc.arrivalDayOffset = extractDayOffset(cols.get(4));
                tc.trainType = cols.get(5).trim();
                tc.daysOfOperation = cols.get(6).trim();
                tc.firstClassRate = Integer.parseInt(cols.get(7).trim());
                tc.secondClassRate = Integer.parseInt(cols.get(8).trim());

                // compute trip duration
                tc.tripDuration = computeDuration(tc.departureTime, tc.arrivalTime, tc.arrivalDayOffset);

                trainConnections.add(tc);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Route %s: %s → %s | %s → %s (+%dd) | %s | Days: %s | 1st: €%d, 2nd: €%d | Duration: %dh %02dm",
                routeID,
                departureCity,
                arrivalCity,
                departureTime,
                arrivalTime,
                arrivalDayOffset,
                trainType,
                daysOfOperation,
                firstClassRate,
                secondClassRate,
                tripDuration.toHours(),
                tripDuration.toMinutesPart());
    }

    // Removes any suffix like "(+1d)" from time strings
    private static String cleanTime(String raw) {
        return raw.replaceAll("\\s*\\(\\+\\d+d\\)", "").trim();
    }

    // Extracts the day offset to compute the trip duration
    private static int extractDayOffset(String raw) {
        if (raw.contains("(+")) {
            String num = raw.replaceAll(".*\\(\\+(\\d+)d\\).*", "$1");
            return Integer.parseInt(num);
        }
        return 0;
    }

    // Compute duration considering next-day offsets
    private static Duration computeDuration(LocalTime dep, LocalTime arr, int dayOffset) {
        long depMinutes = dep.toSecondOfDay() / 60;
        long arrMinutes = arr.toSecondOfDay() / 60 + dayOffset * 24 * 60L;
        return Duration.ofMinutes(arrMinutes - depMinutes);
    }

    private static ArrayList<String> parseCsvLine(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out;
    }
}

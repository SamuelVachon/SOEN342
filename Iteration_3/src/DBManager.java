import java.sql.*;

public class DBManager {

    // Database credentials
    // Use "localhost" when running Java locally, "db" when running in Docker
    private static final String URL = "jdbc:mysql://localhost:3306/train_system";
    private static final String USER = "user";
    private static final String PASSWORD = "pass"; 

    public static Connection getConnection() {
        try {
            //  Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            //  Connect to the database
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

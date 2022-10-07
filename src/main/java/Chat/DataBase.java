package Chat;


import java.sql.*;
import java.util.ArrayList;

public final class DataBase {

    private static Connection connection;
    private static Statement stmt;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:Clients.db");
        stmt = connection.createStatement();
    }

    public void disconnect() {

        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList readEx() throws SQLException {
        String str;
        ArrayList arrayList = new ArrayList();
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM Clients;")) {
            while (rs.next()) {
                str = (rs.getString("login") + " " + rs.getString("pass") + " " + rs.getString("nick"));
                arrayList.add(str);
            }
        }
       return arrayList;
    }

    public void clearTableEx() throws SQLException {
        stmt.executeUpdate("DELETE FROM Clients;");
    }

    public void deleteEx() throws SQLException {
        stmt.executeUpdate("DELETE FROM Clients WHERE name = 'Bob1';");
    }

    public void updateEx(String oldName, String newName) throws SQLException {
        stmt.executeUpdate(String.format("UPDATE Clients SET nick = '%s' WHERE nick = '%s';", newName, oldName));
    }


    public void insertEx() throws SQLException {
        stmt.executeUpdate("INSERT INTO Clients (name, score) VALUES ('Jack', 85);");
    }


}

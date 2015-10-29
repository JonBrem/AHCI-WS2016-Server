package meme_recommender;

import org.apache.derby.drda.NetworkServerControl;
import util.Const;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jonbr on 28.10.2015.
 */
public class DatabaseContextListener implements ServletContextListener {

    public static final int DB_VERSION = 0;
    private static String dbURL = "jdbc:derby://localhost:1527/myDB;create=true;user=me;password=mine";

    private static DatabaseContextListener instance;
    private static Connection conn = null;


    public static DatabaseContextListener getInstance() {
        if(instance == null) {
            instance = new DatabaseContextListener();
        }
        return instance;
    }

    public void openConnection()  {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            conn = DriverManager.getConnection(dbURL);

            if(!(getDBTables(conn).contains("version") || getDBTables(conn).contains("VERSION"))) {
                createVersionTable();
            }
            checkVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkVersion() {
        Const.log(Const.LEVEL_VERBOSE, "Checking version. DB_VERSION should be: " + DB_VERSION);
        try {
            ResultSet results = conn.createStatement().executeQuery("SELECT * FROM version");
            if(results.next()) {
                int version = results.getInt(1);

                if(version > DB_VERSION) {
                    Const.log(Const.LEVEL_WARN, "Warning: Local DB Version is ahead of DB Version. Please adjust.");
                } else if (version == DB_VERSION) {
                    Const.log(Const.LEVEL_INFO, "DB version is: " + version + ", everything is OK.");
                } else {
                    Const.log(Const.LEVEL_INFO, "DB version is: " + version + ", updating DB");
                    updateDbFrom(version);
                }
            } else {
                Const.log(Const.LEVEL_ERROR, "Could not query version table");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Const.log(Const.LEVEL_ERROR, "Could not access version table.");
        }
    }

    private void updateDbFrom(int version) {
    }

    private void createVersionTable() {
        try {
            conn.createStatement().executeUpdate("CREATE TABLE version(version INTEGER)");
            conn.commit();
            conn.createStatement().executeUpdate("INSERT INTO version (version) VALUES (0)");
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        instance = this;
        NetworkServerControl server;
        try {
            server = new NetworkServerControl();
            server.start(null);
            openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        NetworkServerControl server;
        try {
            server = new NetworkServerControl();
            server.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        instance = null;
    }


    private Set<String> getDBTables(Connection targetDBConn) throws SQLException
    {
        Set<String> set = new HashSet<String>();
        DatabaseMetaData dbmeta = targetDBConn.getMetaData();
        readDBTable(set, dbmeta, "TABLE", null);
        readDBTable(set, dbmeta, "VIEW", null);
        return set;
    }

    private void readDBTable(Set<String> set, DatabaseMetaData dbmeta, String searchCriteria, String schema)
            throws SQLException
    {
        ResultSet rs = dbmeta.getTables(null, schema, null, new String[]
                { searchCriteria });
        while (rs.next())
        {
            set.add(rs.getString("TABLE_NAME").toLowerCase());
        }
    }

}

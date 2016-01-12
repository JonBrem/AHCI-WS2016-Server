package meme_recommender;

import de.ur.ahci.model.Meme;
import de.ur.ahci.model.Rating;
import de.ur.ahci.model.Tag;
import de.ur.ahci.model.User;
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

    public static final int DB_VERSION = 2;
    private static String dbURL = "jdbc:derby://localhost:1527/myDB;create=true;user=me;password=mine";

    private static DatabaseContextListener instance;
    private static Connection conn = null;


    public static DatabaseContextListener getInstance() {
        if(instance == null) {
            instance = new DatabaseContextListener();
        }
        return instance;
    }

    private void openConnection()  {
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

    /**
     * This should be the only point where changes to the database are made.
     * @param version
     */
    private void updateDbFrom(int version) {
        if(version == 0) {
            Const.log(Const.LEVEL_INFO, "Trying to update DB from version 0 to version 1.");
            try {
                conn.createStatement().executeUpdate(Meme.CREATE_TABLE);
                conn.createStatement().executeUpdate(Rating.CREATE_TABLE);
                conn.createStatement().executeUpdate(Tag.CREATE_TABLE_TAGS);
                conn.createStatement().executeUpdate(Tag.CREATE_TABLE_MEME_TAGS);
                conn.createStatement().executeUpdate(User.CREATE_TABLE);
                conn.createStatement().executeUpdate(User.CREATE_TABLE_USER_FACE_LANDMARKS);

                conn.createStatement().executeUpdate("UPDATE version SET version = 1");

                conn.commit();
                Const.log(Const.LEVEL_INFO, "Updated DB to version 1.");
                updateDbFrom(1);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if (version == 1) {
            Const.log(Const.LEVEL_INFO, "Trying to update DB to version 2.");
            try {
                for(String table : new String[]{"memes", "ratings", "tags", "meme_tags", "users"}) {
                    try {
                        conn.createStatement().executeUpdate("DROP TABLE " + table);
                    } catch (SQLException e) {

                    }
                }
                conn.createStatement().executeUpdate(Meme.CREATE_TABLE);
                conn.createStatement().executeUpdate(Rating.CREATE_TABLE);
                conn.createStatement().executeUpdate(Tag.CREATE_TABLE_TAGS);
                conn.createStatement().executeUpdate(Tag.CREATE_TABLE_MEME_TAGS);
                conn.createStatement().executeUpdate(User.CREATE_TABLE);

                conn.createStatement().executeUpdate("UPDATE version SET version = 2");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            updateDbFrom(2);
        } else if (version == 2) {

        }
    }

    /**
     * Checks if the db is up to date, i.e., if the version in the Java Constant "DB_VERSION"
     * matches the version in the database. If that is not the case, updateDbFrom(...) will be invoked.
     */
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

    /**
     * Creates the table where the version variable is stored.
     */
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

    public int executeUpdate(String sql) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public ResultSet executeInsert(String sql) {
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            return statement.getGeneratedKeys();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet query(String sql) {
        try {
            return conn.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Is called automatically when the server is started.
     * Starts the derby db server (port 1527 by default) and opens a connection to it.
     * @param servletContextEvent
     */
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

    /**
     * Is called automatically when the server is stopped.
     * stops the server.
     * @param servletContextEvent
     */
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

    public void export(String tableName, String fileName) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "CALL SYSCS_UTIL.SYSCS_EXPORT_TABLE (?,?,?,?,?,?)");
            ps.setString(1,null);
            ps.setString(2,tableName.toUpperCase());
            ps.setString(3,fileName);
            ps.setString(4,"%");
            ps.setString(5,null);
            ps.setString(6,null);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void importData(String tableName, String fileName, String columns, String columnIndices){

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "CALL SYSCS_UTIL.SYSCS_IMPORT_DATA (?,?,?,?,?,?,?,?,?)");
            ps.setString(1,null);
            ps.setString(2,tableName.toUpperCase());
            ps.setString(3,columns.toUpperCase());
            ps.setString(4,columnIndices);
            ps.setString(5,fileName);
            ps.setString(6,"%");
            ps.setString(7,null);
            ps.setString(8,null);
            ps.setInt(9, 0);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

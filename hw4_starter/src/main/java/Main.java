import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import model.Employer;
import model.Job;
import spark.Spark;

import java.sql.SQLException;

public class Main {

    private static Dao getEmployerORMLiteDao() throws SQLException {
        final String URI = "jdbc:sqlite:./JBApp.db";
        ConnectionSource connectionSource = new JdbcConnectionSource(URI);
        TableUtils.createTableIfNotExists(connectionSource, Employer.class);
        return DaoManager.createDao(connectionSource, Employer.class);
    }
    
    private static Dao getJobORMLiteDao() throws SQLException {
        final String URI = "jdbc:sqlite:./JBApp.db";
        ConnectionSource connectionSource = new JdbcConnectionSource(URI);
        TableUtils.createTableIfNotExists(connectionSource, Job.class);
        return DaoManager.createDao(connectionSource, Job.class);
    }

    public static void main(String[] args) {

        final int PORT_NUM = 7000;
        Spark.port(PORT_NUM);

        Spark.get("/employers", (req, res) -> {
            String results = new Gson().toJson(getEmployerORMLiteDao().queryForAll());
            res.type("application/json");
            res.status(200);
            return results;
        });

        // Task 4: Similar to employers endpoint above, write a "jobs" (http get) endpoint
        //  to return all rows in the "jobs" table a JSON!
        //  Note: For this endpoint to work properly, similar to getEmployerORMLiteDao you
        //  would need to write a new method to create "jobs" table, create a Job Dao and
        //  return it from the method!
        Spark.get("/jobs", (req, res) -> {
            String results = new Gson().toJson(getJobORMLiteDao().queryForAll());
            res.type("application/json");
            res.status(200);
            return results;
        });

    }
}

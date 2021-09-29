import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import model.Employer;
import model.Job;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class JobTest {

    private final String URI = "jdbc:sqlite:./JBApp.db";

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class JobORMLiteDaoTest {
        // TODO 5: Similar to what was done in EmployerTest.EmployerORMLiteDaoTest class, write JUnit tests
        //  to test basic CRUD operations on the jobs table! Think of interesting test cases and
        //  write at least four different test cases for each of the C(reate)/U(pdate)/D(elete)
        //  operations!
        //  Note: You need to (write code to) create the "jobs" table before writing your test cases!

        private ConnectionSource connectionSource;
        private Dao<Employer, Integer> employerDao;
        private Dao<Job, Integer> jobDao;

        // create a new connection to JBApp database, create "jobs" table, and create a
        // new dao to be used by test cases
        @BeforeAll
        public void setUpAll() throws SQLException {
            connectionSource = new JdbcConnectionSource(URI);
            TableUtils.createTableIfNotExists(connectionSource, Employer.class);
            employerDao = DaoManager.createDao(connectionSource, Employer.class);
            TableUtils.createTableIfNotExists(connectionSource, Job.class);
            jobDao = DaoManager.createDao(connectionSource, Job.class);
        }

        // delete all rows in the jobs table before each test case
        @BeforeEach
        public void setUpEach() throws SQLException {
            TableUtils.clearTable(connectionSource, Job.class);
            TableUtils.clearTable(connectionSource, Employer.class);
        }

        // inserting a new record where title is null must fail, the reason being
        // there is a non-null constraint on the "title" column in "jobs" table!
        @Test
        public void testCreateTitleNull() throws SQLException {
            //create new employer and persist to database
            Employer e = new Employer("Apple", "Tech", "Summary");
            employerDao.create(e);
            //create an associated job for the employer
            Date d = new Date();
            Job j = new Job(null, d, d, "www.myjob.com", "Baltimore", true, true, "must be human", 23, e);
            // try to insert into jobs table. This must fail!
            Assertions.assertThrows(SQLException.class, () -> jobDao.create(j));
        }

        // inserting a new record where domain is an empty string must succeed!
        @Test
        public void testCreateDomainEmpty() throws SQLException {
            //create new employer and persist to database
            Employer e = new Employer("Company1", "Category1", "Summary");
            employerDao.create(e);
            //create an associated job for the employer
            Date d = new Date();
            Job j = new Job("Position1", d, d, "", "Area1", true, true, "Requirement", 12, e);
            // try to insert into jobs table. This must succeed!
            jobDao.create(j);
            List<Job> ls = jobDao.queryForEq("title", j.getTitle());
            assertEquals(ls.size(), 1);
            assertEquals("", ls.get(0).getDomain());
        }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class JobAPITest {

        final String BASE_URL = "http://localhost:7000";
        private OkHttpClient client;

        @BeforeAll
        public void setUpAll() {
            client = new OkHttpClient();
        }

        @Test
        public void testHTTPGetJobsEndPoint() throws IOException {
            // TODO 6: Write code to send a http get request using OkHttp to the
            //  "jobs" endpoint and assert that the received status code is OK (200)!
            //  Note: In order for this to work, you need to make sure your local sparkjava
            //  server is running, before you run the JUnit test!
            String endpoint = BASE_URL + "/jobs";
            Request request = new Request.Builder()
                    .url(endpoint)
                    .build();
            Response response = client.newCall(request).execute();
            assertEquals(response.code(), 200);
        }
    }

}

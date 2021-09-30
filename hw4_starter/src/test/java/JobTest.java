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
            TableUtils.createTableIfNotExists(connectionSource, Job.class);
            employerDao = DaoManager.createDao(connectionSource, Employer.class);
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

        // insert multiple job records, and assert they were indeed added!
        @Test
        public void testCreateMultipleJobs() throws SQLException {
            //create new employer and persist to database
            Employer e = new Employer("First Solar", "Energy", "A leading global provider of comprehensive PV solar solutions!");
            employerDao.create(e);
            //create multiple new job instances
            Date d = new Date();
            List<Job> lsCreate = new ArrayList<>();
            lsCreate.add(new Job("Supervising Manager", d, d, "something", "San Francisco", true, false, "Good looking", 36, e));
            lsCreate.add(new Job("Engineer", d, d, "boop", "Ontario, Canada", true, true, "Big ideas", 25, e));
            lsCreate.add(new Job("Designer", d, d, "blip", "Ontario, Canada", false, true, "cool :)", 21, e));
            lsCreate.add(new Job("Fun Guy", d, d, "nob", "The Moon", false, false, "just a really fun guy", 9999, e));
            // try to insert them into jobs table. This must succeed!
            jobDao.create(lsCreate);
            // read all jobs
            List<Job> lsRead = jobDao.queryForAll();
            // assert all jobs in lsCreate were inserted and can be read
            for(int i = 0; i < 4; i++) {
                assertEquals(lsCreate.get(i).getTitle(), lsRead.get(i).getTitle());
            }
        }

        // inserting a new record where title is not unique must succeed!
        @Test
        public void testCreateDuplicateTitle() throws SQLException {
            //create new employer and persist to database
            Employer e = new Employer("GamerCorp", "Gaming", "We've got alpha gamers");
            employerDao.create(e);
            //create two associated jobs for the employer with the same title
            Date d = new Date();
            Job j1 = new Job("Gamer", d, d, "nom", "Gamers' Paradise", true, true, "must be an alpha gamer", 2, e);
            Job j2 = new Job("Gamer", d, d, "om", "Gamers' Dungeon", false, false, "must be good at games", 3, e);
            // try to insert them into jobs table. This must succeed!
            jobDao.create(j1);
            jobDao.create(j2);
            List<Job> ls = jobDao.queryForEq("title", j1.getTitle());
            assertEquals(ls.size(), 2);
            assertEquals("Gamer", ls.get(0).getTitle());
            assertEquals("Gamer", ls.get(1).getTitle());
        }

        // insert a new job, update its location, then assert it was indeed updated!
        @Test
        public void testUpdateLocation() throws SQLException {
            Employer e = new Employer("Kraft Heinz", "Food", "A global food and beverage company!");
            employerDao.create(e);
            //create an associated job for the employer
            Date d = new Date();
            Job j = new Job("Ketchup taster", d, d, "here", "Some dark alleyway", true, true, "have good taste", 18, e);
            j.setLocation("Some better alleyway");
            jobDao.createOrUpdate(j);
            // assert the sector is updated successfully!
            assertEquals("Some better alleyway", jobDao.queryForEq("title", "Ketchup taster").get(0).getLocation());
        }

        // insert a new job, update its title, then assert it was indeed updated!
        @Test
        public void testUpdateTitle() throws SQLException {
            Employer e = new Employer("Kraft Heinz", "Food", "A global food and beverage company!");
            employerDao.create(e);
            //create an associated job for the employer
            Date d = new Date();
            Job j = new Job("Ketchup taster", d, d, "here", "Some dark alleyway", true, true, "have good taste", 18, e);
            j.setTitle("Ketchup refiner");
            jobDao.createOrUpdate(j);
            // assert the sector is updated successfully!
            assertEquals("Ketchup refiner", jobDao.queryForEq("location", "Some dark alleyway").get(0).getTitle());
        }

        // insert a new job, update its title, then assert it was indeed updated!
        @Test
        public void testUpdateDomain() throws SQLException {
            Employer e = new Employer("Kraft Heinz", "Food", "A global food and beverage company!");
            employerDao.create(e);
            //create an associated job for the employer
            Date d = new Date();
            Job j = new Job("Ketchup taster", d, d, "here", "Some dark alleyway", true, true, "have good taste", 18, e);
            j.setDomain("everywhere");
            jobDao.createOrUpdate(j);
            // assert the sector is updated successfully!
            assertEquals("everywhere", jobDao.queryForEq("location", "Some dark alleyway").get(0).getDomain());
        }

        //updating a job's id to an id value that exists in the table fails
        @Test
        public void testUpdateJobIDFail() throws SQLException {
            Employer e = new Employer("Kraft Heinz", "Food", "A global food and beverage company!");
            employerDao.create(e);
            //create associated jobs for the employer
            Date d = new Date();
            List<Job> myList = new ArrayList<>();
            myList.add(new Job("Ketchup taster", d, d, "here", "Some dark alleyway", true, true, "have good taste", 18, e));
            myList.add(new Job("Ketchup refiner", d, d, "there", "Some bright alleyway", false, true, "have good skills", 20, e));
            jobDao.create(myList);
            Assertions.assertThrows(SQLException.class, () ->  jobDao.updateId(myList.get(0),myList.get(1).getId()));
        }

        // insert a new record, then delete it, and assert it was indeed removed!
        @Test
        public void testDeleteAllFieldsMatch() throws SQLException {
            // create a new employer and persist to database
            Employer e = new Employer("Kraft Heinz", "Food", "A global food and beverage company!");
            employerDao.create(e);
            //create an associated job for the employer
            Date d = new Date();
            Job j = new Job("Ketchup taster", d, d, "here", "Some dark alleyway", true, true, "have good taste", 18, e);
            //insert into jobs table
            jobDao.create(j);
            List<Job> ls1 = jobDao.queryForEq("title", j.getTitle());
            assertEquals(1, ls1.size());
            assertEquals("Ketchup taster", ls1.get(0).getTitle());
            jobDao.delete(j);
            // Assert "Kraft Heinz" was removed from employers
            List<Job> ls2 = jobDao.queryForEq("title", j.getTitle());
            assertEquals(0, ls2.size());
        }

        //  D(elete)
        //  Deleting an employer record
        //  (using the "delete" function of ORMLite) based on an id that does not exist does not delete any rows even if
        //  a row with the same exact name exists
        @Test
        public void testDeleteEmployerRecordFails() throws SQLException {
            Employer e = new Employer("Kraft Heinz", "Food", "A global food and beverage company!");
            employerDao.create(e);
            //create associated jobs for the employer
            Date d = new Date();
            List<Job> myList = new ArrayList<>();
            myList.add(new Job("Ketchup taster", d, d, "here", "Some dark alleyway", true, true, "have good taste", 18, e));
            myList.add(new Job("Ketchup refiner", d, d, "there", "Some bright alleyway", false, true, "have good skills", 20, e));
            jobDao.create(myList);
            jobDao.updateId(myList.get(0),100);
            jobDao.delete(jobDao.queryForId(10));
            assertEquals(myList.size(),jobDao.queryForAll().size());

            Assertions.assertDoesNotThrow(() ->  jobDao.delete(jobDao.queryForId(10)), "Test passed");
        }

        //Deleting an employer record (using the "delete" function of ORMLite) based on an id that exists succeeds even if the names are different
        @Test
        public void testDeleteJobRecordSucceeds() throws SQLException {
            Employer e = new Employer("Kraft Heinz", "Food", "A global food and beverage company!");
            employerDao.create(e);
            //create associated jobs for the employer
            Date d = new Date();
            List<Job> myList = new ArrayList<>();
            myList.add(new Job("Ketchup taster", d, d, "here", "Some dark alleyway", true, true, "have good taste", 18, e));
            myList.add(new Job("Ketchup refiner", d, d, "there", "Some bright alleyway", false, true, "have good skills", 20, e));
            jobDao.create(myList);
            jobDao.updateId(myList.get(0),10);
            Assertions.assertDoesNotThrow(() ->  jobDao.delete(jobDao.queryForId(10)), "Test passed");
        }

        //Deleting a collection of employers at once (using the "delete" function of ORMLite) removes all those employers that exist from the table
        @Test
        public void testDeleteJobCollectionExists() throws SQLException {
            Employer e = new Employer("Kraft Heinz", "Food", "A global food and beverage company!");
            employerDao.create(e);
            //create associated jobs for the employer
            Date d = new Date();
            List<Job> myList = new ArrayList<>();
            myList.add(new Job("Ketchup taster", d, d, "here", "Some dark alleyway", true, true, "have good taste", 18, e));
            myList.add(new Job("Ketchup refiner", d, d, "there", "Some bright alleyway", false, true, "have good skills", 20, e));
            jobDao.create(myList);
            jobDao.delete(myList);
            List<Job> lsRead = jobDao.queryForAll();
            assertEquals(0, lsRead.size());
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

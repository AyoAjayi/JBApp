import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.*;
import java.io.IOException;

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

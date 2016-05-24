import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.verify.VerificationTimes;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.testng.Assert.*;


public class CRUD {
    static final Logger log = Logger.getLogger(new Object() {}.getClass().getEnclosingClass().getName());
    static MockServerClient mockServer = startClientAndServer(4545);

    String baseUrl;
    HttpClient client = HttpClientBuilder.create().build();
    HttpRequestBase request;

    @BeforeClass
    @Parameters({"baseUrl"})
    public void before(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Test
    @Parameters({ "userId", "userName" })
    public void create(String userId, String userName) throws IOException {
        log.info("creating userId=" + userId + " userName=" + userName);
        // Given
        HttpPost httpPost = createPost(baseUrl);
        List<BasicNameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("user_id", userId));
        nvps.add(new BasicNameValuePair("event", "registration"));
        nvps.add(new BasicNameValuePair("value", userName));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        // When
        HttpResponse response = client.execute(httpPost);

        // Then
        assertEquals(response.getStatusLine().getStatusCode(), 201);
        assertTrue(getContent(response).contains("Operation success"), "there should be 'Operation success'!");
        mockServer.verify(
                request()
                        .withMethod("GET")
                        .withPath("/postbacks")
                        .withQueryStringParameter("user_id", userId)
                        .withQueryStringParameter("event", "registration")
                        .withQueryStringParameter("value", userName),
                VerificationTimes.exactly(1)
        );
    }

    @Test
    @Parameters({ "userId", "userName" })
    public void createWithExistingId(String userId, String userName) throws IOException {
        log.info("creating userId=" + userId + " userName=" + userName);
        // Given
        HttpPost httpPost = createPost(baseUrl);
        List<BasicNameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("user_id", userId));
        nvps.add(new BasicNameValuePair("event", "registration"));
        nvps.add(new BasicNameValuePair("value", userName));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        // When
        HttpResponse response = client.execute(httpPost);

        // Then
        assertNotEquals(response.getStatusLine().getStatusCode(), 201, "existing user should not be replaced!");
    }

    @Test
    @Parameters({ "userId", "userName" })
    public void read(String userId, String userName) throws IOException {
        log.info("reading userId=" + userId + " userName=" + userName);
        // When
        HttpGet httpGet = createGet(baseUrl + "?event=registration&user_id=" + userId);
        HttpResponse response = client.execute(httpGet);

        // Then
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(getContent(response), userName == null ? "null" : userName);
    }

    @Test
    @Parameters({ "userId"})
    public void delete(String userId) throws IOException {
        log.info("deleting userId=" + userId);
        // When
        HttpDelete httpDelete = createDelete(baseUrl + "?event=registration&user_id=" + userId);
        HttpResponse response = client.execute(httpDelete);

        // Then
        assertEquals(response.getStatusLine().getStatusCode(), 204);
    }

    /**
     * Wrong input parameters. Mandatory parameters are: event, user_id, value
     */
    @Test
    @Parameters({"userName" })
    public void createUserIdMissing(String userName) throws IOException {
        // Given
        HttpPost httpPost = createPost(baseUrl);
        List<BasicNameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("event", "registration"));
        nvps.add(new BasicNameValuePair("value", userName));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        // When
        HttpResponse response = client.execute(httpPost);

        // Then
        assertEquals(response.getStatusLine().getStatusCode(), 400);
        assertFalse(getContent(response).contains("Operation success"), "there should be no 'Operation success'!");
    }

    /**
     * Wrong input parameters. Mandatory parameters are: event, user_id, value
     */
    @Test
    @Parameters({ "userId", "userName" })
    public void createEventMissing(String userId, String userName) throws IOException {
        // Given
        HttpPost httpPost = createPost(baseUrl);
        List<BasicNameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("user_id", userId));
        nvps.add(new BasicNameValuePair("value", userName));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        // When
        HttpResponse response = client.execute(httpPost);

        // Then
        assertEquals(response.getStatusLine().getStatusCode(), 400);
        assertFalse(getContent(response).contains("Operation success"), "there should be no 'Operation success'!");
    }

    /**
     * Wrong input parameters. Mandatory parameters are: event, user_id, value
     */
    @Test
    @Parameters({ "userId"})
    public void createValueMissing(String userId) throws IOException {
        // Given
        HttpPost httpPost = createPost(baseUrl);
        List<BasicNameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("user_id", userId));
        nvps.add(new BasicNameValuePair("event", "registration"));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        // When
        HttpResponse response = client.execute(httpPost);

        // Then
        assertEquals(response.getStatusLine().getStatusCode(), 400);
        assertFalse(getContent(response).contains("Operation success"), "there should be no 'Operation success'!");
    }

    @Test
    @Parameters({ "userId"})
    public void levelUp(String userId) throws IOException {
        log.info("level up for userId=" + userId);
        // Given
        HttpPost httpPost = createPost(baseUrl);
        List<BasicNameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("user_id", userId));
        nvps.add(new BasicNameValuePair("event", "levelup"));
        nvps.add(new BasicNameValuePair("value", "10"));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        // When
        HttpResponse response = client.execute(httpPost);

        // Then
        assertEquals(response.getStatusLine().getStatusCode(), 201);
        assertTrue(getContent(response).contains("Operation success"), "there should be 'Operation success'!");
        mockServer.verify(
                request()
                        .withMethod("GET")
                        .withPath("/postbacks")
                        .withQueryStringParameter("user_id", userId)
                        .withQueryStringParameter("event", "levelup")
                        .withQueryStringParameter("value", "10"),
                VerificationTimes.exactly(1)
        );
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        if (request != null) {
            request.releaseConnection();
        }
        mockServer.reset();
    }



//======================================================================================= private:

    private String getContent(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private HttpGet createGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        this.request = httpGet;
        return httpGet;
    }
    private HttpPost createPost(String url) {
        HttpPost httpPost = new HttpPost(url);
        this.request = httpPost;
        return httpPost;
    }

    private HttpDelete createDelete(String url) {
        HttpDelete httpDelete = new HttpDelete(url);
        this.request = httpDelete;
        return httpDelete;
    }
}

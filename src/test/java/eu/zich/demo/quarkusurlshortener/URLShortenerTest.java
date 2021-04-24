package eu.zich.demo.quarkusurlshortener;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class URLShortenerTest {

    /**
     * There should be an index page at /
     */
    @Test
    public void testRootEndpoint() {
        given()
          .when().get("/")
          .then()
             .statusCode(200);
    }

}
package eu.zich.demo.quarkusurlshortener;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

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
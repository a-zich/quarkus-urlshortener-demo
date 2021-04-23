package eu.zich.demo.quarkusurlshortener;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ApiRessourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/urls")
          .then()
             .statusCode(200)
             .body(is("Hello RESTEasy"));
    }

}
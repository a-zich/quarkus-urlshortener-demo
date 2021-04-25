package eu.zich.demo.quarkusurlshortener;

import static io.restassured.RestAssured.given;

import javax.ws.rs.core.MediaType;

import org.hamcrest.core.IsEqual;
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

    /**
     * There should be an index page at /
     */
    @Test
    public void testSwaggerUI() {
        given()
          .when().get("/q/swagger-ui")
          .then()
             .statusCode(200);
    }
    
    @Test
    public void test5digitForward() {
        given()
          .when().get("/12345")
          .then()
             .statusCode(404);
    }

    @Test
    public void test7digitForward() {
        given()
          .when().get("/1234567")
          .then()
             .statusCode(404);
    }
    
    @Test
    public void testCreateDeleteWithNameAndStats() {
        given()
          .when()
          .contentType(MediaType.APPLICATION_JSON).body("{\n"
          		+ "  \"redirectUrl\": \"https://www.google.com\",\n"
          		+ "  \"shortUrl\": \"google\"\n"
          		+ "}").post("/urls")
          .then()
             .statusCode(201);
        
        given()
        .when()
        .get("/stats/google")
        .then()
        	.statusCode(200)
        	.body(IsEqual.equalTo("0"));
        
        given()
          .when()
          .redirects().follow(false)
          .get("/google")
          .then()
          	.statusCode(307);

        given()
        .when()
        .get("/stats/google")
        .then()
        	.statusCode(200)
        	.body(IsEqual.equalTo("1"));

        
        given()
        .when()
        .contentType(MediaType.APPLICATION_JSON).body("{\n"
        		+ "  \"redirectUrl\": \"https://www.google.com\",\n"
        		+ "  \"shortUrl\": \"google\"\n"
        		+ "}").post("/urls")
        .then()
           .statusCode(409);  //should result in a conflict 

        given()
        .when()
        .delete("/urls/google")
        .then()
        .statusCode(204);   

        given()
        .when()
        .delete("/urls/google")
        .then()
        .statusCode(404); //deleting the same twice should result in 404   
    
        given()
        .when()
        .get("/stats/google")
        .then()
        	.statusCode(200)
        	.body(IsEqual.equalTo("0"));
    }

    @Test
    public void testCreateDeleteRandomNameAndStats() {
    	String id = given()
          .when()
          .contentType(MediaType.APPLICATION_JSON).body("{\n"
          		+ "  \"redirectUrl\": \"https://www.google.com\"\n"
          		+ "}").post("/urls")
          .then()
             .statusCode(200)
             .extract().path("shortUrl");
        
        given()
          .when()
          .redirects().follow(false)
          .get("/"+id)
          .then()
          	.statusCode(307);

        given()
        .when()
        .get("/stats/"+id)
        .then()
        	.statusCode(200)
        	.body(IsEqual.equalTo("1"));

        
        given()
        .when()
        .contentType(MediaType.APPLICATION_JSON).body("{\n"
        		+ "  \"redirectUrl\": \"https://www.google.com\",\n"
        		+ "  \"shortUrl\": \""+id+"\"\n"
        		+ "}").post("/urls")
        .then()
           .statusCode(409);  //should result in a conflict 

        given()
        .when()
        .delete("/urls/"+id)
        .then()
        .statusCode(204);   

        given()
        .when()
        .delete("/urls/"+id)
        .then()
        .statusCode(404); //deleting the same twice should result in 404   
    
    }

    @Test
    public void testInvalidUrl() {
        given()
          .when()
          .contentType(MediaType.APPLICATION_JSON).body("{\n"
          		+ "  \"redirectUrl\": \"www.google.com\",\n"
          		+ "  \"shortUrl\": \"google\"\n"
          		+ "}").post("/urls")
          .then()
             .statusCode(400);
    }
    
    @Test
    public void testInvalidCode() {
        given()
          .when()
          .contentType(MediaType.APPLICATION_JSON).body("{\n"
           		+ "  \"httpCode\": 302,\n"
          		+ "  \"redirectUrl\": \"www.google.com\",\n"
          		+ "  \"shortUrl\": \"google\"\n"
          		+ "}").post("/urls")
          .then()
             .statusCode(400);
    }
    
    @Test
    public void testInvalidShortUrl() {
    given()
    .when()
    .contentType(MediaType.APPLICATION_JSON).body("{\n"
    		+ "  \"redirectUrl\": \"https://www.google.com\",\n"
    		+ "  \"shortUrl\": \"google7\"\n"
    		+ "}").post("/urls")
    .then()
       .statusCode(400);
    }
    
}
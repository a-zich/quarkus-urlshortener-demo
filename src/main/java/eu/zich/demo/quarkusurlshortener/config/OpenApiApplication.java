package eu.zich.demo.quarkusurlshortener.config;

import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

@OpenAPIDefinition(
	    info = @Info(
	        title="URL Shortening Demo API",
	        version = "1.0.0",
	        contact = @Contact(
	            name = "Alexander Zich",
	            url = "https://github.com/a-zich/quarkus-urlshortener-demo"
	            ),
	        license = @License(
	            name = "MIT",
	            url = "https://github.com/a-zich/quarkus-urlshortener-demo/blob/master/LICENSE"))
	)
public class OpenApiApplication extends Application {

}

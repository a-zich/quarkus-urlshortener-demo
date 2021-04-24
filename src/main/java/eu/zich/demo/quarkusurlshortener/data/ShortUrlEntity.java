package eu.zich.demo.quarkusurlshortener.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.Response;

import eu.zich.demo.quarkusurlshortener.constants.Constants;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.ToString;

/**
 * Database entity for all short Urls.
 */
@Entity
@ToString
public class ShortUrlEntity extends PanacheEntityBase {

	@Id
	@Pattern(regexp = Constants.REGEXP_6LETTERS, message = "short URL must have exactly 6 letters")
	public String shortUrl;

	@NotNull(message = "the forwarding URL must be given")
	@Pattern(regexp = "^\\w+:.*$", message = "you must provide an absolute URL")
	@Column(length = 3900) // Location Header shouldn't be more then 4k anyway
	public String redirectUrl;

	public int httpCode = Response.Status.TEMPORARY_REDIRECT.getStatusCode();
}

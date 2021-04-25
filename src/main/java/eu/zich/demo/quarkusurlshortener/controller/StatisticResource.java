package eu.zich.demo.quarkusurlshortener.controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import eu.zich.demo.quarkusurlshortener.constants.Constants;
import eu.zich.demo.quarkusurlshortener.service.StatisticService;
import lombok.extern.jbosslog.JBossLog;

/**
 * Provides the /stats/<id> endpoint to get the number of calls this shortUrl
 * had in the last 24h. Currently we don't bother to return a JSON. Instead a
 * plain text number is return. Also we don't care to check whether the URL
 * really exists. If it doesn't exist there is also no Statistics object and
 * thus 0 is returned. Checking for existence would mean going to the database
 * which just creates overhead.
 */
@JBossLog
@Path("/stats")
public class StatisticResource {

	@Inject
	StatisticService statsservice;

	/**
	 * @param id the shortUrl to return statistics for
	 * @return number of times the shortUrl was called in the last 24h
	 */
	@GET
	@Path(Constants.ID_WITH6LETTERS_PATH)
	@APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.TEXT_PLAIN), description = "Number of times this shortUrl was called in the last 24h, 0 if not called or not configured")
	public String getStats(@PathParam("id") String id) {
		log.infov("getting stats for {0}", id);
		return Integer.toString(statsservice.getStats(id));
	}
}

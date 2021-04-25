package eu.zich.demo.quarkusurlshortener.controller;

import java.net.URI;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import eu.zich.demo.quarkusurlshortener.constants.Constants;
import eu.zich.demo.quarkusurlshortener.data.ShortUrlEntity;
import eu.zich.demo.quarkusurlshortener.service.StatisticService;
import io.vertx.core.http.HttpHeaders;
import lombok.extern.jbosslog.JBossLog;

/**
 * REST resource handles for /urls.
 */
@JBossLog
@Path("/urls")
public class UrlResource {

	/**
	 * Numbers of times to try and find an empty slot by random guessing 6 digits
	 * short Url. Even if namespace is 99% full after 32 times the chance of finding
	 * a free spot is 1-(1-99%)^32 = 27,5%
	 */
	static final int MAXIMUM_TRIES_TO_FIND_EMPTY_SLOT = 32;

	@Inject
	StatisticService statsservice;

	@GET
	@Path(Constants.ID_WITH6LETTERS_PATH)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ShortUrlEntity.class, required = true)), description = "display the currently configured information for this short Url")
	@APIResponse(responseCode = "404", description = "when short Url was not found")
	public ShortUrlEntity getShortendUrl(@PathParam("id") String id) {
		log.infov("getting ShortUrlEntity for id={0}", id);
		return ShortUrlEntity.<ShortUrlEntity>findByIdOptional(id)
				.orElseThrow(() -> new NotFoundException("the given shortUrl does not exist"));
	}

	@DELETE
	@Path(Constants.ID_WITH6LETTERS_PATH)
	@Transactional
	@APIResponse(responseCode = "204", description = "short URL successfully deleted and statistics cleaned")
	@APIResponse(responseCode = "404", description = "when short Url was not found")
	public void deleteShortendUrl(@PathParam("id") String id) {
		log.infov("deleting ShortUrlEntity for id={0}", id);

		ShortUrlEntity.findByIdOptional(id, LockModeType.PESSIMISTIC_WRITE)
				.orElseThrow(() -> new NotFoundException("the given shortUrl does not exist")).delete();

		statsservice.deleteStats(id);

		// returning nothing will result in HTTP 204 which is fine according to HTTP
		// spec
		// https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
	}

	@POST
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ShortUrlEntity.class, required = true)), description = "when a new random shortUrl was generated and configured successfully.")
	@APIResponse(responseCode = "204", description = "when the shortUrl was configured successfully.")
	@APIResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = org.jboss.resteasy.api.validation.ViolationReport.class, required = true)),   description = "when the data was not valid")
	@APIResponse(responseCode = "409", description = "when this shortUrl is already taken")
	@APIResponse(responseCode = "503", description = "when no random free spot in the namespace was found. Please try again later.")
	public Response postShortendUrl(@Valid ShortUrlEntity entity, @Context UriInfo uriInfo) {
		log.infov("creating new ShortUrlEntity for {0}", entity.toString());

		// no need to check whether entity is valid as Quarkus will do for us

		// we must distinguish between a shortUrl given or not

		if (entity.shortUrl == null) {
			// try to find an empty slot
			for (int i = 0; i < MAXIMUM_TRIES_TO_FIND_EMPTY_SLOT; i++) {
				entity.shortUrl = RandomStringUtils.randomAlphanumeric(6);
				try {
					createShortUrlInDb(entity);
					URI newLocation = uriInfo.getAbsolutePathBuilder().path(entity.shortUrl).build();
					// we return the JSON and the location to make it easier for the user to know
					// the shortUrl
					return Response.ok(entity).header(HttpHeaders.LOCATION.toString(), newLocation).build();
				} catch (EntityExistsException e) {
					log.debugv("try number {0} slot id {1} is already taken", i, entity.shortUrl);
				}
			}
			return Response
					.status(Status.SERVICE_UNAVAILABLE.getStatusCode(), "couldn't find an empty slot for this URL")
					.build();
		} else {
			try {
				createShortUrlInDb(entity);
				URI newLocation = uriInfo.getAbsolutePathBuilder().path(entity.shortUrl).build();
				return Response.created(newLocation).build();
			} catch (EntityExistsException e) {
				// shortUrl already exists in database -> send conflict
				return Response.status(Status.CONFLICT.getStatusCode(), "the same shortUrl is already used").build();
			}
		}
		// any other exception will be sent as error
	}

	/**
	 * will insert the entity into the DB
	 * 
	 * @param entity
	 * @throws EntityExistsException if an entity with this shortUrl already exists
	 */
	private static void createShortUrlInDb(ShortUrlEntity entity) {
		if (!ShortUrlEntity.findByIdOptional(entity.shortUrl, LockModeType.PESSIMISTIC_WRITE).isEmpty())
			throw new EntityExistsException(); // H2 doesn't throw this on insert

		entity.persistAndFlush(); // might throw Exception if shortUrl is already taken
	}

}
package eu.zich.demo.quarkusurlshortener.controller;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import eu.zich.demo.quarkusurlshortener.data.ShortUrlEntity;
import eu.zich.demo.quarkusurlshortener.service.StatisticService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RoutingExchange;
import io.vertx.core.http.HttpHeaders;
import lombok.extern.jbosslog.JBossLog;

/**
 * This is the real forwarding service. It takes Path in the form of 6
 * alphanumeric short URL and returning either 404 if the shortUrl is not known
 * or a forward to the configured URL.
 */
@JBossLog
@ApplicationScoped
public class Forwarder {

	@Inject
	StatisticService statsservice;

	// we don't bother if it is blocking as H2 isn't reactive anyway
	/**
	 * This will handle all HTTP methods. It tries to find the configured
	 * {@link ShortUrlEntity} in the database. As H2 does not support reactive
	 * programming we declare the method as blocking.
	 * 
	 * @param ex
	 */
	@Route(regex = "\\/(\\w{6})", type = Route.HandlerType.BLOCKING)
	void handle(RoutingExchange ex) {

		String id = ex.getParam("param0").get();
		log.infov("handling forward for {0}", id);
		Optional<ShortUrlEntity> entityOptional = ShortUrlEntity.findByIdOptional(id);

		if (entityOptional.isEmpty()) {
			ex.notFound().end();
		} else {

			ShortUrlEntity entity = entityOptional.get();
			statsservice.increaseCallCount(id);
			// we won't set the status message and thus the default is used
			ex.response().setStatusCode(entity.httpCode).putHeader(HttpHeaders.LOCATION, entity.redirectUrl).end();
		}
	}

}

package edu.damago.cookbook.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import javax.annotation.Priority;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import edu.damago.cookbook.persistence.Person;
import edu.damago.tool.HashCodes;
import edu.damago.tool.RestJpaLifecycleProvider;


/**
 * JAX-RS filter provider that performs HTTP "Basic" authentication on any REST service request
 * within an HTTP server environment. This aspect-oriented design swaps "Authorization" headers
 * for "X-Requester-Identity" headers within any REST service request being received.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthenticationReceiverFilter implements ContainerRequestFilter {

	/**
	 * HTTP request header for the authenticated requester's identity.
	 */
	static public final String REQUESTER_IDENTITY = "X-Requester-Identity";


	/**
	 * Performs HTTP "basic" authentication by calculating a password hash from the password contained in the request's
	 * "Authorization" header, and comparing it to the one stored in the person matching said header's username. The
	 * "Authorization" header is consumed in any case, and upon success replaced by a new "Requester-Identity" header that
	 * contains the authenticated person's identity. The filter chain is aborted in case of a problem.
	 * @param requestContext the request context
	 * @throws NullPointerException if the given argument is {@code null}
	 */
	public void filter (final ContainerRequestContext requestContext) throws NullPointerException {
		final MultivaluedMap<String,String> headers = requestContext.getHeaders();
		if (headers.containsKey(REQUESTER_IDENTITY)) {
			requestContext.abortWith(Response.status(Status.BAD_REQUEST).build());
			return;
		}

		final List<String> header = headers.remove(HttpHeaders.AUTHORIZATION);
		if (header != null && !header.isEmpty() && header.get(0).startsWith("Basic ")) {
			final String credentials = new String(Base64.getDecoder().decode(header.get(0).substring(6)), StandardCharsets.UTF_8);
			final int delimiterPosition = credentials.indexOf(':');
			if (delimiterPosition != -1) {
				final String email = credentials.substring(0, delimiterPosition);
				final String password = credentials.substring(delimiterPosition + 1);

				final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
				final TypedQuery<Person> query = entityManager.createQuery("select p from Person as p where p.email = :email", Person.class);
				final List<Person> people = query.setParameter("email", email).getResultList();
				if (!people.isEmpty()) {
					final Person requester = people.get(0);

					final String leftHash = requester.getPasswordHash();
					final String rightHash = HashCodes.sha2HashText(256, password);
					if (leftHash.equals(rightHash)) {
						headers.putSingle(REQUESTER_IDENTITY, Long.toString(requester.getIdentity()));
						return;
					}
				}
			}
		}

		final Response response = Response.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Basic").build();
		requestContext.abortWith(response);
	}
}

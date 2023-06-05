package edu.damago.cookbook.service;

import javax.persistence.EntityManager;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import edu.damago.cookbook.persistence.Person;
import edu.damago.tool.RestJpaLifecycleProvider;


@Path("people")
public class PersonService {

	/**
	 * HTTP Signature: GET people/<id> IN: - OUT: application/json
	 * @param personIdentity the person identity
	 * @param requesterIdentity the requester identity
	 * @return the person as JSON
	 * @throws ClientErrorException if there is no matching person (404)
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Person findPerson (
		@PathParam("id") @PositiveOrZero final long personIdentity,
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final long identity = personIdentity == 0 ? requesterIdentity : personIdentity;
		final Person person = entityManager.find(Person.class, identity);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);

		return person;
	}
}

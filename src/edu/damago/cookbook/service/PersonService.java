package edu.damago.cookbook.service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import edu.damago.cookbook.persistence.Person;
import edu.damago.tool.RestJpaLifecycleProvider;


@Path("people")
public class PersonService {
	static private final String QUERY_TYPES = "select q.identity from Person as q where "
		+ "(:minCreated is null or q.created >= :minCreated) and "
		+ "(:maxCreated is null or q.created <= :maxCreated) and "
		+ "(:minModified is null or q.modified >= :minModified) and "
		+ "(:maxModified is null or q.modified <= :maxModified) and "
		+ "(:email is null or q.email = :email) and "
		+ "(:emailFragment is null or q.email like concat('%', :emailFragment, '%')) and "
		+ "(:group is null or q.group = :group) and "
		+ "(:title is null or q.name.title = :title) and "
		+ "(:surname is null or q.name.family = :surname) and "
		+ "(:forename is null or q.name.given = :forename) and "
		+ "(:street is null or q.address.street = :street) and "
		+ "(:city is null or q.address.city = :city) and "
		+ "(:country is null or q.address.country = :country) and "
		+ "(:postcode is null or q.address.postcode = :postcode)";
	
	
	
	/**
	 * HTTP Signature: GET Person IN: - OUT: application/json
	 * @param resultOffset the result offset, or null for none
	 * @param resultSize the maximum result size, or null for none
	 * @param minCreated the minimum creation timestamp, or null for none
	 * @param minCreated the maximum creation timestamp, or null for none
	 * @param minModified the minimum modification timestamp, or null for none
	 * @param maxModified the maximum modification timestamp, or null for none
	 * @param email the email, or null for none
	 * @param emailFragment the email fragment, or null for none
	 * @param group
	 * @param title
	 * @param surname
	 * @param street
	 * @param city
	 * @param country
	 * @param postcode
	 * 
	 * @return the Person as JSON
	 * @throws ClientErrorException if there is no matching Person (404)
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person[] queryPerson (
		@QueryParam("result-offset") @PositiveOrZero final Integer resultOffset,
		@QueryParam("result-size") @PositiveOrZero final Integer resultSize,
		@QueryParam("min-created") final Long minCreated,
		@QueryParam("max-created") final Long maxCreated,
		@QueryParam("min-modified") final Long minModified,
		@QueryParam("max-modified") final Long maxModified,
		@QueryParam("email") final String email,
		@QueryParam("emailFragment") final String emailFragment,
		@QueryParam("group") final Person.Group group,
		@QueryParam("title") final String title,
		@QueryParam("surname") final String surname,
		@QueryParam("forename") final String forename,
		@QueryParam("street") final String street,
		@QueryParam("city") final String city,
		@QueryParam("country") final String country,
		@QueryParam("postcode") final String postcode


		) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final TypedQuery<Long> query = entityManager.createQuery(QUERY_TYPES, Long.class);
		if (resultOffset != null) query.setFirstResult(resultOffset);
		if (resultSize != null) query.setMaxResults(resultSize);
		query.setParameter("minCreated", minCreated);
		query.setParameter("maxCreated", maxCreated);
		query.setParameter("minModified", minModified);
		query.setParameter("maxModified", maxModified);
		query.setParameter("email", email);
		query.setParameter("emailFragment", emailFragment);
		query.setParameter("group", group);
		query.setParameter("title", title);
		query.setParameter("surname", surname);
		query.setParameter("forename", forename);
		query.setParameter("street", street);
		query.setParameter("city", city);
		query.setParameter("country", country);
		query.setParameter("postcode", postcode);


	
		final Person[] types = query
			.getResultList()
			.stream()
			.map(identity -> entityManager.find(Person.class, identity))
			.filter(type -> type != null)
			.sorted()
			.toArray(Person[]::new);

		return types;
	}
	
	
	
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

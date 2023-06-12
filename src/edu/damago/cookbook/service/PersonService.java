package edu.damago.cookbook.service;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import edu.damago.cookbook.persistence.Document;
import edu.damago.cookbook.persistence.Person;
import edu.damago.tool.ContentTypes;
import edu.damago.tool.HashCodes;
import edu.damago.tool.RestJpaLifecycleProvider;


@Path("people")
public class PersonService {
	static private final String QUERY_PEOPLE = "select p.identity from Person as p where "
		+ "(:minCreated is null or p.created >= :minCreated) and "
		+ "(:maxCreated is null or p.created <= :maxCreated) and "
		+ "(:minModified is null or p.modified >= :minModified) and "
		+ "(:maxModified is null or p.modified <= :maxModified) and "
		+ "(:email is null or p.email = :email) and "
		+ "(:group is null or p.group = :group) and "
		+ "(:title is null or p.name.title = :title) and "
		+ "(:surname is null or p.name.family = :surname) and "
		+ "(:forename is null or p.name.given = :forename) and "
		+ "(:street is null or p.address.street = :street) and "
		+ "(:city is null or p.address.city = :city) and "
		+ "(:country is null or p.address.country = :country) and "
		+ "(:postcode is null or p.address.postcode = :postcode)";


	/**
	 * HTTP Signature: GET recipes IN: - OUT: application/json
	 * @param resultOffset the result offset, or null for none
	 * @param resultSize the maximum result size, or null for none
	 * @param minCreated the minimum creation timestamp, or null for none
	 * @param minCreated the maximum creation timestamp, or null for none
	 * @param minModified the minimum modification timestamp, or null for none
	 * @param maxModified the maximum modification timestamp, or null for none
	 * @return the matching recipes as JSON
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person[] queryRecipes (
		@QueryParam("result-offset") @PositiveOrZero final Integer resultOffset,
		@QueryParam("result-size") @PositiveOrZero final Integer resultSize,
		@QueryParam("min-created") final Long minCreated,
		@QueryParam("max-created") final Long maxCreated,
		@QueryParam("min-modified") final Long minModified,
		@QueryParam("max-modified") final Long maxModified,
		@QueryParam("email") final String email,
		@QueryParam("group") final Person.Group group,
		@QueryParam("title") final String title,
		@QueryParam("surname") final String surname,
		@QueryParam("forename") final String forename,
		@QueryParam("street") final String street,
		@QueryParam("city") final String city,
		@QueryParam("country") final String country,
		@QueryParam("postcode") final String postcode
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final TypedQuery<Long> query = entityManager.createQuery(QUERY_PEOPLE, Long.class);
		if (resultOffset != null) query.setFirstResult(resultOffset);
		if (resultSize != null) query.setMaxResults(resultSize);
		query.setParameter("minCreated", minCreated);
		query.setParameter("maxCreated", maxCreated);
		query.setParameter("minModified", minModified);
		query.setParameter("maxModified", maxModified);
		query.setParameter("email", email);
		query.setParameter("group", group);
		query.setParameter("title", title);
		query.setParameter("surname", surname);
		query.setParameter("forename", forename);
		query.setParameter("street", street);
		query.setParameter("city", city);
		query.setParameter("country", country);
		query.setParameter("postcode", postcode);

		final Person[] people = query
			.getResultList()
			.stream()
			.map(identity -> entityManager.find(Person.class, identity))
			.filter(person -> person != null)
			.sorted()
			.toArray(Person[]::new);

		return people;
	}


	/**
	 * HTTP Signature: POST people IN: application/json OUT: text/plain
	 * @param requesterIdentity the ID of the authenticated person
	 * @param personTemplate the person template
	 * @return the person identity
	 * @throws ClientErrorException if there is a problem caused by the client
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public long insertOrUpdatePerson (
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@HeaderParam("X-Set-Password") @Size(min=3) final String password,
		@NotNull @Valid final Person personTemplate
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null ) throw new ClientErrorException(Status.FORBIDDEN);
		final boolean insertMode = personTemplate.getIdentity() == 0;

		final Person person;
		if (insertMode) {
			person = new Person();

			final Document defaultAvatar = entityManager.find(Document.class, 1L);
			if (defaultAvatar == null) throw new ServerErrorException(Status.SERVICE_UNAVAILABLE);
			person.setAvatar(defaultAvatar);
		} else {
			person = entityManager.find(Person.class, personTemplate.getIdentity());
			if (person == null) throw new ClientErrorException(Status.NOT_FOUND);
			if (person.getIdentity() != requester.getIdentity() && requester.getGroup() != Person.Group.ADMIN) throw new ClientErrorException(Status.FORBIDDEN);
		}

		person.setModified(System.currentTimeMillis());
		person.setVersion(personTemplate.getVersion());
		person.setEmail(personTemplate.getEmail());
		person.getName().setTitle(personTemplate.getName().getTitle());
		person.getName().setFamily(personTemplate.getName().getFamily());
		person.getName().setGiven(personTemplate.getName().getGiven());
		person.getAddress().setStreet(personTemplate.getAddress().getStreet());
		person.getAddress().setCity(personTemplate.getAddress().getCity());
		person.getAddress().setCountry(personTemplate.getAddress().getCountry());
		person.getAddress().setPostcode(personTemplate.getAddress().getPostcode());
		person.getPhones().retainAll(personTemplate.getPhones());
		person.getPhones().addAll(personTemplate.getPhones());
		if (requester.getGroup().ordinal() >= personTemplate.getGroup().ordinal()) person.setGroup(personTemplate.getGroup());
		if (password != null) person.setPasswordHash(HashCodes.sha2HashText(256, password));

		try {
			if (insertMode) entityManager.persist(person);
			else entityManager.flush();

			entityManager.getTransaction().commit();
		} catch (final RollbackException e) {
			throw new ClientErrorException(Status.CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}

		// whenever our modifications cause the mirror relationship set of an entity to be
		// modified, we need to remove said entity from the 2nd level case -> cache eviction!
		// final Cache cache = entityManager.getEntityManagerFactory().getCache();
		// cache.evict(BaseEntity.class, 42L);

		return person.getIdentity();
	}


	/**
	 * HTTP Signature: GET people/{id} IN: - OUT: application/json
	 * @param personIdentity the person identity
	 * @param requesterIdentity the requester identity
	 * @return the person as JSON
	 * @throws ClientErrorException if there is no matching person (404)
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Person findPerson (
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @PositiveOrZero final long personIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final long identity = personIdentity == 0 ? requesterIdentity : personIdentity;
		final Person person = entityManager.find(Person.class, identity);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);

		return person;
	}


	/**
	 * HTTP Signature: GET people/{id}/avatar IN: - OUT: image/*
	 * @param acceptHeader the HTTP "Accept" header
	 * @param personIdentity the person identity
	 * @return the matching person's avatar content
	 * @throws ClientErrorException if there is no matching person (404)
	 */
	@GET
	@Path("{id}/avatar")
	@Produces("image/*")
	public Response findTypeAvatar (
		@HeaderParam(HttpHeaders.ACCEPT) @NotNull @NotEmpty final String acceptHeader,
		@PathParam("id") @Positive final long typeIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person person = entityManager.find(Person.class, typeIdentity);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);

		final Document avatar = person.getAvatar();
		if (!ContentTypes.isAcceptable(avatar.getType(), acceptHeader)) throw new ClientErrorException(Status.NOT_ACCEPTABLE);
		return Response.ok(avatar.getContent(), avatar.getType()).build();
	}


	@PUT
	@Path("{id}/avatar")
	@Consumes("image/*")
	public long updatePersonAvatar (
		@PathParam("id") @Positive final long personIdentity,
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@HeaderParam(HttpHeaders.CONTENT_TYPE) @NotNull @NotEmpty final String documentType,
		@NotNull final byte[] documentContent
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person person = entityManager.find(Person.class, personIdentity);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || (requester.getIdentity() != person.getIdentity() && requester.getGroup() != Person.Group.ADMIN)) throw new ClientErrorException(Status.FORBIDDEN);

		final TypedQuery<Document> query = entityManager.createQuery(Document.FIND_BY_HASH, Document.class); 
		query.setParameter("hash", HashCodes.sha2HashText(256, documentContent));
		final Document image = query
			.getResultList()
			.stream()
			.findAny()
			.orElseGet(() -> new Document(documentType, documentContent));

		if (image.getIdentity() == 0) {
			try {
				entityManager.persist(image);

				entityManager.getTransaction().commit();
			} catch (final RollbackException e) {
				throw new ClientErrorException(Status.CONFLICT);
			} finally {
				entityManager.getTransaction().begin();
			}
		}

		person.setAvatar(image);

		try {
			entityManager.flush();

			entityManager.getTransaction().commit();
		} finally {
			entityManager.getTransaction().begin();
		}

		return image.getIdentity();
	}
}

package edu.damago.cookbook.service;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
import edu.damago.cookbook.persistence.IngredientType;
import edu.damago.cookbook.persistence.Person;
import edu.damago.tool.ContentTypes;
import edu.damago.tool.HashCodes;
import edu.damago.tool.RestJpaLifecycleProvider;


@Path("ingredient-types")
public class IngredientTypeService {
	static private final String QUERY_TYPES = "select t.identity from IngredientType as t where "
		+ "(:minCreated is null or t.created >= :minCreated) and "
		+ "(:maxCreated is null or t.created <= :maxCreated) and "
		+ "(:minModified is null or t.modified >= :minModified) and "
		+ "(:maxModified is null or t.modified <= :maxModified) and "
		+ "(:alias is null or t.alias = :alias) and "
		+ "(:descriptionFragment is null or t.description like concat('%', :descriptionFragment, '%')) and "
		+ "(:pescatarian is null or t.pescatarian = :pescatarian) and "
		+ "(:lactoOvoVegetarian is null or t.lactoOvoVegetarian = :lactoOvoVegetarian) and "
		+ "(:lactoVegetarian is null or t.lactoVegetarian = :lactoVegetarian) and "
		+ "(:vegan is null or t.vegan = :vegan)";


	/**
	 * HTTP Signature: GET ingredient-types IN: - OUT: application/json
	 * @param resultOffset the result offset, or null for none
	 * @param resultSize the maximum result size, or null for none
	 * @param minCreated the minimum creation timestamp, or null for none
	 * @param minCreated the maximum creation timestamp, or null for none
	 * @param minModified the minimum modification timestamp, or null for none
	 * @param maxModified the maximum modification timestamp, or null for none
	 * @param alias the alias, or null for none
	 * @param descriptionFragment the description fragment, or null for none
	 * @param pescatarian the pescatarian value, or null for none
	 * @param lactoOvoVegetarian the lacto-ovo-vegetarian value, or null for none
	 * @param lactoVegetarian the lacto-vegetarian value, or null for none
	 * @param vegan the vegan value, or null for none
	 * @return the matching ingredient types as JSON
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public IngredientType[] queryIngredientTypes (
		@QueryParam("result-offset") @PositiveOrZero final Integer resultOffset,
		@QueryParam("result-size") @PositiveOrZero final Integer resultSize,
		@QueryParam("min-created") final Long minCreated,
		@QueryParam("max-created") final Long maxCreated,
		@QueryParam("min-modified") final Long minModified,
		@QueryParam("max-modified") final Long maxModified,
		@QueryParam("alias") final String alias,
		@QueryParam("description-fragment") final String descriptionFragment,
		@QueryParam("pescatarian") final Boolean pescatarian,
		@QueryParam("lacto-ovo-vegetarian") final Boolean lactoOvoVegetarian,
		@QueryParam("lacto-vegetarian") final Boolean lactoVegetarian,
		@QueryParam("vegan") final Boolean vegan
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final TypedQuery<Long> query = entityManager.createQuery(QUERY_TYPES, Long.class);
		if (resultOffset != null) query.setFirstResult(resultOffset);
		if (resultSize != null) query.setMaxResults(resultSize);
		query.setParameter("minCreated", minCreated);
		query.setParameter("maxCreated", maxCreated);
		query.setParameter("minModified", minModified);
		query.setParameter("maxModified", maxModified);
		query.setParameter("alias", alias);
		query.setParameter("descriptionFragment", descriptionFragment);
		query.setParameter("pescatarian", pescatarian);
		query.setParameter("lactoOvoVegetarian", lactoOvoVegetarian);
		query.setParameter("lactoVegetarian", lactoVegetarian);
		query.setParameter("vegan", vegan);

		final IngredientType[] types = query
			.getResultList()
			.stream()
			.map(identity -> entityManager.find(IngredientType.class, identity))
			.filter(type -> type != null)
			.sorted()
			.toArray(IngredientType[]::new);

		return types;
	}


	/**
	 * HTTP Signature: POST ingredient-types IN: application/json OUT: text/plain
	 * @param requesterIdentity the ID of the authenticated person
	 * @param typeTemplate the ingredient type template
	 * @return the ingredient type identity
	 * @throws ClientErrorException if there is a problem caused by the client
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public long insertOrUpdateIngredientType (
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@NotNull @Valid final IngredientType typeTemplate
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != Person.Group.ADMIN) throw new ClientErrorException(Status.FORBIDDEN);
		final boolean insertMode = typeTemplate.getIdentity() == 0;
		
		final IngredientType type;
		if (insertMode) {
			type = new IngredientType();

			final Document defaultAvatar = entityManager.find(Document.class, 1L);
			if (defaultAvatar == null) throw new ServerErrorException(Status.SERVICE_UNAVAILABLE);
			type.setAvatar(defaultAvatar);
		} else {
			type = entityManager.find(IngredientType.class, typeTemplate.getIdentity());
			if (type == null) throw new ClientErrorException(Status.NOT_FOUND);
		}

		type.setModified(System.currentTimeMillis());
		type.setVersion(typeTemplate.getVersion());
		type.setAlias(typeTemplate.getAlias());
		type.setDescription(typeTemplate.getDescription());
		type.setPescatarian(typeTemplate.isPescatarian());
		type.setLactoVegetarian(typeTemplate.isLactoOvoVegetarian());
		type.setLactoVegetarian(typeTemplate.isLactoVegetarian());
		type.setVegan(typeTemplate.isVegan());

		try {
			if (insertMode) entityManager.persist(type);
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

		return type.getIdentity();
	}


	/**
	 * HTTP Signature: GET ingredient-types/{id} IN: - OUT: application/json
	 * @param typeIdentity the ingredient type identity
	 * @return the ingredient type as JSON
	 * @throws ClientErrorException if there is no matching ingredient type (404)
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public IngredientType findIngredientType (
		@PathParam("id") @Positive final long typeIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final IngredientType type = entityManager.find(IngredientType.class, typeIdentity);
		if (type == null) throw new ClientErrorException(Status.NOT_FOUND);

		return type;
	}


	/**
	 * HTTP Signature: GET ingredient-types/{id}/avatar IN: - OUT: image/*
	 * @param acceptHeader the HTTP "Accept" header
	 * @param typeIdentity the ingredient type identity
	 * @return the matching ingredient type's avatar content
	 * @throws ClientErrorException if there is no matching ingredient type (404)
	 */
	@GET
	@Path("{id}/avatar")
	@Produces("image/*")
	public Response findIngredientTypeAvatar (
		@HeaderParam(HttpHeaders.ACCEPT) @NotNull @NotEmpty final String acceptHeader,
		@PathParam("id") @Positive final long typeIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final IngredientType type = entityManager.find(IngredientType.class, typeIdentity);
		if (type == null) throw new ClientErrorException(Status.NOT_FOUND);

		final Document avatar = type.getAvatar();
		if (!ContentTypes.isAcceptable(avatar.getType(), acceptHeader)) throw new ClientErrorException(Status.NOT_ACCEPTABLE);
		return Response.ok(avatar.getContent(), avatar.getType()).build();
	}


	@PUT
	@Path("{id}/avatar")
	@Consumes("image/*")
	public long updateIngredientTypeAvatar (
		@PathParam("id") @Positive final long typeIdentity,
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@HeaderParam(HttpHeaders.CONTENT_TYPE) @NotNull @NotEmpty final String documentType,
		@NotNull final byte[] documentContent
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final IngredientType type = entityManager.find(IngredientType.class, typeIdentity);
		if (type == null) throw new ClientErrorException(Status.NOT_FOUND);

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != Person.Group.ADMIN) throw new ClientErrorException(Status.FORBIDDEN);

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

		type.setAvatar(image);

		try {
			entityManager.flush();

			entityManager.getTransaction().commit();
		} finally {
			entityManager.getTransaction().begin();
		}

		return image.getIdentity();
	}
}

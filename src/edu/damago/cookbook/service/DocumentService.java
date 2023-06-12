package edu.damago.cookbook.service;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import edu.damago.cookbook.persistence.Document;
import edu.damago.cookbook.persistence.Person;
import edu.damago.tool.ContentTypes;
import edu.damago.tool.RestJpaLifecycleProvider;


/**
 * Set in MariaDB / MySQL to avoid RollbackExceptions when setting media content:<ul>
 * <li>mariadb> SET GLOBAL max_allowed_packet=16*1024*1024;</li>
 * <li>mariadb> quit;</li>
 * </ul>
 */
@Path("documents")
public class DocumentService {
	static private final String QUERY_DOCUMENTS = "select d.identity from Document as d where "
		+ "(:minCreated is null or d.created >= :minCreated) and "
		+ "(:maxCreated is null or d.created <= :maxCreated) and "
		+ "(:minModified is null or d.modified >= :minModified) and "
		+ "(:maxModified is null or d.modified <= :maxModified) and "
		+ "(:type is null or d.type = :type) and "
		+ "(:hash is null or d.hash = :hash) and "
		+ "(:minSize is null or length(d.content) >= :minSize) and "
		+ "(:maxSize is null or length(d.content) <= :maxSize)";


	/**
	 * HTTP Signature: GET documents IN: - OUT: application/json
	 * @param resultOffset the result offset, or null for none
	 * @param resultSize the maximum result size, or null for none
	 * @param minCreated the minimum creation timestamp, or null for none
	 * @param minCreated the maximum creation timestamp, or null for none
	 * @param minModified the minimum modification timestamp, or null for none
	 * @param maxModified the maximum modification timestamp, or null for none
	 * @param type the content type, or null for none
	 * @param hash the content hash, or null for none
	 * @param minSize the minimum content length, or null for none
	 * @param maxSize the maximum content length, or null for none
	 * @return the matching documents as JSON
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Document[] queryDocuments (
		@QueryParam("result-offset") @PositiveOrZero final Integer resultOffset,
		@QueryParam("result-size") @PositiveOrZero final Integer resultSize,
		@QueryParam("min-created") final Long minCreated,
		@QueryParam("max-created") final Long maxCreated,
		@QueryParam("min-modified") final Long minModified,
		@QueryParam("max-modified") final Long maxModified,
		@QueryParam("type") final String type,
		@QueryParam("hash") final String hash,
		@QueryParam("min-size") @PositiveOrZero final Long minSize,
		@QueryParam("max-size") @PositiveOrZero final Long maxSize
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final TypedQuery<Long> query = entityManager.createQuery(QUERY_DOCUMENTS, Long.class);
		if (resultOffset != null) query.setFirstResult(resultOffset);
		if (resultSize != null) query.setMaxResults(resultSize);
		query.setParameter("minCreated", minCreated);
		query.setParameter("maxCreated", maxCreated);
		query.setParameter("minModified", minModified);
		query.setParameter("maxModified", maxModified);
		query.setParameter("type", type);
		query.setParameter("hash", hash);
		query.setParameter("minSize", minSize);
		query.setParameter("maxSize", maxSize);

		final Document[] documents = query
			.getResultList()
			.stream()
			.map(identity -> entityManager.find(Document.class, identity))
			.filter(document -> document != null)
			.sorted()
			.toArray(Document[]::new);

		return documents;
	}


	/**
	 * HTTP Signature: POST documents IN: "* / *" OUT: text/plain
	 * @param requesterIdentity the ID of the authenticated person
	 * @param typeTemplate the ingredient type template
	 * @return the ingredient type identity
	 * @throws ClientErrorException if there is a problem caused by the client
	 */
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public long insertDocument (
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@HeaderParam(HttpHeaders.CONTENT_TYPE) @NotNull @NotEmpty final String documentType,
		@NotNull final byte[] documentContent
	) throws ClientErrorException {
		if (documentType.trim().equals(MediaType.APPLICATION_JSON)) throw new ClientErrorException(Status.UNSUPPORTED_MEDIA_TYPE);
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != Person.Group.ADMIN) throw new ClientErrorException(Status.FORBIDDEN);

		final Document document = new Document(documentType, documentContent);

		try {
			entityManager.persist(document);

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

		return document.getIdentity();
	}


	/**
	 * HTTP Signature: GET documents/{id} IN: - OUT: "* / *"
	 * @param acceptHeader the HTTP "Accept" header
	 * @param documentIdentity the document identity
	 * @return the document as JSON
	 * @throws ClientErrorException if there is no matching document (404)
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.WILDCARD)
	public Response findDocument (
		@HeaderParam(HttpHeaders.ACCEPT) @NotNull @NotEmpty final String acceptHeader,
		@PathParam("id") @Positive final long documentIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Document document = entityManager.find(Document.class, documentIdentity);
		if (document == null) throw new ClientErrorException(Status.NOT_FOUND);

		final ResponseBuilder builder;
		if (acceptHeader.equals(MediaType.APPLICATION_JSON)) {
			builder = Response.ok(document, MediaType.APPLICATION_JSON);
		} else {
			if (!ContentTypes.isAcceptable(document.getType(), acceptHeader)) throw new ClientErrorException(Status.NOT_ACCEPTABLE);
			builder = Response.ok(document.getContent(), document.getType());
		}

		return builder.build();
	}
}

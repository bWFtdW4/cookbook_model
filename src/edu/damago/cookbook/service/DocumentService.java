package edu.damago.cookbook.service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import edu.damago.cookbook.persistence.Document;
import edu.damago.tool.RestJpaLifecycleProvider;


@Path("documents")
public class DocumentService {
	
	static private final String QUERY_TYPES = "select t.identity from Document as t where "
		+ "(:minCreated is null or t.created >= :minCreated) and "
		+ "(:maxCreated is null or t.created <= :maxCreated) and "
		+ "(:minModified is null or t.modified >= :minModified) and "
		+ "(:maxModified is null or t.modified <= :maxModified) and "
		+ "(:type is null or t.type = :type) and "
		+ "(:hash is null or t.hash = :hash) and "
		+ "(:minSize is null or length(d.content) >= :minSize) and "
		+ "(:maxSize is null or length(d.content) <= :maxSize)"
		;


	/**
	 * HTTP Signature: GET documents IN: - OUT: application/json
	 * @param resultOffset the result offset, or null for none
	 * @param resultSize the maximum result size, or null for none
	 * @param minCreated the minimum creation timestamp, or null for none
	 * @param minCreated the maximum creation timestamp, or null for none
	 * @param minModified the minimum modification timestamp, or null for none
	 * @param maxModified the maximum modification timestamp, or null for none
	 * @param type the type, or null for none
	 * @param hash the hash, or null for none
	 * @param minSize the minimum content length, or null for none
	 * @param maxSize the maximum content length, or null for none
	 * @return documents as JSON
	 * @throws ClientErrorException if there is no matching ingredient type (404)
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Document[] queryDocument(
		@QueryParam("result-offset") @PositiveOrZero final Integer resultOffset,
		@QueryParam("result-size") @PositiveOrZero final Integer resultSize,
		@QueryParam("min-created") final Long minCreated,
		@QueryParam("max-created") final Long maxCreated,
		@QueryParam("min-modified") final Long minModified,
		@QueryParam("max-modified") final Long maxModified,
		@QueryParam("type") final String docType,
		@QueryParam("hash") final String hash,
		@QueryParam("min-size") @PositiveOrZero final Long minSize,
		@QueryParam("max-size") @PositiveOrZero final Long maxSize
		
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final TypedQuery<Long> query = entityManager.createQuery(QUERY_TYPES, Long.class);
		if (resultOffset != null) query.setFirstResult(resultOffset);
		if (resultSize != null) query.setMaxResults(resultSize);
		query.setParameter("minCreated", minCreated);
		query.setParameter("maxCreated", maxCreated);
		query.setParameter("minModified", minModified);
		query.setParameter("maxModified", maxModified);
		query.setParameter("type", docType);
		query.setParameter("hash", hash);
		query.setParameter("minSize", minSize);
		query.setParameter("maxSize", maxSize);

		final Document[] types = query
			.getResultList()
			.stream()
			.map(identity -> entityManager.find(Document.class, identity))
			.filter(type -> type != null)
			.sorted()
			.toArray(Document[]::new);

		return types;
	}


	/**
	 * HTTP Signature: GET documents/<id> IN: - OUT: application/json
	 * @param documentIdentity the document identity
	 * @return the document as JSON
	 * @throws ClientErrorException if there is no matching document (404)
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Document findDocument (
		@PathParam("id") @Positive final long documentIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Document document = entityManager.find(Document.class, documentIdentity);
		if (document == null) throw new ClientErrorException(Status.NOT_FOUND);

		return document;
	}
}

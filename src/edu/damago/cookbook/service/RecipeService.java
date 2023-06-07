package edu.damago.cookbook.service;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
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
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import edu.damago.cookbook.persistence.Document;
import edu.damago.cookbook.persistence.Person;
import edu.damago.cookbook.persistence.Recipe;
import edu.damago.cookbook.persistence.Recipe.Category;
import edu.damago.tool.RestJpaLifecycleProvider;


@Path("recipes")
public class RecipeService {
	
	static private final String QUERY_TYPES = "select r.identity from Recipe as r where "
		+ "(:minCreated is null or r.created >= :minCreated) and "
		+ "(:maxCreated is null or r.created <= :maxCreated) and "
		+ "(:minModified is null or r.modified >= :minModified) and "
		+ "(:maxModified is null or r.modified <= :maxModified) and "
		+ "(:title is null or r.title = :title) and "
		+ "(:descriptionFragment is null or r.description like concat('%', :descriptionFragment, '%')) and "
		+ "(:category is null or r.category = :category) and "
		+ "(:description is null or r.description = :description) and "
		+ "(:instruction is null or r.instruction = :instruction) and "
		+ "(:instructionFragment is null or r.instruction like concat('%', :instructionFragment, '%')) and "
		+ "(:pescatarian is null or (true = all(select i.type.pescatarian from r.ingredients as i)) = :pescatarian) and "
		+ "(:lactoOvoVegetarian is null or (true = all(select i.type.lactoOvoVegetarian from r.ingredients as i)) = :lactoOvoVegetarian) and "
		+ "(:lactoVegetarian is null or (true = all(select i.type.lactoVegetarian from r.ingredients as i)) = :lactoVegetarian) and "
		+ "(:vegan is null or (true = all(select i.type.vegan from r.ingredients as i)) = :vegan)";
;
		
	
	/**
	 * HTTP Signature: GET Recipe IN: - OUT: application/json
	 * @param resultOffset the result offset, or null for none
	 * @param resultSize the maximum result size, or null for none
	 * @param minCreated the minimum creation timestamp, or null for none
	 * @param minCreated the maximum creation timestamp, or null for none
	 * @param minModified the minimum modification timestamp, or null for none
	 * @param maxModified the maximum modification timestamp, or null for none
	 * @param title the title, or null for none
	 * @param descriptionFragment the description fragment, or null for none
	 * @parm category the category, or null for none
	 * @parm description, the description, or null for none
	 * @parm instructionFragment, the instruction fragment, or null for none
	 * @parm pescatarian
	 * @parm lactoOvoVegetarian
	 * @parm lactoVegetarian
	 * @parm lactoVegetarian
	 * @parm ingredients the ingredients´, or null for none
	 * @return the Recipe as JSON
	 * 
	 * @throws ClientErrorException if there is no matching Recipe (404)
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Recipe[] queryRecipe (
		@QueryParam("result-offset") @PositiveOrZero final Integer resultOffset,
		@QueryParam("result-size") @PositiveOrZero final Integer resultSize,
		@QueryParam("min-created") final Long minCreated,
		@QueryParam("max-created") final Long maxCreated,
		@QueryParam("min-modified") final Long minModified,
		@QueryParam("max-modified") final Long maxModified,
		@QueryParam("title") final String title,
		@QueryParam("description-fragment") final String descriptionFragment,
		@QueryParam("category") final Category category,
		@QueryParam("description") final String description,
		@QueryParam("instruction") final String instruction,
		@QueryParam("instructionFragment") final String instructionFragment,
		@QueryParam("pescatarian") final Boolean pescatarian,
		@QueryParam("lactoOvoVegetarian") final Boolean lactoOvoVegetarian,
		@QueryParam("lactoVegetarian") final Boolean lactoVegetarian,
		@QueryParam("vegan") final Boolean vegan
		
		
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final TypedQuery<Long> query = entityManager.createQuery(QUERY_TYPES, Long.class);
		if (resultOffset != null) query.setFirstResult(resultOffset);
		if (resultSize != null) query.setMaxResults(resultSize);
		query.setParameter("minCreated", minCreated);
		query.setParameter("maxCreated", maxCreated);
		query.setParameter("minModified", minModified);
		query.setParameter("maxModified", maxModified);
		query.setParameter("title", title);
		query.setParameter("descriptionFragment", descriptionFragment);
		query.setParameter("category", category);
		query.setParameter("description", description);
		query.setParameter("instruction", instruction);
		query.setParameter("instructionFragment", instructionFragment);
		query.setParameter("pescatarian", pescatarian);
		query.setParameter("lactoOvoVegetarian", lactoOvoVegetarian);
		query.setParameter("lactoVegetarian", lactoVegetarian);
		query.setParameter("vegan", vegan);

	
		final Recipe[] types = query
			.getResultList()
			.stream()
			.map(identity -> entityManager.find(Recipe.class, identity))
			.filter(type -> type != null)
			.sorted()
			.toArray(Recipe[]::new);

		return types;
	}
		
		
	

	/**
	 * HTTP Signature: GET recipes/<id> IN: - OUT: application/json
	 * @param recipeIdentity the recipe identity
	 * @return the recipe as JSON
	 * @throws ClientErrorException if there is no matching recipe (404)
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Recipe findRecipe (
		@PathParam("id") @Positive final long recipeIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);

		return recipe;
	}
	
	
	/**
	 * HTTP Signature: POST <entity-path> IN: application/json OUT: text/plain
	 * @param requesterIdentity the ID of the authenticated person
	 * @param recipeTemplate the recipe type template
	 * @return the recipe type ID
	 * @throws ClientErrorException if there is a problem caused by the client
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
		public long insertOrUpdateType (
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@NotNull @Valid final Recipe recipeTemplate 
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != Person.Group.ADMIN) throw new ClientErrorException(Status.FORBIDDEN);
		final boolean insertMode = recipeTemplate.getIdentity() == 0;
		
		final Recipe recipe;
		if (insertMode) {
			recipe = new Recipe();
			
			final Document defaultAvatar = entityManager.find(Document.class, 1L);
			if (defaultAvatar == null) throw new ServerErrorException(Status.SERVICE_UNAVAILABLE);
			recipe.setAvatar(defaultAvatar);
			recipe.setOwner(requester);
			
		} else {
			recipe = entityManager.find(Recipe.class, recipeTemplate.getIdentity());
			if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		}
		
		recipe.setModified(System.currentTimeMillis());
		recipe.setVersion(recipeTemplate.getVersion());
		recipe.setTitle(recipeTemplate.getTitle());
		recipe.setCategory(recipeTemplate.getCategory());
		recipe.setDescription(recipeTemplate.getDescription());
		recipe.setInstruction(recipeTemplate.getInstruction());

		if (insertMode)
			entityManager.persist(recipe);
		else
			entityManager.flush();

		try {
			entityManager.getTransaction().commit();
		} catch (final RollbackException e) {
			throw new ClientErrorException(Status.CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}

		// whenever our modifications cause the mirror relationship set of an entity
		// to be modified, we need to remove said entity from the 2nd level case
		// -> cache eviction!
		// final Cache cache = entityManager.getEntityManagerFactory().getCache();
		// cache.evict(BaseEntity.class, 42L);

		return recipe.getIdentity();
	}
	

}
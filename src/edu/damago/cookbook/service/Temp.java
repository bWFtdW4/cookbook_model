package edu.damago.cookbook.service;

import java.util.Set;
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
import edu.damago.cookbook.persistence.Ingredient;
import edu.damago.cookbook.persistence.IngredientType;
import edu.damago.cookbook.persistence.Person;
import edu.damago.cookbook.persistence.Recipe;
import edu.damago.cookbook.persistence.Recipe.Category;
import edu.damago.tool.RestJpaLifecycleProvider;


@Path("recipes")
public class Temp {
public class RecipeService {
	
	static private final String QUERY_TYPES = "select t.identity from Recipe as t where "
//		+ "(:minCreated is null or t.created >= :minCreated) and "
//		+ "(:maxCreated is null or t.created <= :maxCreated) and "
//		+ "(:minModified is null or t.modified >= :minModified) and "
//		+ "(:maxModified is null or t.modified <= :maxModified) and "
//		+ "(:alias is null or t.alias = :alias) and "
//		+ "(:descriptionFragment is null or t.description like concat('%', :descriptionFragment, '%')) and "
//		+ "(:pescatarian is null or t.pescatarian = :pescatarian) and "
//		+ "(:lactoOvoVegetarian is null or t.lactoOvoVegetarian = :lactoOvoVegetarian) and "
//		+ "(:lactoVegetarian is null or t.lactoVegetarian = :lactoVegetarian) and "
//		+ "(:vegan is null or t.vegan = :vegan) and "
//		+ "(:title is null or t.title = :title) and "
//		+ "(:category is null or t.category = :category) and "
//		+ "(:description is null or t.description = :description) and "
//		+ "(:instruction is null or t.instruction = :instruction) and "
		+ "(:owner is null or t.owner = :owner) and "
		+ "(:ingredients is null or t.ingredients = :ingredients)"
		;


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
	 * @return the ingredient type as JSON
	 * @throws ClientErrorException if there is no matching ingredient type (404)
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public IngredientType[] queryIngredientTypes (
//		@QueryParam("result-offset") @PositiveOrZero final Integer resultOffset,
//		@QueryParam("result-size") @PositiveOrZero final Integer resultSize,
//		@QueryParam("min-created") final Long minCreated,
//		@QueryParam("max-created") final Long maxCreated,
//		@QueryParam("min-modified") final Long minModified,
//		@QueryParam("max-modified") final Long maxModified,
//		@QueryParam("alias") final String alias,
//		@QueryParam("description-fragment") final String descriptionFragment,
//		@QueryParam("pescatarian") final Boolean pescatarian,
//		@QueryParam("lacto-ovo-vegetarian") final Boolean lactoOvoVegetarian,
//		@QueryParam("lacto-vegetarian") final Boolean lactoVegetarian,
//		@QueryParam("vegan") final Boolean vegan,
//		@QueryParam("title") final String title,
//		@QueryParam("category") final Category category,
//		@QueryParam("description") final String description,
//		@QueryParam("instruction") final String instruction,
		@QueryParam("owner") final Person owner,
		@QueryParam("ingredients") final Set<Ingredient> ingredients
		
		
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final TypedQuery<Long> query = entityManager.createQuery(QUERY_TYPES, Long.class);
//		if (resultOffset != null) query.setFirstResult(resultOffset);
//		if (resultSize != null) query.setMaxResults(resultSize);
//		query.setParameter("minCreated", minCreated);
//		query.setParameter("maxCreated", maxCreated);
//		query.setParameter("minModified", minModified);
//		query.setParameter("maxModified", maxModified);
//		query.setParameter("alias", alias);
//		query.setParameter("descriptionFragment", descriptionFragment);
//		query.setParameter("pescatarian", pescatarian);
//		query.setParameter("lactoOvoVegetarian", lactoOvoVegetarian);
//		query.setParameter("lactoVegetarian", lactoVegetarian);
//		query.setParameter("title", title);
//		query.setParameter("category", category);
//		query.setParameter("description", description);
//		query.setParameter("instruction", instruction);
		query.setParameter("owner", owner);
		query.setParameter("ingredients",ingredients);
		
		


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
}
}

package edu.damago.cookbook.service;

import javax.persistence.EntityManager;
import javax.validation.constraints.Positive;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import edu.damago.cookbook.persistence.Recipe;
import edu.damago.tool.RestJpaLifecycleProvider;


@Path("recipes")
public class RecipeService {

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

package edu.damago.cookbook.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
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
import edu.damago.cookbook.persistence.Ingredient;
import edu.damago.cookbook.persistence.Ingredient.Unit;
import edu.damago.cookbook.persistence.IngredientType;
import edu.damago.cookbook.persistence.Person;
import edu.damago.cookbook.persistence.Recipe;
import edu.damago.tool.ContentTypes;
import edu.damago.tool.HashCodes;
import edu.damago.tool.JSON;
import edu.damago.tool.RestJpaLifecycleProvider;


@Path("recipes")
public class RecipeService {
	static private final String QUERY_RECIPES = "select r.identity from Recipe as r left outer join r.owner as o where "
		+ "(:minCreated is null or r.created >= :minCreated) and "
		+ "(:maxCreated is null or r.created <= :maxCreated) and "
		+ "(:minModified is null or r.modified >= :minModified) and "
		+ "(:maxModified is null or r.modified <= :maxModified) and "
		+ "(:title is null or r.title = :title) and "
		+ "(:category is null or r.category = :category) and "
		+ "(:descriptionFragment is null or r.description like concat('%', :descriptionFragment, '%')) and "
		+ "(:instructionFragment is null or r.instruction like concat('%', :instructionFragment, '%')) and "
		+ "(:pescatarian is null or (true = all(select i.type.pescatarian from r.ingredients as i)) = :pescatarian) and "
		+ "(:lactoOvoVegetarian is null or (true = all(select i.type.lactoOvoVegetarian from r.ingredients as i)) = :lactoOvoVegetarian) and "
		+ "(:lactoVegetarian is null or (true = all(select i.type.lactoVegetarian from r.ingredients as i)) = :lactoVegetarian) and "
		+ "(:vegan is null or (true = all(select i.type.vegan from r.ingredients as i)) = :vegan) and "
		+ "(:ownerEmail is null or (o.email = :ownerEmail))";


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
	public Recipe[] queryRecipes (
		@QueryParam("result-offset") @PositiveOrZero final Integer resultOffset,
		@QueryParam("result-size") @PositiveOrZero final Integer resultSize,
		@QueryParam("min-created") final Long minCreated,
		@QueryParam("max-created") final Long maxCreated,
		@QueryParam("min-modified") final Long minModified,
		@QueryParam("max-modified") final Long maxModified,
		@QueryParam("title") final String title,
		@QueryParam("category") final Recipe.Category category,
		@QueryParam("description-fragment") final String descriptionFragment,
		@QueryParam("instruction-fragment") final String instructionFragment,
		@QueryParam("pescatarian") final Boolean pescatarian,
		@QueryParam("lacto-ovo-vegetarian") final Boolean lactoOvoVegetarian,
		@QueryParam("lacto-vegetarian") final Boolean lactoVegetarian,
		@QueryParam("vegan") final Boolean vegan,
		@QueryParam("owner-email") @Email final String ownerEmail
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final TypedQuery<Long> query = entityManager.createQuery(QUERY_RECIPES, Long.class);
		if (resultOffset != null) query.setFirstResult(resultOffset);
		if (resultSize != null) query.setMaxResults(resultSize);
		query.setParameter("minCreated", minCreated);
		query.setParameter("maxCreated", maxCreated);
		query.setParameter("minModified", minModified);
		query.setParameter("maxModified", maxModified);
		query.setParameter("title", title);
		query.setParameter("category", category);
		query.setParameter("descriptionFragment", descriptionFragment);
		query.setParameter("instructionFragment", instructionFragment);
		query.setParameter("pescatarian", pescatarian);
		query.setParameter("lactoOvoVegetarian", lactoOvoVegetarian);
		query.setParameter("lactoVegetarian", lactoVegetarian);
		query.setParameter("vegan", vegan);
		query.setParameter("ownerEmail", ownerEmail);

		final Recipe[] recipes = query
			.getResultList()
			.stream()
			.map(identity -> entityManager.find(Recipe.class, identity))
			.filter(recipe -> recipe != null)
			.sorted()
			.toArray(Recipe[]::new);

		return recipes;
	}


	/**
	 * HTTP Signature: POST recipes IN: application/json OUT: text/plain
	 * @param requesterIdentity the ID of the authenticated person
	 * @param recipeTemplate the recipe template
	 * @return the recipe identity
	 * @throws ClientErrorException if there is a problem caused by the client
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public long insertOrUpdateRecipe (
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@NotNull @Valid final Recipe recipeTemplate
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
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
			if (recipe.getOwner() != null && recipe.getOwner().getIdentity() != requester.getIdentity() && requester.getGroup() != Person.Group.ADMIN) throw new ClientErrorException(Status.FORBIDDEN);
		}

		recipe.setModified(System.currentTimeMillis());
		recipe.setVersion(recipeTemplate.getVersion());
		recipe.setTitle(recipeTemplate.getTitle());
		recipe.setCategory(recipeTemplate.getCategory());
		recipe.setDescription(recipeTemplate.getDescription());
		recipe.setInstruction(recipeTemplate.getInstruction());

		try {
			if (insertMode) entityManager.persist(recipe);
			else entityManager.flush();

			entityManager.getTransaction().commit();
		} catch (final RollbackException e) {
			throw new ClientErrorException(Status.CONFLICT);
		} finally {
			entityManager.getTransaction().begin();
		}

		// whenever our modifications cause the mirror relationship set of an entity to be
		// modified, we need to remove said entity from the 2nd level case -> cache eviction!
		final Cache cache = entityManager.getEntityManagerFactory().getCache();
		cache.evict(Person.class, requester.getIdentity());

		return recipe.getIdentity();
	}


	/**
	 * HTTP Signature: GET recipes/{id} IN: - OUT: application/json
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
	 * HTTP Signature: GET recipes/{id}/avatar IN: - OUT: image/*
	 * @param acceptHeader the HTTP "Accept" header
	 * @param recipeIdentity the recipe identity
	 * @return the matching recipe's avatar content
	 * @throws ClientErrorException if there is no matching recipe (404)
	 */
	@GET
	@Path("{id}/avatar")
	@Produces("image/*")
	public Response findRecipeAvatar (
		@HeaderParam(HttpHeaders.ACCEPT) @NotNull @NotEmpty final String acceptHeader,
		@PathParam("id") @Positive final long recipeIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);

		final Document avatar = recipe.getAvatar();
		if (!ContentTypes.isAcceptable(avatar.getType(), acceptHeader)) throw new ClientErrorException(Status.NOT_ACCEPTABLE);
		return Response.ok(avatar.getContent(), avatar.getType()).build();
	}


	@PUT
	@Path("{id}/avatar")
	@Consumes("image/*")
	public long updateRecipeAvatar (
		@PathParam("id") @Positive final long recipeIdentity,
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@HeaderParam(HttpHeaders.CONTENT_TYPE) @NotNull @NotEmpty final String documentType,
		@NotNull final byte[] documentContent
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || (recipe.getOwner() != null && requester.getIdentity() != recipe.getOwner().getIdentity() && requester.getGroup() != Person.Group.ADMIN)) throw new ClientErrorException(Status.FORBIDDEN);

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

		recipe.setAvatar(image);

		try {
			entityManager.flush();

			entityManager.getTransaction().commit();
		} finally {
			entityManager.getTransaction().begin();
		}

		return image.getIdentity();
	}


	@GET
	@Path("{id}/ingredients")
	@Produces(MediaType.APPLICATION_JSON)
	public Ingredient[] queryRecipeIngredients (
		@PathParam("id") @Positive final long recipeIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);

		return recipe.getIngredients().stream().sorted().toArray(Ingredient[]::new);
	}


	@PUT
	@Path("{id}/ingredients")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public long updateRecipeIngredients (
		@PathParam("id") @Positive final long recipeIdentity,
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@NotBlank final String json
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || (recipe.getOwner() != null && requester.getIdentity() != recipe.getOwner().getIdentity() && requester.getGroup() != Person.Group.ADMIN)) throw new ClientErrorException(Status.FORBIDDEN);

		final List<Map<String,Object>> maps;
		try {
			maps = JSON.parse(json);
		} catch (final IllegalArgumentException | ClassCastException e) {
			throw new ClientErrorException(Status.BAD_REQUEST);
		}

		final Set<Ingredient> ingredients = new HashSet<>();
		try  {
			for (final Map<String,Object> map : maps) {
				final long ingredientIdentity = ((Number) map.get("identity")).longValue();
				final long ingredientTypeReference = ((Number) map.get("typeReference")).longValue();
				if (ingredientTypeReference == 0) throw new ClientErrorException(Status.BAD_REQUEST);

				final IngredientType ingredientType = entityManager.find(IngredientType.class, ingredientTypeReference);
				if (ingredientType == null) throw new ClientErrorException(Status.NOT_FOUND);

				final Ingredient ingredient;
				if (ingredientIdentity == 0) {
					ingredient = new Ingredient(recipe);
				} else {
					ingredient = entityManager.find(Ingredient.class, ingredientIdentity);
					if (ingredient == null) throw new ClientErrorException(Status.NOT_FOUND);
				}

				ingredients.add(ingredient);
				ingredient.setModified(System.currentTimeMillis());
				ingredient.setVersion(((Number) map.get("version")).intValue());
				ingredient.setAmount(((Number) map.get("amount")).floatValue());
				ingredient.setUnit(Unit.valueOf((String) map.get("unit")));
				ingredient.setType(ingredientType);
			}
		} catch (final NullPointerException | IllegalArgumentException | ClassCastException e) {
			throw new ClientErrorException(Status.BAD_REQUEST);
		}

		try {
			ingredients.stream().filter(ingredient -> ingredient.getIdentity() == 0).forEach(ingredient -> entityManager.persist(ingredient));
			recipe.getIngredients().stream().filter(ingredient -> !ingredients.contains(ingredient)).forEach(ingredient -> entityManager.remove(ingredient));
			entityManager.flush();

			entityManager.getTransaction().commit();
		} catch (final RollbackException e) {
			throw new ClientErrorException(Status.CONFLICT);
		} finally {
			if (!entityManager.getTransaction().isActive()) entityManager.getTransaction().begin();
		}

		// whenever our modifications cause the mirror relationship set of an entity to be
		// modified, we need to remove said entity from the 2nd level case -> cache eviction!
		final Cache cache = entityManager.getEntityManagerFactory().getCache();
		cache.evict(Recipe.class, recipe.getIdentity());

		return recipe.getIdentity();
	}


	@GET
	@Path("{id}/illustration-references")
	@Produces(MediaType.APPLICATION_JSON)
	public long[] queryRecipeIllustrationReferences (
		@PathParam("id") @Positive final long recipeIdentity
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);

		return recipe.getIllustrations().stream().mapToLong(Document::getIdentity).sorted().toArray();
	}


	@PUT
	@Path("{id}/illustration-references")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public long updateRecipeIllustrationReferences (
		@PathParam("id") @Positive final long recipeIdentity,
		@HeaderParam(BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@NotNull long[] illustrationReferences
	) throws ClientErrorException {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null || (recipe.getOwner() != null && requester.getIdentity() != recipe.getOwner().getIdentity() && requester.getGroup() != Person.Group.ADMIN)) throw new ClientErrorException(Status.FORBIDDEN);

		final Set<Document> documents = LongStream
			.of(illustrationReferences)
			.mapToObj(reference -> entityManager.find(Document.class, reference))
			.filter(document -> document != null)
			.collect(Collectors.toSet());

		recipe.getIllustrations().retainAll(documents);
		recipe.getIllustrations().addAll(documents);

		try {
			entityManager.flush();

			entityManager.getTransaction().commit();
		} finally {
			entityManager.getTransaction().begin();
		}

		return recipe.getIdentity();
	}
}

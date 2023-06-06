
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
	import edu.damago.cookbook.persistence.Recipe;
	import edu.damago.cookbook.persistence.Recipe.Category;
	import edu.damago.tool.RestJpaLifecycleProvider;


	@Path("people")
	public class Temp {
	public class PersonService {
		
		
		static private final String QUERY_TYPES = "select t.identity from Recipe as t where "
			+ "(:minCreated is null or t.created >= :minCreated) and "
			+ "(:maxCreated is null or t.created <= :maxCreated) and "
			+ "(:minModified is null or t.modified >= :minModified) and "
			+ "(:maxModified is null or t.modified <= :maxModified) and "
			+ "(:title is null or t.title = :title) and "
			+ "(:descriptionFragment is null or t.description like concat('%', :descriptionFragment, '%')) and "
			+ "(:category is null or t.category = :category) and "
			+ "(:description is null or t.description = :description) and "
			+ "(:instruction is null or t.instruction = :instruction) and "
			+ "(:instructionFragment is null or t.instruction like concat('%', :instructionFragment, '%'))"
			
			+" :email "
			+" :group "
			+" :family "
			+" :given "
			+" :street "
			+" :city "
			+" :country "
			+" :postcode "
			+" :phones? "
			
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
		 * 
		 * @parm ingredients the ingredients´, or null for none
		 * @return the Recipe as JSON
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
			@QueryParam("instructionFragment") final String instructionFragment
			
			
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
}
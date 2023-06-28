package edu.damago.cookbook.service;

import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Positive;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import edu.damago.cookbook.persistence.Animal;
import edu.damago.cookbook.persistence.Person;
import edu.damago.tool.RestJpaLifecycleProvider;


@Path("dex")
public class AnimalService {

	static private final String QUERY_DEX = "SELECT a FROM Animal as a Where "
		+ "(:dexNr is null or a.dexNr = :dexNr) and " 
		+ "(:nameEn is null or a.nameEn = :nameEn) and " 
		+ "(:nameDe is null or a.nameDe = :nameDe)"
		;
	static private final String QUERY_DEX1 = "Select a.id from Animal as a WHERE "
		+ "(:dexNr is null or a.dexNr = :dexNr) and " 
		+ "(:nameEn is null or a.nameEn = :nameEn) and " 
		+ "(:nameDe is null or a.nameDe = :nameDe)"
		;
	
	static private final String QUERY_DEX3 = "Select id from dex_main"
		+ "(:dexNr is null or a.dexNr = :dexNr) and " 
		+ "(:nameEn is null or a.nameEn = :nameEn) and " 
		+ "(:nameDe is null or a.nameDe = :nameDe)"
		;
		

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getStatus () {
		return "There should be a list here...\nNot sure where it went...";
	}


	@GET
	@Path("test")
	@Produces(MediaType.APPLICATION_JSON)
	public String getResource () {
		return "BeepBoop TEST TEST all system JAX-RS system running!";
	}


	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Animal findAnimalById (@Positive @PathParam("id") int id) {
		if (id <= 0) throw new ClientErrorException("Invalid ID. It must be greater than 0", 400);
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");		
		
		final Animal query = entityManager.find(Animal.class, id);
		if (query == null) throw new ClientErrorException("No Animal found with ID: " + id, 404);

		return query;
	}

	
	@GET
	@Path("all-1")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Integer> queryAnimal1 (
		@QueryParam("dexNr") final Integer dexNr,
		@QueryParam("nameEn") final String nameEn, 
		@QueryParam("nameDe") final String nameDe
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final String QUERY_DEX1 = "SELECT a.id FROM Animal as a WHERE " 
			+ "(:dexNr is null or a.dexNr = :dexNr) and " 
			+ "(:nameEn is null or a.nameEn = :nameEn) and " 
			+ "(:nameDe is null or a.nameDe = :nameDe)"
			;
		
		final TypedQuery<Integer> query = entityManager.createQuery(QUERY_DEX1, Integer.class);
		query.setParameter("dexNr", dexNr);
		query.setParameter("nameEn", nameEn);
		query.setParameter("nameDe", nameDe);

		final List<Integer> animals = query.getResultList();
		return animals;
	}
	
	@GET
	@Path("all-2")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Integer> queryAnimal2 (
		@QueryParam("nameEn") final String nameEn, 
		@QueryParam("nameDe") final String nameDe
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final String QUERY_DEX2 = "SELECT a.nameEn FROM Animal as a WHERE " 
			+ "(:nameEn is null or a.nameEn = :nameEn) and " 
			+ "(:nameDe is null or a.nameDe = :nameDe)"
			;
		
		final TypedQuery<Integer> query = entityManager.createQuery(QUERY_DEX2, Integer.class);
		query.setParameter("nameEn", nameEn);
		query.setParameter("nameDe", nameDe);

		final List<Integer> animals = query.getResultList();
		return animals;
	}
	
	@GET
	@Path("all3")
	@Produces(MediaType.APPLICATION_JSON)
	public Animal[] queryAnimal3 (
	    @QueryParam("dexNr") final Integer dexNr,
	    @QueryParam("nameEn") final String nameEn, 
	    @QueryParam("nameDe") final String nameDe
	) {
	    final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

	    final String QUERY_ANIMAL3 = "SELECT a FROM Animal a";

	    final TypedQuery<Animal> query = entityManager.createQuery(QUERY_ANIMAL3, Animal.class);

	    final Animal[] animals = query
	        .getResultList()
	        .toArray(new Animal[0]);
	    return animals;
	}



}
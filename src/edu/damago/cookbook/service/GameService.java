package edu.damago.cookbook.service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import edu.damago.cookbook.persistence.GameEpic;
import edu.damago.cookbook.persistence.GameGFN;
import edu.damago.cookbook.persistence.GameSteam;
import edu.damago.tool.RestJpaLifecycleProvider;

@Path("games")
public class GameService {

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
	@Path("gfn-all")
	@Produces(MediaType.APPLICATION_JSON)
	public GameGFN[] queryAllGfnGames (
	    @QueryParam("name") final String name
	) {
	    final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

	    final String QUERY = "SELECT g FROM GameGFN g";
	    
	    final TypedQuery<GameGFN> query = entityManager.createQuery(QUERY, GameGFN.class);

	    final GameGFN[] games = query
	        .getResultList()
	        .toArray(new GameGFN[0]);
	    return games;
	}
	

	@GET
	@Path("epic-all")
	@Produces(MediaType.APPLICATION_JSON)
	public GameEpic[] queryAllEpicGames (
	    @QueryParam("name") final String name
	) {
	    final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

	    final String QUERY = "SELECT g FROM GameEpic g";
	    
	    final TypedQuery<GameGFN> query = entityManager.createQuery(QUERY, GameGFN.class);

	    final GameEpic[] games = query
	        .getResultList()
	        .toArray(new GameEpic[0]);
	    return games;
	}
	
	@GET
	@Path("steam-all")
	@Produces(MediaType.APPLICATION_JSON)
	public GameSteam[] queryAllSteamGames (
	    @QueryParam("name") final String name
	) {
	    final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

	    final String QUERY = "SELECT g FROM GameSteam g";
	    
	    final TypedQuery<GameSteam> query = entityManager.createQuery(QUERY, GameSteam.class);

	    final GameSteam[] games = query
	        .getResultList()
	        .toArray(new GameSteam[0]);
	    return games;
	}

	@GET
	@Path("gfn")
	@Produces(MediaType.APPLICATION_JSON)
	public GameGFN[] queryGFNGames (
	    @QueryParam("name") final String name
	) {
	    final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

	    //final String QUERY = "SELECT g.Name FROM gfn g JOIN epic e ON e.Name = g.Name";
	    //final String QUERY = "SELECT g FROM GameGFN g JOIN GameEpic e on g.name = e.name";
	    final String QUERY = "SELECT g FROM GameGFN g JOIN GameEpic e on g.name = e.name "
	    	+ "UNION "
	    	+ "SELECT g FROM GameGFN g JOIN GameSteam s on g.name = s.name";
	    
	    final TypedQuery<GameGFN> query = entityManager.createQuery(QUERY, GameGFN.class);

	    final GameGFN[] games = query
	        .getResultList()
	        .toArray(new GameGFN[0]);
	    return games;
	}

}
package edu.damago.cookbook.service;


import java.util.List;
import javax.persistence.EntityManager;
import javax.validation.constraints.Positive;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import edu.damago.tool.RestJpaLifecycleProvider;



		@Path("books")
		public class Temp {
		public class BookService {

		    @GET
		    @Path("resource")
		    @Produces(MediaType.APPLICATION_JSON)
		    public String getResource () {
		        return "Hello, JAX-RS!";
		    }
/**

		    @GET
		    @Path("{id}")
		    @Produces(MediaType.APPLICATION_JSON)
		    public Book findBookById (@Positive @PathParam("id") int id) {
		        if (id <= 0) throw new ClientErrorException("Invalid ID. It must be greater than 0", 400);

		        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		        final Book book = entityManager.find(Book.class, id);
		        if (book == null) throw new ClientErrorException("No book found with ID: " + id, 404);

		        return book;
		    }


		    @GET
		    @Produces(MediaType.APPLICATION_JSON)
		    public List<Book> queryBooks (@QueryParam("title") final String title) {
		        if (title != null && title.isBlank()) throw new ClientErrorException("Title cannot be blank", 400);

		        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		        final List<Book> searchedBooks = entityManager
		            .createQuery("SELECT b FROM Book b WHERE (:title IS NULL OR b.title LIKE :title)", Book.class)
		            .setParameter("title", title == null ? null : "%" + title + "%")
		            .getResultList();

		        return searchedBooks;
		    }

		    @POST
		    @Consumes(MediaType.APPLICATION_JSON)
		    @Produces(MediaType.TEXT_PLAIN)
		    public int createBook (final Book book) {
		        if (book == null || book.getTitle().isBlank()) throw new ClientErrorException("Book title cannot be blank", 400);

		        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		        entityManager.persist(book);
		    
		        try {
		            entityManager.getTransaction().commit();
		            
		        } finally {
		            entityManager.getTransaction().begin();
		        }

		        

		        return book.getId();
 **/
		    }

		}
		

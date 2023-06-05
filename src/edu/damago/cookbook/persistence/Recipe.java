package edu.damago.cookbook.persistence;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import edu.damago.tool.Copyright;
import edu.damago.tool.JsonProtectedPropertyStrategy;


/**
 * Instances of this class model recipe entities.
 */
@Entity
@Table(schema = "cookbook", name = "Recipe")
@PrimaryKeyJoinColumn(name = "recipeIdentity")
@DiscriminatorValue("Recipe")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@Copyright(year = 2022, holders = "Sascha Baumeister")
public class Recipe extends BaseEntity {
	static public enum Category { MAIN_COURSE, APPETIZER, SNACK, DESSERT, BREAKFAST, BUFFET, BARBEQUE, ADOLESCENT, INFANT }


	@NotNull @Size(max = 128)
	@Column(nullable = false, updatable = true, length = 128, unique = true)
	private String title;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = true)
	private Category category;

	@Size(max = 4094)
	@Column(nullable = true, updatable = true, length = 4094)
	private String description;

	@Size(max = 4094)
	@Column(nullable = true, updatable = true, length = 4094)
	private String instruction;

	// avoid @NotNull with @ManyToOne!
	@ManyToOne(optional = false)
	@JoinColumn(name = "avatarReference", nullable = false, updatable = true)
	private Document avatar;

	@ManyToOne(optional = true)
	@JoinColumn(name = "ownerReference", nullable = true, updatable = true)
	private Person owner;

	@NotNull
	@OneToMany(mappedBy = "recipe", cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE })
	private Set<Ingredient> ingredients;

	@NotNull
	@ManyToMany
	@JoinTable(
		schema = "cookbook",
		name = "RecipeIllustrationAssociation",
		joinColumns = @JoinColumn(name = "recipeReference", nullable = false, updatable = false, insertable = true),
		inverseJoinColumns = @JoinColumn(name = "documentReference", nullable = false, updatable = false, insertable = true),
		uniqueConstraints = @UniqueConstraint(columnNames = { "recipeReference", "documentReference" })
	)
	private Set<Document> illustrations;


	public Recipe () {
		this.category = Category.MAIN_COURSE;
		this.ingredients = Collections.emptySet();
		this.illustrations = new HashSet<>();
	}


	public Category getCategory () {
		return this.category;
	}


	public void setCategory (final Category category) {
		this.category = category;
	}


	public String getTitle () {
		return this.title;
	}


	public void setTitle (final String title) {
		this.title = title;
	}


	@JsonbProperty(nillable = true)
	public String getDescription () {
		return this.description;
	}


	public void setDescription (final String description) {
		this.description = description;
	}


	@JsonbProperty(nillable = true)
	public String getInstruction () {
		return this.instruction;
	}


	public void setInstruction (final String instruction) {
		this.instruction = instruction;
	}


	protected Long getAvatarReference () {
		return this.avatar == null ? null : this.avatar.getIdentity();
	}
	
	
	@JsonbTransient
	public Document getAvatar () {
		return this.avatar;
	}


	public void setAvatar (final Document avatar) {
		this.avatar = avatar;
	}


	protected Long getOwnerReference () {
		return this.owner == null ? null : this.owner.getIdentity();
	}


	@JsonbTransient
	public Person getOwner () {
		return this.owner;
	}


	public void setOwner (final Person owner) {
		this.owner = owner;
	}


	public Set<Ingredient> getIngredients () {
		return this.ingredients;
	}


	protected void setIngredients (final Set<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}


	public Set<Document> getIllustrations () {
		return this.illustrations;
	}


	protected void setIllustrations (final Set<Document> illustrations) {
		this.illustrations = illustrations;
	}


	public boolean isPescatarian () {
		return this.ingredients.stream().allMatch(Ingredient::isPescatarian);
	}


	public boolean isLactoOvoVegetarian () {
		return this.ingredients.stream().allMatch(Ingredient::isLactoOvoVegetarian);
	}


	public boolean isLactoVegetarian () {
		return this.ingredients.stream().allMatch(Ingredient::isLactoVegetarian);
	}


	public boolean isVegan () {
		return this.ingredients.stream().allMatch(Ingredient::isVegan);
	}
}

package edu.damago.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import edu.damago.tool.Copyright;
import edu.damago.tool.JsonProtectedPropertyStrategy;


/**
 * Instances of this class model ingredient entities.
 */
@Entity
@Table(schema = "cookbook", name = "Ingredient")
@PrimaryKeyJoinColumn(name = "ingredientIdentity")
@DiscriminatorValue("Ingredient")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@Copyright(year = 2022, holders = "Sascha Baumeister")
public class Ingredient extends BaseEntity {
	static public enum Unit { LITRE, GRAM, TEASPOON, TABLESPOON, PINCH, CUP, CAN, TUBE, BUSHEL, PIECE }


	@Positive
	@Column(nullable = false, updatable = true)
	private float amount;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = true)
	private Unit unit;

	// avoid @NotNull with @ManyToOne!
	@ManyToOne(optional = false)
	@JoinColumn(name = "recipeReference", nullable = false, updatable = false, insertable = true)
	private Recipe recipe;

	// avoid @NotNull with @ManyToOne!
	@ManyToOne(optional = false)
	@JoinColumn(name = "typeReference", nullable = false, updatable = false, insertable = true)
	private IngredientType type;


	protected Ingredient () {
		this(null, null);
	}


	public Ingredient (final Recipe recipe, final IngredientType type) {
		this.recipe = recipe;
		this.type = type;
	}


	public float getAmount () {
		return this.amount;
	}


	public void setAmount (final float amount) {
		this.amount = amount;
	}


	public Unit getUnit () {
		return this.unit;
	}


	public void setUnit (final Unit unit) {
		this.unit = unit;
	}


	public String getAlias () {
		return this.type == null ? null : this.type.getAlias();
	}


	@JsonbProperty(nillable = true)
	public String getDescription () {
		return this.type == null ? null : this.type.getDescription();
	}


	public boolean isPescatarian () {
		return this.type == null ? false : this.type.isPescatarian();
	}


	public boolean isLactoOvoVegetarian () {
		return this.type == null ? false : this.type.isLactoOvoVegetarian();
	}


	public boolean isLactoVegetarian () {
		return this.type == null ? false : this.type.isLactoVegetarian();
	}


	public boolean isVegan () {
		return this.type == null ? false : this.type.isVegan();
	}


	protected Long getAvatarReference () {
		return this.type == null ? null : this.type.getAvatarReference();
	}
	
	
	@JsonbTransient
	public Document getAvatar () {
		return this.type == null ? null : this.type.getAvatar();
	}


	protected Long getRecipeReference () {
		return this.recipe == null ? null : this.recipe.getIdentity();
	}


	@JsonbTransient
	public Recipe getRecipe () {
		return this.recipe;
	}


	protected void setRecipe (final Recipe recipe) {
		this.recipe = recipe;
	}


	protected Long getTypeReference () {
		return this.type == null ? null : this.type.getIdentity();
	}


	@JsonbTransient
	public IngredientType getType () {
		return this.type;
	}


	protected void setType (final IngredientType type) {
		this.type = type;
	}
}
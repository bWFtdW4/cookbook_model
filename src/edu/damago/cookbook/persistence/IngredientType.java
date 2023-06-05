package edu.damago.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import edu.damago.tool.Copyright;
import edu.damago.tool.JsonProtectedPropertyStrategy;


/**
 * Instances of this class model ingredient type entities.
 */
@Entity
@Table(schema = "cookbook", name = "IngredientType")
@PrimaryKeyJoinColumn(name = "ingredientTypeIdentity")
@DiscriminatorValue("IngredientType")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@Copyright(year = 2022, holders = "Sascha Baumeister")
public class IngredientType extends BaseEntity {

	@NotNull @Size(max = 128)
	@Column(nullable = false, updatable = true, length = 128, unique = true)
	private String alias;

	@Column(nullable = false, updatable = true)
	private boolean pescatarian;

	@Column(nullable = false, updatable = true)
	private boolean lactoOvoVegetarian;

	@Column(nullable = false, updatable = true)
	private boolean lactoVegetarian;

	@Column(nullable = false, updatable = true)
	private boolean vegan;

	@Size(max = 4094)
	@Column(nullable = true, updatable = true, length = 4094)
	private String description;

	// avoid @NotNull with @ManyToOne!
	@ManyToOne(optional = false)
	@JoinColumn(name = "avatarReference", nullable = false, updatable = true)
	private Document avatar;


	public String getAlias () {
		return this.alias;
	}


	public void setAlias (final String alias) {
		this.alias = alias;
	}


	@JsonbProperty(nillable = true)
	public String getDescription () {
		return this.description;
	}


	public void setDescription (final String description) {
		this.description = description;
	}


	public boolean isPescatarian () {
		return this.pescatarian;
	}


	public void setPescatarian (final boolean pescatarian) {
		if (!(this.pescatarian = pescatarian)) {
			this.lactoOvoVegetarian = false;
			this.lactoVegetarian = false;
			this.vegan = false;
		}
	}


	public boolean isLactoOvoVegetarian () {
		return this.lactoOvoVegetarian;
	}


	public void setLactoOvoVegetarian (final boolean lactoOvoVegetarian) {
		if (this.lactoOvoVegetarian = lactoOvoVegetarian) {
			this.pescatarian = true;
		} else {
			this.lactoVegetarian = false;
			this.vegan = false;
		}
	}


	public boolean isLactoVegetarian () {
		return this.lactoVegetarian;
	}


	public void setLactoVegetarian (final boolean lactoVegetarian) {
		if (this.lactoVegetarian = lactoVegetarian) {
			this.pescatarian = true;
			this.lactoOvoVegetarian = true;
		} else {
			this.vegan = false;
		}
	}


	public boolean isVegan () {
		return this.vegan;
	}


	public void setVegan (final boolean vegan) {
		if (this.vegan = vegan) {
			this.pescatarian = true;
			this.lactoOvoVegetarian = true;
			this.lactoVegetarian = true;
		}
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
}
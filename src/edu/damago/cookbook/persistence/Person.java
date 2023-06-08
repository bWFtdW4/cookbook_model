package edu.damago.cookbook.persistence;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import edu.damago.tool.Copyright;
import edu.damago.tool.HashCodes;
import edu.damago.tool.JsonProtectedPropertyStrategy;


/**
 * Instances of this class model person entities.
 */
@Entity
@Table(schema = "cookbook", name = "Person")
@PrimaryKeyJoinColumn(name = "personIdentity")
@DiscriminatorValue("Person")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@Copyright(year = 2012, holders = "Sascha Baumeister")
public class Person extends BaseEntity {
	static public enum Group { USER, ADMIN }
	static private final String DEFAULT_PASSWORD = "changeit";
	static private final String DEFAULT_PASSWORD_HASH = HashCodes.sha2HashText(256, DEFAULT_PASSWORD);


	@NotNull @Email @Size(max = 128)
	@Column(nullable = false, updatable = true, length = 128, unique = true)
	private String email;

	@NotNull @Size(min = 64, max = 64)
	@Column(nullable = false, updatable = true, length = 64)
	private String passwordHash;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "groupAlias", nullable = false, updatable = true)
	private Group group;

	@NotNull @Valid
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "family", column = @Column(name = "surname")),
		@AttributeOverride(name = "given", column = @Column(name = "forename"))
	})
	private Name name;

	@NotNull @Valid
	@Embedded
	private Address address;

	@NotNull
	@ElementCollection
	@CollectionTable(
		schema = "cookbook",
		name = "PhoneAssociation",
		joinColumns = @JoinColumn(name = "personReference", nullable = false, updatable = false, insertable = true),
		uniqueConstraints = @UniqueConstraint(columnNames = { "personReference", "phone" })
	)
	@Column(name = "phone",	nullable = false, updatable = false, insertable = true)
	private Set<String> phones;

	// avoid @NotNull with @ManyToOne!
	@ManyToOne(optional = false)
	@JoinColumn(name = "avatarReference", nullable = false, updatable = true)
	private Document avatar;

	@NotNull
	@OneToMany(mappedBy = "owner", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
	private Set<Recipe> recipes;


	public Person () {
		this.passwordHash = DEFAULT_PASSWORD_HASH;
		this.group = Group.USER;
		this.name = new Name();
		this.address = new Address();
		this.phones = new HashSet<>();
		this.recipes = Collections.emptySet();
	}


	public String getEmail () {
		return this.email;
	}


	public void setEmail (final String email) {
		this.email = email;
	}


	@JsonbTransient
	public String getPasswordHash () {
		return this.passwordHash;
	}


	public void setPasswordHash (final String passwordHash) {
		this.passwordHash = passwordHash;
	}


	public Group getGroup () {
		return this.group;
	}


	public void setGroup (final Group group) {
		this.group = group;
	}


	public Name getName () {
		return this.name;
	}


	protected void setName (final Name name) {
		this.name = name;
	}


	public Address getAddress () {
		return this.address;
	}


	protected void setAddress (final Address address) {
		this.address = address;
	}


	public Set<String> getPhones () {
		return this.phones;
	}


	protected void setPhones (final Set<String> phones) {
		this.phones = phones;
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


	protected long[] getRecipeReferences () {
		return this.recipes.stream().mapToLong(Recipe::getIdentity).sorted().toArray();
	}


	@JsonbTransient
	public Set<Recipe> getRecipes () {
		return this.recipes;
	}


	protected void setRecipes (final Set<Recipe> recipes) {
		this.recipes = recipes;
	}
	

}

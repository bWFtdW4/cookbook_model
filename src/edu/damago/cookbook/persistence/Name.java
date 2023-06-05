package edu.damago.cookbook.persistence;

import java.util.Comparator;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import edu.damago.tool.Copyright;
import edu.damago.tool.JsonProtectedPropertyStrategy;


/**
 * Instances can be embedded to model name properties within entities.
 */
@Embeddable
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@Copyright(year = 2012, holders = "Sascha Baumeister")
public class Name implements Comparable<Name> {
	static private final Comparator<Name> COMPARATOR = Comparator
		.comparing(Name::getTitle, Comparator.nullsFirst(Comparator.naturalOrder()))
		.thenComparing(Name::getFamily)
		.thenComparing(Name::getGiven);


	@Size(min = 1, max = 15)
	@Column(nullable = true, updatable = true, length = 15)
	private String title;

	@NotNull @Size(min = 1, max = 31)
	@Column(nullable = false, updatable = true, length = 31)
	private String family;

	@NotNull @Size(min = 1, max = 31)
	@Column(nullable = false, updatable = true, length = 31)
	private String given;


	@JsonbProperty(nillable = true)
	public String getTitle () {
		return this.title;
	}


	public void setTitle (final String title) {
		this.title = title;
	}


	public String getFamily () {
		return this.family;
	}


	public void setFamily (final String family) {
		this.family = family;
	}


	public String getGiven () {
		return this.given;
	}


	public void setGiven (final String given) {
		this.given = given;
	}


	@Override
	public int compareTo (final Name other) {
		return COMPARATOR.compare(this, other);
	}


	@Override
	public String toString () {
		return String.format("(title=%s, family=%s, given=%s)", this.title, this.family, this.given);
	}
}
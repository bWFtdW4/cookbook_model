package edu.damago.cookbook.persistence;

import java.util.Comparator;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import edu.damago.tool.Copyright;
import edu.damago.tool.JsonProtectedPropertyStrategy;


/**
 * Instances can be embedded to model address properties within entities.
 */
@Embeddable
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@Copyright(year = 2012, holders = "Sascha Baumeister")
public class Address implements Comparable<Address> {
	static private final Comparator<Address> COMPARATOR = Comparator
		.comparing(Address::getCountry)
		.thenComparing(Address::getCity)
		.thenComparing(Address::getStreet)
		.thenComparing(Address::getPostcode);


	@NotNull @Size(max = 31)
	@Column(nullable = false, updatable = true, length = 31)
	private String street;

	@NotNull @Size(max = 31)
	@Column(nullable = false, updatable = true, length = 31)
	private String city;

	@NotNull @Size(max = 31)
	@Column(nullable = false, updatable = true, length = 31)
	private String country;

	@NotNull @Size(max = 15)
	@Column(nullable = false, updatable = true, length = 15)
	private String postcode;


	public String getStreet () {
		return this.street;
	}


	public void setStreet (final String street) {
		this.street = street;
	}


	public String getCity () {
		return this.city;
	}


	public void setCity (final String city) {
		this.city = city;
	}


	public String getCountry () {
		return this.country;
	}


	public void setCountry (final String country) {
		this.country = country;
	}


	public String getPostcode () {
		return this.postcode;
	}


	public void setPostcode (final String postcode) {
		this.postcode = postcode;
	}


	@Override
	public int compareTo (final Address other) {
		return COMPARATOR.compare(this, other);
	}


	@Override
	public String toString () {
		return String.format("(street=%s, city=%s, country=%s, postcode=%s)", this.street, this.city, this.country, this.postcode);
	}
}
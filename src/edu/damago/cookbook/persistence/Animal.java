package edu.damago.cookbook.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Instances of this class model person entities.
 * pokedex.dex_main
 */
@Entity
@Table(schema = "pokedex", name = "dexMain")
public class Animal {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	
	//@NotNull @Size(max = 6)
	//@Column(nullable = false, updatable = true, length = 6)
	@Column(nullable = false)
	private int dexNr;
	
	//@NotNull @Size(max = 10)
	//@Column(nullable = false, updatable = true, length = 10)
	@Column(nullable = false)
	private String nameEn;

	//@NotNull @Size(max = 10)
	//@Column(nullable = false, updatable = true, length = 10)
	@Column(nullable = false)
	private String nameDe;

		
	
	public int getDexNr () {
		return dexNr;
	}

	public void setDexNr (int dexNr) {
		this.dexNr = dexNr;
	}

	public String getNameEn () {
		return nameEn;
	}

	public void setNameEn (String nameEn) {
		this.nameEn = nameEn;
	}

	public String getNameDe () {
		return nameDe;
	}

	public void setNameDe (String nameDe) {
		this.nameDe = nameDe;
	}


}

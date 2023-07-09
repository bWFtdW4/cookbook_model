package edu.damago.cookbook.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Instances of this class model person entities.
 * games.steam
 */
@Entity
@Table(schema = "games", name = "steam")
public class GameSteam {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String name;

		
	public String getName () {
		return name;
	}

	public void setName (String name) {
		this.name = name;
	}

}

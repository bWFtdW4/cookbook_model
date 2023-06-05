module cookbook_model {
	requires transitive java.logging;
	requires transitive javax.annotation.api;
	requires transitive java.validation;
	requires transitive java.json.bind;

	requires transitive javax.persistence;
	requires transitive eclipselink.minus.jpa;
	requires transitive java.ws.rs;

	exports edu.damago.cookbook.persistence;
	exports edu.damago.cookbook.service;
	exports edu.damago.tool;

	opens edu.damago.cookbook.persistence;
	opens edu.damago.cookbook.service;
}
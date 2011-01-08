package semweb.lab;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.ValidityReport;

public class Spikes {

	@BeforeClass
	public static void setUpBeforeClass() {
	}

	private InfModel model;

	@Before
	public void setUp() {
		ModelLoader modelLoader = new ModelLoader("file:drupal_schema.owl",
				"file:drupal_data.rdf", "file:drupal.rules");
		this.model = modelLoader.loadModel();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void sparqlQuery_Spike() {
		String queryString = ""
				+ "PREFIX drupal:<http://www.semanticweb.org/ontologies/2010/10/drupal.owl#> "
				+ "SELECT ?x ?name "
				+ "WHERE { ?x drupal:hasDescription ?name }";

		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Output query results
		ResultSetFormatter.out(System.out, results, query);

		// Important – free up resources used running the query
		qe.close();

	}

	@Test
	public void createProperty_Spike() {
		Triple object = new Triple();
		// Property insert
		object
				.setSubject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#PeppiThaProgramma");
		object
				.setPredicate("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#usesInstallable");

		object
				.setObject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#PeppisGreatModule");
		object.setLiteral(false);

		// Resource insert
		object
				.setSubject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#MattlThaOberwicht");
		object.setPredicate("http://www.w3.org/1999/02/22-rdf-syntax-ns");
		object
				.setObject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#Person");
		object.setLiteral(false);

		// Literal insert
		object
				.setSubject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#MattlThaOberwicht");
		object
				.setPredicate("http://www.semanticweb.org/ontologies/2010/10/drupal.owl");
		object.setObject("schubidu");
		object.setLiteral(true);

		Resource resource = model.getResource(object.getSubject());
		Property property = model.createProperty(object.getPredicate());

		if (object.isLiteral()) {
			resource.addLiteral(property, object.getObject());
		} else {
			RDFNode rdfNode = model.getRDFNode(Node.createURI((String) object
					.getObject()));
			resource.addProperty(property, rdfNode);
		}

		ValidityReport validity = model.validate();
		Assert.assertTrue(validity.isValid());

		FileOutputStream file;
		try {
			file = new FileOutputStream("test.owl");
			model.write(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}

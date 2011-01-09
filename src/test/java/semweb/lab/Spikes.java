package semweb.lab;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class Spikes {

	private static final String PREFIX = "http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#";

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void sparqlQuery_spike() {
		Model model = FileManager.get().loadModel("file:drupal_data.rdf");
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
	public void triggerInferenceAfterAdd_spike() {
		Model schema = FileManager.get().loadModel("file:transport_schema.rdf");
		Model data = FileManager.get().loadModel("file:transport_data.rdf");
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(schema);
		InfModel infModel = ModelFactory.createInfModel(reasoner, data);

		Resource resource = infModel.createResource(PREFIX + "18");
		Statement statement = infModel.createStatement(resource, RDF.type, infModel
				.getRDFNode(Node.createURI(PREFIX + "Tramway")));
		infModel.add(statement);

		for (Statement stmt : infModel.getResource(PREFIX + "18").listProperties().toList()) {
			System.out.println(stmt.getPredicate());
			System.out.println(stmt.getObject());
		}
	}
	
	@Test
	public void validation_spike() {
//		Model schema = FileManager.get().loadModel("file:transport_schema.rdf");
//		Model data = FileManager.get().loadModel("file:transport_data.rdf");
//
//		Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
//		reasoner = reasoner.bindSchema(schema);
//		InfModel infModel = ModelFactory.createInfModel(reasoner, data);
		ModelLoader modelLoader = new ModelLoader("file:transport_schema.rdf",
				"file:transport_data.rdf", "file:transport.rules");
		InfModel infModel = modelLoader.loadModel();
		Assert.assertTrue(infModel.validate().isValid());
		
		Resource resource = infModel.createResource(PREFIX + "Ubahn1");
		Property property = infModel.createProperty(PREFIX + "hasCapacity");
		Object value = 100.25;
		Statement statement = infModel.createLiteralStatement(resource, property, value);
		infModel.add(statement);
		ValidityReport result = infModel.validate();
		Assert.assertFalse(result.isValid());
		for (Iterator<Report> iterator = result.getReports(); iterator.hasNext();) {
			Report report = iterator.next();
			System.out.println(report.getDescription());
		}
	}

	@Test
	public void infModel_spike() {
		Model schema = FileManager.get().loadModel("file:transport_schema.rdf");
		Model data = ModelFactory.createDefaultModel();

		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(schema);
		InfModel infModel = ModelFactory.createInfModel(reasoner, data);

		Resource resource = data.createResource(PREFIX + "Bim2");
		Statement statement = data.createStatement(resource, RDF.type, data
				.getRDFNode(Node.createURI(PREFIX + "Tramway")));
		infModel.add(statement);
		Resource bim1 = infModel.getResource(PREFIX + "Bim2");
		for (Statement stmt : bim1.listProperties().toList()) {
			System.out.println(stmt.getPredicate());
			System.out.println(stmt.getObject());
		}
	}

	@Test
	public void createProperty_spike() {
		Triple triple = new Triple();
		// Property insert
		triple
				.setSubject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#PeppiThaProgramma");
		triple
				.setPredicate("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#usesInstallable");

		triple
				.setObject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#PeppisGreatModule");
		triple.setLiteral(false);

		// Resource insert
		triple
				.setSubject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#MattlThaOberwicht");
		triple.setPredicate("http://www.w3.org/1999/02/22-rdf-syntax-ns");
		triple
				.setObject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#Person");
		triple.setLiteral(false);

		// Literal insert
		triple
				.setSubject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#MattlThaOberwicht");
		triple
				.setPredicate("http://www.semanticweb.org/ontologies/2010/10/drupal.owl");
		triple.setObject("schubidu");
		triple.setLiteral(true);

		Resource resource = model.getResource(triple.getSubject());
		Property property = model.createProperty(triple.getPredicate());

		if (triple.isLiteral()) {
			resource.addLiteral(property, triple.getObject());
		} else {
			RDFNode rdfNode = model.getRDFNode(Node.createURI((String) triple
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

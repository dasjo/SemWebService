package semweb.lab;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.apache.cxf.helpers.MapNamespaceContext;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class SemWebServiceResourceTest {

	private static final String RULES_FILE = "file:transport.rules";
	private static final String DATA_FILE = "file:transport_data.rdf";
	private static final String SCHEMA_FILE = "file:transport_schema.rdf";
	private static final String SERVICE_URL = "http://localhost:9999/";
	private static final String PREFIX = "http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#";
	private SemWebServiceResource semWebServiceResource;

	@Before
	public void setUp() {
		Model schema = FileManager.get().loadModel(SCHEMA_FILE);
		Model data = FileManager.get().loadModel(DATA_FILE);
		List<Rule> rules = Rule.rulesFromURL(RULES_FILE);

		semWebServiceResource = new SemWebServiceResource(schema, data, rules);
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setResourceClasses(SemWebServiceResource.class);
		sf.setResourceProvider(SemWebServiceResource.class,
				new SingletonResourceProvider(semWebServiceResource));
		sf.setAddress(SERVICE_URL);
		sf.create();
	}

	private static WebClient createWebClientInsert() {
		return WebClient.create(SERVICE_URL + "/insert");
	}

	private static WebClient createWebClientQuery(String query)
			throws UnsupportedEncodingException {
		UriBuilder builder = UriBuilder.fromUri(SERVICE_URL + "/query");
		builder = builder.queryParam("qry", "{query}");
		return WebClient.create(builder.build(query));
	}

	@Test
	public void insertLiteralProperty_subjectExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Ubahn1";
		triple.predicate = PREFIX + "hasCapacity";
		triple.object = 500;
		triple.isLiteral = true;

		checkNodeExisting("Subject must exist before: "
				+ triple.getSubject(), triple.getSubject(), true);
		Response response = createWebClientInsert().post(triple);

		assertThat("Inserting existing subject is expected to succeed",
				response.getStatus(), is(200));
		Resource resource = semWebServiceResource.getInfModel().getResource(
				triple.getSubject());
		Statement property = getExistingProperty(resource, triple
				.getPredicate());
		assertThat("Subject must have property: " + triple.getPredicate(),
				property, notNullValue());
		assertThat("Property value must be updated to: "
				+ triple.getObject().toString(), property.getInt(), is(triple
				.getObject()));
	}

	@Test
	public void insertLiteralProperty_subjectNotExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Ubahn3";
		triple.predicate = PREFIX + "hasCapacity";
		triple.object = 500;
		triple.isLiteral = true;

		checkNodeExisting("Subject must not exist before: "
				+ triple.getSubject(), triple.getSubject(), false);
		Response response = createWebClientInsert().post(triple);

		assertThat("Inserting non-existing subject is expected to succeed",
				response.getStatus(), is(200));
		Resource resource = semWebServiceResource.getInfModel().getResource(
				triple.getSubject());
		Statement property = getExistingProperty(resource, triple
				.getPredicate());
		assertThat("Subject must have property: " + triple.getPredicate(),
				property, notNullValue());
		assertThat("Property value must be: " + triple.getObject().toString(),
				property.getInt(), is(triple.getObject()));
	}

	@Test
	public void insertProperty_subjectExisting_objectExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Fahrschein1";
		triple.predicate = PREFIX + "isOwnedBy";
		triple.object = PREFIX + "NikkiLauda";

		checkNodeExisting("Subject must exist before: " + triple.getSubject(),
				triple.getSubject(), true);
		checkNodeExisting("Object must exist before: "
				+ triple.getObject().toString(), (String) triple.getObject(),
				true);
		Response response = createWebClientInsert().post(triple);

		assertThat(
				"Inserting existing subject with existing object is expected to succeed",
				response.getStatus(), is(200));
		Resource resource = semWebServiceResource.getInfModel().getResource(
				triple.getSubject());
		Statement property = getExistingProperty(resource, triple
				.getPredicate());
		assertThat("Subject must have property: " + triple.getPredicate(),
				property, notNullValue());
		assertThat("Property value must be: " + triple.getObject().toString(),
				property.getResource().getURI(), is(triple.getObject()));
	}

	@Test
	public void insertProperty_subjectExisting_objectNotExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Fahrschein1";
		triple.predicate = PREFIX + "isOwnedBy";
		triple.object = PREFIX + "JohnDoe";

		checkNodeExisting("Subject must exist before: " + triple.getSubject(),
				triple.getSubject(), true);
		checkNodeExisting("Object must not exist before: "
				+ triple.getObject().toString(), (String) triple.getObject(),
				false);
		Response response = createWebClientInsert().post(triple);

		assertThat(
				"Inserting existing subject with non-existing object is expected to succeed",
				response.getStatus(), is(200));
		Resource resource = semWebServiceResource.getInfModel().getResource(
				triple.getSubject());
		Statement property = getExistingProperty(resource, triple
				.getPredicate());
		assertThat("Subject must have property: " + triple.getPredicate(),
				property, notNullValue());
		assertThat("Property value must be: " + triple.getObject().toString(),
				property.getResource().getURI(), is(triple.getObject()));
	}

	@Test
	public void insertProperty_subjectNotExisting_objectExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Fahrschein2";
		triple.predicate = RDF.type.getURI();
		triple.object = PREFIX + "Ticket";

		checkNodeExisting("Subject must not exist before: "
				+ triple.getSubject(), triple.getSubject(), false);
		checkNodeExisting("Object must not exist before: "
				+ triple.getObject().toString(), (String) triple.getObject(),
				true);
		Response response = createWebClientInsert().post(triple);

		assertThat(
				"Inserting non-existing subject with non-existing object is expected to succeed",
				response.getStatus(), is(200));
		Resource resource = semWebServiceResource.getInfModel().getResource(
				triple.getSubject());
		Statement property = getExistingProperty(resource, triple
				.getPredicate());
		assertThat("Subject must have property: " + triple.getPredicate(),
				property, notNullValue());
		assertThat("Property value must be: " + triple.getObject().toString(),
				property.getResource().getURI(), is(triple.getObject()));
	}

	@Test
	public void insertProperty_owlReasoner_shouldInferStatements() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "18";
		triple.predicate = RDF.type.getURI();
		triple.object = PREFIX + "Tramway";

		checkNodeExisting("Subject must not exist before: "
				+ triple.getSubject(), triple.getSubject(), false);
		checkNodeExisting("Object must exist before: "
				+ triple.getObject().toString(), (String) triple.getObject(),
				true);
		Response response = createWebClientInsert().post(triple);

		assertThat(
				"Inserting non-existing subject with existing object is expected to succeed",
				response.getStatus(), is(200));
		Resource resource = semWebServiceResource.getInfModel().getResource(
				triple.getSubject());
		Statement property = getExistingProperty(resource, RDF.type.getURI(),
				PREFIX + "Vehicle");
		assertThat(
				"OWL Reasoner must have inferred property Vehicle for inserted subject",
				property, notNullValue());
	}

	@Test
	public void insertProperty_ruleReasoner_shouldInferStatements() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "TicketABC";
		triple.predicate = RDF.type.getURI();
		triple.object = PREFIX + "Ticket";
		Response response = createWebClientInsert().post(triple);
		assertThat("Inserting subject with object is expected to succeed",
				response.getStatus(), is(200));

		triple.predicate = PREFIX + "hasPrize";
		triple.object = 10;
		triple.isLiteral = true;
		response = createWebClientInsert().post(triple);
		assertThat("Inserting subject with object is expected to succeed",
				response.getStatus(), is(200));
		Resource resource = semWebServiceResource.getInfModel().getResource(
				triple.getSubject());
		Statement property = getExistingProperty(resource, RDF.type.getURI(),
				PREFIX + "CheapTicket");
		assertThat(
				"CheapTicket property wasn't inferred properly for ticket with value: "
						+ triple.object.toString(), property, notNullValue());
	}

	@Test
	public void insertLiteralProperty_invalidDatatype_shouldReturn500() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Ubahn1";
		triple.predicate = PREFIX + "hasCapacity";
		triple.object = 100.25;
		triple.isLiteral = true;

		Node node = Node.createURI((String) triple.getSubject());
		RDFNode rdfNode = semWebServiceResource.getInfModel().getRDFNode(node);
		assertThat("Subject must exist in model", semWebServiceResource
				.getInfModel().containsResource(rdfNode), is(true));

		Response response = createWebClientInsert().post(triple);

		assertThat("Updating subject must fail due to validation", response
				.getStatus(), is(500));
	}

	@Test
	public void query_allDrivers_shouldReturnAllDrivers() throws IOException,
			ParserConfigurationException, SAXException,
			XPathExpressionException {
		String queryString = "" + "PREFIX ut:<" + PREFIX + "> " + "SELECT ?x "
				+ "WHERE { ?x a ut:Driver }";
		Response response = createWebClientQuery(queryString).get();

		assertThat("Search must succeed", response.getStatus(), is(200));
		NodeList nodes = selectNodesFromSparqlResult(response,
				"/sparql/results/result/binding[@name='x']/uri");
		assertThat("Invalid number of matches in SPARQL query", nodes
				.getLength(), is(3));
		assertThat(
				"Unexpected result in SPARQL query",
				nodes.item(0).getTextContent(),
				is("http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#NikkiLauda"));
		assertThat(
				"Unexpected result in SPARQL query",
				nodes.item(1).getTextContent(),
				is("http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#JochenRindt"));
		assertThat(
				"Unexpected result in SPARQL query",
				nodes.item(2).getTextContent(),
				is("http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#GerhardBerger"));
	}

	@Test
	public void query_afterInsert_shouldReturnInserted() throws IOException,
			ParserConfigurationException, SAXException,
			XPathExpressionException {
		Triple triple = new Triple();
		triple.subject = PREFIX + "MatthiasRauch";
		triple.predicate = RDF.type.getURI();
		triple.object = PREFIX + "Person";

		Response response = createWebClientInsert().post(triple);

		String queryString = "" + "PREFIX ut:<" + PREFIX + "> " + "SELECT ?x "
				+ "WHERE { ?x a ut:Person }";
		response = createWebClientQuery(queryString).get();

		assertThat("Search must succeed", response.getStatus(), is(200));
		NodeList nodes = selectNodesFromSparqlResult(response,
				"/sparql/results/result/binding[@name='x']/uri");
		assertThat("Invalid number of matches in SPARQL query", nodes
				.getLength(), is(5));
		assertThat(
				"Unexpected result in SPARQL query",
				nodes.item(0).getTextContent(),
				is("http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#MatthiasRauch"));
	}
	
	 
  @Test
  public void queryDataPropertyLineIntervals() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {    
    String queryString = ""
        + "PREFIX transport:<" + PREFIX + "> "
        + "SELECT ?line ?interval "
        + "WHERE { ?line transport:hasInterval ?interval }";
    
    Response response = createWebClientQuery(queryString).get();
    
    assertThat("Search must succeed", response.getStatus(), is(200));
    NodeList nodes = selectNodesFromSparqlResult(response,
        "/sparql/results/result/binding[@name='line']/uri");
    assertThat("Invalid number of matches in SPARQL query", nodes
        .getLength(), is(3));
    
    assertThat(
        "Unexpected result in SPARQL query",
        nodes.item(0).getTextContent(),
        is("http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#U3"));
    assertThat(
        "Unexpected result in SPARQL query",
        nodes.item(1).getTextContent(),
        is("http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#84A"));
    assertThat(
        "Unexpected result in SPARQL query",
        nodes.item(2).getTextContent(),
        is("http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#71"));
  }
  
  @Test
  public void queryObjectPropertyLineIntervals() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {    
    String queryString = ""
        + "PREFIX transport:<" + PREFIX + "> "
        + "SELECT ?driver ?vehicle "
        + "WHERE { ?driver transport:isDriverOf ?vehicle }";
    
    Response response = createWebClientQuery(queryString).get();
    
    assertThat("Search must succeed", response.getStatus(), is(200));
    
    NodeList nodes = selectNodesFromSparqlResult(response,
        "/sparql/results/result/binding[@name='driver']/uri");
    assertThat("Invalid number of matches in SPARQL query", nodes
        .getLength(), is(3));
    
    assertThat("Unexpected result in SPARQL query", nodes.item(0).getTextContent(), is(PREFIX + "NikkiLauda"));
    assertThat("Unexpected result in SPARQL query", nodes.item(1).getTextContent(), is(PREFIX + "JochenRindt"));
    assertThat("Unexpected result in SPARQL query", nodes.item(2).getTextContent(), is(PREFIX + "GerhardBerger"));
  }
	
	
	/* ### HELPER METHODS ### */
	
	private void checkNodeExisting(String reason, String uri, boolean existing) {
		Node node = Node.createURI(uri);
		RDFNode rdfNode = semWebServiceResource.getInfModel().getRDFNode(node);
		assertThat(reason, semWebServiceResource.getInfModel()
				.containsResource(rdfNode), is(existing));
	}

	private static NodeList selectNodesFromSparqlResult(Response response,
			String xpathExpr) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse((InputStream) response.getEntity());
		XPath xpath = XPathFactory.newInstance().newXPath();
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.w3.org/2005/sparql-results#");
		NamespaceContext nsContext = new MapNamespaceContext(namespaces);
		xpath.setNamespaceContext(nsContext);
		return (NodeList) xpath
				.evaluate(xpathExpr, doc, XPathConstants.NODESET);

	}

	private Statement getExistingProperty(Resource resource, String predicate) {
		return getExistingProperty(resource, predicate, null);
	}

	private Statement getExistingProperty(Resource resource, String predicate,
			String object) {
		StmtIterator stmtIterator = resource.listProperties();
		while (stmtIterator.hasNext()) {
			Statement statement = (Statement) stmtIterator.next();
			if (statement.getPredicate().getURI().equals(predicate)
					&& (object == null || statement.getObject().toString()
							.equals(object)))
				return statement;
		}
		return null;
	}
}

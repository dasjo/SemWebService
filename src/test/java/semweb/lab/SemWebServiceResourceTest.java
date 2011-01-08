package semweb.lab;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class SemWebServiceResourceTest {

	private static final String SERVICE_URL = "http://localhost:9999/";
	private static final String PREFIX = "http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#";
	private InfModel model;
	private WebClient client;

	@Before
	public void setUp() {
		ModelLoader modelLoader = new ModelLoader("file:transport_schema.rdf",
				"file:transport_data.rdf", "file:transport.rules");
		this.model = modelLoader.loadModel();

		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setResourceClasses(SemWebServiceResource.class);
		sf
				.setResourceProvider(SemWebServiceResource.class,
						new SingletonResourceProvider(
								new SemWebServiceResource(model)));
		sf.setAddress(SERVICE_URL);
		sf.create();

		this.client = WebClient.create(SERVICE_URL + "/sparql");
	}

	@Test
	public void insertLiteralProperty_subjectExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Ubahn1";
		triple.predicate = PREFIX + "hasCapacity";
		triple.object = 500;
		triple.isLiteral = true;

		checkNodeExisting(triple.getSubject(), true);
		Response response = client.post(triple);

		assertThat(response.getStatus(),
				is(200));
		Resource resource = model.getResource(triple.getSubject());
		Statement property = getExistingProperty(resource, triple
				.getPredicate());
		assertThat(property, notNullValue());
		assertThat(property.getInt(), is(triple.getObject()));
	}

	@Test
	public void insertLiteralProperty_subjectNotExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Ubahn3";
		triple.predicate = PREFIX + "hasCapacity";
		triple.object = 500;
		triple.isLiteral = true;

		checkNodeExisting(triple.getSubject(), false);
		Response response = client.post(triple);

		assertThat(response.getStatus(),
				is(200));
		Resource resource = model.getResource(triple.getSubject());
		Statement property = getExistingProperty(resource, triple
				.getPredicate());
		assertThat(property, notNullValue());
		assertThat(property.getInt(), is(triple.getObject()));
	}

	@Test
	public void insertProperty_subjectExisting_objectExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Fahrschein1";
		triple.predicate = PREFIX + "isOwnedBy";
		triple.object = PREFIX + "NikkiLauda";

		checkNodeExisting(triple.getSubject(), true);
		checkNodeExisting((String) triple.getObject(), true);
		Response response = client.post(triple);

		assertThat(response.getStatus(),
				is(200));
		Resource resource = model.getResource(triple.getSubject());
		Statement property = getExistingProperty(resource, triple
				.getPredicate());
		assertThat(property, notNullValue());
		assertThat(property.getResource().getURI(), is(triple.getObject()));
	}

	@Test
	public void insertProperty_subjectExisting_objectNotExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Fahrschein1";
		triple.predicate = PREFIX + "isOwnedBy";
		triple.object = PREFIX + "JohnDoe";

		checkNodeExisting(triple.getSubject(), true);
		checkNodeExisting((String) triple.getObject(), false);
		Response response = client.post(triple);

		assertThat(response.getStatus(),
				is(200));
		Resource resource = model.getResource(triple.getSubject());
		Statement property = getExistingProperty(resource, triple
				.getPredicate());
		assertThat(property, notNullValue());
		assertThat(property.getResource().getURI(), is(triple.getObject()));
	}

	@Test
	public void insertProperty_subjectNotExisting_objectExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Fahrschein2";
		triple.predicate = RDF.type.getURI();
		triple.object = PREFIX + "Ticket";

		checkNodeExisting(triple.getSubject(), false);
		checkNodeExisting((String) triple.getObject(), true);
		Response response = client.post(triple);

		assertThat(response.getStatus(),
				is(200));
		Resource resource = model.getResource(triple.getSubject());
		Statement property = getExistingProperty(resource, triple
				.getPredicate());
		assertThat(property, notNullValue());
		assertThat(property.getResource().getURI(), is(triple.getObject()));
	}

	@Test
	public void insertProperty_owlReasoner_shouldInferStatements() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "18";
		triple.predicate = RDF.type.getURI();
		triple.object = PREFIX + "Tramway";

		checkNodeExisting(triple.getSubject(), false);
		checkNodeExisting((String) triple.getObject(), true);
		Response response = client.post(triple);

		assertThat(response.getStatus(),
				is(200));
		Resource resource = model.getResource(triple.getSubject());
		Statement property = getExistingProperty(resource, RDF.type.getURI(), PREFIX + "Vehicle");
		assertThat(property, notNullValue());
	}
	
	@Test
	public void insertProperty_ruleReasoner_shouldInferStatements() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "TicketABC";
		triple.predicate = RDF.type.getURI();
		triple.object = PREFIX + "Ticket";
		Response response = client.post(triple);
		assertThat(response.getStatus(),
				is(200));
		
		triple.predicate = PREFIX + "hasPrize";
		triple.object = 10;
		triple.isLiteral = true;
		response = client.post(triple);
		assertThat(response.getStatus(), is(200));
		Resource resource = model.getResource(triple.getSubject());
		Statement property = getExistingProperty(resource, RDF.type.getURI(), PREFIX + "CheapTicket");
		assertThat(property, notNullValue());
	}

	private void checkNodeExisting(String uri, boolean existing) {
		Node node = Node.createURI(uri);
		RDFNode rdfNode = model.getRDFNode(node);
		assertThat(model.containsResource(rdfNode), is(existing));
	}

	@Test
	public void insertLiteralProperty_invalidDatatype_shouldReturn500() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Ubahn1";
		triple.predicate = PREFIX + "hasCapacity";
		triple.object = 100.25;
		triple.isLiteral = true;

		Node node = Node.createURI((String) triple.getSubject());
		RDFNode rdfNode = model.getRDFNode(node);
		assertThat(model.containsResource(rdfNode), is(true));

		Response response = client.post(triple);

		assertThat(response.getStatus(),
				is(500));
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

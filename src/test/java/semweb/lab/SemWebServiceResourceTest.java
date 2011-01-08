package semweb.lab;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class SemWebServiceResourceTest {

	private static final String SERVICE_URL = "http://localhost:9999/";
	private static final String PREFIX = "http://www.semanticweb.org/ontologies/2010/10/UrbanTransport.owl#";
	private InfModel model;
	private WebClient client;

	@Before
	public void setUp() {
		ModelLoader modelLoader = new ModelLoader("file:transport_schema.owl",
				"file:transport_data.rdf", "file:transport.rules");
		this.model = modelLoader.loadModel();
		
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setResourceClasses(SemWebServiceResource.class);
		sf.setResourceProvider(SemWebServiceResource.class, new SingletonResourceProvider(
				new SemWebServiceResource(model)));
		sf.setAddress(SERVICE_URL);
		sf.create();
		
		this.client = WebClient.create(SERVICE_URL + "/sparql");
	}

	
	@Test
	public void insertLiteral_resourceExisting_shouldBeInsertedInModel() {
		Triple triple = new Triple();
		triple.subject = PREFIX + "Ubahn1";
		triple.predicate = PREFIX + "hasCapacity";
		triple.object = 500;
		triple.isLiteral = true;
		
		Node node = Node.createURI((String) triple.getSubject());
		RDFNode rdfNode = model.getRDFNode(node);
		assertThat(model.containsResource(rdfNode), is(true));
		
		Response response = client.post(triple);
		
		assertThat(response.getEntity().toString(), response.getStatus(), is(200));
		Resource resource = model.getResource(triple.getSubject());
		Statement property = getExistingProperty(resource, triple.getPredicate());
		assertThat(property, notNullValue());
		assertThat(property.getInt(), is(triple.getObject()));
	}


	private Statement getExistingProperty(Resource resource, String predicate) {
		StmtIterator stmtIterator = resource.listProperties();
		while (stmtIterator.hasNext()) {
			Statement statement = (Statement) stmtIterator.next();
			if (statement.getPredicate().getURI().equals(predicate))
					return statement;
		}
		return null;
	}
}

package semweb.lab;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;

public class SemWebServiceResourceTest {

	private static final String SERVICE_URL = "http://localhost:9999/";
	private InfModel model;
	private WebClient client;

	@Before
	public void setUp() {
		ModelLoader modelLoader = new ModelLoader("file:drupal_schema.owl",
				"file:drupal_data.rdf", "file:drupal.rules");
		this.model = modelLoader.loadModel();
		
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setResourceClasses(SemWebServiceResource.class);
		sf.setResourceProvider(SemWebServiceResource.class, new SingletonResourceProvider(
				new SemWebServiceResource(model)));
		sf.setAddress(SERVICE_URL);
		sf.create();
		
		this.client = WebClient.create("http://localhost:1234/sparql");
	}

	
	@Test
	public void insertLiteral_shouldBeInsertedInModel() {
		
	}
}

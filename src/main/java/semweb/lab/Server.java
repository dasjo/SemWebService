package semweb.lab;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

import com.hp.hpl.jena.rdf.model.InfModel;

public class Server {
	
	private static final String RULES_FILE = "file:drupal.rules";
	private static final String DATA_FILE = "file:drupal_data.rdf";
	private static final String SCHEMA_FILE = "file:drupal_schema.owl";
	
	private static Log logger = LogFactory.getLog(Server.class);
	
	public static void main(String[] args) {	
		ModelLoader modelLoader = new ModelLoader(SCHEMA_FILE, DATA_FILE, RULES_FILE);
		logger.info("Loading models...");
		InfModel infModel = modelLoader.loadModel();
		logger.info("Models loaded.");
		
	    logger.info("Starting webservice...");
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setResourceClasses(SemWebServiceResource.class);
		sf.setResourceProvider(SemWebServiceResource.class, new SingletonResourceProvider(
				new SemWebServiceResource(infModel)));
		sf.setAddress("http://localhost:1234/");
		sf.create();
		logger.info("Webservice started.");
	}
}

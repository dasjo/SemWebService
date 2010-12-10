package semweb.lab;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.util.FileManager;

public class SPARQLServer {
	
	/* logger */
	private static Log logger = LogFactory.getLog(SPARQLServer.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	  Model data = FileManager.get().loadModel("file:drupal.rdf");
    //InfModel infmodel = ModelFactory.createRDFSModel(data);
    //ValidityReport validity = infmodel.validate();
		
    
    
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setResourceClasses(SPARQLServiceResource.class);
		sf.setResourceProvider(SPARQLServiceResource.class, new SingletonResourceProvider(
				new SPARQLServiceResource(data)));
		sf.setAddress("http://localhost:1234/");
		//sf.getInInterceptors().add(new AuthenticationInterceptor());
		sf.create();
		logger.info("webservice server started");
	}
}

package semweb.lab;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

public class SPARQLServer {
	
	/* logger */
	private static Log logger = LogFactory.getLog(SPARQLServer.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setResourceClasses(SPARQLServiceResource.class);
		sf.setResourceProvider(SPARQLServiceResource.class, new SingletonResourceProvider(
				new SPARQLServiceResource()));
		sf.setAddress("http://localhost:1234/");
		//sf.getInInterceptors().add(new AuthenticationInterceptor());
		sf.create();
		logger.info("webservice server started");
	}
}

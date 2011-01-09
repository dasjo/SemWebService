package semweb.lab;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.FileManager;

public class Server {

	private static final String RULES_FILE = "file:transport.rules";
	private static final String DATA_FILE = "file:transport_data.rdf";
	private static final String SCHEMA_FILE = "file:transport_schema.rdf";

	private static Log logger = LogFactory.getLog(Server.class);

	public static void main(String[] args) {
		logger.info("Loading models...");
		Model schema = FileManager.get().loadModel(SCHEMA_FILE);
		Model data = FileManager.get().loadModel(DATA_FILE);
		List<Rule> rules = Rule.rulesFromURL(RULES_FILE);
		logger.info("Models loaded.");

		logger.info("Starting webservice...");
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setResourceClasses(SemWebServiceResource.class);
		sf.setResourceProvider(SemWebServiceResource.class,
				new SingletonResourceProvider(new SemWebServiceResource(schema,
						data, rules)));
		sf.setAddress("http://localhost:1234/");
		sf.create();
		logger.info("Webservice started.");
	}
}

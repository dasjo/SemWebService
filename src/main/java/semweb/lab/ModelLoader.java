package semweb.lab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;


public class ModelLoader {
	private static Log logger = LogFactory.getLog(ModelLoader.class);
	
	private String schemaFile;
	private String instancesFile;
	private String rulesFile;

	public ModelLoader(String schemaFile, String instancesFile, String rulesFile) {
		this.schemaFile = schemaFile;
		this.instancesFile = instancesFile;
		this.rulesFile = rulesFile;
	}
	
	public InfModel loadModel() {
		Model schema = FileManager.get().loadModel(schemaFile);
	    Model data = FileManager.get().loadModel(instancesFile);
	    
	    //Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
	    
	    logger.info("Creating reasoners...");
	    Reasoner reasoner = ReasonerRegistry.getRDFSReasoner(); 
//	    reasoner.setParameter(ReasonerVocabulary.PROPtraceOn, Boolean.TRUE);
	    reasoner.setParameter(ReasonerVocabulary.PROPsetRDFSLevel, ReasonerVocabulary.RDFS_FULL);
	    reasoner.bindSchema(schema);
	    GenericRuleReasoner ruleReasoner = new GenericRuleReasoner(Rule.rulesFromURL(rulesFile));
	    ruleReasoner.bindSchema(schema);
	    
	    logger.info("Inferring information via RDFS reasoner...");
	    InfModel infModel = ModelFactory.createInfModel(reasoner, data);
	    logger.info("Inference completed.");
	    logger.info("Inferring information via generic rule reasoner...");
	    InfModel infModelWithCustomRules = ModelFactory.createInfModel(ruleReasoner, infModel);	
	    logger.info("Inference completed.");
	    return infModelWithCustomRules;
	}
}

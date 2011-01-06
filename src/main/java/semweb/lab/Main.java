package semweb.lab;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

public class Main {

  /**
   * @param args
   */
  public static void main(String[] args) {
    Model schema = FileManager.get().loadModel("file:drupal_schema.owl");
    Model data = FileManager.get().loadModel("file:drupal_data.rdf");

    //Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
    
    Reasoner reasoner = ReasonerRegistry.getRDFSReasoner(); 
    reasoner.setParameter(ReasonerVocabulary.PROPtraceOn, Boolean.TRUE);
    reasoner.setParameter(ReasonerVocabulary.PROPsetRDFSLevel, ReasonerVocabulary.RDFS_FULL);

    reasoner = reasoner.bindSchema(schema);
    InfModel infmodel = ModelFactory.createInfModel(reasoner, data);


    //InfModel infmodel = ModelFactory.createRDFSModel(data);
    //ValidityReport validity = infmodel.validate();
    
    Model all = schema.union(data);

  }

}

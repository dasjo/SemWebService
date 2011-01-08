package semweb.lab;

import javax.ws.rs.core.Response;

public interface SPARQLService {

  Response insert(Triple object);
  
  Response query(String sparqlQuery);

  boolean writeOntologyToFile();
}

package semweb.lab;

import javax.ws.rs.core.Response;

public interface SemWebService {

  Response insert(Triple object);
  
  Response query(String sparqlQuery);
}

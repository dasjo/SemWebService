package semweb.lab;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;

@Path("/sparql")
public class SemWebServiceResource implements SemWebService {

	private static Log logger = LogFactory.getLog(SemWebServiceResource.class);

	@Context
	MessageContext messageContext;

	private InfModel infModel;

	public SemWebServiceResource(InfModel model) {
		this.infModel = model;
	}

	@Override
	@POST
	public Response insert(Triple triple) {
		Resource resource = this.infModel.createResource(triple.getSubject());
		Property property = this.infModel.createProperty(triple.getPredicate());
		
		if (triple.isLiteral) {
			resource.addLiteral(property, triple.getObject());
		} else {
			Node node = Node.createURI((String) triple.getObject());
			RDFNode rdfNode = infModel.getRDFNode(node);
			resource.addProperty(property, rdfNode);
		}

		ValidityReport validityReport = infModel.validate();
		if (!validityReport.isValid()) {
			StringBuffer message = new StringBuffer(
					"Validation of model failed due to following errors:\n");
			while (validityReport.getReports().hasNext()) {
				Report report = validityReport.getReports().next();
				message.append(" * ");
				message.append(report.getDescription());
				message.append("\n");
			}

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(
					message.toString()).build();
		}

		return Response.ok().build();
	}

	@Override
	@GET
	public boolean writeOntologyToFile() {
		logger.debug("writing ontology to file");

		FileOutputStream file;
		try {
			file = new FileOutputStream("test.owl");
			this.infModel.write(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	@GET
	public Response query(String sparqlQuery) {
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query, infModel);
		ResultSet results = qe.execSelect();
		qe.close();
		String result = ResultSetFormatter.asText(results, query);
		
		return Response.ok().entity(result).build();
	}

}

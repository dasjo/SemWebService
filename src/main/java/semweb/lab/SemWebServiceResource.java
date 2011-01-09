package semweb.lab;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

@Path("/sparql")
public class SemWebServiceResource implements SemWebService {

	private static Log logger = LogFactory.getLog(SemWebServiceResource.class);

	@Context
	MessageContext messageContext;

	private InfModel ontModel;
	private InfModel infModel;

	public SemWebServiceResource(Model schema, Model data, List<Rule> rules) {
		logger.info("Creating reasoners...");
		Reasoner ontReasoner = ReasonerRegistry.getRDFSReasoner();
		Reasoner ruleReasoner = new GenericRuleReasoner(rules);

		logger.info("Inferring information via RDFS reasoner...");
		this.ontModel = ModelFactory.createInfModel(ontReasoner
				.bindSchema(schema), data);
		logger.info("Inference completed.");
		logger.info("Inferring information via generic rule reasoner...");
		this.infModel = ModelFactory.createInfModel(ruleReasoner, ontModel);
		logger.info("Inference completed.");
	}

	@Override
	@POST
	public Response insert(Triple triple) {
		Resource resource = this.infModel.createResource(triple.getSubject());
		Property property = this.infModel.createProperty(triple.getPredicate());
		Statement statement = null;
		if (triple.isLiteral) {
			statement = infModel.createLiteralStatement(resource, property,
					triple.getObject());
		} else {
			Node node = Node.createURI((String) triple.getObject());
			RDFNode rdfNode = infModel.getRDFNode(node);
			statement = infModel.createStatement(resource, property, rdfNode);
		}
		infModel.add(statement);

		ValidityReport validityReport = ontModel.validate();
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
	public Response query(@QueryParam("qry") String sparqlQuery) {
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query, infModel);
		ResultSet results = qe.execSelect();

		String result = ResultSetFormatter.asXMLString(results);
		logger.info(result);
		Response response = Response.ok().entity(result).build();
		qe.close();
		return response;
	}

	public Model getInfModel() {
		return infModel;
	}

}

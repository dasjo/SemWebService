import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import semweb.lab.SPARQLInsertObject;

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
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.util.FileManager;

public class MessageServiceTest {
  /* logger */
  private static Log log = LogFactory.getLog(MessageServiceTest.class);

  @BeforeClass
  public static void setUpBeforeClass() {
  }

  @Before
  public void setUp() {
    // SPARQLServer server = new SPARQLServer();
    // SPARQLServer.main(null);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testQuery() {
    Model model = FileManager.get().loadModel("file:drupal.rdf");
    
    String queryString = "" +
    		"PREFIX drupal:<http://www.semanticweb.org/ontologies/2010/10/drupal.owl#> " +
    		"SELECT ?x ?name " +
    		"WHERE { ?x drupal:hasDescription ?name }";

    Query query = QueryFactory.create(queryString);

    // Execute the query and obtain results
    QueryExecution qe = QueryExecutionFactory.create(query, model);
    ResultSet results = qe.execSelect();

    // Output query results
    ResultSetFormatter.out(System.out, results, query);

    // Important – free up resources used running the query
    qe.close();

  }

  @Test
  public void testCreateMessage() {

    SPARQLInsertObject object = new SPARQLInsertObject();

    // Property insert
    object.setResourceURI("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#PeppiThaProgramma");
    object.setPropertyNameSpace("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#usesInstallable");
    object.setPropertyLocalName("usesInstallable");

    object.setObject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#PeppisGreatModule");
    object.setLiteral(false);

    // Resource insert
    object.setResourceURI("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#MattlThaOberwicht");
    object.setPropertyNameSpace("http://www.w3.org/1999/02/22-rdf-syntax-ns");
    object.setPropertyLocalName("type");
    object.setObject("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#Person");
    object.setLiteral(false);

    // Literal insert
    object.setResourceURI("http://www.semanticweb.org/ontologies/2010/10/drupal.owl#MattlThaOberwicht");
    object.setPropertyNameSpace("http://www.semanticweb.org/ontologies/2010/10/drupal.owl");
    object.setPropertyLocalName("hasDescription");
    object.setObject("schubidu");
    object.setLiteral(true);

    Model model = FileManager.get().loadModel("file:drupal.rdf");

    Resource resource = model.getResource(object.getResourceURI());
    Property property = model.createProperty(object.getPropertyNameSpace(), object.getPropertyLocalName());

    if (object.isLiteral()) {
      resource.addLiteral(property, object.getObject());
    } else {
      RDFNode rdfNode = model.getRDFNode(Node.createURI((String) object.getObject()));
      resource.addProperty(property, rdfNode);
    }

    InfModel infmodel = ModelFactory.createRDFSModel(model);
    ValidityReport validity = infmodel.validate();
    Assert.assertTrue(validity.isValid());

    // TODO exceptions

    FileOutputStream file;
    try {
      file = new FileOutputStream("test.owl");
      model.write(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /*
   * 
   * @Test public void testCreateMessage() {
   * 
   * SPARQLInsertObject object = new SPARQLInsertObject();object.setResource(
   * "http://www.semanticweb.org/ontologies/2010/10/drupal.owl#PeppiThaProgramma"
   * );object.setProperty(
   * "http://www.semanticweb.org/ontologies/2010/10/drupal.owl#usesInstallable"
   * );object.setObject(
   * "http://www.semanticweb.org/ontologies/2010/10/drupal.owl#PeppisGreatModule"
   * ); object.setLiteral(false);
   * 
   * WebClient client = WebClient.create("http://localhost:1234/sparql");
   * Response response = client.post(object); log.info("Response status: " +
   * response.getStatus());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus()); }
   */

  /*
   * 
   * 
   * // TODO: getMessages und searchMessages darauf testen, dass im user-objekt
   * e-mail und Passwort nicht gesetzt sind.
   * 
   * @Test public void testGetNotExistingMessage() throws GackleException{
   * 
   * User user = EntityTestFactory.testUser();
   * 
   * int msgId = 0;
   * 
   * log.info("Get not existing Message (id=" + msgId + ") with username: " +
   * user.getUserName() + ", pass: " + user.getUserName());
   * 
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); client.path(msgId);
   * client.type("application/json"); client.accept("application/json");
   * 
   * Response response = client.get();
   * 
   * Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testCommentNotExistingMessage() throws GackleException{
   * 
   * User user = EntityTestFactory.testUser(); Message message =
   * EntityTestFactory.testMessage();
   * 
   * int msgId = 0;
   * 
   * message.setId(msgId);
   * 
   * log.info("Create comment for not existing Message (id=" + msgId +
   * ") with username: " + user.getUserName() + ", pass: " +
   * user.getUserName());
   * 
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); client.path("/" + msgId +
   * "/comment"); client.type("application/json");
   * client.accept("application/json");
   * 
   * Comment comment = EntityTestFactory.testComment(message, user);
   * 
   * Response response = client.post(comment);
   * 
   * Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testRating() throws GackleException{
   * log.info("Try to rate a Message.");
   * 
   * int rating = 2;
   * 
   * Message message = createMessage();
   * 
   * Assert.assertNotNull(message);
   * 
   * //invoke message-get with messageId
   * log.info("Created test message with id: " + message.getId());
   * 
   * log.info("Add rating for Message.");
   * 
   * User user = EntityTestFactory.testUser();
   * 
   * WebClient client =
   * WebClient.create(GackleUtils.getConfig().getProperty("service.rest.messaging"
   * ), user.getUserName(), user.getPassword(), null);
   * 
   * // POST to ../service/messages/{id}/rating client.path("/" +
   * message.getId() + "/rating"); client.type("application/json");
   * client.accept("application/json");
   * 
   * Response response = client.post("" + rating);
   * 
   * log.info("Response status: " + response.getStatus() + " for: " +
   * client.getBaseURI());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus());
   * 
   * // Check if Rating is in DB
   * 
   * message = getMessage(message.getId());
   * 
   * Assert.assertEquals((float) rating, message.getRating(), 0.0f); }
   * 
   * @Test public void testRatingUnAuthorized() throws GackleException {
   * 
   * log.info("Try to rate a Message without being logged-in");
   * 
   * int rating = 2;
   * 
   * User user = EntityTestFactory.testUser(); Message message =
   * EntityTestFactory.testMessage();
   * 
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getPassword()); WebClient client =
   * WebClient
   * .create(GackleUtils.getConfig().getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null);
   * 
   * //create test-message message =
   * client.type("application/json").accept("application/json").post(message,
   * Message.class); //invoke message-get with messageId
   * log.info("Created test message with id: " + message.getId());
   * 
   * 
   * 
   * log.info("Creating get-message request using previously created test message"
   * );
   * 
   * // Configure Client client.path(message.getId());
   * client.type("application/json"); Message returnedMessage =
   * client.get(Message.class);
   * 
   * log.info("Add rating for Message without username and password.");
   * 
   * client =
   * WebClient.create(GackleUtils.getConfig().getProperty("service.rest.messaging"
   * ));
   * 
   * // POST to ../service/messages/{id}/rating client.path("/" +
   * returnedMessage.getId() + "/rating"); client.type("application/json");
   * client.accept("application/json");
   * 
   * Response response = client.post("" + rating);
   * 
   * log.info("Response status: " + response.getStatus() + " for: " +
   * client.getBaseURI());
   * Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
   * response.getStatus());
   * 
   * }
   * 
   * @Test public void testCreateRating() throws GackleException {
   * 
   * int rating = 2;
   * 
   * User user = EntityTestFactory.testUser(); Message message =
   * EntityTestFactory.testMessage();
   * 
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getPassword()); WebClient client =
   * WebClient
   * .create(GackleUtils.getConfig().getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null);
   * 
   * //create test-message message =
   * client.type("application/json").accept("application/json").post(message,
   * Message.class); //invoke message-get with messageId
   * log.info("Created test message with id: " + message.getId());
   * 
   * 
   * 
   * log.info("Creating get-message request using previously created test message"
   * );
   * 
   * // Configure Client client.path(message.getId());
   * client.type("application/json"); Message returnedMessage =
   * client.get(Message.class);
   * 
   * log.info("Received response: " + String.valueOf(returnedMessage));
   * 
   * log.info("Add rating for Message " + returnedMessage.getId() +
   * " with username: " + user.getUserName() + ", pass: " + user.getPassword());
   * 
   * client =
   * WebClient.create(GackleUtils.getConfig().getProperty("service.rest.messaging"
   * ), user.getUserName(), user.getPassword(), null);
   * 
   * // POST to ../service/messages/{id}/rating client.path("/" +
   * returnedMessage.getId() + "/rating"); client.type("application/json");
   * client.accept("application/json");
   * 
   * Response response = client.post("" + rating);
   * 
   * log.info("Response status: " + response.getStatus() + " for: " +
   * client.getBaseURI());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus());
   * 
   * client =
   * WebClient.create(GackleUtils.getConfig().getProperty("service.rest.messaging"
   * ), user.getUserName(), user.getPassword(), null);
   * 
   * // Rating for new Message should be the last Rating
   * client.path(returnedMessage.getId() + "/rating");
   * client.type("text/plain"); client.accept("text/plain");
   * 
   * float serverRating = Float.parseFloat(client.get(String.class));
   * 
   * log.info("Received response: " + serverRating);
   * 
   * Assert.assertEquals((float) rating, serverRating, 0.1f);
   * 
   * }
   * 
   * @Test public void testAnonymousRating() throws GackleException { User user
   * = EntityTestFactory.testUser(); // Sent HTTP POST request to add message
   * Message message = EntityTestFactory.testMessage();
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getPassword()); WebClient client =
   * WebClient
   * .create(GackleUtils.getConfig().getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); Response response =
   * client.type("application/json").accept("application/json").post(message);
   * //
   * client.path("customers").type("application/json").accept("application/json"
   * ).post(customer); log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus()); }
   * 
   * 
   * @Test public void testCreateMessageWithoutAddress() throws GackleException{
   * User user = EntityTestFactory.testUser(); // Sent HTTP POST request to add
   * message Message message = EntityTestFactory.testMessage();
   * message.setAddress(null); log.info("Adding message " + message.getLabel() +
   * " with username: " + user.getUserName() + ", pass: " + user.getPassword());
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); Message responseMessage =
   * client.type("application/json").accept("application/json").post(message,
   * Message.class); //
   * client.path("customers").type("application/json").accept(
   * "application/json").post(customer);
   * 
   * Assert.assertEquals(responseMessage.getLatitude(), message.getLatitude());
   * Assert.assertEquals(responseMessage.getLongitude(),
   * message.getLongitude()); //
   * Assert.assertEquals(responseMessage.getAddress().getLatitude(),
   * message.getLatitude()); //
   * Assert.assertEquals(responseMessage.getAddress().getLongitude(),
   * message.getLongitude()); }
   * 
   * @Test public void testCreateMessageWithoutKeywords() throws
   * GackleException{ User user = EntityTestFactory.testUser(); // Sent HTTP
   * POST request to add message Message message =
   * EntityTestFactory.testMessage(); message.setKeywords(null);
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getPassword()); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); Response response =
   * client.type("application/json").accept("application/json").post(message);
   * //
   * client.path("customers").type("application/json").accept("application/json"
   * ).post(customer); log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testCreateMessageWithoutPosition() throws
   * GackleException{ User user = EntityTestFactory.testUser(); // Sent HTTP
   * POST request to add message Message message =
   * EntityTestFactory.testMessage(); message.setLatitude(null);
   * message.setLongitude(null); log.info("Adding message " + message.getLabel()
   * + " with username: " + user.getUserName() + ", pass: " +
   * user.getPassword()); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); Response response =
   * client.type("application/json").accept("application/json").post(message);
   * //
   * client.path("customers").type("application/json").accept("application/json"
   * ).post(customer); log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testCreateMessageWithoutStartEndTime() throws
   * GackleException{
   * 
   * // Start/End time are optional.
   * 
   * User user = EntityTestFactory.testUser(); // Sent HTTP POST request to add
   * message Message message = EntityTestFactory.testMessage();
   * message.setStartTime(null); message.setEndTime(null);
   * 
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getPassword()); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); Response response =
   * client.type("application/json").accept("application/json").post(message);
   * log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testCreateMessageWithoutLabel() throws GackleException{
   * User user = EntityTestFactory.testUser(); // Sent HTTP POST request to add
   * message Message message = EntityTestFactory.testMessage();
   * message.setLabel(null); log.info("Adding message " + message.getLabel() +
   * " with username: " + user.getUserName() + ", pass: " + user.getPassword());
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); Response response =
   * client.type("application/json").accept("application/json").post(message);
   * //
   * client.path("customers").type("application/json").accept("application/json"
   * ).post(customer); log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testCreateMessageWithoutText() throws GackleException{
   * User user = EntityTestFactory.testUser(); // Sent HTTP POST request to add
   * message Message message = EntityTestFactory.testMessage();
   * message.setText(null); log.info("Adding message " + message.getLabel() +
   * " with username: " + user.getUserName() + ", pass: " + user.getPassword());
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); Response response =
   * client.type("application/json").accept("application/json").post(message);
   * //
   * client.path("customers").type("application/json").accept("application/json"
   * ).post(customer); log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testCreateMessageUnAuthorized() throws GackleException{
   * // Sent HTTP POST request to add customer Message message =
   * EntityTestFactory.testMessage(); log.info("Adding message " +
   * message.getLabel()); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"));
   * Response response =
   * client.type("application/json").accept("application/json").post(message);
   * //
   * client.path("customers").type("application/json").accept("application/json"
   * ).post(customer); log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testCreateMessageDetailedCheck() throws GackleException {
   * User user = EntityTestFactory.testUser(); // Sent HTTP POST request to add
   * message Message message = EntityTestFactory.testMessage(); // set date-time
   * //message.setStartTime(startTime); log.info("Adding message " +
   * message.getLabel() + " with username: " + user.getUserName() + ", pass: " +
   * user.getPassword()); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); Message returnedMessage =
   * client.type("application/json").accept("application/json").post(message,
   * Message.class); //
   * client.path("customers").type("application/json").accept(
   * "application/json").post(customer); log.info("Received response: " +
   * String.valueOf(returnedMessage));
   * 
   * message.setUser(user); compareMessage(message, returnedMessage); }
   * 
   * @Test public void testDeleteNotExistingMessage() throws GackleException{
   * 
   * User user = EntityTestFactory.testUser();
   * 
   * int msgId = 0;
   * 
   * // Sent HTTP POST request to add message
   * log.info("Try to delete not existing message with id = 0, username: " +
   * user.getUserName() + ", pass: " + user.getUserName());
   * 
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null);
   * 
   * // Sent HTTP DELETE request to delete customer
   * log.info("Deleting test message with id: " + msgId); Response response =
   * client
   * .path(msgId).type("application/json").accept("application/json").delete();
   * log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus()); log.info("Deleted");
   * 
   * }
   * 
   * private void compareMessage(Message message, Message returnedMessage) {
   * Assert.assertEquals(message.getLabel(), returnedMessage.getLabel());
   * Assert.assertEquals(message.getText(), returnedMessage.getText());
   * 
   * //can't check created because set on server
   * //Assert.assertEquals(message.getCreated(), returnedMessage.getCreated());
   * Assert.assertEquals(message.getStartTime(),
   * returnedMessage.getStartTime()); Assert.assertEquals(message.getEndTime(),
   * returnedMessage.getEndTime());
   * 
   * Date now = new Date(System.currentTimeMillis()); if(message.getStartTime()
   * != null && message.getEndTime() != null) { boolean isActive =
   * message.getStartTime().before(now) && message.getEndTime().after(now);
   * Assert.assertEquals(isActive, message.isActive()); }
   * 
   * Assert.assertEquals(message.getLatitude(), returnedMessage.getLatitude());
   * Assert.assertEquals(message.getLongitude(),
   * returnedMessage.getLongitude());
   * 
   * if(message.getAddress() != null) {
   * Assert.assertEquals(message.getAddress().getCity(),
   * returnedMessage.getAddress().getCity());
   * Assert.assertEquals(message.getAddress().getCountry(),
   * returnedMessage.getAddress().getCountry());
   * Assert.assertEquals(message.getAddress().getPostalCode(),
   * returnedMessage.getAddress().getPostalCode());
   * Assert.assertEquals(message.getAddress().getStreet(),
   * returnedMessage.getAddress().getStreet());
   * Assert.assertEquals(message.getAddress().getStreetNumber(),
   * returnedMessage.getAddress().getStreetNumber());
   * Assert.assertEquals(message.getAddress().getLatitude(),
   * returnedMessage.getAddress().getLatitude());
   * Assert.assertEquals(message.getAddress().getLongitude(),
   * returnedMessage.getAddress().getLongitude()); }
   * 
   * Assert.assertArrayEquals(message.getKeywords().keySet().toArray(),
   * returnedMessage.getKeywords().keySet().toArray());
   * 
   * if(message.getUser() != null)
   * Assert.assertEquals(message.getUser().getUserName(),
   * returnedMessage.getUser().getUserName()); }
   * 
   * 
   * @Test public void testGetMessage() throws GackleException { User user =
   * EntityTestFactory.testUser(); // Sent HTTP POST request to add message
   * Message message = EntityTestFactory.testMessage();
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getPassword()); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); //create test-message
   * message =
   * client.type("application/json").accept("application/json").post(message,
   * Message.class); //invoke message-get with messageId
   * log.info("Created test message with id: " + message.getId());
   * log.info("Creating get-message request using previously created test message"
   * ); Response response =
   * client.path(message.getId()).type("application/json").get();
   * log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testGetMessageDetailedCheck() throws GackleException {
   * User user = EntityTestFactory.testUser(); // Sent HTTP POST request to add
   * message Message message = EntityTestFactory.testMessage();
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getPassword()); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); //create test-message
   * message =
   * client.type("application/json").accept("application/json").post(message,
   * Message.class); //invoke message-get with messageId
   * log.info("Created test message with id: " + message.getId());
   * 
   * 
   * 
   * log.info("Creating get-message request using previously created test message"
   * ); Message returnedMessage =
   * client.path(message.getId()).type("application/json").get(Message.class);
   * log.info("Received response: " + String.valueOf(returnedMessage));
   * 
   * message.setUser(user); compareMessage(message, returnedMessage);
   * 
   * }
   * 
   * @Test public void testCreateComment() throws GackleException {
   * 
   * User user = EntityTestFactory.testUser(); // Sent HTTP POST request to add
   * message Message message = EntityTestFactory.testMessage(); message =
   * createMessage(message); log.info("Created test message with id: " +
   * message.getId());
   * 
   * Comment comment = EntityTestFactory.testComment(message, user);
   * 
   * log.info("Creating comment for previously created test message");
   * 
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); client.path(message.getId()
   * + "/comment"); client.accept("application/json");
   * client.type("application/json");
   * 
   * comment = client.post(comment, Comment.class); log.info("Comment (id=" +
   * comment.getId() + ") created."); Assert.assertNotNull(comment);
   * 
   * // Check if Comment is in DB client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); client.path(message.getId()
   * + "/comments/" + comment.getId()); client.accept("application/json");
   * client.type("application/json"); Comment dbComment =
   * client.get(Comment.class);
   * 
   * Assert.assertNotNull(dbComment);
   * 
   * log.info("Comment created.");
   * 
   * }
   * 
   * 
   * @Test public void testCreateCommentUnAuthorized() throws GackleException {
   * User user = EntityTestFactory.testUser(); // Sent HTTP POST request to add
   * message Message message = EntityTestFactory.testMessage();
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getPassword()); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); //create test-message
   * message =
   * client.type("application/json").accept("application/json").post(message,
   * Message.class); //invoke message-get with messageId
   * log.info("Created test message with id: " + message.getId());
   * 
   * Comment comment = EntityTestFactory.testComment(message, user);
   * 
   * log.info("Adding comment without authentication"); WebClient client2 =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"));
   * Response response = client2.path(message.getId() +
   * "/comment").type("application/json")
   * .accept("application/json").post(comment); log.info("Response status: " +
   * response.getStatus());
   * Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testCreateCommentWithoutText() throws GackleException {
   * User user = EntityTestFactory.testUser(); // Sent HTTP POST request to add
   * message Message message = EntityTestFactory.testMessage();
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getPassword());
   * 
   * message = createMessage(message); //invoke message-get with messageId
   * Assert.assertNotNull(message); log.info("Created test message with id: " +
   * message.getId());
   * 
   * //Comment without Text Comment comment =
   * EntityTestFactory.testComment(message, user); comment.setText("");
   * 
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); client.path(message.getId()
   * + "/comment"); client.type("application/json");
   * client.accept("application/json");
   * 
   * Response response = client.post(comment);
   * 
   * Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testSearchMessages() throws GackleException { WebClient
   * client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"));
   * client.path("/1"); //create test-message Response response =
   * client.accept("application/json").get(); //invoke message-get with
   * messageId log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testSearchMessagesLoggedIn() throws GackleException {
   * User user = EntityTestFactory.testUser(); Message message =
   * EntityTestFactory.testMessage(); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); client.path("/1"); //create
   * test-message Response response = client.accept("application/json").get();
   * //invoke message-get with messageId log.info("Response status: " +
   * response.getStatus());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus()); }
   * 
   * @Test public void testDeleteMessage() throws GackleException { User user =
   * EntityTestFactory.testUser(); // Sent HTTP POST request to add message
   * Message message = EntityTestFactory.testMessage();
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getUserName()); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); //create test-message
   * message =
   * client.type("application/json").accept("application/json").post(message,
   * Message.class); //invoke message-get with messageId
   * log.info("Created test message with id: " + message.getId());
   * 
   * // Sent HTTP DELETE request to delete customer
   * log.info("Deleting test message with id: " + message.getId()); Response
   * response =
   * client.path(message.getId()).type("application/json").accept("application/json"
   * ).delete(); log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.OK.getStatusCode(),
   * response.getStatus()); log.info("Deleted");
   * 
   * // See if Message is still there.
   * 
   * Message m = getMessage(message.getId());
   * 
   * Assert.assertNull(m); }
   * 
   * @Test public void testDeleteMessageUnAuthorized() throws GackleException {
   * User user = EntityTestFactory.testUser(); // Sent HTTP POST request to add
   * message Message message = EntityTestFactory.testMessage();
   * log.info("Adding message " + message.getLabel() + " with username: " +
   * user.getUserName() + ", pass: " + user.getUserName()); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); //create test-message
   * message =
   * client.type("application/json").accept("application/json").post(message,
   * Message.class); //invoke message-get with messageId
   * log.info("Created test message with id: " + message.getId());
   * 
   * WebClient client2 =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging")); //
   * Sent HTTP DELETE request to delete customer
   * log.info("Deleting test message with id: " + message.getId()); Response
   * response =client2.path(message.getId()).type("application/json").accept(
   * "application/json").delete(); log.info("Response status: " +
   * response.getStatus());
   * Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
   * response.getStatus()); log.info("Not Deleted");
   * 
   * // See if Message is still there.
   * 
   * Message m = getMessage(message.getId()); Assert.assertNotNull(m); }
   * 
   * @Test public void testDeleteMessageWrongUser() throws GackleException {
   * 
   * User user = EntityTestFactory.testUser();
   * 
   * // Sent HTTP POST request to add message Message message =
   * EntityTestFactory.testMessage(); message.setUser(user);
   * 
   * Message dbMsg = createMessage(message); Assert.assertNotNull(dbMsg);
   * 
   * User user2 = EntityTestFactory.testUser2(); WebClient client2 =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user2.getUserName(), user2.getPassword(), null); // Sent HTTP DELETE
   * request to delete customer log.info("Deleting test message with id: " +
   * dbMsg.getId()); Response response =
   * client2.path(dbMsg.getId()).type("application/json"
   * ).accept("application/json").delete();
   * 
   * log.info("Response status: " + response.getStatus());
   * Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
   * response.getStatus()); log.info("Not Deleted");
   * 
   * // See if Message is still there.
   * 
   * Message m = getMessage(dbMsg.getId()); Assert.assertNotNull(m); }
   * 
   * 
   * 
   * @Test public void testDeleteCustomer() throws Exception { // Sent HTTP GET
   * request to get customer log.info("Sent HTTP GET request to get customer");
   * WebClient client = WebClient.create(Const.CUSTOMERMANAGEMENTSERVICE);
   * Customer c =
   * client.path("customers/0").type("application/xml").accept("application/xml"
   * ).get(Customer.class); log.info("Customer name: " + c.getName());
   * 
   * // Sent HTTP DELETE request to delete customer client =
   * WebClient.create(Const.CUSTOMERMANAGEMENTSERVICE);
   * client.path("customers/0"
   * ).type("application/xml").accept("application/xml").delete();
   * log.info("Deleted customer " + customer.getName());
   * 
   * // Sent HTTP GET request to get customer
   * log.info("Sent HTTP GET request to get customer"); client =
   * WebClient.create(Const.CUSTOMERMANAGEMENTSERVICE); c =
   * client.path("customers/0"
   * ).type("application/xml").accept("application/xml").get(Customer.class);
   * Assert.assertNull(c); }
   * 
   * 
   * 
   * public Message getMessage(long id){
   * 
   * User user = EntityTestFactory.testUser();
   * 
   * log.info("Get message with id=" + id + "."); WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"));
   * 
   * log.info("Creating get-message request.");
   * 
   * client.path(id); client.type("application/json");
   * client.accept("application/json");
   * 
   * Message message = null;
   * 
   * try{ message = client.get(Message.class); }catch (WebApplicationException
   * we){ log.error(we.getMessage()); }
   * 
   * if (message != null) log.info("Message received."); else
   * log.info("No Message received.");
   * 
   * return message; }
   * 
   * public Message createMessage(){
   * 
   * Message message = EntityTestFactory.testMessage();
   * 
   * return createMessage(message); }
   * 
   * public Message createMessage(Message message){
   * 
   * User user = EntityTestFactory.testUser();
   * 
   * return createMessage(message, user); }
   * 
   * public Message createMessage(Message message, User user){
   * 
   * // Sent HTTP POST request to add message log.info("Adding message " +
   * message.getLabel() + " with username: " + user.getUserName() + ", pass: " +
   * user.getUserName());
   * 
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null);
   * client.type("application/json"); client.accept("application/json");
   * 
   * Message returnedMessage = client.post(message, Message.class);
   * 
   * if (returnedMessage == null){ log.error("Could not create Message!");
   * return null; }
   * 
   * log.info("Created test message with id: " + returnedMessage.getId());
   * 
   * // Verify the results: Message really created?
   * 
   * 
   * Message dbMessage = getMessage(returnedMessage.getId());
   * 
   * if (dbMessage != null && dbMessage.getId() == returnedMessage.getId()){
   * log.info("Message (id=" + dbMessage.getId() + ") in DB created."); }else{
   * log.error("Message (id=" + dbMessage.getId() + ") was not created in DB.");
   * }
   * 
   * return dbMessage; }
   * 
   * public Comment createComment(Message message, User user){ Comment comment =
   * EntityTestFactory.testComment(message, user);
   * 
   * return createComment(message, user, comment); }
   * 
   * public Comment createComment(Message message, User user, Comment comment){
   * 
   * // Sent HTTP POST request to add message
   * log.info("Adding comment for Message (id=" + message.getLabel() +
   * ") with username: " + user.getUserName() + ", pass: " +
   * user.getUserName());
   * 
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); client.path(message.getId()
   * + "/comment"); client.type("application/json");
   * client.accept("application/json");
   * 
   * comment = client.post(comment, Comment.class);
   * 
   * if (comment == null){ log.error("Could not create Comment!"); return null;
   * }
   * 
   * log.info("Created test comment with id: " + comment.getId());
   * 
   * // Verify the results: Comment really created?
   * 
   * Comment dbComment = getComment(message, comment.getId());
   * 
   * if (dbComment != null && dbComment.getId() == comment.getId()){
   * log.info("Comment (id=" + dbComment.getId() + ") in DB created."); }else{
   * log.error("Comment (id=" + comment.getId() + ") was not created in DB."); }
   * 
   * return dbComment; }
   * 
   * public Comment getComment(Message message, long commentId){
   * 
   * User user = EntityTestFactory.testUser();
   * log.info("Get comment for Message (id=" + message.getId() +
   * ") with username: " + user.getUserName() + ", pass: " +
   * user.getUserName());
   * 
   * WebClient client =
   * WebClient.create(ServerConfig.getProperty("service.rest.messaging"),
   * user.getUserName(), user.getPassword(), null); client.path(message.getId()
   * + "/comment/" + commentId); client.type("application/json");
   * client.accept("application/json");
   * 
   * Comment comment = client.get(Comment.class);
   * 
   * if (comment == null){ log.error("No comment received."); } else {
   * log.error("Comment (id=" + comment.getId() + " received."); }
   * 
   * return comment; }
   */
}

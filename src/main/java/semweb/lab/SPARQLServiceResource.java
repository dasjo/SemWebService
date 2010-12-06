package semweb.lab;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;

@Path("/messages")
@WebService(endpointInterface = "at.gackle.core.ws.interfaces.IMessageService", serviceName = "MessageService")
public class SPARQLServiceResource implements SPARQLService {

	/* logger */
	private static Log logger = LogFactory.getLog(SPARQLServiceResource.class);

	@Context
	MessageContext messageContext;

  @Override
  @GET
  public String sayHello() {
    logger.debug("saying hello");
    return "hallo";
  }

	/*

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@Override
	public Message createMessage(Message message) {
		logger.info("Create message request");

		User myUser = GackleUtils.getUserFromCXFMessage(messageContext, true);
		message.setUser(myUser);
		// Check if received Message is valid

		// Label can't be null or empty
		if (message.getLabel() == null || "".equals(message.getLabel())){
			throw new GackleWebException(Status.BAD_REQUEST, "Label is empty.");
		}

		// Check if Message is null or empty
		if (message.getText() == null || "".equals(message.getText())){
			throw new GackleWebException(Status.BAD_REQUEST, "Message is empty.");
		}

		// Message text must be shorter than 480 characters
		if (message.getText().length() > 480){
			throw new GackleWebException(Status.BAD_REQUEST, "Message is to long: max 480 characters allowed.");
		}

		// Message label must be shorter than 50 characters
		if (message.getLabel().length() > 50){
			throw new GackleWebException(Status.BAD_REQUEST, "Message label is to long: max 50 characters allowed.");
		}



		// Check if endtime is after starttime
		if (message.getStartTime() != null && message.getEndTime() != null){
			if (message.getEndTime().before(message.getStartTime())){
				throw new GackleWebException(Status.BAD_REQUEST, "Invalid Start/End time.");
			}
		}

		// Check if longitude is set.
		if (message.getLongitude() == null || message.getLongitude() == 0){
			throw new GackleWebException(Status.BAD_REQUEST, "Longitude is not set.");
		}

		// Check if latitude is set.
		if (message.getLatitude() == null || message.getLatitude() == 0){
			throw new GackleWebException(Status.BAD_REQUEST, "Latitude is not set.");
		}

		// Client can't set creation date: override it
		// Creation date is set by the PLSQL Function f_insert_message. No need to set it here. 

		// Client can't activate/deactivate message: override it
		// By default every new message is activated.
		message.setActive(true);

		Message createdMessage;

		try {
			createdMessage = sm.createMessage(message);

		} catch (GackleException e) {
			throw new GackleWebException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}

		return createdMessage;
	}

	@GET
	@Produces("application/json")
	@Path("/{id}/")
	@Override
	public Message getMessage(@PathParam("id") long id) {
		logger.info("GetMessage request for messageId: " + id);
		if (id < 0) {
			throw new GackleWebException(Status.BAD_REQUEST, "Invalid id given.");
		}
		try {
			Message message = sm.getMessage(id);

			return message;
		} catch (GackleException e) {
			throw new GackleWebException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@GET
	@Produces ("application/json")
	@Consumes ("application/json")
	@Path ("/{msgId}/comments/{commentId}")
	public Comment getComment(@PathParam("msgId") long msgId, @PathParam("commentId") long commentId){
		logger.info("Get Comment request for messageId: " + msgId + " and comment (id=" + commentId + ")");

		Comment retVal = null;
		
		try {
			// Check if Message is valid.
			Message message = sm.getMessage(msgId);

			if (message == null){
				// Message not found
				throw new GackleWebException(Status.BAD_REQUEST, "Message with id " + msgId + " not found");
			}

			if (msgId < 0 || commentId < 0){
				throw new GackleWebException(Status.BAD_REQUEST, "Ids have to be positive.");
			}
			
			SortedMap<Long, Comment> list = message.getComments();
			
			if (list == null || list.size() == 0){
				throw new GackleWebException(Status.BAD_REQUEST, "No comments found for this Message.");
			}
			
			retVal = list.get(commentId);
			
		} catch (GackleException e) {
			throw new GackleWebException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
		
		return retVal;
	}
	
	@GET
	@Produces ("application/json")
	@Consumes ("application/json")
	@Path ("/{msgId}/comments")
	public List<Comment> getComments(@PathParam("msgId") long msgId){
		logger.info("Get Comment List request for messageId: " + msgId + ".");

		List<Comment> retVal = null;
		
		try {
			// Check if Message is valid.
			Message message = sm.getMessage(msgId);

			if (message == null){
				// Message not found
				throw new GackleWebException(Status.BAD_REQUEST, "Message with id " + msgId + " not found");
			}

			if (msgId < 0){
				throw new GackleWebException(Status.BAD_REQUEST, "Ids have to be positive.");
			}
			
			SortedMap<Long, Comment> list = message.getComments();
			
			if (list == null || list.size() == 0){
				throw new GackleWebException(Status.BAD_REQUEST, "No comments found for this Message.");
			}
			
			retVal = new ArrayList<Comment>(list.values());
			
		} catch (GackleException e) {
			throw new GackleWebException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
		
		return retVal;
	}

	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/{id}/comment")
	@Override
	public Comment commentMessage(Comment comment, @PathParam("id") long id){
		logger.info("Comment request for messageId: " + id);


		try {
			// Check if Message is valid.
			Message message = sm.getMessage(id);

			if (message == null){
				// Message not found
				throw new GackleWebException(Status.BAD_REQUEST, "Message with id " + id + " not found");
			}

			// Check if Comment is valid.
			if (comment.getText() == null || "".equals(comment.getText())){
				// Comment invalid.
				throw new GackleWebException(Status.BAD_REQUEST, "Comment is not valid");
			}

			// Comment must be shorter than 160 characters
			if (comment.getText().length() > 160){
				throw new GackleWebException(Status.BAD_REQUEST, "Comment is to long: max 160 characters allowed.");
			}

			// Check if mID in comment equals the id parameter
			if (comment.getMid() != id){
				// mID is not valid.
				throw new GackleWebException(Status.BAD_REQUEST, "Message ID  is not valid");			
			}

			// Set creationDate
			comment.setCreated(new Date());

			// Check if user is logged in and set him as author
			User myUser = GackleUtils.getUserFromCXFMessage(messageContext, true);
			comment.setUser(myUser);
			
			// Create Comment	
			Comment cmt = sm.createComment(comment);			
			
			cmt.setUser(myUser);

			logger.info("Comment (id=" + cmt.getId() + ") created.");
			
			return cmt;

		} catch (GackleException e) {
			throw new GackleWebException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}

	}

	@GET
	@Produces("application/json")
	@Path("/search")
	public List<Message> searchMessages(@QueryParam("q") String q,
			@QueryParam("ll") String ll, @QueryParam("spn") String spn,
			@QueryParam("tag") List<String> tags) {
		logger.info("Search request with q: " + q
				+ ",ll: " + ll
				+ ",spn: " + spn
				+ ",tag: " + (tags != null ? tags.toString():""));

		try {

			logger.debug("searchMessage called!");

			if (q == null && ll == null && spn == null) {
				return sm.getMessages();
			}

			SearchParameter sp = new SearchParameter(q, ll, spn, tags);
			List<Message> messages = sm.search(sp);
			List<Message> messagesWithRating = new ArrayList<Message>();
			// Set rating for selected messages

			User myUser = GackleUtils.getUserFromCXFMessage(messageContext);

			for (Message m : messages){

				m.setRating(sm.getRating(m));
				messagesWithRating.add(m);

				// Set userrating
				if (myUser != null){

					int userRating = sm.getRating(m.getId(), myUser.getId());
					m.setUserRating(userRating);
				}
			}

			return messagesWithRating;

		} catch (Exception e) {
			throw new WebApplicationException(e);
			//Response response = Response.status(Status.BAD_REQUEST).build();
			//response.
			//throw new WebApplicationException(Response.status(Status.BAD_REQUEST).)
		}
	}

	@DELETE
	@Produces("application/json")
	@Path("/{id}/")
	@Override
	public boolean deleteMessage(@PathParam("id") long messageId) {		
		logger.info("Delete request for messageId: " + messageId);

		User myUser = GackleUtils.getUserFromCXFMessage(messageContext, true);
		
		// Check if Message exists
		Message msg = null;
		try{
			msg = getMessage(messageId);
		}catch (WebApplicationException e){
			
		}
		
		if (msg == null){
			logger.info("Message (id=" + messageId + ") does not exist.");

			return true;
		}
		
		// Only the creator of the message can delete the message
		
		if (messageId < 0){
			throw new GackleWebException(Status.BAD_REQUEST, "Id must be positive.");
		}
		
		boolean returnValue = false;
		
		try {
			
			User creator = sm.getMessage(messageId).getUser();
			logger.info("Creator id: " + creator.getId() + " Request-User: " + myUser.getId());
			
			if (creator.getId() != myUser.getId()){
				throw new GackleWebException(Status.UNAUTHORIZED, "Only the creator of the message can delete the message.");
			}
			
			returnValue = sm.deleteMessage(messageId);
			
		} catch (GackleException e) {
			throw new GackleWebException(Status.BAD_REQUEST, e.getMessage());
		}
		
		return returnValue;
	}

	@POST
	@Produces("application/json")
	@Consumes ("application/json")
	@Path("/{id}/rating")
	@Override
	public float rateMessage(@PathParam("id") long messageId, int rating) {

		logger.info("Rating request for messageId: " + messageId + ", rating: " + rating);

		// Only a registered user can rate a message.
		// Check if user is logged in.
		User myUser = GackleUtils.getUserFromCXFMessage(messageContext, true);

		// Rating must be between 0 and 5
		if (rating < 0 || rating > 5){
			throw new GackleWebException(Status.BAD_REQUEST, "Rating must be between 0 and 5.");
		}

		float actualRating = 0;

		// Check if user has already rated.
		try {
			int userRating = sm.getRating(messageId, myUser.getId());

			// if userRating == 0 user has already rated => update
			if (userRating != 0){

				logger.info("User has already rated this message: Update the rating.");
				sm.updateRating(messageId, myUser.getId(), rating);

			}else{
				// User has not rated yet.
				logger.info("User has not yet rated this message: Create new rating.");

				// Insert rating in DB
				sm.createRating(messageId, myUser.getId(), rating);	
			}

			Message message = null;

			message = sm.getMessage(messageId);
			actualRating = message.getRating();

		} catch (Exception e){
			throw new GackleWebException(Status.BAD_REQUEST, e.getMessage());
		}

		return actualRating;
	}

	@GET
	@Produces ("text/plain")
	@Path ("/{id}/rating")
	public float getRating(@PathParam("id") long messageID){

		logger.info("Rating GET request for messageId: " + messageID);
		
		// Get the rating for a message

		float rating = 0.0f;

		try {
			Message message = sm.getMessage(messageID);

			rating = message.getRating();

		} catch (GackleException e) {
			throw new GackleWebException(Status.BAD_REQUEST, e.getMessage());
		}

		return rating;
	}
	
	*/



}

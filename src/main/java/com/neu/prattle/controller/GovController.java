package com.neu.prattle.controller;

import com.neu.prattle.model.Message;
import com.neu.prattle.service.MessageService;
import com.neu.prattle.service.MessageServiceImpl;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(value = "/gov")
public class GovController {
  private MessageService messageService = MessageServiceImpl.getInstance();

  /***
   *
   */
  @GET
  @Path("/query")
  @Produces(MediaType.APPLICATION_JSON)
  @JsonIgnore
  public Response findMessages(@QueryParam("from") String from, @QueryParam("to") String to,
                           @QueryParam("user") String user ) throws ParseException {
    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    Timestamp timeFrom = new Timestamp(formatter.parse(from).getTime());
    Timestamp timeTo = new Timestamp(formatter.parse(to).getTime());
    List<Message> messages =messageService.fetchUserTimeRange(timeFrom,timeTo,user);
    if (messages!=null){
      return Response.ok().entity(messages).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
    else {
      return Response.status(409).build();
    }




  }





}

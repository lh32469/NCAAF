package org.gpc4j.ncaaf.resources;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Path("status")
public class Status {


  @GET
  @Timed
  @Path("ping")
  public Response ping() {

    Response.ResponseBuilder builder = Response.ok("pong");

    try {
      builder.header("X-Server", InetAddress.getLocalHost().toString());
    } catch (UnknownHostException e) {
      builder.header("X-Error", e.toString());
    }
    
    return builder.build();

  }
}

package com.heymoose.resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.job.AppStatCalculatorTask;
import com.heymoose.job.OfferStatCalculatorTask;
import com.heymoose.job.UserStatCalculatorTask;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;

@Path("tasks")
@Singleton
public class TaskResource {

  private final UserStatCalculatorTask userStatCalculatorTask;
  private final AppStatCalculatorTask appStatCalculatorTask;
  private final OfferStatCalculatorTask offerStatCalculatorTask;
  
  @Inject
  public TaskResource(UserStatCalculatorTask userStatCalculatorTask,
                      AppStatCalculatorTask appStatCalculatorTask,
                      OfferStatCalculatorTask offerStatCalculatorTask) {
    this.userStatCalculatorTask = userStatCalculatorTask;
    this.appStatCalculatorTask = appStatCalculatorTask;
    this.offerStatCalculatorTask = offerStatCalculatorTask;
  }
  
  @GET
  @Path("user-stat-calculator")
  public Response userStatCalculator() {
    userStatCalculatorTask.run(DateTime.now());
    return Response.ok().build();
  }
  
  @GET
  @Path("app-stat-calculator")
  public Response appStatCalculator(@QueryParam("allWeek") @DefaultValue("false") boolean allWeek) {
    appStatCalculatorTask.run(DateTime.now(), allWeek);
    return Response.ok().build();
  }
  
  @GET
  @Path("offer-stat-calculator")
  public Response offerStatCalculator() {
    offerStatCalculatorTask.run(DateTime.now());
    return Response.ok().build();
  }
}

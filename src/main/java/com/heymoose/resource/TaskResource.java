package com.heymoose.resource;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.job.AppStatCalculatorTask;
import com.heymoose.job.SettingsCalculatorTask;
import com.heymoose.job.UserStatCalculatorTask;

@Path("tasks")
@Singleton
public class TaskResource {

  private final SettingsCalculatorTask settingsCalculatorTask;
  private final UserStatCalculatorTask userStatCalculatorTask;
  private final AppStatCalculatorTask appStatCalculatorTask;
  
  @Inject
  public TaskResource(SettingsCalculatorTask settingsCalculatorTask,
                      UserStatCalculatorTask userStatCalculatorTask,
                      AppStatCalculatorTask appStatCalculatorTask) {
    this.settingsCalculatorTask = settingsCalculatorTask;
    this.userStatCalculatorTask = userStatCalculatorTask;
    this.appStatCalculatorTask = appStatCalculatorTask;
  }
  
  @GET
  @Path("settings-calculator")
  public Response settingsCalculator() {
    settingsCalculatorTask.run(DateTime.now());
    return Response.ok().build();
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
}

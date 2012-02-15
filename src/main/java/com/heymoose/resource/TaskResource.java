package com.heymoose.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.job.SettingsCalculatorTask;

@Path("tasks")
@Singleton
public class TaskResource {

  private final SettingsCalculatorTask settingsCalculatorTask;
  
  @Inject
  public TaskResource(SettingsCalculatorTask settingsCalculatorTask) {
    this.settingsCalculatorTask = settingsCalculatorTask;
  }
  
  @GET
  @Path("settings-calculator")
  public Response settingsCalculator() {
    settingsCalculatorTask.run(DateTime.now());
    return Response.ok().build();
  }
}

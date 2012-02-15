package com.heymoose.resource;

import java.util.Map;

import com.heymoose.domain.settings.Settings;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.XmlSettings;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.representation.Form;

import static com.heymoose.util.WebAppUtil.checkNotNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

@Singleton
@Path("settings")
public class SettingResource {

  private final Settings settings;

  @Inject
  public SettingResource(Settings settings) {
    this.settings = settings;
  }

  @GET
  @Path("value")
  @Transactional
  public String value(@QueryParam("name") String name) {
    checkNotNull(name);
    return settings.getString(name);
  }
  
  @GET
  @Transactional
  public XmlSettings all() {
    XmlSettings xmlSettings = new XmlSettings();
    Map<String, String> settingsMap = settings.map();
    xmlSettings.M = settingsMap.get(Settings.M);
    xmlSettings.Q = settingsMap.get(Settings.Q);
    xmlSettings.Davg = settingsMap.get(Settings.D_AVG);
    return xmlSettings;
  }
  
  @PUT
  @Transactional
  public void update(@Context HttpContext context) {
    Form params = context.getRequest().getEntity(Form.class);
    for (String name : params.keySet()) {
      settings.set(name, params.getFirst(name));
    }
  }
}

package com.heymoose.rest.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.heymoose.rest.context.AppContextListener;
import com.heymoose.rest.context.SettingsModule;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.Properties;

public class Launcher {

  private static GrizzlyWebServer server;
  
  private static Properties settings() {
    Injector injector = Guice.createInjector(new SettingsModule());
    return injector.getInstance(Key.get(Properties.class, Names.named("settings")));
  }

  public static void production() throws IOException {
    int port = Integer.parseInt(settings().getProperty("port"));
		launch(port, AppContextListener.class);
  }

  public static class StubServlet extends HttpServlet { }

  public static void launch(int port, Class<? extends GuiceServletContextListener> contextListenerClass) throws IOException {
    if (server != null)
      return;
    server = new GrizzlyWebServer(port);
		ServletAdapter adapter = new ServletAdapter(new StubServlet());
		adapter.addServletListener(contextListenerClass.getName());
		adapter.addFilter(new GuiceFilter(), "GuiceFilter", null);
		server.addGrizzlyAdapter(adapter, new String[]{ "/" });
		server.start();
  }

	public static void main(String[] args) throws Exception {
    production();
	}
}
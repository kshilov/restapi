package com.heymoose.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.heymoose.context.CommonModule;
import com.heymoose.context.ConsoleModule;
import com.heymoose.context.SettingsModule;
import com.heymoose.infrastructure.ActionImporter;
import com.heymoose.domain.model.counter.BufferedClicks;
import com.heymoose.domain.model.counter.BufferedShows;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class ConsoleLauncher {

  private static final Injector injector;

  static {
    // create the application
    injector = Guice.createInjector(
        Stage.PRODUCTION,
        new SettingsModule(),
        new CommonModule(),
        new ConsoleModule()
    );

    // arrange flushes
    final BufferedShows bufferedShows = injector.getInstance(BufferedShows.class);
    final BufferedClicks bufferedClicks = injector.getInstance(BufferedClicks.class);
    new Thread(bufferedShows).start();
    new Thread(bufferedClicks).start();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        bufferedShows.flushAll();
        bufferedClicks.flushAll();
      }
    });
  }

  public static void main(String[] args) throws Exception {

    PosixParser parser = new PosixParser();
    HelpFormatter formatter = new HelpFormatter();

    Options options = new Options()
        .addOption("h", "help", false, "print this help")
        .addOption("ia", "import-actions", false, "import actions manually")
        .addOption("iad", "import-actions-days", true, "how many days to import actions for, default 1")
        .addOption("iak", "import-actions-key", true, "only import actions for this affiliate, default all affiliates");

    CommandLine commandLine = parser.parse(options, args);

    if (commandLine.hasOption("ia")) {
      int days = Integer.parseInt(commandLine.getOptionValue("iad", "1"));
      long aff = Long.parseLong(commandLine.getOptionValue("iak", "-1"));

      ActionImporter actionImporter = injector.getInstance(ActionImporter.class);
      actionImporter.setPeriod(days);
      if (actionImporter.advUrls().containsKey(aff)) {
        HashMap<Long, URL> map = new HashMap<Long, URL>();
        map.put(aff, actionImporter.advUrls().get(aff));
        actionImporter.setAdvUrls(map);
      }

      actionImporter.run();

    } else {
      formatter.printHelp("ConsoleLauncher", options);
    }
  }
}

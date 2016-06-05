package com.esindexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.daemon.DaemonContext;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.esindexer.preferences.IPreferences;
import com.esindexer.preferences.PreferencesImpl;
import com.esindexer.xstream.model.ApplicationPreferences;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * @author Roland Quast (roland@formreturn.com)
 *
 */
public class Main {

  private static Logger LOG = Logger.getLogger(Main.class);

  public static void main(String[] args) {
    new Main(args);
  }

  private IPreferences preferences;

  public Main(String[] args) {
    if (args == null) {
      args = new String[] {};
    }
    initConsoleLog4J();
    LOG.info("Starting ES Indexer Daemon");
    loadPreferences();
    runWatchers();
  }

  private void runWatchers() {
    ApplicationPreferences ap = this.preferences.getApplicationPreferences();

    try {
        processFile(ap.getAofFilePath());
    } catch (Exception ex) {
	LOG.warn(ex.getMessage());
    }
  }


  private void loadPreferences() {
    this.preferences = new PreferencesImpl();
    try {
      this.preferences.load();
    } catch (ClassNotFoundException e) {
      LOG.fatal(e, e);
      System.exit(0);
    } catch (IOException e) {
      LOG.fatal(e, e);
      System.exit(0);
    }
  }



  private void processFile(String aofFilePath) throws IOException,
      InterruptedException {

      // TODO: read the AOF file here.
      FileInputStream fis = new FileInputStream(aofFilePath);
      AppendOnlyFileReader r = new AppendOnlyFileReader(fis);
      
      
      
      // finally
      fis.close();
      
  }

  public void initConsoleLog4J() {
    PatternLayout layout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss,SSS} [%t] %-5p %c %x - %m%n");
    try {
      String filename = PreferencesImpl.getPreferencesDir().getPath() + File.separator
          + "indexer.log";
      DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender(layout, filename,
          "'.'yyyy-MM-dd-HH");
      Logger.getRootLogger().addAppender(rollingAppender);
      Logger.getRootLogger().setLevel(Level.INFO);
    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  public static void init(DaemonContext context) {
  }

  public void stop() {
    LOG.info("Stopping ElasticSearch Indexer Daemon");
  }

  public void destroy() {
  }

}

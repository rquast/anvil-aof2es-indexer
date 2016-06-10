package com.aof2es;

import java.io.File;
import java.io.IOException;

import org.apache.commons.daemon.DaemonContext;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.aof2es.preferences.IPreferences;
import com.aof2es.preferences.PreferencesImpl;

/**
 * @author Roland Quast (rquast@rolandquast.com)
 *
 */
public class Main {

    private static Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) {
	new Main();
    }

    private IPreferences preferences;

    public Main() {
	init();
    }
    
    public void init() {
	initLogging();
	LOG.info("Starting AOF2ES Daemon");
	loadPreferences();
	try {
	    Indexer indexer = new Indexer();
	    indexer.setPreferences(this.preferences);
	    indexer.connect();
	    Reader.read(this.preferences, indexer);
	} catch (Exception ex) {
	    LOG.fatal(ex.getLocalizedMessage(), ex);
	    // sleep to throttle in case this daemon respawns quickly.
	    try {
		Thread.sleep(3000);
	    } catch (InterruptedException e) {
	    }
	    System.exit(0);
	}
    }

    private void loadPreferences() {
	this.preferences = new PreferencesImpl();
	try {
	    this.preferences.load();
	} catch (ClassNotFoundException e) {
	    LOG.fatal(e.getLocalizedMessage(), e);
	    System.exit(0);
	} catch (IOException e) {
	    LOG.fatal(e.getLocalizedMessage(), e);
	    System.exit(0);
	}
    }

    public void initLogging() {
	PatternLayout layout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss,SSS} [%t] %-5p %c %x - %m%n");
	try {
	    String filename = PreferencesImpl.getPreferencesDir().getPath() + File.separator + "indexer.log";
	    DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender(layout, filename,
		    "'.'yyyy-MM-dd-HH");
	    Logger.getRootLogger().addAppender(rollingAppender);
	    Logger.getRootLogger().setLevel(Level.INFO);
	} catch (IOException e) {
	    LOG.error(e.getLocalizedMessage(), e);
	}
    }

    public static void init(DaemonContext context) {
	new Main();
    }

    public void stop() {
	
	// TODO: should do this more cleanly by interrupting.
	LOG.info("Stopping AOF2ES Daemon");
	System.exit(0);
	
    }

    public void destroy() {
    }

}

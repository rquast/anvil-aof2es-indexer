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
import com.aof2es.xstream.model.ApplicationPreferences;

/**
 * @author Roland Quast (rquast@rolandquast.com)
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
	LOG.info("Starting AOF2ES Daemon");
	loadPreferences();
	ApplicationPreferences ap = this.preferences.getApplicationPreferences();
	try {
	    Indexer indexer = new Indexer(ap);
	    Reader.read(ap.getAofFilePath(), indexer);
	} catch (Exception ex) {
	    LOG.error(ex);
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

    public void initConsoleLog4J() {
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
    }

    public void stop() {
	LOG.info("Stopping AOF2ES Daemon");
    }

    public void destroy() {
    }

}

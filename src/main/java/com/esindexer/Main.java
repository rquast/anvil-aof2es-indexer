package com.esindexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.daemon.DaemonContext;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.esindexer.preferences.IPreferences;
import com.esindexer.preferences.PreferencesImpl;
import com.esindexer.xstream.model.ApplicationPreferences;

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
		loadPreferences();
		initConsoleLog4J();
		LOG.info("Starting ES Indexer Daemon");
		loadConfiguration(args);
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

	private void loadConfiguration(String[] args) {
		ApplicationPreferences ap = this.preferences.getApplicationPreferences();
		ArrayList<String> processPaths = ap.getProcessPaths();
		if ( args.length >= 1 ) {
			File processPath = new File(args[0]);
			if ( processPath.exists() && processPath.isFile() && processPath.canRead() ) {
				if ( !(processPaths.contains(processPath)) ) {
					processPaths.add(processPath.getPath());
					try {
						this.preferences.save();
					} catch (IOException e) {
						LOG.fatal(e, e);
					}
				}
			} else {
				LOG.fatal("Could not load path for processing: " + processPath);
				System.exit(0);
			}
		}
		for ( String processPath: processPaths ) {
			try {
				processFile(processPath);
			} catch (FileNotFoundException e) {
				LOG.fatal(e, e);
			} catch (IOException e) {
				LOG.fatal(e, e);
			} catch (ParseException e) {
				LOG.fatal(e, e);
			} catch (java.text.ParseException e) {
				LOG.fatal(e, e);
			}
		}
	}

	private void processFile(String path) throws FileNotFoundException,
			IOException, ParseException, java.text.ParseException {

		JSONParser parser = new JSONParser();

		Object obj = parser.parse(new FileReader(path));

		JSONArray pageList = (JSONArray) obj;

		for (Object pageObj : pageList.toArray()) {
			JSONObject pageJObj = (JSONObject) pageObj;
			String modifiedStr = (String) pageJObj.get("modified");
			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date date = format.parse(modifiedStr);
			System.out.println(date);
			
			System.out.println(pageJObj.get("title"));
			System.out.println(pageJObj.get("path"));
			System.out.println(pageJObj.get("url"));
			// System.out.println(pageJObj.get("content"));
		}

	}

	public void initConsoleLog4J() {

		PatternLayout layout = new PatternLayout(
				"%d{dd MMM yyyy HH:mm:ss,SSS} [%t] %-5p %c %x - %m%n");
		try {
			String filename = this.preferences.getPreferencesDir().getPath()
					+ "es_indexer.log";
			DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender(
					layout, filename, "'.'yyyy-MM-dd-HH");
			Logger.getRootLogger().addAppender(rollingAppender);
			Logger.getRootLogger().setLevel(Level.INFO);
		} catch (IOException e) {
			LOG.warn(e.getLocalizedMessage(), e);
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

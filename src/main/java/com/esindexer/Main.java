package com.esindexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;

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
import com.esindexer.xstream.model.ProcessedIndex;

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
		if ( args == null ) {
			args = new String[]{};
		}		
		initConsoleLog4J();
		LOG.info("Starting ES Indexer Daemon");
		loadPreferences();
		loadConfiguration(args);
		runWatchers();
	}

	private void runWatchers() {
		ApplicationPreferences ap = this.preferences.getApplicationPreferences();
		ArrayList<ProcessedIndex> indexes = ap.getProcessedIndexes();
		for (ProcessedIndex index : indexes) {
			try {
				processFile(index, getConfigJson(index));
			} catch (IOException e) {
				LOG.error(e, e);
			} catch (InterruptedException e) {
				LOG.info(e, e);
			} catch (ParseException e) {
				LOG.error(e, e);
			}
		}
	}

	private ConfigJson getConfigJson(ProcessedIndex index) throws ParseException, FileNotFoundException, IOException {
		
		ConfigJson configJson = new ConfigJson();
		
		File basePath = (new File(index.getPath())).getParentFile();
		String configJsonPath = basePath.getPath() + File.separator + "esindexer_config.json";
		
		JSONParser parser = new JSONParser();

		Object obj = parser.parse(new FileReader(configJsonPath));

		JSONObject confObj = (JSONObject) obj;

		configJson.setGenerator((String) confObj.get("generator"));
		configJson.setIndex((String) confObj.get("index"));
		
		JSONArray nodesArr = (JSONArray) confObj.get("nodes");
		configJson.setNodes(new ArrayList(Arrays.asList(nodesArr.toArray())));
		
		
		
		
		return configJson;

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
		ArrayList<ProcessedIndex> indexes = ap.getProcessedIndexes();
		if ( args.length >= 1 ) {
			File processPath = new File(args[0]);
			if ( processPath.exists() && processPath.isFile() && processPath.canRead() ) {
				boolean found = false;
				for ( ProcessedIndex index: indexes ) {
					if ( index.getPath().equals(processPath.getPath()) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					ProcessedIndex index = new ProcessedIndex();
					index.setPath(processPath.getPath());
					indexes.add(index);
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
	}

	private void processFile(ProcessedIndex index, ConfigJson configJson) throws IOException, InterruptedException {
		
		String parentPath = new File(index.getPath()).getParent();
		System.out.println(parentPath);
		Path toWatch = Paths.get(parentPath);
		
        if(toWatch == null) {
            throw new UnsupportedOperationException("Directory not found");
        }
        WatchService myWatcher = toWatch.getFileSystem().newWatchService();
        FileWatcher fileWatcher = new FileWatcher(myWatcher);
        fileWatcher.setProcessedIndex(index);
        fileWatcher.setPreferences(preferences);
        fileWatcher.setConfigJson(configJson);
        Thread th = new Thread(fileWatcher, "FileWatcher");
        th.start();
        toWatch.register(myWatcher, ENTRY_MODIFY);
        th.join();
	}

	public void initConsoleLog4J() {

		PatternLayout layout = new PatternLayout(
				"%d{dd MMM yyyy HH:mm:ss,SSS} [%t] %-5p %c %x - %m%n");
		try {
			String filename = PreferencesImpl.getPreferencesDir().getPath() + File.separator
					+ "indexer.log";
			DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender(
					layout, filename, "'.'yyyy-MM-dd-HH");
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

package com.esindexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.ArrayList;

import org.apache.commons.daemon.DaemonContext;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

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
		loadPreferences();
		initConsoleLog4J();
		LOG.info("Starting ES Indexer Daemon");
		if ( args == null ) {
			args = new String[]{};
		}
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
		
		boolean found = false;
		
		if (args.length >= 2) {
			for (String node : ap.getNodes()) {
				if (node.equalsIgnoreCase(args[1].trim())) {
					found = true;
				}
			}
			if ( !found ) {
				ap.getNodes().add(args[1].trim());
				try {
					this.preferences.save();
				} catch (IOException e) {
					LOG.error(e, e);
				}
			}
		}
		
		if ( ap.getNodes().size() <= 0 ) {
			LOG.fatal("No server nodes to connect to.");
			System.exit(0);
		}

		for (ProcessedIndex index : indexes) {
			try {
				processFile(index);
			} catch (IOException e) {
				LOG.error(e, e);
			} catch (InterruptedException e) {
				LOG.info(e, e);
			}
		}
		
	}

	private void processFile(ProcessedIndex index) throws IOException, InterruptedException {
		
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
        Thread th = new Thread(fileWatcher, "FileWatcher");
        th.start();
        toWatch.register(myWatcher, ENTRY_MODIFY);
        th.join();
	}

	public void initConsoleLog4J() {

		PatternLayout layout = new PatternLayout(
				"%d{dd MMM yyyy HH:mm:ss,SSS} [%t] %-5p %c %x - %m%n");
		try {
			String filename = this.preferences.getPreferencesDir().getPath() + File.separator
					+ "indexer.log";
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

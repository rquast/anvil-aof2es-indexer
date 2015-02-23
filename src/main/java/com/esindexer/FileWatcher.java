package com.esindexer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.esindexer.preferences.IPreferences;
import com.esindexer.xstream.model.ProcessedIndex;
import com.esindexer.xstream.model.ProcessedPage;

/**
 * @author Roland Quast (roland@formreturn.com)
 *
 */
class FileWatcher implements Runnable {
	
	private static Logger LOG = Logger.getLogger(FileWatcher.class);
	
    private WatchService myWatcher;
	private ProcessedIndex index;
	private IPreferences preferences;
    public FileWatcher(WatchService myWatcher) {
        this.myWatcher = myWatcher;
    }

    @Override
    public void run() {
        try {
            WatchKey key = myWatcher.take();
            while(key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                	final Path changed = (Path) event.context();
                	LOG.info("File updated: " + changed);
                	if ( !(index.getPath().endsWith(changed.toString())) ) {
                		continue;
                	}
                	try {
						processFile();
					} catch (FileNotFoundException e) {
						LOG.error(e, e);
					} catch (IOException e) {
						LOG.error(e, e);
					} catch (ParseException e) {
						LOG.error(e, e);
					} catch (java.text.ParseException e) {
						LOG.error(e, e);
					}
                }
                key.reset();
                key = myWatcher.take();
            }
        } catch (InterruptedException e) {
        	LOG.info(e, e);
        }
    }
    
	// TODO: run this as a file watcher thread.
	private void processFile() throws FileNotFoundException,
			IOException, ParseException, java.text.ParseException {

		TransportClient client = new TransportClient();
		for ( String node: preferences.getApplicationPreferences().getNodes() ) {
			client.addTransportAddress(new InetSocketTransportAddress(node, 9300));
		}
		
		JSONParser parser = new JSONParser();

		Object obj = parser.parse(new FileReader(index.getPath()));

		JSONArray pageList = (JSONArray) obj;

		for (Object pageObj : pageList.toArray()) {
			JSONObject pageJObj = (JSONObject) pageObj;
			String modifiedStr = (String) pageJObj.get("modified");
			String url = (String) pageJObj.get("url");
			String title = (String) pageJObj.get("title");
			String content = (String) pageJObj.get("content");
			String path = (String) pageJObj.get("path");
			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date newModified = format.parse(modifiedStr);
			
			ProcessedPage processedPage = null;
			if ( index.getProcessedPages().containsKey(url) ) {
				processedPage = index.getProcessedPages().get(url);
				Date lastModified = processedPage.getModified();
				if ( newModified.after(lastModified) ) {
					processedPage = new ProcessedPage();
					processedPage.setUrl(url);
					processedPage.setModified(newModified);
					processedPage.setTitle(title);
					processedPage.setContent(content);
					processedPage.setPath(path);
					if ( updateIndex(client, processedPage) ) {
						index.getProcessedPages().put(url, processedPage);
						preferences.save();
					}
				}
			} else {
				processedPage = new ProcessedPage();
				processedPage.setUrl(url);
				processedPage.setModified(newModified);
				processedPage.setTitle(title);
				processedPage.setContent(content);
				processedPage.setPath(path);
				if ( updateIndex(client, processedPage) ) {
					index.getProcessedPages().put(url, processedPage);
					preferences.save();
				}
			}
		}
		
		client.close();

	}

	private boolean updateIndex(Client client, ProcessedPage processedPage) {
		JSONObject obj = new JSONObject();
		obj.put("url", processedPage.getUrl());
		obj.put("title", processedPage.getTitle());
		obj.put("content", processedPage.getContent());
		obj.put("modified", processedPage.getModified());
		String json = obj.toJSONString();
		IndexResponse response = client.prepareIndex("helpindex", "page")
				.setSource(json)
				.execute()
				.actionGet();
		return true; // TODO: return false if elasticsearch didn't like doing and update.
	}

	public void setProcessedIndex(ProcessedIndex index) {
		this.index = index;
	}

	public void setPreferences(IPreferences preferences) {
		this.preferences = preferences;
	}
    
    
}

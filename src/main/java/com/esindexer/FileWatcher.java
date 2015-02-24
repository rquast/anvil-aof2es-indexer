package com.esindexer;

import java.io.BufferedReader;
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
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.esindexer.preferences.IPreferences;
import com.esindexer.xstream.model.ProcessedIndex;
import com.esindexer.xstream.model.ProcessedPage;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Roland Quast (roland@formreturn.com)
 *
 */
class FileWatcher implements Runnable {
	
	private static Logger LOG = Logger.getLogger(FileWatcher.class);
	
    private WatchService myWatcher;
	private ProcessedIndex index;
	private IPreferences preferences;

	private ConfigJson configJson;
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
                	LOG.debug("File updated: " + changed);
                	if ( !(index.getPath().endsWith(changed.toString())) ) {
                		continue;
                	}
                	try {
						processFile();
					} catch (Exception e) {
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

	private String readFileAsString(String filePath) throws Exception {
		BufferedReader reader = null;
		FileReader fileReader = null;
		StringBuffer fileData = new StringBuffer();
		try {
			fileReader = new FileReader(filePath);
			reader = new BufferedReader(fileReader);
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (fileReader != null){
				fileReader.close();
			}
		}
		return fileData.toString();
	}
    
	private void processFile() throws Exception {

		TransportClient client = null;

		try {

			client = new TransportClient();
			for (String node : this.configJson.getNodes()) {
				client.addTransportAddress(new InetSocketTransportAddress(node,
						9300));
			}

			JsonParser parser = new JsonParser();
			JsonObject obj = null;

			try {
				String jsonStr = readFileAsString(index.getPath());
				obj = parser.parse(jsonStr).getAsJsonObject();
			} catch (Exception ex) {
				LOG.error(ex, ex);
				throw (ex);
			}

			JsonArray jsonArr = obj.getAsJsonArray();
			Iterator<JsonElement> jai = jsonArr.iterator();
			
			while (jai.hasNext()) {

				JsonObject pageJObj = jai.next().getAsJsonObject();

				String modifiedStr = pageJObj.get("modified").getAsString();
				String url = pageJObj.get("url").getAsString().trim();
				String title = pageJObj.get("title").getAsString().trim();
				String content = pageJObj.get("content").getAsString().trim();
				String path = pageJObj.get("path").getAsString().trim();
				String categoriesStr = pageJObj.get("categories").getAsString().trim();
				String tag = pageJObj.get("tag").getAsString().trim();
				String type = pageJObj.get("type").getAsString().trim();

				DateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH);
				format.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date newModified = format.parse(modifiedStr);

				ProcessedPage processedPage = null;
				if (index.getProcessedPages().containsKey(url)) {
					processedPage = index.getProcessedPages().get(url);
					Date lastModified = processedPage.getModified();
					if (newModified.after(lastModified)) {
						processedPage = new ProcessedPage();
						processedPage.setUrl(url);
						processedPage.setModified(newModified);
						processedPage.setTitle(title);
						processedPage.setContent(content);
						processedPage.setPath(path);
						processedPage.setType(type);
						for (String category : categoriesStr.split(",")) {
							processedPage.getCategories().add(category.trim());
						}
						processedPage.getTags().add(tag);
						if (updateIndex(client, processedPage)) {
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
					processedPage.setType(type);
					for (String category : categoriesStr.split(",")) {
						processedPage.getCategories().add(category.trim());
					}
					processedPage.getTags().add(tag);
					if (updateIndex(client, processedPage)) {
						index.getProcessedPages().put(url, processedPage);
						preferences.save();
					}
				}
			}

		} catch (Exception ex) {
			LOG.error(ex, ex);
			throw (ex);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		
	}

	private boolean updateIndex(Client client, ProcessedPage processedPage) {
		JsonObject obj = new JsonObject();
		obj.add("url", (new JsonParser()).parse(processedPage.getUrl()).getAsJsonObject());
		obj.add("title", (new JsonParser()).parse(processedPage.getTitle()).getAsJsonObject());
		obj.add("content", (new JsonParser()).parse(processedPage.getContent()).getAsJsonObject());
		obj.add("modified", (new JsonParser()).parse("\"" + processedPage.getModified() + "\"").getAsJsonObject());
		String json = null;
		try {
			json = obj.getAsString();
		} catch ( Exception ex ) {
			LOG.error(ex, ex);
		}
		
		if ( json == null ) {
			return false;
		}
		
		LOG.debug(json);
		
		IndexResponse response = null;
		
		try {
			response = client.prepareIndex(configJson.getIndex(), processedPage.getType(), processedPage.getUrl())
				.setSource(json)
				.execute()
				.actionGet();
		} catch ( Exception ex ) {
			LOG.error(ex, ex);
			return false;
		}
		
		if ( response != null ) {
			if ( response.isCreated() ) {
				LOG.info("ElasticSearch response was \"created\".");
			} else {
				LOG.info("ElasticSearch response was \"updated\"");
			}
			return true;
		} else {
			LOG.error("No response object created.");
			return false;
		}
		
	}

	public void setProcessedIndex(ProcessedIndex index) {
		this.index = index;
	}

	public void setPreferences(IPreferences preferences) {
		this.preferences = preferences;
	}

	public void setConfigJson(ConfigJson configJson) {
		this.configJson = configJson;
	}
    
    
}

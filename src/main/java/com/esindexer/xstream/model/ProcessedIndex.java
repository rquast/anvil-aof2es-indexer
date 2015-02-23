package com.esindexer.xstream.model;

import java.util.HashMap;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Roland Quast (roland@formreturn.com)
 *
 */
@XStreamAlias("processedIndex")
public class ProcessedIndex {
	
	private String path;
	
	private HashMap<String, ProcessedPage> processedPages;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public HashMap<String, ProcessedPage> getProcessedPages() {
		if ( this.processedPages == null ) {
			this.processedPages = new HashMap<String, ProcessedPage>();
		}
		return processedPages;
	}

	public void setProcessedPages(HashMap<String, ProcessedPage> processedPages) {
		this.processedPages = processedPages;
	}
	
}

package com.esindexer.xstream.model;

import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Roland Quast (roland@formreturn.com)
 *
 */
@XStreamAlias("processedPage")
public class ProcessedPage {

	private String url;
	
	private String title;
	
	private String path;
	
	private String content;
	
	private Date modified;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
}

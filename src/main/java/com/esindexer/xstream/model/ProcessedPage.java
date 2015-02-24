package com.esindexer.xstream.model;

import java.util.ArrayList;
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
	
	private String type;
	
	private ArrayList<String> categories;
	
	private ArrayList<String> tags;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public ArrayList<String> getCategories() {
		if ( categories == null ) {
			categories = new ArrayList<String>();
		}
		return categories;
	}

	public void setCategories(ArrayList<String> categories) {
		this.categories = categories;
	}

	public ArrayList<String> getTags() {
		if ( tags == null ) {
			tags = new ArrayList<String>();
		}
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}
	
}

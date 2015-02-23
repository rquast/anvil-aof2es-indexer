package com.esindexer.xstream.model;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("applicationPreferences")
public class ApplicationPreferences {

	ArrayList<String> processPaths;
	
	public ArrayList<String> getProcessPaths() {
		if ( processPaths == null ) {
			processPaths = new ArrayList<String>();
		}
		return processPaths;
	}

	public void setProcessPaths(ArrayList<String> processPaths) {
		this.processPaths = processPaths;
	}

}

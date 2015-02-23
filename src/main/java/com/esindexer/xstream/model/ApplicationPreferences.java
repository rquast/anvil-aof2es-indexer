package com.esindexer.xstream.model;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("applicationPreferences")
public class ApplicationPreferences {

	ArrayList<ProcessedIndex> processedIndexes;

	ArrayList<String> nodes;
	
	public ArrayList<ProcessedIndex> getProcessedIndexes() {
		if ( processedIndexes == null ) {
			processedIndexes = new ArrayList<ProcessedIndex>();
		}
		return processedIndexes;
	}

	public void setProcessedIndexes(ArrayList<ProcessedIndex> processedIndexes) {
		this.processedIndexes = processedIndexes;
	}

	public ArrayList<String> getNodes() {
		if ( nodes == null ) {
			nodes = new ArrayList<String>();
		}
		return nodes;
	}

	public void setNodes(ArrayList<String> nodes) {
		this.nodes = nodes;
	}

}

package com.esindexer.xstream.model;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("applicationPreferences")
public class ApplicationPreferences {

	ArrayList<ProcessedIndex> processedIndexes;
	
	public ArrayList<ProcessedIndex> getProcessedIndexes() {
		if ( processedIndexes == null ) {
			processedIndexes = new ArrayList<ProcessedIndex>();
		}
		return processedIndexes;
	}

	public void setProcessedIndexes(ArrayList<ProcessedIndex> processedIndexes) {
		this.processedIndexes = processedIndexes;
	}

}

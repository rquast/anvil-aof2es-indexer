package com.aof2es.elastic.model;

import java.util.ArrayList;
import java.util.List;

public class Bool {
    
    private List<Must> must = new ArrayList<Must>();
    
    private List<Filter> filter = new ArrayList<Filter>();
    
    public Bool() {
	
	this.must.add(new Must());
	this.must.add(new Must());
	
	this.filter.add(new Filter());
	this.filter.add(new Filter());
	
    }

}

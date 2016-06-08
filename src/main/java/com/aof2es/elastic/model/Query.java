package com.aof2es.elastic.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("query")
public class Query {

    private Bool bool = new Bool();
    
}

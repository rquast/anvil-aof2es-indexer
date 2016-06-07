package com.aof2es;

import java.util.ArrayList;

/**
 * @author Roland Quast (rquast@rolandquast.com)
 *
 */
public class ConfigJson {

    private String generator;
    private String index;
    private ArrayList<String> nodes;

    public void setGenerator(String generator) {
	this.generator = generator;
    }

    public String getGenerator() {
	return generator;
    }

    public void setIndex(String index) {
	this.index = index;
    }

    public String getIndex() {
	return index;
    }

    public void setNodes(ArrayList<String> nodes) {
	this.nodes = nodes;
    }

    public ArrayList<String> getNodes() {
	return nodes;
    }

}

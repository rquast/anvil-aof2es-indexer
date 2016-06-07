package com.esindexer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AnvilRedisAOFReader {

    static final Log LOG = LogFactory.getLog(AnvilRedisAOFReader.class);
    
    public static enum Command { SELECT, SET, ZADD, HSET, PEXPIREAT, DEL }

    private long pos = 0L;

    private RandomAccessFile reader;
    
    private ArrayList<String[]> transactionBuffer = new ArrayList<String[]>();

    public AnvilRedisAOFReader(RandomAccessFile reader) {
	this.reader = reader;
    }

    public RandomAccessFile getReader() {
        return reader;
    }

    public long getPos() {
	return pos;
    }

    private static byte[] toByteArray(List<Integer> in) {
	final int n = in.size();
	byte ret[] = new byte[n];
	for (int i = 0; i < n; i++) {
	    ret[i] = in.get(i).byteValue();
	}
	return ret;
    }

    private byte[] readBytes() throws IOException {

	int lastByte = -1, nextByte = -1;
	List<Integer> buffer = new ArrayList<Integer>();

	while ((nextByte = reader.read()) != -1) {

	    // /r/n line ending standard for redis AOF
	    if (lastByte == 0x0d && nextByte == 0x0a) {
		buffer.remove(buffer.size() - 1);
		if (buffer.get(0).intValue() == 0x24) {
		    buffer.remove(0);
		    return readBytes();
		} else {
		    return toByteArray(buffer);
		}
	    }

	    buffer.add(nextByte);
	    lastByte = nextByte;
	}

	throw new IOException("EOF reached.");

    }

    public String[] next() throws IOException {

	String line;

	try {
	    line = readString();
	} catch (IOException ioex) {
	    return null;
	}
	if (line == null || !line.startsWith("*")) {
	    return null;
	}

	int argc = Integer.parseInt(line.replaceFirst("\\*", ""));
	String[] args = new String[argc];

	for (int i = 0; i < argc; i++) {
	    try {
		args[i] = readString();
	    } catch (IOException ioex) {
		return null;
	    }
	}

	return args;
    }

    private String readString() throws IOException {
	return new String(readBytes(), "ISO8859-1");
    }

    public static void printArgs(String[] args) {

	StringBuffer sb = new StringBuffer();
	sb.append("Command: " + args[0]);
	for (int i = 1; i < args.length; i++) {
	    sb.append(" arg" + i + ": " + args[i]);
	}
	System.out.println(sb.toString());

    }
    
    private void processCommand(String[] args) {
	
	String cmdStr = args[0].toUpperCase();
	
	if (cmdStr.equals("MULTI") || this.transactionBuffer.size() > 0) {
	    this.transactionBuffer.add(args);
	} else if (cmdStr.equals("EXEC")) {
	    for (String[] argsItem: this.transactionBuffer) {
		processCommand(argsItem);
	    }
	    this.transactionBuffer.clear();
	} else if (cmdStr.equals("DISCARD")) {
	    this.transactionBuffer.clear();
	} else {
	    
	    Command cmd = AnvilRedisAOFReader.Command.valueOf(cmdStr);
	    
	    switch (cmd) {
	    
	    case SELECT:
		break;
		
	    case SET:
		// TODO
		break;
		
	    case ZADD:
		// TODO
		break;
		
	    case HSET:
		// TODO
		break;
		
	    case PEXPIREAT:
		// TODO
		break;
	    
	    case DEL:
		// TODO
		break;
	    
	    }
	    
	}
	
    }

    public static void read(String filePath) {

	try {
	    File file = new File(filePath);

	    RandomAccessFile raf = new RandomAccessFile(file, "r");
	    AnvilRedisAOFReader r = new AnvilRedisAOFReader(raf);
	    
	    if (file.exists() && file.canRead()) {
		while (true) {

		    raf.seek(r.getPos());
		    String[] args;

		    while ((args = r.next()) != null) {
			printArgs(args);
			r.processCommand(args);
			r.setPos(raf.getFilePointer());
		    }
		    Thread.sleep(100);
		}
	    }
	    
	    raf.close();
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	}

    }

    private void setPos(long pos) {
	this.pos = pos;
    }

}

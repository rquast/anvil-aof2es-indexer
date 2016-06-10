package com.aof2es;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.aof2es.preferences.IPreferences;
import com.aof2es.xstream.model.ApplicationPreferences;

public class Reader {

    static final Logger LOG = Logger.getLogger(Reader.class);
    
    public static enum Command { MULTI, EXEC, DISCARD, SELECT, SET, ZADD, ZREM, HSET, HDEL, PEXPIREAT, DEL }

    private RandomAccessFile reader;
    
    private ArrayList<String[]> transactionBuffer = new ArrayList<String[]>();

    private ICommandProcessor commandProcessor;

    public Reader(RandomAccessFile reader, ICommandProcessor commandProcessor) {
	this.reader = reader;
	this.commandProcessor = commandProcessor;
    }

    public RandomAccessFile getReader() {
        return reader;
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
		
		// $ (length of next line)
		if (buffer.get(0).intValue() == 0x24) {
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
    
    private void processCommand(String[] args) throws IOException {
	
	String cmdStr = args[0].toUpperCase();

	Command cmd;
	
	try {
	    cmd = Reader.Command.valueOf(cmdStr);
	} catch (IllegalArgumentException e) {
	    LOG.debug("Command not found: " + cmdStr);
	    return;
	}
	
	switch (cmd) {
	
	// Do not process transactional commands.
	case SELECT:
	case MULTI:
	case EXEC:	    
	case DISCARD:
	    return;

	case SET:
	    commandProcessor.processSetCommand(args);
	    break;

	case ZADD:
	    commandProcessor.processZsetCommand(args);
	    break;
	    
	case ZREM:
	    commandProcessor.processZremCommand(args);
	    break;

	case HSET:
	    commandProcessor.processHsetCommand(args);
	    break;
	    
	case HDEL:
	    commandProcessor.processHdelCommand(args);
	    break;

	case PEXPIREAT:
	    commandProcessor.processPexpireatCommand(args);
	    break;

	case DEL:
	    commandProcessor.processDelCommand(args);
	    break;

	}
	
    }

    private void process(String[] args) throws IOException {
	
	String cmdStr = args[0].toUpperCase();
	
	if (cmdStr.equals("EXEC")) {
	    for (String[] argsItem: this.transactionBuffer) {
		processCommand(argsItem);
	    }
	    this.transactionBuffer.clear();
	} else if (cmdStr.equals("DISCARD")) {
	    this.transactionBuffer.clear();
	} else if (cmdStr.equals("MULTI") || this.transactionBuffer.size() > 0) {
	    this.transactionBuffer.add(args);
	} else {
	    processCommand(args);
	}
	
    }

    public static void read(IPreferences preferences, ICommandProcessor commandProcessor) {
	
	ApplicationPreferences applicationPreferences = preferences.getApplicationPreferences();
	
	RandomAccessFile raf = null;

	try {
	    
	    File file = new File(applicationPreferences.getAofFilePath());
	    raf = new RandomAccessFile(file, "r");
	    Reader r = new Reader(raf, commandProcessor);
	    
	    if (file.exists() && file.canRead()) {
		while (true) {
		    
		    raf.seek(applicationPreferences.getPos());
		    String[] args;

		    try {
			
			while ((args = r.next()) != null) {
			    r.process(args);
			}

		    } catch (Exception ex) {
			
			throw ex;

		    } finally {

			if (applicationPreferences.getPos() != raf.getFilePointer()) {
			    applicationPreferences.setPos(raf.getFilePointer());
			    preferences.save();
			} else {
			    Thread.sleep(100);
			}

		    }
		    
		}
	    }
	    
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	} finally {
	    if (raf != null) {
		try {
		    raf.close();
		} catch (IOException e) {
		    LOG.error(e);
		}
	    }
	}

    }

}

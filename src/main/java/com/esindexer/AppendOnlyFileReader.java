/* File:    AppendOnlyFileReader.java
 * Created: Jul 17, 2010
 * Author:  Lars George
 *
 * Original code Copyright (c) 2010 larsgeorge.com
 * Modified code Copyright (c) 2016 Roland Quast
 */

package com.esindexer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implements a Redis Append-Only-File (AOF) reader generated using the <code>BGREWRITEAOF</code> command sent to
 * Redis (see <a href="http://code.google.com/p/redis/wiki/BgrewriteaofCommand">command</a> and
 * <a href="http://code.google.com/p/redis/wiki/AppendOnlyFileHowto">reference</a> help online).
 *
 * @author Lars George
 */
public class AppendOnlyFileReader {

  static final Log LOG = LogFactory.getLog(AppendOnlyFileReader.class);

  private DataInputStream in = null;
  private long pos = 0L;

  public AppendOnlyFileReader(InputStream in) {
    if (!(in instanceof BufferedInputStream))
      in = new BufferedInputStream(in);
    this.in = new DataInputStream(in);
  }

    public DataInputStream getIn() {
	return in;
    }

    public void setIn(DataInputStream in) {
	this.in = in;
    }

    public long getPos() {
	return pos;
    }

    public void setPos(long pos) {
	this.pos = pos;
    }
    
    private static byte[] toByteArray(List<Integer> in) {
	final int n = in.size();
	byte ret[] = new byte[n];
	for (int i = 0; i < n; i++) {
	    ret[i] = in.get(i).byteValue();
	}
	return ret;
    }
    
    private static byte[] readBytes(DataInputStream in) throws IOException {

	int lastByte = -1, nextByte = in.read();
	List<Integer> buffer = new ArrayList<Integer>();
	
	// stop at $ length
	if (nextByte == 0x24) {
	    
	    do {

		// /r/n line ending standard for redis AOF
		if (lastByte == 0x0d && nextByte == 0x0a) {
		    if (buffer.size() > 1) {
			buffer.remove(buffer.size() - 1);
			buffer.remove(0);
		    }
		    /*
		    long length = Long.parseLong(new String(toByteArray(buffer), "ISO8859-1"));
		    
		    byte[] value = new byte[(int) length];
		    for (int i = 0; i < length; i++) {
			value[i] = (byte) in.read();
		    }
		    */
		    
		    return readBytes(in);
		    
		}

		buffer.add(nextByte);
		lastByte = nextByte;
	    } while ((nextByte = in.read()) != -1);
	    
	} else {
	
	    do {

		// /r/n line ending standard for redis AOF
		if (lastByte == 0x0d && nextByte == 0x0a) {
		    if (buffer.size() > 0) {
			buffer.remove(buffer.size() - 1);
			
		    }
		    return toByteArray(buffer);
		}

		buffer.add(nextByte);
		lastByte = nextByte;
	    } while ((nextByte = in.read()) != -1);
	
	}
	
	throw new IOException("EOF reached.");
	
    }

  public String[] next() throws IOException {

    String line = readString(in);
    if (line == null || !line.startsWith("*")) {
	return null;
    }
	
    int argc = Integer.parseInt(line.replaceFirst("\\*", ""));
    String[] args = new String[argc];
    
    for (int i = 0; i < argc; i++) {
	args[i] = readString(in);
    }
    
    return args;
  }

  private String readString(DataInputStream in) throws IOException {
      return new String(readBytes(in), "ISO8859-1");
  }
  
  public static void printArgs(String[] args) {
      
      StringBuffer sb = new StringBuffer();
      sb.append("Command: " + args[0]);
      for (int i = 1; i < args.length; i++) {
	  sb.append(" arg" + i + ": " + args[i]);
      }
      System.out.println(sb.toString());
      
  }

  public static void read(String filePath) {
      
    try {
	File file = new File(filePath);
	AppendOnlyFileReader r = new AppendOnlyFileReader(new FileInputStream(file));
	if (file.exists() && file.canRead()) {
	    while (true) {
		r.getIn().skip(r.getPos());
		String[] args;
		while ((args = r.next()) != null) {
		    printArgs(args);
		    r.setPos(file.length());
		}
		Thread.sleep(1000);
	    }
	}
	r.getIn().close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }

}

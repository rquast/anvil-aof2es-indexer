package com.aof2es;

import java.io.BufferedReader;
import java.io.FileReader;

public final class Util {

  private Util() {
  }

  public static String readFileAsString(String filePath) throws Exception {
    BufferedReader reader = null;
    FileReader fileReader = null;
    StringBuffer fileData = new StringBuffer();
    try {
      fileReader = new FileReader(filePath);
      reader = new BufferedReader(fileReader);
      char[] buf = new char[1024];
      int numRead = 0;
      while ((numRead = reader.read(buf)) != -1) {
        String readData = String.valueOf(buf, 0, numRead);
        fileData.append(readData);
      }
    } catch (Exception ex) {
      throw ex;
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (fileReader != null) {
        fileReader.close();
      }
    }
    return fileData.toString();
  }
  
}

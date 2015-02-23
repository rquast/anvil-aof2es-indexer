package com.esindexer.preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import com.esindexer.xstream.XStreamUtility;
import com.esindexer.xstream.model.ApplicationPreferences;
import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;

public class PreferencesImpl implements IPreferences {

  private XStream xstream;

  private ApplicationPreferences applicationPreferences = new ApplicationPreferences();

  private File preferencesDir;

  private static final File tmpDir = Files.createTempDir();

  private static final String preferencesPath = System.getProperty("user.home") + File.separator
      + ".esindexer";

  private static final String preferencesFileName = "preferences.xml";

  public final void cleanUp() {
    tmpDir.delete();
  }

  public final ApplicationPreferences getApplicationPreferences() {
    return applicationPreferences;
  }

  private File getPreferencesFile() throws IOException {
    preferencesDir = new File(preferencesPath);
    if (!(preferencesDir.exists())) {
      if (!(preferencesDir.mkdirs()) || !(preferencesDir.canWrite())) {
        throw new IOException("Could not create preferences directories.");
      }
    }
    return new File(preferencesDir.getAbsolutePath() + File.separator + preferencesFileName);
  }
  
	public File getPreferencesDir() {
		return preferencesDir;
	}
	
	public final File getTempDirectory() {
		return tmpDir;
	}

  public final synchronized void load() throws IOException, ClassNotFoundException {

    tmpDir.deleteOnExit();
    xstream = XStreamUtility.getXStream();

    File preferencesFile = getPreferencesFile();
    if (!(preferencesFile.exists())) {
      save();
    }

    ObjectInputStream s = null;
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(preferencesFile);
      s = xstream.createObjectInputStream(new InputStreamReader(fis, "UTF-8"));
      applicationPreferences = ((ApplicationPreferences) s.readObject());
    } catch (IOException ex) {
      throw ex;
    } finally {
      if (s != null) {
        s.close();
      }
      if (fis != null) {
        fis.close();
      }
    }

  }

  public final synchronized void save() throws IOException {
    synchronized (PreferencesImpl.class) {
      File preferencesFile = getPreferencesFile();
      String rootNodeName = "esIndexer";
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
          preferencesFile), "UTF-8"));
      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      ObjectOutputStream oos;
      oos = xstream.createObjectOutputStream(out, rootNodeName);
      oos.writeObject(applicationPreferences);
      oos.close();
    }
  }

}

package com.aof2es.preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import com.aof2es.xstream.XStreamUtility;
import com.aof2es.xstream.model.ApplicationPreferences;
import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;

public class PreferencesImpl implements IPreferences {

    private XStream serialize;
    
    private XStream deserialize;

    private ApplicationPreferences applicationPreferences = new ApplicationPreferences();

    private static final File tmpDir = Files.createTempDir();

    private static final String preferencesPath = System.getProperty("user.home") + File.separator + ".aof2es";

    private static final String preferencesFileName = "preferences.json";

    @Override
    public final void cleanUp() {
	tmpDir.delete();
    }

    @Override
    public final ApplicationPreferences getApplicationPreferences() {
	return applicationPreferences;
    }

    private static File getPreferencesFile() throws IOException {
	File preferencesDir = new File(preferencesPath);
	if (!(preferencesDir.exists())) {
	    if (!(preferencesDir.mkdirs()) || !(preferencesDir.canWrite())) {
		throw new IOException("Could not create preferences directories.");
	    }
	}
	return new File(getPreferencesDir().getPath() + File.separator + preferencesFileName);
    }

    public static File getPreferencesDir() throws IOException {
	File preferencesDir = new File(preferencesPath);
	if (!(preferencesDir.exists())) {
	    if (!(preferencesDir.mkdirs()) || !(preferencesDir.canWrite())) {
		throw new IOException("Could not create preferences directories.");
	    }
	}
	return preferencesDir;
    }

    @Override
    public final File getTempDirectory() {
	return tmpDir;
    }

    @Override
    public final synchronized void load() throws IOException, ClassNotFoundException {

	tmpDir.deleteOnExit();
	serialize = XStreamUtility.getSerialize();

	File preferencesFile = getPreferencesFile();
	if (!(preferencesFile.exists())) {
	    save();
	}

	ObjectInputStream s = null;
	FileInputStream fis = null;
	try {
	    fis = new FileInputStream(preferencesFile);
	    s = serialize.createObjectInputStream(new InputStreamReader(fis, "UTF-8"));
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

    @Override
    public final synchronized void save() throws IOException {
	synchronized (PreferencesImpl.class) {
	    
	    if (this.deserialize == null) {
		deserialize = XStreamUtility.getDeserialize();
	    }
	    
	    File preferencesFile = getPreferencesFile();
	    String rootNodeName = "aof2es";
	    BufferedWriter out = new BufferedWriter(
		    new OutputStreamWriter(new FileOutputStream(preferencesFile), "UTF-8"));
	    // out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    ObjectOutputStream oos;
	    oos = deserialize.createObjectOutputStream(out, rootNodeName);
	    oos.writeObject(applicationPreferences);
	    oos.close();
	}
    }

}

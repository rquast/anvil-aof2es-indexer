package com.aof2es.preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
	deserialize = XStreamUtility.getDeserialize();

	File preferencesFile = getPreferencesFile();
	if (!(preferencesFile.exists())) {
	    save();
	}

	deserialize.alias("applicationPreferences", ApplicationPreferences.class);
	applicationPreferences = (ApplicationPreferences)deserialize.fromXML(preferencesFile);

    }

    @Override
    public final synchronized void save() throws IOException {
	synchronized (PreferencesImpl.class) {
	    if (this.serialize == null) {
		serialize = XStreamUtility.getSerialize();
	    }	    
	    File preferencesFile = getPreferencesFile();
	    FileOutputStream fos = new FileOutputStream(preferencesFile);
	    serialize.toXML(applicationPreferences, fos);
	    fos.close();
	}
    }

}

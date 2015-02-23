package com.esindexer.preferences;

import java.io.File;

import com.esindexer.xstream.model.ApplicationPreferences;

public interface IPreferences extends BasePreferences {

  void cleanUp();

  ApplicationPreferences getApplicationPreferences();

  File getTempDirectory();
  
  File getPreferencesDir();

}

package com.aof2es.preferences;

import java.io.File;

import com.aof2es.xstream.model.ApplicationPreferences;

public interface IPreferences extends BasePreferences {

  void cleanUp();

  ApplicationPreferences getApplicationPreferences();

  File getTempDirectory();

}

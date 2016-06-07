package com.aof2es.xstream;

import com.aof2es.xstream.model.ApplicationPreferences;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public final class XStreamUtility {

  public static XStream getXStream() {
    final XStream xstream = loadXStream();
    processAnnotations(xstream);
    return xstream;
  }

  private static XStream loadXStream() {
    XStream xstream = new XStream(new DomDriver("UTF-8")) {
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {
          @Override
          public boolean shouldSerializeMember(@SuppressWarnings("rawtypes") Class definedIn,
              String fieldName) {
            if (definedIn == Object.class) {
              return false;
            }
            return super.shouldSerializeMember(definedIn, fieldName);
          }
        };
      }
    };
    xstream.setMode(XStream.NO_REFERENCES);
    return xstream;
  }

  private static void processAnnotations(XStream xstream) {
    xstream.processAnnotations(ApplicationPreferences.class);
  }

  private XStreamUtility() {
  }

}

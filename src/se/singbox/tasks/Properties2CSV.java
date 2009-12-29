/*
 * Copyright (c) 2009 Singbox AB.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY SINGBOX AB "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SINGBOX AB OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Singbox AB.
 */

package se.singbox.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import se.singbox.filetypes.CSVFile;
import se.singbox.filetypes.LocaleStringDictionary;
import se.singbox.filetypes.LocaleStringSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This task generates a comma-separated values (CSV) file from an existing
 * directory of locale properties file.  It is useful for migrating an existing
 * application's localiation resources to CSV format.  It accepts the following
 * attribute arguments:
 * <ul>
 * <li>inputPropertiesFile: A single file containing localization keys</li>
 * <li>inputPropertiesDir: A directory containing localization properties files</li>
 * <li>outputCSVFile: The output file to generate</li>
 * <li>propertyFileName: The filename to expect when reading a directory of
 * properties files.</li>
 * </ul>
 */
@SuppressWarnings({"UnusedDeclaration"})
// Is a top-level class
public class Properties2CSV extends Task {
  private String inputPropertiesFile = null;
  private String inputPropertiesDir = null;
  private String outputCSVFile = null;
  private String propertyFileName = "myResources.properties";
  // XXX: Is this really needed?
  private String locale = null;

  private final String LOCALE_DIR_MATCH_PATTERN = "[a-z][a-z]_[A-Z][A-Z]";

  public void execute() {
    if(outputCSVFile == null) {
      throw new BuildException("Attribute 'outputFile' must be specified");
    }

    // Stores file readers for each locale properties file
    Map<String, InputStreamReader> localeFileReaders = new HashMap<String, InputStreamReader>();
    // Used to store the names of the different parsed locales
    LocaleStringSet localeNames = new LocaleStringSet();

    try {
      if(inputPropertiesFile != null) {
        // If the locale wasn't manually set, we try to determine it from the path
        // of the properties file.  Generally, locale files should be named:
        // ${source.dir}/locale/xx_YY/filename.properties
        // Where "xx_YY" is the given locale
        if(locale == null) {
          File parentDir = new File(inputPropertiesFile).getParentFile();
          String possibleLocaleName = parentDir.getName();
          if(possibleLocaleName.matches(LOCALE_DIR_MATCH_PATTERN)) {
            locale = possibleLocaleName;
          }
          else {
            throw new BuildException("Locale could not be determined from properties path, and was not given as an attribute");
          }
        }

        // Push into the locale map
        InputStreamReader inStream = new InputStreamReader(new FileInputStream(inputPropertiesFile), "UTF8");
        localeFileReaders.put(locale, inStream);
      }
      else if(inputPropertiesDir != null) {
        // Build a list of subdirectories for this dir
        File inputDirChildren[] = new File(inputPropertiesDir).listFiles();
        for(File child : inputDirChildren) {
          if(child.isDirectory()) {
            if(child.getName().matches(LOCALE_DIR_MATCH_PATTERN)) {
              File childPropertiesFile = new File(child.getPath() + System.getProperty("file.separator") + propertyFileName);
              if(childPropertiesFile.exists()) {
                System.out.println("Found properties file for locale: '" + child.getName() + "'");
                InputStreamReader inStream = new InputStreamReader(new FileInputStream(childPropertiesFile), "UTF8");
                localeFileReaders.put(child.getName(), inStream);
              }
              else {
                throw new BuildException("Could not find '" + propertyFileName + "' in directory '" + child.getPath() + "'");
              }
            }
          }
        }
      }
      else {
        throw new BuildException("Either 'propertiesFile' or 'propertiesDir' must be specified");
      }
    }
    catch(Exception e) {
      throw new BuildException(e);
    }

    try {
      LocaleStringDictionary inKeys = new LocaleStringDictionary();

      for(String localeName : localeFileReaders.keySet()) {
        // Make a map of locale keys based on the properties file
        Properties props = new Properties();
        props.load(localeFileReaders.get(localeName));
        localeNames.put(localeName, null);

        for(Object key : props.keySet()) {
          String value = (String)props.get(key);
          if(!isComment((String)key) && !isComment(value) && !value.isEmpty()) {
            // Add to existing locale key map, otherwise create a new map if it
            // doesn't exist yet
            LocaleStringSet stringSet = inKeys.get(key.toString());
            if(stringSet == null) {
              stringSet = new LocaleStringSet();
            }
            // System.out.println("Adding key '" + key.toString() + "', locale: '" + localeName + "', value: '" + value + "'");
            stringSet.put(localeName, value);
            inKeys.put(key.toString(), stringSet);
          }
        }
      }

      inKeys.put("LOCALE KEY", localeNames);
      CSVFile outFile = new CSVFile();
      outFile.write(outputCSVFile, inKeys);
    }
    catch(Exception e) {
      throw new BuildException(e);
    }
  }

  /**
   * Check to see if the string is a properties-file comment, ie. begins with "--"
   * @param inString String to check
   * @return True if string is a property comment, false otherwise
   */
  private boolean isComment(String inString) {
    boolean result = false;

    if(inString.length() >= 2) {
      if(inString.charAt(0) == '-' && inString.charAt(1) == '-') {
        result = true;
      }
    }

    return result;
  }

  public void setPropertiesDir(String dirname) {
    inputPropertiesDir = dirname;
  }

  public void setPropertiesFile(String filename) {
    inputPropertiesFile = filename;
  }

  public void setPropertyFileName(String filename) {
    propertyFileName = filename;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setOutputFile(String filename) {
    outputCSVFile = filename;
  }
}

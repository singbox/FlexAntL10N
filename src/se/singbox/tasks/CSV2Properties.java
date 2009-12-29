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
import se.singbox.filetypes.LocaleSourceFile;
import se.singbox.filetypes.LocaleStringDictionary;
import se.singbox.filetypes.LocaleStringSet;

import java.io.*;
import java.util.HashMap;

/**
 * This task generates the locale properties files given a master comma-separated values
 * formatted (CSV) file.  It accepts the following attribute arguments:
 * <ul>
 * <li>inputCSVFile: The CSV file containing all locale keys</li>
 * <li>outputDir: Directory to write output properties files</li>
 * <li>propertyFileName: Filename to use for the generated properties files</li>
 * </ul>
 */
@SuppressWarnings({"UnusedDeclaration"})
// Is a top-level class
public class CSV2Properties extends Task {
  private String inputCSVFile = null;
  private String outputDir = null;
  private String propertyFileName = "myResources.properties";

  public void execute() {
    if(inputCSVFile == null) {
      throw new BuildException("Attribute 'csvFile' must be specified");
    }
    else if(outputDir == null) {
      throw new BuildException("Attribute 'outputDir' must be specified");
    }

    try {
      LocaleSourceFile inFile = new CSVFile();
      LocaleStringDictionary inStrings = inFile.read(inputCSVFile);
      if(inStrings == null) {
        throw new BuildException("Error while parsing CSV file");
      }

      // Build a map of locale names and output file streams
      LocaleStringSet localeNames = inStrings.get("LOCALE KEY");
      if(localeNames == null) {
        throw new IOException("Could not find 'LOCALE KEY' string in input map");
      }
      HashMap<String, BufferedWriter> outStreams = new HashMap<String, BufferedWriter>();

      // Now create directories and populate the outStreams map
      for(Object locale : localeNames.keySet()) {
        File localeDir = new File(outputDir + System.getProperty("file.separator") + locale.toString());
        if(!localeDir.mkdir()) {
          throw new BuildException("Could not create output subdirectory '" + localeDir.toString() + "'");
        }
        String localeFileName = localeDir.toString() + System.getProperty("file.separator") + propertyFileName;
        outStreams.put(locale.toString(), new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(localeFileName), "UTF8")));
      }

      // This key is no longer needed
      inStrings.remove("LOCALE KEY");

      // Now go through each key and add it to the respective locale
      for(String key : inStrings.keySet()) {
        LocaleStringSet keyStrings = inStrings.get(key);
        for(String locale : keyStrings.keySet()) {
          BufferedWriter outFile = outStreams.get(locale);
          if(outFile == null) {
            throw new BuildException("Internal error -- could not find stream for key");
          }
          outFile.write(key + "=" + CSVFile.removeStringPadding(keyStrings.get(locale)));
          outFile.write(System.getProperty("line.separator"));
        }
      }
    }
    catch(Exception e) {
      throw new BuildException(e);
    }
  }

  public void setCSVFile(String filename) {
    inputCSVFile = filename;
  }

  public void setOutputDir(String dirname) {
    outputDir = dirname;
  }

  public void setPropertyFileName(String filename) {
    propertyFileName = filename;
  }
}

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
import se.singbox.filetypes.LocaleStringDictionary;
import se.singbox.filetypes.LocaleStringSet;
import se.singbox.filetypes.XLSFile;

import java.io.*;
import java.util.HashMap;

/**
 * This task generates locale properties files given a Microsoft Excel spreadsheet
 * containing locale keys.  The task will only read the first sheet of the spreadsheet,
 * which should be formatted like so:
 *
 * <table border="1">
 * <tr><td><b>LOCALE KEY</b></td><td><b>xx_YY</b></td><td><b>yy_ZZ</b></td></tr>
 * <tr><td>KEY_NAME1</td><td>String for language 1</td><td>String for language 2</td></tr>
 * <tr><td>KEY_NAME2</td><td>Other string for language 1</td><td>Other string for language 2</td></tr>
 * </table>
 *
 * Note that the string "LOCALE KEY" must literally appear in the first cell.  In the
 * above example, xx_YY and yy_ZZ represent the locale codes, like en_US or sv_SV.  This
 * task accepts the following attribute arguments:
 * <ul>
 * <li>inputXLSFile: Excel spreadsheet to read locale strings from</li>
 * <li>outputDir: Directory to generate output properties files in</li>
 * <li>propertyFileName: Name to use for generated properties files</li>
 * </ul>
 */
@SuppressWarnings({"UnusedDeclaration"})
// Is a top-level class
public class XLS2Properties extends Task {
  private String inputXLSFile = null;
  private String outputDir = null;
  private String propertyFileName = "myResources.properties";

  // TODO: This code could be easily refactored -- duh

  public void execute() {
    if(inputXLSFile == null) {
      throw new BuildException("Attribute 'xlsFile' must be specified");
    }
    else if(outputDir == null) {
      throw new BuildException("Attribute 'outputDir' must be specified");
    }

    try {
      XLSFile inFile = new XLSFile();
      LocaleStringDictionary inStrings = inFile.read(inputXLSFile);
      if(inStrings == null) {
        throw new BuildException("Error while parsing XLS file");
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
        if(!localeDir.exists()) {
          if(!localeDir.mkdir()) {
            throw new BuildException("Could not create output subdirectory '" + localeDir.toString() + "'");
          }
        }
        String localeFileName = localeDir.toString() + System.getProperty("file.separator") + propertyFileName;

        // This is probably unnecessary, but it never hurts
        File localeFileNameExists = new File(localeFileName);
        if(localeFileNameExists.exists()) {
          if(!localeFileNameExists.delete()) {
            throw new BuildException("Could not remove '" + localeFileName + "'");
          }
        }

        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(localeFileName), "UTF8"));
        System.out.println("Created " + localeFileName);
        outStreams.put(locale.toString(), outWriter);
      }

      // This key is no longer needed
      inStrings.remove("LOCALE KEY");

      // Now go through each key and add it to the respective locale
      for(String key : inStrings.keySet()) {
        LocaleStringSet keyStrings = inStrings.get(key);
        for(String locale : keyStrings.keySet()) {
          // System.out.println(key + ": " + locale + ": " + keyStrings.get(locale));
          BufferedWriter outWriter = outStreams.get(locale);
          if(outWriter == null) {
            throw new BuildException("Internal error -- could not find stream for key");
          }
          outWriter.write(key + "=" + keyStrings.get(locale));
          outWriter.newLine();
          outWriter.flush();
        }
      }
    }
    catch(Exception e) {
      throw new BuildException(e);
    }
  }

  public void setXLSFile(String filename) {
    inputXLSFile = filename;
  }

  public void setOutputDir(String dirname) {
    outputDir = dirname;
  }

  public void setPropertyFileName(String filename) {
    propertyFileName = filename;
  }
}

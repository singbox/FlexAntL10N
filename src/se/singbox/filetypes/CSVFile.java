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

package se.singbox.filetypes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVFile implements LocaleSourceFile {
  public LocaleStringDictionary read(String filename) throws IOException {
    LocaleStringDictionary outStrings = new LocaleStringDictionary();
    BufferedReader fileReader = new BufferedReader(
      new InputStreamReader(new FileInputStream(filename), "UTF8"));

    String fileLine = fileReader.readLine();
    List<String> parsedLocales = parseCSVFields(fileLine);
    LocaleStringSet localeMap = new LocaleStringSet();
    if(!parsedLocales.get(0).equals("LOCALE KEY")) {
      throw new IOException("Malformed CSV file; needs 'LOCALE KEY' header as first row");
    }
    for(int i = 1; i < parsedLocales.size(); i++) {
      localeMap.put(parsedLocales.get(i), null);
    }
    outStrings.put("LOCALE KEY", localeMap);

    while((fileLine = fileReader.readLine()) != null) {
      List<String> parsedFields = parseCSVFields(fileLine);
      /*
      for(String f : parsedFields) {
        System.out.print("FOUND '" + f + "'");
      }
      System.out.println("  ");
      */
      LocaleStringSet stringMap = new LocaleStringSet();
      for(int i = 1; i < parsedFields.size(); i++) {
        //System.out.println("KEY: '" + parsedFields.get(0) + "', VALUE: '" + parsedFields.get(i) + "'");
        stringMap.put(parsedLocales.get(i), parsedFields.get(i));
      }
      outStrings.put(parsedFields.get(0), stringMap);
    }

    return outStrings;
  }

  /**
   * Breaks apart a CSV line into individual strings, stripping quotation marks from fields
   * @param line String to parse
   * @return A list of fields
   */
  private List<String> parseCSVFields(String line) {
    final int STATE_NOT_IN_STRING = 0;
    final int STATE_IN_STRING = 1;
    final int STATE_IN_QUOTED_STRING = 2;

    ArrayList<String> outFields = new ArrayList<String>();
    int currentPosition = 0;
    int startPosition = 0;
    int currentState = STATE_NOT_IN_STRING;
    while(currentPosition != line.length()) {
      //System.out.println("Current state: " + currentState + ", current char: " + line.charAt(currentPosition));
      switch(currentState) {
        case STATE_NOT_IN_STRING:
          if(currentPosition > startPosition) {
            // Take off the comma
            char fieldBytes[] = new char[currentPosition - startPosition - 1];
            line.getChars(startPosition, currentPosition - 1, fieldBytes, 0);
            outFields.add(removeStringPadding(new String(fieldBytes)));
          }

          startPosition = currentPosition;
          if(line.charAt(currentPosition) == '"') {
            currentState = STATE_IN_QUOTED_STRING;
          }
          else {
            currentState = STATE_IN_STRING;
          }
          break;
        case STATE_IN_STRING:
          if(line.charAt(currentPosition) == ',') {
            currentState = STATE_NOT_IN_STRING;
          }
          break;
        case STATE_IN_QUOTED_STRING:
          if(line.charAt(currentPosition) == '"') {
            if(currentPosition + 1 < line.length()) {
              if(line.charAt(currentPosition + 1) == '"') {
                currentPosition++;
              }
              else {
                currentState = STATE_NOT_IN_STRING;
              }
            }
          }
          break;
        default:
          break;
      }
      currentPosition++;
    }

    // Copy last field
    if(currentPosition > startPosition) {
      char fieldBytes[] = new char[currentPosition - startPosition];
      line.getChars(startPosition, currentPosition, fieldBytes, 0);
      outFields.add(removeStringPadding(new String(fieldBytes)));
    }

    return outFields;
  }

  /**
   * Removes CSV formatting from a string, which includes stripping leading and trailing
   * quotation marks (if they exist), and replacing double quotation marks with single
   * instances.
   * @param inString The string to process
   * @return Another string without padding and double quotes
   */
  public static String removeStringPadding(String inString) {
    int startPosition = 0;
    int endPosition = inString.length();
    if(inString.charAt(0) == '"') {
      startPosition++;
    }
    if(inString.charAt(inString.length() - 1) == '"') {
      endPosition--;
    }
    char outBytes[] = new char[endPosition];
    inString.getChars(startPosition, endPosition, outBytes, 0);
    String outString = new String(outBytes);
    return outString.replace("\"\"", "\"");
  }

  public boolean write(String filename, LocaleStringDictionary inStrings) throws IOException {
    BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));

    // Build array of locales to put
    LocaleStringSet localeNames = inStrings.get("LOCALE KEY");
    if(localeNames == null) {
      throw new IOException("Could not find 'LOCALE KEY' string in input map");
    }

    // First write the LOCALE KEY and all of the locales to file
    outStream.write("LOCALE KEY");
    for(String locale : localeNames.keySet()) {
      outStream.write("," + locale);
    }
    outStream.write(System.getProperty("line.separator"));
    // This key is no longer needed
    inStrings.remove("LOCALE KEY");

    // Now go through the rest of the keys and write them all to the file in the
    // same order
    int writtenKeys = 0;
    for(String key : inStrings.keySet()) {
      LocaleStringSet keyStrings = inStrings.get(key);
      outStream.write(key);
      for(String locale : localeNames.keySet()) {
        // System.out.println("Writing key: '" + key + "', locale: '" + locale + "'");
        outStream.write("," + addStringPadding(keyStrings.get(locale)));
        writtenKeys++;
      }
      outStream.write(System.getProperty("line.separator"));
    }

    System.out.println("Wrote " + writtenKeys + " keys to file");
    outStream.flush();
    outStream.close();
    return true;
  }

  /**
   * Escapes all spaces, commas, and other special characters to make a proper CSV string
   * see: http://en.wikipedia.org/wiki/Comma-separated_values
   * @param inString Input String
   * @return A string suitable to use in a CSV file
   */
  private String addStringPadding(String inString) {
    String outString = inString;
    if(inString == null || inString.isEmpty()) {
      outString = " ";
    }
    return "\"" + outString.replace("\"", "\"\"") + "\"";
  }
}

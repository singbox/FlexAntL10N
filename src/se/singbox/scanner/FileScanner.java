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

package se.singbox.scanner;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Scans an individual file for locale keys */
public class FileScanner {
  /**
   * Regular expression used to find localization keys.  This translates to
   * matching a flex statements of the form:
   * ResourceManager.getInstance().getString('myResources', 'LOCALIZED_KEY_NAME')
   * resourceManager.getString('myResources', 'LOCALIZED_KEY_NAME')
   */
  // TODO: This regex isn't so whitespace tolerant...
  private final String KEY_REGEX = "[Rr]esourceManager\\.(getInstance\\(\\)\\.)?getString\\s*\\([\"']([A-Za-z0-9_]+)[\"'],\\s*[\"']([A-Z0-9_]+)[\"']\\)";
  /** Position in the above regex group match where we will find the actual key name */
  private final int REGEX_KEY_INDEX = 3;

  public Set<String> scan(File inputFile) throws IOException {
    Set<String> outputKeys = new HashSet<String>();

    // Open the file and read through it line-by-line
    FileInputStream fileStream = new FileInputStream(inputFile);
    DataInputStream dataStream = new DataInputStream(fileStream);
    BufferedReader fileReader = new BufferedReader(new InputStreamReader(dataStream));
    String fileLine;
    Pattern keyPattern = Pattern.compile(KEY_REGEX);
    int numKeysFound = 0;

    while((fileLine = fileReader.readLine()) != null) {
      try {
        Matcher matcher = keyPattern.matcher(fileLine);
        while(matcher.find()) {
          outputKeys.add(matcher.group(REGEX_KEY_INDEX));
          numKeysFound++;
        }
      }
      catch(IllegalStateException e) {
        // Reached if no match is found
      }
    }

//    System.out.println(inputFile.getName() + ": " + numKeysFound + " keys found");
    dataStream.close();
    return outputKeys;
  }
}

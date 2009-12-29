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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class LocaleKeyChecker {
  /**
   * Check a given locale file against a set of keys to find unset
   * localization keys.  Will return false if a key is in the given
   * set, but NOT in the corresponding locale file
   * @param localeFile Locale properties file (any language) containing the
   * localized strings for the application
   * @param keys Set of known locale keys in the application's source code
   * @return True if the application's source code has no undefined keys
   * @throws IOException If inputFile could not be read
   */
  public Boolean check(File localeFile, Set<String> keys) throws IOException {
    Boolean result = true;
    int matchedCount = 0;
    int missingCount = 0;

    Properties localeKeys = new Properties();
    localeKeys.load(new FileInputStream(localeFile));

    for(String key : keys) {
      if(localeKeys.getProperty(key) == null) {
        System.err.println("Missing locale key '" + key + "'");
        result = false;
        missingCount++;
      }
      else {
        matchedCount++;
      }
    }

    if(missingCount > 0) {
      System.out.println("Found " + missingCount + " missing locale strings");
    }
    else {
      System.out.println("Matched all " + matchedCount + " locale strings");
    }

    return result;
  }

  /**
   * Prints warnings to the console for any keys which have been defined in
   * the locale property files but not used in the application's source code.
   * @param localeFile Locale properties file (any language) containing the
   * localized strings for the application
   * @param keys Set of known locale keys in the application's source code
   * @throws IOException If inputFile could not be read
   */
  public void warnUnused(File localeFile, Set<String> keys) throws IOException {
    Properties localeKeys = new Properties();
    localeKeys.load(new FileInputStream(localeFile));

    for(Object localeKey : localeKeys.keySet()) {
      if(!keys.contains(localeKey.toString())) {
        System.out.println("Unused locale key '" + localeKey.toString() + "'");
      }
    }
  }
}

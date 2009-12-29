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
import se.singbox.scanner.FileScanner;
import se.singbox.scanner.FolderScanner;
import se.singbox.scanner.LocaleKeyChecker;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This task is responsible for checking a source code directory for locale keys.
 * It accepts the following attribute arguments:
 * <ul>
 * <li>localeDir: A directory containing locale files to check</li>
 * <li>sourceDir: A directory containing the source code to scan</li>
 * <li>matchExtension: Only check for files with the given extension</li>
 * </ul>
 */
// TODO: It would be nice to support nested filesets with multiple sourceDir's
@SuppressWarnings({"UnusedDeclaration"})
// Is a top-level class
public class CheckL10NKeys extends Task {
  private String localeDir = null;
  private String sourceDir = null;
  private String matchExtension = null;
  private Boolean warnUnused = false;
  private Boolean failOnError = true;

  public void execute() {
    if(localeDir == null) {
      throw new BuildException("Attribute 'localeDir' must be defined");
    }
    else if(sourceDir == null) {
      throw new BuildException("Attribute 'sourceDir' must be defined");
    }

    try {
      FolderScanner fs = new FolderScanner();

      // Get all source files to be scanned
      Collection<File> sourceFiles = fs.scanFolder(sourceDir, matchExtension);
      Set<String> keysFound = new HashSet<String>();
      for(File oneFile : sourceFiles) {
        FileScanner scanner = new FileScanner();
        keysFound.addAll(scanner.scan(oneFile));
      }

      // Now verify each localization file has every key in the hashset
      Collection<File> localeFiles = fs.scanFolder(localeDir, "properties");
      Boolean result = true;
      for(File oneFile : localeFiles) {
        log("Checking " + sourceDir + " for locale " + oneFile.getParentFile().getName());
        LocaleKeyChecker checker = new LocaleKeyChecker();
        result &= checker.check(oneFile, keysFound);
        if(warnUnused) {
          checker.warnUnused(oneFile, keysFound);
        }
      }

      if(!result && failOnError) {
        throw new BuildException("One or more locales files are missing localization keys");
      }
    }
    catch(Exception e) {
      throw new BuildException(e);
    }
  }

  public void setLocaleDir(String dir) {
    localeDir = dir;
  }

  public void setSourceDir(String dir) {
    sourceDir = dir;
  }

  public void setMatchExtension(String extension) {
    matchExtension = extension;
  }

  public void setWarnUnused(Boolean warnUnused) {
    this.warnUnused = warnUnused;
  }

  public void setFailOnError(Boolean failOnError) {
    this.failOnError = failOnError;
  }
}

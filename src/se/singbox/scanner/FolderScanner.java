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
import java.util.ArrayList;
import java.util.Collection;

/** Scans a set of folders for a file pattern */
public class FolderScanner {
  /**
   * Returns a collection of files underneath the target folder, ignoring
   * all hidden files/directories
   * @param folderName The folder to scan
   * @param extension Scan only files with this extension, or null to scan all files.
   * Multiple extensions should be comma separated, eg. "xml,as,mxml"
   * @return The collection of matching files
   */
  public Collection<File> scanFolder(String folderName, String extension) {
    final Collection<File> allFiles = new ArrayList<File>();
    addFilesRecursively(new File(folderName), allFiles, extension);
    System.out.println(folderName + ": found " + allFiles.size() + " files");
    return allFiles;
  }

  private static void addFilesRecursively(File file, Collection<File> all, String extension) {
    final File[] children = file.listFiles();
    if(children != null) {
      for(File child : children) {
        // Filter out all hidden files
        if(!child.isHidden()) {
          if(child.isFile()) {
            if(extension != null) {
              if(checkExtension(child.getName(), extension)) {
                all.add(child);
              }
            }
            else {
              all.add(child);
            }
          }
          else if(child.isDirectory()) {
            addFilesRecursively(child, all, extension);
          }
        }
      }
    }
  }

  /**
   * Checks to make sure that the filename has the given extension
   * @param filename Filename
   * @param extension File extension (without the ".")
   * @return True if the file is of type extension
   */
  private static boolean checkExtension(String filename, String extension) {
    boolean result = false;
    int dotIndex = filename.lastIndexOf('.');
    String splitExtensions[] = extension.split(",");

    if(dotIndex > 0) {
      String fileExtension = filename.substring(dotIndex + 1, filename.length());
      for(String splitExtension : splitExtensions) {
        if(splitExtension.equals(fileExtension)) {
          result = true;
          break;
        }
      }
    }

    return result;
  }
}

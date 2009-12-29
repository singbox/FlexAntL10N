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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XLSFile implements LocaleSourceFile {
  public LocaleStringDictionary read(String filename) throws IOException {
    LocaleStringDictionary outStrings = new LocaleStringDictionary();
    InputStream xlsInputStream = new FileInputStream(filename);
    HSSFWorkbook xlsWorkbook = new HSSFWorkbook(xlsInputStream);
    HSSFSheet xlsSheet = xlsWorkbook.getSheetAt(0);

    // Read the first row and extract the locale names
    HSSFRow firstRow = xlsSheet.getRow(0);
    HSSFCell firstRowCell = firstRow.getCell(0);
    if(firstRowCell == null || !firstRowCell.toString().equals("LOCALE KEY")) {
      throw new IOException("Malformed XLS file; needs 'LOCALE KEY' as first cell");
    }

    List<String> parsedLocales = new ArrayList<String>();
    for(int i = 1; i < firstRow.getLastCellNum(); i++) {
      firstRowCell = firstRow.getCell(i);
      if(firstRowCell == null || firstRowCell.toString().isEmpty()) {
        break;
      }
      else {
        parsedLocales.add(firstRowCell.toString());
      }
    }

    File tempFilename = new File(filename);
    String shortFilename = tempFilename.getName();
    LocaleStringSet localeMap = new LocaleStringSet();
    for(String localeName : parsedLocales) {
      System.out.println(shortFilename + ": found locale '" + localeName + "'");
      localeMap.put(localeName, null);
    }
    outStrings.put("LOCALE KEY", localeMap);

    // Now parse the rest of the spreadsheet
    int numKeysFound = 0;
    for(int i = 1; i <= xlsSheet.getLastRowNum(); i++) {
      HSSFRow xlsRow = xlsSheet.getRow(i);
      HSSFCell keyCell = xlsRow.getCell(0);
      // System.out.println("Parsed: " + keyCell.toString());
      // Skip empty rows
      if(keyCell != null && !keyCell.toString().isEmpty()) {
        LocaleStringSet stringMap = new LocaleStringSet();
        for(int j = 1; j <= xlsRow.getLastCellNum(); j++) {
          HSSFCell xlsCell = xlsRow.getCell(j);
          if(xlsCell == null || xlsCell.toString().isEmpty()) {
            if(j < parsedLocales.size()) {
              System.err.println("WARNING: Missing string for key '" + keyCell.toString() +
                "' for locale '" + parsedLocales.get(j - 1) + "'");
              System.err.println("on line " + i + "," + j);
            }
          }
          else {
            stringMap.put(parsedLocales.get(j - 1), xlsCell.toString());
            numKeysFound++;
          }
        }
        outStrings.put(keyCell.toString(), stringMap);
      }
    }

    System.out.println(shortFilename + ": found " + numKeysFound + " locale strings");
    return outStrings;
  }

  public boolean write(String filename, LocaleStringDictionary inStrings) throws IOException {
    throw new IOException("Writing XLS files not yet supported");
  }
}

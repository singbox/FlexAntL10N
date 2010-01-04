Flex Ant Localization Tasks

The FlexAntL10N package provides a set of ant build tasks which help manage locale resources for large flex projects. The following tasks are currently provided:

CheckL10NKeys: Scans a directory containing actionscript or MXML files searching for uses of undefined locale keys.
CSV2Properties: Generates locale properties files from a master comma separated values (CSV) database
Properties2CSV: Generates a CSV database from a directory containing locale properties files
XLS2Properties: Generates locale properties files from a master Excel database file
FlexAntL10N depends on Apache POI 3.2 or later, which can be downloaded at: http://poi.apache.org/

Usage

To use FlexAntL10N in your project, you must first make a path in your ant build file which points to the location of the FlexAntL10N and POI jarfiles. For example:

  <path id="FlexAntL10N.classpath">
    <fileset dir="${basedir}/lib/FlexAntL10N">
      <include name="*.jar"/>
    </fileset>
  </path>

To generate localization properties files from an Excel database:

  <target name="xls2properties">
    <taskdef name="xls2properties"
      classname="se.singbox.tasks.XLS2Properties"
      classpathref="FlexAntL10N.classpath"/>
    <xls2properties xlsFile="${basedir}/locale/localization-keys.xls"
      outputDir="${basedir}/locale"/>
  </target>

To check a project's source code tree for undefined or unused localization keys:

  <target name="checkL10N">
    <taskdef name="checkL10NKeys" classname="se.singbox.tasks.CheckL10NKeys"
      classpathref="FlexAntL10N.classpath"/>
    <checkL10NKeys localeDir="${basedir}/locale"
      sourceDir="${basedir}/src"
      failOnError="true"
      warnUnused="true"/>
  </target>


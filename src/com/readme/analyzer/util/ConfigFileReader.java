package com.readme.analyzer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.config.Config;

public class ConfigFileReader {

    public ConfigFileReader() {

    }

    public List<String> getProjectDirList() {
	List<String> list = new ArrayList<String>();

	try {
	    // File file = new
	    // File(classLoader.getResource("filelist.txt").getFile());
	    File file = new File(Config.projList);
	    FileReader fileReader = new FileReader(file);
	    BufferedReader bufferedReader = new BufferedReader(fileReader);
	    // StringBuffer stringBuffer = new StringBuffer();
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
		list.add(line);
	    }
	    fileReader.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return list;
    }

    public String getProjectName(String projfolder) {

	int startindex = projfolder.lastIndexOf("/");

	String projname = projfolder.substring(startindex + 1);

	return projname;

    }

}

package com.util;

import java.io.File;

import com.repoanalysis.typeresolver.build.BuildConfigType;

public class ConfigTypeChecker {

    public static BuildConfigType getBuildConfigFileExits(String dir) {
	int type = 0;

	BuildConfigType configtype = BuildConfigType.Undefined_Config;

	if (type == 0) {
	    String filePathString = dir + "//" + "pom.xml";

	    File f = new File(filePathString);
	    if (f.exists() && !f.isDirectory()) {
		type = 2;
		configtype = BuildConfigType.Maven_Config;
	    }
	}

	if (type == 0) {
	    String filePathString = dir + "//" + "build.gradle";

	    File f = new File(filePathString);
	    if (f.exists() && !f.isDirectory()) {
		type = 3;
		configtype = BuildConfigType.Gradle_Config;
	    }
	}

	if (type == 0) {

	    String filePathString = dir + "//" + "build.xml";

	    File f = new File(filePathString);
	    if (f.exists() && !f.isDirectory()) {
		type = 1;
		configtype = BuildConfigType.Ant_Config;
	    }
	}

	return configtype;
    }

    public static boolean isGradleBuildGradlewExists(String dir) {
	boolean ret = false;

	String filePathString = dir + "//" + "gradlew";

	File f = new File(filePathString);
	if (f.exists() && !f.isDirectory()) {
	    ret = true;
	}
	return ret;
    }

}

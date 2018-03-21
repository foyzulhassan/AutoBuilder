package com.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.repoanalysis.typeresolver.build.BuildJavaVersion;

public class Config {
    
    //AutoBuilder's Required Directory Config
    public static String mainDir = "/home/Research/Autobuilder_Testing/AutoBuilder/maindir/"; //Change this path
    public static String workDir = mainDir + "workdir";
    public static String logDir = mainDir + "logs-lib";
    
    
    //Directory that contains GitHub Projects
    public static String srcRepoDir = "~/Research/Data/github_Proj/"; //Change this path
    public static String projList=srcRepoDir+"projlist.txt"; //Change the file name    
    
    //Deafult JRE Path
    public static String jrePath = "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib";
    
    // Java Version for Default Build
    public static BuildJavaVersion javaVersion=BuildJavaVersion.JAVA_18;
    
    //Cache Distory for any Third Party Libs
    public static String libCacheDir = mainDir + "libcache";
    
    public static String outDir = mainDir + "outputs";
    
    //Python Script Path that invokes build commands
    public static String script =  mainDir + "build-adv.py";
    
    //Summary File Path
    public static String summaryLog=mainDir+"buildsummary.log";
    
    public static void reconfig(String filepath) throws IOException{
	BufferedReader in = new BufferedReader(new FileReader(filepath));
	for(String line = in.readLine(); line!=null; line = in.readLine()){
	    if(line.startsWith("#") || line.trim().length() == 0){
		;
	    }else{
		if(line.startsWith("mainDir")){
		    Config.mainDir = line.substring(line.indexOf('=') + 1).trim();
		    Config.logDir = Config.mainDir + "logs-lib";
		    Config.workDir = Config.mainDir + "workdir";
		    Config.outDir = Config.mainDir + "outputs";
		}else if(line.startsWith("srcRepoDir")){
		    Config.srcRepoDir = line.substring(line.indexOf('=') + 1).trim();
		}else if(line.startsWith("jrePath")){
		    Config.jrePath = line.substring(line.indexOf('=') + 1).trim();
		}else if(line.startsWith("libCacheDir")){
		    Config.libCacheDir = line.substring(line.indexOf('=') + 1).trim();
		}else if(line.startsWith("script")){
		    Config.script = line.substring(line.indexOf('=') + 1).trim();
		}
	    }
	}
	in.close();
    }
}

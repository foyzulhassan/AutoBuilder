package com.autobuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.buildlog.analysis.BuildErrorLogAnalyzer;
import com.buildlog.analysis.BuildLogAnalyzer;
import com.cmd.executor.CmdExecutor;
import com.config.Config;
import com.filefolder.util.FolderManipulator;
import com.filefolder.util.ProjFinder;

import com.readme.analyzer.entities.CmdRank;
import com.readme.analyzer.entities.CmdRecognitionInfo;
import com.readme.analyzer.entities.CmdToExecute;
import com.readme.analyzer.entities.CmdWithPriority;
import com.readme.analyzer.entities.GitHubProjects;
import com.readme.analyzer.entities.ProjectBuildStatus;
import com.readme.analyzer.string.FileLinkParser;
import com.readme.analyzer.util.ConfigFileReader;
import com.readme.analyzer.util.FileFinder;
import com.readme.analyzer.util.LogPrinter;
import com.repoanalysis.typeresolver.build.BuildConfigType;
import com.repoanalysis.typeresolver.build.BuildJavaVersion;
import com.repoanalysis.typeresolver.build.BuildResult;
import com.repoanalysis.typeresolver.build.BuildType;
import com.repoanalysis.typeresolver.build.Builder;
import com.repoanalysis.typeresolver.build.JavacBuilder;
import com.repoanalysis.typeresolver.librepo.BaseResolver;
import com.repoanalysis.typeresolver.librepo.LibResolvingException;
import com.repoanalysis.typeresolver.librepo.LoadBasic;
import com.util.ConfigTypeChecker;
import com.util.FileManager;
import com.util.Logger;

public class Main {
    private static LogPrinter logprint = new LogPrinter();

    public static void main(String args[]) throws IOException {

	// Setup the Build Environment
	setupEnv();

	LoadBasic basicLoader = new LoadBasic(Config.jrePath,
		Config.libCacheDir);

	ConfigFileReader config = new ConfigFileReader();

	List<String> projfolders = new ArrayList<String>();
	projfolders = config.getProjectDirList();

	int readmeprojcount = 0;
	int autobuilderprojcount = 0;
	int hisbuildprojcount = 0;
	int javacbuildprojcount = 0;

	List<ProjectBuildStatus> projectsbuildstatuslst = new ArrayList<ProjectBuildStatus>();

	for (int i = 0; i < projfolders.size(); i++) {

	    boolean isbuildsuccess = false;

	    GitHubProjects proj = new GitHubProjects();
	    ProjectBuildStatus projstatus = new ProjectBuildStatus();

	    proj.setProjectName(projfolders.get(i));
	    proj.setProjectFolder(Config.srcRepoDir + projfolders.get(i));
	    proj.setProjectWorkDir(
		    Config.workDir + "/" + proj.getProjectName());

	    projstatus.setProjectName(proj.getProjectName());

	    if ((new File(Config.logDir + "/" + proj.getProjectName()))
		    .exists()) {
		continue;
	    }

	    System.out.println("Setup for Project:" + proj.getProjectName());

	    setUpWorkDir(Config.srcRepoDir + "/" + proj.getProjectName());

	    URL location = Main.class.getProtectionDomain().getCodeSource()
		    .getLocation();

	    CmdExecutor gitupdatecmdexecutor = new CmdExecutor(
		    Config.logDir + "/" + proj.getProjectName());

	    gitupdatecmdexecutor.ExecuteCommand(proj.getProjectWorkDir(),
		    "git reset --hard", location.getPath());

	    gitupdatecmdexecutor.ExecuteCommand(proj.getProjectWorkDir(),
		    "git pull origin", location.getPath());

	    Logger logger;

	    List<String> folderlist = new ArrayList<String>();

	    folderlist = ProjFinder.getProjectList(proj.getProjectWorkDir());

	    for (String folderitem : folderlist) {

		Path pathAbsolute = Paths.get(proj.getProjectWorkDir());
		Path pathBase = Paths.get(folderitem);
		Path pathRelative = pathAbsolute.relativize(pathBase);
		System.out.println(pathRelative);

		String relpathinprj = pathRelative.toString();

		if (relpathinprj.length() > 0)
		    relpathinprj = "/" + relpathinprj;

		// try ant, maven, and Gradle
		BuildConfigType buildtype;

		buildtype = ConfigTypeChecker.getBuildConfigFileExits(
			proj.getProjectWorkDir() + relpathinprj);

		try {
		    String sumStr = "START...";
		    System.out.println(
			    "Trying to build " + proj.getProjectName() + "...");
		    // setUpWorkDir(Config.srcRepoDir + "/"
		    // + proj.getProjectName());
		    sumStr += "|SETUP:Success";
		    logger = new Logger(proj.getProjectName() + relpathinprj,
			    Logger.LEVEL_INFO);

		    if (buildtype == BuildConfigType.Ant_Config) {
			gitupdatecmdexecutor.ExecuteCommand(
				proj.getProjectWorkDir() + relpathinprj,
				"ant clean", location.getPath());
			logger.logTimeStart("Ant");
			sumStr = sumStr + "|Ant:"
				+ tryBuild(proj.getProjectName() + relpathinprj,
					logger, BuildType.Type_Ant,
					Config.javaVersion, "0", false);
			logger.logTimeEnd("Ant");
			projstatus.setBuildConfType("Ant");
		    }

		    else if (buildtype == BuildConfigType.Maven_Config) {
			gitupdatecmdexecutor.ExecuteCommand(
				proj.getProjectWorkDir() + relpathinprj,
				"mvn clean", location.getPath());
			logger.logTimeStart("Maven");
			sumStr = sumStr + "|Maven:"
				+ tryBuild(proj.getProjectName() + relpathinprj,
					logger, BuildType.Type_Maven,
					Config.javaVersion, "0", false);
			logger.logTimeEnd("Maven");
			projstatus.setBuildConfType("Maven");
		    }

		    else if (buildtype == BuildConfigType.Gradle_Config) {
			logger.logTimeStart("Gradle");

			boolean checkgradlew = false;

			checkgradlew = ConfigTypeChecker
				.isGradleBuildGradlewExists(
					proj.getProjectWorkDir()
						+ relpathinprj);

			if (checkgradlew == false) {
			    gitupdatecmdexecutor.ExecuteCommand(
				    proj.getProjectWorkDir() + relpathinprj,
				    "gradle clean", location.getPath());
			    sumStr = sumStr + "|Gradle:"
				    + tryBuild(
					    proj.getProjectName()
						    + relpathinprj,
					    logger, BuildType.Type_Gradle,
					    Config.javaVersion, "0",
					    false);
			} else {
			    gitupdatecmdexecutor.ExecuteCommand(
				    proj.getProjectWorkDir() + relpathinprj,
				    "chmod 777 gradlew", location.getPath());
			    gitupdatecmdexecutor.ExecuteCommand(
				    proj.getProjectWorkDir() + relpathinprj,
				    "./gradlew clean", location.getPath());
			    sumStr = sumStr + "|Gradle:"
				    + tryBuild(
					    proj.getProjectName()
						    + relpathinprj,
					    logger, BuildType.Type_Gradlew,
					    Config.javaVersion, "0",
					    false);
			}

			logger.logTimeEnd("Gradle");
			projstatus.setBuildConfType("Gradle");
		    }

		    else {
			logger.logTimeStart("Javac");
			sumStr = sumStr + "|Javac:"
				+ tryBuild(proj.getProjectName() + relpathinprj,
					logger, BuildType.Type_Javac,
					Config.javaVersion, "0", false);
			logger.logTimeEnd("Javac");
			projstatus.setBuildConfType("Javac");
		    }

		    String flagPart = sumStr.substring(20);

		    if (flagPart.indexOf("Success") != -1) {
			// logger.logSummary(sumStr);
			autobuilderprojcount++;
			projstatus
				.setBuildSuccessfulType("Deafult AutoBuilder");
			projstatus.setBuildStatus("SUCCESSFUL");
			continue;
		    }

		    boolean buildfail = true;

		    flagPart = sumStr.substring(20);
		    if (flagPart.indexOf("Success") != -1) {
			// logger.logSummary(sumStr);
			continue;
		    }

		    if (buildfail) {
			sumStr = "START...";
			sumStr += "|SETUP:Success";
			System.out.println(
				"Trying to build with Historical Build "
					+ proj.getProjectName() + "...");

			if (buildtype == BuildConfigType.Ant_Config) {
			    gitupdatecmdexecutor.ExecuteCommand(
				    proj.getProjectWorkDir() + relpathinprj,
				    "ant clean", location.getPath());

			    logger.logTimeStart("Ant");
			    sumStr = sumStr + "|Ant:"
				    + tryBuild(
					    proj.getProjectName()
						    + relpathinprj,
					    logger, BuildType.Type_His_Ant,
					    BuildJavaVersion.JAVA_00, "1",
					    false);
			    logger.logTimeEnd("Ant");
			}

			else if (buildtype == BuildConfigType.Maven_Config) {
			    gitupdatecmdexecutor.ExecuteCommand(
				    proj.getProjectWorkDir() + relpathinprj,
				    "mvn clean", location.getPath());
			    logger.logTimeStart("Maven");
			    sumStr = sumStr + "|Maven:"
				    + tryBuild(
					    proj.getProjectName()
						    + relpathinprj,
					    logger, BuildType.Type_His_Maven,
					    BuildJavaVersion.JAVA_00, "1",
					    false);
			    logger.logTimeEnd("Maven");
			}

			else if (buildtype == BuildConfigType.Gradle_Config) {
			    logger.logTimeStart("Gradle");

			    boolean checkgradlew = false;

			    checkgradlew = ConfigTypeChecker
				    .isGradleBuildGradlewExists(
					    proj.getProjectWorkDir()
						    + relpathinprj);

			    if (checkgradlew == false) {
				gitupdatecmdexecutor.ExecuteCommand(
					proj.getProjectWorkDir() + relpathinprj,
					"gradle clean", location.getPath());
				sumStr = sumStr + "|Gradle:"
					+ tryBuild(
						proj.getProjectName()
							+ relpathinprj,
						logger,
						BuildType.Type_His_Gradle,
						BuildJavaVersion.JAVA_00, "1",
						false);
			    } else {
				gitupdatecmdexecutor.ExecuteCommand(
					proj.getProjectWorkDir() + relpathinprj,
					"chmod 777 gradlew",

					location.getPath());
				gitupdatecmdexecutor.ExecuteCommand(
					proj.getProjectWorkDir() + relpathinprj,
					"./gradlew clean", location.getPath());
				sumStr = sumStr + "|Gradle:"
					+ tryBuild(
						proj.getProjectName()
							+ relpathinprj,
						logger,
						BuildType.Type_His_Gradlew,
						BuildJavaVersion.JAVA_00, "1",
						false);
			    }

			    logger.logTimeEnd("Gradle");
			}

			else {
			    logger.logTimeStart("Javac");
			    sumStr = sumStr + "|Javac:"
				    + tryBuild(
					    proj.getProjectName()
						    + relpathinprj,
					    logger, BuildType.Type_His_Javac,
					    BuildJavaVersion.JAVA_00, "1",
					    false);
			    logger.logTimeEnd("Javac");
			}

		    }

		    /////// historical build end

		    flagPart = sumStr.substring(20);
		    if (flagPart.indexOf("Success") != -1) {
			// logger.logSummary(sumStr);
			hisbuildprojcount++;
			projstatus.setBuildSuccessfulType(
				"Historical AutoBuilder");
			projstatus.setBuildStatus("SUCCESSFUL");
			continue;
		    }

		    // setUpWorkDir(Config.srcRepoDir + "/"
		    // + proj.getProjectName());
		    BaseResolver resolver = new BaseResolver(
			    Config.workDir + "/" + proj.getProjectName(),
			    logger);
		    try {
			resolver.resolve(basicLoader);
			sumStr += "|RESOLVE:Success";
		    } catch (LibResolvingException e) {
			logger.log(e.getMessage(), Logger.LEVEL_IMPORTANT);
			sumStr += "|RESOLVE:Failure";
			e.printStackTrace();
			cleanAndCopy(proj.getProjectName(), false);
		    }

		    // retry build
		    Builder jb = new JavacBuilder();
		    BuildResult result = jb.build(
			    proj.getProjectName() + relpathinprj, logger,
			    BuildJavaVersion.JAVA_18.getText(), "0");
		    if (result.success()) {
			sumStr += "|BUILD:Success";
			projstatus.setBuildSuccessfulType(
				"Javac Resolve and Build");
			projstatus.setBuildStatus("SUCCESSFUL");

			javacbuildprojcount++;
		    } else {
			sumStr += "|BUILD:Failure";
		    }
		    // sumStr += "|" + result.getMajorError();
		    // sumStr += result.getSummary();
		    cleanAndCopy(proj.getProjectName(), false);
		    // logger.logSummary(sumStr);
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}

	    } // *

	    projectsbuildstatuslst.add(projstatus);
	    buildReporting(projectsbuildstatuslst);
	}

	buildReporting(projectsbuildstatuslst);

	System.out.println("Build Process Completed");
	System.out.println(" AutoBuilder Successful Count:"
		+ autobuilderprojcount + " Historical Build Successful Count:"
		+ hisbuildprojcount);

    }

    private static void buildReporting(
	    List<ProjectBuildStatus> projectsbuildstatuslst) {
	String fileName = Config.summaryLog;

	try {
	    // Assume default encoding.
	    FileWriter fileWriter = new FileWriter(fileName);

	    // Always wrap FileWriter in BufferedWriter.
	    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

	    for (int index = 0; index < projectsbuildstatuslst
		    .size(); index++) {
		ProjectBuildStatus projectstatus = projectsbuildstatuslst
			.get(index);

		bufferedWriter.write(projectstatus.getProjectName() + "----"
			+ projectstatus.getBuildConfType() + "----"
			+ projectstatus.getBuildStatus() + "----"
			+ projectstatus.getBuildSuccessfulType() + "\n");
	    }

	    // Always close files.
	    bufferedWriter.close();
	} catch (IOException ex) {
	    System.out.println("Error writing to file '" + fileName + "'");
	    // Or we could just do this:
	    // ex.printStackTrace();
	}

    }

    private static void setupEnv() {
	if (!(new File(Config.logDir)).exists()) {
	    (new File(Config.logDir)).mkdirs();
	}
	if (!(new File(Config.outDir)).exists()) {
	    (new File(Config.outDir)).mkdirs();
	}
    }

    private static String tryBuild(String proj, Logger logger, BuildType type,
	    BuildJavaVersion javaversion, String hisbuild,
	    boolean trycleanbuild) throws IOException, InterruptedException {

	if (trycleanbuild == true)
	    setUpWorkDir(Config.srcRepoDir + "/" + proj);

	Builder b = Builder.createBuilder(type);
	BuildResult result = b.build(proj, logger, javaversion.getText(),
		hisbuild);
	if (result.success()) {
	    return "Success";
	} else {
	    return "Failure";
	}
    }

    private static void cleanAndCopy(String projName, boolean copy)
	    throws IOException {
	if (copy) {
	    FileUtils.copyDirectoryToDirectory(
		    new File(Config.workDir + '/' + projName),
		    new File(Config.outDir));
	}
	FileManager.recursiveDeleteDir(Config.workDir + '/' + projName);
    }

    private static void setUpWorkDir(String projPath) throws IOException {
	File workDirFile = new File(Config.workDir);
	if (!workDirFile.exists()) {
	    workDirFile.mkdirs();
	} else {
	    FileManager.recursiveDeleteDir(Config.workDir);
	    workDirFile.mkdirs();
	}

	try {
	    FileUtils.copyDirectoryToDirectory(new File(projPath),
		    new File(Config.workDir));
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

}

package com.repoanalysis.typeresolver.build;

import java.io.IOException;

import com.config.Config;
import com.util.CommandRunner;
import com.util.Logger;
import com.util.CommandRunner.CommandResult;

public class BaseBuilder extends Builder{

    public BuildResult build(String proj, Logger logger, String javaversion, String hisbuild) throws IOException,
	    InterruptedException {
	        CommandResult cr = CommandRunner.runCommand("python " + Config.script
	        	+ " " + this.type.getText() + " " + logger.getBuildLogPath(this.type) + " "
	        	+ Config.workDir + " " + proj+ " " + javaversion + " "+ hisbuild);
	        logger.log("build", cr.getStdOut(), Logger.LEVEL_INFO);
	        logger.log("buildError", cr.getErrOut(), Logger.LEVEL_IMPORTANT);
	        
	        return checkBuild(logger.getBuildLogPath(this.type), logger);
	    }

}

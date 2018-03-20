package com.repoanalysis.typeresolver.build.errors;

import com.repoanalysis.typeresolver.build.BuildError;

public class PackageDoesNotExist extends BuildError{

    private String packName;

    public PackageDoesNotExist(String fileName, int lineNum, String packName) {
	super(fileName, lineNum, "package does not exist");
	this.packName = packName;
    }
    public String getPackName(){
	return this.packName;
    }

}

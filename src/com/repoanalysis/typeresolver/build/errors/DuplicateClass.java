package com.repoanalysis.typeresolver.build.errors;

import com.repoanalysis.typeresolver.build.BuildError;

public class DuplicateClass extends BuildError{

    private String duplicate;

    public DuplicateClass(String fileName, int lineNum, String duplicate) {
	super(fileName, lineNum, "duplicate class");
	this.duplicate = duplicate;
    }
    public String getDuplicateClass(){
	return this.duplicate;
    }
}

package com.repoanalysis.typeresolver.build;

public enum BuildConfigType {
    Maven_Config ("mvn"),
    Ant_Config ("ant"),
    Gradle_Config("gradle"),
    Undefined_Config("un");   

    private String text;
    private BuildConfigType(String text){
	this.text = text;
    }
    public String getText(){
	return this.text;
    }

}

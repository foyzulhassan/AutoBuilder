package com.repoanalysis.typeresolver.build;

public class BaseGradleBuilder extends BaseBuilder {
    public BaseGradleBuilder(){
	this.type = BuildType.Type_Gradle;
    }
    
    public BaseGradleBuilder(BuildType type){
	this.type = type;
    }

}

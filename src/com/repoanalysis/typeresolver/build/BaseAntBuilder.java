package com.repoanalysis.typeresolver.build;

public class BaseAntBuilder extends BaseBuilder{
    public BaseAntBuilder(){
	this.type = BuildType.Type_Ant;
    }
    
    public BaseAntBuilder(BuildType type){
   	this.type = type;
       }
}

# AutoBuilder
An automatic approach to build Java project managed by Ant, Maven and Gradle in batch mode. For building we used default build commands for Ant, Maven and Gradle. Using this tool projects can also be built with different version of JDK. Even after using default command there can have issue of Java and Build tool version issue. To resolve the issue, we used commit date to dynamically select tool version.

### How to Configure and Run the Tool
Since the tool tries to build project in batch mode, we need to set the path of the subject project repo directory. Suppose the subject projects are in "~/GitHubProject_Dir" folder. Then in Config.java file we need to set following path

public static String srcRepoDir = "~/GitHubProject_Dir"; 

Inside the repo you can select the projects that you want to build using project list file. Project list file should contain all the projects folder name each line and list file path should also be set as follows:

public static String projList=srcRepoDir+"projlist.txt"; 

After setting the configurations we need to build project following mvn build command:

mvn clean install

This will generate jar file at target directory. You can run jar with following command at target folder:

java -cp AutoBuilder-0.0.1-SNAPSHOT.jar  com.autobuilder.Main

AutoBuilder will try to build all the selected project in source directory. All the project's build log will be stored at ~\AutoBuilder\maindir\logs-lib. After trying to build all the project, summary of project build will be stored at ~\AutoBuilder\maindir\buildsummary.log file.


# Papers on AutoBuilder

Automatic Building of Java Projects in Software Repositories: A Study on Feasibility and Challenges.
Foyzul Hassan, Shaikh Mostafa, Edmund Lam, Xiaoyin Wang
ACM/IEEE Symposium on Empirical Software Engineering and Measurement (ESEM), 38–47, Nov, 2017





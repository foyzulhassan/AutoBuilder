import sys
import commands
import subprocess


global android_path
global aapt_path

def countLevel(pathStr):
    return len(pathStr.split('/')) - 1

def checkTree(seed, fileList):
    prefix = seed[0:seed.rfind('/')]
    for fileItem in fileList:
        if not fileItem.startswith(prefix):
            return False
    return True

def checkAndroid(path):
    rfiles = commands.getoutput('grep -r \"import.*\\.R;\" --include *.java ' + path).split('\n')
    if len(rfiles) == 0 or len(rfiles[0]) == 0:
        return (None, None, None)
    rfilepath = path + '/summary_R_files'
    f = open(rfilepath, 'w')
    for line in rfiles:
        f.write(line + '\n')
    f.close()
    
    festfile = commands.getoutput('find ' + path + ' -name \"AndroidManifest*\"').split('\n')

    if len(festfile) == 0 or len(festfile[0]) == 0:
        return (None, None, None)
    festpath = festfile[0]
    
    parentpath = festpath[0][:festpath[0].rfind('/')]

   

    if 'res' in commands.getoutput('ls ' + parentpath).split('\n'):
        respath = parentpath + '/res'
    else: 
        resfiles = commands.getoutput('find ' + path + ' -name \"res\"').split('\n')
        if len(resfiles) == 0 or len(resfiles[0]) == 0:
            return (None, None, None)
        else:
            respath = resfiles[0]
    return (rfilepath, festpath, respath)
    

def handleAndroid(path):
    (rfiles, festpath, respath) = checkAndroid(path)
    if rfiles == None or festpath == None or respath == None:
        return
    else:

        command = aapt_path + '/aapt package -f -M ' + festpath + ' -F a.out -I ' + android_path + '/android.jar' + ' -S ' + respath + ' -m -J ' + path + '/src'

	festfilepath = path + '/summary_command_files'
    	f = open(festfilepath, 'w')    	
        f.write(command + '\n')
    	f.close()

        print command
        print commands.getoutput(command) 
    

def buildConf(path):
    commands.getoutput('find ' + path + ' -name \"*.java\" > ' + path + '/tmp-javalist')
    jars = commands.getoutput('find ' + path + ' -name \"*.jar\"').split('\n')
    outjars = ''
    for jar in jars:
        outjars = outjars + jar  + ':'
    if outjars.endswith(':'):
        outjars = outjars[:-1]
    commands.getoutput('echo '+ '\"' + outjars + '\" > ' + path + '/tmp-jarlist')
    return path + '/tmp-javalist'

def findConf(path, tgt):
    if tgt == 'javac':
        handleAndroid(path)
        return buildConf(path)
    confs = commands.getoutput('find ' + path + ' -name \''+tgt + '\'').split('\n')
#    print confs
    minlevel = 1000
    minconf = ''
    for conf in confs:
        level = countLevel(conf)
        if level < minlevel:
            minlevel = level
            minconf = conf
    if minconf == '':
        return None
    else:
        if(checkTree(minconf, confs)):
            return minconf
        else:
            return 'Warning:' + minconf



def getAntDefaultCmd(ctype,parentPath):
	buildtaskflag = False
	buildtaskfound = False
	buildtask ='build'
	if ctype == 'ant':
		gradletasks= commands.getoutput('cd ' + parentPath + ' && ' + 'ant')
		for line in gradletasks.splitlines():
    			
    			if buildtaskfound == False:
				buildtaskflag = True			
				tokens=line.split(':')
				task=tokens[0]
				
				if task=='build' or task=='compile' or task=='assemble' or task=='jar':
					buildtask=task
					buildtaskfound=True;

	

	return buildtask


def getGradleDefaultCmd(ctype,parentPath):
	buildtaskflag = False
	buildtaskendflag = False
	buildtaskfoundasm = False
	buildtaskfoundasmdbg = False
	buildtaskfoundbld = False
	buildtask ='assemble'
	if ctype == 'gradle':
		gradletasks= commands.getoutput('cd ' + parentPath + ' && ' + 'gradle tasks')
		for line in gradletasks.splitlines():    			
    			if line == 'Build tasks':
				buildtaskflag = True

			if buildtaskflag == True and buildtaskendflag == False:
				if len(line)>0:
					tokens=line.split(' - ')
					task=tokens[0]
				
					if task=='assemble':						
						buildtaskfoundasm=True;

					elif task=='assembleDebug':						
						buildtaskfoundasmdbg=True;

					elif task=='build':						
						buildtaskfoundbld=True;
			        else:
					buildtaskendflag=True


	elif ctype == 'gradlew':
		gradletasks= commands.getoutput('cd ' + parentPath + ' && ' + './gradlew tasks')
		for line in gradletasks.splitlines():    			
    			if line == 'Build tasks':
				buildtaskflag = True

			if buildtaskflag == True and buildtaskendflag == False:
				if len(line)>0:
					tokens=line.split(' - ')
					task=tokens[0]
				
					if task=='assemble':						
						buildtaskfoundasm=True;

					elif task=='assembleDebug':						
						buildtaskfoundasmdbg=True;

					elif task=='build':						
						buildtaskfoundbld=True;
			        else:
					buildtaskendflag=True


	if buildtaskfoundbld == True: 
		buildtask ='build'
	if buildtaskfoundasm == True: 
		buildtask ='assemble'	
	if buildtaskfoundasmdbg == True: 
		buildtask ='assembleDebug'
	

	return buildtask

		

def getCommand(ctype,parentPath,javaversion):
    if ctype=='mvn':
	if javaversion=="1_8":
        	return 'mvn compile'
	elif javaversion=="1_7":
		return 'JAVA_HOME=/usr/local/java/jdk1.7.0_79 && mvn compile'
        else:
                return 'mvn compile'

    elif ctype == 'ant':
	deafultgradlecmd = getAntDefaultCmd(ctype,parentPath)
        return 'ant '+deafultgradlecmd
    elif ctype == 'gradle':
	deafultgradlecmd = getGradleDefaultCmd(ctype,parentPath)
	if javaversion=="1_8":
        	return 'gradle '+deafultgradlecmd
	elif javaversion=="1_7":
		return 'gradle '+deafultgradlecmd+' -Dorg.gradle.java.home=/usr/local/java/jdk1.7.0_79/'
	else:
		return 'gradle '+deafultgradlecmd
    elif ctype == 'gradlew':
	deafultgradlecmd = getGradleDefaultCmd(ctype,parentPath)
	if javaversion=="1_8":
        	return 'chmod 777 gradlew && ./gradlew '+deafultgradlecmd
	elif javaversion=="1_7":
		return 'chmod 777 gradlew && ./gradlew '+deafultgradlecmd+' -Dorg.gradle.java.home=/usr/local/java/jdk1.7.0_79/'
        else:
                return 	'chmod 777 gradlew && ./gradlew '+deafultgradlecmd
    elif ctype == 'javac':
        return 'javac -cp @tmp-jarlist @tmp-javalist'
    else:
        return None

def getTgt(ctype):
    if ctype == 'mvn':
        return 'pom.xml'
    elif ctype == 'ant':
        return 'build.xml'
    elif ctype == 'gradle':
	return 'build.gradle'
    elif ctype == 'gradlew':
	return 'build.gradle'
    elif ctype == 'javac':
        return 'javac'
    else:
        return None


def setJavaEnv(commityr):
    print 'setJavaEnv'
    if(commityr>2011 and commityr<=2016):	
   	return 'JAVA_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/JDK/jdk1.7.0_79 && PATH=$JAVA_HOME/bin:$PATH'      
    elif(commityr>2006 and commityr<=2011):
	return 'JAVA_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/JDK/jdk1.6.0_45 && PATH=$JAVA_HOME/bin:$PATH'
    else:
        return 'JAVA_HOME=$JAVA_HOME'


def setAndroidSdkEnv(commityr):
    if(commityr==2014):
   	return 'ANDROID_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/ANDROID_SDK/2014/android-sdk-linux && PATH=$ANDROID_HOME:$PATH && PATH=$ANDROID_HOME/build-tools:$PATH'
    elif(commityr==2013):
   	return 'ANDROID_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/ANDROID_SDK/2013/android-sdk-linux && PATH=$ANDROID_HOME:$PATH && PATH=$ANDROID_HOME/build-tools:$PATH'
    elif(commityr==2012):
   	return 'ANDROID_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/ANDROID_SDK/2012/android-sdk-linux && PATH=$ANDROID_HOME:$PATH && PATH=$ANDROID_HOME/build-tools:$PATH'
    elif(commityr==2011):
   	return 'ANDROID_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/ANDROID_SDK/2011/android-sdk-linux && PATH=$ANDROID_HOME:$PATH && PATH=$ANDROID_HOME/build-tools:$PATH'
    else:    
	return 'ANDROID_HOME=$ANDROID_HOME'


def setAndroidNdkEnv(commityr):
    if(commityr==2015):
   	return 'NDK_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/ANDROID_NDK/2015/android-ndk-r10e && PATH=$NDK_HOME:$PATH'
    elif(commityr==2014):
   	return 'NDK_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/ANDROID_NDK/2014/android-ndk-r9d && PATH=$NDK_HOME:$PATH'
    elif(commityr==2013):
   	return 'NDK_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/ANDROID_NDK/2013/android-ndk-r8e && PATH=$NDK_HOME:$PATH'
    elif(commityr==2012):
   	return 'NDK_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/ANDROID_NDK/2012/android-ndk-r7c && PATH=$NDK_HOME:$PATH'
    elif(commityr==2011):
   	return 'NDK_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/ANDROID_NDK/2011/android-ndk-r6 && PATH=$NDK_HOME:$PATH'
    else:
        return 'NDK_HOME=$NDK_HOME'



def setMavenEnv(commityr):
    if(commityr==2014):
   	return 'M2_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/Maven/2014/apache-maven-3.2.3 && PATH=$M2_HOME/bin:$PATH'
    elif(commityr==2013):
   	return 'M2_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/Maven/2013/apache-maven-3.1.0 && PATH=$M2_HOME/bin:$PATH'
    elif(commityr==2012):
   	return 'M2_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/Maven/2012/apache-maven-3.0.4 && PATH=$M2_HOME/bin:$PATH'
    else:
        return 'M2_HOME=$M2_HOME'

def setGradleEnv(commityr):
    if(commityr==2014):
   	return 'GRADLE_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/Gradle/2014/gradle-2.2.1 && PATH=$GRADLE_HOME/bin:$PATH'
    elif(commityr==2013):
   	return 'GRADLE_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/Gradle/2013/gradle-1.10 && PATH=$GRADLE_HOME/bin:$PATH' 
    elif(commityr==2012):
   	return 'GRADLE_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/Gradle/2012/gradle-1.3 && PATH=$GRADLE_HOME/bin:$PATH'
    elif(commityr==2011):
   	return 'GRADLE_HOME=/home/foyzulhassan/AutoBuilder_Lib_Path/Gradle/2011/gradle-1.0-milestone-6 && PATH=$GRADLE_HOME/bin:$PATH' 
    else:
        return 'GRADLE_HOME=$GRADLE_HOME'


def getLastCommitInfo(projpath):
    lastcommit= commands.getoutput('cd ' + projpath + ' && ' + 'git log -1 --date=short --pretty=format:%cd')
    commityr=lastcommit[:4]    

    return commityr


def getLastCommitHash(projpath):
    commithash= commands.getoutput('cd ' + projpath + ' && ' + 'git log --pretty=format:%h -n 1')
    print commithash

def init(projname, homeDir):
    commands.getoutput('cp -r ' + homeDir + '/sample-projs/' + projname + ' ' + homeDir + '/workdir')

def clean():
    commands.getoutput('rm -rf workdir/*')

def runConf(confType, logPath, workDir, projname, javaversion, hisbuild):
    projpath = workDir + '/' + projname
    print 'Trying to build ' + projname + ' with ' + confType 
    getLastCommitHash(projpath)

    if(hisbuild=="1"):
    	commityr=int(getLastCommitInfo(projpath))
    	hisjava=setJavaEnv(commityr)
    	hissdk=setAndroidSdkEnv(commityr)
    	hisndk=setAndroidNdkEnv(commityr)
    	hismvn=setMavenEnv(commityr)
    	hisgradle=setGradleEnv(commityr)        
   
    	conf = findConf(projpath, getTgt(confType))
    	if conf == None:
        	print "Cannot find " + confType + " configuration file"
        	return
    	elif conf.startswith('Warning:'):
        	conf = conf[8:]
        	print 'Warning'
        	commands.getoutput('echo \"Warning: tree violation build in project:' + projname + ';;build-approach:' + confType + ';;filepath:' + conf + '\n\" >> summary.txt')
    	else:
        	commands.getoutput('echo \"Info: normal build in project:' + projname + ';;build-approach:' + confType + ';;filepath:' + conf + '\n\" >> summary.txt')

#    subPath = conf[len(homeDir + '/sample-projs/'):]
#    projname = subPath[0:subPath.find('/')]
#    print projname   
#    init(projname)
#    newPath = homeDir + '/workdir/' + subPath
    	parentPath = conf[0:conf.rfind('/')]
#    if confType=='javac':
#        print commands.getoutput('cp ' + workDir + '/tmp-javalist ' + parentPath)
#        print commands.getoutput('cp ' + workDir + '/tmp-jarlist ' + parentPath)


    	print 'Running ' + confType + ' on ' + conf

	execmd='cd ' + parentPath + ' && ' + hisjava + ' && ' + hissdk + ' && ' + hisndk + ' && ' + hismvn + ' && ' + hisgradle + ' && ' +getCommand(confType,parentPath,javaversion) +' > ' + logPath + ' 2>&1';
	print execmd
    	commands.getoutput(execmd)
    	commands.getoutput('cd ' + workDir)
    	if confType=='javac':
        	commands.getoutput('echo \"tmp-javas\" >>' + logPath)
        	commands.getoutput('cat ' + projpath + '/tmp-javalist >> ' + logPath)
        	commands.getoutput('echo \"tmp-jars\" >>' + logPath)
        	commands.getoutput('cat ' + projpath + '/tmp-jarlist >> ' + logPath)
     
    else:
	conf = findConf(projpath, getTgt(confType))
    	if conf == None:
        	print "Cannot find " + confType + " configuration file"
        	return
    	elif conf.startswith('Warning:'):
        	conf = conf[8:]
        	print 'Warning'
        	commands.getoutput('echo \"Warning: tree violation build in project:' + projname + ';;build-approach:' + confType + ';;filepath:' + conf + '\n\" >> summary.txt')
    	else:
        	commands.getoutput('echo \"Info: normal build in project:' + projname + ';;build-approach:' + confType + ';;filepath:' + conf + '\n\" >> summary.txt')

#    subPath = conf[len(homeDir + '/sample-projs/'):]
#    projname = subPath[0:subPath.find('/')]
#    print projname   
#    init(projname)
#    newPath = homeDir + '/workdir/' + subPath
    	parentPath = conf[0:conf.rfind('/')]
#    if confType=='javac':
#        print commands.getoutput('cp ' + workDir + '/tmp-javalist ' + parentPath)
#        print commands.getoutput('cp ' + workDir + '/tmp-jarlist ' + parentPath)


    	print 'Running ' + confType + ' on ' + conf
	execmd='cd ' + parentPath + ' && ' + getCommand(confType,parentPath,javaversion) +' > ' + logPath + ' 2>&1'
	print execmd
    	commands.getoutput(execmd)
    	commands.getoutput('cd ' + workDir)
    	if confType=='javac':
        	commands.getoutput('echo \"tmp-javas\" >>' + logPath)
        	commands.getoutput('cat ' + projpath + '/tmp-javalist >> ' + logPath)
        	commands.getoutput('echo \"tmp-jars\" >>' + logPath)
        	commands.getoutput('cat ' + projpath + '/tmp-jarlist >> ' + logPath)





cmd, confType, logPath, workDir, projname, javaversion, hisbuild = sys.argv

android_path = '/home/foyzulhassan/Android/android-sdk-linux/platforms/android-23'
aapt_path = '/home/foyzulhassan/Android/android-sdk-linux/build-tools/23.0.0'
runConf(confType, logPath, workDir, projname, javaversion, hisbuild)



import os
import sys
import time
import RealtimeAppBuilder

#==============================================================================#
# Realtime Framework Builder Configuration
#==============================================================================#

config = {}

#------------------------------------------------------------------------------#
# Project   
#------------------------------------------------------------------------------#
config["Project"] = \
{
      "Name"                      : "ISCInterface",
      "Version"                   : "v1.0-r5.6",
      "BuildNumber"               : time.strftime("%y%m%d", time.localtime(time.time()))+"1",
}

#------------------------------------------------------------------------------#
# BuildEnvironment   
#------------------------------------------------------------------------------#
config["BuildEnvironment"] = \
   {
      "JDKHome"                   : "C:\\Program Files\\Postilion\\realtime\\jdk",
      "OutputDir"                 : ".\\build"
   }


#------------------------------------------------------------------------------#
# Tasks   
#------------------------------------------------------------------------------#
config["Tasks"] = \
{
      "ISCInterface"    :
         {
            "TaskType"            : RealtimeAppBuilder.TASK_TYPE_INTERCHANGE,
            "Service"             : True,
            "Description"         : "ISCInterface Interface.",
            "MainClass"           : "postilion.realtime.sdk.env.App",
            "ClassArguments"      : 
               [
                  "ISCInterface",
                  0,
                  "postilion.realtime.sdk.node.InterchangeProcessor",
                  "postilion.realtime.sdk.node.Interchange",
                  "postilion.realtime.iscinterface.ISCInterface"
               ]
         }
}

#------------------------------------------------------------------------------#
# Events
#------------------------------------------------------------------------------#
config["Events"] = \
   {
      "EventResourceFile"    : ".\\resources\\events\\events.er",
   }


#------------------------------------------------------------------------------#
# Java    
#------------------------------------------------------------------------------#
config["Java"] = \
   {
      "BasePackage"          : "postilion.realtime.iscinterface",
      "ClassPaths"			 : \
      	 [
           ".\\resources\\oem\\lib\\LibIsoTranslateB24.jar"
         ],
      "SourceDirs"           : \
         [
            (".\\source\\java", RealtimeAppBuilder.INCLUDE_RECURSE)
         ],
   }


#------------------------------------------------------------------------------#
# JavaDoc									 
#------------------------------------------------------------------------------#
#config["JavaDoc"] = \
#{
 #     "Title"                     : "ISCInterface SDK version " + config["Project"]["Version"],
  #    "SourceDirs"                  : \
   #      [
    #        "./source/java",
     #       "./build/java",
    #     ],
     # "Packages"                  : \
     #    [
     #       ("postilion.realtime.iscinterface", RealtimeAppBuilder.INCLUDE_RECURSE)
     #    ],
     # "Archive"                   : "javadoc",
#}


#------------------------------------------------------------------------------#
# Documentation																					 #
#------------------------------------------------------------------------------#


config["Documentation"] = \
	{
		 "userguide" 			: 
            {                 	
				 "Name"			: "User Guide",
				 "Type"			: "CHM",
				 "SourceDir"	: "doc\\userguide",
				 "Project"		: "ug_ISCInterface",
				 "Replacements"	        :   
					 {           	
						 "Files"	: ["Title.htm"]
					 }
			 },
		 "releasenotes" 		: 
			 {                 	
				 "Name"			: "Release Notes",
				 "Type"			: "CHM",
				 "SourceDir"	: "doc\\releasenotes",
				 "Project"		: "rn_ISCInterface",
			 }
	}


#------------------------------------------------------------------------------#
# Release									 #
#------------------------------------------------------------------------------#

config["Release"] = \
	{
		"Packaging"				: \
			[	
				(RealtimeAppBuilder.WINDOWS_ONLY,"build\\install\\standard_edition\\setup.exe", "setup.exe"),
				(RealtimeAppBuilder.WINDOWS_ONLY,"build\\install\\standard_edition\\setupc.exe", "setupc.exe"),
				(RealtimeAppBuilder.LINUX_ONLY,"build\\install\\standard_edition\\setup", "setup"),
				(RealtimeAppBuilder.LINUX_ONLY,"build\\install\\standard_edition\\setup.dat", "setup.dat"),
				#("ALL_PLATFORMS", "doc\\README.TXT", "README.TXT"),
				("ALL_PLATFORMS", "build\\doc\\ug_ISCInterface.chm", "ug_ISCInterface.chm"),
				("ALL_PLATFORMS", "build\\doc\\rn_ISCInterface.chm", "rn_ISCInterface.chm"),
			]
	}
	
#==============================================================================#
# Realtime Framework Builder										          #
#==============================================================================#

# Get the target and any target arguments that are present
target = RealtimeAppBuilder.getTargetName(sys.argv)
target_args = RealtimeAppBuilder.getTargetArguments(sys.argv)
   
# Build the target project.
RealtimeAppBuilder.RealtimeAppBuilder(config).buildProject(build_target=target, build_target_args=target_args)

Name: Shrikant Dange
Email ID: sdange1@binghamton.edu
Project Title: Remote Procedure Calls (RPC)

Implementation Details:

Project includes total 4 packages.
Package: com.shrikant.client
implemented client code
Package: com.shrikant.server
implemented client code			
Package: com.shrikant.handler
implemented service handler code	
Package: com.shrikant.service
autogenerated files by thrift

Folder: Directory
                 --local  (folder to save input files)
                 --server
Folder: finalJars
includes all required jars
Folder: src
contains all packages in it

Files:

server, bash script 
client, bash script 
build.xml
README.txt 

SAMPLE OUTPUT:

SERVER:
^Cremote14:~/ThirdSem/DistributedSystems/RemoteProcedureCalls> ./server 9097
Server
Buildfile: /import/linux/home/sdange1/ThirdSem/DistributedSystems/RemoteProcedureCalls/build.xml

build-subprojects:

init:

build-project:
     [echo] RemoteProcedureCalls: /import/linux/home/sdange1/ThirdSem/DistributedSystems/RemoteProcedureCalls/build.xml
    [javac] Compiling 2 source files to /import/linux/home/sdange1/ThirdSem/DistributedSystems/RemoteProcedureCalls/bin

build:

BUILD SUCCESSFUL
Total time: 1 second
Buildfile: /import/linux/home/sdange1/ThirdSem/DistributedSystems/RemoteProcedureCalls/build.xml

Server:
     [java] Starting the file Server


	 
remote14:~/ThirdSem/DistributedSystems/RemoteProcedureCalls> ./client localhost 9097 --operation delete --filename test3.txt --user shrikant
Client
Buildfile: /import/linux/home/sdange1/ThirdSem/DistributedSystems/RemoteProcedureCalls/build.xml

Client:
     [java] delete
     [java] Report:SUCCESSFUL

BUILD SUCCESSFUL
Total time: 1 second

	 
	 

TO COMPILE:
1. Extract the file.
2. Execute: 
          ant build

TO RUN:
Server:
./server 9090
Client:
./client localhost 9090 --operation write --filename test3.txt --user shrikant


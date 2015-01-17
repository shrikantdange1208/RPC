package com.shrikant.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.shrikant.service.FileStore;
import com.shrikant.service.RFile;
import com.shrikant.service.RFileMetadata;
import com.shrikant.service.Status;
import com.shrikant.service.StatusReport;
import com.shrikant.service.SystemException;

public class Client {
	public static void main(String[] args){
		TIOStreamTransport transport1 = new TIOStreamTransport(System.out);
		TProtocol jsonProtocol = new TJSONProtocol.Factory().getProtocol(transport1);
		String host=null;
		int port=0;
		String operation=null;
		String user=null;
		String fileName=null;
		
		try{
			if(!((args.length==6)||(args.length==8))){
				SystemException argsException = new SystemException();
				String message = "Enter valid 5 arguments in format:";
				String argumentFormat = "localhost 9090 --opertaion read --fileName example.txt --user guest";
				argsException.setMessage(message+argumentFormat);
				throw argsException;
			}
		}catch(SystemException ex){
			try {
				ex.write(jsonProtocol);
				System.exit(0);
			} catch (TException e) {
				e.printStackTrace();
			}
		}
		host = args[0].trim().toLowerCase();
		port = Integer.parseInt(args[1].trim());
		String argumentType;
		int i;
		for(i=2;i<args.length;i=i+2){
			argumentType = args[i].trim().toLowerCase();
			if(argumentType.equals("--operation")){
				operation = args[i+1].trim().toLowerCase();
				System.out.println(operation);
			}
			else if(argumentType.equals("--filename")){
				fileName = args[i+1].trim().toLowerCase();
			}
			else if(argumentType.equals("--user")){
				user = args[i+1].trim().toLowerCase();
			}
		}
		TTransport transport;
		try {
			transport = new TSocket(host,port);
			transport.open();
			
			TProtocol protocol = new TBinaryProtocol(transport);
			FileStore.Client client = new FileStore.Client(protocol);
			
			doOperation(client,operation,fileName,user);
			transport.close();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}

	private static void doOperation(FileStore.Client client,String operation, String fileName, String user) {
		
		TIOStreamTransport transport = new TIOStreamTransport(System.out);
		TProtocol jsonProtocol = new TJSONProtocol.Factory().getProtocol(transport);
		if(operation.equals("write")){
			try {
				RFile writeFile = null;
				RFileMetadata metadata=null;
				String contents=null;;
				contents = readLocalFile(fileName);
				if(contents==null){
					System.out.println("File does not exists in local directory");
					System.exit(0);
				}
				else{		
					//Partially update the new Object of RFile and pass it to the handler
					writeFile= new RFile();
					metadata = new RFileMetadata();
					metadata.setOwner(user);
					metadata.setFilename(fileName);
					writeFile.setMeta(metadata);
					writeFile.setContent(contents);
					//Partially update the new Object of RFile and pass it to the handler
					
					StatusReport status = client.writeFile(writeFile);
					status.write(jsonProtocol);
				}
				
			} catch (SystemException e2) {
				try {
					e2.write(jsonProtocol);
					System.exit(0);
				} catch (TException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (TException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		else if(operation.equals("read")){
			try {
				RFile rFile = client.readFile(fileName, user);
				rFile.write(jsonProtocol);
				System.out.println("\n");
			} catch (SystemException e1) {
				try {
					e1.write(jsonProtocol);
				} catch (TException e) {
					e.printStackTrace();
				}
			} catch (TException e1) {
				e1.printStackTrace();
			}
		}
		else if(operation.equals("list")){
		 	
			List<RFileMetadata> ownedFileList = new ArrayList<RFileMetadata>();
			try {
				ownedFileList=client.listOwnedFiles(user);
				if(ownedFileList.size()==0){
					System.out.println("There are no files for this owner");
				}
				else{
					for(RFileMetadata data : ownedFileList){
						data.write(jsonProtocol);
					}
				}
				
			} catch (SystemException e) {
				try {
					e.write(jsonProtocol);
					System.exit(0);
				} catch (TException e1) {
					e1.printStackTrace();
				}
				
			} catch (TException e) {
				e.printStackTrace();
			}
		}
		else if(operation.equals("delete")){
			try {
				StatusReport report = client.deleteFile(fileName, user);
				Status status = report.getStatus();
				System.out.println("Report:"+status.toString());
			} catch (SystemException e) {
				try {
					e.write(jsonProtocol);
					System.exit(0);
				} catch (TException e1) {
					
					e1.printStackTrace();
				}
				
			} catch (TException e) {
				
				e.printStackTrace();
			}
		}
		
		
	}

	private static String readLocalFile(String fileName){
		System.out.println("Client:"+fileName);
		String content="";
	    File directory = new File("./Directory/local/");
	    File[] listOfFiles = directory.listFiles();
	    for(File file: listOfFiles){
	    	if(file.isFile()){
	    		if(file.getName().equals(fileName)){
	    			String line=null;
	    			try {
						BufferedReader br = new BufferedReader(new FileReader("./Directory/local/"+fileName));
						while((line = br.readLine())!=null){
							content+= line+"\n";
						}
						br.close();
						return content;	
	    			}catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
	    		}
	    	}
	    }
		return null;
	}
}

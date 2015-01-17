package com.shrikant.handler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.thrift.TException;
import com.shrikant.service.FileStore;
import com.shrikant.service.RFile;
import com.shrikant.service.RFileMetadata;
import com.shrikant.service.Status;
import com.shrikant.service.StatusReport;
import com.shrikant.service.SystemException;

public class FileHandler implements FileStore.Iface{

	public static Map<String,RFile> fileStorage;
	
	public FileHandler(){
		fileStorage = new HashMap<String,RFile>();
		File oldFiles = new File("./Directory/server/");
		for(File file:oldFiles.listFiles()){
			if(!(file.isDirectory())){
				file.delete();
			}
		}
	}
	@Override
	public List<RFileMetadata> listOwnedFiles(String user)
			throws SystemException, TException {
		
		List<RFileMetadata> ownedFiles = new ArrayList<RFileMetadata>();
		Set<String> keySet = fileStorage.keySet();
		Iterator<String> keySetIterator = keySet.iterator();
		RFile rFile;
		RFileMetadata metadata;
		while(keySetIterator.hasNext()){
			String key = keySetIterator.next();
			rFile = fileStorage.get(key);
			metadata=rFile.getMeta();
			
			
			
			if((metadata.getOwner().equals(user))&&((metadata.getDeleted()==0))){
				ownedFiles.add(metadata);
			}
			
		}
		return ownedFiles;
	}

	@Override
	public StatusReport writeFile(RFile rFile) throws SystemException,
			TException {
		File newFile=null;
		RFile serverRFile = null;
		RFileMetadata serverMetadata = null;
		
		RFileMetadata userMetadata = rFile.getMeta();
		String absoluteFileName = userMetadata.getFilename();
		String[] name = absoluteFileName.split("\\.");
		String fileName = name[0];
		String content = rFile.getContent();
		SystemException exception;
		
		//With static map
			if(fileStorage.containsKey(fileName)){
				serverRFile = fileStorage.get(fileName);
				serverMetadata = serverRFile.getMeta();
				if(userMetadata.getOwner().equals(serverMetadata.getOwner())){
					//Overwrite file
					File serverFile = new File("./Directory/server/"+absoluteFileName);
					try {
						PrintWriter writer = new PrintWriter(serverFile);
						writer.write(content);
						writer.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					//update data structure(Time Stamps , content lenght , content hash)
					  serverRFile.setContent(content);
					  updateMetadata(serverMetadata,content,serverFile);
					  serverMetadata.setVersion(serverMetadata.getVersion()+1);
					  serverRFile.setMeta(serverMetadata);
					//update data structure(Time Stamps , content lenght , content hash)
					// generate a status report an return
					  	Status status = Status.SUCCESSFUL;
						StatusReport statusReport = new StatusReport();
						statusReport.setStatus(status);
					// generate a status report an return
						
						return statusReport;
				}
				else{
					exception = new SystemException();
					exception.setMessage("You cant overwrite a file of different owner. Enter valid arguments");
					throw exception;
				}
			}
			else{
				//create a new file and write the contents
				try {
					newFile = new File("./Directory/server/"+absoluteFileName);
					FileOutputStream is = new FileOutputStream(newFile);
				    OutputStreamWriter osw = new OutputStreamWriter(is);
					BufferedWriter writer = new BufferedWriter(osw);
					writer.write(rFile.getContent());
					writer.close();
					is.close();
					osw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//update its metadata
				updateMetadata(userMetadata,content,newFile);
				userMetadata.setVersion(0);
				//update its metadata
				
				//create new entry in fileStorage
				rFile.setContent(content);
				rFile.setMeta(userMetadata);
				fileStorage.put(fileName, rFile);
				//create new entry in fileStorage
				
				//create status report and return
				Status status = Status.SUCCESSFUL;
				StatusReport statusReport = new StatusReport();
				statusReport.setStatus(status);
				
				Set<String> key = fileStorage.keySet();
				Iterator<String> keySetIterator = key.iterator();
				RFile rf;
				RFileMetadata data;
				while(keySetIterator.hasNext()){
					String key1 = keySetIterator.next();
					rf = fileStorage.get(key1);
					data=rf.getMeta();
				}
				return statusReport;
				//create status report and return
			}
	}

	private void updateMetadata(RFileMetadata metadata, String content, File file) {
		
			metadata.setCreated(System.currentTimeMillis());
			
			metadata.setUpdated(System.currentTimeMillis());
			metadata.setDeleted(0);
			metadata.setContentLength(content.length());
			String md5Hash = generateHash(file);
			metadata.setContentHash(md5Hash);
	}
	
	private String generateHash(File file) {
		MessageDigest md=null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			
			e1.printStackTrace();
		}
		String hash = "";
		byte[] data = new byte[(int)file.length()];
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			fis.read(data);
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		md.update(data);
		 
		byte[] digest = md.digest();
	 
		for (int i = 0; i < digest.length; i++) {
			String hex = Integer.toHexString(digest[i]);
			if (hex.length() == 1)
				hex = "0" + hex;
			hex = hex.substring(hex.length() - 2);
			hash += hex;
		}
		
		return hash;
	}
	@Override
	public RFile readFile(String filename, String owner)
			throws SystemException, TException {
		SystemException exception;
		RFile rFile = null;
		String[] name= filename.split("\\.");
		String key = name[0];
		rFile = fileStorage.get(key);
		if(rFile==null){
			exception= new SystemException();
			exception.setMessage("File not found. Please enter valid file name");
			throw exception;
		}
		else{
			
			RFileMetadata metadata= rFile.getMeta();
			if(metadata.getOwner().equals(owner)){
				return rFile;
			}
			else{
				exception=new SystemException();
				exception.setMessage("File with given owner not found. Please enter valid arguments");
				throw exception;
			}
		}
		
	}

	@Override
	public StatusReport deleteFile(String filename, String owner)
			throws SystemException, TException {
		SystemException exception;
		Status status = null;
		StatusReport statusReport = new StatusReport();
		RFile rFile = null;
		RFileMetadata metadata = null;
		
		
		String[] names = filename.split("\\.");
		String currentFileName = names[0];
		System.out.println(currentFileName);
		long deletedTime = 0;
		
		if(fileStorage.containsKey(currentFileName)){
				rFile = fileStorage.get(currentFileName);
				metadata =rFile.getMeta();
				if(owner.equals(metadata.getOwner())){
					if(metadata.getDeleted()==0){
						File file = new File("./Directory/server/"+filename);
						if(file.delete()){
							deletedTime = System.currentTimeMillis();
							metadata.setDeleted(deletedTime);
							rFile.setContent(null);
							status = Status.SUCCESSFUL;
							statusReport.setStatus(status);
							return statusReport;
					}
				}else{
					exception = new SystemException();
					exception.setMessage("File is already deleted.Enter valid arguments");
					throw exception;
				}
				
			}else{
					exception = new SystemException();
					exception.setMessage("File is not owned by specified owner.Enter valid arguments");
					throw exception;
			}
		}else{
			exception = new SystemException();
			exception.setMessage("Requested file does not exists. Enter valid arguments");
			throw exception;
		}
		status = Status.FAILED;
		statusReport.setStatus(status);
		return statusReport;
	}

}

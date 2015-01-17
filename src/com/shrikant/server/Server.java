package com.shrikant.server;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import com.shrikant.handler.FileHandler;
import com.shrikant.service.FileStore;
import com.shrikant.service.FileStore.Iface;
import com.shrikant.service.FileStore.Processor;
import com.shrikant.service.SystemException;

public class Server {
	public static FileHandler fileHandler;
	public static FileStore.Processor<Iface> processor;
	
	public static void main(String[] args){
		TIOStreamTransport transport = new TIOStreamTransport(System.out);
		TProtocol jsonProtocol = new TJSONProtocol.Factory().getProtocol(transport);
		try{
			if(args.length!=1){
				SystemException argsException = new SystemException();
				argsException.setMessage("Invalid Argumetnts. Please provide port number");
				throw argsException;
			}
		}catch(SystemException exception){
			try {
				exception.write(jsonProtocol);
				System.exit(0);
			} catch (TException e) {
				e.printStackTrace();
			}
		}
		
		try{
			int portNumber = Integer.parseInt(args[0].trim());
			fileHandler = new FileHandler();
			processor = new FileStore.Processor<FileStore.Iface>(fileHandler);
			startServer(processor,portNumber);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private static void startServer(Processor<Iface> processor,int portNumber) {
		
		try {
			TServerTransport serverTransport = new TServerSocket(portNumber);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));
			System.out.println("Starting the file Server");
			server.serve();
			serverTransport.close();
		} catch (TTransportException e) {
			
			e.printStackTrace();
		}
	}
}

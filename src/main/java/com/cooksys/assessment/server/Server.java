package com.cooksys.assessment.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Server implements Runnable {
	private static Logger log = LoggerFactory.getLogger(Server.class);
	
	private int port;
	private ExecutorService executor;
	static Map<String, Socket> connectedClients = new ConcurrentHashMap<String, Socket>();
	static Queue<Message> broadcastQ = new ConcurrentLinkedQueue<Message>();
	
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	public void run() {
		log.info("server started");
		ServerSocket ss;
		try {
			// Ready to receive connections
			ss = new ServerSocket(this.port);
			while (true) {
				// Accepts connections and returns a socket object
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket);
				executor.execute(handler);
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	/**
	 * Used to send (broadcast) a message to all clients. Because two threads could call this
	 * method at the same time, it is synchronized to ensure that all clients receive the
	 * broadcasted messages in the same order. 
	 * @throws IOException
	 */
	public static synchronized void broadcast() throws IOException {
		// Get the message to send to all the clients
		Message m = broadcastQ.remove();
		for (String key: connectedClients.keySet()) {
			// Create a buffered writer for each client
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(connectedClients.get(key).getOutputStream()));
			
			// Create a gson object necessary to convert the message object to JSON
			GsonBuilder builder = new GsonBuilder();
	        Gson gson = builder.create();
	        
	        // Write the object
			output.write(gson.toJson(m));
			output.flush();
		}
	}

}

package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						// If the username is already taken, alert the new user and close the socket.
						// Otherwise, they are still connected to the server but without a username and
						// without a way to set it. This gives them the option to reconnect with a new username.
						if (Server.connectedClients.containsKey(message.getUsername())) {
							message.setContents("This username is already taken, please select a new one.");
							sendClientMessage(message, this.socket);
							this.socket.close();
							break;
						// If the username is not taken, sign them up
						} else {
							Server.connectedClients.put(message.getUsername(), this.socket);
							// Send all other clients a message about 'client x' connection
							message.setContents("has connected");
							Server.broadcastQ.add(message);
							Server.broadcast();
							break;
						}
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						this.socket.close();
						// Remove a disconnected client from the connectedClients HashMap
						Server.connectedClients.remove(message.getUsername());
						// Send all other clients a message about 'client x' disconnection
						message.setCommand("disconnect");
						Server.broadcastQ.add(message);
						Server.broadcast();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message);
						log.info(response);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						// Add the message to the broadcastQ, call broadcast()
						Server.broadcastQ.add(message);
						Server.broadcast();
						break;
					case "users":
						log.info("user <{}> requested all users", message.getUsername());
						// Get the connected users
						Set<String> users = Server.connectedClients.keySet();
						// Set the contents of the message to users.toString
						String usersString = new String();
						
						// Add a newline to the end of each user so it's prints out nicely
						for (String user : users) {
							usersString += (user + '\n');
						}
						
						// Chop off the last newline character to it prints nicely to the console.
						message.setContents(usersString.substring(0, usersString.length()-1));
						String allUsers = mapper.writeValueAsString(message);
						writer.write(allUsers);
						writer.flush();
						break;
					default:
						// if the command has an @ symbol in the front
						if (message.getCommand().matches("[@].+")) {			                
							log.info("User <{}> has sent a direct message to <{}>, saying <{}>", message.getUsername(), message.getCommand(), message.getContents());
							// Get the socket info for the message recepient
							Socket destination = Server.connectedClients.get(message.getCommand().substring(1));
							// If user attempeted to send a message to a user that isn't logged in, alert them
							if (destination == null) {
								// Alert the user that they made an error
								message.setContents("You attempted to send a message to " + message.getCommand() + ", but no user is logged in with that username");
								String errorResponse = mapper.writeValueAsString(message);
								writer.write(errorResponse);
								writer.flush();
								break;
							} else {
								sendClientMessage(message, destination);
								break;
							}
						// If it doesn't start with @
						} else {
							message.getContents();
						}
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
		
	}
	
	/**
	 * The method used to send a message to a single client.
	 * @param message - a message object
	 * @param socket - a socket object
	 * @throws IOException
	 */
	public void sendClientMessage(Message message, Socket socket) throws IOException {
		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		//Create the gson object necessary to convert the message object to JSON
		GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        
        // Write the object
		output.write(gson.toJson(message));
		output.flush();
	}

}

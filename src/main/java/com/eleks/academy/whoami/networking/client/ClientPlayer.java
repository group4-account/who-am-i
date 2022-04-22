package com.eleks.academy.whoami.networking.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Logger;

import com.eleks.academy.whoami.core.Player;

public class ClientPlayer implements Player {

	private String name;
	private Socket socket;
	private BufferedReader reader;
	private PrintStream writer;
	private String message;
	private static Logger log = Logger.getLogger(ClientPlayer.class.getName());

	public ClientPlayer(String name, Socket socket) throws IOException {
		this.name = name;
		this.socket = socket;
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintStream(socket.getOutputStream());
		this.message = "test message";
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getQuestion() {
		String question = "";

		try {
			writer.println("Ask your questinon: ");
			question = reader.readLine();
			message = name + " Asked question " + question;
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Player: " + name + ". Asks: " + question);
		return question;	
	}

	@Override
	public String answerQuestion(String question, String character) {
		String answer = "";
		
		try {
			writer.println("Answer second player question: " + question + "Character is:"+ character);
			answer = reader.readLine();
			message = name + " Answer second player question: " + question;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return answer;
	}

	@Override
	public String getGuess() {
		String answer = "";
		
		try {
			writer.println("Write your guess: ");
			answer = reader.readLine();
			message = name + " guess answer: " + answer;
			
		} catch (IOException e) {

			e.printStackTrace();
		}
		return answer;
	}

	@Override
	public boolean isReadyForGuess() {
		String answer = "";
		
		try {
			writer.println("Are you ready to guess? ");
			answer = reader.readLine();
			message = name + (answer.equals("Yes") ? " Ready to guess" : " Not ready to guess");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return answer.equals("Yes") ? true : false;
	}

	@Override
	public String answerGuess(String guess, String character) {
		String answer = "";
		
		try {
			writer.println("Write your answer: ");
			answer = reader.readLine();
			message = name + " write answer:" + answer;
		} catch (IOException e) {

			e.printStackTrace();
		}
		return answer;
	}
	
	@Override
	 public void logMessage()
    {
       log.info(message);
    }   

}

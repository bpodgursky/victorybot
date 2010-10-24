package ai;


/*****************************************************************************
 * $Id: TestAI.java,v 1.1 2004/09/09 06:32:18 heb Exp $ 
 *
 * Copyright � 2002, 2004 by Henrik Bylund
 * This code is released in the public domain.
 *****************************************************************************/

import dip.daide.comm.MessageListener;
import dip.daide.comm.DisconnectedException;
import dip.daide.comm.UnknownTokenException;
import dip.daide.comm.Server;
import gamesearch.GameSearch;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import representation.Country;
import state.BeliefState;
import state.BoardState;
import state.DiplomaticState;

/**
 * An interactive client for testing the communication. Lines are read from
 * <code>System.in</code> and all incoming messages are printed to
 * <code>System.out</code>. When breaking the input into tokens, whitespace is
 * not allowed within text tokens (Any text surrounded by <code>'</code>)
 * 
 * @author <a href="mailto:heb@ludd.luth.se">Henrik Bylund</a>
 * @version 1.0
 */
public class Bot implements MessageListener {
	static String VERSION = "v 0.7";
	
	public final String name;
	private final Server serv;
	boolean atPrompt;
	
	private final BoardState board;
	private final DiplomaticState diplomaticState;
	private final BeliefState beliefs;
	
	private GameSettings settings;
	private GameSearch search;
	
	void printPrompt() {
		if (!atPrompt) {
			System.out.print("Enter message: ");
			atPrompt = true;
		}
	}

	void printMsg(String who, String[] message) {
		StringBuffer sbuf = new StringBuffer();
		for (int i = 0; i < message.length; i++) {
			String m = message[i];
			sbuf.append(m);
			boolean last = (i + 1) == message.length;
			if (m.equals("(") || (!last && message[i + 1].equals(")"))) {
				continue;
			} else {
				if (!last) {
					sbuf.append(' ');
				}
			}
		}
		if (atPrompt) {
			System.out.println();
			atPrompt = false;
		}
		System.out.println("<" + who + ">: " + sbuf.toString());
	}

	public Bot(InetAddress ip, int port, String name) throws Exception {
		this.name = name;

		board = new BoardState();
		diplomaticState = new DiplomaticState();
		beliefs = new BeliefState();
		
		serv = new Server(ip, port);
		serv.addMessageListener(this);
		serv.connect();
		
		String[] nme = new String[] { "NME", "(", "'" + name + "'", ")",
				"(", "'" + VERSION + "'", ")" };
		printMsg(name, nme);
		serv.send(nme);
			
	}

	public void messageReceived(String[] message) {
		try{
			// Print the message
			printMsg("srv", message);
			// Accept the MAP message
			if (message[0].equals("MAP")) {
				try {
					String[] tokens = new String[message.length + 3];
					tokens[0] = "YES";
					tokens[1] = "(";
					System.arraycopy(message, 0, tokens, 2, message.length);
					tokens[tokens.length - 1] = ")";
					printMsg(name, tokens);
					serv.send(tokens);
				} catch (UnknownTokenException ute) {
					ute.printStackTrace();
					System.exit(1);
				} catch (DisconnectedException de) {
					System.err.println("Disconnected, exiting");
					System.exit(1);
				}
			}
			
			if(message[0].equals("HLO")){
				
				Country power = Country.valueOf(message[2]);
				String password = message[5];
				
				int lvl = 0;
				int mtl = -1;
				int rtl = -1;
				int btl = -1;
				
				boolean dsd = false;
				boolean aoa = false;
				
				int i = 6;
				while(i < message.length){
					
					String s = message[i];
					
					if(s.equals("(") || (s.equals(")"))){
						i++;
					}
					else if(s.equals("LVL")){
						i++;	
						lvl = Integer.parseInt(message[i]);
						i++;
					}
					else if(s.equals("MTL")){
						i++;
						mtl = Integer.parseInt(message[i]);
						i++;
					}
					else if(s.equals("RTL")){
						i++;
						rtl = Integer.parseInt(message[i]);
						i++;
					}
					else if(s.equals("BTL")){
						i++;
						btl = Integer.parseInt(message[i]);
						i++;
					}
					else if(s.equals("DSD")){
						dsd = true;
						i++;
					}
					else if(s.equals("AOA")){
						aoa = true;
						i++;
					}else{
						throw new Exception("Unknown token");
					}
				}
				
				this.settings = new GameSettings(power, password, lvl, mtl, rtl, btl, dsd, aoa);
				this.search = new GameSearch(board.getPlayer(power), board, diplomaticState, beliefs);

			}
			
			if (message[0].equals("ORD")) {
				StringBuilder move = new StringBuilder();
				for(int i = 0; i < message.length; i++){
					move.append(message[i]);
					move.append(" ");
				}
				
				String moves = move.toString();
				
				board.update(moves);
				diplomaticState.update(moves);
				beliefs.update(moves);
				
				search.noteBoardUpdate();
				search.noteDiplomaticUpdate();
				search.noteBeliefUpdate();
				
			}
			printPrompt();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException {

		Thread[] bots = new Thread[1];
		for(int i = 0; i < 1; i++){
			bots[i] = new Thread(new BotLauncher(args));
			bots[i].start();
		}
		
		while(true){
			Thread.sleep(100);
		}
	}
	
	//TODO this is not the right way to do this, but it is easy
	static class BotLauncher implements Runnable{
		
		String[] args;
		
		public BotLauncher(String[] args){
			this.args = args;
		}
		
		public void run(){
			try {
				Bot victoryBot = new Bot(InetAddress.getByName(args[0]), Integer
						.parseInt(args[1]), args[2]);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				
				while (true) {
					victoryBot.printPrompt();
					String line = br.readLine();
					String[] order = line.split(" ");
					victoryBot.atPrompt = false;
					try {
						victoryBot.printMsg(victoryBot.name, order);
						victoryBot.serv.send(order);
					} catch (UnknownTokenException ute) {
						System.err.println("Unknown token '" + ute.getToken()
								+ "' - Message not sent.");
					} catch (DisconnectedException de) {
						System.err.println("Disconnected from server, "
								+ "command not sent");
					}
				}
				
			} catch (ArrayIndexOutOfBoundsException be) {
				usage();
			} catch (UnknownHostException uhe) {
				System.err.println("Unknown host: " + uhe.getMessage());
			} catch (NumberFormatException nfe) {
				usage();
			} catch (Exception ex) {
				
				ex.printStackTrace();
				
				System.out.println("Error");
			} 
		}
	}

	public static void usage() {
		System.err.println("Usage:\n" + "  TestAI <ip> <port> <name>");
	}
}

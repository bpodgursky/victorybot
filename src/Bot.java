
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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
	String name;
	Server serv;
	boolean atPrompt;
	GameState board;

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

	public Bot(InetAddress ip, int port, String name) {
		this.name = name;
		try {
			board = new GameState();
			serv = new Server(ip, port);
			serv.addMessageListener(this);
			serv.connect();
			String[] nme = new String[] { "NME", "(", "'" + name + "'", ")",
					"(", "'" + VERSION + "'", ")" };
			printMsg(name, nme);
			serv.send(nme);
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (DisconnectedException de) {
			System.out.println("Ok, we're disconnected. Exiting...");
			System.exit(0);
		} catch (UnknownTokenException ute) {
			System.err.println("Unknown token '" + ute.getToken() + "'");
			System.exit(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void messageReceived(String[] message) {
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
		if (message[0].equals("ORD")) {
			StringBuilder move = new StringBuilder();
			for(int i = 0; i < message.length; i++)
			{
				move.append(message[i]);
				move.append(" ");
			}
			board.update(move.toString());
		}
		printPrompt();
	}

	public static void main(String[] args) {
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
			System.out.println("Error");
		} 
	}

	public static void usage() {
		System.err.println("Usage:\n" + "  TestAI <ip> <port> <name>");
	}
}

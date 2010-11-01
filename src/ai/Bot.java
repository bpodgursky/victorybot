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
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import order.Order;
import order.OrderFactory;

import representation.Country;
import state.constant.BoardConfiguration;
import state.dynamic.BeliefState;
import state.dynamic.BoardState;
import state.dynamic.DiplomaticState;
import state.dynamic.BoardState.Phase;

/**
 * An interactive client for testing the communication. Lines are read from
 * <code>System.in</code> and all incoming messages are printed to
 * <code>System.out</code>. When breaking the input into tokens, whitespace is
 * not allowed within text tokens (Any text surrounded by <code>'</code>)
 * 
 * @author <a href="mailto:heb@ludd.luth.se">Henrik Bylund</a>
 * @version 1.0
 */
public class Bot{
	static String VERSION = "v 0.7";
	
	public final String name;
	private final Server serv;
	boolean atPrompt;
	
	private final BoardConfiguration board;
	private final DiplomaticState diplomaticState;
	private final BeliefState beliefs;
	
	private BoardState boardState;
	
	private GameSettings settings;
	private GameSearch search;
	
	private final Thread messageHandlerThread;
	private final BotMessageHandler botMessageHandler;

	private final OrderFactory orderFactory;
	
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
	
	String cleanMsg(String[] message){
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
		
		return sbuf.toString();
	}

	public Bot(InetAddress ip, int port, String name) throws Exception {
		this.name = name;

		board = new BoardConfiguration();
		diplomaticState = new DiplomaticState();
		beliefs = new BeliefState();
		
		boardState = board.getInitialState();
		
		orderFactory = new OrderFactory(board);
		
		botMessageHandler = new BotMessageHandler();
		
		serv = new Server(ip, port);
		serv.addMessageListener(botMessageHandler);
		serv.connect();
		
		messageHandlerThread = new Thread(botMessageHandler);
		messageHandlerThread.start();
		
		String[] nme = new String[] { "NME", "(", "'" + name + "'", ")",
				"(", "'" + VERSION + "'", ")" };
		printMsg(name, nme);
		serv.send(nme);
			
	}

	class BotMessageHandler implements MessageListener, Runnable {
		
		public final static long SUBMISSION_BUFFER = 5000; 
		
		long nextOrders = -1;
		boolean submitted = false;
		
		//	Orders are sent one at a time; gather them all here.  when a NOW arrives,
		//	send them to the gamestate en masse
		Set<Order> receivedOrders = new HashSet<Order>();
		
		Set<Integer> yearsBuiltIn = new HashSet<Integer>();
		
		public void run(){
			
			//	for now, all this will do is sit here and wait until orders are almost due, and then submit them
			while(true){
				try{
				
					long currentTime = System.currentTimeMillis();
					
					if(nextOrders != -1 && currentTime + SUBMISSION_BUFFER > nextOrders && !submitted){
						submitted = true;
					
						Order[] orders = search.currentOrders().toArray(new Order[0]);

						String orderString = "SUB ";
						
						for(int i = 0; i < orders.length; i++){
							if(i != orders.length -1){
								orderString+=orders[i].toOrder(boardState)+" ";
							}else{
								orderString+=orders[i].toOrder(boardState);
							}
						}
						
						String[] tokens = orderString.split(" ");
						
						//System.out.println("Sending: "+Arrays.toString(tokens));
						
						printMsg(name, tokens);
						
						serv.send(tokens);
						
						//TODO resubmit only if they have changed
					}else{
						Thread.sleep(10);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		public void messageReceived(String[] message) {
			try{
				// Print the message
				printMsg("srv", message);
				
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
				else if(message[0].equals("HLO")){
					
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
					
					settings = new GameSettings(power, password, lvl, mtl, rtl, btl, dsd, aoa);
					search = new GameSearch(board.getPlayer(power), board, diplomaticState, beliefs);

				}
				else if(message[0].equals("NOW")){
					
					if(message[2].equals("SPR") || message[2].equals("FAL")){
						if(settings.mtl != -1){
							this.nextOrders = System.currentTimeMillis() + settings.mtl;
						}else{
							this.nextOrders = -1;
						}
					}else if(message[2].equals("SUM") || message[2].equals("AUT")){
						if(settings.rtl != -1){
							this.nextOrders = System.currentTimeMillis() + settings.rtl;
						}else{
							this.nextOrders = -1;
						}
					}else if(message[2].equals("WIN")){
						if(settings.btl != -1){
							this.nextOrders = System.currentTimeMillis() + settings.btl;
						}else{
							this.nextOrders = -1;
						}
					}else{
						throw new Exception("Unexpected");
					}

					StringBuilder move = new StringBuilder();
					for(int i = 0; i < message.length; i++){
						move.append(message[i]);
						move.append(" ");
					}
					
					//	the NOW message means all orders have been received, so go ahead
					//	and update the state
					
					boardState = board.update(Integer.parseInt(message[3]), Phase.valueOf(message[2]), boardState, receivedOrders);
					diplomaticState.update(receivedOrders);
					beliefs.update(receivedOrders);
					
					receivedOrders.clear();
					
					search.noteBoardUpdate(boardState);
					search.noteDiplomaticUpdate();
					search.noteBeliefUpdate();
					
					submitted = false;
					
				}
				else if (message[0].equals("ORD")) {

					receivedOrders.add(orderFactory.buildOrder(boardState, message));	
				}
			}catch(Exception e){
				e.printStackTrace();
				
				System.exit(0);
			}
		}
	}
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Main class for launching bot
	
	public final static int LAUNCH_BOTS = 1;
	
	public static void main(String[] args) throws InterruptedException {

		Thread[] bots = new Thread[LAUNCH_BOTS];
		for(int i = 0; i < LAUNCH_BOTS; i++){
			bots[i] = new Thread(new BotLauncher(args));
			bots[i].start();
		}
		
		while(true){
			Thread.sleep(100);
		}
	}

	public static void usage() {
		System.err.println("Usage:\n" + "  TestAI <ip> <port> <name>");
	}
	
	//TODO this is not a clean way to do this, but it is easy
	static class BotLauncher implements Runnable{
		
		String[] args;
		
		public BotLauncher(String[] args){
			this.args = args;
		}
		
		public void run(){
			try {
				Bot victoryBot = new Bot(InetAddress.getByName(args[0]), Integer
						.parseInt(args[1]), args[2]);
				while (true) {
					Thread.sleep(1000);
				}
				
			}catch (Exception ex) {
				ex.printStackTrace();
			} 
		}
	}
}

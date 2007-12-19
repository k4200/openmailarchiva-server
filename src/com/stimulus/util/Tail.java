package com.stimulus.util;

import java.io.RandomAccessFile;
import java.util.*;

public class Tail {

	
	private static boolean parseLinesFromLast(byte[] bytearray, int lineCount, ArrayList<String> lastNlines)
	{
		String lastNChars = new String (bytearray);
		StringBuffer sb = new StringBuffer(lastNChars);
		lastNChars = sb.reverse().toString();
		StringTokenizer tokens= new StringTokenizer(lastNChars,"\n");
		while(tokens.hasMoreTokens())
		{
			StringBuffer sbLine = new StringBuffer((String)tokens.nextToken());			
			lastNlines.add(sbLine.reverse().toString());
			if(lastNlines.size() == lineCount)
			{
				return true;//indicates we got 'lineCount' lines
			}
		}
		return false; //indicates didn't read 'lineCount' lines
	}
  
	public static String tail(String fileName, int lineCount, int chunkSize)
	{
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName,"r");
			ArrayList<String> lastNlines = new ArrayList<String>();			
			int delta=0;
			long curPos = raf.length() - 1;
			long fromPos;
			byte[] bytearray;
			while(true)
			{				
				fromPos = curPos - chunkSize;
				if(fromPos <= 0)
				{
					raf.seek(0);
					bytearray = new byte[(int)curPos];
					raf.readFully(bytearray);
					parseLinesFromLast(bytearray, lineCount, lastNlines);
					break;
				}
				else
				{					
					raf.seek(fromPos);
					bytearray = new byte[chunkSize];
					raf.readFully(bytearray);
					if(parseLinesFromLast(bytearray, lineCount, lastNlines))
					{
						break;
					}
					delta = ((String)lastNlines.get(lastNlines.size()-1)).length();
					lastNlines.remove(lastNlines.size()-1);
					curPos = fromPos + delta;	
				}
			}
			raf.close();
			StringBuffer output = new StringBuffer();
			for (String line : lastNlines) { 
				output.append(line);
				output.append("\n");
			}
			return output.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}	
}

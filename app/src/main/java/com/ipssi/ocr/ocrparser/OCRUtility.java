package com.ipssi.ocr.ocrparser;

import android.os.Environment;
import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRUtility {



	private static Pattern g_clean_pattern = Pattern.compile("(\\d[\\d:/\\.]*\\d)|(\\w+)");
	public static String getCleanedString(String s) {
		Matcher matcher = g_clean_pattern.matcher(s);
		StringBuilder sb = new StringBuilder();
		boolean found = false;
		while (matcher.find()) {
			if (found)
				sb.append(" ");
			sb.append(s.substring(matcher.start(), matcher.end()));
			found = true;
		}
		return sb.toString();
	}


	public static void writeStringAsFile(final String fileContents, int[] location) {

		try {
			String filename = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/"+getFilename()+".txt";
			//Toast.makeText(context, filename, Toast.LENGTH_LONG).show();
			Log.e("file-", filename);
			FileWriter fw = new FileWriter(filename, true);
			fw.write("---------------------------Start--------------------------- ");//+"x="+location[0]+",y="+location[1] + "\n");
			fw.write(fileContents + "\n\n");
			fw.close();
		} catch (IOException e) {
			Log.e("OcrDetectorProcessor", e.getMessage());
		}
	}


	public static void writeStringAsFile(String filename, final String fileContents) {

		try {
			String file = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/"+filename;
			//Toast.makeText(context, filename, Toast.LENGTH_LONG).show();
			Log.e("file-", file);
			FileWriter fw = new FileWriter(file, true);
//			fw.write("---------------------------Start--------------------------- "+"x="+location[0]+",y="+location[1] + "\n");
//			fw.write("---------------------------Start--------------------------- "+"x="+location[0]+",y="+location[1] + "\n");
			fw.write(fileContents + "\n\n");
			fw.close();
		} catch (IOException e) {
			Log.e("OcrDetectorProcessor", e.getMessage());
		}
	}
	public static String getFilename(){
		Date c = Calendar.getInstance().getTime();
		int h=c.getHours();
		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
		String formattedDate = df.format(c);
		return "OCR_"+formattedDate+"_"+h;
	}

	public static void appendLog(String tag){
		appendLog(tag,null);
	}
	public static void appendLog(String tag, String text1)
	{
//		Log.d(tag, text1==null?"":text1);
return;

		/*try {
			String filename = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/"+getFilename()+"_log.txt";
			//Toast.makeText(context, filename, Toast.LENGTH_LONG).show();
			Log.e("file-", filename);
			FileWriter fw = new FileWriter(filename, true);
			fw.write(tag+ "");
			if(text1!=null)
				fw.write(text1 + "\n");
			fw.close();
		} catch (IOException e) {
			Log.e("OcrDetectorProcessor", e.getMessage());
		}
*/
	}

	public static String getNextWord(String str, String word) {
	    String nextWord = null;
	    // to remove multi spaces with single space
	    str = str.trim().replaceAll(" +", " ");
	    int totalLength = str.length();
	    int wordStartIndex = str.indexOf(word);
	    if (wordStartIndex != -1) {
	        int startPos = wordStartIndex + word.length() + 1;
	        if (startPos < totalLength) {
	            int nextSpaceIndex = str.substring(startPos).indexOf(" ");
	            int endPos = 0;
	            if (nextSpaceIndex == -1) {
	                // we've reached end of string, no more space left
	                endPos = totalLength;
	            } else {
	                endPos = startPos + nextSpaceIndex;
	            }
	            nextWord = str.substring(startPos, endPos).trim();
	        }
	    }
	    if(nextWord!=null && !"".equals(nextWord))	
	    	nextWord=nextWord.substring(0,1).equalsIgnoreCase("|")?nextWord.substring(1):nextWord;
	    return nextWord;
	}
	
	
	public static String getPreviousWord(String str, String word) {
        String lastWord = null;
        // to remove multi spaces with single space
        if (str != null && str.length() <= word.length())
            return null;
        str = str.trim().replaceAll(" +", " ");
        int wordStartIndex = str.indexOf(word);
        str = str.substring(0, wordStartIndex).trim();
        lastWord = str.substring(str.lastIndexOf(' '), str.length()).trim();
        if (lastWord != null && !"".equals(lastWord))
            lastWord = lastWord.substring(0, 1).equalsIgnoreCase("|") ? lastWord.substring(1) : lastWord;
        return lastWord;
    }
}

package com.ipssi.ocr.ocrparser;

import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Document {
	private String name;
	private ArrayList<Pair<String, Boolean>> mustKeys;
	private ArrayList<FormField> formFields;

	
	/**
	 * @param name
	 * @param mustKeys
	 * @param formFields
	 */
	public Document(String name, ArrayList<Pair<String, Boolean>> mustKeys,
                    ArrayList<FormField> formFields) {
		super();
		this.name = name;
		this.mustKeys = mustKeys;
		this.formFields = formFields;
	}

	public ArrayList<Pair<String, Boolean>> getMustKeys() {
		return mustKeys;
	}

	public void setMustKeys(ArrayList<Pair<String, Boolean>> mustKeys) {
		for (Pair<String, Boolean> key:mustKeys)
			key.first=OCRUtility.getCleanedString(key.first);
		this.mustKeys = mustKeys;
	}

	public boolean isMustComplete() {
		return false;
	}

	public boolean isFormFieldsComplete() {
		int count=0;
		for(FormField field: formFields){
			if(field.isComplete())
				count++;
			else
				break;
		}
		if (count == formFields.size())
			return true;
		return false;
	}

	public int totalFormFieldsCompleted() {
		int count=0;
		for(FormField field: formFields)
			if(field.isComplete())
				count++;


		return count;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDocumentKeyMatch(String string){
		for (Pair<String, Boolean> key : mustKeys) {
			if (string.contains(key.first)) {
				key.second = true;
				return true;
			}
		}
			return false;
	}
	public boolean isAllDocumentMustKeysMatches(String string) {
		int count = 0;
		for (Pair<String, Boolean> key : mustKeys) {
			if (string.contains(key.first)) {
				key.second = true;
				 return true;
			}
			if (key.second)
				count++;
		}
		if (count >= (mustKeys.size()-2))
			return true;
		return false;
	}

	public ArrayList<FormField> getFormFields() {
		return formFields;
	}

	public void setFormFields(ArrayList<FormField> formFields) {
		this.formFields = formFields;
	}

	public boolean checkIfKeysInLineAndIfComplete(MyLine line) {
		boolean seenIncomplete = false;
		for (Pair<String, Boolean> key:mustKeys) {
			if (key.second)
				continue;
			key.second = line.getMatchPos(key.first, 0).pos >= 0;
			if (!key.second)
				seenIncomplete = true;
		}
		return !seenIncomplete;
	}

	public MyLine getPrevLine(int top, int bottom, int left, int right, ArrayList<MyLine> allLines, int currIndex, int maxLineToLook) {
		int prevTopSeen = top;
		int height = bottom-top;
		int fracMismatch = (int)(0.2*height);
		int linesLooked = 0;
		for (int i=currIndex-1;i>=0;i--) {
			MyLine line = allLines.get(i);
			if (line.getBottom() >= top+fracMismatch) //line on same height
				continue;
			if (line.getBottom() < prevTopSeen+fracMismatch) {
				linesLooked++;
				prevTopSeen = line.getTop();
				if (linesLooked > maxLineToLook)
					break;
			}

			//check if line has an element that overlaps currLine
			for (int j=0,js=line.getLineElem().size();j<js;j++) {
				MyLine.ElemPlusPos elem = line.getLineElem().get(j);
				Rect r = elem.getElem().getBoundingBox();
				if ((r.right >= left || left < 0) && (r.left <= right || right < 0)) {
					//matches
					return line;
				}
			}

		}
		return null;
	}
	public MyLine getNextLine(int top, int bottom, int left, int right, ArrayList<MyLine> allLines, int currIndex, int maxLineToLook) {
		int prevBottomSeen = top;
		int height = bottom-top;
		int fracMismatch = (int)(0.2*height);
		int linesLooked = 0;
		for (int i=currIndex+1,is=allLines.size();i<is;i++) {
			MyLine line = allLines.get(i);
			if (line.getTop() <= bottom-fracMismatch) //line on same height
				continue;
			if (line.getTop() > prevBottomSeen-fracMismatch) {
				linesLooked++;
				prevBottomSeen = line.getBottom();
				if (linesLooked > maxLineToLook)
					break;
			}

			//check if line has an element that overlaps currLine
			for (int j=0,js=line.getLineElem().size();j<js;j++) {
				MyLine.ElemPlusPos elem = line.getLineElem().get(j);
				Rect r = elem.getElem().getBoundingBox();
				if ((r.right >= left || left < 0) && (r.left <= right || right < 0)) {
					//matches
					return line;
				}
			}

		}
		return null;
	}
	public boolean updateFormField(MyLine currLine, ArrayList<MyLine> allLines, int currIndex, NewValReader valReader) {
		MyLine g_prevLine= OcrHelper.g_prevLine;
		//returns true all fields updated
		boolean retval = true;
		Log.d("CURRLINE",currLine.getLineString());
		for (FormField field : formFields) {
			FormFieldConfig config = field.getFormconfig();
			if (field.isComplete())
				continue;
			int nextPosToCheckFrom = 0;
			int lineSz = currLine.getLineString().length();
			boolean fieldComplete = false;

			for (; nextPosToCheckFrom < lineSz; ) {

				if (valReader != null) {
					Pair<MyLine.ElemPlusPos, Integer> elemPlusSearchRes = currLine.getElemMatchAndWhereToReadFrom(config.getToMatch(), nextPosToCheckFrom);
					if (elemPlusSearchRes == null)
						break;
					MyLine.ElemPlusPos searchRes = elemPlusSearchRes.first;
					boolean found = true;
					if (config.getUpperKey() != null && config.getUpperKey().length() > 0) {
						found = valReader.exists(searchRes,true,config.getUpperKey());
						if (false && !found) {
							found = valReader.exists(searchRes,true,config.getUpperKey());
						}


					} else if (config.getBelowKey() != null && config.getBelowKey().length() > 0) {
						found = valReader.exists(searchRes,true,config.getBelowKey());
					}
					if (!found) {
						nextPosToCheckFrom = searchRes.pos+elemPlusSearchRes.second;
						continue;
					}

					String tempOcrStr = null;
					int valueLen = config.getReadUptoLength();
					int readPattern = config.getReadPattern();
					String replaceVal = config.getReplaceExpr();//TODO
					String cleanExp = config.getReplaceExpr();
					String matchExp = config.getMatchExpr();
					// 0-UP, 1-DOWN, 2-FORWARD, 3-BACKWARD
					//readDirection;//-1 backwad, 0 full text, 1 forward
					if (readPattern == 0) {  //get from previous line
						tempOcrStr = valReader.getValueAboveBelow(searchRes, true, valueLen, cleanExp, matchExp);
					} else if (readPattern == 1) {// match upper key n value in nextLine
						tempOcrStr = valReader.getValueAboveBelow(searchRes, false, valueLen, cleanExp, matchExp);
						if (false && tempOcrStr == null) {
							tempOcrStr = valReader.getValueAboveBelow(searchRes, false, valueLen, cleanExp, matchExp);
						}
					} else if (readPattern == 2) {// 2-FORWARD
						tempOcrStr = valReader.getValueInLine(searchRes,elemPlusSearchRes.second, valueLen, cleanExp, matchExp);
						if (false && tempOcrStr == null) {
							tempOcrStr = valReader.getValueInLine(searchRes,elemPlusSearchRes.second, valueLen, cleanExp, matchExp);
						}
					}
					if (tempOcrStr != null && !"null".equalsIgnoreCase(tempOcrStr) && tempOcrStr.length() > 0) {//TODO do we need it
						boolean isValid = true;//validateType(tempOcrStr, config); //validation already done
						if (!isValid) {
							OCRUtility.appendLog("" + config.getFormFieldName() + ": ", "REJECTED Value[" + tempOcrStr + "]");
							break;
						}
						OCRUtility.appendLog("" + config.getFormFieldName() + ": ", "Add Value[" + tempOcrStr + "]");
						field.addValue(tempOcrStr);
						fieldComplete = field.isComplete();
						break;//found a match pos in the form field with appropriate value .. look at next form field
					} else {
						nextPosToCheckFrom += config.getToMatch().length();
					}
					break; //found a match pos for the the form field in the line that seems
				}//end of new approach
				else {

					MyLine.ElemPlusPos searchRes = currLine.getMatchPos(config.getToMatch(), nextPosToCheckFrom); //old approach

					if (searchRes.pos < 0)//&&  (config.getUpperKey().length()<=0 && config.getReadDirection()!=0))
						break;
					boolean found = true;
					Rect bnd = searchRes.getElem().getBoundingBox();
					int top = bnd.top;
					int bottom = bnd.bottom;
					int right = bnd.right;
					int left = bnd.left;
					int maxLinesToLook = config.getMaxLinesToLook();
					if (config.isRightLookUnbounded())
						right = -1;
					if (config.isLeftLookUnbounded())
						left = -1;
					MyLine prevLine = null;
					MyLine nextLine = null;
					if (config.getUpperKey() != null && config.getUpperKey().length() > 0) {
						if (prevLine == null)
							prevLine = getPrevLine(top, bottom, left, right, allLines, currIndex, maxLinesToLook);
						found = prevLine != null && prevLine.exists(config.getUpperKey(), searchRes.getElem());
					} else if (config.getBelowKey() != null && config.getBelowKey().length() > 0) {
						if (nextLine == null)
							nextLine = getNextLine(top, bottom, left, right, allLines, currIndex, maxLinesToLook);
						found = nextLine != null && nextLine.exists(config.getUpperKey(), searchRes.getElem());
					}

					if (!found) {
						nextPosToCheckFrom = searchRes.pos;
						continue;
					}
					if (searchRes.pos < 0)//&&  (config.getUpperKey().length()<=0 && config.getReadDirection()!=0))
						break;

					//					System.out.println("searchRes Element ["+searchRes.getElem().getValue()+"]["+searchRes.getPos()+"]");


					//string matches, upper, lower matches ... now try to read values
					String tempOcrStr = "";
					int valueLen = config.getReadUptoLength();
					int readPattern = config.getReadPattern();
					String replaceVal = config.getReplaceExpr();//TODO

					// 0-UP, 1-DOWN, 2-FORWARD, 3-BACKWARD
					//readDirection;//-1 backwad, 0 full text, 1 forward
					if (readPattern == 0) {  //get from previous line
						if (prevLine == null)
							prevLine = getPrevLine(top, bottom, left, right, allLines, currIndex, maxLinesToLook);
						if (prevLine != null)
							tempOcrStr = prevLine.getValue(searchRes.elem, -1, valueLen, config.getToMatch(), config.getReadDirection());
					} else if (readPattern == 1) {// match upper key n value in nextLine

						if (nextLine == null)
							nextLine = getNextLine(top, bottom, left, right, allLines, currIndex, maxLinesToLook);
						Log.d("nextLine", nextLine == null ? "NULL" : nextLine.getLineString());
						tempOcrStr = nextLine == null ? tempOcrStr : nextLine.getValue(searchRes.elem, -1, valueLen, config.getToMatch(), config.getReadDirection());

					} else if (readPattern == 2) {// 2-FORWARD
						tempOcrStr = currLine.getValue(null, searchRes.pos, valueLen, null, 1);

					}

					if (!"null".equalsIgnoreCase(tempOcrStr) && tempOcrStr.length() > 0) {
						boolean isValid = validateType(tempOcrStr, config);
						if (!isValid) {
							OCRUtility.appendLog("" + config.getFormFieldName() + ": ", "REJECTED Value[" + tempOcrStr + "]");
							break;
						}
						OCRUtility.appendLog("" + config.getFormFieldName() + ": ", "Add Value[" + tempOcrStr + "]");
						field.addValue(tempOcrStr);
						fieldComplete = field.isComplete();
						break;//found a match pos in the form field with appropriate value .. look at next form field
					} else {
						nextPosToCheckFrom += config.getToMatch().length();
					}
					break; //found a match pos for the the form field in the line that seems
				}//end of old approach
			}//for each whole of line
			if (!fieldComplete)
				retval = false;
		}//for each for field
		return retval;
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
//return;

		try {
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

	}

	

	private boolean validateType(String tempOcrStr, FormFieldConfig config) {
		boolean retV = false;
		//-1 alphanumeric, 0 float, 1 int,2 Alpha
		switch (config.getType()) {
			case -1:

				return (checkLengthRange(tempOcrStr,config) && tempOcrStr.matches("^[a-zA-Z0-9]*$"));
			case 0:
				return (checkLengthRange(tempOcrStr,config) && isStringFloat(tempOcrStr));
			case 1:
				return (checkLengthRange(tempOcrStr,config) &&  isStringInt(tempOcrStr));
			case 2:
				return (config.getReadUptoLength()>0?(tempOcrStr.length()==config.getReadUptoLength() && isStringLong(tempOcrStr)):isStringLong(tempOcrStr));
			case 100:
				return true;
		}
		return retV;
	}
	public boolean checkLengthRange(String tempOcrStr, FormFieldConfig config)
	{
		if(config.getReadUptoLength()>0){
			if("vehicle_no".equalsIgnoreCase(config.getFormFieldName()))
				return 	(tempOcrStr.length()>=config.getReadUptoLength()-1 && tempOcrStr.length()<=config.getReadUptoLength());
			else
				return tempOcrStr.length()==config.getReadUptoLength();
		}
		else
			return true;
	}
	public boolean isStringLong(String s)
	{
		try
		{
			Long.parseLong(s);
			return true;
		} catch (NumberFormatException ex)
		{
			return false;
		}
	}
	public boolean isStringInt(String s)
	{
		try
		{
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException ex)
		{
			return false;
		}
	}
	public boolean isStringFloat(String s)
	{
		try
		{
			Float.parseFloat(s);
			return true;
		} catch (NumberFormatException ex)
		{
			return false;
		}
	}
	public boolean isDoubleInt(double d)
	{
		//select a "tolerance range" for being an integer
		double TOLERANCE = 1E-5;
		//do not use (int)d, due to weird floating point conversions!
		return Math.abs(Math.floor(d) - d) < TOLERANCE;
	}
}

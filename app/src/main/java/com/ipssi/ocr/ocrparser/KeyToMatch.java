package com.ipssi.ocr.ocrparser;

import java.util.ArrayList;

public class KeyToMatch {
	// private String toMatch;
	private int readCount = 0;
	ArrayList<Pair<String, Boolean>> keysToMatch;
	private Document document;

	// private ArrayList<FormValue> formValues;
	public KeyToMatch(ArrayList<Pair<String, Boolean>> keysToMatch,
                      Document documentConfig) {
		this.keysToMatch = keysToMatch;
		this.document = documentConfig;
	}

	public boolean isProperRead() {
		return false;
	}
	public boolean keyInLine(MyLine line) {
		boolean retval = false;
		for (Pair<String, Boolean> en:keysToMatch) {
			if (line.getMatchPos(en.first, 0).pos >= 0) {
				en.second = true;
				readCount++;
				retval = true;
			}
		}
		return retval;
	}
	public boolean isMatch(String string) {

		for (Pair<String, Boolean> key : keysToMatch) {
			if (string.contains(key.first)) {
				key.second = true;
				readCount++;
				return true;
			}
		}

		return false;
	}

	public int getReadCount() {
		return readCount;
	}

	public void setReadCount(int readCount) {
		this.readCount = readCount;
	}

	public ArrayList<Pair<String, Boolean>> getKeysToMatch() {
		return keysToMatch;
	}

	public void setKeysToMatch(ArrayList<Pair<String, Boolean>> keysToMatch) {
		this.keysToMatch = keysToMatch;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}

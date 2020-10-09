package com.ipssi.ocr.ocrparser;

import android.graphics.RectF;

public class FormFieldConfig {

    private final String afterCompleteAction;
    private RectF fieldRect;
    private String formFieldName;
    private int type;//-1 alphanumeric, 0 float, 1 int,2 Alpha
    private String toMatch;
    private String upperKey;
    private String belowKey;
    private String replaceExpr;
    private int readPattern;// 0-UP, 1-DOWN, 2-FORWARD, 3-BACKWARD
    private int readDirection;//-1 backwad, 0 full text, 1 forward
    private int readUptoLength;
    private int valuesReadCount;
    private int bestDiffThreshold;
    private int readThreshold;
    private boolean readNextLine;
    private boolean ignoreComplete = false;
    private boolean leftLookUnbounded = false;
    private boolean rightLookUnbounded = false;
    private int maxLinesToLook = 1;
    private boolean valueIgnoreCase = false;
    private String matchExpr = null;

    public FormFieldConfig(RectF fieldRect, String formFieldName, int type, String toMatch, String upperKey,
                           String belowKey, String replaceExpr, int readPattern, int readDirection, int readUptoLength,
                           int valuesReadCount, int bestDiffThreshold, int readThreshold, boolean readNextLine,
                           boolean ignoreComplete, boolean leftLookUnbounded, boolean rightLookUnbounded, int maxLinesToLook,
                           boolean valueIgnoreCase, String matchExpr, String afterCompleteAction) {
        this.fieldRect = fieldRect;
        this.formFieldName = formFieldName;
        this.type = type;
        this.toMatch = toMatch;
        this.upperKey = upperKey;
        this.belowKey = belowKey;
        this.replaceExpr = replaceExpr;
        this.readPattern = readPattern;
        this.readDirection = readDirection;
        this.readUptoLength = readUptoLength;
        this.valuesReadCount = valuesReadCount;
        this.bestDiffThreshold = bestDiffThreshold;
        this.readThreshold = readThreshold;
        this.readNextLine = readNextLine;
        this.ignoreComplete = ignoreComplete;
        this.leftLookUnbounded = leftLookUnbounded;
        this.rightLookUnbounded = rightLookUnbounded;
        this.maxLinesToLook = maxLinesToLook;
        this.valueIgnoreCase = valueIgnoreCase;
        this.matchExpr = matchExpr;
        this.afterCompleteAction = afterCompleteAction;
    }

    public static String getMatchExprForNumeric() {
        return "[\\d\\.]+";
    }

    public boolean isLeftLookUnbounded() {
        return leftLookUnbounded;
    }

    public void setLeftLookUnbounded(boolean leftLookUnbounded) {
        this.leftLookUnbounded = leftLookUnbounded;
    }

    public boolean isRightLookUnbounded() {
        return rightLookUnbounded;
    }

    public void setRightLookUnbounded(boolean rightLookUnbounded) {
        this.rightLookUnbounded = rightLookUnbounded;
    }

    public int getMaxLinesToLook() {
        return maxLinesToLook;
    }

    public void setMaxLinesToLook(int maxLinesToLook) {
        this.maxLinesToLook = maxLinesToLook;
    }

    //	public FormFieldConfig(RectF fieldRect, String formFieldName, int type, String toMatch, String upperKey, String belowKey, String replaceExpr
//			, int readPattern, int readDirection, int readUptoLength, int valuesReadCount, int bestDiffThreshold
//			, int readThreshold, boolean readNextLine, boolean ignoreComplete, String matchExpr) {
//		this.fieldRect = fieldRect;
//		this.formFieldName = formFieldName;
//		this.type = type;
//		this.toMatch = toMatch;
//		this.upperKey = upperKey;
//		this.belowKey = belowKey;
//		this.replaceExpr = replaceExpr;
//		this.readPattern = readPattern;
//		this.readDirection = readDirection;
//		this.readUptoLength = readUptoLength;
//		this.valuesReadCount = valuesReadCount;
//		this.bestDiffThreshold = bestDiffThreshold;
//		this.readThreshold = readThreshold;
//		this.readNextLine = readNextLine;
//		this.ignoreComplete = ignoreComplete;
//		this.matchExpr = matchExpr;
//	}
    public String getMatchExpr() {
        return matchExpr;
    }

    public void setMatchExpr(String matchExpr) {
        this.matchExpr = matchExpr;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getReadDirection() {
        return readDirection;
    }

    public void setReadDirection(int readDirection) {
        this.readDirection = readDirection;
    }

    public boolean isReadNextLine() {
        return readNextLine;
    }

    public void setReadNextLine(boolean readNextLine) {
        this.readNextLine = readNextLine;
    }

    @Override
    public String toString() {
        return
//				"'prevLine':'"+ prevLine+
//		"','nextLine':'"+ nextLine+
                "'formFieldName':'" + formFieldName +
                        "','toMatch':'" + toMatch +
                        "','upperKey':'" + upperKey +
                        "','belowKey':'" + belowKey +
                        "','replaceExpr':'" + replaceExpr +
                        "','readPattern':'" + readPattern +
                        "','readUptoLength':'" + readUptoLength +
                        "','valuesReadCount':'" + valuesReadCount +
                        "','bestDiffThreshold':'" + bestDiffThreshold +
                        "','readThreshold':'" + readThreshold + "'";
    }


    public String getFormFieldName() {
        return formFieldName;
    }

    public void setFormFieldName(String formFieldName) {
        this.formFieldName = formFieldName;
    }

    public String getToMatch() {
        return toMatch;
    }

    public void setToMatch(String toMatch) {
        this.toMatch = OCRUtility.getCleanedString(toMatch);
    }

    public String getUpperKey() {
        return upperKey;
    }

    public void setUpperKey(String upperKey) {
        this.upperKey = upperKey;
    }

    public String getBelowKey() {
        return belowKey;
    }

    public void setBelowKey(String belowKey) {
        this.belowKey = belowKey;
    }

    public String getReplaceExpr() {
        return replaceExpr;
    }

    public void setReplaceExpr(String replaceExpr) {
        this.replaceExpr = replaceExpr;
    }

    public int getReadPattern() {
        return readPattern;
    }

    public void setReadPattern(int readPattern) {
        this.readPattern = readPattern;
    }

    public int getReadUptoLength() {
        return readUptoLength;
    }

    public void setReadUptoLength(int readUptoLength) {
        this.readUptoLength = readUptoLength;
    }

    public int getValuesReadCount() {
        return valuesReadCount;
    }

    public void setValuesReadCount(int valuesReadCount) {
        this.valuesReadCount = valuesReadCount;
    }

    public int getBestDiffThreshold() {
        return bestDiffThreshold;
    }

    public void setBestDiffThreshold(int bestDiffThreshold) {
        this.bestDiffThreshold = bestDiffThreshold;
    }

    public int getReadThreshold() {
        return readThreshold;
    }

    public void setReadThreshold(int readThreshold) {
        this.readThreshold = readThreshold;
    }

    public boolean isIgnoreComplete() {
        return ignoreComplete;
    }

    public void setIgnoreComplete(boolean ignoreComplete) {
        this.ignoreComplete = ignoreComplete;
    }

    public RectF getFieldRect() {
        return fieldRect;
    }

    public void setFieldRect(RectF fieldRect) {
        this.fieldRect = fieldRect;
    }

    public String getAfterCompleteAction() {
        return afterCompleteAction;
    }

    public boolean isValueIgnoreCase() {
        return valueIgnoreCase;
    }

    public void setValueIgnoreCase(boolean valueIgnoreCase) {
        this.valueIgnoreCase = valueIgnoreCase;
    }
}

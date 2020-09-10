package com.ipssi.ocr.ocrparser;
import android.graphics.Rect;
import android.util.Log;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gnu.trove.TIntProcedure;

public class NewValReader {
    public final static int LOG_LEVEL = 0; //0 - no log, 1 = final result of ask of next element etc, 2 trace
    public final static int RELATIVE_ONLY_OVERLAP = 0;
    public final static int RELATIVE_RIGHT_EDGE_ELEM = 1;
    public final static int RELATIVE_LEFT_EDGE_ELEM = 2;
    public final static int RELATIVE_RIGHT_EDGE_LINE = 3;
    public final static int UP = 0;
    public final static int DOWN = 1;
    public final static int RIGHT = 2;


    public class PrintId implements TIntProcedure {
        private List arrayOfIds = new ArrayList() ;
        public boolean execute(int val) {
            arrayOfIds.add(val);
      //      System.out.println("Exe:"+val);
            return true;
        }
        public boolean visit(int val) {
            arrayOfIds.add(val);
        //    System.out.println("Visit:"+val);
            return true;
        }
        public List getArrayOfIds() {
            return arrayOfIds;
        }
        public void cleanArrayOfIds() {
            arrayOfIds.clear();
        }
    }

    RTree rtree = null;
    ArrayList<MyLine> allLines = null;
    ArrayList<Pair<Integer, Integer>> elemIndexToInfo = new ArrayList<Pair<Integer, Integer>>();
    Rectangle rtreebound = null;
    //int gapBetweenLinesAvg = 10;
    //int lineHeightAvg = 25;
    public static int getOverlapErrorThresh(Rect refBox, Rect box) {
        int minHeight = box == null ? refBox.bottom-refBox.top : Math.min(refBox.bottom-refBox.top, box.bottom-box.top);
        int buffer = (int)(minHeight/4);
        return buffer;
    }
    public static boolean isSimilarLeft(Rect refBox, Rect box) {
        int minwidth = Math.min(refBox.right-refBox.left, box.right-box.left);
        return Math.abs(refBox.left-box.left) <= minwidth/4;
    }
    public static boolean isProbablySameLine(Rect refBox, Rect box) {
        int buffer = getOverlapErrorThresh(refBox, box);
        return !(box.bottom <= refBox.top-buffer) && !(box.bottom <= refBox.top-buffer);
    }
    public static boolean isDefinitelySameLine(Rect refBox, Rect box) {
        if (box.bottom > refBox.top && box.top < refBox.bottom) {
            int minH = Math.min(refBox.bottom-refBox.top, box.bottom-box.top);
            int overlap = 0;
            if (box.top <= refBox.top) {
                if (box.bottom >= refBox.bottom)
                    overlap = refBox.bottom-refBox.top;
                else
                    overlap = box.bottom-refBox.top;
            }
            else {
                if (box.bottom >= refBox.bottom)
                    overlap = refBox.bottom-box.top;
                else
                    overlap = box.bottom-box.top;
            }
            return overlap >= 0.66*minH;
        }
        return false;
    }
    public static boolean isProbablyAboveLine(Rect refBox, Rect box) {
        int buffer = getOverlapErrorThresh(refBox, box);
        return (box.bottom <= refBox.top+buffer);
    }
    public static boolean isProbablyBelowLine(Rect refBox, Rect box) {
        int buffer = getOverlapErrorThresh(refBox, box);
        return (box.top >= refBox.bottom-buffer);
    }

    public static int getBoxVertAlignmentDontUse(Rect refBox, Rect box) {//0 - same line, -1 above, 1 below
        int buffer = getOverlapErrorThresh(refBox, box);
        if (box.bottom <= refBox.top+buffer)
            return -1;
        else if (box.top >= refBox.bottom-buffer)
            return 1;
        else
            return 0;
    }
    public static int getBoxHorizAlignment(Rect refBox, Rect box) {//-1 to left , 1 to right, 0 overlapping
        int buffer = getOverlapErrorThresh(refBox, box);
        if (box.right <= refBox.left+buffer)
            return -1;
        else if (box.left >= refBox.right-buffer)
            return 1;
        else
            return 0;
    }

    public static NewValReader getNewValReader(ArrayList<MyLine> allLines) {
        NewValReader retval = new NewValReader();
        Properties properties = new Properties();
        properties.put("MinNodeEntries", 5);
        properties.put("MaxNodeEntries", 10);
        retval.allLines = allLines;
        retval.rtree = new RTree();
        retval.rtree.init(properties);
        for (int i=0,is=allLines.size();i<is;i++) {
            MyLine line = allLines.get(i);
            ArrayList<MyLine.ElemPlusPos> elemList = line.getLineElem();
            for (int j=0,js=elemList.size();j<js;j++) {
                MyLine.ElemPlusPos elem = elemList.get(j);
                retval.elemIndexToInfo.add(new Pair<Integer, Integer>(i,j));
                int pos = retval.elemIndexToInfo.size()-1;
                elem.setLookupIndexInNewValReader(pos);
                Rectangle rectangle = new Rectangle();
                Rect r = elem.getElem().getBoundingBox();
                rectangle.set(r.left, -r.top, r.right,-r.bottom);//flipping to cartersian - not sure if rtree works
                Log.d("RTREE",elem.toString()+" r(LTRB):"+rectangle.minX+","+rectangle.minY+","+rectangle.maxX+","+rectangle.maxY);
                retval.rtree.add(rectangle, pos);
            }
        }
        retval.rtreebound = retval.rtree.getBounds();
        return retval;
    }

    public String getValueAboveBelow(MyLine.ElemPlusPos refElem, boolean above, int valueLen, String cleanExp, String matchExp) {
        MyLine.ElemPlusPos aboveOrBelowElem = this.getFirstElemAboveOrBelow(refElem, above, NewValReader.RELATIVE_ONLY_OVERLAP, 4, matchExp);
        if (LOG_LEVEL > 0) {
            Log.d("ValueA/B", "A?:"+above+" Ask:"+refElem.toString()+" A/B Elem:"+(aboveOrBelowElem == null ? "null"
                    :(aboveOrBelowElem.toString()+" Idx:"+this.elemIndexToInfo.get(aboveOrBelowElem.getLookupIndexInNewValReader()).first
                                                      +","+this.elemIndexToInfo.get(aboveOrBelowElem.getLookupIndexInNewValReader()).second)
            ));
        }
        if (aboveOrBelowElem != null) {
            String v = getValueInLine(aboveOrBelowElem, 0, valueLen, cleanExp, matchExp);
            if (LOG_LEVEL > 0) {
                Log.d("ValueA/B Ret", v);
            }
            return v;
        }
        if (LOG_LEVEL > 0)
            Log.d("ValueA/B Ret","null");
        return null;
    }
    public boolean exists(MyLine.ElemPlusPos refElem, boolean above, String matchString) {
        MyLine.ElemPlusPos firstRtElem = this.getFirstElemOnRightInLine(refElem, "[a-zA-Z0-9]");
        if (firstRtElem == null)
            firstRtElem = refElem;
        MyLine.ElemPlusPos aboveOrBelowElem = this.getFirstElemAboveOrBelow(firstRtElem, above, NewValReader.RELATIVE_LEFT_EDGE_ELEM, 2, null);
        if (LOG_LEVEL > 0) {
            Log.d("ExistsA/B", "A?:"+above+" Ask:"+refElem.toString()+" A/B Elem:"+(aboveOrBelowElem == null ? "null"
                    :(
                            aboveOrBelowElem.toString()+" Idx:"+this.elemIndexToInfo.get(aboveOrBelowElem.getLookupIndexInNewValReader()).first
                                    +","+this.elemIndexToInfo.get(aboveOrBelowElem.getLookupIndexInNewValReader()).second)
            ));
        }
        if (aboveOrBelowElem != null) {
            //clean Exp: replace multiple spaces with
            String s = getValueInLine(aboveOrBelowElem, 0, -1, null, null);
            s = MyLine.cleanupString(s);
            if (LOG_LEVEL > 0) {
                Log.d("ExistsA/B VinLine", s);
            }

            if (s != null)
                return s.indexOf(matchString) >= 0;
        }
        return false;
    }

    public String getValueInLine(MyLine.ElemPlusPos refElem, int strPosAfterOrIncl, int valueLen, String cleanExp, String matchExp) {
        StringBuilder sb = new StringBuilder();
        int refIndex = refElem.getLookupIndexInNewValReader();
        Pair<Integer, Integer> refLookup = this.elemIndexToInfo.get(refIndex);
        MyLine refLine = allLines.get(refLookup.first);
        boolean toContinueReading = true;
        boolean mergeWithSpace = cleanExp == null || " ".replaceAll(cleanExp,"").length() != 0;
        if (strPosAfterOrIncl>=0) {
            String meVal = refElem.getElem().getValue();
            if (meVal.length() > strPosAfterOrIncl) {
                toContinueReading = helperAddToValue(sb, meVal.substring(strPosAfterOrIncl), valueLen, cleanExp, mergeWithSpace);
            }
        }
        if (toContinueReading) {
            toContinueReading = helperAddToRestOfLine(sb, refLine, refLookup.second+1,valueLen, cleanExp, mergeWithSpace);
        }
        if (LOG_LEVEL > 0) {
            Log.d("GetValue","RefElem:"+refElem.toString()+" Idx:"+refLookup.first+","+refLookup.second+" valueLen:"+valueLen+" In currLine:"+sb+" toCont?"+toContinueReading);
        }
        if (toContinueReading) {
            for (MyLine.ElemPlusPos rtElem = getFirstElemOnRightInLine(refElem, null); toContinueReading && rtElem != null; rtElem = getFirstElemOnRightInLine(rtElem, null)) {
                refIndex = rtElem.getLookupIndexInNewValReader();
                refLookup = this.elemIndexToInfo.get(refIndex);
                refLine = allLines.get(refLookup.first);
                toContinueReading = helperAddToRestOfLine(sb, refLine, refLookup.second, valueLen, cleanExp, mergeWithSpace);
                if (LOG_LEVEL > 0) {
                    Log.d("GetValueNextElem","RtElem:"+rtElem.toString()+" Idx:"+refLookup.first+","+refLookup.second+" valueLen:"+valueLen+" In currLine:"+sb+" toCont?"+toContinueReading);
                }
            }
        }
        return getMatchingString(sb, matchExp);
    }
    private String getMatchingString(StringBuilder sb, String matchExp) {
        if (matchExp == null || matchExp.length() == 0)
            return sb.toString();
        Pattern pattern = Pattern.compile(matchExp);
        Matcher matcher = pattern.matcher(sb);
        if (matcher.find()) {
            return sb.substring(matcher.start(), matcher.end());
        }
        else {
            return null;
        }
    }
    private MyLine.ElemPlusPos getFirstElemOnRightInLine(MyLine.ElemPlusPos refElem, String regExp) {
        int refIndex = refElem.getLookupIndexInNewValReader();
        Pair<Integer, Integer> refLookup = this.elemIndexToInfo.get(refIndex);
        MyLine refLine = allLines.get(refLookup.first);

        Rect lastRect = refLine.getLastRect();
        int buffer = getOverlapErrorThresh(lastRect, null);

        float desiredTop = (lastRect.top-buffer);
        float desiredLeft = lastRect.right+buffer;
        float desiredBottom = (lastRect.bottom+buffer);
        float desiredRight = rtreebound.maxX;

        Rectangle lookInThis = new Rectangle(desiredLeft, -desiredTop, desiredRight, -desiredBottom);
        PrintId pid=new PrintId();
        rtree.intersects(lookInThis, pid);
       // rtree.contains(lookInThis, pid);
        List<Integer> idsIntersecting = pid.getArrayOfIds();
        MyLine.ElemPlusPos match = null;

        Rect matchRect = null;

        if (LOG_LEVEL > 1) {
            Log.d("FirstRight"," Ref:"+refElem.toString()+" lastRect(LTBR):["+lastRect.left+","+lastRect.top+","+lastRect.bottom+","+lastRect.right+"]"
            +" RtreeRect(LTRB):"+lookInThis.minX+","+lookInThis.minY+","+lookInThis.maxX+","+lookInThis.maxX);
        }
        Pattern pattern = regExp == null ? null : Pattern.compile(regExp);
        for (Integer idx: idsIntersecting) {
            Pair<Integer, Integer> lookup = this.elemIndexToInfo.get(idx);
            MyLine line = allLines.get(lookup.first);
            MyLine.ElemPlusPos elem = line.getLineElem().get(lookup.second);
            Rect meRect = elem.getElem().getBoundingBox();
            int horizAlign =  NewValReader.getBoxHorizAlignment(lastRect, meRect);
            boolean isVertOK = NewValReader.isProbablySameLine(lastRect, meRect);

            if (LOG_LEVEL > 1) {
                Pair<Integer, Integer> dbgLookup = this.elemIndexToInfo.get(elem.getLookupIndexInNewValReader());
                StringBuilder dbg = new StringBuilder();
                dbg.append(" ElemCheck:").append(elem.toString()).append(" Idx:").append(dbgLookup.first).append(",").append(dbgLookup.second)
                        .append(" Align(HV):").append(horizAlign).append(",").append(isVertOK);
                Log.d("FirstRight",dbg.toString());
            }
            if (horizAlign != 1)
                continue;
            if (!isVertOK)
                continue;
            boolean matches = true;
            boolean isSimilarLfet = match != null && !NewValReader.isDefinitelySameLine(matchRect, meRect)
                    && isSimilarLeft(matchRect, meRect);
            if (match == null ||
                    (isSimilarLfet && meRect.top < matchRect.top)
            || (!isSimilarLfet && meRect.left < matchRect.left)
                    ) {
                if (pattern != null) {
                    //check if matches
                    String str = elem.getElem().getValue().trim();

                    Matcher matcher = pattern.matcher(str);
                    if (!matcher.find() || matcher.start() != 0)
                        matches = false;
                    //if (!str.matches(regExp))
                    //    matches = false;
                }
                if (matches) {
                    match = elem;
                    matchRect = meRect;
                }
                if (LOG_LEVEL > 1)
                    Log.d("FirstRight"," Is On left till now best regexpMatch?"+matches);
            }
        }
        return match;
    }

    private MyLine.ElemPlusPos getFirstElemAboveOrBelow(MyLine.ElemPlusPos refElem, boolean above, int relativeEdge, int approxLineGap, String regExp) {
        int refIndex = refElem.getLookupIndexInNewValReader();
        Pair<Integer, Integer> refLookup = this.elemIndexToInfo.get(refIndex);
        MyLine refLine = allLines.get(refLookup.first);
        Rect refRect = refElem.getElem().getBoundingBox();


        float desiredTop = 0;
        float desiredLeft = 0;
        float desiredBottom = 0;
        float desiredRight = 0;
        switch (relativeEdge) {

            case RELATIVE_RIGHT_EDGE_ELEM: {
                desiredLeft = refRect.right;
                desiredRight = rtreebound.maxX;
                break;
            }
            case RELATIVE_LEFT_EDGE_ELEM : {
                desiredLeft = refRect.left;
                desiredRight =  rtreebound.maxX;
                break;
            }
            case RELATIVE_RIGHT_EDGE_LINE : {

                desiredLeft = refLine.getLastRect().right;
                desiredRight =  rtreebound.maxX;
                break;
            }
            default : {
                desiredLeft = refRect.left;
                desiredRight = refRect.right;
                break;
            }
        }
        int buffer = NewValReader.getOverlapErrorThresh(refRect, null);
        if (approxLineGap <= 0) {//unbounded on top
            if (above) {
                desiredBottom = (refRect.top+buffer);
                desiredTop = -rtreebound.maxY; //top is higher Y in rtree
            }
            else {
                desiredTop = (refRect.bottom-buffer);
                desiredBottom = -rtreebound.minY; //top is higher Y in rtree
            }
        }
        else {
            int approxHeight = (int)(approxLineGap*(refRect.bottom-refRect.top)*3);//safety
            if (above) {
                desiredBottom = (refRect.top+buffer);
                desiredTop = (desiredBottom - approxHeight);
            }
            else {
                desiredTop = (refRect.bottom-buffer);
                desiredBottom = (desiredTop+approxHeight);
            }
        }
        Rectangle lookInThis = new Rectangle(desiredLeft, -desiredTop, desiredRight, -desiredBottom);
        if (LOG_LEVEL > 1) {
            Log.d("FirstAboveBelow"," Ref:"+refElem.toString()+" refRect(LTBR):["+refRect.left+","+refRect.top+","+refRect.bottom+","+refRect.right+"]"
                    +" RtreeRect(LTRB):"+lookInThis.minX+","+lookInThis.minY+","+lookInThis.maxX+","+lookInThis.maxY);
        }

        PrintId pid=new PrintId();
        rtree.intersects(lookInThis, pid);
        //rtree.contains(lookInThis, pid);
        List<Integer> idsIntersecting = pid.getArrayOfIds();
        MyLine.ElemPlusPos match = null;
        Rect matchRect = null;
        MyLine.ElemPlusPos matchFirstElem = null;
        Rect matchFirstRect = null;

        Pattern pattern = regExp == null ? null : Pattern.compile(regExp);
        for (Integer idx: idsIntersecting) {
            Pair<Integer, Integer> lookup = this.elemIndexToInfo.get(idx);
            MyLine line = allLines.get(lookup.first);
            MyLine.ElemPlusPos elem = line.getLineElem().get(lookup.second);

            MyLine.ElemPlusPos firstElem = relativeEdge == NewValReader.RELATIVE_ONLY_OVERLAP ? elem : line.getLineElem().get(0);
            //boolean isVertOK = above ? NewValReader.isProbablyAboveLine(refRect, firstElem.getElem().getBoundingBox())
            //            : NewValReader.isProbablyBelowLine(refRect, firstElem.getElem().getBoundingBox())
            //        ;
            Rect mefirstRect = firstElem.getElem().getBoundingBox();
            Rect meRect = elem.getLookupIndexInNewValReader() == firstElem.getLookupIndexInNewValReader() ? mefirstRect : elem.getElem().getBoundingBox();
            int vertAlign = getBoxVertAlignmentDontUse(refRect, mefirstRect);
            boolean isVertOK = above ? vertAlign == -1 : vertAlign == 1;
            int horizAlign = getBoxHorizAlignment(refRect, mefirstRect);
            if (LOG_LEVEL > 1) {
                Pair<Integer, Integer> dbgLookup = this.elemIndexToInfo.get(elem.getLookupIndexInNewValReader());
                StringBuilder dbg = new StringBuilder();
                dbg.append(" ElemCheck:").append(elem.toString()).append(" Idx:").append(dbgLookup.first).append(",").append(dbgLookup.second)
                        .append(" HVAlign:").append(horizAlign).append(",").append(isVertOK)
                .append(" FirstElem:").append(firstElem.toString());
                Log.d("FirstAboveBelow",dbg.toString());
            }
            if (!isVertOK)
                continue;
            if (relativeEdge == NewValReader.RELATIVE_ONLY_OVERLAP && horizAlign != 0)
                continue;
            boolean matches = match == null;
            if (!matches) {//check if the best and this one are on same line
                if (matchFirstElem.getLookupIndexInNewValReader() == firstElem.getLookupIndexInNewValReader()) {
                    //on same line ... get the one that is on left
                    matches = matchRect.left > meRect.left;
                }
                else {
                    boolean candVertAligned = NewValReader.isProbablySameLine(matchFirstRect, mefirstRect);
                    if (candVertAligned)
                        matches = matchRect.left > meRect.left;
                    else {
                        matches = (above && mefirstRect.top > matchFirstRect.top)
                                || (!above && mefirstRect.top < matchFirstRect.top)
                                ;
                    }
                }
            }
            if (matches) {
                if (pattern != null) {
                    //check if matches
                    String str = elem.getElem().getValue().trim();

                    Matcher matcher = pattern.matcher(str);
                    if (!matcher.find() || matcher.start() != 0)
                        matches = false;
                    //if (!str.matches(regExp))
                    //    matches = false;
                }
                if (matches) {
                    match = elem;
                    matchFirstElem = firstElem;
                    matchRect = meRect;
                    matchFirstRect = mefirstRect;
                }
                if (LOG_LEVEL > 1)
                    Log.d("FirstAboveBelow"," Is Approp and better till now best regexpMatch?"+matches);

            }
        }
        return match;
    }

    private static boolean helperAddToValue(StringBuilder sb, String s, int valueLen, String cleanExp, boolean mergeWordsWithSpace) {
        boolean toContinueReading = true;
        String parts[] = s.split(" ");
        for (int j=0,js=parts.length;toContinueReading && j<js;j++) {
            if (parts[j] == null)
                continue;
            if (cleanExp != null && cleanExp.length() > 0)
                parts[j] = parts[j].replaceAll(cleanExp,"");
            parts[j] = parts[j].trim();
            if (parts[j].length() == 0)
                continue;
            if (sb.length() > 0 && mergeWordsWithSpace)
                sb.append(" ");
            sb.append(parts[j]);
            toContinueReading = valueLen == -1 || (valueLen > sb.length()) || sb.length() == 0;
        }
        return toContinueReading;
    }
    private static boolean helperAddToRestOfLine(StringBuilder sb, MyLine refLine, int inclThisPos, int valueLen, String cleanExp, boolean mergeWithSpace) {
        boolean toContinueReading = true;
        for (int i=inclThisPos,is=refLine.getLineElem().size();toContinueReading && i<is;i++)
            toContinueReading = helperAddToValue(sb, refLine.getLineElem().get(i).getElem().getValue(), valueLen, cleanExp, mergeWithSpace);
        return toContinueReading;
    }


}

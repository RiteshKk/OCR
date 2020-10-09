package com.ipssi.ocr.ocrparser;

import android.graphics.Rect;

import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyLine  implements Comparable<MyLine> {
    @Override
    public int compareTo(MyLine myLine) {
        if (top == myLine.getTop())
            return 0;
        else if (top > myLine.getTop())
            return 1;
        else
            return -1;
    }

    public static class ElemPlusPos {
        int pos;
        Element elem;
        int lookupIndexInNewValReader = -1;
        public String toString() {
            Rect r = elem.getBoundingBox();
            return elem.getValue()+"["+r.left+","+","+r.top+","+r.bottom+","+r.right+"]"+" Pos:"+lookupIndexInNewValReader;
        }
        public int getLookupIndexInNewValReader() {
            return lookupIndexInNewValReader;
        }
        public void setLookupIndexInNewValReader(int val) {
            this.lookupIndexInNewValReader = val;
        }
        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        public Element getElem() {
            return elem;
        }

        public void setElem(Element elem) {
            this.elem = elem;
        }

        public ElemPlusPos(Element e, int len) {
            this.pos = len;
            this.elem = e;
        }
    }
    private int top;
    private int bottom;
    private double lineSlope = 0;
    private String lineString = null;
    private Line line =null;
    ArrayList<ElemPlusPos> lineElem = null;
    public MyLine(String v, ArrayList<ElemPlusPos> list, Line line) {
        this.lineElem = list;
        this.lineString = v;
        this.line=line;
        this.top= line.getBoundingBox().top;
        this.bottom = line.getBoundingBox().bottom;
    }
    public void setLineSlope(double slope) {
        this.lineSlope = slope;
    }
    public double getLineSlope() {
        return this.lineSlope;
    }
    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }
    public int getBottom() {
        return bottom;
    }
    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public String getLineString() {
        return lineString;
    }

    public void setLineString(String lineString) {
        this.lineString = lineString;
    }

    public ArrayList<ElemPlusPos> getLineElem() {
        return lineElem;
    }

    public void setLineElem(ArrayList<ElemPlusPos> lineElem) {
        this.lineElem = lineElem;
    }
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

    public static String cleanupString(String s) {
        return s == null ? null : s.replaceAll("\\W+"," ").trim();
    }
    public static MyLine getLine(Line line) {
        ArrayList<ElemPlusPos> elemList = new ArrayList<ElemPlusPos>();
        List<Element> words = (List<Element>) line.getComponents();
        StringBuilder sb = new StringBuilder();
        for (int i=0,is=words.size();i<is;i++) {
            String b=words.get(i).getValue();
            String v ="";
            if(sb.toString().contains("Invoice No") || sb.toString().contains("GST Bill"))
                v=words.get(i).getValue();
            else
             v = getCleanedString(words.get(i).getValue());
            if (v == null || v.length() == 0)
                continue;
            if (sb.length() != 0)
                sb.append(" ");
            if (sb.length() ==1 ) {
                v = getCleanedString(words.get(i).getValue());
            }
            elemList.add(new ElemPlusPos(words.get(i), sb.length()));
            sb.append(v);
        }
        return new MyLine(sb.toString(), elemList,line);
    }

    public boolean exists(String s, Element refElem) {
        int lookFrom = 0;
        if (refElem != null) {
            Rect refRect = refElem.getBoundingBox();
//            System.out.println("Search Rect- ["+refRect+"]");
            for (int i = 0, is = lineElem.size(); i < is; i++) {
                Element e = lineElem.get(i).elem;
                Rect erect = e.getBoundingBox();
//                System.out.println("E- ["+e.getValue()+"] pos ["+lineElem.get(i).getPos()+"] rect["+erect+"] condition["+ (erect.right < refRect.left)+"]");
                if (erect.right < refRect.left)
                //if (erect.right >= refRect.left)
                    continue;
                lookFrom = lineElem.get(i).pos;
            }
//            System.out.println("exists lookFrom ["+lookFrom+"]["+s+"] in ["+lineString+"]");
        }
        return lineString.indexOf(s, 0) >= 0;
    }
    public ElemPlusPos getMatchPos(String s, int lookFrom) {
        //s assumed to be cleaned up with same pattern
        int pos = this.lineString.toLowerCase(Locale.US).indexOf(s.toLowerCase(Locale.US), lookFrom);
        if (pos < 0)
            return new ElemPlusPos(null, -1);
        Element e = null;
        for (int i=0,is=lineElem.size();i<is;i++) {
            if (lineElem.get(i).pos <= pos && (i == is-1 || lineElem.get(i+1).pos > pos)) {
                e = lineElem.get(i).elem;
                break;
            }
        }
        return new ElemPlusPos(e, pos+s.length());
    }
    public Pair<ElemPlusPos, Integer> getElemMatchAndWhereToReadFrom(String s, int lookFrom) {
        //s assumed to be cleaned up with same pattern
        int pos = this.lineString.toLowerCase(Locale.US).indexOf(s.toLowerCase(Locale.US), lookFrom);
        if (pos < 0)
            return null;

        pos += s.length();
        for (int i=0,is=lineElem.size();i<is;i++) {
            if (lineElem.get(i).pos <= pos && (i == is-1 || lineElem.get(i+1).pos > pos)) {
                ElemPlusPos item = lineElem.get(i);
                int inThisLookForValAfter = pos-item.getPos();
                return new Pair<ElemPlusPos, Integer>(item, inThisLookForValAfter);
            }
        }
        return null;
    }

    public String getValue(Element refElem, int strPosAfterOrIncl, int valueLen, String keyToMatch, int readDirection) {
        //only one of refElem of strPosAfterOrIncl is valid
        //if refElem is null then will look in line from position of refElem to right
        // else will look from strPosAfterOrIncl in the string for the line

        if (refElem != null) {
            Rect refRect = refElem.getBoundingBox();

            for (int i = 0, is = lineElem.size(); i < is; i++) {
                Element e = lineElem.get(i).elem;
                Rect erect = e.getBoundingBox();
                if (erect.right < refRect.left)
                    continue;
                strPosAfterOrIncl = lineElem.get(i).pos;
            }
        }
        if (readDirection == -1) {
            readDirection = -1;
        }
        if (strPosAfterOrIncl >= 0 && strPosAfterOrIncl < lineString.length() && lineString.charAt(strPosAfterOrIncl) == ' ')
            strPosAfterOrIncl++;
        if (valueLen == -1) {
            if (keyToMatch != null && keyToMatch.length() < strPosAfterOrIncl)
                strPosAfterOrIncl = keyToMatch.length();

            if (readDirection == -1)
                return lineString.substring(0, lineString.length() - keyToMatch.length());
            else if (readDirection == 0)
                return lineString;

            return (strPosAfterOrIncl<0 || strPosAfterOrIncl>lineString.length())?"":lineString.substring(strPosAfterOrIncl);
        }
        else {
            int nextSpace = lineString.indexOf(" ", strPosAfterOrIncl);//+
            if (nextSpace < 0 )
                return lineString.substring(strPosAfterOrIncl>=0?strPosAfterOrIncl:0);
            else if(strPosAfterOrIncl>=0)
                return lineString.substring(strPosAfterOrIncl,nextSpace);
            else
                return "";
        }
    }
    public Rect getFirstRect() {
        return this.lineElem.get(0).getElem().getBoundingBox();
    }
    public Rect getLastRect() {
        return this.lineElem.get(lineElem.size()-1).getElem().getBoundingBox();
    }

}

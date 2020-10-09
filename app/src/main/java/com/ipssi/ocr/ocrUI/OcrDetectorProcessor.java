/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipssi.ocr.ocrUI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.ipssi.ocr.C;
import com.ipssi.ocr.ManualEntryActivity;
import com.ipssi.ocr.camera.GraphicOverlay;
import com.ipssi.ocr.ocrparser.Document;
import com.ipssi.ocr.ocrparser.MyLine;
import com.ipssi.ocr.ocrparser.NewValReader;
import com.ipssi.ocr.ocrparser.OCRUtility;
import com.ipssi.ocr.ocrparser.OcrHelper;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrFormGraphic> graphicOverlay;
    private Context context;

    OcrDetectorProcessor(GraphicOverlay<OcrFormGraphic> ocrGraphicOverlay, Context ocrContext) {
        graphicOverlay = ocrGraphicOverlay;
        context=ocrContext;
//        createOCRFormGraphics();
    }



    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */

    public void receiveDetections_old(Detector.Detections<TextBlock> detections) {
        graphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();

        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item == null)
                continue;
            Log.e("OcrDetectorProcessor", "Text detected! " + item.getValue()+"\n\n\n");
            List<Line> lineList = (List<Line>) item.getComponents();
            MyLine currLine=null;
            MyLine prevLine=null;
            MyLine nextLine=null;
            for (int j=0,js=lineList.size(); j<js; j++) {
                if (currLine == null)
                    currLine = MyLine.getLine(lineList.get(j));
                if (prevLine == null && j != 0)
                    prevLine = MyLine.getLine(lineList.get(j-1));
                if (nextLine == null && j != js-1)
                    nextLine = MyLine.getLine(lineList.get(j+1));

//                if(OcrHelper.process(currLine, prevLine, nextLine)){
//                    OCRUtility.appendLog("OcrDetectorProcessor::","FORM IS COMPLETE ..... STOP PROCESSING.....");
//                    release();
//                    Intent intent = new Intent(context, NavigationActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(intent);
//                }

                prevLine = currLine;
                currLine = nextLine;

                nextLine = null;
            }



//
//            int[] location = new int[2];
//            OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
//            graphicOverlay.add(graphic);
//            graphicOverlay.getLocationOnScreen(location);

//          writeStringAsFile(item.getValue(),null);



           /* if (item != null && item.getValue() != null) {
                Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
                int[] location = new int[2];
                 OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);

                graphicOverlay.add(graphic);
                graphicOverlay.getLocationOnScreen(location);
                if(OcrHelper.process(item)){
                    OCRUtility.appendLog("OcrDetectorProcessor::","FORM IS COMPLETE ..... STOP PROCESSING.....");
                    release();
                    Intent intent = new Intent(context, NavigationActivity.class);
                    context.startActivity(intent);
                }

                writeStringAsFile(item.getValue(),location);
            }*/

         /*   if (item != null && item.getValue() != null) {

                List<Line> lineList = (List<Line>) item.getComponents();
                for (int j = 0; j <lineList.size(); j++) {
                    Line line=lineList.get(i);
                    //OCRUtility.writeStringAsFile(OCRUtility.getFilename()+"_line.txt",line.getValue());
                    if(OcrHelper.process(line.getValue())){
                        OCRUtility.appendLog("OcrDetectorProcessor::","FORM IS COMPLETE ..... STOP PROCESSING.....");

                        release();
                        Intent intent = new Intent(context, NavigationActivity.class);
                        context.startActivity(intent);
                    }

                }

                //Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
                int[] location = new int[2];
                OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
                graphicOverlay.add(graphic);
                graphicOverlay.getLocationOnScreen(location);

                // OCRUtility.writeStringAsFile(item.getValue(),location);

//Now sending to OcrHelper.Processor


            }*/



        }

        createOCRFormGraphics();
    }

    public static double getSlopeLine(MyLine line) {
        Rect first = line.getLineElem().get(0).getElem().getBoundingBox();
        Rect last = line.getLineElem().get(line.getLineElem().size()-1).getElem().getBoundingBox();
        if (last.top > first.top)
            return (double)(last.bottom-first.bottom)/(double)(last.right-first.left);
        else
            return (double)(last.top-first.top)/(double)(last.right-first.left);
    }
    public double getLocalSlope(ArrayList<MyLine> tempAllLines, int index) {
        double maxSlope = 0;
        int minHorizLength = 250;
        int maxLookAround = 3;
        for (int i=0;i<maxLookAround;i++) {
            for (int art=0;art<2;art++) {
                MyLine line = art == 0 ? (index-i >= 0 ? tempAllLines.get(index-i) : null)
                        : index+i < tempAllLines.size() && i != 0 ? tempAllLines.get(index+i) : null;
                if (line != null && line.getLineElem().size() > 1) {
                    double slope = line.getLineSlope();
                    int left = line.getLineElem().get(0).getElem().getBoundingBox().left;
                    int right = line.getLineElem().get(line.getLineElem().size()-1).getElem().getBoundingBox().right;
                    if (right-left > minHorizLength)
                        return slope;
                    if (Math.abs(maxSlope) < Math.abs(slope))
                        maxSlope = slope;
                }
            }
        }
        if (true)
            return maxSlope;
        for (int i = index, is = Math.max(0,index-3); i>=is; i--) {
            if (Math.abs(tempAllLines.get(i).getLineSlope()) > Math.abs(maxSlope))
                maxSlope = tempAllLines.get(i).getLineSlope();
        }
        for (int i = index + 1, is = Math.min(tempAllLines.size(), index + 3); i < is; i++) {
            if (Math.abs(tempAllLines.get(i).getLineSlope()) > Math.abs(maxSlope))
                maxSlope = tempAllLines.get(i).getLineSlope();
        }
        return maxSlope;
    }

    public static final boolean g_doSlopeBased = false;
    public static final boolean g_doNewValReader = true;

    public static void mergeLine(ArrayList<MyLine> allLines, MyLine newLine) {
        ArrayList<MyLine.ElemPlusPos> newLineElemList = newLine.getLineElem();
        if (newLineElemList == null && newLineElemList.size() <= 0)
            return;
        Rect firstRect = newLineElemList.get(0).getElem().getBoundingBox();
        Rect lastRect = newLineElemList.get(newLineElemList.size() - 1).getElem().getBoundingBox();
        int left = firstRect.left;
        int right = lastRect.right;
        int adjustedTop = newLine.getTop();
        int adjustedBottom = newLine.getBottom();
        int height = adjustedBottom - adjustedTop;
        boolean merged = false;
//        Log.d("New Line", newLine.getLineString()+" AdjTop:"+adjustedTop+" Left:"+left+" Rt:"+right);
        for (int i = 0, is = allLines.size(); i < is; i++) {
            MyLine candidateLine = allLines.get(i);
            ArrayList<MyLine.ElemPlusPos> candidateLineElemList = candidateLine.getLineElem();
            Rect candidateLineFirstRect = candidateLineElemList.get(0).getElem().getBoundingBox();
            Rect candidateLineLastRect = candidateLineElemList.get(candidateLineElemList.size() - 1).getElem().getBoundingBox();

            int candidateLineLeft = candidateLineFirstRect.left;
            int candidateLineRight = candidateLineLastRect.right;
            int candidateBottom = candidateLine.getBottom();
            int candidateTop = candidateLine.getTop();
            int candidateHeight = candidateBottom - candidateTop;
//            Log.d("Candidate Line", candidateLine.getLineString()+" AdjTop:"+candidateTop+" Left:"+candidateLineLeft+" Rt:"+candidateLineRight);
            int overlapVert = 0;
            if (candidateBottom <= adjustedTop || candidateTop >= adjustedBottom)
                overlapVert = 0;
            else if (candidateTop >= adjustedTop) {  //candidate line below top of newLine
                if (candidateBottom <= adjustedBottom) //but candidate bottom above bottom of new line
                    overlapVert = candidateBottom - candidateTop;
                else //if (candidateBottom > adjustedBottom)
                    overlapVert = adjustedBottom - candidateTop;
            } else { //if (adjustedTop > candidateTop) {//new line below candidate
                if (candidateBottom <= adjustedBottom) //but candidateBottom is above new line bottom
                    overlapVert = candidateBottom - adjustedTop;
                else //if (candidateBottom > adjustedBottom)
                    overlapVert = adjustedBottom - adjustedTop;
            }
            int minH = Math.min(height, candidateHeight);
            if (overlapVert > 0.5 * height) { //vertically seems to be in same line
                if (candidateLineRight <= left) { //candidate line to left of newLine
                    merged = true;
                    addLineToElementList(candidateLine, newLine, true);//candidateLine.add(newLine.getLineElem().get(i2));
//                    Log.d("Line Merged : "," Cand On Left:"+candidateLine.getLineString());

                } else if (candidateLineLeft >= right) {//candidate line to right of newLine
                    merged = true;
                    addLineToElementList(candidateLine, newLine, false);
//                    Log.d("Line Merged : "," Cand On right:"+candidateLine.getLineString());
                }


            }
            if (merged)
                break;

        }
        if (!merged)
            allLines.add(newLine);
    }

    public void writeSortedLinesStringAsFile(MyLine line) {

        try {
            String filename = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/" + getFilename();
            FileWriter fw = new FileWriter(filename, true);
//            fw.write("---------Sorted Line-------- \n");//+"x="+location[0]+",y="+location[1] + "\n");
            fw.write("line:[" + line.getLineString() + "]");
            fw.write("Rect:[" + line.getLine().getBoundingBox().flattenToString() + "]\n\n");

            fw.close();
        } catch (IOException e) {
            Log.e("OcrDetectorProcessor", e.getMessage());
        }
    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        graphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        ArrayList<MyLine> allLines = new ArrayList<MyLine>();
        ArrayList<MyLine> tempAllLines = new ArrayList<MyLine>();


        double slopeTot = 0;
        double slopeCnt = 0;
        double maxSlope = 0;
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item == null)
                continue;
            Log.e("OcrDetectorProcessor", "TextBlock detected! " + item.getValue() + " Rect:[" + item.getBoundingBox().flattenToString() + "]\n\n\n");
            List<Line> lineList = (List<Line>) item.getComponents();

            for (Line line : lineList) {
                MyLine myLine = MyLine.getLine(line);
                if (myLine.getLineElem() == null || myLine.getLineElem().size() == 0)
                    continue;
                tempAllLines.add(myLine);
                if (!g_doSlopeBased)
                    continue;
                double slope = getSlopeLine(myLine);
                myLine.setLineSlope(slope);
                slopeTot += slope;//not used
                slopeCnt++;//not used
                if (Math.abs(slope) > Math.abs(maxSlope))//not used
                    maxSlope = slope;//not used
            }
        }
        if (g_doSlopeBased) {
            double slope = maxSlope; //slopeTot/slopeCnt not used
            slope = 0;
            for (int i = 0, is = tempAllLines.size(); i < is; i++) {
                MyLine line = tempAllLines.get(i);
                Rect first = line.getLineElem().get(0).getElem().getBoundingBox();
                slope = getLocalSlope(tempAllLines, i);
                int intercept = (int) Math.round(first.top - slope * first.left);
                line.setTop(intercept);
                line.setBottom(intercept + first.bottom - first.top);
            }
            Log.d("Bef Merge", "Lines: slope: " + slope + " Avg:" + slopeTot + " slopecnt:" + slopeCnt);
            //        for (MyLine line:tempAllLines) {
            //            System.out.println("line:["+line.getLineString()+"] Cnt:"+line.getLineElem().size()+" adjTop:"+line.getTop()
            //                    +" First top:"+line.getLineElem().get(0).getElem().getBoundingBox().top
            //                    +" First left:"+line.getLineElem().get(0).getElem().getBoundingBox().left
            //                    +" Last top:"+line.getLineElem().get(line.getLineElem().size()-1).getElem().getBoundingBox().top
            //                    +" Last right:"+line.getLineElem().get(line.getLineElem().size()-1).getElem().getBoundingBox().right
            //                    +" Slope Of Line:"+((double)(line.getLineElem().get(line.getLineElem().size()-1).getElem().getBoundingBox().top-line.getLineElem().get(0).getElem().getBoundingBox().top)
            //                    /(double)(line.getLineElem().get(line.getLineElem().size()-1).getElem().getBoundingBox().right-line.getLineElem().get(0).getElem().getBoundingBox().left))
            //            );
            //        }
            for (MyLine myLine : tempAllLines) {
                mergeLine(allLines, myLine);
            }

            Collections.sort(allLines);
            Log.d("Sorted", "All Lines: slope: " + slopeTot + " slopecnt:" + slopeCnt);
          /*  for (MyLine line : allLines) {
                System.out.println("line:[" + line.getLineString() + "] Cnt:" + line.getLineElem().size() + " adjTop:" + line.getTop()
                        + " First top:" + line.getLineElem().get(0).getElem().getBoundingBox().top
                        + " First left:" + line.getLineElem().get(0).getElem().getBoundingBox().left
                        + " Last top:" + line.getLineElem().get(line.getLineElem().size() - 1).getElem().getBoundingBox().top
                        + " Last right:" + line.getLineElem().get(line.getLineElem().size() - 1).getElem().getBoundingBox().right
                );
            }*/
        }//if doSlopeBased
        else {
            allLines = tempAllLines;
        }
        NewValReader valReader = g_doNewValReader ? NewValReader.getNewValReader(allLines) : null;
        MyLine currLine=null;
        MyLine prevLine=null;
        MyLine nextLine=null;
        for (int j=0,js=allLines.size(); j<js; j++) {
//            if (!g_doNewValReader) {
                if (currLine == null)
                    currLine = allLines.get(j);
                if (prevLine == null && j != 0)
                    prevLine = allLines.get(j - 1);
                if (nextLine == null && j != js - 1)
                    nextLine = allLines.get(j + 1);
//            }
            if(OcrHelper.process(currLine, allLines, j, valReader)){
                OCRUtility.appendLog("OcrDetectorProcessor::","FORM IS COMPLETE ..... STOP PROCESSING.....");
                release();
                Intent intent = new Intent(context, ManualEntryActivity.class);
                intent.putExtra(C.IsScanned,true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            if (g_doNewValReader) {
                prevLine = currLine;
                currLine = nextLine;

                nextLine = null;
            }
        }
        createOCRFormGraphics();
    }

    private static void addLineToElementList(MyLine candidateLine, MyLine newLine,boolean addToLeft) {
        ArrayList<MyLine.ElemPlusPos> candidateLineElemList = candidateLine.getLineElem();
        ArrayList<MyLine.ElemPlusPos> newLineElemList = newLine.getLineElem();
        if (addToLeft){//candidate is to left and we are adding newElem to right

            int candidateLastElemPos= candidateLine.getLineString().length()+1;

            candidateLine.setLineString(candidateLine.getLineString()+" "+newLine.getLineString());
            for (int i = 0,is=newLineElemList.size(); i<is ; i++) {
                MyLine.ElemPlusPos elem=newLineElemList.get(i);
                int newElemPos=elem.getPos()+candidateLastElemPos;
                elem.setPos(newElemPos);
                candidateLineElemList.add(elem);
            }
            return ;
        }else{// add condidate list to RIGHT of newLine
            int newLineLastElemPos= newLine.getLineString().length()+1;
            candidateLine.setLineString(newLine.getLineString()+" "+candidateLine.getLineString());
            for (int i = 0; i <candidateLineElemList. size() ; i++) {
                MyLine.ElemPlusPos elem=candidateLineElemList.get(i);
                int newElemPos=elem.getPos()+newLineLastElemPos;
                elem.setPos(newElemPos);
            }
            for (int i = newLineElemList. size() - 1; i >= 0 ; i--) {
                MyLine.ElemPlusPos elem=newLineElemList.get(i);
                candidateLineElemList.add(0,elem);
            }
            return ;
        }
    }


    public void writeStringAsFile(final String fileContents, int[] location) {

        try {
            String filename = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/"+getFilename();
            //Toast.makeText(context, filename, Toast.LENGTH_LONG).show();
            Log.e("file-", filename);
            FileWriter fw = new FileWriter(filename, true);
            Date c = Calendar.getInstance().getTime();
            fw.write(c.toString()+"--------------------------- \n");//+"x="+location[0]+",y="+location[1] + "\n");
            fw.write(fileContents + "\n\n");
            fw.close();
        } catch (IOException e) {
            Log.e("OcrDetectorProcessor", e.getMessage());
        }
    }

    public String getFilename(){
        Date c = Calendar.getInstance().getTime();
        int h=c.getHours();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        return "OCR_"+formattedDate+"_"+h+".txt";
    }


    private void createOCRFormGraphics() {
        Document doc=OcrHelper.getCurrDocument();
        if(doc!=null) {
            OcrFormGraphic graphic = new OcrFormGraphic(graphicOverlay, doc);
            graphicOverlay.add(graphic);
        }else  if(doc==null) {
            Document document1=null;
            for ( Document docu:OcrHelper.configList){
                if(docu.totalFormFieldsCompleted()>=2)
                    document1=docu;
            }
            if(document1!=null) {
                OcrFormGraphic graphic = new OcrFormGraphic(graphicOverlay, document1);
                graphicOverlay.add(graphic);
            }
        }
   }


    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        graphicOverlay.clear();
    }
}

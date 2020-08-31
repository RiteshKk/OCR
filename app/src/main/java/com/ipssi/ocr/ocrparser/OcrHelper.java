package com.ipssi.ocr.ocrparser;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class OcrHelper {
    public static ArrayList<Document> configList = null;
    static ArrayList<KeyToMatch> allDocumentsKeyToMatch = new ArrayList<KeyToMatch>();
    private static String previousLine;
    private static Document currDocument;
    private static String configFilename = "D:\\config.json";
    private static Gson gson = new Gson();

    public static Document getCurrDocument() {
        return currDocument;
    }

    public static String getFormValues() {
        StringBuilder sb = new StringBuilder("");
        if (currDocument != null) {
            for (FormField field : currDocument.getFormFields()) {

                sb.append(field.getValuesReadCount() + "--" + field.getFormconfig().getFormFieldName() + "=");

//				ArrayList<Entry<String, Integer>> sortedValues=field.getSortedValues();
//				System.out.println(field.getValuesReadCount()+"-field-"+field.getFormconfig().getFormFieldName()+"=");
//				System.out.println(sortedValues);
                sb.append("[value1=" + field.getValueBestPossible() + "] value2=[" + field.getValueSecondBestPossible() + "]\n");
                sb.append("\n");
            }

        } else {

            if (configList != null) {
                sb.append("Current Document is Empty..\n");

                for (Document doc : configList) {
                    sb.append("----------------------Doc=" + doc.getName() + "\n");
                    for (FormField field : doc.getFormFields()) {
                        sb.append("" + field.getFormconfig().getFormFieldName() + "=" + field.getSortedValues());
                        sb.append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String readJsonFromURL(Context context) {

        if (configList != null)
            return "";
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL("http://203.197.197.18:8480/static/OCR/config.json");
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            OcrHelper.readConfigFile(context, "config.json");
//			e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//		setAllDocumentsKeyToMatch() ;

        return null;
    }

    /**
     * @return
     */
    public static void readConfigFile(Context context, String fileName) {

        OCRUtility.appendLog("OcrHelper", "readConfigFile config.json ");
        String jsonString = getJsonFromAssets(context, fileName);
//		String jsonString=readJsonFromURL();

        OCRUtility.appendLog("Config::", jsonString);
        ConfigContainer configs = gson.fromJson(jsonString, ConfigContainer.class);
        configList = configs.getDocuments();

        for (Document doc : configList) {
            for (FormField formField : doc.getFormFields()) {
                FormFieldConfig config = formField.getFormconfig();
                config.setToMatch(OCRUtility.getCleanedString(config.getToMatch()));
                config.setReadThreshold(5);
            }
        }
    }


    static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonString;
    }

    /**
     * @param currLine
     * @param prevLine
     * @param nextLine
     * @return
     */

    private static MyLine g_currLine; public static MyLine g_prevLine; private static MyLine g_nextLine;

    public static boolean process(MyLine currLine, ArrayList<MyLine> allLines, int currIndex, NewValReader valReader) {


//        MyLine g_currLine= currLine;

//        MyLine g_nextLine=nextLine!=null?nextLine:null;
//		public static boolean process(Line currLine, Line prevLine, Line nextLine) {
if(currLine!=null)
        Log.e("Curr Line [" , currLine.getLineString() + "]"+currLine.getTop()+" b:"+currLine.getBottom()+" idx:"+currIndex);

        boolean isAllFieldscomplete = false;

        if (currDocument == null) {
            for (Document document : configList) {
                if (document.checkIfKeysInLineAndIfComplete(currLine)) {
                    currDocument = document;
                    break;
                }
            }
        }
        if (currDocument == null) {
            for (Document document : configList) {
                isAllFieldscomplete = document.updateFormField(currLine, allLines, currIndex, valReader);
                if (isAllFieldscomplete) {
                    currDocument = document;
                    break;
                }
            }
        } else {
            isAllFieldscomplete = currDocument.updateFormField(currLine, allLines, currIndex, valReader);

        }
        if (isAllFieldscomplete) {
            OCRUtility.appendLog("OcrHelper", "Current Doc[" + currDocument.getName() + "] All Form Fields Found.[Exiting Reading]");
            return isAllFieldscomplete;
        }
        g_prevLine=currLine;
        return isAllFieldscomplete;
    }

    private static void readOCRData(String filename) {
        try {
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String str = "";
            while ((str = br.readLine()) != null) {
                if (str.contains("---") || "".equalsIgnoreCase(str))
                    continue;
//				if(str.contains("Shipped to")){
                //	process(str);
            }
            br.close();
            in.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Exception=>" + e.getMessage());
            e.printStackTrace();
        }

    }


	public static void resetObjects() {
    	currDocument=null;
    	configList=null;
	}
}

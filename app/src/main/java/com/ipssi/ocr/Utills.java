package com.ipssi.ocr;

public class Utills {

    //    private static final String SERVER ="10.0.2.2:8080";
    private static final String SERVER = "203.197.197.18:9950";
    public static final String CONFIG_FILE_URL = "http://" + SERVER + "/static/OCR/config.json";
    public static final String LOGIN_URL = "http://" + SERVER + "/LocTracker/api/login.do";
    public static final String DATA_SAVE_URL = "http://" + SERVER + "/LocTracker/api/mplmobileservice/handle.do?action=saveInvoice";
    public static final String MAT_URL = "http://" + SERVER + "/LocTracker/TempleDashboardData.jsp?action_p=global_json";
    public static final String GPS_DATA_URL = "http://" + SERVER + "/LocTracker/api/mplmobileservice/handle.do?action=getVehicleStatus&veh=";
    public static final String AFTER_UNLOAD_URL = "http://" + SERVER + "/LocTracker/api/mplmobileservice/handle.do?action=saveReceipt&veh=";

    public static String toCamelCase(final String init) {
        if (init == null)
            return null;

        final StringBuilder ret = new StringBuilder(init.length());

        for (final String word : init.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(Character.toUpperCase(word.charAt(0)));
                ret.append(word.substring(1).toLowerCase());
            }
            if (!(ret.length() == init.length()))
                ret.append(" ");
        }

        return ret.toString();
    }
    //203.197.197.16:9950
}

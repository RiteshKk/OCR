package com.ipssi.ocr.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FormData implements Parcelable {
    public FormData(String vehicleNo, String invoiceNo, String dateTime, String customer, String srNo, String qty) {
        this.vehicleNo = vehicleNo;
        this.invoiceNo = invoiceNo;
        this.dateTime = dateTime;
        this.customer = customer;
        this.srNo = srNo;
        this.qty = qty;
    }

    public FormData() {
    }

    protected FormData(Parcel in) {
        vehicleNo = in.readString();
        invoiceNo = in.readString();
        dateTime = in.readString();
        customer = in.readString();
        srNo = in.readString();
        qty = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vehicleNo);
        dest.writeString(invoiceNo);
        dest.writeString(dateTime);
        dest.writeString(customer);
        dest.writeString(srNo);
        dest.writeString(qty);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FormData> CREATOR = new Creator<FormData>() {
        @Override
        public FormData createFromParcel(Parcel in) {
            return new FormData(in);
        }

        @Override
        public FormData[] newArray(int size) {
            return new FormData[size];
        }
    };

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getSrNo() {
        return srNo;
    }

    public void setSrNo(String srNo) {
        this.srNo = srNo;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    private String vehicleNo;
    private String invoiceNo;
    private String dateTime;
    private String customer;
    private String srNo;
    private String qty;


}

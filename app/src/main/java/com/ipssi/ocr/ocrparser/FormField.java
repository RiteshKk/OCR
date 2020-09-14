package com.ipssi.ocr.ocrparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class FormField {
	private FormFieldConfig fieldconfig;
	private ArrayList<Entry<String, Integer>> sortedValues= new ArrayList<>();
	
	public FormField(FormFieldConfig fieldconfig){
		this.fieldconfig=fieldconfig;
	}
	//
	public FormField(){}
	
	public void addValue(String value){
		if (fieldconfig.getReadUptoLength()>0)
		value=value.trim().substring(0,fieldconfig.getReadUptoLength());
		else if (value.length()<fieldconfig.getReadUptoLength()){
			return;
		}
		 int strCount;
		 boolean entryFound=false;
		for (Entry<String, Integer> stored : sortedValues) {
			 if (value.equals(stored.getKey())){
				 strCount=stored.getValue()+1;
				 stored.setValue(strCount);
				 entryFound=true;
				 break;
			 }
		}
		if(!entryFound){
			HashMap<String, Integer> map= new HashMap<>();
			map.put(value,1);
			sortedValues.addAll(map.entrySet());
		}
		
		//Sort
	    Collections.sort(sortedValues,
	            new Comparator<Entry<String, Integer>>() {
	                @Override
	                public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
	                    return e2.getValue().compareTo(e1.getValue());
	                }
	            }
	    );
	    
	}
	
	public int getValuesReadCount(){
		return this.sortedValues.size();
	}
	
	public String getValueBestPossible(){
		return this.sortedValues.size()>0?this.sortedValues.get(0).getKey():null;
	}
	public String getValueSecondBestPossible(){
		return this.sortedValues.size()>1?this.sortedValues.get(1).getKey():null;
	}
	
	public boolean isComplete(){
  //Check is complete

		int totalReadCount=0;
		for (Entry<String, Integer> stored : sortedValues) {
			totalReadCount =totalReadCount + stored.getValue();
			}
		if(totalReadCount>1 && fieldconfig.isIgnoreComplete())
			return true;
		if(totalReadCount>= fieldconfig.getReadThreshold()){
			System.out.println("Total ReadThreshold Reached ["+fieldconfig.getFormFieldName()+"]");
			return true;
		}else if(this.sortedValues.size()>=2){
			int diff=this.sortedValues.get(0).getValue()-this.sortedValues.get(1).getValue();
			if(diff>= fieldconfig.getBestDiffThreshold()){
				System.out.println("Total BestDiffThreshold Reached ["+fieldconfig.getFormFieldName()+"]["+this.sortedValues.get(0)+"],["+this.sortedValues.get(1)+"]");
				return true;
			}
		}
		return false;
	}
	public FormFieldConfig getFormconfig() {
		return fieldconfig;
	}
	public void setFormconfig(FormFieldConfig fieldconfig) {
		this.fieldconfig = fieldconfig;
	}
	
	public boolean isKeyMatch(String string){
		return string.contains(fieldconfig.getToMatch());
	}
	public String getKeyMatchString(){
		return fieldconfig.getToMatch();
	}
	
	public ArrayList<Entry<String, Integer>> getSortedValues() {
		return sortedValues;
	}
	public void setSortedValues(ArrayList<Entry<String, Integer>> sortedValues) {
		this.sortedValues = sortedValues;
	}
	
	
}

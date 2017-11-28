/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class Normalize extends Store {
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
    
    public void initializeData(List<String> labels, List<String> dimensions, String classifier, List<Data> listData){
        setLabel(labels);
        setDimension(dimensions);
        setClassifier(classifier);
        setListData(listData);
    }
    
    private List<Double> getColumn(List<Data> result, String key){
        List<Double> columnValue = new ArrayList<>();
        for(Data data : result){
            try {
                columnValue.add(numberFormat.parse(data.getAttribute(key).toString()).doubleValue());
            } catch (ParseException ex) {
                Logger.getLogger(Normalize.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return columnValue;
    }
    
    //Normalisasi min max
    public List<Data> normalizeMinMax(Double newMin, Double newMax){
        List<Data> result = duplicateList();
        
        for(String key : getDimension()){
            List<Double> columnValue = getColumn(result,key);             
            Double max = getMax(columnValue);   
            Double min = getMin(columnValue);
                        
            for(Data data : result){
                try {
                    Double currentData = numberFormat.parse(data.getAttribute(key).toString()).doubleValue();
                    Double newValue = (((currentData - min)*(newMax - newMin))/(max-min))+newMin;
                    newValue = Math.round(newValue * 1000.0) / 1000.0;
                    data.getAttribute().put(key, newValue);
                } catch (ParseException ex) {
                    Logger.getLogger(Normalize.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return result;
    }
    
    //Normalisasi z-score
    public List<Data> normalizeZScore(){
        List<Data> result = duplicateList();
        
        for(String key : getDimension()){
            List<Double> columnValue = getColumn(result,key);             
            Double mean = countMean(columnValue);  
            Double std = countDeviation(columnValue);
                        
            for(Data data : result){
                try {
                    Double currentData = numberFormat.parse(data.getAttribute(key).toString()).doubleValue();
                    Double newValue = (currentData - mean)/std;
                    newValue = Math.round(newValue * 1000.0) / 1000.0;
                    data.getAttribute().put(key, newValue);
                } catch (ParseException ex) {
                    Logger.getLogger(Normalize.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return result;
    }
    
    //Normalisasi Decimal Scaling
    public List<Data> normalizeDecimalScaling(){
        List<Data> result = duplicateList();
        
        for(String key : getDimension()){
            List<Double> columnValue = getColumn(result,key);            
            Integer scale = getCardinalScaling(getMax(columnValue));   
            
            for(Data data : result){
                try {
                    Double currentData = numberFormat.parse(data.getAttribute(key).toString()).doubleValue();
                    Double newValue = currentData / Math.pow(10,scale);
                    newValue = Math.round(newValue * 1000.0) / 1000.0;
                    data.getAttribute().put(key, newValue);
                } catch (ParseException ex) {
                    Logger.getLogger(Normalize.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return result;
    }
    
    //Normalisasi Sigmoidal
    public List<Data> normalizeSigmoidal(){
        List<Data> result = duplicateList();
        
        for(String key : getDimension()){
            List<Double> columnValue = getColumn(result,key);            
            Double mean = countMean(columnValue);  
            Double std = countDeviation(columnValue);   
            
            for(Data data : result){
                try {
                    Double currentData = numberFormat.parse(data.getAttribute(key).toString()).doubleValue();
                    Double x = (currentData - mean)/std;
                    Double newValue = (1-Math.pow(Math.E,(x*(-1))))/(1+Math.pow(Math.E,(x*(-1))));
                    newValue = Math.round(newValue * 1000.0) / 1000.0;
                    data.getAttribute().put(key, newValue);
                } catch (ParseException ex) {
                    Logger.getLogger(Normalize.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return result;
    }
    
    //Normalisasi SoftMax
    public List<Data> normalizeSoftMax(Integer x){
        List<Data> result = duplicateList();
        
        for(String key : getDimension()){
            List<Double> columnValue = getColumn(result,key);            
            Double mean = countMean(columnValue);  
            Double std = countDeviation(columnValue);   
            
            int i = 0;
            for(Data data : result){
                try {
                    Double currentData = numberFormat.parse(data.getAttribute(key).toString()).doubleValue();
                    Double transfdata = (currentData-mean)/(x*(std/(2*Math.PI)));
                    Double newValue = 1/(1+Math.pow(Math.E, transfdata*(-1)));
                    newValue = Math.round(newValue * 1000.0) / 1000.0;
                    data.getAttribute().put(key, newValue);
                } catch (ParseException ex) {
                    Logger.getLogger(Normalize.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return result;
    }
    
}

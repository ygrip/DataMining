/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class Store {
    private Scanner userInput = new Scanner(System.in);
    private Util system = new Util();
    //Each data
    public class Data{
        public Data(){
            
        }
        public Data(Map<String,Object> data){
            this.attribute = data;
        }
        private Map<String,Object> attribute = new HashMap<>();

        public Map<String, Object> getAttribute() {
            return attribute;
        }
        
        public Object getAttribute(String key){
            return attribute.get(key);
        }
        
        public void setAttribute(Map<String, Object> atribute) {
            this.attribute = atribute;
        }
        
        public void setAttribute(String key, Object value){
            this.attribute.put(key, value);
        }
        
    }
    
    //Label and dimension
    private List<String> label =  new ArrayList<>();
    private List<String> dimension =  new ArrayList<>();
    private String classifier = "";
        
    public List<String> getLabel() {
        return label;
    }

    public void setLabel(List<String> label) {
        this.label = label;
    }
    
    public void addLabel(String label){
        this.label.add(label);
    }
    
    public void clearLabel(){
        this.label.clear();
    }

    public List<String> getDimension() {
       return dimension;
    }
    
    public void addDimension(String dimension){
        this.dimension.add(dimension);
    }
    
    public void clearDimension(){
        this.dimension.clear();
    }

    public void setDimension(List<String> dimension) {
        this.dimension = dimension;
    }
    
    public void setClassifier(String classifier){
        this.classifier = classifier;
    }
    
    public String getClassifier(){
        return classifier;
    }
    
    //Data management
    private List<Data> listData = new ArrayList<>();
    private Map<String, List<Data>> mappedData = new HashMap<>();

    public List<Data> getListData() {
        return listData;
    }

    public void setListData(List<Data> listData) {
        this.listData = listData;
    }
    
    public void addData(Data data){
        this.listData.add(data);
    }

    public Map<String, List<Data>> getMappedData() {
        return mappedData;
    }

    public void setMappedData(Map<String, List<Data>> mappedData) {
        this.mappedData = mappedData;
    }
    
    public List<Data> duplicateList(){
        List<Data> result = new ArrayList<>();
        for(Data data : getListData()){
            Data attribute = new Data();
            for(String key : data.getAttribute().keySet()){
                attribute.setAttribute(key, data.getAttribute(key));
            }
            result.add(attribute);
        }
        return result;
    }
    
    public List<Data> createSubSet(String... labels){
        List<Data> result = new ArrayList<>();
        for(Data data : getListData()){
            Data attribute = new Data();
            for(String key : data.getAttribute().keySet()){
                for(String label : labels){
                    if(key.equals(label)){
                        attribute.setAttribute(key, data.getAttribute(key));
                    }
                }
            }
            result.add(attribute);
        }
        return result;
    }
    
    public List<Data> removeSet(String... labels){
        List<Data> result = new ArrayList<>();
        for(Data data : getListData()){
            Data attribute = new Data();
            for(String key : data.getAttribute().keySet()){
                for(String label : labels){
                    if(!key.equals(label)){
                        attribute.setAttribute(key, data.getAttribute(key));
                    }
                }
            }
            result.add(attribute);
        }
        return result;
    }
    
    public void mappingData(){
        for(Data data : getListData()){
            if(this.mappedData.containsKey(String.valueOf(data.getAttribute(getClassifier())))){
                List<Data> list = this.mappedData.get(String.valueOf(data.getAttribute(getClassifier())));
                list.add(data);
            }else{
                List<Data> list = new ArrayList<>();
                list.add(data);
                this.mappedData.put(String.valueOf(data.getAttribute(getClassifier())), list);
            }
        }
    }
    
    //Sorting and evaluating data
    public void sort(String field, List<Data> dataList){
        Comparator<Data> comparator = defaultComparator;
        field = field.toLowerCase();
        boolean isLabel = false;
        for(String label : getLabel()){
            
            if(field.equals(label)){
                for(String dimension : getDimension()){
                    if(field.equals(dimension)){
                        comparator = (Data o1, Data o2) -> {
                            return Double.valueOf(o1.getAttribute(dimension).toString())
                                    .compareTo(Double.valueOf(o2.getAttribute(dimension).toString()));
                        };
                        isLabel = true;
                        break;
                    }
                }  
                if(isLabel){
                    break;
                }
            }
        }
        
        if(!isLabel){
            throw new IllegalArgumentException("Comparator not found for " + field);
        }else{
             Collections.sort(dataList, comparator);
        }
    }
    private Comparator<Data> defaultComparator = (Data o1, Data o2) -> {
        return String.valueOf(o1.getAttribute(getClassifier()))
                .compareTo(String.valueOf(o2.getAttribute(getClassifier())));
    };
    
    public void resetData(){
        classifier = "";
        dimension.clear();
        label.clear();
        listData.clear();
        mappedData.clear();
    }
    
    public void clearData(){
        mappedData.clear();
        listData.clear();
    }
    
    public void clearMappedData(){
        mappedData.clear();
    }
    
    //Data modification
    public void storeData(List<List<String>> rawData){
        String[] labels = {};
        storeData(rawData,labels);
    }
    
    public void storeData(List<List<String>> rawData, String... labels){
        
        if(!rawData.isEmpty()){
            System.out.println("\nSistem perlu bantuan anda untuk mengenali file,");
            List<String> listLabel = new ArrayList<>();
            int useDefaultLabel = 1;
            if(labels.length!=0){
                int check = 0;
                for(String label : labels){
                    if(check<rawData.get(0).size()){
                        if(!label.equals("")||!label.equals(" ")){
                            listLabel.add(label);
                        }
                    }
                }
                
                if(listLabel.size()!=rawData.get(0).size()){
                    listLabel.clear();
                    listLabel = rawData.get(0);
                }
                if(listLabel.size()==rawData.get(0).size()){
                    useDefaultLabel = 0;
                }
            }else{
                listLabel.clear();
                listLabel = rawData.get(0);
            }
            setLabel(listLabel);
            if(getDimension().isEmpty()){
                while(!checkDimension()){
                    
                }
                system.clearScreen();
            }
            
            if(getClassifier().isEmpty()){
                while(!checkClassifier()){
                    
                }
                system.clearScreen();
            }
            
            for(int i = useDefaultLabel; i<rawData.size(); i++){
                List<String> row = rawData.get(i);
                Data attribute = new Data();
                for(int j = 0; j<row.size(); j++){
                    attribute.setAttribute(getLabel().get(j), row.get(j));
                }
                addData(attribute);
            }
        }
    }
    
    private boolean checkDimension(){
        int count = 0;
        System.out.println("Pilih minimal satu kolom untuk dijadikan dimensi : \n");
        
        boolean selectDimension = false;
        for(String label : getLabel()){
            System.out.print("================");
        }
        System.out.print("\n|");
        for(String label : getLabel()){
            System.out.print("\t"+label+"\t|");
        }
        System.out.println("");
        for(String label : getLabel()){
            System.out.print("================");
        }
        System.out.print("\n\nPilihan anda (Pisahkan dengan , ) : ");
 
           String input = userInput.nextLine();
           String dimension[] = input.split(",");
           
           for(String label : getLabel()){
               for(String dim : dimension){
                   if(dim.toLowerCase().equals(label.toLowerCase())){
                       addDimension(label);
                       count++;
                   }
               }
           }
           if(count<1){
               system.clearScreen();
               checkDimension();
           }else{
               selectDimension = true;
           }
           return selectDimension;
    }
    
    private boolean checkClassifier(){
        System.out.println("Pilih satu kolom untuk dijadikan klasifier : \n");
        
        boolean selectClassifier = false;
        for(String label : getLabel()){
            if(!getDimension().contains(label)){
                System.out.print("================");
            } 
        }
        System.out.print("\n|");
        for(String label : getLabel()){
            if(!getDimension().contains(label)){
                System.out.print("\t"+label+"\t|");
            }        
        }
        System.out.println("");
        for(String label : getLabel()){
            if(!getDimension().contains(label)){
                System.out.print("================");
            } 
        }
        System.out.print("\n\nPilihan anda : ");
 
        String input = userInput.nextLine();
           
        for(String label : getLabel()){
            if(input.toLowerCase().equals(label.toLowerCase())){
                for(String dimension : getDimension()){
                    if(!input.toLowerCase().equals(dimension.toLowerCase())){
                        setClassifier(label);
                        selectClassifier = true;
                        break;
                    }
                }
                if(selectClassifier){
                    break;
                }
            }
        }
            
        return selectClassifier;
    }
    
    
    public void showData(List<Data> listData){
        showData(getLabel(), listData);
    }
    
    public void showData(List<String> labels, List<Data> listData){
        if(!labels.isEmpty()){
            for(String label : labels){
                System.out.print("================");
//                if(label.equals(getClassifier())){
//                    System.out.print("================");
//                }
            }
            System.out.print("\n|");
            for(String label : labels){
                System.out.print("\t"+label+"\t");
//                if(label.equals(getClassifier())){
//                    System.out.print("\t");
//                }
                System.out.print("|");
            }
            System.out.println("");
            for(String label : labels){
                System.out.print("================");
//                if(label.equals(getClassifier())){
//                    System.out.print("================");
//                }
            }
        }else{
            System.out.print("================");
            System.out.print("\n|\tnull\t|\n");
            System.out.print("================\n");
        }
        
        if(!listData.isEmpty()&&!labels.isEmpty()){
            for(Data row : listData){
                System.out.print("\n|");
                for(String key : labels){
                    System.out.print("\t"+row.getAttribute(key).toString()+"\t");
//                    if(label.equals(getClassifier())){
//                        System.out.print("\t");
//                    }
                    System.out.print("|");
                }
            }
            System.out.print("\n");
            for(String label : labels){
                System.out.print("================");
//                if(label.equals(getClassifier())){
//                    System.out.print("================");
//                }
            }
            System.out.print("\n");
        }else if(listData.isEmpty()&&!labels.isEmpty()){
            System.out.print("\n|");
            for(String key : labels){
                System.out.print("\tnull\t");
//                if(label.equals(getClassifier())){
//                    System.out.print("\t");
//                }
                System.out.print("|");
            }
            System.out.print("\n");
            for(String label : labels){
                System.out.print("================");
//                if(label.equals(getClassifier())){
//                    System.out.print("================");
//                }
            }
            System.out.print("\n");
        }else{
            System.out.print("|\tnull\t|\n");
            System.out.print("================\n");
        }
    }
    
    public Map<String, Object> getRow(int index){
        Map<String, Object> result = new HashMap<>();
        
        if(!getListData().isEmpty()){
            if(index >= 0 && index < getListData().size()){
                result = getListData().get(index).getAttribute();
            }
        }
        
        return result;
    }
    
    public List<Object> getColumn(String column){
        List<Object> result = new ArrayList<>();
        
        if(!getListData().isEmpty()){
            if(getLabel().contains(column)){
                for(Data data : getListData()){
                    result.add(data.getAttribute(column));
                }
            }
        }
        
        return result;
    }
    
    public Double sumData(List<Double> listData){
        Double result = 0.0;
        for(Double data : listData){
            result += data;
        }
        return result;
    }
    
    public Double countMean(List<Double> listData){
        return sumData(listData)/listData.size();
    }
    
    public Double countVariation(List<Double> listData){
        List<Double> listDataPow = new ArrayList<>();
        
        for(Double data : listData){
            listDataPow.add(Math.pow(data, 2));
        }
        
        return ((listData.size()*sumData(listDataPow))-Math.pow(sumData(listData),2))/(listData.size()*(listData.size()-1));
    }
    
    public Double countDeviation(List<Double> listData){
        Double result = 0.0;
        result = countVariation(listData);
        return Math.sqrt(result);
    }
    
    public Double getMax(List<Double> list){
        Collections.sort(list);
        return list.get(list.size()-1);
    }
    
    public Double getMin(List<Double> list){
        Collections.sort(list);
        return list.get(0);
    }
    
    public Integer getCardinalScaling(Double data){
        Integer result = 0;
        char[] cardinality = data.toString().toCharArray();
        
        for(char check : cardinality){
            if(cardinality.length==1&&check=='0'){
                result = 0;
            }else{
                if(check=='.'||check==','){
                    break;
                }else{
                    result++;
                }                
            }
        }
        return result;
    }
    
    public List<List<String>> convertToString(List<Data> listData){
        List<List<String>> text = new ArrayList<>();
        List<String> label = new ArrayList<>();
        for(String key : listData.get(0).getAttribute().keySet()){
            label.add(key);
        }
        text.add(label);
        for(Data data : listData){
            List<String> write = new ArrayList<>();
            for(String key : data.getAttribute().keySet()){
                write.add(data.getAttribute(key).toString());
            }
            text.add(write);
        }
        return text;
    }
    
    public void exportFile(String directory, String fileName, String fileType, List<List<String>> data){
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(directory+"\\"+fileName+"."+fileType));
            StringBuilder sb = new StringBuilder();
            for(List<String> line : data){
                int i = 0;
                for(String element : line){
                    if(i==(line.size()-1)){
                        sb.append(element);
                    }else{
                        sb.append(element).append(",");
                    }
                    
                    i++;
                }
                sb.append("\n");
            }
            pw.write(sb.toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Store.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }
    }
}


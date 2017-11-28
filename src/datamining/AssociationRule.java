/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining;

import static datamining.DecisionTree.data;
import datamining.Store.Data;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author user
 */
public class AssociationRule {
    static Store data = new Store();
    static Util system = new Util();
    static Scanner userInput = new Scanner(System.in);
    static String filePath = "D:\\data\\transaksi.txt";
    static File file = new File(filePath);
    static String directory = file.getParentFile().getAbsolutePath();
    static String openedFile = file.getName();
    static String[] fileName = openedFile.split("\\.");
    
    public static void main(String[] args){
        String input = "";
        data.storeData(system.readData(filePath));
        do{
            system.clearScreen();            
            System.out.println("Data "+fileName[0]+" : \n__________________");
            data.showData(data.getListData());
            System.out.println("");

            System.out.println("Itemset "+fileName[0]+" : \n__________________");
            printItemset(data.getListData());
            
            Map<String,Double> minimum = setMinimum();
            
            System.out.println("\nMetode apriori dengan minimum support : "+minimum.get("support")
                    +" dan minimum confidence : "+minimum.get("confidence")
                    +"\n__________________");
            
            //menghitung support masing masing elemen
            List<Map<String,String>> combinations = new ArrayList<>();
            int comboSize = 0;
            List<String> comboElement = new ArrayList<>();
            comboElement.addAll(data.getDimension());
            System.out.println("\nMenghitung Support : \n__________________");
            do{
                comboSize++;
                combinations.clear();
                combinations.addAll(makeCombo(comboElement,comboElement.size(),comboSize));
                List<Map<String,String>> dummyCombination = filterCombination(combinations,comboSize,minimum.get("support"));
                combinations.clear();
                combinations.addAll(dummyCombination); 
                comboElement.clear();
                for(Map<String,String> itemset : combinations){
                    for(String item : itemset.get("element").split(",")){
                        comboElement.add(item);
                    }
                }
                List<String> listDistinct = comboElement.stream().distinct().collect(Collectors.toList());
                comboElement.clear();
                comboElement.addAll(listDistinct);
            }while(!checkAssociation(combinations));
            
            //mendapatkan frequent itemset terakhir yang mungkin dibentuk
            comboElement.clear();
            for(String item : combinations.get(0).get("element").split(",")){
                comboElement.add(item);
            }
            
            //menghitung confidence yang mungkin dari frequent items
            System.out.println("\nMenghitung Confidence : \n__________________");
            List<Map<String,String>> confidences = possibleConfidence(makePermutation(comboElement));
            List<Map<String,String>> checkConfidence = filterConfidence(confidences,minimum.get("confidence"));
            confidences.clear();
            confidences.addAll(checkConfidence);
            
            //mencari kesimpulan
            summarize(confidences);
            
            System.out.print("\nApakah anda ingin mencoba lagi ?\n\t[Y/N]\t: ");
            input = userInput.nextLine();
        }while(input.equals("Y")||input.equals("y"));
    }
    
    public static void summarize(List<Map<String,String>> result){
        System.out.println("\nKesimpulan : \n__________________");
        for(Map<String,String> conf : result){
            System.out.print("Peluang munculnya ");
            String[] element = conf.get("element").split(",");                
            for(int i = 0; i<element.length; i++){
                System.out.print(element[i]);
                if(i<(element.length-1)){
                    System.out.print(" dan ");
                }
            }
            System.out.print(" untuk setiap data yang memiliki ");
            String[] target = conf.get("label").split(",");
            for(int i = 0; i<target.length; i++){
                System.out.print(target[i]);
                if(i<(target.length-1)){
                    System.out.print(" dan ");
                }
            }
            Double percentage = Double.valueOf(conf.get("confidence"))*100;
            System.out.print(" adalah "+percentage+"%.\n");
        }
    }
    
    public static Map<String,Double> setMinimum(){
        Double minSupport = 0.0;
        Double minConfidence = 0.0;
            
        System.out.print("\nMasukkan nilai minimum support [0 - 1] : ");
        String min = userInput.nextLine().replaceAll(",", ".");
        while(!checkInput(min,0,1)){
            System.out.print("Input anda salah, masukkan lagi : ");
            min = userInput.nextLine().replaceAll(",", ".");
            if(checkInput(min,0,1)){
                break;
            }
        }
        minSupport = Double.valueOf(min);
            
        System.out.print("\nMasukkan nilai minimum confidence [0 - 1] : ");
        min = userInput.nextLine().replaceAll(",", ".");
        while(!checkInput(min,0,1)){
            System.out.print("Input anda salah, masukkan lagi : ");
            min = userInput.nextLine().replaceAll(",", ".");
            if(checkInput(min,0,1)){
                break;
            }
        }
        minConfidence = Double.valueOf(min);
        Map<String,Double> minimum = new HashMap<>();
        minimum.put("support",minSupport);
        minimum.put("confidence",minConfidence);
        return minimum;
    }
    
    public static List<Map<String,String>> filterConfidence(List<Map<String,String>> confidences, Double minConfidence){
        List<Map<String,String>> newConf = new ArrayList<>();
        for(Map<String,String> conf : confidences){
            Double confidence = 0.0;
            List<String> element = new ArrayList<>();
            List<String> target = new ArrayList<>();
              
            for(String s : conf.get("label").split(",")){
                target.add(s);
            }
              
            element.addAll(target);
            for(String s : conf.get("element").split(",")){
                element.add(s);
            }
             
            confidence = countSupport(element) / countSupport(target);
            confidence = Math.round(confidence * 100.0) / 100.0;
            conf.put("confidence", confidence.toString());
        }
        System.out.println("Confidence awal : \n__________________");
        for(Map<String,String> conf : confidences){
            System.out.println("Conf("+conf.get("label")+" -> "+conf.get("element")+") = "+conf.get("confidence"));
        }
            
        List<Map<String,String>> dummyConf = new ArrayList<>();
        dummyConf.addAll(confidences);
        for(Map<String,String> conf : dummyConf){
            if(Double.valueOf(conf.get("confidence"))<minConfidence){
                confidences.remove(conf);
            }else{
                newConf.add(conf);
            }
        }
        System.out.println("\nConfidence terpilih : \n__________________");
        for(Map<String,String> conf : confidences){
            System.out.println("Conf("+conf.get("label")+" -> "+conf.get("element")+") = "+conf.get("confidence"));
        }
        
        return newConf;
    }
    
    public static List<Map<String,String>> possibleConfidence(List<List<String>> permutations){
        List<Map<String,String>> confidences = new ArrayList<>();
        int elementSize = 1;
        int index = 0;
        while(permutations.get(permutations.size()-1).size() > elementSize){
            List<String> items = permutations.get(index);
            String label = "";
            String element = "";
            if(items.size()>elementSize){
                for(int i = 0; i<items.size(); i++){
                    if(i<elementSize){
                        label = label.concat(items.get(i)+",");
                    }else{
                        element = element.concat(items.get(i)+",");
                    }
                }
                Map<String,String> confidence = new HashMap<>();
                confidence.put("label",label);
                confidence.put("element",element);
                confidences.add(confidence);
            }
            index++;
            if(index==permutations.size()){
                index = 0;
                elementSize++;
            }                
        }
            
        List<Map<String,String>> dummyConf = new ArrayList<>();
        dummyConf.addAll(confidences);
            
        for(int i = 0; i<dummyConf.size(); i++){
            Map<String,String> checkConf = dummyConf.get(i);
            int count = 0;
            for(int j = 0; j < confidences.size(); j++){
                Map<String,String> conf = confidences.get(j);
                
                if(checkDuplicate(checkConf.get("label").split(","),conf.get("label").split(","))
                        &&checkDuplicate(checkConf.get("element").split(","),conf.get("element").split(","))){
                    count++;                 
                }
            }
            if(count>1){
                confidences.remove(checkConf);
            }
        }
        return confidences;
    }
    
    public static boolean checkDuplicate(String[] current, String[] prev){
        int count = 0;
        for(int i = 0; i < current.length; i++){
            int check = 0;
            for(int j = 0; j < prev.length; j++){
                if(current[i].equals(prev[j])){
                    check++;
                }
            }
            if(check>0){
                count++;
            }
        }
        if(count==current.length&&current.length==prev.length){
            return true;
        }else{
            return false;
        }
    }
    
    public static List<List<String>> makePermutation(List<String> items){
        List<Map<String,String>> dummyCombination = new ArrayList<>();
        List<List<String>> permutations = new ArrayList<>();
        for(int i = 2; i <= items.size(); i++){
            dummyCombination.addAll(makeCombo(items,items.size(),i));
        }
           
        for(Map<String,String> combo : dummyCombination){
            List<String> check = new ArrayList<>();
            for(String el : combo.get("element").split(",")){
                check.add(el);
            }
            permutations.addAll(generatePerm(check));
        }
        return permutations;
    }
    
    public static List<List<String>> generatePerm(List<String> original) {
     if (original.isEmpty()) {
       List<List<String>> result = new ArrayList<>(); 
       result.add(new ArrayList<>()); 
       return result; 
     }
     String firstElement = original.remove(0);
     List<List<String>> returnValue = new ArrayList<>();
     List<List<String>> permutations = generatePerm(original);
     for (List<String> smallerPermutated : permutations) {
       for (int index=0; index <= smallerPermutated.size(); index++) {
         List<String> temp = new ArrayList<>(smallerPermutated);
         temp.add(index, firstElement);
         returnValue.add(temp);
       }
     }
     return returnValue;
   }
    
    public static boolean checkAssociation(List<Map<String,String>> combinations){
        if(combinations.isEmpty()){
            return false;
        }else return combinations.size()==1;
    }
    
    public static List<Map<String,String>> filterCombination(List<Map<String,String>> itemList,int comboSize,Double minSupport){
        List<Map<String,String>> result = new ArrayList<>();
        System.out.println("\nItemset awal untuk L"+comboSize+" : \n__________________");
        for(int i = 0; i<4; i++){
            System.out.print("================");
        }
        System.out.print("\n|\t\titemset\t\t|\t\tsupport\t\t|\n");
        for(int i = 0; i<4; i++){
            System.out.print("================");
        }
        System.out.print("\n");
        for(Map<String,String> combination : itemList){
            Integer count = 0;                
            List<String> items = new ArrayList<>();
            for(String item : combination.get("element").split(",")){
                items.add(item);
            }
            Integer total = data.getListData().size();
            Double support = countSupport(items);
            combination.put("support",support.toString());
            if(support>=minSupport){
                result.add(combination);
            }
            System.out.print("|  "+combination.get("element")+"  |\t\t"+combination.get("support")+"\t\t|\n");
        }
        for(int i = 0; i<4; i++){
            System.out.print("================");
        }
        
        System.out.println("\n\nItemset terpilih untuk L"+comboSize+" : \n__________________");
        for(int i = 0; i<4; i++){
            System.out.print("================");
        }
        System.out.print("\n|\t\titemset\t\t|\t\tsupport\t\t|\n");
        for(int i = 0; i<4; i++){
            System.out.print("================");
        }
        System.out.print("\n");
        for(Map<String,String> combination : result){
            System.out.print("|\t"+combination.get("element")+"\t|\t\t"+combination.get("support")+"\t\t|\n");
        }
        for(int i = 0; i<4; i++){
            System.out.print("================");
        }
        System.out.println("");
        return result;
    }
    
    public static Double countSupport(List<String> items){
        Double support = 0.0;
        Integer count = 0;
        for(Data transaction : data.getListData()){
            int check = 0;
            for(String item : items){
                if(Integer.valueOf(transaction.getAttribute(item).toString())>0){
                    check++;
                }
            }
            if(check==items.size()){
                count++;
            }
        }
        Integer total = data.getListData().size();
        support = Double.valueOf(count.toString())/Double.valueOf(total.toString());
        support = Math.round(support * 100.0) / 100.0;
        return support;
    }
    
    static void addCombination(List<String> items, List<String> data, int start,int end, int index, int r){
        // Current combination is ready to add in list
        if (index == r){    
            String element = "";
            for (int j=0; j<r; j++){
                element = element.concat(data.get(j)+",");
            }
            Map<String,String> combination = new HashMap<>();
            combination.put("element", element);
            tempCombination.add(combination);
        }
 
        // replace index with all possible elements. The condition
        // "end-i+1 >= r-index" makes sure that including one element
        // at index will make a combination with remaining elements
        // at remaining positions
        for (int i=start; i<=end && end-i+1 >= r-index; i++)
        {
            data.remove(data.get(index));
            data.add(index,items.get(i));
            addCombination(items, data, i+1, end, index+1, r);
        }
    }
 
    // The main function that prints all combinations of size r
    private static List<Map<String,String>> tempCombination = new ArrayList<>();
    static List<Map<String,String>> makeCombo(List<String> items, int n, int r){
        tempCombination = new ArrayList<>();
        // A temporary array to store all combination one by one
        List<String> dummy = new ArrayList<>();
        dummy.addAll(items);
 
        addCombination(items, dummy, 0, n-1, 0, r);
        return tempCombination;
    }
    
    public static boolean checkInput(String input, int min, int max){
        if(Pattern.matches("^[\\+\\-]{0,1}[0-9]+[\\.\\,][0-9]+$", (CharSequence) input)){
            Double val = Double.parseDouble(input);
            return !(val < min || val > max);
        }else{
            return false;
        }
    }
    
    public static Map<String,List<String>> printItemset(List<Data> listTransaction){
        Map<String,List<String>> transactions = new HashMap<>();
        for(Data item : listTransaction){
            List<String> itemList = new ArrayList<>();
            for (int i = 0; i < data.getDimension().size(); i++) {
                String label = data.getDimension().get(i);
                if(Integer.valueOf(item.getAttribute(label).toString())>0){
                    itemList.add(label);
                }
            }
            transactions.put(item.getAttribute(data.getClassifier()).toString(),itemList);
        }
        List<String> sortedKeys = new ArrayList(transactions.keySet());
        Collections.sort(sortedKeys);
        for(String key : sortedKeys){
            System.out.print(key+" : { ");
            for(int i = 0; i < transactions.get(key).size();i++){
                System.out.print(transactions.get(key).get(i));
                if(i<(transactions.get(key).size()-1)){
                    System.out.print(", ");
                }
            }
            System.out.print(" }\n");
        }
        return transactions;
    }
}

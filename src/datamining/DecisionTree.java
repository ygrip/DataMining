package datamining;

import datamining.Store;
import datamining.Store.Data;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DecisionTree{
    static Store data = new Store();
    static Util system = new Util();
    static Scanner userInput = new Scanner(System.in);
    static List<Map<String,String>> rules = new ArrayList<>();
    static String filePath = "D:\\data\\network.txt";
    static File file = new File(filePath);
    static String directory = file.getParentFile().getAbsolutePath();
    static String openedFile = file.getName();
    static String[] fileName = openedFile.split("\\.");
    
    public static void main(String[] args){      
        data.storeData(system.readData(filePath));
        System.out.println("Data "+fileName[0]+" : \n__________________");
        data.showData(data.getListData());
        System.out.println("");
        
        buildRules();
        showBasicRules();
        convertRulesToNarrative(rearrangeRule());
        checkConsistency();
        System.out.println("");
    }
    
    public static void buildRules(){
        String choosen = "";
       
        while(checkRules()){ 
            List<String> dimension = new ArrayList<>();
            List<Data> processedData =  new ArrayList<>();
            if(rules.isEmpty()){
                dimension.addAll(data.getDimension());
                processedData.addAll(data.getListData());
                for(String key : calculateEntropy(dimension,processedData).keySet()){
                    choosen = key;
                    break;
                }
                List<List<String>> unique =  new ArrayList<>(getUniqueElements(data.createSubSet(choosen)).values());
                
                for(String element : unique.get(0)){
                    String result = "";
                    List<String> results = new ArrayList<>();
                    if(checkElement(choosen,element,processedData)){
                        for(Data attribute : processedData){
                            if(attribute.getAttribute(choosen).equals(element)){
                                result = attribute.getAttribute(data.getClassifier()).toString();
                                break;
                            }
                        }
                    }else{

                        if(dimension.size()<=1){
                            for(Data attribute : processedData){
                                if(attribute.getAttribute(choosen).equals(element)){
                                    results.add(attribute.getAttribute(data.getClassifier()).toString());
                                }
                            }
                            List<String> list = results.stream().distinct().collect(Collectors.toList());

                            for(String s :  list){
                                result = result.concat(s+",");
                            }
                        }else{
                            result = "";
                        }
                    }
                    Map<String,String> decision = new HashMap<>();
                    decision.put("target", choosen);
                    decision.put("value",element);
                    decision.put("result",result);
                    decision.put("parent","");
                    decision.put("children","");
                    rules.add(decision);
                    updateRules();
                }                
            }else{
                dimension.addAll(data.getDimension());
                processedData.addAll(data.getListData());
                for(int i = 0; i<rules.size(); i++){
                    updateRules();
                    Map<String,String> rule = new HashMap<>();
                    rule = rules.get(i);
                    if(rule.get("result").equals("")){
                        choosen = rule.get("target");
                        List<String> removedDimension = new ArrayList<>();
                        List<String> countedElement = new ArrayList<>();
                        String parent = rule.get("index");
                        countedElement.add(rule.get("value"));
                        Map<String,String> checkRule = new HashMap<>();
                        checkRule.putAll(rule);
                        removedDimension.add(checkRule.get("target"));
                        
                        while(!checkRule.get("parent").equals("")){
                            Map<String,String> dummy = new HashMap<>();
                            if(checkRule.get("parent").equals("")){
                                break;
                            }else{                                
                                dummy.putAll(getRule(Integer.valueOf(checkRule.get("parent"))));
                                checkRule.clear();
                                checkRule.putAll(dummy);
                                removedDimension.add(checkRule.get("target"));
                            }
                            
                        }
                        List<String> removedList = removedDimension.stream().distinct().collect(Collectors.toList());

                        dimension.removeAll(removedList);
                        processedData.clear();
                        processedData.addAll(updateData(countedElement,choosen));
                        showUpdatedData(processedData,countedElement,choosen);
                        for(String key : calculateEntropy(dimension,processedData).keySet()){
                            choosen = key;
                            break;
                        }
                        
                        List<List<String>> unique =  new ArrayList<>(getUniqueElements(data.createSubSet(choosen)).values());
                        
                        for(String element : unique.get(0)){
                            Map<String,String> decision = new HashMap<>();
                            String result = "";
                            List<String> results = new ArrayList<>();
                            if(checkElement(choosen,element,processedData)){
                                for(Data attribute : processedData){
                                    if(attribute.getAttribute(choosen).equals(element)){
                                        result = attribute.getAttribute(data.getClassifier()).toString();
                                        break;
                                    }
                                }
                            }else{

                                if(dimension.size()<=1){
                                    for(Data attribute : processedData){
                                        if(attribute.getAttribute(choosen).equals(element)){
                                            results.add(attribute.getAttribute(data.getClassifier()).toString());
                                        }
                                    }
                                    List<String> list = results.stream().distinct().collect(Collectors.toList());

                                    for(String s :  list){
                                        result = result.concat(s+",");
                                    }
                                }else{
                                    result = "";
                                }
                            }

                            decision.put("target", choosen);
                            decision.put("value",element);
                            decision.put("result",result);
                            decision.put("parent",parent);
                            decision.put("children","");
                            rules.add(decision);
                        }
                    }
                    
                }
            }
        }
    }
    
    public static Map<String,Map<String,String>> rearrangeRule(){
        List<Integer> resultedIndex = new ArrayList<>();
        Map<String,Map<String,String>> check = new HashMap<>();
        for(Map<String,String> rule : rules){
            if(!rule.get("result").equals("")){
                resultedIndex.add(Integer.valueOf(rule.get("index")));
            }
        }
        
        for(Integer index : resultedIndex){
            if(check.containsKey(getRule(index).get("result"))){
                Map<String,String> setRule = check.get(getRule(index).get("result"));
                String setChild = "";
                String setParent = getRule(index).get("parent");
                if(setParent.equals("")){
                    setParent = "-1";
                }
                if(setRule.containsKey(setParent)){
                    String[] children = setRule.get(setParent).split(",");
                    for(String s : children){
                        setChild = setChild.concat(s+",");
                    }
                    setChild = setChild.concat(getRule(index).get("index"));
                    setRule.put(setParent, setChild);
                    check.put(getRule(index).get("result"),setRule);
                }else{
                    setChild = getRule(index).get("index");
                    setRule.put(setParent, setChild);
                    check.put(getRule(index).get("result"),setRule);
                }
            }else{
                Map<String,String> setRule = new HashMap<>();
                String setChild = "";
                String setParent = "";
                setParent = setParent.concat(getRule(index).get("parent"));
                if(setParent.equals("")){
                    setParent = "-1";
                }
                setRule.put(setParent,getRule(index).get("index"));
                check.put(getRule(index).get("result"), setRule);
                     
            }
        }
        return check;
    }
    
    public static void rulesPruning(){
         
        List<Map<String,String>> duplicatedRule = new ArrayList<>();
        duplicatedRule.addAll(rules);
        for(Map<String,String> rule : rules){ 
            List<Integer> indexRemoved = new ArrayList<>();
            if(!rule.get("children").equals("")){
                String[] children = rule.get("children").split(",");                
                String[] parent = rule.get("parent").split(",");

                if(children.length>1&&parent.length==1){
                    List<Map<String,String>> newRule = new ArrayList<>();
                    for(String child : children){
                        Map<String,String> check = getRule(Integer.valueOf(child));
                        if(!check.get("result").equals("")){
                            newRule.add(check);
                        }                        
                    }
                    
                    if(newRule.size()>1){
                        int count = 0;
                        String currentResult = newRule.get(0).get("result");
                        for(Map<String,String> checkRule : newRule){
                            if(!checkRule.get("result").equals(currentResult)){
                                count++;
                            }
                        }

                        if(count==0){
                            for(Map<String,String> checkRule : newRule){
                                indexRemoved.add(Integer.valueOf(checkRule.get("index")));
                            }
                            duplicatedRule.get(Integer.valueOf(rule.get("index"))).put("result", currentResult);
                            duplicatedRule.get(Integer.valueOf(rule.get("index"))).put("children", "");
                            
                            for(Integer i : indexRemoved){
                                duplicatedRule.remove(getRule(i));
                            }
                        }
                    }
                    
                }
            }
        }
        
        rules.clear();
        rules.addAll(duplicatedRule);
    }
    
    public static boolean checkConsistency(){
        boolean state = false;
        List<Map<String,String>> checkRule = new ArrayList<>();
        String[] result = {""};
        for(Map<String,String> rule : rules){
            result = rule.get("result").split(",");
            
            if(result.length>1){
                checkRule.add(rule);
            }
        }
        
        boolean change = false;
        int countChange = 0;
        if(!checkRule.isEmpty()){
            state = true;            
            System.out.println("\n\nTerjadi inkonsistensi data");
            for(Map<String,String> rule : checkRule){   
                System.out.print("\naturan berikut ambigu : \n\t\t");
                Map<String,String> statement = new HashMap<>();
                int currentIndex = Integer.valueOf(rule.get("index"));
                do{
                    if(!getRule(currentIndex).get("parent").equals("")){
                        statement.put(getRule(currentIndex).get("target"),getRule(currentIndex).get("value"));
                        currentIndex = Integer.valueOf(getRule(currentIndex).get("parent"));
                    }else{
                        statement.put(getRule(currentIndex).get("target"),getRule(currentIndex).get("value"));
                        break;
                    }
                }while(true);
                
                System.out.print("Jika ");
                int stat = 0;
                result = rule.get("result").split(",");
                for(String key : statement.keySet()){
                    System.out.print(key);
                    System.out.print(" adalah ");
                    System.out.print(statement.get(key));
                    if(stat<(statement.size()-1)){
                        System.out.print(" dan ");
                    }
                    stat++;
                }
                System.out.print(" maka ");
                System.out.print(data.getClassifier());
                System.out.print(" adalah ");
                if(result.length>1){   
                    for(int j = 0; j < result.length; j++){
                        if(j==result.length-1){
                            System.out.print(result[j]);
                        }else{
                            System.out.print(result[j]+" atau "); 
                        }                                
                    }
                }else{
                    System.out.print(rule.get("result"));
                } 
                System.out.println(".");
                
                System.out.println("Apakah anda ingin merubah ? [Y/N]");
                String input = userInput.nextLine();
                if(input.equals("Y")||input.equals("y")){
                    while(!setNewResult(result,rule.get("index"))){
                        
                    }
                    countChange++;
                }
            }
            
        }
        
        if(countChange!=0){
            Integer count = 0;
            for(Map<String,String> rule : checkRule){            
                Map<String,String> statement = new HashMap<>();
                int currentIndex = Integer.valueOf(rule.get("index"));
                do{
                    if(!getRule(currentIndex).get("parent").equals("")){
                        statement.put(getRule(currentIndex).get("target"),getRule(currentIndex).get("value"));
                        currentIndex = Integer.valueOf(getRule(currentIndex).get("parent"));
                    }else{
                        statement.put(getRule(currentIndex).get("target"),getRule(currentIndex).get("value"));
                        break;
                    }
                }while(true);
                for(Data attribute : data.getListData()){
                    int status = 0;
                    for(String key :  statement.keySet()){
                        if(attribute.getAttribute(key).equals(statement.get(key))){
                            status++;
                        }
                    }
                    
                    if(status==statement.size()){
                        if(!attribute.getAttribute(data.getClassifier()).equals(rule.get("result"))){
                            count++;
                        }
                    }                   
                }
            }
            rulesPruning();
            showBasicRules();            
            convertRulesToNarrative(rearrangeRule());
            
            Integer total = data.getListData().size();
            Double percentage = (Double.valueOf(count.toString())/Double.valueOf(total.toString()))*100.0;
            percentage = Math.round(percentage * 100.0) / 100.0;
            System.out.println("\n\nProsentase error pada data adalah : "+percentage+"%");
        }
        
        return state;
    }
    
    private static boolean setNewResult(String[] option, String index){
        System.out.println("Pilih nilai baru untuk "+data.getClassifier()+" : \n");
        
        boolean selectClassifier = false;
        for(String label : option){           
            System.out.print("================");
        }
        System.out.print("\n|");
        for(String label : option){
            System.out.print("\t"+label+"\t|");       
        }
        System.out.println("");
        for(String label : option){           
            System.out.print("================");
        }
        System.out.print("\n\nPilihan anda : ");
 
        String input = userInput.nextLine();
           
        for(String label : option){
            if(input.toLowerCase().equals(label.toLowerCase())){
                selectClassifier = true;
                for(Map<String,String> rule :  rules){
                    if(rule.get("index").equals(index)){
                        rule.put("result", label);
                    }
                }
            }
        }
            
        return selectClassifier;
    }
   
    public static Map<String,String> getRule(Integer index){
        Map<String,String> result = new HashMap<>();
        for(Map<String,String> rule : rules){
            if(rule.get("index").equals(index.toString())){
                result.putAll(rule);
                break;
            }
        }
        return result;
    }
    
    public static List<String> convertRulesToNarrative(Map<String,Map<String,String>> check){
        List<String> sentence = new ArrayList<>();
        for(String result : check.keySet()){
            List<String> reversed = new ArrayList<>();
            Map<String,String> rule = check.get(result);
            
            reversed.add(". ");
            String[] subResult = result.split(",");
            if(subResult.length>1){                            
                for(int j = 0; j < subResult.length; j++){
                    reversed.add(subResult[j]);
                    if(j<(subResult.length-1)){
                        reversed.add(" atau ");                                 
                    }                                
                }
                reversed.add(" (terjadi inkonsistensi data) ");
            }else{
                reversed.add(result);
            }
            reversed.add(" adalah ");
            reversed.add(data.getClassifier());
            reversed.add(" maka ");
            int atau = 0;
            for(String parent : rule.keySet()){
                String[] children = rule.get(parent).split(",");
                List<String> clear = new ArrayList<>();
                for(int i = 0; i<children.length; i++){
                    if(!children[i].equals("")){
                        clear.add(children[i]);
                    }
                }
                if(atau>0){
                    reversed.add(" atau ");
                }
                if(clear.size()>1){                            
                    for(int j = 0; j < clear.size(); j++){
                        Integer currentIndex = Integer.valueOf(clear.get(j));
                        do{                            
                            if(!getRule(currentIndex).get("parent").equals("")){
                                reversed.add(getRule(currentIndex).get("value"));
                                reversed.add(" adalah ");
                                reversed.add(getRule(currentIndex).get("target"));
                                reversed.add(" dan ");
                                currentIndex = Integer.valueOf(getRule(currentIndex).get("parent"));
                            }else{
                                reversed.add(getRule(currentIndex).get("value"));
                                reversed.add(" adalah ");
                                reversed.add(getRule(currentIndex).get("target"));
                                break;
                            }
                        }while(true);
                        if(j<(clear.size()-1)){
                            reversed.add(" atau ");                                 
                        }                                
                    }
                }else{
                    Integer currentIndex = Integer.valueOf(clear.get(0));
                    do{                            
                        if(!getRule(currentIndex).get("parent").equals("")){
                            reversed.add(getRule(currentIndex).get("value"));
                            reversed.add(" adalah ");
                            reversed.add(getRule(currentIndex).get("target"));
                            reversed.add(" dan ");
                            currentIndex = Integer.valueOf(getRule(currentIndex).get("parent"));
                        }else{
                            reversed.add(getRule(currentIndex).get("value"));
                            reversed.add(" adalah ");
                            reversed.add(getRule(currentIndex).get("target"));
                            break;
                        }
                    }while(true);
                }
                atau++;
            }
            reversed.add("\nJika ");
            String collect = "";
            for(int i = reversed.size()-1; i >=0; i--){
                collect = collect.concat(reversed.get(i));
            }
            
            sentence.add(collect);
        }
        
        for(String rule : sentence){
            System.out.print(rule);
        }
        return sentence;
    }
    
    public static String convertRulesToAlgorithm(){
        String algorithm = "";
        
        return algorithm;
    }
    
    public static void showBasicRules(){
        System.out.println("\nAturan : \n__________________\n");
        for(int i = 0; i<rules.size(); i++){
            Map<String,String> element = rules.get(i);
            System.out.print("Node #"+rules.get(i).get("index")+" : \n\t{");
            for(String key : element.keySet()){
                System.out.print("{["+key+"]:["+element.get(key)+"]}, ");
            }
            System.out.print("}\n");
        }
    }
    
    public static void showUpdatedData(List<Data> processedData, List<String> keep, String choosen){
        System.out.print("\nUpdate data "+fileName[0]+" terhadap "+choosen+" = ");
        for(int i = 0; i<keep.size(); i++){
            if(i==(keep.size()-1)){
                if(keep.size()!=1){
                    System.out.print(" dan "+keep.get(i)+" ");
                }else{
                    System.out.print(" "+keep.get(i)+" "); 
                }
            }else{
                System.out.print(keep.get(i)+", ");
            }
        }
        System.out.print(" : \n__________________\n");
        data.showData(processedData);
        System.out.println("");
    }
    
    public static List<Data> updateData(List<String> countedElement, String choosen){
        List<Data> updateData = new ArrayList<>();
//        System.out.println("kolom : "+choosen);
        for(Data data : data.getListData()){
            for(String val : countedElement){
                
//                System.out.println("nilai : "+val);
                if(data.getAttribute(choosen).equals(val)){
                    updateData.add(data);
                }
            }
        }
        Set<Data> removeDup = new HashSet<>();
        removeDup.addAll(updateData);
        updateData.clear();
        updateData.addAll(removeDup);
        return updateData;
    }
    
    public static boolean checkRules(){
        boolean result = false;
        if(rules.isEmpty()){
            result = true;
        }else{
            int unfinished = 0;
            for(int i = 0; i<rules.size(); i++){
                Map<String,String> rule = rules.get(i);
                if(rule.get("children").equals("")&&rule.get("result").equals("")){
                    unfinished++;
                }
            }
            if(unfinished!=0){
                result = true;
            }
        }
        return result;
    }
    
    public static void updateRules(){
        Map<String,List<String>> hasChild = new HashMap<>();
        for(Integer i = 0; i<rules.size(); i++){
            Map<String,String> rule = rules.get(i);
            
            if(!rule.get("parent").equals("")){
                if(hasChild.containsKey(rule.get("parent"))){
                    List<String> member = new ArrayList<>();
                    for(String child : hasChild.get(rule.get("parent"))){
                        member.add(child);
                    }
                    member.add(i.toString());
                    hasChild.put(rule.get("parent"), member);
                }else{
                    List<String> member = new ArrayList<>();
                    member.add(i.toString());
                    hasChild.put(rule.get("parent"), member);
                }
            }
            rules.get(i).put("index",i.toString());
        }
        
        for(String key : hasChild.keySet()){
            String children = "";
            for(String c : hasChild.get(key)){
                children = children.concat(c+",");
            }
            rules.get(Integer.valueOf(key)).put("children", children);
        }
    }
    
    public static Map<String,Double> calculateEntropy(List<String> checkEntropy, List<Data> processedData){
        Map<String,Double> listEntropy = new HashMap<>();
        for(String dimensi : checkEntropy){
            System.out.println("\nMenghitung kolom "+dimensi+" : \n________________");
            Map<String,List<String>> unique =  new HashMap<>();
            List<Data> newData = new ArrayList<>();
            unique = getUniqueElements(data.createSubSet(dimensi,data.getClassifier()));

            newData = countProbability(unique,processedData);

            List<String> labels = new ArrayList<>();
            for(String key : newData.get(0).getAttribute().keySet()){
                labels.add(key);
            }

            data.showData(labels, newData);
            Double entropy = countEntropy(unique,processedData);
            listEntropy.put(dimensi, entropy);
            System.out.println("Entropy "+dimensi+" : "+entropy);
        }
        return sortByValue(listEntropy);
    }
    
    public static boolean checkElement(String key, String value, List<Data> processedData){
        List<String> elements = new ArrayList<>();
        for(Data attribute : processedData){
            if(attribute.getAttribute(key).equals(value)){
                elements.add(attribute.getAttribute(data.getClassifier()).toString());
            }
        } 
        List<String> listDistinct = elements.stream().distinct().collect(Collectors.toList());
        
        if(listDistinct.size()==1){
            return true;
        }else{
            return false;
        }
    }
    
    public static List<Data> createProbability(Map<String,List<String>> listData){
        List<Data> result = new ArrayList<>();
        List<String> allPremise = new ArrayList<>();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        
        int i =0;
        for(String key : listData.keySet()){
            if(i==0){
                for(String data : listData.get(key)){
                    list1.add(key+":"+data);
                }
            }else{
               for(String data : listData.get(key)){
                    list2.add(key+":"+data);
                }
            }
            i++;
        }
        
        for(String condition1 : list1){
            for(String condition2 : list2){
                allPremise.add(condition1+","+condition2);
            }
        }
        
        for(String premises : allPremise){
            String[] combination = premises.split(",");
            Data element = new Store().new Data();
            for(String data : combination){
                String[] identify = data.split(":");
                element.setAttribute(identify[0],identify[1]);
            }
            result.add(element);
        }
        
        
        
        return result;
    }
    
    public static List<Data> countProbability(Map<String,List<String>> listData, List<Data> primary){
        List<Data> result = new ArrayList<>();
        
        result = createProbability(listData);
        
        for(Data dataCheck : result){
            int count = 0;
            for(Data dataPrimary : primary){
                boolean similar = true;
                for(String key : dataCheck.getAttribute().keySet()){
                    if(!dataCheck.getAttribute(key).toString().equals(dataPrimary.getAttribute(key).toString())){
                        similar = false;
                    }
                }
                if(similar){
                    count++;
                }
            }
            dataCheck.setAttribute("Jumlah", count);            
        }
        
        return result;
    }

    public static Double countEntropy(Map<String,List<String>> listData, List<Data> primary){
        Double entropy = 0.0;
        List<List<String>> unique =  new ArrayList<>(getUniqueElements(data.createSubSet(data.getClassifier())).values());
        Map<String,Double> listEntropy = new HashMap<>();        
        Map<String,Integer> countElement = new HashMap<>();
        List<Data> premises = new ArrayList<>();
        premises = countProbability(listData,primary);

        Integer allMember  = 0;
        for(Data data : premises){
            allMember += Integer.valueOf(data.getAttribute("Jumlah").toString());
        }
        
        for(String key : premises.get(0).getAttribute().keySet()){
            if(!key.equals(data.getClassifier())&&!key.equals("Jumlah")){
                for(Data data : premises){
                    if(countElement.containsKey(data.getAttribute(key).toString())){
                        Integer newVal = countElement.get(data.getAttribute(key).toString()) + Integer.valueOf(data.getAttribute("Jumlah").toString());       
                        countElement.put(data.getAttribute(key).toString(), newVal);
                    }else{
                        countElement.put(data.getAttribute(key).toString(), Integer.valueOf(data.getAttribute("Jumlah").toString()));
                    }
                    
                }
            }
        }
        
        for(String check : countElement.keySet()){
            Double value = 0.0;
            for(String key : premises.get(0).getAttribute().keySet()){
                if(!key.equals(data.getClassifier())&&!key.equals("Jumlah")){
                   for(Data attribute : premises){
                        if(attribute.getAttribute(key).equals(check)){
                            Integer currentProb = Integer.valueOf(attribute.getAttribute("Jumlah").toString());
                            Integer sameProb = countElement.get(attribute.getAttribute(key).toString());
                            Double prob = Double.valueOf(currentProb.toString())/Double.valueOf(sameProb.toString());
                            value += countLog(prob);
//                            System.out.println(currentProb+"/"+sameProb+"="+value);
                       }
                   }
                }
            }
            value *= -1;
            listEntropy.put(check, value);
        }
        
        for(String key : countElement.keySet()){
            Double probability = Double.valueOf(countElement.get(key).toString())/Double.valueOf(allMember.toString());
            Double q = Double.valueOf(listEntropy.get(key).toString());
            entropy += (probability)*q;
        }
        return Math.round(entropy * 100.0) / 100.0;
    }
    
    public static Double countLog(Double prob){
        if(prob==0){
            return 0.0;
        }else{
            return prob*(Math.log(prob)/Math.log(2));
        }
    }
    
    public static Map<String,List<String>> getUniqueElements(List<Data> listData){
        Map<String,List<String>> mappedElements = new HashMap<>();
        
        for(String label : listData.get(0).getAttribute().keySet()){
            List<String> elements = new ArrayList<>();
            for(Data data : listData){
                elements.add(data.getAttribute(label).toString());
            }
            List<String> listDistinct = elements.stream().distinct().collect(Collectors.toList());
            mappedElements.put(label, listDistinct);
        }
        
        return mappedElements;
    }
    
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                  .stream()
                  .sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
                  .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue, 
                    (e1, e2) -> e1, 
                    LinkedHashMap::new
                  ));
    }
}

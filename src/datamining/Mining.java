/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining;

import datamining.Store.Data;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author user
 */
public class Mining{
    static Store data = new Store();
    static Util system = new Util();
    static Scanner userInput = new Scanner(System.in);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        // TODO code application logic here
        String filePath = "D:\\data\\data.csv";
//        data.storeData(system.readData(filePath));
//        data.showData(data.getListData());
               
//        System.out.println(data.getListData().size());
//        data.mappingData();
//        Map<String,List<Data>> mappedData = new HashMap<>();
//        mappedData = data.getMappedData();
//        System.out.println(mappedData.size()); 
//        
//        for(String key : mappedData.keySet()){
//            List<Data> classMember = new ArrayList<>();
//            classMember = mappedData.get(key);
//            System.out.println("Kelas "+data.getClassifier()+" = "+key+ " :"); 
//            System.out.println("Jumlah anggota "+data.getClassifier()+" = "+key+ " : "+classMember.size());
//
//            data.showData(classMember);
//        }
//        
//        String filePath = "C:\\Users\\user\\Downloads\\Documents\\newthyroid.txt";
//        normalisasi(filePath,'\t',"A","B","C","D","E","CLASS");
          normalisasi(filePath);
        
    }
    
    public static void normalisasi(String filePath) throws IOException{
        normalisasi(filePath,',',true,true,new String[] {});
    }
    
    public static void normalisasi(String filePath,String... labels) throws IOException{
        normalisasi(filePath,',',true,true,labels);
    }
    
    public static void normalisasi(String filePath, char separator, String... labels) throws IOException{
        normalisasi(filePath,separator,true,true,labels);
    }
    
    public static void normalisasi(String filePath, char separator, boolean read, String... labels) throws IOException{
        normalisasi(filePath,separator,read,true,labels);
    }
    
    public static void normalisasi(String filePath, char separator, boolean read, boolean write, String ...labels) throws IOException{
        String path = filePath;
        String savedName = "";
        
        File file = new File(path);
        String directory = file.getParentFile().getAbsolutePath();
        String openedFile = file.getName();
        String unique = system.getCurrentTime().concat(".csv");
        String[] fileName = openedFile.split("\\.");
        
        directory = directory.concat("\\Mining\\").concat(fileName[0]);
        File checkDir = new File(directory);
        if (! checkDir.exists()){
            checkDir.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        
        Normalize processedData = new Normalize();
        processedData.storeData(system.readData(path,separator, true),labels);
        
        System.out.println("\nData "+fileName[0]+" awal : "
                + "\n___________________");
        System.out.println("Keterangan : ");
        for(String label : processedData.getDimension()){
            List<Double> column = new ArrayList<>();
            for(Object data : processedData.getColumn(label)){
                column.add(Double.valueOf(data.toString()));
            }
            System.out.println("\t\tKolom "+label
                    +" terkecil => "+processedData.getMin(column)
                    +" : terbesar => "+processedData.getMax(column));
        }
        System.out.println("___________________");
        processedData.showData(processedData.getLabel(),processedData.getListData());
        
        System.out.println("\nNormalisasi "+fileName[0]+" min - max : "
                + "\n___________________");
        Double min = 0.0;
        Double max = 1.0;
        System.out.println("Keterangan : ");
        for(String label : processedData.getDimension()){
            List<Double> column = new ArrayList<>();
            for(Data data : processedData.normalizeMinMax(min,max)){
                column.add(Double.valueOf(data.getAttribute(label).toString()));
            }
            System.out.println("\t\tKolom "+label
                    +" terkecil => "+processedData.getMin(column)
                    +" : terbesar => "+processedData.getMax(column));
        }
        System.out.println("___________________");
        processedData.showData(processedData.getLabel(), processedData.normalizeMinMax(min,max));
        savedName = directory.concat("\\").concat("min_max").concat(unique);
        write(savedName,processedData.convertToString(processedData.normalizeMinMax(min,max)));
        
        System.out.println("\nNormalisasi "+fileName[0]+" Z-Score : "
                + "\n___________________");
        System.out.println("Keterangan : ");
        for(String label : processedData.getDimension()){
            List<Double> column = new ArrayList<>();
            for(Data data : processedData.normalizeZScore()){
                column.add(Double.valueOf(data.getAttribute(label).toString()));
            }
            System.out.println("\t\tKolom "+label
                    +" terkecil => "+processedData.getMin(column)
                    +" : terbesar => "+processedData.getMax(column));
        }
        System.out.println("___________________");
        processedData.showData(processedData.getLabel(), processedData.normalizeZScore());
        savedName = directory.concat("\\").concat("z_score").concat(unique);
        write(savedName,processedData.convertToString(processedData.normalizeZScore()));
        
        System.out.println("\nNormalisasi "+fileName[0]+" Decimal Scaling : "
                + "\n___________________");
        System.out.println("Keterangan : ");
        for(String label : processedData.getDimension()){
            List<Double> column = new ArrayList<>();
            for(Data data : processedData.normalizeDecimalScaling()){
                column.add(Double.valueOf(data.getAttribute(label).toString()));
            }
            System.out.println("\t\tKolom "+label
                    +" terkecil => "+processedData.getMin(column)
                    +" : terbesar => "+processedData.getMax(column));
        }
        System.out.println("___________________");
        processedData.showData(processedData.getLabel(), processedData.normalizeDecimalScaling());
        savedName = directory.concat("\\").concat("dec_scaling").concat(unique);
        write(savedName,processedData.convertToString(processedData.normalizeDecimalScaling()));
        
        System.out.println("\nNormalisasi "+fileName[0]+" Sigmoidal : "
                + "\n___________________");
        System.out.println("Keterangan : ");
        for(String label : processedData.getDimension()){
            List<Double> column = new ArrayList<>();
            for(Data data : processedData.normalizeSigmoidal()){
                column.add(Double.valueOf(data.getAttribute(label).toString()));
            }
           System.out.println("\t\tKolom "+label
                    +" terkecil => "+processedData.getMin(column)
                    +" : terbesar => "+processedData.getMax(column));
        }
        System.out.println("___________________");
        processedData.showData(processedData.getLabel(), processedData.normalizeSigmoidal());
        savedName = directory.concat("\\").concat("sigmoidal").concat(unique);
        write(savedName,processedData.convertToString(processedData.normalizeSigmoidal()));
        
        System.out.println("\nNormalisasi "+fileName[0]+" SoftMax : "
                + "\n___________________");
        Integer x = 10;
        System.out.println("Keterangan : ");
        for(String label : processedData.getDimension()){
            List<Double> column = new ArrayList<>();
            for(Data data : processedData.normalizeSoftMax(x)){
                column.add(Double.valueOf(data.getAttribute(label).toString()));
            }
            System.out.println("\t\tKolom "+label
                    +" terkecil => "+processedData.getMin(column)
                    +" : terbesar => "+processedData.getMax(column));
        }
        System.out.println("___________________");
        processedData.showData(processedData.getLabel(), processedData.normalizeSoftMax(x));
        savedName = directory.concat("\\").concat("soft_max").concat(unique);
        write(savedName,processedData.convertToString(processedData.normalizeSoftMax(x)));

//        for(String label : processedData.getDimension()){
//            List<Double> listColumn = new ArrayList<>();
//            for(Object data : processedData.getColumn(label)){
//                listColumn.add(Double.valueOf(data.toString()));
//            }
//            System.out.println("Rata-rata kolom "+label+" : "+processedData.countMean(listColumn));
//            System.out.println("Standar deviasi kolom "+label+" : "+processedData.countDeviation(listColumn));
//        }
    }
    
    public static void write(String savedName,List<List<String>> data) throws IOException{
        FileWriter writer = new FileWriter(savedName);
        for(List<String> values : data){
            system.writeLine(writer, values);
        }
        writer.flush();
        writer.close();
    }
    
    public static void userInteraction(){
        String userAnswer;
        do{
            int x = printMenu();
            
            if(x==0){
                error();
            }
            System.out.flush();
            System.out.println("Kembali ke menu utama, jika tidak program akan keluar (Y/N) ?");
            userAnswer = userInput.next();
        }while(userAnswer.equals("Y")||userAnswer.equals("y"));
    }
    
    public static void error(){
        System.out.println("Maaf input anda salah");
    }
    
    public static int printMenu(){
        system.clearScreen();
        List<String> listMenu = new ArrayList<>();
        listMenu.add("Tampilkan data");
        listMenu.add("Tampilkan mapping data");
        listMenu.add("Normalisasi data");
        listMenu.add("Reset data");

        int i = 0;
        System.out.println("Main Menu : ");
        for(String menu : listMenu){
            i++;
            System.out.println(i+".\t"+menu);
        }
        System.out.print("\nMasukkan input anda {1 - "+i+") : ");
     
        int x = userInput.nextInt();
        if(x<=0||x>i){
            x = 0;
        }
        return x;
    }
    
}

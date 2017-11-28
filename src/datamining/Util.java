/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author user
 */
public class Util {
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';    
    private static final boolean CHECK_STATUS = false;
    private String openedFile = "";

    public String getOpenedFile() {
        return openedFile;
    }

    public void setOpenedFile(String openedFile) {
        this.openedFile = openedFile;
    }
    
    public List<List<String>> readData(String filePath){
        return readData(filePath,DEFAULT_SEPARATOR,DEFAULT_QUOTE,CHECK_STATUS);
    }
    
    public List<List<String>> readData(String filePath, boolean check){
        return readData(filePath,DEFAULT_SEPARATOR,DEFAULT_QUOTE,check);
    }
    
    public List<List<String>> readData(String filePath, char separators){
        return readData(filePath,separators,DEFAULT_QUOTE,CHECK_STATUS);
    }
    
    public List<List<String>> readData(String filePath, char separators, boolean check){
        return readData(filePath,separators,DEFAULT_QUOTE,check);
    }
    
    public List<List<String>> readData(String filePath, char separators, char customQuote, boolean check){
        
        List<List<String>> data = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filePath));

            while(scanner.hasNext()){
                List<String> row = parseLine(scanner.nextLine(),separators,customQuote);
                data.add(row);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(check){
            this.openedFile = statusRead(filePath,data);
        }
        return data;
    }
    
    private  List<String> parseLine(String row) {
        return parseLine(row, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    private  List<String> parseLine(String row, char separators) {
        return parseLine(row, separators, DEFAULT_QUOTE);
    }

    private  List<String> parseLine(String row, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (row == null && row.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer currentRow = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = row.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            currentRow.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        currentRow.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        currentRow.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        currentRow.append('"');
                    }

                } else if (ch == separators) {

                    result.add(currentRow.toString());

                    currentRow = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    currentRow.append(ch);
                }
            }

        }

        result.add(currentRow.toString());

        return result;
    }
    
    private String statusRead(String path, List<List<String>> rawData){
        String[] checkPath = path.split(":?\\\\");
        String file[] = checkPath[(checkPath.length-1)].split("\\.");
        String fileName = "";
        String fileType = "";
        for(int i = 0; i<file.length; i++){
            String name = file[i];
            if(i<file.length-1){
                fileName = fileName.concat(name);
            }else{
                fileType = name;
            }
        }
        if(rawData.isEmpty()){
            System.out.println("Gagal membaca file");
        }else{
            System.out.println("Berhasil membaca file : "+fileName+"."+fileType);
        }
        
        return fileName;
    }
    
    
    public void clearScreen(){
        System.out.print("\u001b[2J");
        System.out.flush();
        try {
            Robot pressbot = new Robot();
            pressbot.keyPress(17); // Holds CTRL key.
            pressbot.keyPress(76); // Holds L key.
            pressbot.keyRelease(17); // Releases CTRL key.
            pressbot.keyRelease(76); // Releases L key.
        } catch (AWTException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getCurrentTime(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());  
        String check = timestamp.toString().replaceAll("\\.", "");
        check = check.replaceAll(":", "");
        return check.replaceAll("\\s", "_");
    }
    
    public void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }

    public void writeLine(Writer w, List<String> values, char separators) throws IOException {
        writeLine(w, values, separators, ' ');
    }

    //https://tools.ietf.org/html/rfc4180
    private String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }

    public void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());


    }
}

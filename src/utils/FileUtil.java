package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class FileUtil {
    public static List<String> readFile(String filePath){
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = null;
            while( (line = br.readLine()) != null ){
                lines.add(line);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            return lines;
        }
    }

    public static Set<String> readFileAsSet(String filePath){
        Set<String> lines = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = null;
            while( (line = br.readLine()) != null ){
                lines.add(line);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            return lines;
        }
    }

    public static List<String> readFile(InputStream is){
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while( (line = br.readLine()) != null ){
                lines.add(line);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            return lines;
        }
    }

    public static String readFileAsALine(InputStream is){
        List<String> lines = readFile(is);
        StringBuffer buffer = new StringBuffer();
        for(String line : lines){
            buffer.append(line);
        }
        return buffer.toString();
    }

    public static void writeFile(List<String> lines, String filePath){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            for(String line : lines){
                bw.write(line+"\n");
            }
            bw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void writeFile(List<String> lines, String filePath, boolean ifContinueWrite){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, ifContinueWrite));
            for(String line : lines){
                bw.write(line+"\n");
            }
            bw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

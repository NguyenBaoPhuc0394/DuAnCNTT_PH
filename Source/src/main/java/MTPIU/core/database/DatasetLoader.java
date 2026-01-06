package MTPIU.core.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasetLoader {
    
    public static Database loadDatabase(String path){
        Database db = null;
        Map<Integer, Map<String, Double>> probMap = new HashMap<>();
        List<Transaction> trans = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            while((line = br.readLine()) != null){
                String[] parts = line.split("\\s+");
                int ts = Integer.parseInt(parts[0]);
                List<UncertainItem> items = new ArrayList<>();
                Map<String, Double> itemProbs = new HashMap<>(); 
                for(int i = 1; i<parts.length; i++){
                    UncertainItem uncertainItem = getUncertainItem(parts[i]);
                    if(uncertainItem != null){
                        items.add(uncertainItem);
                        itemProbs.put(uncertainItem.getItem(), uncertainItem.getProbability());
                        probMap.put(ts, itemProbs);
                    }
                }
                trans.add(new Transaction(ts, ts, items));
                items = null;
            }
            if(trans.size()>0){
                int minTs = trans.get(0).getTimestamp();
                int maxTs = trans.get(trans.size()-1).getTimestamp();
                return new Database(trans, probMap, minTs, maxTs);
            }
            
        }catch(FileNotFoundException e){
            System.out.println("File not found");
            System.err.println(e);
        }catch(IOException ioe){
            System.out.println("File error");
            System.err.println(ioe);
        }
        catch(NumberFormatException nfe){
            System.out.println("NumberFormatException");
            System.err.println(nfe);
            // System.out.println(nfe.getMessage());
        }

        return db;
    }

    // Lấy một đối tượng là một item bao gồm tên và xác suất của nó.
    public static UncertainItem getUncertainItem(String input){
        int idx1 = input.indexOf("(");
        int idx2 = input.lastIndexOf(")");
        String item = input.substring(0, idx1);
        String prop = input.substring(idx1 + 1, idx2);
        double propability;
        try{
            propability = Double.parseDouble(prop);
        }catch(NumberFormatException e){
            return null;
        }
        return new UncertainItem(item, propability);
    }
}

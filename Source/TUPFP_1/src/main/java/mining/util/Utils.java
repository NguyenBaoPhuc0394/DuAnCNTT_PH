package main.java.mining.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import main.java.mining.config.Parameters;
import main.java.mining.model.Transaction;
import main.java.mining.model.UncertainItem;

public class Utils {

    static Scanner sc = new Scanner(System.in);
    
    // Kiểm tra giá trị input đầu vào bao gồm K và maxPer có hợp lệ không
    // Các giá trị phải là số nguyên và lớn hơn 0
    public static boolean checkInputs(String topKInput, String maxPerInput){
        try{
            int K = Integer.parseInt(topKInput);
            double maxExpPer = Double.parseDouble(maxPerInput);
            if(K <= 0 || maxExpPer <= 0){
                return false;
            }
        }catch(NumberFormatException e){
            return false;
        }
        return true;
    }

    // Xử lý các giá trị đầu vào không hợp lệ, nếu giá trị không hợp lệ, yêu cầu nhập lại.
    // Nếu người dùng không nhập đúng định dạng thì yêu cầu người dùng nhập lại.
    public static Parameters inputHandler(){
        boolean valid = true;
        String K;
        String maxExpPer;
        do{
            valid = true;
            System.out.print("Please enter Top-K value: ");
            K = sc.nextLine();
            System.out.print("Please enter maxPer value: ");
            maxExpPer = sc.nextLine();
            valid = Utils.checkInputs(K, maxExpPer);
        }while(!valid);
        return new Parameters(Integer.parseInt(K), Double.parseDouble(maxExpPer));
    } 

    // Load dữ liệu từ file database lên, lưu vào một cấu trúc dữ liệu để dễ dàng truy xuất.
    public static List<Transaction> loadDatabase(String path){
        /// Hàm này sẽ đọc file và xử lý các lỗi có thể xảy ra
        /// Kết quả sẽ được sử dụng để lưu vào các class như Transaction và UncertainItem
        /// Để sau này bước Scanner không cần phải đọc từ file mà chỉ cần sử dụng kết quả của hàm này để tìm các item đủ điều kiện.
        List<Transaction> result = new ArrayList<>();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while((line = reader.readLine()) != null){
                String[] items = line.split("\\s+");
                List<UncertainItem> uncertainItems = new ArrayList<>();
                for(int i = 1; i<items.length ; i++){
                    UncertainItem uncertainItemsValue = getUncertainItem(items[i]); 
                    if(uncertainItemsValue != null){
                        uncertainItems.add(uncertainItemsValue);
                    }
                }

                result.add(new Transaction(Integer.parseInt(items[0]), Integer.parseInt(items[0]), uncertainItems));
                uncertainItems = null;
            }
            reader.close();
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
        
        return result;
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

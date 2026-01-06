import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Sinh xác suất ngẫu nhiên cho các item trong tập dữ liệu.
 */
public class ProbabilityGenerator {

    private static List<List<String>> trans = new ArrayList<>();
    private static Map<String, Integer> freq = new HashMap<>();

    private static void readInputFile(String path){
        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            while((line = br.readLine()) != null){
                String[] parts = line.split("\\s+");
                List<String> tran = new ArrayList<>();
                for(String part : parts){
                    tran.add(part);
                    freq.put(part, freq.getOrDefault(part, 0)+1);
                }
                trans.add(tran);
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }
    private static void generateProb(String dist, String output){
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(output)))
        {
            if(dist.equals("uniform")){
                Random rand = new Random();
                int tranId = 1;
                for(List<String> tran : trans){
                    bw.write(tranId+" ");
                    for(String item : tran){
                        double prob = rand.nextDouble();
                        bw.write(item + "(" + String.format("%.2f", prob) + ")  ");
                    }
                    bw.newLine();
                    tranId++;
                }
            }
            else if(dist.equals("frequency")){
                int totalFrequency = freq.values().stream().mapToInt(Integer::intValue).sum();
                int tranId = 1;
                for (List<String> tran : trans) {
                    bw.write(tranId + " ");
                    for (String item : tran) {
                        double prob = (double) freq.get(item) / totalFrequency;
                        bw.write(item + "(" + String.format("%.2f", prob) + ")  ");
                    }
                    bw.newLine();
                    tranId++;
                }
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    private static String getOutputName(String dist, String input){
        String[] parts = input.split("/");
        String[] inp = parts[parts.length-1].split("\\.");
        String outputName = "../data/uncertain/" + inp[0] + "_" + "." + inp[1];
        return outputName;
    }

    public static void main(String[] args){
        String input = "../data/raw/mushrooms.txt";
        String distribution = "uniform"; //hoặc có thể dùng frequency
        String output = getOutputName(distribution, input);
        readInputFile(input);
        generateProb(distribution, output);
    }
}

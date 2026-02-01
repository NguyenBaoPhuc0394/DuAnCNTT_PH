package TKPIU.core.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import TKPIU.core.pattern.Pattern;

/**
 * Thực hiện ghi kết quả top-K patterns vào file output
 */
public class PatternWriter {
    public static void writePatternsToFile(List<Pattern> patterns, String inputFile, int k, double alpha, double runtime) {
        String[] parts = inputFile.split("/"); 
        String fileName = parts[parts.length - 1]; 

        String outputFile = "data/output/" + fileName;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            bw.write("K = "+k + ", alpha = "+alpha+", beta= "+(1-alpha)+", runtime = "+runtime);
            bw.newLine();
            for (Pattern p : patterns) {
                bw.write(p.toString());
                bw.newLine();
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }

        System.out.println("Patterns has been written to: " + outputFile);
    }

}

package main.java.MTPIU.core.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import main.java.MTPIU.core.pattern.Pattern;

public class PatternWriter {
    public static void writePatternsToFile(List<Pattern> patterns, String inputFile) {
        String[] parts = inputFile.split("/"); 
        String fileName = parts[parts.length - 1]; 

        String outputFile = "data/output/" + fileName;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            for (Pattern p : patterns) {
                bw.write(p.toString());
                bw.newLine();
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }

        System.out.println("Patterns đã được ghi ra file: " + outputFile);
    }

}

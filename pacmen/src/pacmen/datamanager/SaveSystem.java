package pacmen.datamanager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveSystem {
    // writes a list of strings to data/data.txt
    public static void saveLines(String filePath, List<String> data) {
        File file = new File(filePath);
        
        // ensures dir exists
        file.getParentFile().mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String line: data) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            // for debug
            System.err.println("SaveSystem error: Could not write to " + filePath);
            e.printStackTrace();
        }
    }

    // read a file and return its contents as a List of Strings
    public static List<String> loadLines(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return lines;   // returns empty list if file doesn't exist
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            // for debug
            System.err.println("SaveSystem error: Could not read from " + filePath);
            e.printStackTrace();
        }
        return lines;
    }
}

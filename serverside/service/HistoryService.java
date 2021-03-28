package serverside.service;

import serverside.interfaces.History;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryService implements History {

    @Override
    public void SaveHistory(List<String> arr) {
        if (arr != null) {
            Collections.reverse(arr);
            for (String s : arr) {
                if (s != null) {
                    try (FileWriter writer = new FileWriter("History.txt", true)) {
                        writer.append(s.trim());
                        writer.append('\n');
                        writer.flush();
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public List<String> loadHistory() {
        List<String> arr = new ArrayList<>();
        try {
            File file = new File("History.txt");
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line = reader.readLine();
            while (line != null) {
                if(arr.size() == 100){
                    return arr;
                }
                arr.add(line.trim());
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arr;
    }

    @Override
    public List<String> Censor() {
        List<String> arr = new ArrayList<>();
        try {
            File file = new File("censor.txt");
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line = reader.readLine();
            while (line != null) {
                arr.add(line.trim());
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arr;
    }
}


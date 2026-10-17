package com.arnas.data.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author ARNAS_
 */
public class DataLoader {
    
    public String[] loadFileToLine(File file) throws FileNotFoundException, IOException {
        ArrayList<String> T = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
            T.add(sCurrentLine);
        }
        return T.toArray(new String[T.size()]);
    }
    
    public ArrayList<String[]> loadFileToWord(File file, String separator) throws FileNotFoundException, IOException {
        ArrayList<String[]> S = new ArrayList<>();
        String[] temp = loadFileToLine(file);
        for (String t : temp) {
            S.add(t.split(separator));
        }
        return S;
    }
    
    public ArrayList<String[]> parseLineToWord(String[] S, String separator) {
        ArrayList<String[]> T = new ArrayList<>();
        for (String s : S) {
            T.add(s.split(separator));
        }
        return T;
    }
    
    public String[] splitLastWord(ArrayList<String[]> S) {
        String[] T = new String[S.size()];
        for (int i = 0; i < S.size(); i++) {
            String[] temp = S.remove(i);
            String[] t = new String[temp.length-1];
            int j;
            for (j = 0; j < t.length; j++) {
                t[j] = temp[j];
            }
            T[i] = temp[j];
            S.add(i, t);
        }
        return T;
    }
    
    public double[][] convertWordToDouble(ArrayList<String[]> S) {
        double[][] data = new double[S.size()][];
        for (int i = 0; i < S.size(); i++) {
            String [] S1 = S.get(i);
            data[i] = new double[S1.length];
            for (int j = 0; j < S1.length; j++) {
                if (!"?".equals(S1[j])) {
                    data[i][j] = Double.parseDouble(S1[j]);
                } else {
                    data[i][j] = Double.NaN;
                }
            }
        }
        return data;
    }
    
    public int[][] convertWordToInteger(ArrayList<String[]> S) {
        int[][] data = new int[S.size()][];
        for (int i = 0; i < S.size(); i++) {
            String[] S1 = S.get(i);
            data[i] = new int[S1.length];
            for (int j = 0; j < S1.length; j++) {
                data[i][j] = Integer.parseInt(S1[j]);
            }
        }
        return data;
    }
}

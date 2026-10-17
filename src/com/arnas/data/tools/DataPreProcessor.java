package com.arnas.data.tools;

import com.arnas.data.handler.MetaData;
import java.util.ArrayList;

/**
 *
 * @author ARNAS_
 */
public class DataPreProcessor {
    
    ArrayList<Integer> removedColumn;
    ArrayList<Integer> removedRow;
    
    private ArrayList<Integer> nominalIndex;
    private double[] modus;     // length = nominalIndex.size
    
    private ArrayList<Integer> linearIndex;
    private double[] mean;      // length = linearIndex.size
    private double[] stDev;     // length = linearIndex.size
    
    double[][] D;
    String[] T, N;
    
    public DataPreProcessor() {
        removedColumn = new ArrayList();
        removedRow = new ArrayList();
    }
    
    /**
     * training data preprocessing method
     * @param data
     * @param metadata
     * @return
     * @throws Exception 
     */
    public double[][] preprocess(double[][] data, MetaData metadata) throws Exception {
        this.D = data;
        this.T = metadata.getDataType();
        this.N = metadata.getAttributeNames();
        //for (int i = 0; i < 4; i++) {
        //    removedColumn.add(i);
        //}
        removeMissingValue();
        //replaceMissingValue();
        removeSameValue(1);
        removeColumn();
        convertToType();
        removeRow();
        
        metadata.setDataType(T);
        metadata.setAttributeNames(N);
        return D;
    }
    
    private void removeMissingValue() {
        for (int i = 0; i < D[0].length; i++) {
            if (!removedColumn.contains(i)) {
                int j = 0;
                boolean missing = false;
                while (!missing && j < D.length) {
                    if (Double.isNaN(D[j][i])) {
                        missing = true;
                    } else {
                        j++;
                    }
                }
                if (missing) {
                    removedColumn.add(i);
                }
            }
        }
    }
    
    private void replaceMissingValue() throws Exception {
        for (int i = 0; i < T.length; i++) {
            switch (T[i]) {
                case "nominal":
                    ArrayList<Double> A = new ArrayList();
                    for (double[] D1 : D) {
                        if (!Double.isNaN(D1[i])) {
                            A.add(D1[i]);
                        }
                    }
                    double modus = modus(A);
                    for (int j = 0; j < D.length; j++) {
                        if (Double.isNaN(D[j][i])) {
                            D[j][i] = modus;
                        }
                    }
                    break;
                case "linear":
                    A = new ArrayList();
                    for (double[] D1 : D) {
                        if (!Double.isNaN(D1[i])) {
                            A.add(D1[i]);
                        }
                    }
                    double mean = mean(A);
                    for (int j = 0; j < D.length; j++) {
                        if (Double.isNaN(D[j][i])) {
                            D[j][i] = mean;
                        }
                    }
                    break;
                default :
                    throw new Exception("the data type that defined not recognize");
            }
        }
    }
    
    private double modus(ArrayList<Double> A) {
        int[] count = new int[2];
        for (double a : A) {
            if (a == 1.0) {
                count[1]++;
            } else {
                count[0]++;
            }
        }
        if (count[0] > count[1]) {
            return 0.0;
        } else {
            return 1.0;
        }
    }
    
    private double mean(ArrayList<Double> A) {
        double sum = 0.0;
        for (double a : A) {
            sum = sum + a;
        }
        return sum / A.size();
    }
    
    private void removeSameValue(double ratio) {
        for (int i = 0; i < D[0].length; i++) {
            if (!removedColumn.contains(i)) {
                ArrayList<Double> value = new ArrayList();
                ArrayList<Integer> count = new ArrayList();
                for (double[] d : D) {
                    int j = value.indexOf(d[i]);
                    if (j > -1) {
                        count.add(j, count.get(j)+1);
                    } else {
                        value.add(d[i]);
                        count.add(1);
                    }
                }
                int same = 0;
                for (int j : count) {
                    if (same < j) {
                        same = j;
                    }
                }
                if (same >= (D.length * ratio)) {
                    removedColumn.add(i);
                }
            }
        }
    }
    
    private void removeRow() {
        if (removedRow.isEmpty()) {
            return;
        }
        double[][] newD = new double[D.length - removedRow.size()][D[0].length];
        int j = 0;
        for (int i = 0; i < D.length; i++) {
            if (!removedRow.contains(i)) {
                System.arraycopy(D[i], 0, newD[j], 0, D[i].length);
                j++;
            }
        }
        D = newD;
    }
    
    private void removeColumn() {
        for (int i = 0; i < D.length; i++) {
            
            double d[] = new double[D[i].length];
            System.arraycopy(D[i], 0, d, 0, D[i].length);
            
            D[i] = new double[d.length - removedColumn.size()];
            
            int k = 0;
            for (int j = 0; j < d.length; j++) {
                if (!removedColumn.contains(j)) {
                    D[i][k] = d[j];
                    k++;
                }
            }
        }
        
        String t[] = new String[T.length];
        System.arraycopy(T, 0, t, 0, T.length);
            
        T = new String[t.length - removedColumn.size()];
        int k = 0;
        for (int i = 0; i < t.length; i++) {
            if (!removedColumn.contains(i)) {
                T[k] = t[i];
                k++;
            }
        }
        
        if (N != null) {
            String n[] = new String[N.length];
            System.arraycopy(N, 0, n, 0, N.length);
            
            N = new String[n.length - removedColumn.size()];
            
            k = 0;
            for (int i = 0; i < n.length; i++) {
                if (!removedColumn.contains(i)) {
                    N[k] = n[i];
                    k++;
                }
            }
        }
    }
    
    private void convertToType() throws Exception {
        
        nominalIndex = new ArrayList();
        linearIndex = new ArrayList();
        
        for (int i = 0; i < T.length; i++) {
            switch (T[i]) {
                case "nominal":
                    nominalIndex.add(i);
                    break;
                case "linear":
                    linearIndex.add(i);
                    break;
                default :
                    throw new Exception("the data type that defined not recognize");
            }
        }
        
        toNominalType();
        toLinearType();
    }
    
    private void toNominalType() {
        modus = new double[nominalIndex.size()];
        for (int i = 0; i < nominalIndex.size(); i++) {
            for (int j = 0; j < D.length; j++) {    
                if (D[j][nominalIndex.get(i)] == 0.0) {
                    D[j][nominalIndex.get(i)] = -1.0;
                }
            }
            modus[i] = modus(nominalIndex.get(i));
        }
    }
    
    private double modus(int i) {
        int[] count = new int[2];
        for (double[] d : D) {
            if (d[i] == 1.0) {
                count[1]++;
            } else {
                count[0]++;
            }
        }
        if (count[0] > count[1]) {
            return -1.0;
        } else {
            return 1.0;
        }
    }
    
    private void toLinearType() {
        mean = new double[linearIndex.size()];
        stDev = new double[linearIndex.size()];
        
        for (int i = 0; i < linearIndex.size(); i++) {
            mean[i] = mean(linearIndex.get(i));
            stDev[i] = stDev(linearIndex.get(i), mean[i]);
            standarization(linearIndex.get(i), mean[i], stDev[i]);
            removeOutLier(linearIndex.get(i), 0.0, 1.0);
            //maxMinScaling(linearIndex.get(i));
        }
    }
    
    private double mean(int i) {
        double sum = 0.0;
        for (double[] d : D) {
            sum = sum + d[i];
        }
        return sum / D.length;
    }
    
    private double stDev(int i, double mean) {
        double sum = 0.0;
        for (double[] d : D) {
            sum = sum + Math.pow((d[i] - mean), 2);
        }
        return Math.sqrt(sum / (D.length - 1));
    }
    
    private void standarization(int i, double mean, double stDev) {
        for (int j = 0; j < D.length; j++) {
            D[j][i] = (D[j][i] - mean) / stDev;
        }
    }
    
    private void maxMinScaling(int i) {
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (double[] d : D) {
            if (d[i] > max) max = d[i];
            if (d[i] < min) min = d[i];
        }
        for (int j = 0; j < D.length; j++) {
            D[j][i] = (D[j][i] - min) / (max - min);
        }
    }
    
    private void removeOutLier(int i, double mean, double stDev) {
        for (int j = 0; j < D.length; j++) {
            if (!removedRow.contains(j) && Math.abs(D[j][i] - mean) > (2 * stDev)) {
                removedRow.add(j);
            }
        }
    }
    
    /* Data testing PreProcess begin here */
    
    /**
     * testing data preprocessing method
     * @param Data
     * @return
     */
    public double[] preprocess(double[] Data) {
        Data = removeColumn(Data);
        replaceMissingValue(Data);
        convertToType(Data);
        return Data;
    }
    
    private double[] removeColumn(double[] D) {
        double[] newD = new double[D.length - removedColumn.size()];
        int j = 0;
        for (int i = 0; i < D.length; i++) {
            if (!removedColumn.contains(i)) {
                newD[j] = D[i];
                j++;
            }
        }
        return newD;
    }
    
    private void replaceMissingValue(double[] D) {
        for (int i = 0; i < D.length; i++) {
            if (!removedColumn.contains(i)) {
                if (Double.isNaN(D[i])) {
                    if (linearIndex.contains(i)) {
                        D[i] = mean[linearIndex.indexOf(i)];
                    } else if(nominalIndex.contains(i)) {
                        D[i] = modus[nominalIndex.indexOf(i)];
                    }
                }
            }
        }
    }
    
    private void convertToType(double[] D) {
        /* Nominal Type */
        for (int i : nominalIndex) {
            if (D[i] == 0.0) {
                D[i] = -1.0;
            }
        }
        
        /* Linear Type */
        for (int i = 0; i < linearIndex.size(); i++) {
            D[linearIndex.get(i)] = (D[linearIndex.get(i)] - mean[i]) / stDev[i];
        }
    }
    
}

package com.arnas.manfis.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author ARNAS_
 */
public class AnfisModel {

    public static final int VERSION = 1;

    double a[][];
    double b[][];
    double p[][][];
    String mfType;
    String tNorm;
    String outputType;
    int nRule;
    int nInput;
    int nOutput;
    
    public void saveModel(double[][] A, double[][] B, double[][][] P) {
        a = A;
        b = B;
        p = P;
        if (A != null) {
            nRule = A.length;
            nInput = A[0].length;
        }
        if (P != null && P.length > 0) {
            nOutput = P[0].length;
        }
    }

    public void setMeta(String mfType, String tNorm, String outputType) {
        this.mfType = mfType;
        this.tNorm = tNorm;
        this.outputType = outputType;
    }
       
    public double[][] getA() {
        return a;
    }
    
    public double[][] getB() {
        return b;
    }
    
    public double[][][] getP() {
        return p;
    }

    public String getMfType() {
        return mfType;
    }

    public String getTNorm() {
        return tNorm;
    }

    public String getOutputType() {
        return outputType;
    }

    public int getNRule() {
        return nRule;
    }

    public int getNInput() {
        return nInput;
    }

    public int getNOutput() {
        return nOutput;
    }
    
    public void saveToFile(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("ANFIS " + VERSION);
            writer.newLine();
            writer.write("mf_type " + nullToDash(mfType));
            writer.newLine();
            writer.write("t_norm " + nullToDash(tNorm));
            writer.newLine();
            writer.write("output_type " + nullToDash(outputType));
            writer.newLine();
            writer.write("n_rule " + a.length);
            writer.newLine();
            writer.write("n_input " + a[0].length);
            writer.newLine();
            writer.write("n_output " + p[0].length);
            writer.newLine();
            writer.write("A "+a.length+" "+a[0].length);
            writer.newLine();
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a[i].length; j++) {
                    writer.write(String.valueOf(i)+" "+String.valueOf(j)+" "+String.valueOf(a[i][j]));
                    writer.newLine();
                }
            }
            writer.write("B "+b.length+" "+b[0].length);
            writer.newLine();
            for (int i = 0; i < b.length; i++) {
                for (int j = 0; j < b[i].length; j++) {
                    writer.write(String.valueOf(i)+" "+String.valueOf(j)+" "+String.valueOf(b[i][j]));
                    writer.newLine();
                }
            }
            writer.write("P "+p.length+" "+p[0].length+" "+p[0][0].length);
            writer.newLine();
            for (int i = 0; i < p.length; i++) {
                for (int j = 0; j < p[i].length; j++) {
                    for (int k = 0; k < p[i][j].length; k++) {
                        writer.write(String.valueOf(i)+" "+String.valueOf(j)+" "+String.valueOf(k)+" "+String.valueOf(p[i][j][k]));
                        writer.newLine();
                    }
                }
            }
            writer.flush();
        }
    }

    private static String nullToDash(String s) {
        return s == null ? "-" : s;
    }
    
    public void loadFromFile(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        String[] part = line.split(" ");
        if (part[0].equals("ANFIS")) {
            int version = Integer.parseInt(part[1]);
            if (version != VERSION) {
                throw new Exception("unsupported model version " + version);
            }
            mfType = readKeyed(reader, "mf_type");
            tNorm = readKeyed(reader, "t_norm");
            outputType = readKeyed(reader, "output_type");
            nRule = Integer.parseInt(readKeyed(reader, "n_rule"));
            nInput = Integer.parseInt(readKeyed(reader, "n_input"));
            nOutput = Integer.parseInt(readKeyed(reader, "n_output"));
            part = reader.readLine().split(" ");
        }
        if (part[0].equals("A")) {
            a = new double[Integer.parseInt(part[1])][Integer.parseInt(part[2])];
            nRule = a.length;
            nInput = a[0].length;
            part = reader.readLine().split(" ");
            do {    
                a[Integer.parseInt(part[0])][Integer.parseInt(part[1])] = Double.parseDouble(part[2]);
                part = reader.readLine().split(" ");
            } while (!part[0].equals("B"));
            b = new double[Integer.parseInt(part[1])][Integer.parseInt(part[2])];
            part = reader.readLine().split(" ");
            do {
                b[Integer.parseInt(part[0])][Integer.parseInt(part[1])] = Double.parseDouble(part[2]);
                part = reader.readLine().split(" ");
            } while (!part[0].equals("P"));
            p = new double[Integer.parseInt(part[1])][Integer.parseInt(part[2])][Integer.parseInt(part[3])];
            nOutput = p[0].length;
            while ((line = reader.readLine()) != null) {
                part = line.split(" ");
                p[Integer.parseInt(part[0])][Integer.parseInt(part[1])][Integer.parseInt(part[2])] = Double.parseDouble(part[3]);
            }
        } else {
            throw new Exception("wrong file");
        }
    }

    private static String readKeyed(BufferedReader reader, String key) throws Exception {
        String[] part = reader.readLine().split(" ");
        if (!key.equals(part[0])) {
            throw new Exception("expected " + key);
        }
        return part.length > 1 ? part[1] : "-";
    }
    
}

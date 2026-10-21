package com.arnas.manfis.network;

import com.arnas.manfis.network.rule.Rule;
import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.network.rule.membershipFunction.MembershipFunction;

/**
 *
 * @author ARNAS_
 */
public class Network {
    
    /** OutputType parameter for Nominal Output */
    public static final String NOMINAL_OUTPUT = "NOMINAL";
    /** OutputType parameter for Linear Output */
    public static final String LINEAR_OUTPUT = "LINEAR";
    
    private final String outputType;
    
    private final int nInput;
    private final int nRule;
    private final int nOutput;
    
    private final Rule rule[];
    
    private final double Y[];
    
    private final double error[];
    
    private double sumw;
    private boolean isErrorCalculated;
    
    public Network(String outputType, String mfType, int nInput, int nRule, int nOutput) {
        this(outputType, mfType, Rule.PRODUCT_NORM, nInput, nRule, nOutput);
    }

    public Network(String outputType, String mfType, String tNorm, int nInput, int nRule, int nOutput) {
        this.outputType = outputType;
        this.nInput = nInput;
        this.nRule = nRule;
        this.nOutput = nOutput;
        rule = new Rule[nRule];
        for (int i = 0; i < nRule; i++) {
            rule[i] = new Rule(nInput, nOutput, mfType, tNorm);
            ///*
            double[] A = new double[nInput];
            double[] B = new double[nInput];
            for (int j = 0; j < nInput; j++) {
                A[j] = 0.4 - 0.04 * nRule;
                B[j] = -1 + ((i + 1) * 2.0 - 1.0) / nRule;
            }
            //System.out.println("a"+i+" : "+A[0]);
            //System.out.println("b"+i+" : "+B[0]);
            rule[i].setPremiseParameters(A, B);
            //*/
        }
        Y = new double[nOutput];
        error = new double[nOutput];
    }
    
    public void execute(AnfisInput data) {
        //menghitung layer 1 dan layer 2
        for (int i = 0; i < nRule; i++) {
            //System.out.println("rule-"+i);//testing
            rule[i].executeLayer1(data);
            rule[i].executeLayer2();
        }
        
        //menghitung layer 3 dan layer 4
        sumw = 0;
        for (int i = 0; i < nRule; i++) {
            sumw = sumw + rule[i].w();
        }
        for (int i = 0; i < nRule; i++) {
            rule[i].executeLayer3(sumw);
            rule[i].executeLayer4(data);
        }
        
        //menghitung layer 5
        //System.out.println("Layer-5");
        for (int i = 0; i < nOutput; i++) {
            Y[i] = 0;
            for (int j = 0; j < nRule; j++) {
                Y[i] = Y[i] + rule[j].y(i);
            }
            //System.out.println(Y[i]);
        }
        
        isErrorCalculated = false;
    }
    
    public double[] getResult() {
        return Y;
    }
    
    public double calculateError(AnfisInput data) {
        double SSE = 0;
        switch (outputType) {
            case NOMINAL_OUTPUT :
                for (int i = 0; i < nOutput; i++) {
                    if ((data.YBin(i)==2) || (activationFunction(Y[i]) == data.YBin(i))) {
                        error[i] = 0;
                    } else {
                        if (data.YBin(i) == 0) {
                            error[i] = - 0.001 - Y[i];
                        } else {
                            error[i] = 0.001 - Y[i];
                        }
                        SSE += 1;
                    }
                }
                break;
            case LINEAR_OUTPUT :
            default :
                //for (int i = 0; i < nOutput; i++) {
                    error[0] = data.Y(0) - Y[0];
                    SSE += Math.pow(error[0],2);
                //}
                break;
        }
        isErrorCalculated = true;
        double MSE = SSE/nOutput;
        return MSE;
    }
    
    private int activationFunction(double x) {
        if (x < 0) {
            return 0;
        } else {
            return 1;
        }
    }
    
    public void correctError(AnfisInput data, double η, double m) {
        
        //menghitung error layer 5 dan layer 4
        if (!isErrorCalculated) {
            calculateError(data);
        }
        //System.out.println("error");
        //for (double e : error) {
        //    System.out.println(e);
        //}
        //memperbaiki error parameter konsekuen
        for (int i = 0; i < nRule; i++) {
            rule[i].correctConsequentError(error, data, η, m);
        }
        
        //menghitung error layer 3
        //System.out.println("E3");
        double E3[] = new double[nRule];
        for (int i = 0; i < nRule; i++) {
            E3[i] = rule[i].computeE3(error);
        }
        
        //menghitung error layer 2
        //System.out.println("E2");
        sumw = 0;
        for (int i = 0; i < nRule; i++) {
            sumw = sumw + rule[i].w();
        }
        double E2[] = new double[nRule];
        for (int i = 0; i < nRule; i++) {
            //E2[i] = E3[i];
            //*
            double num = 0.0;
            for (int j = 0; j < nRule; j++) {
                if (i == j) {
                    num = num + ((sumw - rule[i].w()) * E3[j]);
                } else {
                    num = num + (- rule[i].w() * E3[j]);
                }
            }
            E2[i] = num / sumw;
            //System.out.println(E2[i]);
            //*/
        }
        
        //menghitung error layer 1
        //System.out.println("E1");
        double E1[][] = new double[nRule][];
        for (int i = 0; i < nRule; i++) {
            E1[i] = rule[i].computeE1(E2[i]);
            //for (double e : E1[i]) {
            //    System.out.print(e+"\t");
            //}
            //System.out.println();
        }
        
        //memperbaiki error parameter premis
        for (int i = 0; i < nRule; i++) {
            rule[i].correctPremisError(E1[i], data, η, m);
        }
    }

    /** Gradient step for premise parameters only (hybrid ANFIS). */
    public void correctPremiseError(AnfisInput data, double η, double m) {
        if (!isErrorCalculated) {
            calculateError(data);
        }
        double E3[] = new double[nRule];
        for (int i = 0; i < nRule; i++) {
            E3[i] = rule[i].computeE3(error);
        }
        sumw = 0;
        for (int i = 0; i < nRule; i++) {
            sumw = sumw + rule[i].w();
        }
        double E2[] = new double[nRule];
        for (int i = 0; i < nRule; i++) {
            double num = 0.0;
            for (int j = 0; j < nRule; j++) {
                if (i == j) {
                    num = num + ((sumw - rule[i].w()) * E3[j]);
                } else {
                    num = num + (- rule[i].w() * E3[j]);
                }
            }
            E2[i] = sumw == 0 ? 0 : num / sumw;
        }
        for (int i = 0; i < nRule; i++) {
            rule[i].correctPremisError(rule[i].computeE1(E2[i]), data, η, m);
        }
    }
    
    /**
     * Get premise parameters A
     * @return array of premise parameters A
     */
    public double[][] getA() {
        double[][] a = new double[nRule][];
        for (int i = 0; i < nRule; i++) {
            a[i] = rule[i].getA();
        }
        return a;
    }
    
    /**
     * Get premise parameters B
     * @return array of premise parameters B
     */
    public double[][] getB() {
        double[][] b = new double[nRule][];
        for (int i = 0; i < nRule; i++) {
            b[i] = rule[i].getB();
        }
        return b;
    }
    
    /**
     * Get consequent parameters P
     * @return array of consequent parameters P
     */
    public double[][][] getP() {
        double[][][] p = new double[nRule][][];
        for (int i = 0; i < nRule; i++) {
            p[i] = rule[i].getP();
        }
        return p;
    }
    
    /**
     * Set premise and consequent parameters
     * @param A
     * @param B
     * @param P 
     */
    public void setParameters(double[][] A, double[][] B, double[][][] P) {
        for (int i = 0; i < nRule; i++) {
            rule[i].setParameters(A[i], B[i], P[i]);
        }
    }
    
    public Rule rule(int i) {
        return rule[i];
    }
    
    public MembershipFunction[][] getMembershipFunctionList() {
        MembershipFunction[][] mfList = new MembershipFunction[nInput][nRule];
        for (int i = 0; i < nInput; i++) {
            for (int j = 0; j < nRule; j++) {
                mfList[i][j] = rule[j].getMembershipFunction(i);
            }
        }
        return mfList;
    }
    
}

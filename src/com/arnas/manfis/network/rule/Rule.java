package com.arnas.manfis.network.rule;

import com.arnas.manfis.network.rule.membershipFunction.BellMF;
import com.arnas.manfis.network.rule.membershipFunction.GaussianMF;
import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.network.rule.consequent.Consequent;
import com.arnas.manfis.network.rule.membershipFunction.MembershipFunction;
import com.arnas.manfis.network.rule.sNorm.AlgebricSum;
import com.arnas.manfis.network.rule.tNorm.BasicTNorm;
//import com.arnas.manfis.network.rule.tNorm.EinsteinProduct;

/**
 *
 * @author ARNAS_
 */
public class Rule {
    
    /** MFmethod parameter for Gaussian MF */
    public static final String GAUSSIAN_MF = "GAUSSIAN_MF";
    /** MFmethod parameter for Bell MF */
    public static final String BELL_MF = "BELL_MF";
    
    private final int nInput;
    private final int nOutput;
    
    MembershipFunction mf[];
    
    private double w;
    private double w̅;
    
    private Consequent Cons[];
    
    private double y[];
    
    private final BasicTNorm basicTNorm;
    //private final EinsteinProduct einsteinProduct;
    private final AlgebricSum algebricSum;
    
    public Rule(int nInput, int nOutput, String MFmethod) {
        this.nInput = nInput;
        this.nOutput = nOutput;
        
        switch (MFmethod) {
            case GAUSSIAN_MF :
                mf = new GaussianMF[nInput];
                for (int i = 0; i < nInput; i++) {
                    mf[i] = new GaussianMF();
                }
            case BELL_MF :
            default :
                mf = new BellMF[nInput];
                for (int i = 0; i < nInput; i++) {
                    mf[i] = new BellMF();
                }
                break;
                
        }
        
        basicTNorm = new BasicTNorm(nInput);
        //einsteinProduct = new EinsteinProduct(nInput);
        algebricSum = new AlgebricSum(nInput);
        
        Cons = new Consequent[nOutput];
        for (int i = 0; i < nOutput; i++) {
            Cons[i] = new Consequent(nInput);
            ///*
            double[] P = new double[nInput+1];
            for (int j = 0; j < nInput+1; j++) {
                //P[j] = 2 * (i+1) * 0.1;
                P[j] = 0.5;
            }
            Cons[i].setParameters(P);
            //*/
        }
        y = new double[nOutput];
    }
    
    public void executeLayer1(AnfisInput data) {
        //System.out.println("Layer-1");
        for (int i = 0; i < nInput; i++) {
            mf[i].compute(data.X(i));
            //System.out.println(mf[i].μ());//testing
        }
    }
    
    public void executeLayer2() {
        //System.out.println("Layer-2");
        double μ[] = new double[nInput];
        for (int i = 0; i < nInput; i++) {
            μ[i] = mf[i].μ();
        }
        //w = basicTNorm.computeNorm(μ);
        w = algebricSum.computeNorm(μ);
        //System.out.println(w);
        //w = einsteinProduct.computeNorm(μ);
    }
    
    public void executeLayer3(double sumw) {
        //System.out.println("Layer-3");
        w̅ = w / sumw;
        //System.out.println(w̅);
    }
    
    public void executeLayer4(AnfisInput data) {
        //System.out.println("Layer-4");
        for (int i = 0; i < nOutput; i++) {
            y[i] = w̅ * Cons[i].compute(data);
            //System.out.print(Cons[i].compute(data)+"\t");
            //System.out.println(y[i]);
        }
    }
    
    public double computeE3(double E4[]) {
        double E3 = 0;
        for (int i = 0; i < nOutput; i++) {
            E3 = E3 + Cons[i].f() * E4[i];
        }
        //System.out.println(E3);
        return E3;
    }
    
    public double[] computeE1(double E2) {
        double μ[] = new double[nInput];
        for (int i = 0; i < nInput; i++) {
            μ[i] = mf[i].μ();
        }
        //return basicTNorm.calculateError(E2, μ);
        return algebricSum.calculateError(E2, μ);
        //return einsteinProduct.calculateError(E2, μ);
    }
    
    public void correctPremisError(double E1[], AnfisInput data, double η, double m) {
        //System.out.println("PP");
        for (int i = 0; i < nInput; i++) {
            mf[i].correctError(E1[i], data.X(i), η, m);
        }
    }
    
    public void correctConsequentError(double E4[], AnfisInput data, double η, double m) {
        //System.out.println("CP");
        for (int i = 0; i < nOutput; i++) {
            Cons[i].correctError(E4[i], w̅, data, η, m);
            //System.out.println("");
        }
    }
    
    /**
     * get premise parameters A
     * @return array of premise parameters A
     */
    public double[] getA() {
        double a[] = new double[nInput];
        for (int i = 0; i < mf.length; i++) {
            a[i] = mf[i].a();
        }
        return a;
    }
    
    /**
     * get premise parameters B
     * @return array of premise parameters B
     */
    public double[] getB() {
        double b[] = new double[nInput];
        for (int i = 0; i < nInput; i++) {
            b[i] = mf[i].b();
        }
        return b;
    }
    
    /**
     * get consequent parameters P
     * @return array of consequent parameters P
     */
    public double[][] getP() {
        double[][] p = new double[nOutput][nInput+1];
        for (int i = 0; i < nOutput; i++) {
            System.arraycopy(Cons[i].getParameter(), 0, p[i], 0, nInput+1);
        }
        return p;
    }
    
    public void setParameters(double[] A, double[] B, double[][] P) {
        setPremiseParameters(A, B);
        setConsequentParameters(P);
    }
    
    public void setPremiseParameters(double[] A, double[] B) {
        for (int i = 0; i < nInput; i++) {
            mf[i].setParameters(A[i], B[i]);
        }
    }
    
    public void setConsequentParameters(double[][] P) {
        for (int i = 0; i < nOutput; i++) {
            Cons[i].setParameters(P[i]);
        }
    }
    
    public double w() {
        return w;
    }
    
    public double w̅() {
        return w̅;
    }
    
    public double y(int i) {
        return y[i];
    }
    
    public Consequent Cons(int i) {
        return Cons[i];
    }
    
    public MembershipFunction getMembershipFunction(int i) {
        return mf[i];
    }
    
}

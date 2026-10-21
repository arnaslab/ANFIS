package com.arnas.data.tools;

import com.arnas.data.handler.MetaData;
import com.arnas.manfis.data.AnfisInput;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author ARNAS_
 */
public class DataSplitter {
    
    AnfisInput[] newDataTraining;
    AnfisInput[] newDataValidation;
    AnfisInput[] newDataHoldout;
    AnfisInput[][] foldData;
    
    public void fixSplitter(AnfisInput[] dataTraining, MetaData metadata, int ratioTr, int ratioVal) {
        
        ArrayList<Integer> iDataTraining = new ArrayList();
        ArrayList<Integer> iDataValidation = new ArrayList();
        
        int ratioTotal = ratioTr + ratioVal;
        
        for (int i = 0; i < metadata.nClassCode(); i++) {
            int k = 0;
            for (int j = 0; j < dataTraining.length; j++) {
                if (dataTraining[j].YNum()[0] == i) {
                    if ((k == 1)||(k > ratioTotal - ratioVal)) {
                        iDataValidation.add(j);
                    } else {
                        iDataTraining.add(j);
                    }
                    if (k == ratioTotal-1) {
                        k = 0;
                    } else {
                        k++;
                    }
                }
            }
        }
        
        newDataTraining = makeNewAnfisData(dataTraining, iDataTraining);
        newDataValidation = makeNewAnfisData(dataTraining, iDataValidation);    
    }

    public void threeWaySplitter(AnfisInput[] data, MetaData metadata,
            int ratioTr, int ratioVal, int ratioTs) {
        ArrayList<Integer> iTrain = new ArrayList<>();
        ArrayList<Integer> iVal = new ArrayList<>();
        ArrayList<Integer> iTest = new ArrayList<>();
        int ratioTotal = ratioTr + ratioVal + ratioTs;
        for (int c = 0; c < metadata.nClassCode(); c++) {
            int k = 0;
            for (int j = 0; j < data.length; j++) {
                if (data[j].YNum()[0] == c) {
                    if (k < ratioTr) {
                        iTrain.add(j);
                    } else if (k < ratioTr + ratioVal) {
                        iVal.add(j);
                    } else {
                        iTest.add(j);
                    }
                    k = (k == ratioTotal - 1) ? 0 : k + 1;
                }
            }
        }
        newDataTraining = makeNewAnfisData(data, iTrain);
        newDataValidation = makeNewAnfisData(data, iVal);
        newDataHoldout = makeNewAnfisData(data, iTest);
    }
    
    public void randomSplitter(AnfisInput[] dataTraining, MetaData metadata, int ratioTr, int ratioVal) {

        ArrayList<Integer>[] iClassGroup = new ArrayList[metadata.nClassCode()];
        
        for (int i = 0; i < metadata.nClassCode(); i++) {
            iClassGroup[i] = new ArrayList();
            for (int j = 0; j < dataTraining.length; j++) {
                if (dataTraining[j].YNum()[0] == i) {
                    iClassGroup[i].add(j);
                }
            }
        }
        
        ArrayList<Integer> iDataTraining = new ArrayList();
        ArrayList<Integer> iDataValidation = new ArrayList();
        
        Random R = new Random();
        
        for (ArrayList<Integer> cg : iClassGroup) {
            int ratioTotal = ratioTr+ratioVal;
            int valSize = (cg.size()+(ratioTotal/2))* ratioVal/ratioTotal;
            for (int i = 0; i < valSize; i++) {
                int r = R.nextInt(cg.size());
                iDataValidation.add(cg.get(r));
                cg.remove(r);
            }
            for (int c : cg) {
                iDataTraining.add(c);
            }
        }        
        //System.out.println("dt tr");
        newDataTraining = makeNewAnfisData(dataTraining, iDataTraining);
        //System.out.println("dt ts");
        newDataValidation = makeNewAnfisData(dataTraining, iDataValidation);
    }
    
    public void kFoldSplitter(AnfisInput[] dataTraining, MetaData metadata, int k) {
        foldData = new AnfisInput[k][];
        ArrayList<Integer>[] iFold = new ArrayList[k];
        for (int i = 0; i < k; i++) {
            iFold[i] = new ArrayList();
        }
        int l = 0;
        for (int i = 0; i < metadata.nClassCode(); i++) {
            for (int j = 0; j < dataTraining.length; j++) {
                if (dataTraining[j].YNum()[0] == i) {
                    iFold[l].add(j);
                    if (l < k-1) {
                        l++;
                    } else {
                        l = 0;
                    }
                }
            }
        }
        
        for (int i = 0; i < k; i++) {
            foldData[i] = makeNewAnfisData(dataTraining, iFold[i]);
        }
        
    }
    
    public void definedSplitter(AnfisInput[] dataTraining, ArrayList<Integer> iDataTraining, ArrayList iDataValidation) {
        newDataTraining = makeNewAnfisData(dataTraining, iDataTraining);
        newDataValidation = makeNewAnfisData(dataTraining, iDataValidation);
    }
    
    public void definedSplitter(AnfisInput[] dataTraining, int[] splitIndex) {
        
        ArrayList<Integer> iDataTraining = new ArrayList();
        ArrayList<Integer> iDataValidation = new ArrayList();
        
        for (int i = 0; i < splitIndex.length; i++) {
            if (splitIndex[i] == 0) {
                iDataTraining.add(i);
            } else {
                iDataValidation.add(i);
            }
        }
        
        newDataTraining = makeNewAnfisData(dataTraining, iDataTraining);
        newDataValidation = makeNewAnfisData(dataTraining, iDataValidation);
        
    }
    
    private AnfisInput[] makeNewAnfisData(AnfisInput[] source, ArrayList<Integer> index) {
        AnfisInput[] newData = new AnfisInput[index.size()];
        int j = 0;
        for (int i : index) {
            //System.out.println(source[i].index()+"\t"+j);
            newData[j] = new AnfisInput(j, source[i].X());
            newData[j].setTarget(source[i].YNum(), source[i].YBin(), source[i].Y());
            j++;
        }
        return newData;
    }

    public AnfisInput[] getNewDataTraining() {
        return newDataTraining;
    }

    public AnfisInput[] getNewDataValidation() {
        return newDataValidation;
    }

    public AnfisInput[] getNewDataHoldout() {
        return newDataHoldout;
    }
    
    public AnfisInput[][] getFoldData() {
        return foldData;
    }
    
}

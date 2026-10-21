package com.arnas.manfis.main;

import com.arnas.data.handler.DataHandler;
import com.arnas.data.handler.KFoldData;
import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.data.AnfisModel;
import com.arnas.manfis.data.AnfisOutput;
import com.arnas.manfis.network.rule.Rule;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Headless entry point. Usage:
 *   mvn -q exec:java -Dexec.args="train [options]"
 *   mvn -q exec:java -Dexec.args="kfold --k 5 [options]"
 *   mvn -q exec:java -Dexec.args="predict --model FILE --prefix xor"
 */
public class AnfisCli {

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "help".equals(args[0]) || "-h".equals(args[0])
                || "--help".equals(args[0])) {
            printUsage();
            return;
        }
        if ("predict".equals(args[0])) {
            runPredict(parseOptions(args));
            return;
        }
        if ("kfold".equals(args[0])) {
            runKFold(parseOptions(args));
            return;
        }
        if (!"train".equals(args[0])) {
            System.err.println("Unknown command: " + args[0]);
            printUsage();
            System.exit(1);
            return;
        }
        runTrain(parseOptions(args));
    }

    private static void runTrain(Map<String, String> opts) throws Exception {
        Path dataDir = Path.of(opts.getOrDefault("data-dir", "dataset"));
        String prefix = opts.getOrDefault("prefix", "xor");
        int nRule = Integer.parseInt(opts.getOrDefault("rules", "2"));
        String mfType = opts.getOrDefault("mf", "BELL_MF");
        String tNorm = opts.getOrDefault("tnorm", Rule.PRODUCT_NORM);
        double learningRate = Double.parseDouble(opts.getOrDefault("lr", "0.01"));
        double momentum = Double.parseDouble(opts.getOrDefault("momentum", "0.1"));
        int epochs = Integer.parseInt(opts.getOrDefault("epochs", "20"));
        double tolerance = Double.parseDouble(opts.getOrDefault("tolerance", "0.0"));
        int trainRatio = Integer.parseInt(opts.getOrDefault("train-ratio", "2"));
        int valRatio = Integer.parseInt(opts.getOrDefault("val-ratio", "1"));
        int testRatio = Integer.parseInt(opts.getOrDefault("test-ratio", "1"));
        File modelOut = opts.containsKey("model") ? new File(opts.get("model")) : null;

        DataHandler handler = loadHandler(dataDir, prefix);
        handler.useTrainValTestSplit(trainRatio, valRatio, testRatio);

        Anfis anfis = trainModel(handler.dataTraining(), handler.dataTesting(),
                handler.dataHoldout(), handler, nRule, mfType, tNorm,
                learningRate, momentum, epochs, tolerance, true);

        AnfisOutput test = anfis.evaluateTestSet();
        double lastRmse = anfis.getTrainingResult().getRmse();
        if (Double.isNaN(lastRmse) || Double.isInfinite(lastRmse)) {
            System.err.println("Training did not produce a finite RMSE.");
            System.exit(2);
        }
        File[] saved = saveRun(anfis);
        if (modelOut != null) {
            anfis.saveModelToFile(modelOut);
        }
        System.out.printf("done  last_training_rmse=%.6f%n", lastRmse);
        if (test != null) {
            System.out.printf("test  rmse=%.6f  acc=%.2f%n", test.getRmse(), test.getAcc());
        }
        System.out.println("model  " + (modelOut != null ? modelOut.getPath() : saved[0].getPath()));
        System.out.println("log    " + saved[1].getPath());
    }

    private static void runPredict(Map<String, String> opts) throws Exception {
        if (!opts.containsKey("model")) {
            System.err.println("predict requires --model FILE");
            System.exit(1);
        }
        Path dataDir = Path.of(opts.getOrDefault("data-dir", "dataset"));
        String prefix = opts.getOrDefault("prefix", "xor");
        File modelFile = new File(opts.get("model"));
        File predictFile = opts.containsKey("input")
                ? new File(opts.get("input"))
                : dataDir.resolve(prefix + ".dataPr.txt").toFile();

        DataHandler handler = loadHandler(dataDir, prefix);
        handler.useFixSpliting(3, 1);
        AnfisModel peek = new AnfisModel();
        peek.loadFromFile(modelFile);
        int nRule = peek.getNRule() > 0 ? peek.getNRule() : 2;
        String mfType = peek.getMfType() != null && !"-".equals(peek.getMfType())
                ? peek.getMfType() : "BELL_MF";
        String tNorm = peek.getTNorm() != null && !"-".equals(peek.getTNorm())
                ? peek.getTNorm() : Rule.PRODUCT_NORM;
        Anfis anfis = new Anfis(handler.dataTraining(), handler.dataTesting(), null,
                handler.metadata(), nRule, mfType, tNorm);
        anfis.loadModelFromFile(modelFile);
        handler.loadDataPrediction(predictFile);
        anfis.predict(handler.dataPrediction());
        AnfisOutput result = anfis.getPredictionResult();
        for (int i = 0; i < handler.dataPrediction().length; i++) {
            int[] bin = result.yBin(i);
            System.out.printf("%d  yNum=%d", i, result.yNum(i));
            if (bin != null) {
                System.out.print("  yBin=");
                for (int b : bin) {
                    System.out.print(b);
                }
            }
            System.out.println();
        }
    }

    private static DataHandler loadHandler(Path dataDir, String prefix) throws Exception {
        File dataFile = dataDir.resolve(prefix + ".dataTr.txt").toFile();
        File typeFile = dataDir.resolve(prefix + ".type.txt").toFile();
        File labelFile = dataDir.resolve(prefix + ".label.txt").toFile();
        File nameFile = dataDir.resolve(prefix + ".name.txt").toFile();
        DataHandler handler = new DataHandler();
        handler.loadAndPreprocess(dataFile, typeFile, nameFile, labelFile);
        return handler;
    }

    private static void runKFold(Map<String, String> opts) throws Exception {
        Path dataDir = Path.of(opts.getOrDefault("data-dir", "dataset"));
        String prefix = opts.getOrDefault("prefix", "xor");
        int k = Integer.parseInt(opts.getOrDefault("k", "5"));
        if (k < 2) {
            throw new IllegalArgumentException("kfold requires --k >= 2, got " + k);
        }
        int nRule = Integer.parseInt(opts.getOrDefault("rules", "2"));
        String mfType = opts.getOrDefault("mf", "BELL_MF");
        String tNorm = opts.getOrDefault("tnorm", Rule.PRODUCT_NORM);
        double learningRate = Double.parseDouble(opts.getOrDefault("lr", "0.01"));
        double momentum = Double.parseDouble(opts.getOrDefault("momentum", "0.1"));
        int epochs = Integer.parseInt(opts.getOrDefault("epochs", "20"));
        double tolerance = Double.parseDouble(opts.getOrDefault("tolerance", "0.0"));

        DataHandler handler = loadHandler(dataDir, prefix);
        handler.useKFold(k);
        KFoldData folds = handler.foldData();
        folds.resetK();
        int foldCount = folds.getK();
        double sumRmse = 0;
        double sumAcc = 0;
        int reported = 0;
        Anfis last = null;
        for (int i = 0; i < foldCount; i++) {
            if (i > 0) {
                folds.setNextK();
            }
            handler.metadata().setMetaData(handler.dataTraining(), handler.dataTesting());
            AnfisInput[] train = handler.dataTraining();
            AnfisInput[] holdout = handler.dataTesting();
            if (train == null || train.length == 0) {
                System.err.printf("fold %d/%d skipped: empty training set%n", i + 1, foldCount);
                continue;
            }
            if (holdout == null || holdout.length == 0) {
                System.err.printf("fold %d/%d skipped: empty holdout%n", i + 1, foldCount);
                continue;
            }
            Anfis anfis = trainModel(train, null, holdout, handler, nRule, mfType, tNorm,
                    learningRate, momentum, epochs, tolerance, false);
            AnfisOutput test = anfis.evaluateTestSet();
            if (test == null) {
                System.err.printf("fold %d/%d skipped: no test metrics%n", i + 1, foldCount);
                continue;
            }
            last = anfis;
            System.out.printf("fold %d/%d  n_train=%d  n_test=%d  rmse=%.6f  acc=%.2f%n",
                    i + 1, foldCount, train.length, holdout.length,
                    test.getRmse(), test.getAcc());
            sumRmse += test.getRmse();
            sumAcc += test.getAcc();
            reported++;
        }
        if (last == null || reported == 0) {
            throw new Exception("kfold produced no trained folds");
        }
        File[] saved = saveRun(last);
        System.out.printf("kfold  k=%d  mean_rmse=%.6f  mean_acc=%.2f%n",
                reported, sumRmse / reported, sumAcc / reported);
        System.out.println("model  " + saved[0].getPath() + "  (last fold)");
        System.out.println("log    " + saved[1].getPath() + "  (last fold)");
    }

    private static Anfis trainModel(AnfisInput[] train, AnfisInput[] val, AnfisInput[] holdout,
            DataHandler handler, int nRule, String mfType, String tNorm,
            double learningRate, double momentum, int epochs, double tolerance,
            boolean printEpochs) throws Exception {
        Anfis anfis = new Anfis(train, val, holdout, handler.metadata(), nRule, mfType, tNorm);
        anfis.startTraining(learningRate, momentum);
        anfis.setIndicator(epochs, tolerance, epochs + 1);
        while (true) {
            double rmse = anfis.trainNextEpoch();
            if (printEpochs) {
                AnfisOutput tr = anfis.getTrainingResult();
                AnfisOutput v = anfis.getValidationResult();
                if (!Double.isNaN(tr.getRmse())) {
                    if (val != null && val.length > 0) {
                        System.out.printf(
                                "epoch %d  rmse_tr=%.6f  acc_tr=%.2f  rmse_val=%.6f  acc_val=%.2f%n",
                                anfis.getLog().getEpoch(),
                                tr.getRmse(), tr.getAcc(), v.getRmse(), v.getAcc());
                    } else {
                        System.out.printf(
                                "epoch %d  rmse_tr=%.6f  acc_tr=%.2f%n",
                                anfis.getLog().getEpoch(), tr.getRmse(), tr.getAcc());
                    }
                }
            }
            if (Double.isNaN(rmse)) {
                break;
            }
        }
        return anfis;
    }

    private static Map<String, String> parseOptions(String[] args) {
        Map<String, String> opts = new LinkedHashMap<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }
            String key = arg.substring(2);
            int eq = key.indexOf('=');
            if (eq >= 0) {
                opts.put(key.substring(0, eq), key.substring(eq + 1));
            } else if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                opts.put(key, args[++i]);
            } else {
                opts.put(key, "true");
            }
        }
        return opts;
    }

    private static void printUsage() {
        System.out.println("ANFIS — Adaptive Neuro-Fuzzy Inference System");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  mvn -q exec:java -Dexec.args=\"train [options]\"");
        System.out.println("  mvn -q exec:java -Dexec.args=\"kfold --k 5 [options]\"");
        System.out.println("  mvn -q exec:java -Dexec.args=\"predict --model FILE [options]\"");
        System.out.println();
        System.out.println("train options:");
        System.out.println("  --data-dir DIR     directory with sample files (default: dataset)");
        System.out.println("  --prefix NAME      file prefix (default: xor)");
        System.out.println("  --rules N          number of fuzzy rules (default: 2)");
        System.out.println("  --mf TYPE          BELL_MF (Cauchy) or GAUSSIAN_MF (default: BELL_MF)");
        System.out.println("  --tnorm TYPE       PRODUCT, MIN, EINSTEIN, HAMACHER, SUM (default: PRODUCT)");
        System.out.println("  --lr RATE          learning rate (default: 0.01)");
        System.out.println("  --momentum M       momentum (default: 0.1)");
        System.out.println("  --epochs N         max epochs (default: 20)");
        System.out.println("  --tolerance T      stop when validation RMSE <= T (default: 0)");
        System.out.println("  --train-ratio A    train parts (default: 2)");
        System.out.println("  --val-ratio B      validation parts (default: 1)");
        System.out.println("  --test-ratio C     held-out test parts (default: 1)");
        System.out.println("  --model FILE       write the trained model here");
        System.out.println();
        System.out.println("kfold options: same as train, plus:");
        System.out.println("  --k N              number of folds (default: 5)");
        System.out.println("  Each fold trains on k-1 parts and reports RMSE/accuracy on the held-out");
        System.out.println("  fold only after training stops. Prints mean RMSE and accuracy.");
        System.out.println();
        System.out.println("predict options:");
        System.out.println("  --model FILE       trained model (required)");
        System.out.println("  --data-dir DIR     directory with sample files (default: dataset)");
        System.out.println("  --prefix NAME      file prefix (default: xor)");
        System.out.println("  --input FILE       unlabeled rows (default: dataset/<prefix>.dataPr.txt)");
    }

    static File[] saveRun(Anfis anfis) throws IOException {
        Files.createDirectories(new File("result").toPath());
        Files.createDirectories(new File("log").toPath());
        String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File modelFile = new File("result", "anfis-" + stamp + ".model.txt");
        File logFile = new File("log", "anfis-" + stamp + ".log.txt");
        anfis.saveModelToFile(modelFile);
        anfis.saveLogToFile(logFile, String.valueOf(anfis.getLog().getDuration()));
        return new File[] {modelFile, logFile};
    }
}

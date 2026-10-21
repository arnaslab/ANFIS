# ANFIS

Adaptive Neuro-Fuzzy Inference System (MANFIS): a 5-layer Takagi–Sugeno network trained from the command line.

Java 17, Maven. This project is **CLI-only**. The old NetBeans Swing UI was removed in [#6](https://github.com/arnaslab/ANFIS/issues/6). Plot training curves or membership functions with any tool that can read `log/*.log.txt` and `result/*.model.txt` (for example Python/Jupyter).

## Build

```bash
mvn -q test
```

## Train and predict

```bash
mvn -q exec:java -Dexec.args="train --prefix xor --epochs 20 --model result/xor.model.txt"
mvn -q exec:java -Dexec.args="kfold --prefix xor --k 5 --epochs 20"
mvn -q exec:java -Dexec.args="predict --model result/xor.model.txt --prefix xor"
```

Train splits data into train / validation / test (defaults 2:1:1). Each epoch fits consequent parameters by LSE and premise MFs by gradient descent. Early stop uses **validation** RMSE; test is reported once at the end.

Options: `--data-dir`, `--prefix`, `--rules`, `--mf`, `--tnorm`, `--lr`, `--momentum`, `--epochs`, `--tolerance`, `--train-ratio`, `--val-ratio`, `--test-ratio`, `--model`. K-fold also takes `--k`.

Sample data is under [`dataset/`](dataset/) (`xor.*`). A successful `train` writes `result/*.model.txt` and `log/*.log.txt`. Run from the repo root with `mvn exec:java` (there is no fat jar).

## Layout

- `com.arnas.manfis` — network, learning, model/log I/O, CLI
- `com.arnas.data` — load, preprocess, split, attribute scores

Layer 2 defaults to algebraic product (`--tnorm PRODUCT|MIN|EINSTEIN|HAMACHER|SUM`). Membership types: `BELL_MF` (Cauchy / 2-parameter bell) and `GAUSSIAN_MF`.

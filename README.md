# ANFIS

Adaptive Neuro-Fuzzy Inference System (MANFIS): a 5-layer Takagi–Sugeno network with a Swing UI and a headless trainer.

Java 17, Maven. Original desktop code plus a reconstructed `Exec` / `KFoldExec` (those files were never committed — see [#2](https://github.com/arnaslab/ANFIS/issues/2) and [#3](https://github.com/arnaslab/ANFIS/issues/3)).

## Build

```bash
mvn -q test
```

## Train (headless)

```bash
mvn -q exec:java -Dexec.args="train --prefix xor --epochs 20 --model result/xor.model.txt"
mvn -q exec:java -Dexec.args="predict --model result/xor.model.txt --prefix xor"
```

Train splits data into train / validation / test (defaults 2:1:1). Each epoch fits consequent parameters by LSE and premise MFs by gradient descent. Early stop uses **validation** RMSE; test is reported once at the end.

Options: `--data-dir`, `--prefix`, `--rules`, `--mf`, `--tnorm`, `--lr`, `--momentum`, `--epochs`, `--tolerance`, `--train-ratio`, `--val-ratio`, `--test-ratio`, `--model`.

Sample data is under [`dataset/`](dataset/) (`xor.*`). A successful `train` writes `result/*.model.txt` and `log/*.log.txt`. Run from the repo root with `mvn exec:java` (there is no fat jar).

## GUI

```bash
mvn -q exec:java -Dexec.args="gui"
```

## Layout

- `com.arnas.manfis` — network, learning, model/log I/O
- `com.arnas.data` — load, preprocess, split, attribute scores
- `com.arnas.exec` — Swing orchestration (`Exec`, `KFoldExec`)
- `com.arnas.gui` — NetBeans Swing UI
- `com.arnas.manfis.main.AnfisCli` — CLI

Layer 2 defaults to algebraic product (`--tnorm PRODUCT|MIN|EINSTEIN|HAMACHER|SUM`). Membership types: `BELL_MF` (Cauchy / 2-parameter bell) and `GAUSSIAN_MF`. GUI rewrite is [#6](https://github.com/arnaslab/ANFIS/issues/6).

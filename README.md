# ANFIS

Adaptive Neuro-Fuzzy Inference System (MANFIS): a 5-layer Takagi–Sugeno network with a Swing UI and a headless trainer.

Java 17, Maven. Original desktop code plus a reconstructed `Exec` / `KFoldExec` (those files were never committed — see [#2](https://github.com/arnaslab/ANFIS/issues/2) and [#3](https://github.com/arnaslab/ANFIS/issues/3)).

## Build

```bash
mvn -q test
```

## Train (headless)

```bash
mvn -q exec:java -Dexec.args="train --prefix xor --epochs 20"
```

Options: `--data-dir`, `--prefix`, `--rules`, `--mf` (`BELL_MF` or `GAUSSIAN_MF`), `--lr`, `--momentum`, `--epochs`, `--tolerance`, `--train-ratio`, `--test-ratio`.

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

Layer 2 defaults to algebraic product. Membership types: `BELL_MF` (Cauchy / 2-parameter bell) and `GAUSSIAN_MF` (`exp(-½ ((x−c)/σ)²)`). Hybrid LSE-each-epoch is [#5](https://github.com/arnaslab/ANFIS/issues/5).

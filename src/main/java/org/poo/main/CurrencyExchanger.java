package org.poo.main;
import org.poo.fileio.ExchangeInput;
import java.util.HashMap;
import java.util.Map;

class Graph {
    private final int nodes;
    private final double[][] values;
    private final boolean[] used;

    public Graph(int nodes) {
        this.nodes = nodes;
        this.used = new boolean[nodes];
        values = new double[nodes][nodes];
        for (int i = 0; i < nodes; i++) {
            values[i][i] = 1;
        }
    }

    public void addEdge(Integer a, Integer b, double rate) {
        values[a][b] = rate;
        values[b][a] = 1 / rate;
    }

    public void resetUsed() {
        for (int i = 0; i < nodes; i++) {
            used[i] = false;
        }
    }

    public double getDistance(final Integer a, final Integer b) {
        if (values[a][b] != 0) {
            return values[a][b];
        }

        used[a] = true;

        for (int i = 0; i < nodes; i++) {
            if (i != b && !used[i] && values[a][i] != 0) {
                double v = getDistance(i, b);
                if (v > 0) {
                    v *= values[a][i];
                    values[a][b] = v;
                    return v;
                }
            }
        }

        used[a] = false;

        return -1;
    }
}

public class CurrencyExchanger {
    private final Map<String, Integer> map = new HashMap<>();
    private final Graph graph;

    public CurrencyExchanger(ExchangeInput[] exchangeRates) {
        int nodes = 0;
        for (ExchangeInput exchange : exchangeRates) {
            for (String str : new String[]{exchange.getFrom(), exchange.getTo()}) {
                if (!map.containsKey(str)) {
                    map.put(str, nodes++);
                }
            }
        }
        graph = new Graph(nodes);
        for (ExchangeInput exchange : exchangeRates) {
            graph.addEdge(map.get(exchange.getFrom()), map.get(exchange.getTo()), exchange.getRate());
        }
    }

    public double convert(final String from, final String to) {
        graph.resetUsed();
        return graph.getDistance(map.get(from), map.get(to));
    }
}

package org.poo.main.Payments;

import org.poo.fileio.ExchangeInput;

import java.util.Map;
import java.util.HashMap;

class Graph {
    private final int nodes;
    private final double[][] values;
    private final boolean[] inUse;

    Graph(final int nodeCount) {
        nodes = nodeCount;
        inUse = new boolean[nodes];
        values = new double[nodes][nodes];
        for (int i = 0; i < nodes; i++) {
            values[i][i] = 1;
        }
    }

    void addEdge(final Integer a, final Integer b, final double rate) {
        if (rate <= 0) {
            throw new RuntimeException("Watch out for scammers!");
        }
        values[a][b] = rate;
        values[b][a] = 1 / rate;
    }

    double getDistance(final Integer a, final Integer b) {
        if (values[a][b] > 0) {
            return values[a][b];
        }

        if (values[b][a] > 0) {
            values[a][b] = 1 / values[b][a];
            return values[a][b];
        }

        inUse[a] = true;

        for (int i = 0; i < nodes; i++) {
            if (i != b && !inUse[i] && values[a][i] > 0) {
                double v = getDistance(i, b);
                if (v > 0) {
                    v *= values[a][i];
                    values[a][b] = v;
                    inUse[a] = false;
                    return v;
                }
            }
        }

        inUse[a] = false;
        return -1;
    }
}

public class CurrencyExchanger {
    private final ExchangeInput[] exchangeRates;
    private final Map<String, Integer> currencyMap = new HashMap<>();
    private Graph graph = null;

    public CurrencyExchanger(final ExchangeInput[] givenExchangeRates) {
        exchangeRates = givenExchangeRates;
    }

    /**
     * Implemented a proxy strategy so the graph is initialised
     *      only if an exchange is solicited.
     */
    private Graph getGraph() {
        if (graph == null) {
            createGraph();
        }
        return graph;
    }

    private void createGraph() {
        int nodes = 0;
        for (ExchangeInput exchange : exchangeRates) {
            for (String str : new String[] {exchange.getFrom(), exchange.getTo()}) {
                if (!currencyMap.containsKey(str)) {
                    currencyMap.put(str, nodes++);
                }
            }
        }
        graph = new Graph(nodes);
        for (ExchangeInput exchange : exchangeRates) {
            graph.addEdge(currencyMap.get(exchange.getFrom()),
                    currencyMap.get(exchange.getTo()), exchange.getRate());
        }
    }

    /** Calculates the exchange rate between 2 currencies. */
    public double convert(final String from, final String to) {
        if (from.equals(to)) {
            return 1;
        }

        double dst = getGraph().getDistance(currencyMap.get(from), currencyMap.get(to));
        if (dst < 0) {
            throw new RuntimeException("Cannot convert " + from + " to " + to);
        }
        return dst;
    }

}

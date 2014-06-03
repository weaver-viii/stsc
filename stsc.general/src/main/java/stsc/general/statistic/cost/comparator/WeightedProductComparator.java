package stsc.general.statistic.cost.comparator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import stsc.general.statistic.Statistics;

public class WeightedProductComparator implements StatisticsComparator {

	private final Map<String, Double> parameters = new HashMap<>();

	public WeightedProductComparator() {
		parameters.put("getAvGain", 1.0);
	}

	public void addParameter(String name, Double value) {
		parameters.put(name, value);
	}

	@Override
	public int compare(Statistics o1, Statistics o2) {
		Double sum = 0.0;
		for (Double d : parameters.values()) {
			sum += d;
		}
		Double result = 1.0;
		for (Entry<String, Double> i : parameters.entrySet()) {
			final Double v1 = Statistics.invokeMethod(o1, i.getKey());
			final Double v2 = Statistics.invokeMethod(o2, i.getKey());
			if (Double.compare(0.0, v2) == 0 || Double.compare(0.0, v1) == 0) {
				continue;
			}
			final Double w = i.getValue() / sum;
			result *= Math.pow((v1 / v2), w);
		}
		return (int) (result - 1.0);
	}
}

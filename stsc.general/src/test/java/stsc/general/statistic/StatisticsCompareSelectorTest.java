package stsc.general.statistic;

import java.util.Iterator;

import org.joda.time.LocalDate;

import stsc.common.Settings;
import stsc.general.statistic.cost.comparator.CostWeightedSumComparator;
import stsc.general.strategy.TradingStrategy;
import stsc.general.testhelper.TestStatisticsHelper;
import junit.framework.TestCase;

public class StatisticsCompareSelectorTest extends TestCase {
	public void testStatisticsCompareSelector() {
		final CostWeightedSumComparator c = new CostWeightedSumComparator();
		c.addParameter("getWinProb", 5.0);
		c.addParameter("getAvLoss", 14.0);
		final StatisticsCompareSelector sel = new StatisticsCompareSelector(3, c);

		sel.addStrategy(TradingStrategy.createTest(TestStatisticsHelper.getStatistics(50, 150, new LocalDate(2013, 5, 8))));
		sel.addStrategy(TradingStrategy.createTest(TestStatisticsHelper.getStatistics(50, 150, new LocalDate(2013, 5, 4))));
		sel.addStrategy(TradingStrategy.createTest(TestStatisticsHelper.getStatistics(50, 150, new LocalDate(2013, 5, 16))));
		sel.addStrategy(TradingStrategy.createTest(TestStatisticsHelper.getStatistics(50, 150, new LocalDate(2013, 5, 12))));

		assertEquals(3, sel.getStrategies().size());
		final Iterator<TradingStrategy> si = sel.getStrategies().iterator();
		assertEquals(0.254668, si.next().getAvGain(), Settings.doubleEpsilon);		
		assertEquals(-0.929453, si.next().getAvGain(), Settings.doubleEpsilon);
		assertEquals(-1.052631, si.next().getAvGain(), Settings.doubleEpsilon);
//		assertEquals(-0.902797, si.next().getAvGain(), Settings.doubleEpsilon);
	}

}

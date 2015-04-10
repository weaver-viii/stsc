package stsc.integration.tests.algorithms.stock.indices.macd;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import stsc.algorithms.Input;
import stsc.algorithms.stock.indices.macd.MacdDivergence;
import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.Settings;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.stocks.Stock;
import stsc.common.stocks.UnitedFormatStock;
import stsc.integration.tests.helper.StockAlgoInitHelper;
import stsc.signals.DoubleSignal;

public class MacdDivergenceTest {

	@Test
	public void testMacdMacd() throws ParseException, BadAlgorithmException, IOException, BadSignalException {
		final StockAlgoInitHelper stockInit = new StockAlgoInitHelper("in", "aapl");
		final Input in = new Input(stockInit.getInit());

		final StockAlgoInitHelper macdDInit = new StockAlgoInitHelper("macdD", "aapl", stockInit.getStorage());
		macdDInit.getSettings().addSubExecutionName("in");
		final MacdDivergence macdD = new MacdDivergence(macdDInit.getInit());

		final Stock aapl = UnitedFormatStock.readFromUniteFormatFile("./test_data/aapl.uf");
		final int aaplIndex = aapl.findDayIndex(new LocalDate(2011, 9, 4).toDate());
		final ArrayList<Day> days = aapl.getDays();

		for (int i = aaplIndex; i < days.size(); ++i) {
			final Day day = days.get(i);
			in.process(day);
			macdD.process(day);

			final double macdSignal = stockInit.getStorage().getStockSignal("aapl", "macdD_MacdSignal", day.getDate())
					.getContent(DoubleSignal.class).getValue();
			final double macd = stockInit.getStorage().getStockSignal("aapl", "macdD_MacdSignal_Macd", day.getDate())
					.getContent(DoubleSignal.class).getValue();
			final double v = stockInit.getStorage().getStockSignal("aapl", "macdD", day.getDate()).getContent(DoubleSignal.class).getValue();

			Assert.assertEquals(macd - macdSignal, v, Settings.doubleEpsilon);
		}
	}
}
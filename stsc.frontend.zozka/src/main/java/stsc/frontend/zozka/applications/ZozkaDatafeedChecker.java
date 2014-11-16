package stsc.frontend.zozka.applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import stsc.common.stocks.Stock;
import stsc.common.storage.StockStorage;
import stsc.frontend.zozka.dialogs.StockListDialog;
import stsc.frontend.zozka.models.StockDescription;
import stsc.frontend.zozka.panes.StockDatafeedListPane;
import stsc.frontend.zozka.settings.ZozkaDatafeedCheckerHelper;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ZozkaDatafeedChecker extends Application {

	private Stage owner;

	@FXML
	private BorderPane borderPane;
	@FXML
	private final Label datafeedPathLabel = new Label();
	private String datafeedPath;
	private String datafeedPrefix;

	private StockDatafeedListPane dataStockList;
	private StockDatafeedListPane filteredStockDataList;

	public ZozkaDatafeedChecker() {
		datafeedPathLabel.setText("./test_data/");
	}

	@Override
	public void start(final Stage owner) throws Exception {
		this.owner = owner;
		owner.setScene(initializeGui());
		owner.setMinHeight(500);
		owner.setMinWidth(830);
		owner.setWidth(830);
		owner.show();
		connectDatafeedChange();
	}

	private void connectDatafeedChange() {
		datafeedPathLabel.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
				chooseFolder();
			}
		});
	}

	private Scene initializeGui() throws IOException {
		borderPane = new BorderPane();
		borderPane.setTop(datafeedPathLabel);
		final Scene scene = new Scene(borderPane);
		final SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.HORIZONTAL);
		dataStockList = new StockDatafeedListPane(owner, "Data");
		addData(splitPane, dataStockList);
		setOnDoubleClickTableAction(dataStockList);
		filteredStockDataList = new StockDatafeedListPane(owner, "Filtered data");
		addData(splitPane, filteredStockDataList);
		setOnDoubleClickTableAction(filteredStockDataList);
		borderPane.setCenter(splitPane);
		return scene;
	}

	private void setOnDoubleClickTableAction(StockDatafeedListPane listPane) {
		listPane.setOnMouseDoubleClick(new Function<StockDescription, Void>() {
			@Override
			public Void apply(StockDescription sd) {
				try {
					final String stockName = sd.getStock().getName();
					final Stock data = dataStockList.getStockStorage().getStock(stockName);
					final Stock filtered = filteredStockDataList.getStockStorage().getStock(stockName);
					final ZozkaDatafeedCheckerHelper helper = new ZozkaDatafeedCheckerHelper(stockName, dataStockList,
							filteredStockDataList, null);
					helper.checkStockAndAskForUser(sd, data, filtered, owner);
				} catch (Exception e) {
					Dialogs.create().owner(owner).showException(e);
				}
				return null;
			}
		});

	}

	private void addData(SplitPane splitPane, StockDatafeedListPane listPane) {
		splitPane.getItems().add(listPane);
	}

	public void datafeedEdit(final MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
			chooseFolder();
		}
	}

	private void chooseFolder() {
		final String path = datafeedPathLabel.getText();
		final File f = new File(path);

		final Action response = Dialogs.create().owner(owner).title("Datafeed Path").masthead("Do you want to change datafeed path?")
				.message("Current path is: " + path).showConfirm();
		if (response != Dialog.Actions.YES) {
			return;
		}
		final DirectoryChooser dc = new DirectoryChooser();
		if (f.exists()) {
			dc.setInitialDirectory(f);
		}
		final File result = dc.showDialog(owner);
		if (result != null && result.isDirectory()) {
			datafeedPathLabel.setText(result.getAbsolutePath());
			try {
				loadDatafeed();
			} catch (IOException e) {
				Dialogs.create().showException(e);
			}
		}
	}

	private void loadDatafeed() throws IOException {
		final List<String> prefixVariants = new ArrayList<>();
		for (char z = 'a'; z != 'z'; z++) {
			prefixVariants.add(String.valueOf(z));
		}
		prefixVariants.add("");
		final Optional<String> result = Dialogs.create().owner(owner).title("Select Prefix for StockStorage").masthead(null)
				.showChoices(prefixVariants);
		if (result.isPresent()
				&& ((datafeedPrefix == null || datafeedPrefix != result.get()) || (datafeedPath == null || datafeedPath != datafeedPathLabel
						.getText()))) {
			datafeedPath = datafeedPathLabel.getText();
			datafeedPrefix = result.get();
			runLoadDatafeed(result);
		}
	}

	private void runLoadDatafeed(final Optional<String> result) {
		final Predicate<String> predicate = (p) -> {
			return !p.startsWith(result.get());
		};
		dataStockList.loadDatafeed(datafeedPath + "/data", onDataEnd -> {
			filteredStockDataList.loadDatafeed(datafeedPath + "/filtered_data", onFilterEnd -> {
				checkLists();
				return null;
			}, predicate);
			return null;
		}, predicate);
	}

	private void checkLists() {
		checkThatStocksAreEqual();
	}

	private void checkThatStocksAreEqual() {
		final StockStorage dataStockStorage = dataStockList.getStockStorage();
		final StockStorage filteredDataStockStorage = filteredStockDataList.getStockStorage();

		final Set<String> allList = dataStockStorage.getStockNames();
		final Set<String> filteredList = filteredDataStockStorage.getStockNames();
		final Set<String> notEqualStockList = ZozkaDatafeedCheckerHelper.findDifferenceByDaysSizeAndStockFilter(dataStockStorage,
				filteredDataStockStorage, allList, filteredList);
		if (!notEqualStockList.isEmpty()) {
			runShowListDialog(dataStockStorage, filteredDataStockStorage, notEqualStockList);
		}
	}

	private void runShowListDialog(final StockStorage dataStockStorage, final StockStorage filteredDataStockStorage,
			final Set<String> notEqualStockList) {
		final StockListDialog stockListDialog = new StockListDialog(owner,
				"List of Stocks which have different days size at data and filtered data.");
		stockListDialog.setOnMouseDoubleClicked(sd -> {
			final String stockName = sd.getStock().getName();
			final Stock data = dataStockStorage.getStock(stockName);
			final Stock filtered = filteredDataStockStorage.getStock(stockName);
			final ZozkaDatafeedCheckerHelper helper = new ZozkaDatafeedCheckerHelper(stockName, dataStockList, filteredStockDataList,
					stockListDialog.getModel());
			helper.checkStockAndAskForUser(sd, data, filtered, owner);
			return null;
		});
		int index = 0;
		for (String stockName : notEqualStockList) {
			final Stock s = dataStockStorage.getStock(stockName);
			stockListDialog.getModel().add(new StockDescription(index++, s));
		}
		stockListDialog.show();
	}

	public static void main(String[] args) {
		Application.launch(ZozkaDatafeedChecker.class, args);
	}
}
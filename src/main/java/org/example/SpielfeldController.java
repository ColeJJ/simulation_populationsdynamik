package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpielfeldController implements Initializable {
    private Spielfeld spielfeld;
    private GraphicsContext gc;
    private int spielfeldHoehe = 320; //default-wert
    private int spielfeldBreite = 320; //default-wert
    private boolean initRandom = true; //default-wert
    private boolean inSimulation;
    private int generation = 0;
    private int nrOfThreads = 1;
    private int nrOfGenerationsPerRun = 600;

    public Canvas canvas = new Canvas();
    public HBox hbox;
    public Button toggleSimulationButton;
    public Button buttonReset;
    public ToggleGroup group = new ToggleGroup();
    public RadioButton rbThree;
    public RadioButton rbFive;
    public Spinner<Integer> spinnerNrOfThreads;
    public Spinner<Integer> spinnerNrOfGenerationPerRun;
    public Text generationDisplay;
    // bar chart
    public CategoryAxis xAxis    = new CategoryAxis();
    public NumberAxis yAxis = new NumberAxis();
    public BarChart<String, Number> barChart;
    XYChart.Series<String, Number> seriesBlack = new XYChart.Series<>();
    XYChart.Series<String, Number> seriesRed = new XYChart.Series<>();
    XYChart.Series<String, Number> seriesBlue = new XYChart.Series<>();
    XYChart.Series<String, Number> seriesYellow = new XYChart.Series<>();
    XYChart.Series<String, Number> seriesGreen = new XYChart.Series<>();
    XYChart.Series<String, Number> seriesOrange = new XYChart.Series<>();

    private Messung messungRun;
    private MessungenList messungenList = new MessungenList();
    public TableView<Messung> tableMessungen = new TableView<>();
    public TableColumn<Messung, Double> timeInMsColumn = new TableColumn<>("Ms");
    public TableColumn<Messung, Double> timeInSColumn = new TableColumn<>("Sec");

    private int gesamtAnzahlPixel;
    private int countBlack = 0;
    private int countBlue = 0;
    private int countRed = 0;
    private int countYellow = 0;
    private int countGreen = 0;
    private int countOrange = 0;

    private double probReproduction = 1; //default-wert
    private double probMovement = 5; //default-wert
    private double probSelection = 1; //default-wert

    public ExecutorService executor;
    public ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spielfeld = new Spielfeld();
        readJSONParams();
        setTableColumns();
        if(initRandom) {
            spielfeld.initialiseFields(spielfeldBreite, spielfeldHoehe, rbFive.isSelected());
        } else {
            spielfeld.initialiseFieldsStatic(spielfeldBreite, spielfeldHoehe, rbFive.isSelected());
        }
        this.drawSpielfeld();
    }

    private void setTableColumns() {
        timeInMsColumn.setCellValueFactory(new PropertyValueFactory<>("timeInMs"));
        timeInSColumn.setCellValueFactory(new PropertyValueFactory<>("timeInS"));
        tableMessungen.setItems(messungenList.getMessungen());
    }

    public void drawSpielfeld() {
        gc = canvas.getGraphicsContext2D();

        setColors();

        //RadioButton
        rbFive.setToggleGroup(group);
        rbThree.setToggleGroup(group);
        rbThree.setSelected(true);

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                resetSimulation();
            }
        });

        //Thread Anzahl
        SpinnerValueFactory<Integer> threadValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        spinnerNrOfThreads.setValueFactory(threadValueFactory);
        spinnerNrOfThreads.valueProperty().addListener((obs, oldValue, newValue) ->
                nrOfThreads = newValue);

        //Generation per Run Anzahl
        SpinnerValueFactory<Integer> generationValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 3200, nrOfGenerationsPerRun);
        spinnerNrOfGenerationPerRun.setValueFactory(generationValueFactory);
        spinnerNrOfGenerationPerRun.valueProperty().addListener((obs, oldValue, newValue) ->
                nrOfGenerationsPerRun = newValue);

        // Start/Stop Button
        toggleSimulationButton.setOnAction(actionEvent -> {
            inSimulation = !inSimulation;
            this.starteSimulation();
        });

        // Generation Label
        generationDisplay.setFont(Font.font("Verdana", FontWeight.BOLD, 13));

        //BarChart
        initBarChart();

    }

    public void setColors() {
        countBlack = 0;
        countBlue = 0;
        countRed = 0;
        countYellow = 0;
        countGreen = 0;
        countOrange = 0;
        for (int x = 0; x < spielfeld.getSpielfeldWidth(); x++) {
            for (int y = 0; y < spielfeld.getSpielfeldHight(); y++) {
                switch(spielfeld.getSpielfeld()[x][y]){
                    case 0: gc.getPixelWriter().setColor(x, y, Color.BLACK); countBlack++; break;
                    case 1: gc.getPixelWriter().setColor(x, y, Color.BLUE); countBlue++; break;
                    case 2: gc.getPixelWriter().setColor(x, y, Color.RED); countRed++; break;
                    case 3: gc.getPixelWriter().setColor(x, y, Color.YELLOW); countYellow++; break;
                    case 4: gc.getPixelWriter().setColor(x, y, Color.GREEN); countGreen++; break;
                    case 5: gc.getPixelWriter().setColor(x, y, Color.ORANGE); countOrange++; break;
                }
            }
        }
    }

    private void starteSimulation() {
        if(!inSimulation) {
            return;
        }
        if(executor != null) {
            executor.shutdown();
        }
        toggleSimulationButton.setDisable(true);
        executor = Executors.newFixedThreadPool(nrOfThreads);
        generateMessung();
        for (int i = 0; i<nrOfGenerationsPerRun; i++) {
            ParalleAusfuehrung paralleAusfuehrung = new ParalleAusfuehrung(spielfeld.getSpielfeld(), rbFive.isSelected(), nrOfThreads, probSelection, probReproduction,probMovement, executor);
            paralleAusfuehrung.setOnSucceeded((e) -> {
                if(inSimulation) {
                    generation++;
                    updateSpielfeld();
                    updateBarchartData();
                    handleMessung();
                }
            });
            taskExecutor.execute(paralleAusfuehrung);
        }
    }

    private void generateMessung() {
        messungRun = new Messung();
        messungRun.setStartTime(System.nanoTime());
    }

    private void handleMessung() {
        if(generation == nrOfGenerationsPerRun) {
            messungRun.setEndTime(System.nanoTime());
            messungRun.zeitBerechnen(generation);
            messungenList.add(messungRun);
        }
    }

    public void readJSONParams() {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream is = this.getClass().getResourceAsStream("/org/example/params.json");
        JsonNode jsonNode = null;

        try {
            jsonNode = objectMapper.readValue(is, JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        probReproduction = jsonNode.get("pReproduction").doubleValue();
        probMovement = jsonNode.get("pMovement").doubleValue();
        probSelection = jsonNode.get("pMovement").doubleValue();
        spielfeldBreite  = jsonNode.get("spielfeldbreite").asInt();
        spielfeldHoehe  = jsonNode.get("spielfeldhoehe").asInt();
        initRandom = jsonNode.get("initRandom").asBoolean();

        try {
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSpielfeld(){
        gc.clearRect(0, 0, spielfeld.getSpielfeldHight(), spielfeld.getSpielfeldHight());
        setColors();
        generationDisplay.setText("Generation: " + generation);
    }

    public void initBarChart() {
        yAxis.setLabel("Anzahl");
        xAxis.setCategories(FXCollections.<String>observableArrayList(Arrays.asList(
                "Spezies")));
        xAxis.setLabel("Generation: " + generation);
        barChart.setAnimated(false);

        seriesBlack.getData().add(new XYChart.Data<>("Spezies", countBlack));
        seriesBlack.setName("Leer: " + getPercentage(countBlack) + "%");
        seriesBlue.getData().add(new XYChart.Data<>("Spezies", countBlue));
        seriesBlue.setName("Blau: " + getPercentage(countBlue) + "%");
        seriesRed.getData().add(new XYChart.Data<>("Spezies", countRed));
        seriesRed.setName("Rot: " + getPercentage(countRed) + "%");
        seriesYellow.getData().add(new XYChart.Data<>("Spezies", countYellow));
        seriesYellow.setName("Gelb: " + getPercentage(countYellow) + "%");
        seriesOrange.getData().add(new XYChart.Data<>("Spezies", countOrange));
        seriesOrange.setName("Orange: " + getPercentage(countOrange) + "%");
        seriesGreen.getData().add(new XYChart.Data<>("Spezies", countGreen));
        seriesGreen.setName("Grün: " + getPercentage(countGreen) + "%");


        barChart.getData().addAll(seriesBlack, seriesBlue, seriesRed, seriesYellow, seriesOrange,seriesGreen);

        // CSS-Datei einbinden
        String css = getClass().getResource("chartstyle.css").toExternalForm();
        barChart.getStylesheets().add(css);
    }

    public void updateBarchartData() {
        xAxis.setLabel("Generation: " + generation);
        seriesBlack.getData().get(0).setYValue(countBlack);
        seriesBlack.setName("Leer: " + getPercentage(countBlack) + "%");
        seriesBlue.getData().get(0).setYValue(countBlue);
        seriesBlue.setName("Blau: " + getPercentage(countBlue) + "%");
        seriesRed.getData().get(0).setYValue(countRed);
        seriesRed.setName("Rot: " + getPercentage(countRed) + "%");
        seriesYellow.getData().get(0).setYValue(countYellow);
        seriesYellow.setName("Gelb: " + getPercentage(countYellow) + "%");
        seriesOrange.getData().get(0).setYValue(countOrange);
        seriesOrange.setName("Orange: " + getPercentage(countOrange) + "%");
        seriesGreen.getData().get(0).setYValue(countGreen);
        seriesGreen.setName("Grün: " + getPercentage(countGreen) + "%");
    }

    public void resetSimulation() {
        inSimulation = false;
        generation = 0;
        toggleSimulationButton.setDisable(false);
        if(initRandom) {
            spielfeld.initialiseFields(spielfeldBreite, spielfeldHoehe, rbFive.isSelected());
        } else {
            spielfeld.initialiseFieldsStatic(spielfeldBreite, spielfeldHoehe, rbFive.isSelected());
        }
        updateSpielfeld();
        updateBarchartData();
    }

    public void resetMeasureTable() {
        messungenList.clear();
        tableMessungen.getItems().clear();
    }

    public String getPercentage(int count){
        DecimalFormat df = new DecimalFormat("0.00");
        gesamtAnzahlPixel = spielfeldBreite * spielfeldHoehe;
        float value = (count * 100.0f) / gesamtAnzahlPixel;
        df.setRoundingMode(RoundingMode.UP);
        String percent = df.format(value);
        return percent;
    }
}

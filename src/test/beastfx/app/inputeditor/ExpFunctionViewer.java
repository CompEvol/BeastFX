package test.beastfx.app.inputeditor;



import java.text.DecimalFormat;
import java.util.Arrays;

import beast.base.core.Log;
import beast.base.evolution.substitutionmodel.Frequencies;
import beast.base.evolution.substitutionmodel.GeneralSubstitutionModel;
import beast.base.inference.parameter.RealParameter;
import beast.base.util.Randomizer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ExpFunctionViewer extends javafx.application.Application {
	final static int N = 6;
	
	double a0 = 0.25, a1 = 0.0, b1=0.25, b2 = 0.75, a2=-0.25;
	LineChart<Number,Number> chart;
	Label a0label, a1label, a2label, b1label, b2label;
	DecimalFormat format = new DecimalFormat("#.##");
	GeneralSubstitutionModel gtr;
	TableView<RateRow> table;
	
	class RateRow {
		double [] r;
		int rowNr;
		
		public Double getR0() {
			return r[0];
		}
		public void setR0(Double r0) {
			this.r[0] = r0;
			normalise();
		}
		public Double getR1() {
			return r[1];
		}
		public void setR1(Double r1) {
			this.r[1] = r1;
			normalise();
		}
		public Double getR2() {
			return r[2];
		}
		public void setR2(Double r2) {
			this.r[2] = r2;
			normalise();
		}
		public Double getR3() {
			return r[3];
		}
		public void setR3(Double r3) {
			this.r[3] = r3;
			normalise();
		}
		public Double getR4() {
			return r[4];
		}
		public void setR4(Double r4) {
			this.r[4] = r4;
			normalise();
		}
		public Double getR5() {
			return r[5];
		}
		public void setR5(Double r5) {
			this.r[5] = r5;
			normalise();
		}
		public Double getR6() {
			return r[6];
		}
		public void setR6(Double r6) {
			this.r[6] = r6;
			normalise();
		}

		
		RateRow(int rowNr) {
			this.rowNr = rowNr;
			r = new double[7];
			for (int i = 0; i < N; i++) {
				r[i] = Randomizer.nextDouble();
			}
		}
		public void setR(int i, Double newValue) {
			r[i] = newValue;
			normalise();
		}
		public Double getR(int i) {
			return r[i];
		}
	}
	
	private void normalise() {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < i; j++) {
				rows.get(i).r[j] = rows.get(j).r[i];
			}
		}
		
		for (int j = 0; j < N; j++) {
			double sum = 0;
			RateRow row = rows.get(j);
			for (int i = 0; i < N; i++) {
				if (i != j) {
					sum += row.r[i];
				}
			}
			row.r[j] = -sum;
		}
		
		table.refresh();
	}

    ObservableList<RateRow> rows;

	@Override
	public void start(Stage primaryStage) throws Exception {
		setUpGTR();
		
		SplitPane pane = new SplitPane();
		VBox ctrlPane = new VBox();
		ctrlPane.setPrefWidth(200);
		pane.getItems().add(ctrlPane);
		
		// create control panel
		Slider a0Slider = new Slider(0, 1, 0.25);
		Slider a1Slider = new Slider(-1, 1, 0.0);
		Slider b1Slider = new Slider(0, 5, 0.25);
		Slider b2Slider = new Slider(0, 5, 0.75);

		a0label = new Label("a0=0.25");
		a1label = new Label("a1=0.0");
		a2label = new Label("a2=-0.25");
		b1label = new Label("b1=0.5");
		b2label = new Label("b2=0.5");
		
		ctrlPane.getChildren().addAll(a0label, a0Slider,
				a1label, a1Slider, 
				a2label,
				b1label,
				b1Slider, b2label,b2Slider);
		a0Slider.valueProperty().addListener((ov, oldValue, new_val) -> {
        	a0 = new_val.doubleValue();
        	repaint();
		});
		a1Slider.valueProperty().addListener((ov, oldValue, new_val) -> {
        	a1 = new_val.doubleValue();
        	repaint();
		});
		b1Slider.valueProperty().addListener((ov, oldValue, new_val) -> {
        	b1 = new_val.doubleValue();
        	repaint();
		});
		b2Slider.valueProperty().addListener((ov, oldValue, new_val) -> {
        	b2 = new_val.doubleValue();
        	repaint();
		});

		table = new TableView<>();
		for (int i = 0; i < N; i++) {
	        TableColumn<RateRow, String> col = new TableColumn<>("R" + i);
	        col.setPrefWidth(65);
	        col.setEditable(true);
	        col.setOnEditCommit(event-> {
	        	try {
	        	String str = event.getNewValue();
							Double newValue = Double.parseDouble(str);
							RateRow rateRow = event.getRowValue();
							String colName = event.getTableColumn().getText();
							int colNr = Integer.parseInt(colName.substring(1));
							rateRow.setR(colNr, newValue);
							repaint();
							System.err.println(colNr + " " + newValue);
	        	} catch (NumberFormatException e) {
	        		// ignore
	        	}
					});
	        col.setCellFactory(TextFieldTableCell.forTableColumn());

	        col.setCellValueFactory(event -> {
	        	SimpleObjectProperty<String> property = new SimpleObjectProperty<>();
				String colName = event.getTableColumn().getText();
				int colNr = Integer.parseInt(colName.substring(1));
	            property.setValue(event.getValue().getR(colNr) + "");
	            return property;
	        });
	        table.getColumns().add(col);
		}
		
		rows = FXCollections.observableArrayList();
		for (int i = 0; i < N; i++) {
			RateRow rateRow = new RateRow(i);
			rows.add(rateRow);
		}
		normalise();
		
		table.setItems(rows);
        table.setEditable(true);
        ctrlPane.getChildren().add(table);
        table.setPrefHeight(24 * N+40);
        
        for (Node n : ctrlPane.getChildren()) {
        	((Control)n).setPadding(new Insets(4));
        }

		
		// create chart
		NumberAxis xAxis = new NumberAxis(0, 10, 1);
        xAxis.setLabel("Time t");                
        NumberAxis yAxis = new NumberAxis(0, 2.0/N, 0.4/N);        
        yAxis.setLabel("P(t)");
        chart = new LineChart<Number,Number>(xAxis,yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        calcSeries();
        pane.getItems().add(chart);
		pane.setDividerPositions(0.33,0.67);
       		
		
        pane.setPrefWidth(1024);
        pane.setPrefHeight(600);
		Scene scene = new Scene(pane);
		primaryStage.setScene(scene);
		primaryStage.show();

		primaryStage.setOnCloseRequest((event) -> {
		    System.exit(0);
		});
	}       

	private void setUpGTR() {
		gtr = new GeneralSubstitutionModel();
		Double [] freqs = new Double[N];
		for (int i = 0; i < N; i++) {
			freqs[i] = new Double (1.0/N);
		}
		RealParameter freqParam = new RealParameter(freqs);
		Frequencies fs = new Frequencies();
		fs.initByName("frequencies", freqParam);
		RealParameter rates = new RealParameter();
		rates.initByName("dimension", N*(N-1),"value", "1.0");
		gtr.initByName("frequencies", fs, "rates", rates);
	}

	private void repaint() {
		//chart.getData().clear();
		calcSeries();
		a0label.setText("a0=" + format.format(a0));
		a1label.setText("a1=" + format.format(a1));
		a2label.setText("a2=" + format.format(a2));
		b1label.setText("b1=" + format.format(b1));
		b2label.setText("b2=" + format.format(b2));

	}

	private void calcSeries() {
		if (chart.getData().size() == 0) {
			LineChart.Series<Number,Number> series1 = new LineChart.Series<>();
	        for (int i = 0; i < 100; i++) {
	        	series1.getData().add(new XYChart.Data(0,0));
	        }
	        chart.getData().addAll(series1);

	        for (int k = 0; k < N; k++) {
				LineChart.Series<Number,Number> series = new LineChart.Series<>();
		        for (int i = 0; i < 100; i++) {
		        	series.getData().add(new XYChart.Data(0,0));
		        }
		        chart.getData().addAll(series);
	        }
		}
		
        for (int i = 0; i < 100; i++) {
        	double x = (i+0.0)/(10);
        	a2= -a0 - a1;
        	double y = a0 + a1 * Math.exp(-b1 * x) + a2 * Math.exp(-b2 * x);
        	Data<Number, Number>p = chart.getData().get(0).getData().get(i);
        	p.setXValue(x);
        	p.setYValue(y);
            //series1.getData().add(new XYChart.Data(x, y));
        }
        

        RealParameter rates = (RealParameter) gtr.ratesInput.get();
        int k = 0;
        for (int i = 0; i < N; i++) {
        	for (int j = 0; j < N; j++) {
        		if (i != j) {
        			double r = rows.get(i).getR(j);
        			rates.setValue(k++, r);
        		}
        	}
        }
        double [] matrix= new double[N*N];
        gtr.initAndValidate();
        Log.warning(Arrays.toString(gtr.getEigenDecomposition(null).getEigenValues()));
        for (int i = 0; i < N; i++) {
        	RateRow row = rows.get(i);
        	for (int j = 0; j < N; j++) {
        		System.out.print(format.format(row.r[j]) + " ");
        	}
        	System.out.println();
        }
        
        for (int i = 0; i < 100; i++) {
        	double x = (i+0.0)/(10);
        	gtr.getTransitionProbabilities(null, x, 0, 1.0, matrix);
            for (k = 1; k < N; k++) {
            	double y = matrix[k];
            	Data<Number, Number>p = chart.getData().get(k).getData().get(i);
            	p.setXValue(x);
            	p.setYValue(y);
            }
            //series1.getData().add(new XYChart.Data(x, y));
        }

	}

	public static void main(String[] args) {
	    launch();
	}
	
}

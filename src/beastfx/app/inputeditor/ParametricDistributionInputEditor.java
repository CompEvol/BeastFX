package beastfx.app.inputeditor;





import java.io.PrintWriter;
import java.io.StringWriter;

//import javax.swing.UIManager;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;

import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.evolution.tree.MRCAPrior;
import beast.base.evolution.tree.TreeDistribution;
import beast.base.inference.distribution.ParametricDistribution;
import beastfx.app.util.FXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ParametricDistributionInputEditor extends BEASTObjectInputEditor {

	public ParametricDistributionInputEditor() {
		super();
	}
    public ParametricDistributionInputEditor(BeautiDoc doc) {
		super(doc);
	}

    boolean useDefaultBehavior;
	boolean mayBeUnstable;

    @Override
    public Class<?> type() {
        //return ParametricDistributionInputEditor.class;
        return ParametricDistribution.class;
    }

    @Override
    public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption, boolean addButtons) {
        useDefaultBehavior = !((beastObject instanceof beast.base.inference.distribution.Prior) || beastObject instanceof MRCAPrior || beastObject instanceof TreeDistribution);

//    	if (useDefaultBehavior && false) {
//    		super.init(input, beastObject, isExpandOption, addButtons);
//    	} else {
        m_bAddButtons = addButtons;
        m_input = input;
        m_beastObject = beastObject;
		this.itemNr = itemNr;
        if (input.get() != null) {
            super.init(input, beastObject, itemNr, ExpandOption.TRUE, addButtons);
        } else {
        	pane = new HBox();
        }
        Pane pane1 = pane;
        registerAsListener(pane);        
        pane = FXUtils.newHBox();
        pane.getChildren().add(pane1);
        pane.getChildren().add(createGraph());
        getChildren().add(pane);
//    	}
    } // init


    private void registerAsListener(Node node) {
		if (node instanceof InputEditor) {
			((InputEditor)node).addValidationListener(_this);
		}
		if (node instanceof Pane) {
			for (Node child : ((Pane)node).getChildren()) {
				registerAsListener(child);
			}
		}
	}
    
	@Override
    /** suppress combobox **/
    protected void addComboBox(Pane box, Input<?> input, BEASTInterface beastObject0) {
        if (useDefaultBehavior) {
        	super.addComboBox(box, input, beastObject0);
        }
    }

    @Override
    /** suppress input label**/
    protected void addInputLabel() {
        if (useDefaultBehavior) {
            super.addInputLabel();
        }
    }

    /**
     * maps most significant digit to nr of ticks on graph *
     */
    final static int[] NR_OF_TICKS = new int[]{5, 10, 8, 6, 8, 10, 6, 7, 8, 9, 10};

    PDPanel graphPanel;
    
    /* class for drawing information for a parametric distribution **/
    class PDPanel extends VBox {
    	
    	LineChart<Number,Number> chart;
    	LineChart.Series<Number,Number> series;
    	Label infoLabel1;
    	Label infoLabel2;
    	
        // the length in pixels of a tick
        private static final int TICK_LENGTH = 5;

        // the right margin
        private static final int RIGHT_MARGIN = 20;

        // the margin to the left of y-labels
        private static final int MARGIN_LEFT_OF_Y_LABELS = 5;

        // the top margin
        private static final int TOP_MARGIN = 10;

        int m_nTicks;

        PDPanel() {
    		NumberAxis xAxis = new NumberAxis();
            //xAxis.setLabel("x");                
            NumberAxis yAxis = new NumberAxis();        
            yAxis.setLabel("p(x)");
            chart = new LineChart<Number,Number>(xAxis,yAxis);
            //chart.setAnimated(false);
            chart.setLegendVisible(false);
            chart.setCreateSymbols(false);
            chart.getXAxis().setAutoRanging(true);
            chart.getYAxis().setAutoRanging(true);
            series = new LineChart.Series<>();
	        for (int i = 0; i < 100; i++) {
	        	series.getData().add(new XYChart.Data<Number,Number>(0,0));
	        }
	        chart.getData().add(series);
	        getChildren().add(chart);
	        
	    	infoLabel1 = new Label();
	    	infoLabel1.setStyle("-fx-font-size:6pt;");
	    	infoLabel1.setPadding(new Insets(0, 100, 0, MARGIN_LEFT_OF_Y_LABELS));
	    	infoLabel2 = new Label();
	    	infoLabel2.setStyle("-fx-font-size:6pt;");
	    	HBox box = new HBox();
	    	// box.setSpacing(50);
	    	box.setAlignment(Pos.CENTER);
	    	box.getChildren().addAll(infoLabel1, infoLabel2);
	    	getChildren().add(box);
        }
        
        // @Override
        synchronized private void paintComponent() {
        	
        	//GraphicsContext g = getGraphicsContext2D();
            // g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Record current font, since drawError can take over part-way
            // through the call to drawGraph, which alters the graphics font size.
            // Font originalFont = g.getFont();

            ParametricDistribution m_distr = (ParametricDistribution)m_input.get();
            if (m_distr == null) {
                drawError();
            } else {
                try {
                    m_distr.initAndValidate();
                    //int fsize = UIManager.getFont("Label.font").getSize();
                    drawGraph(m_distr);//, fsize * 9/2);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    // ex.printStackTrace();
                    //g.setFont(originalFont);
                    drawError();
                }
            }

        }

        private void drawError() {
        	// chart.getData().clear();
//            g.setFill(Color.WHITE);
//            g.fillRect(0, 0, getWidth(), getHeight());
//            g.setStroke(Color.BLACK);
//            g.rect(0, 0, getWidth()-1, getHeight()-1);
//
//            String errorString = "Cannot display distribution.";
//            
//            int stringWidth = stringWidth(errorString);
//            int stringHeight = stringHeight(errorString);
//            g.strokeText(errorString,
//                    (getWidth() - stringWidth)/2,
//                    (getHeight() - stringHeight)/2);
        }

		private void drawGraph(ParametricDistribution m_distr) {//, int labelOffset) {
            final int width = (int)getWidth();
            final int height = (int)getHeight();

            double minValue = 0.1;
            double maxValue = 1;
            try {
                minValue = m_distr.inverseCumulativeProbability(0.01);
                maxValue = m_distr.inverseCumulativeProbability(0.99);
            } catch (Exception e) {
                // use defaults
            }
            double xRange = maxValue - minValue;
            // adjust yMax so that the ticks come out right
            double x0 = minValue;
            int k = 0;
            double f = xRange;
            double f2 = x0;
            while (f > 10) {
                f /= 10;
                f2 /= 10;
                k++;
            }
            while (f < 1 && f > 0) {
                f *= 10;
                f2 *= 10;
                k--;
            }
            f = Math.ceil(f);
            f2 = Math.floor(f2);
//			final int NR_OF_TICKS_X = NR_OF_TICKS[(int) f];
            for (int i = 0; i < k; i++) {
                f *= 10;
                f2 *= 10;
            }
            for (int i = k; i < 0; i++) {
                f /= 10;
                f2 /= 10;
            }
            //double adjXRange = f;

            xRange = xRange + minValue - f2;
            xRange = adjust(xRange);
            final int NR_OF_TICKS_X = m_nTicks;

            minValue = f2; //xRange = adjXRange;

            int points;
            if (!m_distr.isIntegerDistribution()) {
                points = 100;
            } else {
                points = (int) (xRange);
            }
            double[] xPoints = new double[points];
            double[] yPoints = new double[points];
            double[] fyPoints = new double[points];
            double yMax = 0;
            for (int i = 0; i < points; i++) {
                //try {
            	xPoints[i] = minValue + (xRange * i) / points;
                fyPoints[i] = getDensityForPlot(m_distr, minValue + (xRange * i) / points);
                //}
                if (Double.isInfinite(fyPoints[i]) || Double.isNaN(fyPoints[i])) {
                    fyPoints[i] = 0;
                }
                //fyPoints[i] = Math.exp(m_distr.logDensity(minValue + (xRange * i)/points));
                yMax = Math.max(yMax, fyPoints[i]);
            }

            yMax = adjust(yMax);
            final int NR_OF_TICKS_Y = m_nTicks;
            
            
//            // draw ticks on edge
//            Font font = g.getFont();
//            Font smallFont = new Font(font.getName(), font.getSize() * 2/3);
//            g.setFont(smallFont);
//
//            // collect the ylabels and the maximum label width in small font
//            String[] ylabels = new String[NR_OF_TICKS_Y+1];
//            int maxLabelWidth = 0;
//            //FontMetrics sfm = getFontMetrics(smallFont);
//            for (int i = 0; i <= NR_OF_TICKS_Y; i++) {
//                ylabels[i] = format(yMax * i / NR_OF_TICKS_Y);
//                double stringWidth = stringWidth(ylabels[i]);
//                if (stringWidth > maxLabelWidth) maxLabelWidth = (int) stringWidth;
//            }
//
//            // collect the xlabels
//            String[] xlabels = new String[NR_OF_TICKS_X+1];
//            for (int i = 0; i <= NR_OF_TICKS_X; i++) {
//                xlabels[i] = format(minValue + xRange * i / NR_OF_TICKS_X);
//            }
//            int maxLabelHeight = stringHeight("Hfg");//sfm.getMaxAscent()+sfm.getMaxDescent();
//
//            int leftMargin = maxLabelWidth + TICK_LENGTH + 1 + MARGIN_LEFT_OF_Y_LABELS;
//            int bottomMargin = maxLabelHeight + TICK_LENGTH + 1;
//
//            int graphWidth = width - leftMargin - RIGHT_MARGIN;
//            int graphHeight = height - TOP_MARGIN - bottomMargin - labelOffset;
//
//            // DRAW GRAPH PAPER
//            g.setFill(Color.web("0xf0f0f0"));
//            g.fillRect(0, 0, getWidth(), getHeight());
//            g.setFill(Color.WHITE);
//            g.fillRect(leftMargin, TOP_MARGIN, graphWidth, graphHeight);
//            g.setStroke(Color.BLACK);
//            g.rect(leftMargin, TOP_MARGIN, graphWidth, graphHeight);


            for (int i = 0; i < points; i++) {
//                xPoints[i] = leftMargin + graphWidth * i / points;
//                yPoints[i] = 1 + (int) (TOP_MARGIN + graphHeight - graphHeight * fyPoints[i] / yMax);
            	Data<Number, Number> p = series.getData().get(i);
            	p.setXValue(xPoints[i]);
            	p.setYValue(fyPoints[i]);
            }
            synchronized (this) {
                if (chart.getData().size() == 0) {
                	try {
                		chart.getData().add(series);
                	} catch (IllegalArgumentException e) {
                		// ignore
                	}
                }
			}

//            if (!m_distr.isIntegerDistribution()) {
//                g.strokePolyline(xPoints, yPoints, points);
//            } else {
//                int y0 = 1 + TOP_MARGIN + graphHeight;
//                int dotDiameter = graphHeight/20;
//                for (int i=0; i<points; i++) {
//                    g.strokeLine(xPoints[i], y0, xPoints[i], yPoints[i]);
//                    g.fillOval(xPoints[i]-dotDiameter/2, yPoints[i]-dotDiameter/2, dotDiameter, dotDiameter);
//                }
//            }
//
//            for (int i = 0; i <= NR_OF_TICKS_X; i++) {
//                int x = leftMargin + i * graphWidth / NR_OF_TICKS_X;
//                g.strokeLine(x, TOP_MARGIN + graphHeight, x, TOP_MARGIN + graphHeight + TICK_LENGTH);
//                g.strokeText(xlabels[i], x-stringWidth(xlabels[i])/2, TOP_MARGIN + graphHeight + TICK_LENGTH + 1 /* + sfm.getMaxAscent()*/);
//            }
//
//            // draw the y labels and ticks
//            for (int i = 0; i <= NR_OF_TICKS_Y; i++) {
//                int y = TOP_MARGIN + graphHeight - i * graphHeight / NR_OF_TICKS_Y;
//                g.strokeLine(leftMargin - TICK_LENGTH, y, leftMargin, y);
//                g.strokeText(ylabels[i], leftMargin - TICK_LENGTH - 1 - stringWidth(ylabels[i]), y + 3);
//            }

            
            //int fontHeight = (int)(font.getSize() * 10 / 12);
            //g.setFont(new Font(font.getName(), fontHeight));

            String info1 = "", info2 = "";
            String[] strs = new String[]{"2.5% Quantile", "5% Quantile", "Median", "95% Quantile", "97.5% Quantile"};
            Double[] quantiles = new Double[]{0.025, 0.05, 0.5, 0.95, 0.975};
            mayBeUnstable = false;
            for (k = 0; k < 5; k++) {

                //int y = TOP_MARGIN + graphHeight + bottomMargin + /* g.getFontMetrics().getMaxAscent() +*/ k * fontHeight;
                try {
                    info1 += format(m_distr.inverseCumulativeProbability(quantiles[k]));
                    //g.strokeText(format(m_distr.inverseCumulativeProbability(quantiles[k])), graphWidth / 2 + leftMargin, y);
                } catch (MathException | RuntimeException e) {
                	info1 += "not available";
                    //g.strokeText("not available", graphWidth / 2 + leftMargin, y);
                }
                info1 += strs[k];
                info1 += "\n";
                // g.strokeText(strs[k], graphWidth / 2 - stringWidth(strs[k]) + leftMargin - fontHeight, y);
            }
            if (mayBeUnstable) {
                //int x = graphWidth * 3/ 4 + leftMargin; int y =TOP_MARGIN + graphHeight + bottomMargin + fontHeight;
                info1 += "* numbers\n";
                info1 += "may not be\n";
                info1 += "accurate\n";
                //g.strokeText("* numbers", x, y + 2*fontHeight);
                //g.strokeText("may not be", x, y + 3*fontHeight);
                //g.strokeText("accurate", x, y + 4*fontHeight);
                
            }
            try {
            	info2 += "mean " + format(m_distr.getMean());
                //g.strokeText("mean " + format(m_distr.getMean()),
                //       graphWidth * 3/ 4 + leftMargin, TOP_MARGIN + graphHeight + bottomMargin + fontHeight);
            } catch (RuntimeException e) {
                // catch in case it is not implemented.
            }
            infoLabel1.setText(info1);
            infoLabel2.setText(info2);
        }
        
        private int stringWidth(String string) {
			Text text = new Text(string);
			text.applyCss();
			double width = text.getLayoutBounds().getWidth();
			return (int) width;
		}

        private int stringHeight(String string) {
			Text text = new Text(string);
			text.applyCss();
			double height = text.getLayoutBounds().getHeight();
			return (int) height;
		}

		private String format(double value) {
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            pw.printf("%.3g", value);
            if (value != 0.0 && Math.abs(value) / 1000 < 1e-320) { // 2e-6 = 2 * AbstractContinuousDistribution.solverAbsoluteAccuracy
            	mayBeUnstable = true;
            	pw.printf("*");
            }
            pw.flush();
            return writer.toString();
        }
        
        private double adjust(double yMax) {
            // adjust yMax so that the ticks come out right
            int k = 0;
            double y = yMax;
            while (y > 10) {
                y /= 10;
                k++;
            }
            while (y < 1 && y > 0) {
                y *= 10;
                k--;
            }
            y = Math.ceil(y);
            m_nTicks = NR_OF_TICKS[(int) y];
            for (int i = 0; i < k; i++) {
                y *= 10;
            }
            for (int i = k; i < 0; i++) {
                y /= 10;
            }
            return y;
        }
    }
    
    /**
     * Returns the density of pDistr at x when pDistr is a density of a
     * continuous variable, but returns the probability of the closest
     * integer when pDistr is a probability distribution over an integer-valued
     * parameter.
     * 
     * @param pDistr
     * @param x
     * @return density at x or probability of closest integer to x
     */
    private double getDensityForPlot(ParametricDistribution pDistr, double x) {
        if (pDistr.isIntegerDistribution()) {
            return pDistr.density((int) Math.round(x));
        } else {
            return pDistr.density(x);
        }
    }

    private Node createGraph() {
    	
    	graphPanel = new PDPanel();
//        int fsize = UIManager.getFont("Label.font").getSize();
//        Dimension2D size = new Dimension2D(200 * fsize / 13, 200 * fsize / 13);
//        //panel.setSize(size);
//        HBox box = FXUtils.newHBox();
//        //box.setBorder(BorderFactory.createEmptyBorder());
//        box.getChildren().add(graphPanel);        
//        box.setPrefSize(size.getHeight(), size.getWidth());
//        box.setMinSize(size.getHeight(), size.getWidth());
        graphPanel.paintComponent();
        return graphPanel;
//        return box;
    }
    
    
    @Override
    public void validateInput() {
		graphPanel.paintComponent();
		super.validateInput();
    }

//    @Override
//    public void validate() {
//        super.validate();
//        repaint();
//    }

}

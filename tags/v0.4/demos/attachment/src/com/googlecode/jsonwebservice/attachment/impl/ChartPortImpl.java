package com.googlecode.jsonwebservice.attachment.impl;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xml.DatasetReader;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;

import com.googlecode.jsonwebservice.attachment.ChartConfig;
import com.googlecode.jsonwebservice.attachment.ChartInput;
import com.googlecode.jsonwebservice.attachment.ChartOutput;
import com.googlecode.jsonwebservice.attachment.ChartPort;
import com.googlecode.jsonwebservice.attachment.Colors;
import com.googlecode.jsonwebservice.attachment.Size;
import com.googlecode.jsonwebservice.attachment.Visibility;
import com.googlecode.jsonwebservice.attachment.impl.SvgImageWriterSpi.SVGContentImage;

@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = "ChartPort", targetNamespace = "http://jsonwebservice.googlecode.com/attachment", endpointInterface = "com.googlecode.jsonwebservice.attachment.ChartPort")
public class ChartPortImpl implements ChartPort {

	@Override
	public ChartOutput getChart(ChartInput getChartInput) {
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			StringWriter reader = new StringWriter();

			CategoryDataset dataset = null;

			transformer.transform(getChartInput.getXmlData(), new StreamResult(
					reader));
			dataset = DatasetReader
					.readCategoryDatasetFromXML(new ByteArrayInputStream(reader
							.getBuffer().toString().getBytes()));

			// create the chart...
			final JFreeChart chart = ChartFactory.createBarChart("Bar Chart", // chart
																				// title
					"Domain", "Range", dataset, // data
					PlotOrientation.VERTICAL, true, // include legend
					true, false);
			updateSettings(chart,getChartInput.getChartConfig());
			ChartOutput outPut = new ChartOutput();
			outPut.setImage(getBufferedImage(chart, getChartInput
					.getChartConfig()));
			outPut.setSvg(getSVGImage(chart, getChartInput.getChartConfig()));

			return outPut;
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private BufferedImage getBufferedImage(JFreeChart chart, ChartConfig config) {
		Size size = config.getSize();
		int zoomLevel = size.getZoomLevel() > 0 ? size.getZoomLevel() : 1;
		int viewboxWidth = size.getWidth() / zoomLevel;
		int viewboxHeight = size.getHeight() / zoomLevel;
		return chart.createBufferedImage(viewboxWidth, viewboxHeight,
				BufferedImage.TYPE_INT_RGB, null);
	}

	private SVGContentImage getSVGImage(JFreeChart chart, ChartConfig config)
			throws SVGGraphics2DIOException {
		// Get a DOMImplementation
		DOMImplementation domImpl = GenericDOMImplementation
				.getDOMImplementation();
		// Create an instance of org.w3c.dom.Document
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		org.w3c.dom.Document document = domImpl.createDocument(svgNS, "svg",
				null);
		// Create an instance of the SVG Generator
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		// Ask the chart to render into the SVG Graphics2D implementation
		Size size = config.getSize();
		int zoomLevel = size.getZoomLevel() > 0 ? size.getZoomLevel() : 1;
		int viewboxWidth = size.getWidth() / zoomLevel;
		int viewboxHeight = size.getHeight() / zoomLevel;
		int chartWidth = viewboxWidth - (60 / zoomLevel);
		int chartHeight = viewboxHeight - (28 / zoomLevel);

		chart.draw(svgGenerator, new Rectangle2D.Double(10, 10, chartWidth,
				chartHeight), null);
		Element root = svgGenerator.getRoot();
		root.setAttributeNS(null, "viewBox", "0 0 " + viewboxWidth + " "
				+ viewboxHeight);
		// Finally, stream out SVG
		// character to byte encoding
		SVGContentImage svgImage = new SVGContentImage();
		svgGenerator.stream(root, svgImage.getWriter(), config.getVisibility()
				.isUseCSS(), false);
		return svgImage;
	}

	
	public void updateSettings(JFreeChart chart, ChartConfig chartConfig){
		Plot plot = chart.getPlot();
		
		Visibility 	visibility 	= chartConfig.getVisibility();
		Colors 		colors 		= chartConfig.getColors();
		
		// Chart level visible items
		chart.setBorderVisible(visibility.isBorder());
		plot.setOutlineVisible(visibility.isOutline());
		
		// chart level colors
		final Color  BACKGROUND	= new Color(colors.getBg());
		plot.setBackgroundPaint(BACKGROUND);
		chart.setBackgroundPaint(BACKGROUND);
		
		// Chart level title 
		if(chart.getTitle() != null){
			TextTitle title = chart.getTitle();
			title.setVisible(visibility.isTitle());
			title.setBackgroundPaint(new Color(colors.getTitleBg()));
			title.setPaint(new Color(colors.getTitle()));
			title.setExpandToFitSpace(true);
		}
		
		// In case of multiple plot read and update child level chart
		if(plot instanceof MultiplePiePlot){
			MultiplePiePlot multiplePiePlot = (MultiplePiePlot)plot;
			chart	= multiplePiePlot.getPieChart();
			plot 	= chart.getPlot();
			
			chart.setBorderVisible(visibility.isBorder());
			plot.setOutlineVisible(visibility.isOutline());
			
			chart.setBackgroundPaint(BACKGROUND);
			plot.setBackgroundPaint(BACKGROUND);
		}
		
		if(plot instanceof CategoryPlot){
			CategoryPlot 	categoryPlot 	= (CategoryPlot)chart.getPlot();
			categoryPlot.setRangeGridlinesVisible(visibility.isRangeGridLines());
			categoryPlot.setOrientation((visibility.getOrientation() != null &&
					visibility.getOrientation() == com.googlecode.jsonwebservice.attachment.PlotOrientation.HORIZONTAL)
					? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL);
			categoryPlot.setRangeZeroBaselineVisible(visibility.isRangeZeroBaseline());
			categoryPlot.setRangeCrosshairVisible(visibility.isRangeCrosshair());
			/**
			 * RANGE Axis settings
			 */
			ValueAxis 		rangeAxis  		= categoryPlot.getRangeAxis();
			
			rangeAxis.setAxisLineVisible(visibility.isAxisLine());
			rangeAxis.setTickMarksVisible(visibility.isTickMarks());
			rangeAxis.setMinorTickMarksVisible(visibility.isMinerTickMarks());
			
			Color axisLabel = new Color(colors.getAxisLabel());
			Color tickLabel = new Color(colors.getTickLabel());
			
			rangeAxis.setLabelPaint(axisLabel);
			rangeAxis.setTickLabelPaint(tickLabel);
			if(rangeAxis instanceof NumberAxis){
				NumberAxis numberAxis = (NumberAxis)rangeAxis;
				// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			}
			
			/**
			 * DOAMIN AXIS settings
			 */
			CategoryAxis 	domainAxis 		= categoryPlot.getDomainAxis();
			domainAxis.setAxisLineVisible(visibility.isAxisLine());
			domainAxis.setTickMarksVisible(visibility.isTickMarks());
			domainAxis.setMinorTickMarksVisible(visibility.isMinerTickMarks());
			
			domainAxis.setLabelPaint(axisLabel);
			domainAxis.setTickLabelPaint(tickLabel);
			
			domainAxis.setCategoryLabelPositions(
					CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
			
			CategoryItemRenderer rendrer = categoryPlot.getRenderer();
			for(int index = 0; index < colors.getSeries().size(); index++){
				rendrer.setSeriesPaint(index, new Color(colors.getSeries().get(index)));
			}
		}else if(plot instanceof PiePlot){
			PiePlot piePlot = (PiePlot)plot;
			
			piePlot.setSimpleLabels(visibility.isPieSimpleLabels());
			piePlot.setSectionOutlinesVisible(visibility.isPieOutlines());
			piePlot.setLabelLinksVisible(visibility.isPieLabelLinks());
			
			//piePlot.setExplodePercent("1", 0.07000000000000001D);
			//piePlot.setInteriorGap(0.00D);
			piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator(chartConfig.getDataSettings().getPieSectionLabel()));
			//piePlot.setLabelBackgroundPaint(new Color(220, 220, 220));
			for(int index = 0; index < colors.getSeries().size(); index++){
				piePlot.setSectionPaint(String.valueOf(index), new Color(colors.getSeries().get(index)));
			}
		}
	}
}

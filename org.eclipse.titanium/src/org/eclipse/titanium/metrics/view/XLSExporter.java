/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.MetricGroup;
import org.eclipse.titanium.metrics.preferences.PreferenceManager;
import org.eclipse.titanium.metrics.utils.RiskLevel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;


/**
 * A utility class that solves exporting the content of the {@link MetricsView}
 * to an excel file.
 * 
 * @author poroszd
 * 
 */
class XLSExporter {
	private File file;
	private MetricData data;

	public XLSExporter(final MetricData data) {
		this.data = data;
	}

	public void setFile(final File f) {
		file = f;
	}

	public void write(RiskLevel r) {
		try {
			HSSFWorkbook workbook = new HSSFWorkbook();

			for (final MetricGroup type : new MetricGroup[] { MetricGroup.MODULE, MetricGroup.ALTSTEP, MetricGroup.FUNCTION,MetricGroup.TESTCASE }) {
				for (IMetricEnum metric : type.getMetrics()) {
					if (!(PreferenceManager.isEnabledOnView(metric))) {
						continue;
					}

					ProjectStatNode pn = new ProjectStatNode(metric);
					if (!pn.hasChildren(data) || pn.getRiskLevel(data).compareTo(r) < 0) {
						continue;
					}

					Sheet sheet = workbook.createSheet(getSheetName(metric));
					
					printChildren(sheet, pn, 0, 0);
				}
			}

			workbook.write(new FileOutputStream(file));

		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Error while exporting to excel",e);
		}
	}
	
	/**
	 * Creates and returns the Excel sheet name belonging to the parameter.
	 * This text has to be unique and less than 32 characters long.
	 * 
	 * @param metric the metric
	 * @return the sheet name of the provided metric
	 * */
	private String getSheetName(final IMetricEnum metric) {
		StringBuilder builder = new StringBuilder(metric.getName());
		builder.append(" (").append(metric.groupName()).append(")");
		return builder.length() > 31 ? builder.substring(0, 31): builder.toString(); 	
	}
	
	
	protected int printChildren(Sheet sheet, IContentNode n, int col, int line) {
		Object[] objs = n.getChildren(data);
		List<IContentNode> cns = new ArrayList<IContentNode>();
		for (Object o : objs) {
			cns.add((IContentNode) o);
		}

		Collections.sort(cns, new CNComparator(data));

		int currentRow = line;
		for (IContentNode c : cns) {
			
			Row row = sheet.createRow(currentRow++);	
			Cell cell = row.createCell(col);		
			cell.setCellValue(c.getColumnText(data, 0));
			
			if (c.hasChildren(data)) {	
				currentRow = printChildren(sheet, c, col + 1, currentRow);
			} else {								
				Cell number = row.createCell(col + 1);
				number.setCellValue(Double.parseDouble(c.getColumnText(data, 1)));			
			}
		}
				
		return currentRow;
	}
	

	
}

class CNComparator implements Comparator<IContentNode> {
	private MetricData data;

	public CNComparator(MetricData data) {
		this.data = data;
	}

	@Override
	public int compare(final IContentNode e1, final IContentNode e2) {
		final Double d1 = e1.risk(data);
		final Double d2 = e2.risk(data);				

		return d2.compareTo(d1);
	}
	
}
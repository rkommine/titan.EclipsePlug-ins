/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.eclipse.titan.common.logging.ErrorReporter;


/**
 * A class used for merging code smell tables.
 * 
 * @author Gobor Daniel
 */
public class ExportedProblemMerger {

	private final List<File> files;
	private final File outfile;
	
	private String msg;

	private HSSFWorkbook outbook;
	private HSSFSheet summarysheet;

	private String project;
	private SortedSet<Date> dates;
	private Map<Date, Integer> datecol;
	private Map<Date, File> datefile;
	private Map<String, Integer> smellrow;
	private int smellindex;

	public ExportedProblemMerger(List<File> files, File outfile) {
		this.files = files;
		this.outfile = outfile;

		dates = new TreeSet<Date>();
		datecol = new HashMap<Date, Integer>();
		datefile = new HashMap<Date, File>();
		smellrow = new HashMap<String, Integer>();
		smellindex = 2;
	}

	public String getErrorMessage() {
		return msg;
	}

	/**
	 * Creates a workbook with the output file given in the constructor.
	 * 
	 * @return A new workbook on the outfile
	 */
	private HSSFWorkbook createWorkbook() {
		HSSFWorkbook workbook = null;
		try {		
			InputStream in = ExportedProblemMerger.class.getResourceAsStream("ProblemMarkers.xlt");
			
			if (in == null) {
				if (!outfile.exists()){
					outfile.createNewFile();
				}
				workbook = new HSSFWorkbook();
				workbook.createSheet("Summary");
				workbook.setSheetOrder("Summary", 0);
			} else {
				workbook = new HSSFWorkbook(new POIFSFileSystem(in), true);
				in.close();
			}
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while creating merged excel",e);
		}
		return workbook;
	}

	/**
	 * Find the project name in the given sheet.
	 * 
	 * @param sheet
	 *            The sheet
	 * @return The project name contained in the sheet
	 */
	private String getProjectName(HSSFSheet sheet) {
		final Cell cell = sheet.getRow(0).getCell(0);    
		return cell.getStringCellValue();
	}

	/**
	 * Collect the dates contained in a sheet.
	 * 
	 * @param file
	 *            The file that the date belongs to
	 * @param sheet
	 *            The sheet being processed
	 */
	private void collectDates(File file, HSSFSheet sheet) {
		
		final int cols = sheet.getRow(1).getLastCellNum();
		
		for (int col = 1; col < cols; ++col) {
			
			final Cell cell = sheet.getRow(1).getCell(col);
			// if not a deleted column
			if (cell.getCellType() != HSSFCell.CELL_TYPE_BLANK && cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {

				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					Date date = cell.getDateCellValue();
					if (!dates.contains(date)) {
						dates.add(date);
						datefile.put(date, file);
						datecol.put(date, col);
					}
				}
			}
		}
	}

	/**
	 * Collect the smell names contained in a sheet.
	 * 
	 * @param sheet
	 *            The sheet to process
	 */
	private void collectSmellNames(HSSFSheet sheet) {
		final int rows = sheet.getLastRowNum();
		int row = 2;
		while (row < rows) {
			HSSFRow actualRow = sheet.getRow(row);
			if (actualRow != null) {
				Cell cell = actualRow.getCell(0);
				final String name = cell.getStringCellValue();
				// new smell found
				if (!smellrow.containsKey(name) && !name.isEmpty()) {
					smellrow.put(name, smellindex);
					smellindex += 1;
				}
			}
			row += 1;
		}
	}

	/**
	 * Collect all data from the files given in the constructor.
	 */
	private void collectData() {
		boolean first = true;
		for (File file : files) {
			HSSFWorkbook workbook = null;
			try {
				workbook = new HSSFWorkbook(new FileInputStream(file));
				HSSFSheet sheet = workbook.getSheetAt(0);
				if (first) {
					project = getProjectName(sheet);
					first = false;
				} else {
					// check if it is the same project as the first
					if (!project.equals(getProjectName(sheet))) {
						continue;
					}
				}
				collectSmellNames(sheet);
				collectDates(file, sheet);
			} catch (ArrayIndexOutOfBoundsException e) {
				// outside content boundaries
				ErrorReporter.logExceptionStackTrace("Possibly wrong structure of " + file.getName(), e);
				System.out.println("Possibly wrong structure of " + file.getName());
			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace("Error while opening " + file.getName(), e);
				System.out.println("Error opening " + file.getName());
			} finally {
				if (workbook != null) {
					workbook = null;
				}
			}
		}
	}

	/**
	 * Write the project name and decoration.
	 * 
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	private void writeBasics() {
		Row row0 = summarysheet.createRow(0);
		row0.createCell(0).setCellValue(project);
		
		Row row1 = summarysheet.createRow(1);
		row1.createCell(0).setCellValue("Code smell \\ date");
	}
	/**
	 * Write the dates and smell data.
	 * @throws FileNotFoundException 
	 * 
	 * @throws RowsExceededException
	 * @throws WriteException
	 * @throws BiffException
	 * @throws IOException
	 */
	private void writeData() throws FileNotFoundException, IOException {
		
		int col = 1;
		for (Date date : dates) {
			if (col > 250) {
				System.out.println("could not process date " + date + "\t column limit exceeded.");
			} else {
				File file = datefile.get(date);
				System.out.println("Processing file: " + file.getName() + " | date: " + new SimpleDateFormat("yyyy.MM.dd").format(date));
				HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));
				HSSFSheet sheet = workbook.getSheetAt(0);
				
				// add date
				CellStyle cellStyle = outbook.createCellStyle();
				cellStyle.setDataFormat(outbook.getCreationHelper().createDataFormat().getFormat("yyyy.mm.dd"));
				Cell cell = summarysheet.getRow(1).createCell(col);
				cell.setCellValue(date);
				cell.setCellStyle(cellStyle);

				writeSmellData(sheet, date, col);

				++col;
								
				workbook = null;
			}
		}
	}

	/**
	 * Write the smell data.
	 * 
	 * @param sheet
	 *            The sheet from which to read the data
	 * @param date
	 *            The date which we write now
	 * @param col
	 *            The next column where the data will be written
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	private void writeSmellData(HSSFSheet sheet, Date date, int col) {
		int rows = sheet.getLastRowNum();
		int colinfile = datecol.get(date);
		for (int row = 2; row < rows; ++row)
		{
			HSSFRow actualRow = sheet.getRow(row);
			if(actualRow == null) {
				continue;
			}
			Cell cell = actualRow.getCell(0);
			String name = cell.getStringCellValue();
			// the number of smells
			cell = actualRow.getCell(colinfile);		
			if (cell.getCellType() != HSSFCell.CELL_TYPE_BLANK) {
				double value = cell.getNumericCellValue();				
				Row r = summarysheet.getRow(smellrow.get(name));	
				Cell number = r.createCell(col);
				number.setCellValue(value);	
			}
		}
	}

	/**
	 * Write the smell names.
	 * 
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	private void writeSmellNames() {
		for (String name : smellrow.keySet()) {
			Row row = summarysheet.createRow(smellrow.get(name));
			Cell label = row.createCell(0);
			label.setCellValue(name);
		}
	}
	
	/**
	 * Autosize all columns in summarysheet.
	 * 
	 */
	private void resizeColumns() {
		
		int numberOfColumns = datecol.size() + 1;
		for (int i = 0; i < numberOfColumns; ++i)
		{
			summarysheet.autoSizeColumn(i);
		}
	}

	/**
	 * Close the output workbook.
	 * @throws FileNotFoundException 
	 * @throws IOException
	 */
	private void close() throws FileNotFoundException, IOException {
		outbook.write(new FileOutputStream(outfile));
	}

	/**
	 * Run the algorithm which merges the given tables.
	 * 
	 * @return True if no exceptions occurred
	 */
	public boolean run() {
		outbook = createWorkbook();
		if (outbook == null) {
			return false;
		}

		summarysheet = outbook.getSheetAt(0);
		
		collectData();

		try {
			writeBasics();
			writeSmellNames();
			writeData();
			resizeColumns();
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while writing the merged data",e);
			System.out.println("Error writing output file");
			return false;
		}

		try {
			close();
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while closing the output file",e);
			System.out.println("Error closing output file");
			return false;
		}

		return true;
	}
}
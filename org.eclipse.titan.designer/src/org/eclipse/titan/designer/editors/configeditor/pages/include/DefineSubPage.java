/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.include;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.cfg.CfgLexer;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler.Definition;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kristof Szabados
 * */
public final class DefineSubPage {

	private Label totalDefineElementsLabel;
	private Table defineElementsTable;
	private TableViewer defineElementsTableViewer;
	private Button add;
	private Button remove;

	private static final String[] COLUMN_NAMES = new String[] { "definitionName", "definitionValue" };

	private ConfigEditor editor;
	private DefineSectionHandler defineSectionHandler;

	public DefineSubPage(final ConfigEditor editor) {
		this.editor = editor;
	}

	void createDefinitionSection(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);

		toolkit.paintBordersFor(client);

		defineElementsTable = toolkit.createTable(client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		defineElementsTable.setEnabled(defineSectionHandler != null);

		TableColumn column = new TableColumn(defineElementsTable, SWT.LEFT, 0);
		column.setText("Definition name");
		column.setMoveable(false);
		column.setWidth(150);

		column = new TableColumn(defineElementsTable, SWT.LEFT, 1);
		column.setText("Definition value");
		column.setWidth(300);
		column.setMoveable(false);

		defineElementsTable.setHeaderVisible(true);
		defineElementsTable.setLinesVisible(true);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		gd.heightHint = 200;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		defineElementsTable.setLayoutData(gd);

		Composite buttons = toolkit.createComposite(client);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL));
		add = toolkit.createButton(buttons, "Add...", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		add.setLayoutData(gd);
		add.setEnabled(defineSectionHandler != null);
		add.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (defineSectionHandler == null) {
					return;
				}

				if (defineSectionHandler.getLastSectionRoot() == null) {
					createNewDefineSection();
				}

				Definition newItem = createNewDefineItem();
				if (newItem == null) {
					return;
				}

				if (defineSectionHandler.getDefinitions().isEmpty()) {
					defineSectionHandler.getLastSectionRoot().setNextSibling(newItem.getRoot());
				} else {
					Definition item = defineSectionHandler.getDefinitions().get(defineSectionHandler.getDefinitions().size() - 1);
					item.getRoot().setNextSibling(newItem.getRoot());
				}

				defineSectionHandler.getDefinitions().add(newItem);

				internalRefresh();
				defineElementsTable.select(defineSectionHandler.getDefinitions().size() - 1);
				defineElementsTable.showSelection();
				editor.setDirty();
			}
		});

		remove = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		remove.setLayoutData(gd);
		remove.setEnabled(defineSectionHandler != null);
		remove.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (defineElementsTableViewer == null || defineSectionHandler == null) {
					return;
				}

				removeSelectedDefineItems();

				if (defineSectionHandler.getDefinitions().isEmpty()) {
					removeDefineSection();
				}

				internalRefresh();
				editor.setDirty();
			}

		});

		totalDefineElementsLabel = toolkit.createLabel(buttons, "Total defined: 0");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		totalDefineElementsLabel.setLayoutData(gd);

		section.setText("Definitions");
		section.setDescription("Specify the list of Definitions for this configuration.");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});

		gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);

		defineElementsTableViewer = new TableViewer(defineElementsTable);
		defineElementsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				// not needed this time
			}
		});

		defineElementsTableViewer.setContentProvider(new DefineDataContentProvider());
		defineElementsTableViewer.setLabelProvider(new DefineDataLabelProvider());
		defineElementsTableViewer.setInput(defineSectionHandler);
		defineElementsTableViewer.setColumnProperties(COLUMN_NAMES);
		defineElementsTableViewer.setCellEditors(new TextCellEditor[] { new TextCellEditor(defineElementsTable),
				new TextCellEditor(defineElementsTable) });
		defineElementsTableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(final Object element, final String property) {
				return true;
			}

			@Override
			public String getValue(final Object element, final String property) {
				int columnIndex = Arrays.asList(COLUMN_NAMES).indexOf(property);
				DefineDataLabelProvider labelProvider = (DefineDataLabelProvider) defineElementsTableViewer.getLabelProvider();
				return labelProvider.getColumnText(element, columnIndex);
			}

			@Override
			public void modify(final Object element, final String property, final Object value) {
				int columnIndex = Arrays.asList(COLUMN_NAMES).indexOf(property);
				if (element != null && element instanceof TableItem && value instanceof String) {
					Definition definition = (Definition) ((TableItem) element).getData();

					switch (columnIndex) {
					case 0:
						definition.getDefinitionName().setText(((String) value).trim());
						break;
					case 1:
						definition.getDefinitionValue().setText(((String) value).trim());
						break;
					default:
						break;
					}

					defineElementsTableViewer.refresh(definition);
					editor.setDirty();
				}
			}
		});

		defineElementsTableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { DefineItemTransfer.getInstance() },
				new DefineSectionDragSourceListener(this, defineElementsTableViewer));
		defineElementsTableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT, new Transfer[] { DefineItemTransfer
				.getInstance() }, new DefineSectionDropTargetListener(defineElementsTableViewer, editor));

		internalRefresh();
	}

	private void internalRefresh() {
		add.setEnabled(defineSectionHandler != null);
		remove.setEnabled(defineSectionHandler != null);
		defineElementsTable.setEnabled(defineSectionHandler != null);
		defineElementsTableViewer.setInput(defineSectionHandler);
		if (defineSectionHandler != null) {
			totalDefineElementsLabel.setText("Total: " + defineSectionHandler.getDefinitions().size());
		}
	}

	public void refreshData(final DefineSectionHandler defineSectionHandler) {
		this.defineSectionHandler = defineSectionHandler;

		if (defineElementsTableViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}

	private void createNewDefineSection() {
		if (defineSectionHandler == null) {
			return;
		}

		defineSectionHandler.setLastSectionRoot(new LocationAST("[DEFINE]"));
		defineSectionHandler.getLastSectionRoot().setHiddenBefore(new CommonHiddenStreamToken("\n"));
		LocationAST sectionRoot = new LocationAST("");
		sectionRoot.setFirstChild(defineSectionHandler.getLastSectionRoot());

		LocationAST root = editor.getParseTreeRoot();
		if (root != null) {
			root.addChild(sectionRoot);
		}
	}

	private Definition createNewDefineItem() {
		if (defineSectionHandler == null) {
			return null;
		}

		Definition item = new DefineSectionHandler.Definition();
		item.setRoot(new LocationAST(""));

		LocationAST node;
		item.setDefinitionName(new LocationAST("definition_name"));
		item.getDefinitionName().setHiddenBefore(new CommonHiddenStreamToken(CfgLexer.WS, "\n"));
		item.getRoot().setFirstChild(item.getDefinitionName());
		node = new LocationAST(" := ");
		item.getDefinitionName().setNextSibling(node);
		item.setDefinitionValue(new LocationAST("definition_value"));
		node.setNextSibling(item.getDefinitionValue());

		return item;
	}

	private void removeDefineSection() {
		if (defineSectionHandler == null || defineSectionHandler.getLastSectionRoot() == null) {
			return;
		}

		ConfigTreeNodeUtilities.removeFromChain(editor.getParseTreeRoot().getFirstChild(), defineSectionHandler.getLastSectionRoot()
				.getParent());
		defineSectionHandler.setLastSectionRoot((LocationAST)null);
	}

	public void removeSelectedDefineItems() {
		if (defineSectionHandler == null || defineElementsTableViewer == null) {
			return;
		}

		StructuredSelection selection = (StructuredSelection) defineElementsTableViewer.getSelection();
		// remove the selected elements
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			Definition item = (Definition) iterator.next();
			if (item != null) {
				ConfigTreeNodeUtilities.removeFromChain(defineSectionHandler.getLastSectionRoot(), item.getRoot());

				defineSectionHandler.getDefinitions().remove(item);
			}
		}
	}
}

/*
 * Open Source software for novelists and authors.
 * Original idea 2008 - 2012 Martin Mustun
 * Copyrigth (C) Favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package storybook.ui.chart;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.book.Book;
import storybook.tools.LOG;
import storybook.tools.file.FileFilter;
import storybook.tools.file.IOUtil;
import storybook.tools.swing.PrintUtil;
import storybook.tools.swing.ScreenImage;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractPanel;

public abstract class AbstractChartPanel extends AbstractPanel implements ActionListener {

	protected JPanel panel;
	protected JPanel optionsPanel;
	protected String chartTitle;
	protected boolean partRelated = false;
	protected boolean needsFullRefresh = false;
	private JScrollPane scroller;
	private AbstractAction exportAction;

	public AbstractChartPanel(MainFrame mainFrame, String param) {
		super(mainFrame);
		this.chartTitle = I18N.getMsg(param);
	}

	protected abstract void initChart();

	protected abstract void initChartUi();

	protected abstract void initOptionsUi();

	public String getTitle() {
		return chartTitle;
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		Object localObject = evt.getNewValue();
		String str = evt.getPropertyName();
		if (!(localObject instanceof View)) {
			return;
		}
		View localView1 = (View) localObject;
		View localView2 = (View) getParent().getParent();
		switch (Ctrl.getPROPS(evt.getPropertyName())) {
			case EXPORT:
				if (localView2 == localView1) {
					getExportAction().actionPerformed(null);
				}
				return;
			case REFRESH:
				if (localView2 == localView1) {
					refresh();
				}
				return;
			case PRINT:
				if (localView2 == localView1) {
					PrintUtil.printComponent(this);
				}
				return;
			default:
				break;
		}
		ActKey act = new ActKey(evt);
		if ((this.partRelated) && (Book.TYPE.PART.compare(act.type) && Ctrl.PROPS.CHANGE.check(str))) {
			if (this.needsFullRefresh) {
				refresh();
			} else {
				refreshChart();
			}
			return;
		}
	}

	@Override
	public void init() {
		try {
			initChart();
		} catch (Exception e) {
			LOG.err("AbstractChartPanel.init() Exception" + e.getLocalizedMessage());
		}
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("flowy,fill,ins 0", "", ""));
		this.panel = new JPanel(new MigLayout("flowy,fill,ins 2", "", "[][grow][]"));
		initChartUi();
		this.optionsPanel = new JPanel(new MigLayout("flowx,fill"));
		this.optionsPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
		initOptionsUi();
		this.scroller = new JScrollPane(this.panel);
		SwingUtil.setMaxPreferredSize(this.scroller);
		add(this.scroller, "grow");
		if (this.optionsPanel.getComponentCount() > 0) {
			add(this.optionsPanel, "growx");
		} else {
			add(new JLabel());
		}
	}

	protected void refreshChart() {
		this.panel.removeAll();
		initChartUi();
		this.panel.revalidate();
		this.panel.repaint();
	}

	private AbstractChartPanel getThis() {
		return this;
	}

	private static final String L_EXPORT = "export";
	private static final String L_PNG = ".png";

	private AbstractAction getExportAction() {
		if (this.exportAction == null) {
			this.exportAction = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						File f1 = new File(mainFrame.getBook().getParam().getParamExport().getDirectory());
						JFileChooser fc = new JFileChooser(f1);
						FileFilter filter = new FileFilter(FileFilter.TYPE.PNG);
						fc.setFileFilter(filter);
						fc.setApproveButtonText(I18N.getMsg(L_EXPORT));
						String str = AbstractChartPanel.this.mainFrame.getProject().getName()
						   + " - " + AbstractChartPanel.this.chartTitle;
						str = IOUtil.filenameCleanup(str);
						fc.setSelectedFile(new File(str));
						int i = fc.showDialog(AbstractChartPanel.this.getThis(), I18N.getMsg(L_EXPORT));
						if (i == 1) {
							return;
						}
						File f2 = fc.getSelectedFile();
						if (!f2.getName().endsWith(L_PNG)) {
							f2 = new File(f2.getPath() + L_PNG);
						}
						ScreenImage.createImage(AbstractChartPanel.this.panel, f2.toString());
						JOptionPane.showMessageDialog(AbstractChartPanel.this.getThis(),
						   I18N.getMsg(L_EXPORT + ".success"),
						   I18N.getMsg(L_EXPORT), 1);
					} catch (HeadlessException | IOException localException) {
					}
				}
			};
		}
		return this.exportAction;
	}

	public JPanel getPanelToExport() {
		return (null);
	}

}

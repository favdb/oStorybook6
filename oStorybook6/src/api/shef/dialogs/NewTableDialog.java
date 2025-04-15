/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import i18n.I18N;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import resources.icons.ICONS;
import resources.icons.IconUtil;

public class NewTableDialog extends OptionDialog {

	private LayoutPanel layoutPanel = new LayoutPanel();
	private TableAttributesPanel propsPanel;
	private static Icon icon = IconUtil.getIconSmall(ICONS.K.TABLE);

	public NewTableDialog(Frame parent) {
		super(parent, I18N.getMsg("shef.new_table"), I18N.getMsg("shef.new_table_desc"), icon);
		init();
	}

	public NewTableDialog(Dialog parent) {
		super(parent, I18N.getMsg("shef.new_table"), I18N.getMsg("shef.new_table_desc"), icon);
		init();
	}

	@SuppressWarnings("unchecked")
	private void init() {
		//default attribs
		Hashtable ht = new Hashtable();
		ht.put("border", "1");
		ht.put("width", "100%");
		propsPanel = new TableAttributesPanel();
		propsPanel.setAttributes(ht);
		propsPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder(I18N.getMsg("shef.properties")),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)
				));
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(layoutPanel, BorderLayout.NORTH);
		mainPanel.add(propsPanel, BorderLayout.CENTER);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setContentPane(mainPanel);
		pack();
		setResizable(false);
	}

	public String getHTML() {
		String html = "<table";
		Map attribs = propsPanel.getAttributes();
		for (Iterator e = attribs.keySet().iterator(); e.hasNext();) {
			String key = e.next().toString();
			String val = attribs.get(key).toString();
			html += ' ' + key + "=\"" + val + "\"";
		}
		html += ">\n";
		int numRows = layoutPanel.getRows();
		int numCols = layoutPanel.getColumns();
		for (int row = 1; row <= numRows; row++) {
			html += "<tr>\n";
			for (int col = 1; col <= numCols; col++) {
				html += "\t<td>\n</td>\n";
			}
			html += "</tr>\n";
		}
		return html + "</table>";
	}

	private class LayoutPanel extends JPanel {

		private JLabel rowsLabel = null, colsLabel = null;
		private int iRows, iCols;
		private JSpinner rowsField = null, colsField = null;

		/**
		 * This is the default constructor
		 */
		public LayoutPanel() {
			this(1, 1);
		}

		public LayoutPanel(int r, int c) {
			super();
			iRows = (r > 0) ? r : 1;
			iCols = (c > 0) ? c : 1;
			initialize();
		}

		public int getRows() {
			return Integer.parseInt(rowsField.getModel().getValue().toString());
		}

		public int getColumns() {
			return Integer.parseInt(colsField.getModel().getValue().toString());
		}

		/**
		 * This method initializes this
		 *
		 * @return void
		 */
		private void initialize() {
			setLayout(new GridBagLayout());
			setSize(330, 60);
			setPreferredSize(new Dimension(330, 60));
			//this.setMaximumSize(this.getPreferredSize());
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(
							null, I18N.getMsg("shef.layout"),
							TitledBorder.DEFAULT_JUSTIFICATION,
							TitledBorder.DEFAULT_POSITION, null, null),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));

			rowsLabel = new JLabel(I18N.getMsg("shef.rows"));
			add(rowsLabel, new GBC("0,0,anchor W, wy 0.0, ins 0 0 0 5"));
			add(getRowsField(), new GBC("0,1, fill N, wx 0.0, anchor W, ins 0 0 0 15"));
			colsLabel = new JLabel(I18N.getMsg("shef.columns"));
			add(colsLabel, new GBC("0,2,anchor W, ins 0 0 0 5"));
			add(getColsField(), new GBC("0,3,fill N, wx 1.0, anchor W"));
		}

		/**
		 * This method initializes rowsField
		 *
		 * @return javax.swing.JSpinner
		 */
		private JSpinner getRowsField() {
			if (rowsField == null) {
				rowsField = new JSpinner(new SpinnerNumberModel(iRows, 1, 999, 1));
			}
			return rowsField;
		}

		/**
		 * This method initializes colsField
		 *
		 * @return javax.swing.JSpinner
		 */
		private JSpinner getColsField() {
			if (colsField == null) {
				colsField = new JSpinner(new SpinnerNumberModel(iCols, 1, 999, 1));
			}
			return colsField;
		}
	}

}

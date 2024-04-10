/*
 * Copyright (C) 2022 favdb
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.scene;

import api.mig.swing.MigLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import storybook.App;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorIcon;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class IntensityPanel extends JPanel {

	private static final String TT = "IntensityPanel";

	public static final Color colors[] = {
		App.preferences.intensityGet(0),
		App.preferences.intensityGet(1),
		App.preferences.intensityGet(2),
		App.preferences.intensityGet(3),
		App.preferences.intensityGet(4)
	};
	private JComboBox cbList;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public IntensityPanel(int value) {
		super();
		initialize();
		setValue(value);
	}

	/**
	 * initialize the JPanel
	 */
	@SuppressWarnings("unchecked")
	private void initialize() {
		setLayout(new MigLayout(MIG.INS0));
		cbList = new JComboBox();
		cbList.setRenderer(new IntensityRenderer());
		for (int i = 0; i < colors.length; i++) {
			cbList.addItem(i);
		}
		add(cbList);
	}

	public int getValue() {
		return cbList.getSelectedIndex() + 1;
	}

	public void setValue(int value) {
		cbList.setSelectedIndex(value - 1);
	}

	public static Color getIntensityColor(int i) {
		//LOG.trace(TT + ".getIntensityColor(i=" + i + ")");
		return (i >= 1 && i <= 5 ? colors[i - 1] : colors[0]);
	}

	public Color getColor() {
		return colors[cbList.getSelectedIndex()];
	}

	public static Color getColorValue(int val) {
		return colors[val];
	}

	public String getHtmlValue() {
		Integer v = getValue();
		String html = v.toString() + " " + Html.getColorSpan(getColor(), 3);
		return html;
	}

	class IntensityRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean sel, boolean focus) {
			JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
			if (value instanceof Integer) {
				Color color = colors[(Integer) value];
				lb.setIcon(new ColorIcon(color, FontUtil.getWidth() * 2));
				lb.setToolTipText(ColorUtil.getPaletteString(color));
				lb.setText("" + (((Integer) value) + 1));
				lb.setHorizontalTextPosition(SwingConstants.LEFT);
			} else {
				if (value instanceof JLabel) {
					return (JComponent) value;
				}
			}
			return lb;
		}
	}

}

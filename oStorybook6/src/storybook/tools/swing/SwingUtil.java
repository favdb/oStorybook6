/*
 Storybook: Scene-based software for novelists and authors.
 Copyright (C) 2008 - 2012 Martin Mustun

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.tools.swing;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.db.abs.AbstractEntity;
import storybook.tools.LOG;
import storybook.tools.StringUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.js.JSToolBar;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.panels.AbstractPanel;

public class SwingUtil {

	private static final String TT = "SwingUtil";

	private SwingUtil() {
		// empty
	}

	/**
	 * set the enable given value for the given JPanel and all dependend components
	 *
	 * @param panel
	 * @param b
	 */
	public static void setEnable(JPanel panel, boolean b) {
		//LOG.trace(TT + ".setEnable(panel, b=" + LOG.trace(b) + ")");
		for (Component c : panel.getComponents()) {
			if (c instanceof JPanel) {
				setEnable((JPanel) c, b);
			} else if (c != null) {
				c.setEnabled(b);
			}
		}
		panel.setEnabled(b);
	}

	/**
	 * enable/disable a JToolNar
	 *
	 * @param panel
	 * @param b
	 */
	public static void setEnable(JSToolBar panel, boolean b) {
		//LOG.trace(TT + ".setEnable(panel, b=" + LOG.trace(b) + ")");
		panel.setEnabled(b);
	}

	/**
	 * set the default size of the given JComboBox
	 *
	 * @param cb
	 */
	public static void setCBsize(JComboBox cb) {
		int height = IconUtil.getDefSize() + (FontUtil.getHeight() / 4);
		cb.setMinimumSize(new Dimension(height, height));
	}

	/**
	 * set the default border for the given JComponent
	 *
	 * @param tf
	 */
	public static void setBoderDefault(JComponent tf) {
		JTextField tfi = new JTextField();
		tf.setBorder(tfi.getBorder());
	}

	/**
	 * set the dimensions for the given JComponent (mini, maxi, pref)
	 *
	 * @param comp
	 */
	public static void setSizes(JComponent comp) {
		comp.setMinimumSize(AbstractPanel.MINIMUM_SIZE);
		comp.setPreferredSize(AbstractPanel.HALF_SIZE);
		comp.setMaximumSize(AbstractPanel.MAXIMUM_SIZE);
	}

	/**
	 * get set default selected background color
	 *
	 * @return
	 */
	public static Color getSelectedBGColor() {
		return javax.swing.UIManager.getDefaults().getColor("List.selectionBackground");
	}

	/**
	 * get set default selected foreground color
	 *
	 * @return
	 */
	public static Color getSelectedFGColor() {
		return javax.swing.UIManager.getDefaults().getColor("List.selectionForeground");
	}

	/**
	 * set the selected colors for the given JComponent
	 *
	 * @param comp
	 */
	public static void setSelectedColor(JComponent comp) {
		comp.setForeground(getSelectedFGColor());
		comp.setBackground(getSelectedBGColor());
	}

	/**
	 * check if LanguageTool is OK for the given lang
	 *
	 * @param lang
	 * @return
	 */
	public static boolean isLanguageOK(String lang) {
		boolean rc = false;
		if (lang.contentEquals("none")) {
			return (true);
		}
		File f = new File("languagetool"
				+ File.separator + "rules" + File.separator
				+ lang.substring(0, lang.indexOf("_")));
		if (f.isDirectory()) {
			rc = true;
		}
		return (rc);
	}

	/**
	 * set the unit increment to 20 for the given JScrollPane
	 *
	 * @param scroller
	 */
	public static void setUnitIncrement(JScrollPane scroller) {
		scroller.getVerticalScrollBar().setUnitIncrement(20);
		scroller.getHorizontalScrollBar().setUnitIncrement(20);
	}

	/**
	 * showing the beta JDialog
	 *
	 * @param mainFrame
	 * @return
	 */
	public static int showBetaDialog(MainFrame mainFrame) {
		return JOptionPane.showConfirmDialog(mainFrame,
				I18N.getMsg("beta.confirm"),
				I18N.getMsg("question"), JOptionPane.YES_NO_OPTION);
	}

	public static String getBytesHR(long b) {
		int e = (int) (Math.log(b) / Math.log(1000));
		String[] sx = {"B", "kB", "MB", "GB", "TB", "PB", "EB"};
		return String.format("%.1f %s", b / Math.pow(1000, e), sx[e]);
	}

	// **************************
	// ** utilities for memory **
	// **************************
	public static void printMemoryUsage() {
		LOG.log(getMemoryUsageHr());
	}

	public static String getMemoryUsageSimpleHr() {
		return SwingUtil.getBytesHR(getMemoryUsed()) + " / " + SwingUtil.getBytesHR(getMemoryMax());
	}

	public static String getMemoryUsageHr() {
		return "Memory Usage (used/free/total/max): "
				+ SwingUtil.getBytesHR(getMemoryUsed()) + " / "
				+ SwingUtil.getBytesHR(getMemoryFree()) + " / "
				+ SwingUtil.getBytesHR(getMemoryTotal()) + " / "
				+ SwingUtil.getBytesHR(getMemoryMax());
	}

	public static long getMemoryUsed() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	public static long getMemoryFree() {
		return Runtime.getRuntime().freeMemory();
	}

	public static long getMemoryMax() {
		return Runtime.getRuntime().maxMemory();
	}

	public static long getMemoryTotal() {
		return Runtime.getRuntime().totalMemory();
	}
	// **** end of utilities for memory ****

	/**
	 * get a JButton with a name as text
	 *
	 * @param name
	 * @param action
	 * @return
	 */
	public static JButton getIconButton(String name, Action action) {
		JButton bt = new JButton(IconUtil.getIconSmall(ICONS.getIconKey(name)));
		bt.setToolTipText(I18N.getMsg(name));
		SwingUtil.setFixedSize(bt, IconUtil.getDefDim());
		if (action != null) {
			bt.addActionListener(action);
		}
		return bt;
	}

	/**
	 * get a JButton with an icon without text
	 *
	 * @param name
	 * @param action
	 * @return
	 */
	public static JButton getIconButton(String name, ActionListener action) {
		JButton bt = new JButton(IconUtil.getIconSmall(ICONS.getIconKey(name)));
		bt.setToolTipText(I18N.getMsg(name));
		SwingUtil.setFixedSize(bt, IconUtil.getDefDim());
		if (action != null) {
			bt.addActionListener(action);
		}
		return bt;
	}

	/**
	 * create a JButton
	 *
	 * @param text of the button
	 * @param icon
	 * @param tips text tooltip
	 * @param action the ActionListener, may be null
	 *
	 * @return
	 */
	public static JButton createButton(String text, ICONS.K icon, String tips,
			ActionListener... action) {
		return createButton(text, icon.toString(), tips, true, action);
	}

	/**
	 * create a JButton
	 *
	 * @param text of the button
	 * @param icon
	 * @param tips text tooltip
	 * @param nomargin margin to set
	 * @param action the ActionListener, may be null
	 *
	 * @return
	 */
	public static JButton createButton(String text, ICONS.K icon, String tips,
			boolean nomargin, ActionListener... action) {
		return createButton(text, icon.toString(), tips, nomargin, action);
	}

	/**
	 * create a JButton
	 *
	 * @param text of the button
	 * @param icon
	 * @param tips the text tooltip
	 * @param nomargin set the margin
	 * @param action to call
	 *
	 * @return
	 */
	public static JButton createButton(String text, String icon, String tips,
			boolean nomargin, ActionListener... action) {
		JButton bt = new JButton();
		bt.setName((text.isEmpty() ? tips : text));
		if (!text.isEmpty()) {
			bt.setText(I18N.getMsg(text));
		}
		if (!icon.isEmpty() && !icon.equalsIgnoreCase("empty")) {
			bt.setIcon(IconUtil.getIconSmall(icon));
		}
		if (!tips.isEmpty()) {
			bt.setToolTipText(I18N.getMsg(tips));
		}
		bt.setFocusable(false);
		if (nomargin) {
			bt.setMargin(new Insets(0, 0, 0, 0));
		}
		if (action != null && action.length > 0) {
			bt.addActionListener(action[0]);
		}
		return (bt);
	}

	/**
	 * create a zoom JPanel
	 *
	 * @param name
	 * @param zoom
	 * @param zoomin
	 * @param zoomout
	 * @return
	 */
	public static JPanel createZoomPanel(String name, int zoom,
			ActionListener zoomin, ActionListener zoomout) {
		JPanel zoomPanel = new JPanel(new MigLayout());
		zoomPanel.setOpaque(false);
		zoomPanel.add(new JLabel(I18N.getColonMsg(name)));
		zoomPanel.add(SwingUtil.createButton("", ICONS.K.MINUS, "zoom.out", zoomin));
		zoomPanel.add(new JLabel("" + (zoom + 1)));
		zoomPanel.add(SwingUtil.createButton("", ICONS.K.PLUS, "zoom.in", zoomout));
		return zoomPanel;
	}

	/**
	 * create a HTML JTextComponent with the given Font
	 *
	 * @param font
	 * @return
	 */
	public static JTextComponent createTextComponent(Font font) {
		JTextComponent tc = new JEditorPane();
		JEditorPane ep = (JEditorPane) tc;
		HTMLEditorKit kit = new HTMLEditorKit();
		StyleSheet styleSheet = kit.getStyleSheet();
		String bodyRule = "body { font-family: " + font.getFamily() + "; "
				+ "font-size: " + font.getSize() + "pt; }"
				+ "em {background-color: yellow;font-style: normal;}";
		styleSheet.addRule(bodyRule);
		ep.setEditorKitForContentType(Html.TYPE, kit);
		ep.setContentType(Html.TYPE);
		return tc;
	}

	/**
	 * create a JTextArea with the given width, without any scroll
	 *
	 * @param str
	 * @param width
	 * @return
	 */
	public static JTextArea createTextArea(String str, int... width) {
		//LOG.trace(TT + ".initTextArea(str="+str+")");
		JTextArea ta = new JTextArea();
		ta.setText(str);
		if (width == null || width.length == 0) {
			ta.setColumns(32);
		} else {
			ta.setColumns(width[0]);
		}
		ta.setRows(5);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setCaretPosition(0);
		return (ta);
	}

	/**
	 * replace a component in the given Container by index
	 */
	public static void replaceComponent(Container cont, int index, Component comp) {
		cont.remove(index);
		cont.add(comp, index);
		cont.validate();
		cont.repaint();
	}

	public static void replaceComponent(Container cont, Component comp1, Component comp2) {
		replaceComponent(cont, comp1, comp2, "");
	}

	/**
	 * replace a component in a Container
	 *
	 * @param cont
	 * @param comp1
	 * @param comp2
	 * @param constraints
	 */
	public static void replaceComponent(Container cont, Component comp1, Component comp2, String constraints) {
		int index = -1;
		int i = 0;
		for (Component comp : cont.getComponents()) {
			if (comp.equals(comp1)) {
				index = i;
				break;
			}
			++i;
		}
		cont.remove(comp1);
		cont.add(comp2, constraints, index);
		cont.validate();
		cont.repaint();
	}

	/**
	 * forced revalidate for the given Component
	 *
	 * @param comp
	 */
	public static void forceRevalidate(Component comp) {
		comp.invalidate();
		comp.validate();
		comp.repaint();
	}

	/**
	 * expand a Rectangle
	 *
	 * @param rect
	 */
	public static void expandRectangle(Rectangle rect) {
		Point p = rect.getLocation();
		p.translate(-5, -5);
		rect.setLocation(p);
		rect.grow(10, 10);
	}

	/**
	 * get the background color for a JTable
	 *
	 * @return
	 */
	public static Color getTableBackgroundColor() {
		return getTableBackgroundColor(false);
	}

	/**
	 * get the background color for a JTable, if not colored option return standard color
	 *
	 * @param colored
	 * @return
	 */
	public static Color getTableBackgroundColor(boolean colored) {
		if (colored) {
			return new Color(0xf4f4f4);
		}
		return UIManager.getColor("Table.background");
	}

	/**
	 * set the Dimension for the given component to Short.MAX_VALUE
	 *
	 * @param comp
	 */
	public static void setMaxPreferredSize(JComponent comp) {
		comp.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
	}

	/**
	 * set the Dimension for the given component to the given Dimension
	 *
	 * @param comp
	 */
	public static void setMaxPreferredSize(JComponent comp, Dimension dim) {
		comp.setMaximumSize(dim);
		comp.setPreferredSize(dim);
	}

	/**
	 * set a fixed Dimension for the given component
	 *
	 * @param comp
	 * @param dim
	 */
	public static void setFixedSize(JComponent comp, Dimension dim) {
		//LOG.trace("setFixedSize(comp=" + comp.toString() + ", dim=" + dim.toString() + ")");
		comp.setMinimumSize(dim);
		comp.setMaximumSize(dim);
		comp.setPreferredSize(dim);
	}

	/**
	 * set the maximum width to the given value, the height will be Short.MAX_VALUE
	 *
	 * @param comp
	 * @param v
	 */
	public static void setMaxWidth(JComponent comp, int v) {
		comp.setMaximumSize(new Dimension(v, Short.MAX_VALUE));
	}

	/**
	 * set the maximum heigth to the given value, the width will be Short.MAX_VALUE
	 *
	 * @param comp
	 * @param v
	 */
	public static void setMaxHeight(JComponent comp, int v) {
		comp.setMaximumSize(new Dimension(Short.MAX_VALUE, v));
	}

	public static void printUIDefaults() {
		UIDefaults uiDefaults = UIManager.getDefaults();
		Enumeration<Object> e = uiDefaults.keys();
		while (e.hasMoreElements()) {
			Object key = e.nextElement();
			Object val = uiDefaults.get(key);
			LOG.log("[" + key.toString() + "]:[" + (null != val ? val.toString() : "(null)") + "]");
		}
	}

	public static void addCtrlEnterAction(JComponent comp, AbstractAction action) {
		InputMap inputMap = comp.getInputMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), action);
	}

	/**
	 * Creates a JSlider and ensure the given value is between min and max.
	 *
	 * @param orientation the orientation of the slider
	 * @param min the minimum value of the slider
	 * @param max the maximum value of the slider
	 * @param value the initial value of the slider
	 * @return the JSlider
	 */
	public static JSlider createSafeSlider(int orientation, int min, int max, int value) {
		if (value < min) {
			value = min;
		} else if (value > max) {
			value = max;
		}
		return new JSlider(orientation, min, max, value);
	}

	/**
	 * Gets the dimension of the screen.
	 *
	 * @return the dimension of the screen
	 */
	public static Dimension getScreenSize() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		return tk.getScreenSize();
	}

	/**
	 * Enables or disables all children of the given container.
	 *
	 * @param container the container
	 * @param enable if true, the components are enabled, otherwise they are disabled
	 */
	public static void enableContainerChildren(Container container, boolean enable) {
		if (container == null) {
			return;
		}
		for (Component comp : container.getComponents()) {
			try {
				comp.setEnabled(enable);
				((JComponent) comp).setOpaque(enable);
				if (comp instanceof Container) {
					enableContainerChildren((Container) comp, enable);
				}
			} catch (ClassCastException e) {
				// ignore component
			}
		}
	}

	public static Color getBackgroundColor() {
		return Color.white;
	}

	public static String getDayName(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		return sdf.format(date);
	}

	public static String getTimestamp(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return sdf.format(date);
	}

	public static void addEnterAction(JComponent comp, Action action) {
		comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
		comp.getActionMap().put("ENTER", action);
	}

	public static void addEscAction(JComponent comp, Action action) {
		comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), action);
		comp.getActionMap().put(action, action);
	}

	public static Border getBorderDefault() {
		return getBorderDefault(1);
	}

	public static Border getBorderDefault(int thickness) {
		return BorderFactory.createLineBorder(Color.black, thickness);
	}

	public static Color getSelectBackground() {
		return UIManager.getColor("List.selectionBackground");
	}

	public static void setUIFont(FontUIResource uiFont) {
		UIManager.put("defaultFont", uiFont);
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, uiFont);
			}
		}
		UIManager.put("Button.font", uiFont);
		UIManager.put("CheckBox.font", uiFont);
		UIManager.put("CheckBoxMenuItem.acceleratorFont", uiFont);
		UIManager.put("CheckBoxMenuItem.font", uiFont);
		UIManager.put("ColorChooser.font", uiFont);
		UIManager.put("ComboBox.font", uiFont);
		UIManager.put("defaultFont", uiFont);
		UIManager.put("default", uiFont);
		UIManager.put("EditorPane.font", uiFont);
		UIManager.put("FormattedTextField.font", uiFont);
		UIManager.put("IconButton.font", uiFont);
		UIManager.put("InternalFrame.optionDialogTitleFont", uiFont);
		UIManager.put("InternalFrame.paletteTitleFont", uiFont);
		UIManager.put("InternalFrame.titleFont", uiFont);
		UIManager.put("Label.font", uiFont);
		UIManager.put("List.font", uiFont);
		UIManager.put("Menu.acceleratorFont", uiFont);
		UIManager.put("MenuBar.font", uiFont);
		UIManager.put("Menu.font", uiFont);
		UIManager.put("MenuItem.acceleratorFont", uiFont);
		UIManager.put("MenuItem.font", uiFont);
		UIManager.put("OptionPane.buttonFont", uiFont);
		UIManager.put("OptionPane.font", uiFont);
		UIManager.put("OptionPane.messageFont", uiFont);
		UIManager.put("Panel.font", uiFont);
		UIManager.put("PasswordField.font", uiFont);
		UIManager.put("PopupMenu.font", uiFont);
		UIManager.put("ProgressBar.font", uiFont);
		UIManager.put("RadioButton.font", uiFont);
		UIManager.put("RadioButtonMenuItem.acceleratorFont", uiFont);
		UIManager.put("RadioButtonMenuItem.font", uiFont);
		UIManager.put("ScrollPane.font", uiFont);
		UIManager.put("Slider.font", uiFont);
		UIManager.put("Spinner.font", uiFont);
		UIManager.put("TabbedPane.font", uiFont);
		UIManager.put("TabbedPane.smallFont", uiFont);
		UIManager.put("Table.font", uiFont);
		UIManager.put("TableHeader.font", uiFont);
		UIManager.put("TextArea.font", uiFont);
		UIManager.put("TextField.font", uiFont);
		UIManager.put("TextPane.font", uiFont);
		UIManager.put("TitledBorder.font", uiFont);
		UIManager.put("ToggleButton.font", uiFont);
		UIManager.put("ToolBar.font", uiFont);
		UIManager.put("ToolTip.font", uiFont);
		UIManager.put("Tree.font", uiFont);
		UIManager.put("Viewport.font", uiFont);
	}

	public static void setWaitingCursor(Component comp) {
		comp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	}

	public static void setDefaultCursor(Component comp) {
		comp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public static List<Component> findComponentsByClass(Container container, Class<? extends Component> cname,
			List<Component> res) {
		if (container instanceof Container) {
			Component[] comps = ((Container) container).getComponents();
			for (Component comp : comps) {
				if (cname.isInstance(comp)) {
					res.add(comp);
				}
				if (comp instanceof Container) {
					findComponentsByClass((Container) comp, cname, res);
				}
			}
		}
		return res;
	}

	public static List<Component> findComponentsNameStartsWith(Container container,
			String starts, List<Component> res) {
		if (container instanceof Container) {
			Component[] components = container.getComponents();
			for (Component comp : components) {
				String name = comp.getName();
				if (name != null && name.startsWith(starts)) {
					res.add(comp);
				}
				if (comp instanceof Container) {
					findComponentsNameStartsWith((Container) comp, starts, res);
				}
			}
		}
		return res;
	}

	public static Component findComponentByName(Component component, String name) {
		if (component.getName() != null || component.getName().equals(name)) {
			return component;
		}
		if (component instanceof Container) {
			Component[] components = ((Container) component).getComponents();
			for (int i = 0; i < components.length; ++i) {
				Component comp = findComponentByName(components[i], name);
				if (comp != null) {
					return comp;
				}
			}
		}
		return null;
	}

	public static Component printComponentHierarchy(Component rootComponent) {
		return printComponentHierarchy(rootComponent, -1);
	}

	private static Component printComponentHierarchy(Component rootComponent, int level) {
		LOG.log(StringUtil.repeat("    ", level + 1) + (level + 1) + ":"
				+ formateComponentInfosToPrint(rootComponent)
		);
		if (rootComponent instanceof Container) {
			Component[] components = ((Container) rootComponent).getComponents();
			for (int i = 0; i < components.length; ++i) {
				Component comp = printComponentHierarchy(components[i], level);
				if (comp != null) {
					return comp;
				}
			}
		}
		return null;
	}

	public static String formateComponentInfosToPrint(Component comp) {
		StringBuilder buf = new StringBuilder();
		buf.append(comp.getClass().getSimpleName());
		buf.append(" [");
		buf.append(comp.getName());
		if (comp instanceof JLabel) {
			buf.append(",\"").append(((JLabel) comp).getText()).append("\"");
		}
		buf.append(",");
		buf.append(comp.getClass().getName());
		buf.append(",");
		buf.append(comp.isVisible() ? "visible" : "not visible");
		buf.append(",");
		buf.append(comp.isValid() ? "valid" : "invalid");
		buf.append("]");
		return buf.toString();
	}

	public static int getComponentIndex(Container container, Component component) {
		int index = 0;
		for (Component comp : container.getComponents()) {
			if (comp == component) {
				return index;
			}
			++index;
		}
		return -1;
	}

	public static Frame getFrame(String frameName) {
		Frame[] frames = Frame.getFrames();
		for (Frame frame : frames) {
			if (frame.getName().equals(frameName)) {
				return frame;
			}
		}
		return null;
	}

	public static JFrame findParentJFrame(JComponent c) {
		if (c == null) {
			return null;
		}
		Component parent = c.getParent();
		while (!(parent instanceof JFrame) && (parent != null)) {
			parent = parent.getParent();
		}
		return (JFrame) parent;
	}

	public static JTabbedPane findParentJTabbedPane(JComponent c) {
		if (c == null) {
			return null;
		}
		Component parent = c.getParent();
		while (!(parent instanceof JTabbedPane) && (parent != null)) {
			parent = parent.getParent();
		}
		return (JTabbedPane) parent;
	}

	public static Component findParent(Component c) {
		if (c == null) {
			return null;
		}
		Component parent = c.getParent();
		Component comp = null;
		while (parent != null) {
			comp = parent;
			parent = comp.getParent();
		}
		return comp;
	}

	public static void showFrame(JFrame frame, JFrame parent) {
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setLocation(parent.getX()
				+ (parent.getWidth() - frame.getWidth()) / 2, parent.getY()
				+ (parent.getHeight() - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	public static void showDialog(JDialog dlg, Component parent) {
		showDialog(dlg, parent, true);
	}

	public static void showDialog(JDialog dlg, Component parent, boolean resizable) {
		dlg.setResizable(resizable);
		dlg.pack();
		dlg.setLocationRelativeTo(parent);
		dlg.setVisible(true);
	}

	public static void showModalDialog(JDialog dlg, Component parent) {
		showModalDialog(dlg, parent, true);
	}

	public static void showModalDialog(JDialog dlg, Component parent, boolean resizable) {
		dlg.setResizable(resizable);
		dlg.setModal(true);
		dlg.pack();
		dlg.setLocationRelativeTo(parent);
		dlg.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	public static JComboBox createDateFormat() {
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement("dd-MM-yyyy");
		model.addElement("MM-dd-yyyy");
		model.addElement("dd/MM/yyyy");
		return new JComboBox(model);
	}

	public static void showError(String msg) {
		JOptionPane.showMessageDialog(null,
				I18N.getMsg(msg),
				I18N.getMsg("warning"),
				JOptionPane.ERROR_MESSAGE);

	}

	private static final String L_EDITOR = "Editor_";
	private static final String L_MODEL = "storybook.model.hbn.entity.";

	private static void saveDlgPosition(AbstractEntity e, int X, int Y, int H, int W) {
		//LOG.trace("SwingUtil.saveDlgPosition(e="+e.getObjType()+", X="+X+", Y="+Y+", W="+W);
		String key = L_EDITOR + e.getClass().getName().replace(L_MODEL, "");
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		String pos = X + "," + Y + "," + H + "," + W + "," + gs.length;
		Pref pref = App.preferences;
		if (!pos.equals(pref.getString(key, ""))) {
			pref.setString(key, pos);
		}
	}

	public static void saveDlgPosition(JDialog dlg, AbstractEntity e) {
		int formX = dlg.getX();
		int formY = dlg.getY();
		int formH = dlg.getHeight();
		int formW = dlg.getWidth();
		saveDlgPosition(e, formX, formY, formH, formW);
	}

	public static void saveDlgPosition(JFrame dlg, AbstractEntity e) {
		int formX = dlg.getX();
		int formY = dlg.getY();
		int formH = dlg.getHeight();
		int formW = dlg.getWidth();
		saveDlgPosition(e, formX, formY, formH, formW);
	}

	public static Dimension getDlgPosition(AbstractEntity e) {
		Dimension d = new Dimension(0, 0);
		Pref pref = App.preferences;
		String keyName = L_EDITOR + e.getClass().getName().replace(L_MODEL, "");
		String x = pref.getString(keyName, "");
		if (x.isEmpty()) {
			return (d);
		}
		String z[] = x.split(",");
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		if (z.length > 4 && gs.length != Integer.parseInt(z[4])) {
			return (new Dimension(0, 0));
		}
		d = new Dimension();
		d.height = Integer.parseInt(z[0]);
		d.width = Integer.parseInt(z[1]);
		return d;
	}

	public static void resetDlgPosition() {
		LOG.trace("SwingUtil.resetDlgPosition()");
		String keys[] = {"Attribute", "Category", "Chapter", "Gender", "Idea", "Item", "Itemlink",
			"Location", "Memo", "Part", "Person", "Plot", "Relation", "Scene", "Status", "Strand",
			"Tag", "Taglink", "Event"};
		Pref pref = App.preferences;
		for (String k : keys) {
			String keyName = L_EDITOR + k;
			pref.removeString(keyName);
		}
	}

	public static Dimension getDlgSize(AbstractEntity e) {
		Dimension d = new Dimension(1024, 800);
		Pref pref = App.preferences;
		String keyName = L_EDITOR + e.getClass().getName().replace(L_MODEL, "");
		String x = pref.getString(keyName, "");
		if (!x.isEmpty()) {
			String z[] = x.split(",");
			if (z.length > 3) {
				d = new Dimension();
				d.height = Integer.parseInt(z[3]);
				d.width = Integer.parseInt(z[2]);
			}
		}
		return d;
	}

	public static void setFullScreen(JFrame frame) {
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice vc = e.getDefaultScreenDevice();
		vc.setFullScreenWindow(frame);
	}

	public static String setFontDefault(MainFrame mainFrame) {
		Font f = FontUtil.getFont(App.preferences.getString(Pref.KEY.FONT_DEFAULT, Const.FONT_DEFAULT));
		return Html.getStyle(Html.FONT_FAMILY + ": " + f.getFamily() + "; "
				+ Html.FONT_SIZE + ": " + f.getSize() + "pt;");
	}

	/**
	 * set the aspect for a gigen JTextField
	 *
	 * @param tf
	 * @param aspect : may start with B for bold, I for italic, space for normal
	 */
	public static void setAspect(JTextField tf, String aspect) {
		JTextField tx = new JTextField();
		tf.setBackground(tx.getBackground());
		tf.setForeground(tx.getForeground());
		tf.setFont(tx.getFont());
		if (aspect != null && !aspect.isEmpty()) {
			if (aspect.startsWith("B")) {
				tf.setFont(FontUtil.getBold(tf.getFont()));
			} else if (aspect.startsWith("I")) {
				tf.setFont(FontUtil.getItalic(tf.getFont()));
			}
			if (aspect.length() > 1) {
				Color color = ColorUtil.fromHexString(aspect.substring(1));
				tf.setForeground(color);
			}
		}
	}

	/**
	 * getting the font char width
	 *
	 * @param font
	 * @return
	 */
	public static int getCharWidth(Font font) {
		JPanel lb = new JPanel();
		FontMetrics fm = lb.getFontMetrics(font);
		return (fm.charWidth('W') + fm.charWidth('I') + fm.charWidth('E')) / 3;
	}

	/**
	 * getting the font char height
	 *
	 * @param font
	 * @return
	 */
	public static int getCharHeight(Font font) {
		JPanel lb = new JPanel();
		FontMetrics fm = lb.getFontMetrics(font);
		return fm.getHeight();
	}

	/**
	 * A utility method for drawing rotated text from org.jfree.jcommon.text.TextUtilities
	 * <P>
	 * A common rotation is -Math.PI/2 which draws text 'vertically' (with the top of the characters
	 * on the left).
	 *
	 * @param text the text.
	 * @param graphics the graphics device.
	 * @param textX the x-coordinate for the text (before rotation).
	 * @param textY the y-coordinate for the text (before rotation).
	 * @param color
	 */
	public static void drawRotatedString(String text,
			Graphics2D graphics,
			Float textX,
			Float textY,
			Color color) {
		if ((text == null) || (text.equals(""))) {
			return;
		}
		double angle = -Math.PI / 2;
		AffineTransform saved = graphics.getTransform();
		AffineTransform rotate = AffineTransform.getRotateInstance(angle, textX, textY);
		graphics.transform(rotate);
		Color oc = graphics.getColor();
		if (ColorUtil.isDark(color)) {
			Color c = (ColorUtil.isDark(color) ? Color.WHITE : Color.BLACK);
			graphics.setColor(c);
			FontMetrics fm = graphics.getFontMetrics();
			Rectangle2D rect = fm.getStringBounds(text, graphics);
			graphics.fillRect(textX.intValue(),
					textY.intValue() - fm.getAscent(),
					(int) rect.getWidth(),
					(int) rect.getHeight());
			graphics.setColor(oc);
		}
		graphics.drawString(text, textX, textY);
		graphics.setColor(oc);
		graphics.setTransform(saved);
	}

	/**
	 * draw a vertical object
	 *
	 * @param graphics
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param color
	 * @param v
	 */
	public static void drawVertical(Graphics graphics,
			int x, int y,
			int width,
			int height,
			Color color,
			long v) {
		if (color == null) {
			color = Color.GRAY;
		}
		GradientPaint gp = new GradientPaint(0, 0, ColorUtil.lighter(color, 0.5D), width / 4, 0, color, true);
		Graphics2D g2d = (Graphics2D) graphics;
		Paint op = g2d.getPaint();
		g2d.setPaint(gp);
		g2d.fillRect(x, y, width, height);
		g2d.setPaint(op);
		Color oc = graphics.getColor();
		graphics.setColor(color);
		graphics.drawRect(x, y, width, height);
		graphics.drawRect(x + 1, y + 1, width - 2, height - 2);
		graphics.setColor(oc);
		Font nf = new Font("Sans", Font.BOLD, 10);
		Font of = graphics.getFont();
		graphics.setFont(nf);
		String vx = Long.toString(v);
		Rectangle2D r = graphics.getFont().getStringBounds(vx, graphics.getFontMetrics().getFontRenderContext());
		int x1 = (int) (x + width - r.getWidth()) - 1;
		int y1 = (int) (y + (r.getHeight()));
		graphics.setColor(ColorUtil.getFontColor(color));
		graphics.drawString(vx, x1, y1);
		graphics.setFont(of);
		graphics.setColor(oc);
	}

	public static void setErrorForeground(JComponent comp, boolean b) {
		JTextField tf = new JTextField();
		comp.setForeground((b ? Color.red : tf.getForeground()));
	}

	public static void setBackground(JComponent panel, Color color) {
		panel.setOpaque(true);
		panel.setBackground(color);
		if (ColorUtil.isDark(color)) {
			panel.setForeground(Color.WHITE);
		}
	}

}

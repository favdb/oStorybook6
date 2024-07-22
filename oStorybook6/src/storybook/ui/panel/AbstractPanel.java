package storybook.ui.panel;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.text.JTextComponent;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Pref;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.ctrl.Ctrl.PROPS;
import storybook.db.book.Book;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.tools.Markdown;
import storybook.tools.html.Html;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSToolBar;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.Ui;
import storybook.ui.interfaces.IPaintable;
import storybook.ui.interfaces.IRefreshable;

@SuppressWarnings("serial")
public abstract class AbstractPanel extends JPanel
	implements ActionListener, IRefreshable, IPaintable {

	private static final String TT = "AbstractPanel.",
		CB_PART_FILTER = "cbPartFilter";

	public static Dimension MINIMUM_SIZE = new Dimension(400, 120),
		MAXIMUM_SIZE = new Dimension(1024, 800),
		PREF_SIZE = new Dimension(1024, 800),
		HALF_SIZE = new Dimension(640, 400);
	public static boolean ADD = true, ALL = true,
		EMPTY = true,
		MANDATORY = true,
		BORDER = true,
		CENTER = true,
		FULL = true,
		GROW = true,
		HIDE = true,
		INFO = true,
		NEW = true,
		NEWTAB = true,
		TIME = true,
		TOP = true,
		WRAP = true;
	public MainFrame mainFrame;
	public Ctrl ctrl;
	public Book book;
	public JToolBar toolbar;
	public boolean withPart;
	public JComboBox cbPartFilter;

	public AbstractPanel() {
		setName(this.getClass().getSimpleName());
	}

	public AbstractPanel(MainFrame m) {
		this.mainFrame = m;
		if (m.project != null) {
			this.book = m.project.book;
		}
		this.ctrl = mainFrame.getBookController();
		setName(this.getClass().getSimpleName());
	}

	@Override
	public abstract void init();

	@Override
	public abstract void initUi();

	public abstract void modelPropertyChange(PropertyChangeEvent evt);

	public void initAll() {
		init();
		initUi();
	}

	@Override
	public void refresh() {
		//LOG.printInfos(TT+"refresh() panel="+this.getClass().getSimpleName());
		removeAll();
		initAll();
		invalidate();
		validate();
		repaint();
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public void setWithPart(boolean withPart) {
		this.withPart = withPart;
	}

	public JToolBar initToolbar() {
		//this.withPart = withPart;
		//LOG.printInfos(TT + ".initToolbar() withPart=" + (withPart ? "true" : "false"));
		toolbar = new JSToolBar(false);
		toolbar.setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3)));
		toolbar.setFloatable(false);
		if (this.withPart) {
			@SuppressWarnings("unchecked")
			List<Part> parts = (List) mainFrame.project.getList(Book.TYPE.PART);
			if (parts != null && parts.size() > 1) {
				toolbar.add(new JLabel(I18N.getColonMsg("part")));
				cbPartFilter = Ui.initComboBox(CB_PART_FILTER, "", parts, null, !EMPTY, ALL, this);
				toolbar.add(cbPartFilter);
			}
		}
		if (App.preferences.getBoolean(Pref.KEY.LAF_COLORED)) {
			toolbar.setBackground(Color.decode(App.preferences.getString(Pref.KEY.LAF_COLOR)));
		}
		return toolbar;
	}

	public Part getCbPart() {
		if (cbPartFilter == null || cbPartFilter.getSelectedIndex() < 1) {
			return (Part) null;
		}
		return (Part) cbPartFilter.getSelectedItem();
	}

	public JLabel initLabel(String msg, boolean bold) {
		JLabel lb = new JLabel(I18N.getColonMsg(msg));
		lb.setName(msg);
		if (bold) {
			lb.setFont(FontUtil.getBold());
		}
		return (lb);
	}

	public static JButton initButton(String name, String text, ICONS.K icon, String tooltip, ActionListener... al) {
		//LOG.printInfos(TT+"initButton(name=" + name + ", text=" + text + ", icon=" + icon.toString() + ")");
		JButton btn = new JButton();
		btn.setName(name);
		if (text != null && !text.isEmpty()) {
			btn.setText(I18N.getMsg(text));
		}
		if (icon != ICONS.K.NONE) {
			btn.setIcon(IconUtil.getIconSmall(icon));
			if (text != null && text.isEmpty()) {
				btn.setMargin(new Insets(0, 0, 0, 0));
			}

		}
		if (!tooltip.isEmpty()) {
			btn.setToolTipText(I18N.getMsg(tooltip));
		}
		if (al != null && al.length > 0) {
			btn.addActionListener(al[0]);
		}
		return (btn);
	}

	public JTextComponent initHtmlField(String fieldName, Scene scene) {
		JTextArea ta = new JTextArea();
		JTextComponent tcText = SwingUtil.createTextComponent(App.fonts.editorGet());
		tcText.setFont(App.fonts.editorGet());
		if (book.getMarkdown()) {
			Markdown mark = new Markdown("BookTextPanel", Html.TYPE, "");
			mark.setHeader(scene, book.info.scenarioGet());
			mark.setText(scene.getSummary());
			mark.setEditorFont(App.fonts.monoGet());
			tcText = mark.html;
		} else {
			String strRegEx = "<[/]?img[^>]*>";
			tcText.setText(scene.getSummary().replaceAll(strRegEx, "[" + I18N.getMsg("image") + "]"));
		}
		tcText.setName(fieldName);
		tcText.setDragEnabled(true);
		//tcText.addFocusListener(this);
		tcText.setCaretPosition(0);
		tcText.setEditable(false);
		tcText.setBackground(ta.getBackground());
		tcText.setForeground(ta.getForeground());
		return tcText;
	}

	public boolean isChange(ActKey act) {
		return (Ctrl.PROPS.CHANGE.check(act.getCmd()));
	}

	public boolean isEdit(ActKey act) {
		return (Ctrl.PROPS.EDIT.check(act.getCmd()));
	}

	public boolean isDelete(ActKey act) {
		return (Ctrl.PROPS.DELETE.check(act.getCmd()));
	}

	public boolean isInit(ActKey act) {
		return (Ctrl.PROPS.INIT.check(act.getCmd()));
	}

	public boolean isNew(ActKey act) {
		return (Ctrl.PROPS.NEW.check(act.getCmd()));
	}

	public boolean isUpdate(ActKey act) {
		return (Ctrl.PROPS.UPDATE.check(act.getCmd()));
	}

	/**
	 * set the value between min and max
	 *
	 * @param val
	 * @param min
	 * @param max
	 * @return
	 */
	public static int setMinMax(int val, int min, int max) {
		int r = (val < min ? min : val);
		return (r > max ? max : r);
	}

	/**
	 * check if it is the view refresh command
	 *
	 * @param evt the property event
	 * @param xview the SbView
	 * @return
	 */
	public boolean isRefresh(PropertyChangeEvent evt, SbView.VIEWNAME xview) {
		return (Ctrl.getPROPS(evt.getPropertyName()).equals(PROPS.REFRESH)
			&& (evt.getNewValue() instanceof SbView && ((SbView) evt.getNewValue()).equals(xview)));
	}

}

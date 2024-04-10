package storybook.ui.panel.reading;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.book.Book;
import storybook.db.strand.Strand;
import storybook.tools.LOG;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractPanel;

@SuppressWarnings("serial")
public class StrandPanel extends AbstractPanel implements ItemListener {

	private final ReadingPanel bookReading;
	private List<Long> strandIds;
	private List<JCheckBox> cbList;

	public StrandPanel(MainFrame mainFrame, ReadingPanel booReading) {
		this.mainFrame = mainFrame;
		this.bookReading = booReading;
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		if (Book.TYPE.STRAND.compare(act.type) && Ctrl.PROPS.UPDATE.check(act.getCmd())) {
			refresh();
		}
	}

	@Override
	public void init() {
		strandIds = new ArrayList<>();
		cbList = new ArrayList<>();
		addAllStrands();
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("wrap"));
		setBackground(Color.white);
		setBorder(SwingUtil.getBorderDefault());
		@SuppressWarnings("unchecked")
		List<Strand> list = mainFrame.project.strands.getList();
		for (Strand strand : list) {
			JCheckBox cb = new JCheckBox(strand.toString());
			cbList.add(cb);
			long id = strand.getId();
			if (strandIds.contains(id)) {
				cb.setSelected(true);
			}
			cb.setName(Long.toString(id));
			cb.setOpaque(false);
			cb.addItemListener(this);
			add(new JSLabel(strand.getColorIcon()), "split 2");
			add(cb);
		}
		JButton btAll = new JButton(getSelectAllAction());
		btAll.setText(I18N.getMsg("tree.show.all"));
		btAll.setName("all");
		btAll.setOpaque(false);
		add(btAll, "sg,gapy 20");
		JButton cbNone = new JButton(getSelectNoneAction());
		cbNone.setText(I18N.getMsg("tree.show.none"));
		cbNone.setName("none");
		cbNone.setOpaque(false);
		add(cbNone, "sg");
	}

	private AbstractAction getSelectAllAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addAllStrands();
				for (JCheckBox cb : cbList) {
					cb.setSelected(true);
				}
				bookReading.refresh();
			}
		};
	}

	private AbstractAction getSelectNoneAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				strandIds.clear();
				for (JCheckBox cb : cbList) {
					cb.setSelected(false);
				}
				bookReading.refresh();
			}
		};
	}

	private void addAllStrands() {
		@SuppressWarnings("unchecked")
		List<Strand> list = mainFrame.project.strands.getList();
		for (Strand strand : list) {
			strandIds.add(strand.getId());
		}
	}

	@Override
	public void itemStateChanged(ItemEvent evt) {
		try {
			JCheckBox cb = (JCheckBox) evt.getSource();
			Long id = Long.valueOf(cb.getName());
			if (cb.isSelected()) {
				strandIds.add(id);
			} else {
				strandIds.remove(id);
			}
			bookReading.refresh();
		} catch (NumberFormatException e) {
			LOG.err("StrandPanel.itemStateChanged error", e);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}

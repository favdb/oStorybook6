/*
 * Copyright (C) 2024 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.db.challenge;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import resources.icons.ICONS;
import storybook.db.book.BookUtil;
import static storybook.db.challenge.Challenge.NBVALUES;
import storybook.dialog.MessageDlg;
import storybook.tools.DateUtil;
import storybook.tools.StringUtil;
import storybook.tools.swing.CircleProgressBar;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;
import storybook.ui.chart.occurences.DatasetItem;
import storybook.ui.panel.AbstractPanel;

/**
 * JPanel for the Challenge, inspired by Philippe <br>
 * in https://www.jeunesecrivains.com/t63749-outils-logiciels-tableau-de-bord-d-avancement-nanowrimo
 *
 * @author favdb
 */
public class ChallengePanel extends AbstractPanel {

	private static final String TT = "ChallengePanel.";

	private static final Integer COL_LEN = 10;
	private final Challenge challenge;
	private JPanel objectivePanel, valuesPanel, barPanel, diagram;
	private JButton doChallenge;
	private JTextField dateStart, daysNb,
		wordsNb, wordsByDay, wordsMin, wordsMax, wordsMean,
		today, leftDays, endday,
		wordsWrited, wordsTowrite, leftWords;
	private CircleProgressBar progress;
	private final ChallengeDlg challengeDlg;
	private BarChart barChart;
	private List<DatasetItem> dataset;
	private JPanel pWordsByDay;

	public ChallengePanel(ChallengeDlg dlg) {
		super(dlg.getMainFrame());
		this.challengeDlg = dlg;
		this.challenge = mainFrame.getProject().challenge;
		initAll();
	}

	/**
	 * initialize all JTextField datas
	 */
	@Override
	public void init() {
		dateStart = new JTextField();
		wordsNb = new JTextField();
		wordsByDay = new JTextField();
		wordsMin = new JTextField();
		wordsMax = new JTextField();
		wordsMean = new JTextField();
		today = new JTextField();
		leftDays = new JTextField();
		wordsWrited = new JTextField();
		wordsTowrite = new JTextField();
		endday = new JTextField();
		daysNb = new JTextField();
		leftWords = new JTextField();
	}

	/**
	 * initialize the user interface
	 */
	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE3, MIG.WRAP), "[][65%]"));
		tableInit();
		add(barPanel, MIG.GROW);
		JPanel right = new JPanel(new MigLayout(MIG.HIDEMODE3));
		objectiveInit();
		right.add(objectivePanel, MIG.get(MIG.SPAN, MIG.GROWX));
		valuesInit();
		right.add(valuesPanel, MIG.get(MIG.SPAN, MIG.CENTER));
		add(right, MIG.TOP);
		dataSet();
	}

	/**
	 * initialize the objective panel
	 */
	private void objectiveInit() {
		objectivePanel = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE3)));
		objectivePanel.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("objective")));
		daysNb = initTextField(objectivePanel, "challenge.days", 4, false);
		wordsNb = initTextField(objectivePanel, "challenge.words", 8, true);
		pWordsByDay = new JPanel();
		wordsByDay = initTextField(pWordsByDay, "challenge.words_by_day", 6, false);
		objectivePanel.add(pWordsByDay, MIG.SPANX2);
		doChallenge = Ui.initButton("btStart", "challenge.begin", ICONS.K.EMPTY, "", e -> beginChallenge());
		objectivePanel.add(doChallenge, MIG.get(MIG.RIGHT, MIG.SPAN));
	}

	/**
	 * initialize a standard JTextField
	 *
	 * @param p
	 * @param name
	 * @param len
	 * @param towrap
	 *
	 * @return a JTextField
	 */
	private JTextField initTextField(JPanel p, String name, int len, boolean towrap) {
		p.add(new JLabel(I18N.getColonMsg(name)), MIG.RIGHT);
		JTextField tf = new JTextField();
		tf.setName(name);
		tf.setColumns(len);
		tf.setEditable(false);
		tf.setHorizontalAlignment(JTextField.CENTER);
		tf.setMinimumSize(new Dimension(FontUtil.getWidth() * len, FontUtil.getHeight()));
		p.add(tf, (towrap ? MIG.WRAP : ""));
		return tf;
	}

	/**
	 * initalize the challenge values
	 *
	 * @return
	 */
	private JPanel valuesInit() {
		valuesPanel = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE3)));
		//p12 JPanel for activity
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE3, MIG.WRAP), "[][][]"));
		// start date, words writed, words to write
		initLine(p, "start", dateStart, "writed", wordsWrited, "towrite", wordsTowrite);
		// min words, average words, max words
		initLine(p, "words_min", wordsMin, "words_mean", wordsMean, "words_max", wordsMax);
		// date of today, days to left, % realized
		initLine(p, "today", today, "days_left", leftDays, "words_by_day", leftWords);
		// empty, end date, empty
		initLine(p, "", null, "end", endday, "", null);
		diagramInit(p);
		valuesPanel.add(p, MIG.WRAP);
		return valuesPanel;
	}

	private String getDates(int day) {
		Date dt = DateUtil.addDays(challenge.beginDateGet(), day);
		return DateUtil.simpleDateToString(dt);
	}

	/**
	 * initialize the barChart to show values
	 *
	 * @return
	 */
	private JPanel tableInit() {
		barPanel = new JPanel(new MigLayout(MIG.FILL));
		int max = Math.max(challenge.valuesMax(), challenge.wordsAverageGet());
		dataset = new ArrayList<>();
		for (int i = 0; i < NBVALUES; i++) {
			dataset.add(new DatasetItem("date", 0, Color.GREEN));
		}
		barChart = new BarChart(BarChart.HORIZONTAL,
			challenge.nbDaysGet(), max, dataset);
		Dimension dim = new Dimension(FontUtil.getHeight(),
			FontUtil.getHeight() * (challenge.nbDaysGet() + 1));
		barChart.setMinimumSize(dim);
		//dim = SwingUtil.getScreenSize();
		dim.width = getWidth() - (FontUtil.getWidth() * 6);
		barChart.setPreferredSize(dim);
		barChart.setMaximumSize(SwingUtil.getScreenSize());
		JScrollPane scroll = new JScrollPane(barChart);
		SwingUtil.setSizes(scroll);
		barPanel.add(scroll, MIG.GROW);
		return barPanel;
	}

	/**
	 * initialize the drawing for global realized objective
	 */
	private void diagramInit(JPanel p) {
		diagram = new JPanel(new MigLayout());
		progress = new CircleProgressBar(0, challenge.nbWordsGet());
		diagram.add(progress, MIG.get(MIG.SPAN, MIG.CENTER));
		p.add(diagram, MIG.get(MIG.SPAN, MIG.CENTER));
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

	/**
	 * begin a new challenge
	 */
	private void beginChallenge() {
		if (doChallenge.getName().equals("btCancel")) {
			if (JOptionPane.showConfirmDialog(mainFrame,
				I18N.getMsg("challenge.ask_cancel"),
				I18N.getMsg("challenge"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
				return;
			}
			doChallenge.setName("btStart");
			doChallenge.setText(I18N.getMsg("challenge.begin"));
			challenge.init();
			challengeDlg.dispose();
			mainFrame.setUpdated();
			return;
		}
		int dnb = StringUtil.getInteger(daysNb.getText()),
			wnb = StringUtil.getInteger(wordsNb.getText());
		if (dnb == 0 || dnb > Challenge.NBVALUES) {
			MessageDlg.show(this, I18N.getMsg("challenge.err_days"),
				I18N.getMsg("challenge"), true);
			return;
		}
		if (wnb < 1) {
			MessageDlg.show(this, I18N.getMsg("challenge.err_words"),
				I18N.getMsg("challenge"), true);
			return;
		}
		challenge.nbDaysSet(dnb);
		challenge.nbWordsSet(wnb);
		challenge.beginDateSet();
		challenge.valuesInit();
		int nbw = BookUtil.getNbWords(mainFrame.getProject());
		challenge.beginWordsSet(nbw);
		challenge.lastWordsSet(nbw);
		daysNb.setText(dnb + "");
		wordsNb.setText(wnb + "");
		progress.setMaximum(wnb);
		barChart.setNbValues(dnb);
		tableSet();
		dataSet();
		mainFrame.setUpdated();
	}

	/**
	 * set the datas of the challenge
	 */
	private void dataSet() {
		if (challenge.nbDaysGet() > 0) {
			daysNb.setText(challenge.nbDaysGet().toString());
			wordsNb.setText(challenge.nbWordsGet().toString());
			doChallenge.setName("btCancel");
			doChallenge.setText(I18N.getMsg("challenge.terminate"));
			pWordsByDay.setVisible(true);
		} else {
			daysNb.setText("");
			wordsNb.setText("");
			doChallenge.setName("btBegin");
			doChallenge.setText(I18N.getMsg("challenge.begin"));
			pWordsByDay.setVisible(false);
		}
		dateStart.setText(DateUtil.simpleDateTimeToString(challenge.beginDateGet()));
		boolean b = dataCompute();
		daysNb.setEditable(!b);
		wordsNb.setEditable(!b);
		valuesPanel.setVisible(b);
		barPanel.setVisible(b);
		diagram.setVisible(b);
		challengeDlg.pack();
		challengeDlg.setLocationRelativeTo(mainFrame);
	}

	/**
	 * set the barChart values
	 */
	private void tableSet() {
		if (challenge.nbDaysGet() > 0) {
			dataset.clear();
			int sum = 0;
			String day = DateUtil.simpleDateToString(challenge.beginDateGet());
			dataset.add(new DatasetItem(day, challenge.wordsFor(0), Color.GREEN));
			for (int i = 1; i < challenge.nbDaysGet(); i++) {
				day = DateUtil.simpleDateToString(DateUtil.addDays(new Date(challenge.beginDateGet().getTime()), i));
				dataset.add(new DatasetItem(day, challenge.wordsFor(i), Color.GREEN));
				sum = sum + challenge.wordsFor(i);
			}
			barChart.setObjective(challenge.nbWordsGet() / challenge.nbDaysGet());
			barChart.setValues(dataset);
			barChart.repaint();
			progress.setValue(sum);
			progress.repaint();
		}
	}

	/**
	 * compute all datas
	 */
	private boolean dataCompute() {
		if (challenge.isActive()) {
			Integer writed = BookUtil.getNbWords(mainFrame.getProject()) - challenge.beginWordsGet();
			wordsWrited.setText(writed.toString());
			Integer towrite = challenge.nbWordsGet() - writed;
			wordsTowrite.setText(towrite.toString());
			wordsByDay.setText((challenge.nbWordsGet() / challenge.nbDaysGet()) + "");
			Integer left = challenge.daysGetLeft();
			Integer r = -1;
			if (towrite > 0 && left > 0) {
				r = towrite / left;
			}
			wordsMin.setText(challenge.valuesMin().toString());
			wordsMean.setText(challenge.valuesAverage().toString());
			wordsMax.setText(challenge.valuesMax().toString());
			today.setText(DateUtil.simpleDateToString(new Date()));
			leftDays.setText(left.toString());
			leftWords.setText(r.toString());
			int pc = writed - challenge.beginWordsGet();
			boolean b = true;
			if (pc < challenge.nbWordsGet()) {
				endday.setText(DateUtil.simpleDateTimeToString(challenge.dateEndGet()));
				tableSet();
			} else {
				endday.setText(I18N.getMsg("challenge.finished"));
				b = false;
			}
			return b;
		}
		return false;
	}

	/**
	 * initialize a line of 3 JTextField
	 *
	 * @param p: the JPanel where to set the fields
	 * @param l1: label for the first field
	 * @param t1; first JTextField
	 * @param l2: label for the second field
	 * @param t2: second JTextField
	 * @param l3: label for the third field
	 * @param t3: third JTextField
	 */
	private void initLine(JPanel p,
		String l1, JTextField t1,
		String l2, JTextField t2,
		String l3, JTextField t3) {
		if (l1.isEmpty()) {
			p.add(new JLabel(""));
		} else {
			p.add(new JLabel(I18N.getMsg("challenge." + l1)), MIG.CENTER);
		}
		if (l2.isEmpty()) {
			p.add(new JLabel(""));
		} else {
			p.add(new JLabel(I18N.getMsg("challenge." + l2)), MIG.CENTER);
		}
		if (l3.isEmpty()) {
			p.add(new JLabel(""));
		} else {
			p.add(new JLabel(I18N.getMsg("challenge." + l3)), MIG.CENTER);
		}
		if (l1.isEmpty()) {
			p.add(new JLabel(""));
		} else {
			initTf(p, t1, l1);
		}
		if (l2.isEmpty()) {
			p.add(new JLabel(""));
		} else {
			initTf(p, t2, l2);
		}
		if (l3.isEmpty()) {
			p.add(new JLabel(""));
		} else {
			initTf(p, t3, l3);
		}
	}

	/**
	 * initialize a reduced JTextField
	 *
	 * @param p: JPanel where to set the field
	 * @param tf: the JTextField to create
	 * @param name: name for the JTextField
	 */
	private void initTf(JPanel p, JTextField tf, String name) {
		tf.setName(name);
		tf.setColumns(COL_LEN);
		tf.setEditable(false);
		tf.setHorizontalAlignment(JTextField.CENTER);
		p.add(tf);
	}

}

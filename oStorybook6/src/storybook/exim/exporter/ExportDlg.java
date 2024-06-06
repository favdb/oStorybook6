/*
 * Copyright (C) 2017 favdb
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
package storybook.exim.exporter;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.book.BookParamExport;
import storybook.dialog.AbsDialog;
import storybook.exim.exporter.options.CSVpanel;
import storybook.exim.exporter.options.TXTpanel;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 * dialog for exporting some data
 *
 * @author favdb
 */
public class ExportDlg extends AbsDialog implements ActionListener, CaretListener {

    private static final String EXPORT = "export";

    private JTextField txFolder;
    private JComboBox cbReport, cbEntities;
    public ArrayList<ExportType> exports;
    //private HTMLpanel HTML;
    private CSVpanel CSV;
    private TXTpanel TXT;
    //private BOOKPanel BOOK;
    private JRadioButton rbHtml, rbTxt, rbCsv, rbXml;
    private JPanel pnFormat;
    private ArrayList<ExportType> entities;
    private String toCheck;
    private BookParamExport param;

    public ExportDlg(MainFrame m) {
        super(m);
        initAll();
    }

    public static void show(MainFrame m) {
        SwingUtil.showModalDialog(new ExportDlg(m), m, true);
    }

    @Override
    public void init() {
        param = book.param.getParamExport();
        toCheck = param.toCheck();
        initExports();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initUi() {
        //LOG.printInfos("ExportDlg.initUi()");
        super.initUi();
        setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.HIDEMODE2)));
        //setBackground(Color.white);
        setTitle(I18N.getMsg(EXPORT));

        JLabel lbFolder = new JLabel(I18N.getMsg("export.folder"));
        txFolder = new JTextField();
        File dir = new File(mainFrame.getBook().getParam().getParamExport().getDirectory());
        setFolder(dir);
        txFolder.setColumns(32);
        txFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                File f = new File(txFolder.getText());
                if (f.exists() && f.isDirectory()) {
                    mainFrame.getBook().getParam().getParamExport().setDirectory(txFolder.getText());
                    mainFrame.setUpdated();
                }
                setFolder(f);
            }
        });
        JButton bt = new JButton(IconUtil.getIconSmall(ICONS.K.F_OPEN));
        bt.addActionListener((java.awt.event.ActionEvent evt) -> {
            String s = txFolder.getText();
            if (txFolder.getText().isEmpty()) {
                s = mainFrame.getProject().getPath();
            }
            JFileChooser chooser = new JFileChooser(s);
            chooser.setFileSelectionMode(1);
            int i = chooser.showOpenDialog(null);
            if (i != 0) {
                return;
            }
            File file = chooser.getSelectedFile();
            setFolder(file.getAbsoluteFile());
            mainFrame.getBook().getParam().getParamExport().setDirectory(txFolder.getText());
            mainFrame.setUpdated();
            //LOG.printInfos("change folder OK");
        });
        bt.setMargin(new Insets(0, 0, 0, 0));
        add(lbFolder, MIG.SPLIT + " 3");
        add(txFolder);
        add(bt, MIG.WRAP);

        add(new JLabel(I18N.getMsg("export.type")), MIG.SPLIT + " 3");
        initReport();
        add(cbReport);
        add(cbEntities);
        cbEntities.setVisible(false);

        initFormat();
        add(pnFormat, MIG.get(MIG.NEWLINE, MIG.SPAN));
        CSV = new CSVpanel(param);
        pnFormat.add(CSV, MIG.get(MIG.NEWLINE, MIG.SKIP, MIG.SPAN));
        CSV.setVisible(false);
        TXT = new TXTpanel(param);
        pnFormat.add(TXT, MIG.get(MIG.NEWLINE, MIG.SKIP, MIG.SPAN));
        TXT.setVisible(false);
        pnFormat.setVisible(false);

        add(getOkButton(), MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.SG, MIG.RIGHT));
        add(getCancelButton(), MIG.SG);
        pack();
        setLocationRelativeTo(mainFrame);
    }

    private void initExports() {
        exports = new ArrayList<>();
        exports.add(new ExportType("summary", "export.book.summary"));
        exports.add(new ExportType("list", "export.list"));
        //exports.add(new ExportType("data", "export.data"));
        exports.add(new ExportType("data", "html.form"));
    }

    @SuppressWarnings("unchecked")
    private void initReport() {
        cbReport = new JComboBox();
        cbReport.setName("cbReport");
        cbReport.addItem(" ");
        for (ExportType export : exports) {
            cbReport.addItem(export);
        }
        cbReport.setSelectedIndex(0);
        cbReport.addActionListener(this);
        entities = new ArrayList<>();
        entities.add(new ExportType("parts", "parts"));
        entities.add(new ExportType("chapters", "chapters"));
        entities.add(new ExportType("scenes", "scenes"));
        entities.add(new ExportType("persons", "persons"));
        entities.add(new ExportType("locations", "locations"));
        entities.add(new ExportType("tags", "tags"));
        entities.add(new ExportType("items", "items"));
        entities.add(new ExportType("ideas", "ideas"));
        entities.add(new ExportType("endnotes", "endnotes"));
        cbEntities = new JComboBox();
        cbEntities.setName("cbEntities");
        for (ExportType t : entities) {
            cbEntities.addItem(t);
        }
    }

    @SuppressWarnings("unchecked")
    private void initFormat() {
        String m = "";
        if (pnFormat == null) {
            pnFormat = new JPanel(new MigLayout(MIG.HIDEMODE3));
            JLabel lb3 = new JLabel(I18N.getMsg("export.format") + ":");
            pnFormat.add(lb3);
            ButtonGroup group1 = new ButtonGroup();
            rbHtml = new JRadioButton("html");
            group1.add(rbHtml);
            pnFormat.add(rbHtml);
            rbHtml.addActionListener(this);
            rbTxt = new JRadioButton("txt");
            group1.add(rbTxt);
            pnFormat.add(rbTxt);
            rbTxt.addActionListener(this);
            rbCsv = new JRadioButton("csv");
            group1.add(rbCsv);
            pnFormat.add(rbCsv);
            rbCsv.addActionListener(this);
            rbXml = new JRadioButton("xml");
            group1.add(rbXml);
            pnFormat.add(rbXml);
            rbXml.addActionListener(this);
        } else {
            m = getFormat();
        }

        switch (getReportName()) {
            case "list":
                setAllowedFormat("csv,txt,html");
                m = "html";
                break;
            default:
                setAllowedFormat("csv,txt,html");
                break;
        }
        setFormat(m);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox) {
            JComboBox cb = (JComboBox) e.getSource();
            if (cb.getName().equals("cbReport") && cb.getSelectedIndex() > 0) {
                Object cbsel = cb.getSelectedItem();
                String sel = "";
                if (cbsel instanceof String) {
                    sel = (String) cbsel;
                }
                if (cbsel instanceof ExportType) {
                    sel = ((ExportType) cbsel).getTitle();
                }
                if (sel.equals(I18N.getMsg("export.list"))
                        || sel.equals(I18N.getMsg("export.data"))
                        || sel.equals(I18N.getMsg("html.form"))) {
                    cbEntities.setVisible(true);
                } else {
                    cbEntities.setVisible(false);
                }
                pnFormat.setVisible(cbEntities.isVisible());
                if (sel.equals(I18N.getMsg("html.form"))) {
                    pnFormat.setVisible(false);
                }
                pack();
            }
        } else if (e.getSource() instanceof JRadioButton) {
            JRadioButton rb = (JRadioButton) e.getSource();
            CSV.setVisible(rb.getText().equals("csv"));
            TXT.setVisible(rb.getText().equals("txt"));
            pack();
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        // empty
    }

    @Override
    protected AbstractAction getOkAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cbReport.getSelectedIndex() < 1) {
                    return;
                }
                if (doExport()) {
                    dispose();
                }
            }
        };
    }

    private String getReportName() {
        if (cbReport.getSelectedIndex() == 0) {
            return "";
        }
        return (((ExportType) cbReport.getSelectedItem()).getName().toLowerCase());
    }

    private boolean doExport() {
        //LOG.trace("ExportDlg.doExport()");
        String title = book.getTitle();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    I18N.getMsg("export.missing.title"),
                    I18N.getMsg(EXPORT), 1);
            return (false);
        }
        // check if the folder exists
        String dir = txFolder.getText();
        if (dir.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    I18N.getMsg("export.dir.missing"),
                    I18N.getMsg(EXPORT), 1);
            return false;
        }
        //check if dir is a directory
        File f = new File(dir);
        if (!(f.exists() && f.isDirectory())) {
            JOptionPane.showMessageDialog(this,
                    I18N.getMsg("export.dir.error"),
                    I18N.getMsg(EXPORT), 1);
            return false;
        }
        if (!param.getDirectory().equals(dir)) {
            param.setDirectory(dir);
            mainFrame.setUpdated();
        }
        saveParam();
        String exportName = ((ExportType) cbReport.getSelectedItem()).getName().toLowerCase();
        String entityType = ((ExportType) cbEntities.getSelectedItem()).getName().toLowerCase();
        if (exportName.equals("list") || exportName.equals("data")) {
            exportName += "_" + entityType;
        }
        String exportTitle = ((ExportType) cbReport.getSelectedItem()).getTitle();
        String format = getFormat();
        if (exportName.startsWith("list_")) {
            ExportList.doExec(mainFrame, format, exportName);
            return true;
        }
        String ret = "";
        int n = 0;
        ExportTable exporter = new ExportTable(mainFrame, format);
        if ("list_all".equals(exportName)) {
            for (ExportType t : entities) {
                if (exporter.askFileExists("list_" + t.getTitle())) {
                    ret += I18N.getMsg(t.title) + "\n";
                    n++;
                }
            }
        } else if ("data_all".equals(exportName)) {
            for (ExportType t : entities) {
                if (exporter.askFileExists("data_" + t.getTitle())) {
                    ret += I18N.getMsg(t.title) + "\n";
                    n++;
                }
            }
        } else if (exporter.askFileExists(exportName)) {
            ret += exportTitle + "\n";
        }
        if (!ret.replace("\n", "").isEmpty() && JOptionPane.showConfirmDialog(this.getParent(),
                (n > 1 ? I18N.getMsg("export.replace", ret) : I18N.getMsg("export.replaces", ret)),
                I18N.getMsg(EXPORT),
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
            return (false);
        }
        SwingUtil.setWaitingCursor(this);
        switch (exportName) {
            case "summary":
                ExportInfoView.exec(mainFrame, exportName);
                break;
            case "list_all":
                for (ExportType t : entities) {
                    if (t.isList) {
                        ExportTable.exportTable(mainFrame, t.getName(), format);
                    }
                }
                break;
            case "data_all":
                for (ExportType t : entities) {
                    if (t.isList) {
                        ExportInfoView.exec(mainFrame, t.getName());
                    }
                }
                break;
            default:
                if (exportName.startsWith("list")) {
                    ExportTable.exportTable(mainFrame, entityType, format);
                }
                if (exportName.startsWith("data")) {
                    ExportInfoView.exec(mainFrame, entityType);
                }
                break;
        }
        SwingUtil.setDefaultCursor(this);
        if (!ret.isEmpty()) {
            return (false);
        }
        return (true);
    }

    private void saveParam() {
        CSV.apply();
        TXT.apply();
    }

    private String getFormat() {
        if (rbTxt.isSelected()) {
            return ("txt");
        }
        if (rbCsv.isSelected()) {
            return ("csv");
        }
        if (rbXml.isSelected()) {
            return ("xml");
        }
        return ("html");
    }

    private void setFormat(String m) {
        switch (m) {
            case "txt":
                rbTxt.setSelected(true);
                return;
            case "csv":
                rbCsv.setSelected(true);
                return;
            case "xml":
                rbXml.setSelected(true);
                return;
            default:
                break;
        }
        rbHtml.setSelected(true);
    }

    private void setAllowedFormat(String str) {
        rbHtml.setEnabled(str.contains("html"));
        rbCsv.setEnabled(str.contains("csv"));
        rbTxt.setEnabled(str.contains("txt"));
        rbXml.setEnabled(str.contains("xml"));
    }

    private void setFolder(File file) {
        UIDefaults defaults = javax.swing.UIManager.getDefaults();
        txFolder.setText(file.getAbsolutePath());
        if (!file.exists()) {
            txFolder.setForeground(Color.WHITE);
            txFolder.setBackground(Color.RED);
        } else {
            txFolder.setForeground(defaults.getColor("TextField.foreground"));
            txFolder.setBackground(defaults.getColor("TextField.background"));
        }
    }

    public class ExportType {

        private String name;
        private String title;
        private boolean isList;

        public ExportType(String name, String key) {
            this.name = name;
            this.title = I18N.getMsg(key);
            this.isList = (name.contains("list"));
        }

        public void setExportName(String name) {
            this.name = name;
        }

        public void setKey(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return getTitle();
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return (title);
        }

    }
}

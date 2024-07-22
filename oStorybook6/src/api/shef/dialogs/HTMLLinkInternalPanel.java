/*
 * Created on April 29, 2018
 *
 */
package api.shef.dialogs;

import api.shef.actions.TextEditPopupManager;
import i18n.I18N;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MainFrame;

public class HTMLLinkInternalPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JPanel hlinkPanel = null;
    private JComboBox urlField = null;
    private JTextField textField = null;
    private MainFrame mainFrame;

    public HTMLLinkInternalPanel(MainFrame m) {
	super();
	mainFrame = m;
	initialize();
    }

    public void setLinkText(String text) {
	textField.setText(text);
    }

    public String getLinkText() {
	return textField.getText();
    }

    private void initialize() {
	this.setLayout(new BorderLayout(5, 5));
	this.setSize(328, 218);
	this.add(getHlinkPanel(), java.awt.BorderLayout.NORTH);

	TextEditPopupManager popupMan = TextEditPopupManager.getInstance();
	//popupMan.registerJTextComponent(urlField);
	popupMan.registerJTextComponent(textField);
    }

    private JPanel getHlinkPanel() {
	if (hlinkPanel == null) {
	    hlinkPanel = new JPanel();
	    hlinkPanel.setBorder(BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder(null, I18N.getMsg("shef.link"),
			    TitledBorder.DEFAULT_JUSTIFICATION,
			    TitledBorder.DEFAULT_POSITION, null, null),
		    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
	    hlinkPanel.setLayout(new GridBagLayout());
	    JSLabel urlLabel = new JSLabel(I18N.getMsg("chapter") + "+" + I18N.getMsg("scene"));
	    hlinkPanel.add(urlLabel, new GBC("0,0,anchor W, ins 0 0 5 5"));
	    hlinkPanel.add(getUrlField(), new GBC("0,1,fill H, wx 1.0, ins 0 0 5 0"));
	    JSLabel textLabel = new JSLabel(I18N.getMsg("shef.text"));
	    hlinkPanel.add(textLabel, new GBC("1,0,anchor W, ins 0 0 5 5"));
	    hlinkPanel.add(getTextField(), new GBC("1,1,fill H,wx 1.0, ins 0 0 5 0"));
	}
	return hlinkPanel;
    }

    @SuppressWarnings("unchecked")
    private JComboBox getUrlField() {
	if (urlField == null) {
	    List<StringChapScene> list = new ArrayList<>();
	    List<Chapter> chapters = (List<Chapter>) mainFrame.project.chapters.getList();
	    List<Scene> scenes = (List<Scene>) mainFrame.project.scenes.getList();
	    for (Chapter chapter : chapters) {
		list.add(new StringChapScene(chapter.getId(),
			0, chapter.getChapterno() + ":" + chapter.getName()));
		for (Scene scene : scenes) {
		    if (scene.getChapter() == null) {
			continue;
		    }
		    if (scene.getChapter().equals(chapter)) {
			list.add(new StringChapScene(scene.getChapter().getId(),
				scene.getId(), "    " + (scene.getChapterSceneNo() + scene.getTitle())));
		    }
		}
	    }
	    urlField = new JComboBox(list.toArray());
	    urlField.setMaximumRowCount(15);
	}
	return urlField;
    }

    private JTextField getTextField() {
	if (textField == null) {
	    textField = new JTextField();
	}
	return textField;
    }

    public String getUrl() {
	StringChapScene link = (StringChapScene) urlField.getModel().getSelectedItem();
	if (link == null) {
	    return ("");
	}
	return link.getIdent();
    }

    private static class StringChapScene {

	long chapterId;
	long sceneId;
	String text;

	public StringChapScene(long c, long s, String t) {
	    chapterId = c;
	    sceneId = s;
	    text = t;
	}

	public String getLink() {
	    if (sceneId == 0L) {
		return (Long.toString(chapterId));
	    }
	    return (Long.toString(chapterId) + "." + Long.toString(sceneId));
	}

	public String getIdent() {
	    return String.format("S%03d", chapterId, sceneId);
	}

	@Override
	public String toString() {
	    return (text);
	}
    }

}

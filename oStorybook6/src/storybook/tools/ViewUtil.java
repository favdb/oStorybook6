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
package storybook.tools;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import storybook.action.ScrollToEntityAction;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.AbstractScenePanel;
import storybook.ui.panel.book.BookPanel;
import storybook.ui.panel.book.BookScenePanel;
import storybook.ui.panel.chrono.SceneSticker;
import storybook.ui.panel.book.StrandDateLabel;
import storybook.ui.panel.manage.ManageChapter;
import storybook.ui.panel.manage.ManagePanel;
import storybook.ui.panel.manage.ManageSceneDnd;

/**
 * Provides tools around the views.
 *
 * @author martin
 *
 */
public class ViewUtil {

	private ViewUtil() {
		// empty
	}

	public static void scrollToTop(final JScrollPane scroller) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JViewport viewport = scroller.getViewport();
				JComponent comp = (JComponent) viewport.getView();
				if (comp instanceof JTextPane) {
					JTextPane textPane = (JTextPane) comp;
					textPane.setCaretPosition(0);
				} else {
					viewport.setViewPosition(new Point(0, 0));
				}
			}
		});
	}

	public static boolean scrollToStrandDate(AbstractPanel container, JPanel panel, Strand strand, Date date) {
		if (strand == null || date == null) {
			return false;
		}
		return doScrolling(container, panel, strand, date);
	}

	public static boolean scrollToChapter(AbstractPanel container, JPanel panel, Chapter chapter) {
		if (container instanceof ManagePanel) {
			if (chapter == null) {
				return false;
			}
			final ScrollToEntityAction action = new ScrollToEntityAction(container, panel, chapter);
			Timer timer = new Timer(200, action);
			timer.setRepeats(false);
			timer.start();
			return action.isFound();
		}
		// chrono and book view
		@SuppressWarnings("null")
		MainFrame mainFrame = container.getMainFrame();
		Scene scene = mainFrame.project.scenes.findFirst(chapter);
		return scrollToScene(container, panel, scene);
	}

	public static boolean scrollToScene(AbstractPanel container, JPanel panel, Scene scene) {
		if (scene == null) {
			return false;
		}
		final ScrollToEntityAction action = new ScrollToEntityAction(container, panel, scene);
		Timer timer = new Timer(200, action);
		timer.setRepeats(false);
		timer.start();
		return action.isFound();
	}

	public static boolean doScrolling(AbstractPanel container, JPanel panel, Scene scene) {
		boolean found = false;
		List<AbstractScenePanel> panels = findScenePanels(container);
		for (AbstractScenePanel scenePanel : panels) {
			Scene sc = scenePanel.getScene();
			if (sc == null) {
				continue;
			}
			if (scene.getId().equals(sc.getId())) {
				Rectangle rect = scenePanel.getBounds();
				/*if (container instanceof ChronoPanel) {
					rect = SwingUtilities.convertRectangle(scenePanel.getParent(), rect, panel);
				}*/
				if (container instanceof ManagePanel) {
					rect = SwingUtilities.convertRectangle(scenePanel.getParent(), rect, panel);
				}
				SwingUtil.expandRectangle(rect);
				// scroll
				panel.scrollRectToVisible(rect);
				// flash the found component
				//SwingUtil.flashComponent(scenePanel);
				found = true;
				break;
			}
		}
		return found;
	}

	public static boolean doScrolling(AbstractPanel container, JPanel panel, Chapter chapter) {
		boolean found = false;
		List<ManageChapter> panels = findChapterPanels(container);
		for (ManageChapter scenePanel : panels) {
			Chapter ch = scenePanel.getChapter();
			if (ch == null) {
				continue;
			}
			if (chapter.getId().equals(ch.getId())) {
				Rectangle rect = scenePanel.getBounds();
				SwingUtil.expandRectangle(rect);
				// scroll and repaint
				panel.scrollRectToVisible(rect);
				// flash the found component
				//SwingUtil.flashComponent(scenePanel);
				found = true;
				break;
			}
		}
		return found;
	}

	public static boolean doScrolling(AbstractPanel container, JPanel panel, Strand strand, Date date) {
		boolean found = false;
		List<StrandDateLabel> panels = findStrandDateLabels(container);
		for (StrandDateLabel sdPanel : panels) {
			Strand s = sdPanel.getStrand();
			Date d = sdPanel.getDate();
			if (s == null || d == null) {
				continue;
			}
			if (strand.getId().equals(s.getId()) && date.compareTo(d) == 0) {
				JComponent comp;
				/*if (container instanceof ChronoPanel) {
					comp = (JComponent) sdPanel.getParent();
				} else */
				if (container instanceof BookPanel) {
					comp = (JComponent) sdPanel.getParent().getParent();
				} else {
					break;
				}
				Rectangle rect = comp.getBounds();
				SwingUtil.expandRectangle(rect);
				// scroll
				panel.scrollRectToVisible(rect);
				// flash the found component
				//SwingUtil.flashComponent(comp);
				found = true;
				break;
			}
		}
		return found;
	}

	public static List<AbstractScenePanel> findScenePanels(Container cont) {
		/*if (cont instanceof ChronoPanel) {
			return findChronoScenePanels(cont);
		}*/
		if (cont instanceof BookPanel) {
			return findBookScenePanels(cont);
		}
		if (cont instanceof ManagePanel) {
			return findManageScenePanels(cont);
		}
		return new ArrayList<>();
	}

	private static List<AbstractScenePanel> findBookScenePanels(Container cont) {
		List<Component> components = new ArrayList<>();
		components = SwingUtil.findComponentsByClass(cont, BookScenePanel.class, components);
		List<AbstractScenePanel> panels = new ArrayList<>();
		for (Component comp : components) {
			panels.add((AbstractScenePanel) comp);
		}
		return panels;
	}

	private static List<AbstractScenePanel> findChronoScenePanels(Container cont) {
		List<Component> components = new ArrayList<>();
		components = SwingUtil.findComponentsByClass(cont,
				SceneSticker.class, components);
		List<AbstractScenePanel> panels = new ArrayList<>();
		for (Component comp : components) {
			panels.add((AbstractScenePanel) comp);
		}
		return panels;
	}

	/**
	 * find the Scene Panels in the Manage view
	 *
	 * @param cont
	 * @return
	 */
	private static List<AbstractScenePanel> findManageScenePanels(Container cont) {
		List<Component> components = new ArrayList<>();
		components = SwingUtil.findComponentsByClass(cont, ManageSceneDnd.class, components);
		List<AbstractScenePanel> panels = new ArrayList<>();
		for (Component comp : components) {
			panels.add((AbstractScenePanel) comp);
		}
		return panels;
	}

	private static List<ManageChapter> findChapterPanels(Container cont) {
		List<Component> components = new ArrayList<>();
		components = SwingUtil.findComponentsByClass(cont, ManageChapter.class, components);
		List<ManageChapter> panels = new ArrayList<>();
		for (Component comp : components) {
			panels.add((ManageChapter) comp);
		}
		return panels;
	}

	private static List<StrandDateLabel> findStrandDateLabels(Container cont) {
		List<Component> components = new ArrayList<>();
		components = SwingUtil.findComponentsByClass(cont, StrandDateLabel.class, components);
		List<StrandDateLabel> labels = new ArrayList<>();
		for (Component comp : components) {
			labels.add((StrandDateLabel) comp);
		}
		return labels;
	}
}

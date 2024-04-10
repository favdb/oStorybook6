/*
 * Copyright (C) 2004 NNL Technology AB
 * Visit www.infonode.net for information about InfoNode(R) 
 * products and how to contact NNL Technology AB.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA 02111-1307, USA.
 */
// $Id: TabWindow.java,v 1.57 2007/01/28 21:25:10 jesper Exp $
package api.infonode.docking;

import api.infonode.docking.drag.DockingWindowDragSource;
import api.infonode.docking.drag.DockingWindowDragger;
import api.infonode.docking.drag.DockingWindowDraggerProvider;
import api.infonode.docking.internal.WriteContext;
import api.infonode.docking.internalutil.*;
import api.infonode.docking.model.TabWindowItem;
import api.infonode.docking.model.ViewWriter;
import api.infonode.docking.properties.TabWindowProperties;
import api.infonode.properties.base.Property;
import api.infonode.properties.propertymap.PropertyMap;
import api.infonode.properties.propertymap.PropertyMapTreeListener;
import api.infonode.properties.propertymap.PropertyMapWeakListenerManager;
import api.infonode.properties.util.PropertyChangeListener;
import api.infonode.tabbedpanel.TabAdapter;
import api.infonode.tabbedpanel.TabEvent;
import api.infonode.tabbedpanel.TabRemovedEvent;
import api.infonode.util.ArrayUtil;
import api.infonode.util.Direction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * A docking window containing a tabbed panel.
 *
 * @author $Author: jesper $
 * @version $Revision: 1.57 $
 */
public class TabWindow extends AbstractTabWindow {

	private static final ButtonInfo[] buttonInfos = {
		new UndockButtonInfo(TabWindowProperties.UNDOCK_BUTTON_PROPERTIES),
		new DockButtonInfo(TabWindowProperties.DOCK_BUTTON_PROPERTIES),
		new MinimizeButtonInfo(TabWindowProperties.MINIMIZE_BUTTON_PROPERTIES),
		new MaximizeButtonInfo(TabWindowProperties.MAXIMIZE_BUTTON_PROPERTIES),
		new RestoreButtonInfo(TabWindowProperties.RESTORE_BUTTON_PROPERTIES),
		new CloseButtonInfo(TabWindowProperties.CLOSE_BUTTON_PROPERTIES)
	};

	private AbstractButton[] buttons = new AbstractButton[buttonInfos.length];

	private PropertyChangeListener minimumSizePropertiesListener =
			(Property property, Object valueContainer, Object oldValue, Object newValue) -> {
		revalidate();
	};
	private PropertyMapTreeListener buttonFactoryListener =
			(Map changes) -> {
		doUpdateButtonVisibility(changes);
	};

	/**
	 * Creates an empty tab window.
	 */
	public TabWindow() {
		this((DockingWindow) null);
	}

	/**
	 * Creates a tab window with a tab containing the child window.
	 *
	 * @param window the child window
	 */
	public TabWindow(DockingWindow window) {
		this(window == null ? null : new DockingWindow[]{window});
	}

	/**
	 * Creates a tab window with tabs for the child windows.
	 *
	 * @param windows the child windows
	 */
	public TabWindow(DockingWindow[] windows) {
		this(windows, null);
	}

	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	protected TabWindow(DockingWindow[] windows, TabWindowItem windowItem) {
		super(true, windowItem == null ? new TabWindowItem() : windowItem);

		setTabWindowProperties(((TabWindowItem) getWindowItem()).getTabWindowProperties());

		PropertyMapWeakListenerManager.addWeakPropertyChangeListener(getTabWindowProperties().getMap(),
				TabWindowProperties.RESPECT_CHILD_WINDOW_MINIMUM_SIZE,
				minimumSizePropertiesListener);

		new DockingWindowDragSource(getTabbedPanel(), (MouseEvent mouseEvent) -> {
			if (!getWindowProperties().getDragEnabled()) {
				return null;
			}
			Point p = SwingUtilities.convertPoint((Component) mouseEvent.getSource(),
					mouseEvent.getPoint(),
					getTabbedPanel());
			return getTabbedPanel().tabAreaContainsPoint(p)
					? (getChildWindowCount() == 1 ? getChildWindow(0) : TabWindow.this).startDrag(getRootWindow())
					: null;
		});

		initMouseListener();
		init();

		getTabbedPanel().addTabListener(new TabAdapter() {
			@Override
			public void tabAdded(TabEvent event) {
				doUpdateButtonVisibility(null);
			}

			@Override
			public void tabRemoved(TabRemovedEvent event) {
				doUpdateButtonVisibility(null);
			}
		});

		if (windows != null) {
			for (DockingWindow window : windows) {
				addTab(window);
			}
		}

		PropertyMapWeakListenerManager.addWeakTreeListener(getTabWindowProperties().getMap(),
				buttonFactoryListener);
	}

	@Override
	public TabWindowProperties getTabWindowProperties() {
		return ((TabWindowItem) getWindowItem()).getTabWindowProperties();
	}

	@Override
	@SuppressWarnings("NonPublicExported")
	protected void tabSelected(WindowTab tab) {
		super.tabSelected(tab);

		if (getUpdateModel()) {
			((TabWindowItem) getWindowItem()).setSelectedItem(
					tab == null ? null : getWindowItem().getChildWindowContaining(tab.getWindow().getWindowItem()));
		}
	}

	@Override
	protected void update() {
	}

	@Override
	protected void updateButtonVisibility() {
		doUpdateButtonVisibility(null);
	}

	private void doUpdateButtonVisibility(Map changes) {
		//System.out.println("%%  Updating tab window buttons!");
		//System.out.println(getTabbedPanel().getTabCount() + "  " + getTabbedPanel().getSelectedTab() + "  " + isMaximizable() + "  " + isUndockable() + "  " + isMinimizable() + "  " + isClosable());
		if (InternalDockingUtil.updateButtons(buttonInfos,
				buttons,
				null,
				this,
				getTabWindowProperties().getMap(),
				changes)) {
			updateTabAreaComponents();
		}

		super.updateButtonVisibility();
	}

	@Override
	protected int getTabAreaComponentCount() {
		return ArrayUtil.countNotNull(buttons);
	}

	@Override
	protected void getTabAreaComponents(int index, JComponent[] components) {
		for (AbstractButton button : buttons) {
			if (button != null) {
				components[index++] = button;
			}
		}
	}

	@Override
	protected void optimizeWindowLayout() {
		if (getWindowParent() == null) {
			return;
		}

		if (getTabbedPanel().getTabCount() == 0) {
			internalClose();
		} else if (getTabbedPanel().getTabCount() == 1
				&& (getWindowParent().showsWindowTitle() || !getChildWindow(0).needsTitleWindow())) {
			getWindowParent().internalReplaceChildWindow(this, getChildWindow(0).getBestFittedWindow(getWindowParent()));
		}
	}

	@Override
	public int addTab(DockingWindow w, int index) {
		int actualIndex = super.addTab(w, index);
		setSelectedTab(actualIndex);
		return actualIndex;
	}

	@Override
	protected int addTabNoSelect(DockingWindow window, int index) {
		DockingWindow beforeWindow = index == getChildWindowCount() ? null : getChildWindow(index);

		int i = super.addTabNoSelect(window, index);

		if (getUpdateModel()) {
			addWindowItem(window, beforeWindow == null ? -1 : getWindowItem().getWindowIndex(
					getWindowItem().getChildWindowContaining(beforeWindow.getWindowItem())));
		}

		return i;
	}

	@Override
	protected void updateWindowItem(RootWindow rootWindow) {
		super.updateWindowItem(rootWindow);
		((TabWindowItem) getWindowItem()).setParentTabWindowProperties(rootWindow == null
				? TabWindowItem.emptyProperties
				: rootWindow.getRootWindowProperties()
						.getTabWindowProperties());
	}

	@Override
	protected PropertyMap getPropertyObject() {
		return getTabWindowProperties().getMap();
	}

	@Override
	protected PropertyMap createPropertyObject() {
		return new TabWindowProperties().getMap();
	}

	@Override
	protected int getEdgeDepth(Direction dir) {
		return dir == getTabbedPanel().getProperties().getTabAreaOrientation()
				? 1
				: super.getEdgeDepth(dir);
	}

	@Override
	protected int getChildEdgeDepth(DockingWindow window, Direction dir) {
		return dir == getTabbedPanel().getProperties().getTabAreaOrientation()
				? 0
				: 1 + super.getChildEdgeDepth(window, dir);
	}

	@Override
	protected DockingWindow getOptimizedWindow() {
		return getChildWindowCount() == 1 ? getChildWindow(0).getOptimizedWindow() : super.getOptimizedWindow();
	}

	@Override
	protected boolean acceptsSplitWith(DockingWindow window) {
		return super.acceptsSplitWith(window) && (getChildWindowCount() != 1 || getChildWindow(0) != window);
	}

	@Override
	protected DockingWindow getBestFittedWindow(DockingWindow parentWindow) {
		return getChildWindowCount() == 1 && (!getChildWindow(0).needsTitleWindow() || parentWindow.showsWindowTitle())
				? getChildWindow(0).getBestFittedWindow(parentWindow) : this;
	}

	@Override
	protected void write(ObjectOutputStream out, WriteContext context, ViewWriter viewWriter) throws IOException {
		out.writeInt(WindowIds.TAB);
		viewWriter.writeWindowItem(getWindowItem(), out, context);
		super.write(out, context, viewWriter);
	}

}

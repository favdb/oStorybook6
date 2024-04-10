/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

import i18n.I18N;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.tools.LOG;
import storybook.shortcut.Shortcuts;

/**
 * Manages compound undoable edits.
 *
 * Before an undoable edit happens on a particular document, you should call the static method
 * CompoundUndoManager.beginCompoundEdit(doc)
 *
 * Conversely after an undoable edit happens on a particular document, you shoulc call the static
 * method CompoundUndoManager.beginCompoundEdit(doc)
 *
 * For either of these methods to work, you must add an instance of CompoundUndoManager as a
 * document listener... e.g
 *
 * doc.addUndoableEditListener(new CompoundUndoManager(doc, new UndoManager());
 *
 * Note that each CompoundUndoManager should have its own UndoManager.
 *
 *
 * @author Bob Tantlinger
 */
public class CompoundManager implements UndoableEditListener {

	/**
	 * Static undo action that works across all documents with a CompoundUndoManager registered as
	 * an UndoableEditListener
	 */
	public static Action ActionUndo = new UndoAction();

	/**
	 * Static undo action that works across all documents with a CompoundUndoManager registered as
	 * an UndoableEditListener
	 */
	public static Action ActionRedo = new RedoAction();

	private UndoManager undoer;
	private CompoundEdit compoundEdit = null;
	private Document document = null;
	private static List<Document> docs = new ArrayList<>();
	private static List<CompoundManager> lsts = new ArrayList<>();
	private static List<UndoManager> undoers = new ArrayList<>();

	protected static void registerDocument(Document doc, CompoundManager lst, UndoManager um) {
		docs.add(doc);
		lsts.add(lst);
		undoers.add(um);
	}

	/**
	 * Gets the undo manager for a document that has a CompoundUndoManager as an
	 * UndoableEditListener
	 *
	 * @param doc
	 * @return The registed undomanger for the document
	 */
	public static UndoManager getUndoManagerForDocument(Document doc) {
		if (undoers != null) {
			for (int i = 0; i < docs.size(); i++) {
				if (docs.get(i) == doc) {
					return (UndoManager) undoers.get(i);
				}
			}
		}

		return null;
	}

	/**
	 * Notifies the CompoundUndoManager for the specified Document that a compound edit is about to
	 * begin.
	 *
	 * @param doc
	 */
	public static void beginCompoundEdit(Document doc) {
		for (int i = 0; i < docs.size(); i++) {
			if (docs.get(i) == doc) {
				CompoundManager l = (CompoundManager) lsts.get(i);
				l.beginCompoundEdit();
				return;
			}
		}
	}

	/**
	 * Notifies the CompoundUndoManager for the specified Document that a compound edit is complete.
	 *
	 * @param doc
	 */
	public static void endCompoundEdit(Document doc) {
		for (int i = 0; i < docs.size(); i++) {
			if (docs.get(i) == doc) {
				CompoundManager l = (CompoundManager) lsts.get(i);
				l.endCompoundEdit();
				return;
			}
		}
	}

	/**
	 * Updates the enabled states of the ActionUndo and ActionRedo actions for the specified
	 * document
	 *
	 * @param doc
	 */
	public static void updateUndo(Document doc) {
		UndoManager um = getUndoManagerForDocument(doc);
		if (um != null) {
			ActionUndo.setEnabled(um.canUndo());
			ActionRedo.setEnabled(um.canRedo());
		}
	}

	/**
	 * Discards all edits for the specified Document
	 *
	 * @param doc
	 */
	public static void discardAllEdits(Document doc) {
		UndoManager um = getUndoManagerForDocument(doc);
		if (um != null) {
			um.discardAllEdits();
			ActionUndo.setEnabled(um.canUndo());
			ActionRedo.setEnabled(um.canRedo());
		}
	}

	/**
	 * Creates a new CompoundUndoManager
	 *
	 * @param doc
	 * @param um The UndoManager to use for this document
	 */
	public CompoundManager(Document doc, UndoManager um) {
		undoer = um;
		document = doc;
		registerDocument(document, this, undoer);
	}

	/**
	 * Creates a new CompoundUndoManager
	 *
	 * @param doc
	 */
	public CompoundManager(Document doc) {
		this(doc, new UndoManager());
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent evt) {
		UndoableEdit edit = evt.getEdit();
		if (compoundEdit != null) {
			compoundEdit.addEdit(edit);
		} else {
			undoer.addEdit(edit);
			updateUndo(document);
		}
	}

	protected void beginCompoundEdit() {
		compoundEdit = new CompoundEdit();
	}

	protected void endCompoundEdit() {
		if (compoundEdit != null) {
			compoundEdit.end();
			undoer.addEdit(compoundEdit);
			updateUndo(document);
		}
		compoundEdit = null;
	}

	public static class UndoAction extends TextAction {

		public UndoAction() {
			super("edit-undo");
			putValue(Action.SMALL_ICON, IconUtil.getIconSmall(ICONS.K.EDIT_UNDO));
			putValue(MNEMONIC_KEY, I18N.getMnem("shef.undo"));
			//putValue(Action.ACCELERATOR_KEY, Shortcuts.getKeyStroke("shef", "undo"));
			putValue(SHORT_DESCRIPTION, Shortcuts.getTooltips("shef", "undo"));
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Document doc = getTextComponent(e).getDocument();
			UndoManager um = getUndoManagerForDocument(doc);
			if (um != null) {
				try {
					um.undo();
				} catch (CannotUndoException ex) {
					LOG.err("Unable to undo:", ex);
				}
				updateUndo(doc);
			}
		}
	}

	public static class RedoAction extends TextAction {

		public RedoAction() {
			super("edit-redo");
			putValue(Action.SMALL_ICON, IconUtil.getIconSmall(ICONS.K.EDIT_REDO));
			putValue(MNEMONIC_KEY, I18N.getMnem("shef.undo"));
			//putValue(Action.ACCELERATOR_KEY, Shortcuts.getKeyStroke("shef", "redo"));
			putValue(SHORT_DESCRIPTION, Shortcuts.getTooltips("shef", "redo"));
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Document doc = getTextComponent(e).getDocument();
			UndoManager um = getUndoManagerForDocument(doc);
			if (um != null) {
				try {
					um.redo();
				} catch (CannotUndoException ex) {
					LOG.err("Unable to redo", ex);
				}
				updateUndo(doc);
			}
		}
	}
}

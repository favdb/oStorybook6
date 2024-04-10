package storybook.ui.panel.planning;

import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;

public class PlanningElement {

	Object element;
	int size;
	int maxSize;
	int words;

	public Object getElement() {
		return element;
	}

	public void setElement(Object element) {
		this.element = element;
		if (element instanceof Part) {
			maxSize = ((Part) element).getObjectiveChars();
		} else if (element instanceof Chapter) {
			maxSize = ((Chapter) element).getObjectiveChars();
		}
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int size) {
		this.maxSize = size;
	}

	public int getWords() {
		return words;
	}

	public void setWords(int size) {
		this.words = size;
	}

	private static final String OPEN_PAR = "    (";
	private static final String CLOSE_PAR = ")";

	private String intoPar(int s) {
		return OPEN_PAR + s + CLOSE_PAR;
	}

	private String intoPar(int s, int max) {
		return OPEN_PAR + s + "/" + max + CLOSE_PAR;
	}

	@Override
	public String toString() {
		String t = "";
		String notes = "";
		if (element instanceof Part) {
			Part part = (Part) element;
			if (part.hasNotes()) {
				notes = "*";
			}
			t = part.getName() + notes + intoPar(size, maxSize);
			maxSize = part.getObjectiveChars();
		} else if (element instanceof Chapter) {
			Chapter chapter = (Chapter) element;
			if (chapter.hasNotes()) {
				notes = "*";
			}
			maxSize = chapter.getObjectiveChars();
			t = chapter.getName() + notes + intoPar(size, maxSize);
		} else if (element instanceof Scene) {
			Scene scene = ((Scene) element);
			if (scene.hasNotes()) {
				notes = "*";
			}
			t = scene.getTitle() + notes + intoPar(size);
		} else if (element instanceof String) {
			t = (String) element + intoPar(size, maxSize);
		}
		return (t);
	}

}

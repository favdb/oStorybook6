/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions.manager;

import java.util.Objects;

public class Separator {

	private String id;
	private Number weight;
	private boolean lineVisible;

	public Separator() {
		this(null, null, true);
	}

	public Separator(Number weight) {
		this(null, weight, true);
	}

	public Separator(String id, Number weight, boolean lineVisible) {
		this.id = id;
		this.weight = weight;
		this.lineVisible = lineVisible;
	}

	public String getId() {
		return this.id;
	}

	public Number getWeight() {
		return this.weight;
	}

	public void setWeight(Number weight) {
		this.weight = weight;
	}

	public boolean isLineVisible() {
		return this.lineVisible;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Separator) {
			Separator s = (Separator) obj;
			if (this.id == null) {
				if (s.id != null) {
					return false;
				}
			} else if (!this.id.equals(s.id)) {
				return false;
			}
			if (this.weight == null) {
				if (s.weight != null) {
					return false;
				}
			} else if (!this.weight.equals(s.weight)) {
				return false;
			}
			return (this.lineVisible == s.lineVisible);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 43 * hash + Objects.hashCode(this.id);
		hash = 43 * hash + Objects.hashCode(this.weight);
		hash = 43 * hash + (this.lineVisible ? 1 : 0);
		return hash;
	}
}

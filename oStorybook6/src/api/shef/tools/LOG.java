/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.tools;

import storybook.App;

public final class LOG {

	private LOG() {
		//nothing
	}

	private static int LEVEL = 4;
	public static final int NONE = 0,
			DEV = 1,
			DEBUG = 2,
			CONFIG = 3,
			ERR = 4,
			ALL = 5;
	private static final String stringLevel[] = {
		"NONE", "DEVEL", "DEBUG", "CONFIG", "ERROR", "ALL"
	};

	public static void setLevel(int level) {
		LEVEL = level;
		trace("Log level setting to " + stringLevel[level]);
	}

	public static void write(int priority, Throwable t) {
		if (App.isDev()) {
			t.printStackTrace(System.out);
		} else if (priority == ERR) {
			t.printStackTrace(System.err);
		} else if (priority <= LEVEL) {
			t.printStackTrace(System.out);
		}
	}

	public static void trace(String text) {
		write(1, text);
	}

	public static void message(String text) {
		write(2, text);
	}

	public static void err(String text, Exception... ex) {
		if (!text.isEmpty()) {
			write(ERR, text);
		}
		if (ex != null && ex.length > 0) {
			ex[0].printStackTrace(System.err);
		}
	}

	public static void write(int priority, String text) {
		if (priority == ERR) {
			System.err.println("Shef::" + text);
		} else if (priority <= LEVEL) {
			System.out.println("Shef::" + text);
		}
	}
}

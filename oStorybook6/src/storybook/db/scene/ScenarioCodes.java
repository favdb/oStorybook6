/*
 * Copyright (C) 2021 favdb
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
package storybook.db.scene;

import assistant.Assistant;
import i18n.I18N;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author favdb
 */
public class ScenarioCodes {

	public enum TYPE {
		SHOT,
		MVT,
		MOM,
		LOC,
		STAGE,
		START,
		END
	}

	public static String getValue(TYPE type, Integer value) {
		switch (type) {
			case SHOT:
				return getShot(value);
			case MVT:
				return getMvt(value);
			case MOM:
				return getMom(value);
			case LOC:
				return getLoc(value);
			default:
				break;
		}
		return "";
	}

	public enum STATUS {
		OUTLINE,
		DRAFT,
		EDIT1,
		EDIT2,
		DONE;
	}

	public static List getStatusList() {
		List<String> list = new ArrayList<>();
		for (STATUS st : STATUS.values()) {
			list.add(getStatus(st));
		}
		return list;
	}

	public static String getStatus(STATUS st) {
		return I18N.getMsg("status." + st.name().toLowerCase());
	}

	public enum SHOT {
		NONE,
		NORMAL,
		LOW,
		HIGH;
	}

	public static final String MVT = "FPTSGZD";

	public static List<String> getShotList() {
		List<String> list = new ArrayList<>();
		for (SHOT shot : SHOT.values()) {
			list.add(shot.name());
		}
		return list;
	}

	public static String getShot(int shot) {
		if (shot == 0) {
			return "";
		}
		return I18N.getMsg("shot." + getShotList().get(shot).toLowerCase());
	}

	public static int getShotByName(String name) {
		for (SHOT shot : SHOT.values()) {
			if (shot.name().equalsIgnoreCase(name)) {
				return shot.ordinal();
			}
		}
		return 0;
	}

	public static List getLightList() {
		List<String> list = new ArrayList<>();
		list.add(I18N.getMsg("???"));
		list.add(I18N.getMsg("light.day"));
		list.add(I18N.getMsg("light.night"));
		return list;
	}

	public static List getFocaleList() {
		String foc[] = {"12", "24", "35", "50", "70", "85", "100", "135"};
		List<String> list = new ArrayList<>();
		list.addAll(Arrays.asList(foc));
		return list;
	}

	public static List getStartList() {
		String foc[] = {"", "CUT", "FADE IN"};
		List<String> list = new ArrayList<>();
		list.addAll(Arrays.asList(foc));
		return list;
	}

	public static List getEndList() {
		String foc[] = {"", "CUT", "FADE OUT", "DISSOLVE TO"};
		List<String> list = new ArrayList<>();
		list.addAll(Arrays.asList(foc));
		return list;
	}

	public static List<String> getMvtList() {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < MVT.length(); i++) {
			list.add(MVT.charAt(i) + "");
		}
		return list;
	}

	public static String getMvt(int mvt) {
		if (mvt == 0) {
			return "";
		}
		return MVT.charAt(mvt) + "";
	}

	public static String getMom(int mom) {
		if (mom == 0) {
			return "";
		}
		String str = I18N.getMsg(String.format("scenario.moment.%02d", mom));
		if (str.startsWith("!")) {
			return ("???");
		}
		return (str);
	}

	public static String getLoc(int loc) {
		switch (loc) {
			case 1:
				return I18N.getMsg("scenario.loc.int");
			case 2:
				return I18N.getMsg("scenario.loc.ext");
			default:
				return "";
		}
	}

	public static String getStage(Integer stage) {
		if (stage == null || stage == 0) {
			return ("");
		}
		return Assistant.getListOf("scene", "stage").get(stage - 1);
	}

	public enum START {
		OUTLINE,
		DRAFT,
		EDIT1,
		EDIT2,
		DONE;
	}

	public static String findScenarioKey(String type, int i) {
		//App.trace("ScenarioPanel.findKey(type=" + type + ", i=" + i + ")");
		if (i == 0) {
			return "";
		}
		String key = type + "." + String.format("%02d", i);
		try {
			String msg = I18N.getMsg(key);
			if (msg.startsWith("!")) {
				return ("");
			}
			return (msg);
		} catch (Exception ex) {
			return ("");
		}
	}

}

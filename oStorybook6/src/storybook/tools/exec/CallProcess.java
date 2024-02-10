/*
 * Copyright (C) 2022 favdb
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
package storybook.tools.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import storybook.tools.LOG;

/**
 *
 * @author favdb
 */
public class CallProcess {

	private static final String TT = "CallProcess";

	private static String python = "python3", pythonVersion = "";

	public static String getPython() {
		return python;
	}

	public static String getPythonVersion() {
		return pythonVersion;
	}

	public static boolean initPython() {
		//LOG.trace(TT + ".initPython()");
		boolean r = true;
		if (!isPythonExists()) {
			python = "python2";
			if (!isPythonExists()) {
				python = "python";
				if (!isPythonExists()) {
					python = "";
					r = false;
				}
			}
		}
		LOG.trace("detecting python=" + (r ? getPythonVersion() : "no"));
		return r;
	}

	public static boolean isPythonExists() {
		//LOG.trace(TT + ".isPythonExists()");
		if (python.isEmpty()) {
			return false;
		}
		String rc = runCommand(python, "--version");
		if (!rc.startsWith("*** ")) {
			pythonVersion = rc;
		}
		return !rc.startsWith("*** ");
	}

	public static String runCommand(String... command) {
		String str = Arrays.toString(command).replace(",", "").replace("[", "").replace("]", "");
		//LOG.trace(TT + ".runCommand(command=\"" + str + "\")");
		StringBuilder buf = new StringBuilder();
		ProcessBuilder proc = new ProcessBuilder().command(command);
		try {
			Process process = proc.start();
			InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
			try ( BufferedReader reader = new BufferedReader(inputStreamReader)) {
				String output;
				while ((output = reader.readLine()) != null) {
					buf.append(output);
				}
				process.waitFor();
			}
			process.destroy();
		} catch (IOException | InterruptedException e) {
			LOG.err(TT + ".runCommand(command=\"" + str + "\")", e);
			return "*** exception";
		}
		return buf.toString();
	}

}

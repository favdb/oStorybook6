/*
 * Copyright (C) 2025 favdb
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.tools.xml;

/**
 * Validate/fix a XML compatibility if needed, usable for HTML
 *
 * @author favdb
 */
import java.io.*;
import java.nio.charset.*;
import java.util.zip.*;

public class XmlCleaner {

	private static final String TT = "XmlCleaner.";

	/**
	 * Validate and correct a XML String
	 *
	 * @param xmlContent
	 * @return
	 */
	public static String cleanString(String xmlContent) {
		if (xmlContent == null || xmlContent.isEmpty()) {
			return xmlContent;
		}
		byte[] contentBytes = xmlContent.getBytes(StandardCharsets.UTF_8);
		return cleanAndConvert(contentBytes);
	}

	/**
	 * Validate and correct a ZIP file if needed
	 *
	 * @param path
	 * @return
	 * @throws java.lang.Exception
	 */
	public static String cleanZip(String path) throws Exception {
		try (ZipFile zipFile = new ZipFile(path)) {
			ZipEntry entry = zipFile.entries().nextElement();
			byte[] bytes;
			try (InputStream is = zipFile.getInputStream(entry); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				bytes = baos.toByteArray();
			}
			if (isValid(bytes)) {
				return path;
			}
			String cleaned = cleanAndConvert(bytes);
			String fixed = createFixedZip(path, entry.getName(), cleaned);
			return fixed;
		}
	}

	/**
	 * check if the given content is UTF8 valide
	 *
	 * @param bytes
	 * @return
	 */
	public static boolean isValid(byte[] bytes) {
		if (containsNull(bytes)) {
			return false;
		}
		return isValidUtf8(bytes);
	}

	/**
	 * Check if there is a null character in the given bytes
	 *
	 * @param bytes
	 * @return
	 */
	public static boolean containsNull(byte[] bytes) {
		for (byte b : bytes) {
			if (b == 0x00) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Cleanup and convert the given content
	 *
	 * @param bytes
	 * @return
	 */
	public static String cleanAndConvert(byte[] bytes) {
		byte[] cleanedBytes = removeNull(bytes);
		String content = convertToUtf8(cleanedBytes);
		content = content.replace("\u0000", "");
		return content;
	}

	/**
	 * Remove all null characters from the bytes
	 *
	 * @param bytes
	 * @return
	 */
	public static byte[] removeNull(byte[] bytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (byte b : bytes) {
			if (b != 0x00) {
				baos.write(b);
			}
		}
		return baos.toByteArray();
	}

	/**
	 * Check if the bytes are UTF8
	 *
	 * @param bytes
	 * @return
	 */
	public static boolean isValidUtf8(byte[] bytes) {
		try {
			CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT);

			decoder.decode(java.nio.ByteBuffer.wrap(bytes));
			return true;
		} catch (CharacterCodingException e) {
			return false;
		}
	}

	/**
	 * Convert the bytes to UTF8
	 *
	 * @param bytes
	 * @return
	 */
	public static String convertToUtf8(byte[] bytes) {
		if (isValidUtf8(bytes)) {
			return new String(bytes, StandardCharsets.UTF_8);
		}
		try {
			String latin1 = new String(bytes, StandardCharsets.ISO_8859_1);
			StringBuilder converted = new StringBuilder(latin1.length());
			for (int i = 0; i < latin1.length(); i++) {
				char c = latin1.charAt(i);
				if (c == 0x00) {
					continue;
				}
				if (c < 0x80) {
					converted.append(c);
				} else {
					converted.append(convertExtendedCharacter(c));
				}
			}

			return converted.toString();

		} catch (Exception e) {
			return fallbackConversion(bytes);
		}
	}

	/**
	 * Convert a character to UTF-8
	 */
	private static char convertExtendedCharacter(char c) {
		switch (c) {
			case 0x91:
			case 0x92:
				return '\'';
			case 0x93:
			case 0x94:
				return '"';
			// Dash
			case 0x96:
			case 0x97:
				return '-';
			// Accentuated characters
			case 0xE0:
				return 'à';
			case 0xE1:
				return 'á';
			case 0xE2:
				return 'â';
			case 0xE3:
				return 'ã';
			case 0xE4:
				return 'ä';
			case 0xE7:
				return 'ç';
			case 0xE8:
				return 'è';
			case 0xE9:
				return 'é';
			case 0xEA:
				return 'ê';
			case 0xEB:
				return 'ë';
			case 0xEE:
				return 'î';
			case 0xEF:
				return 'ï';
			case 0xF4:
				return 'ô';
			case 0xF6:
				return 'ö';
			case 0xF9:
				return 'ù';
			case 0xFB:
				return 'û';
			case 0xFC:
				return 'ü';
			// Special characters
			case 0xA3:
				return '£';
			case 0xA9:
				return '©';
			case 0xAE:
				return '®';
			case 0xB0:
				return '°';
			case 0xB5:
				return 'µ';
			default:
				return Character.isValidCodePoint(c) ? c : '?';
		}
	}

	/**
	 * Fallback conversion if other methods fail
	 */
	private static String fallbackConversion(byte[] bytes) {
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE)
				.replaceWith("?");

		try {
			return decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException ex) {
			return new String(bytes, StandardCharsets.UTF_8).replace("\u0000", "");
		}
	}

	/**
	 * Create a fixed ZIP file
	 *
	 * @param path
	 * @param entry
	 * @param content
	 * @return
	 * @throws java.lang.Exception
	 */
	public static String createFixedZip(String path, String entry, String content) throws Exception {
		String fixedPath = path + ".fixed";
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(fixedPath))) {
			ZipEntry newEntry = new ZipEntry(entry);
			zos.putNextEntry(newEntry);
			byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
			zos.write(contentBytes);
			zos.closeEntry();
		}
		return fixedPath;
	}

	/**
	 * Utility to cleanup a problematic XML
	 *
	 * @param content
	 * @return
	 */
	public static String sanitizeXml(String content) {
		if (content == null) {
			return null;
		}
		StringBuilder cleaned = new StringBuilder();
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == 0x09 || c == 0x0A || c == 0x0D
					|| (c >= 0x20 && c <= 0xD7FF)
					|| (c >= 0xE000 && c <= 0xFFFD)) {
				cleaned.append(c);
			}
		}
		return cleaned.toString();
	}

}

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
package storybook.tools.zip;

/**
 *
 * @author favdb
 */
import java.io.*;
import java.nio.charset.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import org.w3c.dom.Document;

public class XmlZipHandler {

	private static final String TT = "XmlZipHandler.";

	public Document initDomFromZip(String filePath) throws Exception {
		// Vérifie et corrige si nécessaire le fichier ZIP
		String validatedPath = validateAndFixZipIfNeeded(filePath);

		// Charge le DOM depuis le fichier validé
		try (ZipFile zipFile = new ZipFile(validatedPath)) {
			ZipEntry entry = zipFile.entries().nextElement(); // On sait qu'il n'y a qu'un fichier
			try (InputStream is = zipFile.getInputStream(entry)) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				return builder.parse(is);
			}
		}
	}

	public static String validateAndFixZipIfNeeded(String originalPath) throws Exception {
		try (ZipFile zipFile = new ZipFile(originalPath)) {
			ZipEntry entry = zipFile.entries().nextElement();

			byte[] contentBytes;
			try (InputStream is = zipFile.getInputStream(entry); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				contentBytes = baos.toByteArray();
			}

			if (isValidUtf8(contentBytes)) {
				return originalPath;
			}

			String cleanContent = convertToUtf8(contentBytes);
			String fixedPath = createFixedZipFile(originalPath, entry.getName(), cleanContent);
			return fixedPath;
		}
	}

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

	public static String convertToUtf8(byte[] bytes) {
		// Essaie d'abord de décoder en tant que ISO-8859-1
		try {
			String latin1 = new String(bytes, StandardCharsets.ISO_8859_1);
			StringBuilder converted = new StringBuilder(latin1.length());

			for (int i = 0; i < latin1.length(); i++) {
				char c = latin1.charAt(i);
				if (c < 0x80) {  // ASCII standard
					converted.append(c);
				} else {
					// Conversion spécifique pour les caractères étendus courants
					switch (c) {
						// Apostrophes et guillemets
						case 0x91:
						case 0x92:
							converted.append('\'');
							break;
						case 0x93:
						case 0x94:
							converted.append('"');
							break;

						// Tirets
						case 0x96:
						case 0x97:
							converted.append('-');
							break;

						// Caractères accentués courants
						case 0xE0:
							converted.append('à');
							break; // à
						case 0xE1:
							converted.append('á');
							break; // á
						case 0xE2:
							converted.append('â');
							break; // â
						case 0xE3:
							converted.append('ã');
							break; // ã
						case 0xE4:
							converted.append('ä');
							break; // ä
						case 0xE7:
							converted.append('ç');
							break; // ç
						case 0xE8:
							converted.append('è');
							break; // è
						case 0xE9:
							converted.append('é');
							break; // é
						case 0xEA:
							converted.append('ê');
							break; // ê
						case 0xEB:
							converted.append('ë');
							break; // ë
						case 0xEE:
							converted.append('î');
							break; // î
						case 0xEF:
							converted.append('ï');
							break; // ï
						case 0xF4:
							converted.append('ô');
							break; // ô
						case 0xF6:
							converted.append('ö');
							break; // ö
						case 0xF9:
							converted.append('ù');
							break; // ù
						case 0xFB:
							converted.append('û');
							break; // û
						case 0xFC:
							converted.append('ü');
							break; // ü

						// Caractères spéciaux courants
						case 0xA3:
							converted.append('£');
							break; // Livre sterling
						case 0xA9:
							converted.append('©');
							break; // Copyright
						case 0xAE:
							converted.append('®');
							break; // Registered
						case 0xB0:
							converted.append('°');
							break; // Degré
						case 0xB5:
							converted.append('µ');
							break; // Micro

						default:
							// Si on ne reconnaît pas le caractère, on essaie de le convertir directement
							if (isValidUtf8Character((byte) c)) {
								converted.append(c);
							} else {
								// En dernier recours, on met un point d'interrogation
								converted.append('?');
							}
					}
				}
			}
			return converted.toString();

		} catch (Exception e) {
			// Si tout échoue, on retourne une conversion basique avec remplacement
			CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPLACE)
					.onUnmappableCharacter(CodingErrorAction.REPLACE)
					.replaceWith("?");

			try {
				return decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
			} catch (CharacterCodingException ex) {
				return new String(bytes, StandardCharsets.UTF_8);
			}
		}
	}

	public static boolean isValidUtf8Character(byte b) {
		try {
			CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT);

			decoder.decode(java.nio.ByteBuffer.wrap(new byte[]{b}));
			return true;
		} catch (CharacterCodingException e) {
			return false;
		}
	}

	public static String createFixedZipFile(String originalPath, String entryName, String content) throws Exception {
		String fixedPath = originalPath + ".fixed";
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(fixedPath))) {
			ZipEntry newEntry = new ZipEntry(entryName);
			zos.putNextEntry(newEntry);
			byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
			zos.write(contentBytes);
			zos.closeEntry();
		}
		return fixedPath;
	}

}

package at.metalab.m68k.giebelkreuz_grinder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class GkgApp {
	public static void main(String[] args) throws Exception {
		String workDir = System.getProperty("grinder.workdir",
				"/home/m68k/Downloads/raiffeisen_grinder/work/");

		List<String> lines = IOUtils.readLines(new FileInputStream(new File(
				workDir + "/_compacted_complete.txt")));

		List<Einzug> einzuege = new ArrayList<Einzug>();

		{
			Einzug einzug = null;
			Einzugszeile einzugszeile = null;

			int rowNum = 0;
			for (String raw : lines) {
				rowNum++;
				if (raw.contains("/DDT/U/")) {
					einzugszeile = null;

					String[] chewed = chew(raw, " /");

					einzug = new Einzug();
					einzuege.add(einzug);

					einzug.setDatum(chewed[0]);
					einzug.setEinzugsId(chewed[1]);
				} else if (raw.startsWith("  ANZUMS")) {
					String[] chewed = chew(raw, " ");

					einzug.setErwartetAnzahl(Integer.parseInt(chewed[1]));
				} else if (raw.startsWith("0") || raw.startsWith(" 0")) {
					einzugszeile = new Einzugszeile();
					einzug.add(einzugszeile);

					String[] chewed = chew(raw, " ");

					einzugszeile.setNummer(Integer.parseInt(chewed[0]
							.replaceFirst("^0+(?!$)", "")));
					for (int i = 0; i < chewed.length; i++) {
						if ("EUR".equals(chewed[i])) {
							einzugszeile.setBetrag(new BigDecimal(chewed[i + 1]
									.replace(',', '.').replace("-", "")));
						}
					}
				} else if (raw.trim().startsWith("E-BVB")) {
					String[] chewed = chew(raw, " ");
					einzugszeile.setIban(chewed[1]);
				} else if (raw.trim().startsWith("EMPBIC")) {
					String[] chewed = chew(raw, " ");
					einzugszeile.setBic(chewed[1]);
				} else if (StringUtils.normalizeSpace(raw.trim()).startsWith(
						"MANDATSDATEN MNDREF")) {
					String[] chewed = chew(raw, " ");
					einzugszeile.setMandatsreferenz(chewed[2]);
				} else if (raw.trim().startsWith("PMD")) {
					String[] chewed = chew(raw, " ");
					chewed[0] = null;
					chewed[1] = null;
					chewed[chewed.length - 1] = null;

					String pmd = StringUtils.join(chewed, " ").trim(); // best
																		// effort

					einzugszeile.setVerwendungszweck(pmd);
				} else if (raw.endsWith(" K N ")) {
					einzugszeile.setName(raw.trim().substring(0, 50).trim());
				}
			}

			System.out.println("processed " + rowNum + " rows");
		}

		BufferedWriter b = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(workDir + "/_mos.txt")));

		BufferedWriter csv = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(workDir + "/_mos.csv")));

		for (Einzug e : einzuege) {
			List<String> errors2 = new LinkedList<String>();

			if (e.getErwartetAnzahl() != e.getZeilen().size()) {
				errors2.add(String.format(" ROWS(%d <> %d)",
						e.getErwartetAnzahl(), e.getZeilen().size()));

				Map<Integer, Einzugszeile> lookupById = new HashMap<Integer, Einzugszeile>();
				for (Einzugszeile z : e.getZeilen()) {
					lookupById.put(z.getNummer(), z);
				}
				for (int i = 1; i < e.getErwartetAnzahl(); i++) {
					if (!lookupById.containsKey(i)) {
						errors2.add(" MISSING #" + i);
					}
				}
			}

			if (errors2.size() > 0) {
				b.write("* ");
			} else {
				b.write("  ");
			}

			b.write(asString(e));

			csv.write("\n# " + asCSV(e) + "\n");

			if (errors2.size() > 0) {
				// 3110/DDT/U/
				String msg = String.format("ERROR @ %d/DDT/U/ %s",
						Integer.parseInt(e.getEinzugsId()),
						Arrays.toString(errors2.toArray()));
				System.out.println("DDT: " + msg);
				b.write(" " + msg);
			}

			b.write("\n");
			b.write("\n");

			for (Einzugszeile z : e.getZeilen()) {
				List<String> errors = new LinkedList<String>();
				if (StringUtils.isBlank(z.getIban())) {
					errors.add("IBAN");
				}
				if (StringUtils.isBlank(z.getBic())) {
					errors.add("BIC");
				}
				if (StringUtils.isBlank(z.getName())) {
					errors.add("NAME");
				}
				if (StringUtils.isBlank(z.getMandatsreferenz())) {
					errors.add("MNDREF");
				}
				if (StringUtils.isBlank(z.getVerwendungszweck())) {
					errors.add("PMD");
				}
				if (z.getBetrag() == null) {
					errors.add("BETRAG");
				}

				if (errors.size() > 0) {
					b.write("* ");
				} else {
					b.write("  ");
				}

				b.write(asString(z, e));
				csv.write(asCSV(z, e) + "\n");

				if (errors.size() > 0) {
					String msg = "ERROR @ " + z.createHint(e) + " "
							+ Arrays.toString(errors.toArray());
					System.out.println("EIN: " + msg);
					b.write(" " + msg);
				}

				b.write("\n");
			}

			b.write("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n\n");
		}

		b.flush();
		b.close();

		csv.flush();
		csv.close();
	}

	private static String asCSV(Einzug einzug) throws ParseException {
		SimpleDateFormat i = new SimpleDateFormat("ddMMyyyy"); // 18062015
		Date d = i.parse(einzug.getDatum());
		String strDate = new SimpleDateFormat("yyyy-MM-dd").format(d); // 2014-04-03

		return strDate + "(" + einzug.getEinzugsId() + ")";
	}

	private static String asCSV(Einzugszeile einzugszeile, Einzug einzug)
			throws ParseException {
		// > Vorname;Nachname;KONTO;BLZ;Vorname
		// Nachname;BETRAG;VERWENDUNGSZWECK;IBAN;BIC;MNDREF;2014-04-03;RCUR;AT29HXR00000037632

		SimpleDateFormat i = new SimpleDateFormat("ddMMyyyy"); // 18062015
		Date d = i.parse(einzug.getDatum());
		String strDate = new SimpleDateFormat("yyyy-MM-dd").format(d); // 2014-04-03

		long konto = getKonto(einzugszeile.getIban());
		long blz = getBlz(einzugszeile.getIban());

		String vorname = getVorname(einzugszeile.getName());
		String nachname = getNachname(einzugszeile.getName());

		StringBuilder comment = new StringBuilder();
		if (StringUtils.isBlank(vorname)) {
			comment.append("VN ");
			vorname = "# VN:" + einzugszeile.getName().hashCode() + " #";
		}
		if (StringUtils.isBlank(nachname)) {
			comment.append("NN ");
			nachname = "# NN:" + einzugszeile.getName().hashCode() + " #";
		}
		if (StringUtils.isBlank(einzugszeile.getMandatsreferenz())) {
			comment.append("MNDREF ");
		}

		return String.format("%s;%s;%s;%d;%d;%s;%d;%s;%s;%s;%s;%s;_t_;_c_",
				comment.toString().trim(), vorname, nachname, konto, blz,
				einzugszeile.getName(), einzugszeile.getBetrag()
						.intValueExact(), einzugszeile.getVerwendungszweck(),
				einzugszeile.getIban(), einzugszeile.getBic(), StringUtils
						.defaultString(einzugszeile.getMandatsreferenz(),
								"# MNDREF:"
										+ einzugszeile.getName().hashCode()
										+ " #"), strDate);
	}

	private static String getVorname(String name) {
		String[] chewed = chew(name, " ");
		if (chewed.length != 2) {
			return null;
		} else {
			return chewed[0];
		}
	}

	private static String getNachname(String name) {
		String[] chewed = chew(name, " ");
		if (chewed.length != 2) {
			return null;
		} else {
			return chewed[1];
		}
	}

	private static String asString(Einzugszeile einzugszeile, Einzug einzug) {
		return String
				.format("EINZUG id=%s nummer=%d bic='%s' iban='%s' betrag='%s' mandatsreferez='%s' name='%s' pmd='%s'",
						einzug.getEinzugsId(), einzugszeile.getNummer(),
						einzugszeile.getBic(), einzugszeile.getIban(),
						einzugszeile.getBetrag().toPlainString(),
						einzugszeile.getMandatsreferenz(),
						einzugszeile.getName(),
						einzugszeile.getVerwendungszweck());
	}

	private static String asString(Einzug einzug) {
		return String.format("DDT id=%s datum='%s' rowsExpected=%d",
				einzug.getEinzugsId(), einzug.getDatum(),
				einzug.getErwartetAnzahl());
	}

	private static String[] chew(String raw, String seperators) {
		String cooked = StringUtils.trim(raw);
		cooked = StringUtils.normalizeSpace(cooked);
		// System.out.println("cooked: " + cooked);

		String[] chewed = StringUtils.split(cooked, seperators);
		return chewed;
	}

	// https://www.oenb.at/Zahlungsverkehr/SEPA/IBAN-und-BIC/IBAN.html
	// http://www.iban.de/berechnung_iban.html

	private static long getKonto(String iban) {
		if (!iban.startsWith("AT") && !iban.startsWith("DE")) {
			throw new IllegalArgumentException("nur at oder de möglich: "
					+ iban.substring(0, 2));
		}

		if (iban.startsWith("AT")) {
			return Long.parseLong(StringUtils.stripStart(iban.substring(9, 20),
					"0"));
		} else {
			return Long.parseLong(StringUtils.stripStart(
					iban.substring(12, 22), "0"));
		}
	}

	private static long getBlz(String iban) {
		if (!iban.startsWith("AT") && !iban.startsWith("DE")) {
			throw new IllegalArgumentException("nur at oder de möglich: "
					+ iban.substring(0, 2));
		}
		if (iban.startsWith("AT")) {
			return Long.parseLong(StringUtils.stripStart(iban.substring(4, 9),
					"0"));
		} else {
			return Long.parseLong(StringUtils.stripStart(iban.substring(4, 12),
					"0"));
		}
	}
}

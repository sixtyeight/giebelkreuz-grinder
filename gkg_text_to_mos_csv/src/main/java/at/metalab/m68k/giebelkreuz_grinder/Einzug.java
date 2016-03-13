package at.metalab.m68k.giebelkreuz_grinder;

import java.util.ArrayList;
import java.util.List;

public class Einzug {

	private Integer erwartetAnzahl;

	private String einzugsId;

	private String datum;

	private List<Einzugszeile> zeilen = new ArrayList<Einzugszeile>();

	public void setDatum(String datum) {
		Utils.assertNull(this.datum);
		this.datum = datum;
	}

	public String getDatum() {
		return datum;
	}

	public void setEinzugsId(String einzugsId) {
		Utils.assertNull(this.einzugsId);
		this.einzugsId = einzugsId;
	}

	public String getEinzugsId() {
		return einzugsId;
	}

	public void setErwartetAnzahl(int erwartetAnzahl) {
		Utils.assertNull(this.erwartetAnzahl);
		this.erwartetAnzahl = erwartetAnzahl;
	}

	public Integer getErwartetAnzahl() {
		return erwartetAnzahl;
	}

	public void add(Einzugszeile einzugszeile) {
		zeilen.add(einzugszeile);
	}

	public List<Einzugszeile> getZeilen() {
		return zeilen;
	}
}

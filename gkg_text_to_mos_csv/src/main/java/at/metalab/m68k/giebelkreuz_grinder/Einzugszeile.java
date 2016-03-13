package at.metalab.m68k.giebelkreuz_grinder;

import java.math.BigDecimal;

public class Einzugszeile {

	private Integer nummer;

	private String iban;
	
	private String bic;
	
	private String name;

	private BigDecimal betrag;

	private String mandatsreferenz;

	private String verwendungszweck;

	public void setBic(String bic) {
		Utils.assertNull(this.bic);
		this.bic = bic;
	}
	
	public void setName(String name) {
		Utils.assertNull(this.name);
		this.name = name;
	}
	
	public String getBic() {
		return bic;
	}
	
	public String getName() {
		return name;
	}
	
	public String getVerwendungszweck() {
		return verwendungszweck;
	}

	public void setVerwendungszweck(String verwendungszweck) {
		Utils.assertNull(this.verwendungszweck);
		this.verwendungszweck = verwendungszweck;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(String iban) {
		Utils.assertNull(this.iban);
		this.iban = iban;
	}

	public void setNummer(int nummer) {
		Utils.assertNull(this.nummer);
		this.nummer = nummer;
	}

	public int getNummer() {
		return nummer;
	}

	public String getMandatsreferenz() {
		return mandatsreferenz;
	}

	public void setMandatsreferenz(String mandatsreferenz) {
		Utils.assertNull(this.mandatsreferenz);
		this.mandatsreferenz = mandatsreferenz;
	}

	public void setBetrag(BigDecimal betrag) {
		Utils.assertNull(this.betrag);
		this.betrag = betrag;
	}

	public BigDecimal getBetrag() {
		return betrag;
	}
	
	public String createHint(Einzug einzug) {
		// D00031110000001
		return String.format("D%07d%07d (IBAN=%s)",
				Integer.parseInt(einzug.getEinzugsId()), this.getNummer(),
				getIban());
	}

}

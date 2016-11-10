package net.vexelon.currencybg.app.db.models;

import java.util.Date;

public class CurrencyData {

	private String code;
	private int ratio = 1; // default
	private String buy;
	private String sell;
	private Date date;
	private int source;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	public String getBuy() {
		return buy;
	}

	public void setBuy(String buy) {
		this.buy = buy;
	}

	public String getSell() {
		return sell;
	}

	public void setSell(String sell) {
		this.sell = sell;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return "CurrencyData{" + "code='" + code + '\'' + ", ratio=" + ratio + ", buy='" + buy + '\'' + ", sell='"
				+ sell + '\'' + ", date=" + date + ", source=" + source + '}';
	}

}

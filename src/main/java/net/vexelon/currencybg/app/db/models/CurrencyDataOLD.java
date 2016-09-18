package net.vexelon.currencybg.app.db.models;

import java.util.Date;

public class CurrencyDataOLD {
	private int gold;
	private String name;
	private String code;
	private int ratio;
	private String reverseRate;
	private String rate;
	private String extraInfo;
	private Date currDate;
	private String title;
	private int fStar;
	private boolean isFixed;

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

	public String getReverseRate() {
		return reverseRate;
	}

	public void setReverseRate(String reverseRate) {
		this.reverseRate = reverseRate;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	public Date getCurrDate() {
		return currDate;
	}

	public void setCurrDate(Date currDate) {
		this.currDate = currDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getfStar() {
		return fStar;
	}

	public void setfStar(int fStar) {
		this.fStar = fStar;
	}

	public boolean isFixed() {
		return isFixed;
	}

	public void setIsFixed(boolean isFixed) {
		this.isFixed = isFixed;
	}

	@Override
	public String toString() {
		return "CurrencyData{" +
				"gold=" + gold +
				", name='" + name + '\'' +
				", code='" + code + '\'' +
				", ratio=" + ratio +
				", reverseRate='" + reverseRate + '\'' +
				", rate='" + rate + '\'' +
				", extraInfo='" + extraInfo + '\'' +
				", currDate=" + currDate +
				", title='" + title + '\'' +
				", fStar=" + fStar +
				", isFixed=" + isFixed +
				'}';
	}
}

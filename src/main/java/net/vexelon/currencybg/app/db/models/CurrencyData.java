package net.vexelon.currencybg.app.db.models;

import java.util.Date;

public class CurrencyData {

//	private String code;
//	private int ratio = 0; // default
//	private String buy = "0"; // default
//	private String sell = "0"; // default
//	private Date date;
//	private int source;

//	<GOLD>1</GOLD>
//	<NAME_>Бразилски реал</NAME_>
//	<CODE>BRL</CODE>
//	<RATIO>10</RATIO>
//	<REVERSERATE>1.83779</REVERSERATE>
//	<RATE>5.44133</RATE>
//	<CURR_DATE>07.09.2016</CURR_DATE>
//	<F_STAR>0</F_STAR>




	//	private int gold;
//	private String name;
	private String code;
	private int ratio;
	private String buy;
	private String sell;
	//	private String extraInfo;
	private Date date;
	private int source;
//	private String title;
//	private int fStar;
//	private boolean isFixed;

//	public int getGold() {
//		return gold;
//	}
//
//	public void setGold(int gold) {
//		this.gold = gold;
//	}

//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}

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

	//	public String getReverseRate() {
//		return reverseRate;
//	}
//
//	public void setReverseRate(String reverseRate) {
//		this.reverseRate = reverseRate;
//	}
//
//	public String getRate() {
//		return rate;
//	}
//
//	public void setRate(String rate) {
//		this.rate = rate;
//	}

//	public String getExtraInfo() {
//		return extraInfo;
//	}
//
//	public void setExtraInfo(String extraInfo) {
//		this.extraInfo = extraInfo;
//	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}


//	public Date getCurrDate() {
//		return currDate;
//	}
//
//	public void setCurrDate(Date currDate) {
//		this.currDate = currDate;
//	}

//	public String getTitle() {
//		return title;
//	}

//	public void setTitle(String title) {
//		this.title = title;
//	}

//	public int getfStar() {
//		return fStar;
//	}

//	public void setfStar(int fStar) {
//		this.fStar = fStar;
//	}

//	public boolean isFixed() {
//		return isFixed;
//	}

//	public void setIsFixed(boolean isFixed) {
//		this.isFixed = isFixed;
//	}


	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return "CurrencyData{" +
				"code='" + code + '\'' +
				", ratio=" + ratio +
				", buy='" + buy + '\'' +
				", sell='" + sell + '\'' +
				", date=" + date +
				", source=" + source +
				'}';
	}

	//	@Override
//	public String toString() {
//		return "CurrencyData{" +
////				"gold=" + gold +
////				" name='" + name + '\'' +
//				" code='" + code + '\'' +
//				", ratio=" + ratio +
//				", buy='" + buy + '\'' +
//				", sell='" + sell + '\'' +
//				", date=" +  date+
////				", reverseRate='" + reverseRate + '\'' +
////				", rate='" + rate + '\'' +
////				", extraInfo='" + extraInfo + '\'' +
////				", currDate=" + currDate +
////				", title='" + title + '\'' +
////				", fStar=" + fStar +
////				", isFixed=" + isFixed +
//				'}';
//	}
}

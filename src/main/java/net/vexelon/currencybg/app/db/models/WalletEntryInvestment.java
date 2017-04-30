package net.vexelon.currencybg.app.db.models;

import java.math.BigDecimal;

/**
 * Describes investment info for a single wallet entry and a currency source
 * 
 */
public class WalletEntryInvestment {

	private WalletEntry walletEntry;
	private CurrencyData currencyData;

	private BigDecimal initialValue;
	private BigDecimal currentValue;
	private BigDecimal investmentMargin;
	private BigDecimal investmentMarginPercentage;
	private int investmentDuration; // in days
	private BigDecimal dailyChange;
	private BigDecimal dailyChangePercentage;

	public WalletEntryInvestment(WalletEntry entry, CurrencyData currencyData) {
		this.walletEntry = entry;
		this.currencyData = currencyData;
	}

	public BigDecimal getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(BigDecimal initialValue) {
		this.initialValue = initialValue;
	}

	public BigDecimal getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(BigDecimal currentValue) {
		this.currentValue = currentValue;
	}

	public WalletEntry getWalletEntry() {
		return walletEntry;
	}

	public void setWalletEntry(WalletEntry walletEntry) {
		this.walletEntry = walletEntry;
	}

	public CurrencyData getCurrencyData() {
		return currencyData;
	}

	public void setCurrencyData(CurrencyData currencyData) {
		this.currencyData = currencyData;
	}

	public BigDecimal getInvestmentMargin() {
		return investmentMargin;
	}

	public void setInvestmentMargin(BigDecimal investmentMargin) {
		this.investmentMargin = investmentMargin;
	}

	public BigDecimal getInvestmentMarginPercentage() {
		return investmentMarginPercentage;
	}

	public void setInvestmentMarginPercentage(BigDecimal investmentMarginPercentage) {
		this.investmentMarginPercentage = investmentMarginPercentage;
	}

	public int getInvestmentDuration() {
		return investmentDuration;
	}

	public void setInvestmentDuration(int investmentDuration) {
		this.investmentDuration = investmentDuration;
	}

	public BigDecimal getDailyChange() {
		return dailyChange;
	}

	public void setDailyChange(BigDecimal dailyChange) {
		this.dailyChange = dailyChange;
	}

	public BigDecimal getDailyChangePercentage() {
		return dailyChangePercentage;
	}

	public void setDailyChangePercentage(BigDecimal dailyChangePercentage) {
		this.dailyChangePercentage = dailyChangePercentage;
	}
}

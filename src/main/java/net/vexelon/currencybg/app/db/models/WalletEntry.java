/*
 * CurrencyBG App
 * Copyright (C) 2016 Vexelon.NET Services
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
package net.vexelon.currencybg.app.db.models;

import java.util.Date;

public class WalletEntry {

	private int id;
	private String code;
	private String amount;
	private String purchaseRate;
	private Date purchaseTime;

	public int getId() {	return id;	}

	public void setId(int id) {	this.id = id;	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getPurchaseRate() {
		return purchaseRate;
	}

	public void setPurchaseRate(String purchaseRate) {
		this.purchaseRate = purchaseRate;
	}

	public Date getPurchaseTime() {
		return purchaseTime;
	}

	public void setPurchaseTime(Date purchaseTime) {
		this.purchaseTime = purchaseTime;
	}

	@Override
	public String toString() {
		return "WalletEntry{" + "id=" + id + ", code='" + code + '\'' + ", amount='" + amount + '\''
				+ ", purchaseRate='" + purchaseRate + '\'' + ", purchaseTime=" + purchaseTime + '}';
	}
}

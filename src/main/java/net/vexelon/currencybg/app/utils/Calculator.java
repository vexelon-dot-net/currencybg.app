package net.vexelon.currencybg.app.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Created by TSVETOSLAV on 9.11.2016 г..
 */

public class Calculator {

	private BigDecimal result;
	private int scale;

	public Calculator(int scale) {
		this(new BigDecimal(0), scale);
	}

	public Calculator(BigDecimal a, int scale) {
		this.result = a;
		this.scale = scale;
	}

	public Calculator add(BigDecimal b) {
		this.result = this.result.add(b);
		return this;
	}

	public Calculator sub(BigDecimal b) {
		this.result = this.result.subtract(b);
		return this;
	}

	public Calculator div(BigDecimal b) {
		this.result = this.result.divide(b, this.scale, RoundingMode.HALF_UP);
		return this;
	}

	public Calculator mul(BigDecimal b) {
		this.result = this.result.multiply(b);
		return this;
	}

	public String getNormalizedResult() {
		return this.result.setScale(this.scale, RoundingMode.HALF_EVEN).toPlainString();
	}

	public Calculator clear() {
		result = new BigDecimal(0);
		return this;
	}

	public BigDecimal getResult() {
		return this.result;
	}

}

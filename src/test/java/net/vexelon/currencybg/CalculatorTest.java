package net.vexelon.currencybg;

import net.vexelon.currencybg.app.utils.Calculator;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by TSVETOSLAV on 10.11.2016 г..
 */

public class CalculatorTest {
	@Test
	public void testCase1() {
		Calculator calculator = new Calculator(new BigDecimal(0.4850),2);

		Assert.assertTrue(calculator.add(new BigDecimal(1.3480)).add(new BigDecimal(0.9700)).getNormalizedResult().equals("2.80"));
		System.out.print(calculator.getNormalizedResult());
	}

	@Test
	public void testCase2(){
		Calculator calculator = new Calculator(new BigDecimal(0.4850),2);

		calculator.add(new BigDecimal(1.3480));

		System.out.println(calculator.getNormalizedResult());
	}
}

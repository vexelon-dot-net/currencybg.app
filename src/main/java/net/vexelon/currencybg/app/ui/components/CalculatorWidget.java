package net.vexelon.currencybg.app.ui.components;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.ui.fragments.AbstractFragment;
import net.vexelon.currencybg.app.utils.Calculator;
import net.vexelon.currencybg.app.utils.StringUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Encapsulates the behavior of the UI calculator
 * 
 */
public class CalculatorWidget implements View.OnClickListener {

	private static final int BASE_INDEX = 0x80;
	private static final int MAX_DISPLAY_LENGTH = 20;

	private static final int OP_NOOP = -1;
	private static final int OP_0 = 0;
	private static final int OP_1 = 1;
	private static final int OP_2 = 2;
	private static final int OP_3 = 3;
	private static final int OP_4 = 4;
	private static final int OP_5 = 5;
	private static final int OP_6 = 6;
	private static final int OP_7 = 7;
	private static final int OP_8 = 8;
	private static final int OP_9 = 9;
	private static final int OP_CLEAR = 10;
	private static final int OP_DIV = 11;
	private static final int OP_MUL = 12;
	private static final int OP_MINUS = 13;
	private static final int OP_PLUS = 14;
	private static final int OP_EQUAL = 15;
	private static final int OP_DECIMAL = 16;
	private static final int OP_PERCENT = 17;

	private static final int OP_MAX_OPS = OP_PERCENT + 1;

	private static final String OP_SYMBOLS[] = { "÷", "×", "-", "+" };

	private Context context;
	private TextView display;
	private Button[] opButtons = new Button[OP_MAX_OPS];
	private Button opIndicatorButton;
	private String lastDisplay = "";
	private int lastOp = OP_NOOP;
	private boolean clearDisplay = false;

	public CalculatorWidget(Context context) {
		this.context = context;
	}

	private void init(View view) {
		display = (TextView) view.findViewById(R.id.calc_display);
		display.setText("0"); // TODO initial
		lastDisplay = "";

		opIndicatorButton = (Button) view.findViewById(R.id.calc_button_op_display);

		bindButton(view, OP_0, R.id.calc_button0);
		bindButton(view, OP_1, R.id.calc_button1);
		bindButton(view, OP_2, R.id.calc_button2);
		bindButton(view, OP_3, R.id.calc_button3);
		bindButton(view, OP_4, R.id.calc_button4);
		bindButton(view, OP_5, R.id.calc_button5);
		bindButton(view, OP_6, R.id.calc_button6);
		bindButton(view, OP_7, R.id.calc_button7);
		bindButton(view, OP_8, R.id.calc_button8);
		bindButton(view, OP_9, R.id.calc_button9);
		bindButton(view, OP_CLEAR, R.id.calc_button_clear);
		bindButton(view, OP_DIV, R.id.calc_button_div);
		bindButton(view, OP_MUL, R.id.calc_button_mul);
		bindButton(view, OP_MINUS, R.id.calc_button_minus);
		bindButton(view, OP_PLUS, R.id.calc_button_plus);
		bindButton(view, OP_EQUAL, R.id.calc_button_eq);
		bindButton(view, OP_DECIMAL, R.id.calc_button_dec);
		bindButton(view, OP_PERCENT, R.id.calc_button_percent);
	}

	private void bindButton(View view, int id, int resid) {
		Button button = (Button) view.findViewById(resid);
		button.setId(BASE_INDEX + id);
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		String number = "";

		int opId = v.getId() - BASE_INDEX;
		switch (opId) {
		case OP_0:
			number = "0";
			break;
		case OP_1:
			number = "1";
			break;
		case OP_2:
			number = "2";
			break;
		case OP_3:
			number = "3";
			break;
		case OP_4:
			number = "4";
			break;
		case OP_5:
			number = "5";
			break;
		case OP_6:
			number = "6";
			break;
		case OP_7:
			number = "7";
			break;
		case OP_8:
			number = "8";
			break;
		case OP_9:
			number = "9";
			break;
		case OP_CLEAR:
			lastOp = OP_NOOP;
			display.setText("0");
			break;
		case OP_DIV:
		case OP_MUL:
		case OP_MINUS:
		case OP_PLUS:
			if (lastOp != OP_NOOP) {
				doCalc();
			}
			opIndicatorButton.setText(OP_SYMBOLS[opId - OP_DIV]);
			lastOp = opId;
			lastDisplay = display.getText().toString();
			clearDisplay = true;
			return;
		case OP_EQUAL:
			if (lastOp != OP_NOOP) {
				doCalc();
			}
			return;
		case OP_DECIMAL:
			if (display.getText().toString().indexOf(".") == -1) {
				display.setText(display.getText() + ".");
			}
			return;
		case OP_PERCENT:
			display.setText(new Calculator(new BigDecimal(display.getText().toString()), Defs.SCALE_SHOW_SHORT)
					.div(new BigDecimal(100)).getNormalizedResult());
			return;
		}

		if ("E".equals(display.getText())) {
			display.setText("0");
		}

		if ("0".equals(number) && "0".equals(display.getText())) {
			return;
		}

		if (!number.isEmpty()) {
			if (clearDisplay) {
				clearDisplay = false;
				display.setText("0");
			}

			if ("0".equals(display.getText())) {
				display.setText(number);
			} else if (display.getText().length() < MAX_DISPLAY_LENGTH) {
				display.setText(display.getText() + number);
			}
		}

	}

	private void doCalc() {
		try {
			Calculator calculator = new Calculator(new BigDecimal(lastDisplay), Defs.SCALE_SHOW_SHORT);
			BigDecimal value = new BigDecimal(display.getText().toString());

			switch (lastOp) {
			case OP_DIV:
				calculator.div(value);
				break;
			case OP_MUL:
				calculator.mul(value);
				break;
			case OP_MINUS:
				calculator.sub(value);
				break;
			case OP_PLUS:
				calculator.add(value);
				break;
			}

			display.setText(calculator.getNormalizedResult());
		} catch (NumberFormatException e) {
			Log.w(Defs.LOG_TAG, "Invalid calculator display value!", e);
			display.setText("E");
		} catch (ArithmeticException e) {
			Log.w(Defs.LOG_TAG, "Calculator error!", e);
			display.setText("E");
		}

		opIndicatorButton.setText("");
		lastOp = OP_NOOP;
	}

	public void showCalculator(final String value, final Listener listener) {
		final AppSettings appSettings = new AppSettings(context);

		final MaterialDialog dialog = new MaterialDialog.Builder(context).customView(R.layout.calculator_layout, false)
				.positiveText(R.string.text_ok).negativeText(R.string.text_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						// notify
						if (display.getText().length() > 0 && !"E".equals(display.getText())) {
							listener.onValue(new BigDecimal(display.getText().toString()));
						}
					}
				}).build();

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dlg) {
				// bind calculator events
				CalculatorWidget.this.init(dialog.getCustomView());
				if (!StringUtils.isEmpty(value)) {
					display.setText(value);
				}
			}
		});

		dialog.show();
	}

	public void showCalculator(final Listener listener) {
		showCalculator(null, listener);
	}

	/**
	 * Events listener
	 * 
	 */
	public static interface Listener {

		void onValue(BigDecimal value);

	}
}

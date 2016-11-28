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

import java.math.BigDecimal;

/**
 * Encapsulates the behavior of the UI calculator
 * 
 */
public class CalculatorWidget implements View.OnClickListener {

	private static final int OP_CLEAR = 0;
	private static final int OP_DIV = 1;
	private static final int OP_MUL = 2;
	private static final int OP_MINUS = 3;
	private static final int OP_PLUS = 4;
	private static final int OP_EQUAL = 5;
	private static final int OP_DECIMAL = 6;
	private static final int OP_PERCENT = 7;
	private static final int OP_MAX_OPS = OP_PERCENT + 1;

	private Context context;

	private TextView display;
	private Button[] numberButtons = new Button[10];
	private Button[] opButtons = new Button[OP_MAX_OPS];

	public CalculatorWidget(Context context) {
		this.context = context;
	}

	private void init(View rootView) {
		display = (TextView) rootView.findViewById(R.id.calc_display);

		numberButtons[0] = (Button) rootView.findViewById(R.id.calc_button0);
		numberButtons[1] = (Button) rootView.findViewById(R.id.calc_button1);
		numberButtons[2] = (Button) rootView.findViewById(R.id.calc_button2);
		numberButtons[3] = (Button) rootView.findViewById(R.id.calc_button3);
		numberButtons[4] = (Button) rootView.findViewById(R.id.calc_button4);
		numberButtons[5] = (Button) rootView.findViewById(R.id.calc_button5);
		numberButtons[6] = (Button) rootView.findViewById(R.id.calc_button6);
		numberButtons[7] = (Button) rootView.findViewById(R.id.calc_button7);
		numberButtons[8] = (Button) rootView.findViewById(R.id.calc_button8);
		numberButtons[9] = (Button) rootView.findViewById(R.id.calc_button9);

		opButtons[OP_CLEAR] = (Button) rootView.findViewById(R.id.calc_button_clear);
		opButtons[OP_DIV] = (Button) rootView.findViewById(R.id.calc_button_div);
		opButtons[OP_MUL] = (Button) rootView.findViewById(R.id.calc_button_mul);
		opButtons[OP_MINUS] = (Button) rootView.findViewById(R.id.calc_button_minus);
		opButtons[OP_PLUS] = (Button) rootView.findViewById(R.id.calc_button_plus);
		opButtons[OP_EQUAL] = (Button) rootView.findViewById(R.id.calc_button_eq);
		opButtons[OP_DECIMAL] = (Button) rootView.findViewById(R.id.calc_button_dec);
		opButtons[OP_PERCENT] = (Button) rootView.findViewById(R.id.calc_button_percent);

		// TODO
	}

	@Override
	public void onClick(View v) {
		// TODO
		switch (v.getId()) {
		case OP_CLEAR:
			break;
		}
	}

	public void showCalculator(final Listener listener) {
		final AppSettings appSettings = new AppSettings(context);

		final MaterialDialog dialog = new MaterialDialog.Builder(context).customView(R.layout.calculator_layout, false)
				.positiveText(R.string.text_ok).negativeText(R.string.text_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						Log.d(Defs.LOG_TAG, "CALC: " + display.getText());

						// notify
						listener.onValue(new BigDecimal(display.getText().toString()));
					}
				}).build();

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dlg) {
				// bind calculator events
				CalculatorWidget.this.init(dialog.getCustomView());
			}
		});

		dialog.show();
	}

	/**
	 * Events listener
	 * 
	 */
	public static interface Listener {

		void onValue(BigDecimal value);

	}
}

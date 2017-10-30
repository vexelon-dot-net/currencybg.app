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
package net.vexelon.currencybg.app.ui.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.KeyEvent;
import android.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public final class UIUtils {

	/**
	 * Display an alert dialog using resource IDs
	 * 
	 * @param activity
	 * @param messageResId
	 * @param titleResId
	 */
	public static void showAlertDialog(Activity activity, int messageResId, int titleResId) {
		showAlertDialog(activity, activity.getResources().getString(messageResId),
				activity.getResources().getString(titleResId));
	}

	/**
	 * Display alert dialog using string message
	 * 
	 * @param activity
	 * @param message
	 * @param title
	 */
	public static void showAlertDialog(Activity activity, String message, String title) {
		final Activity act = activity;
		final String s1 = message, s2 = title;
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog alert = createAlertDialog(act, s1, s2);
				alert.show();
			}
		});
	}

	/**
	 * Create alert dialog using resource IDs
	 * 
	 * @param context
	 * @param messageResId
	 * @param titleResId
	 * @return
	 */
	public static AlertDialog createAlertDialog(Context context, int messageResId, int titleResId) {
		return createAlertDialog(context, context.getResources().getString(messageResId),
				context.getResources().getString(titleResId));
	}

	/**
	 * Create an alert dialog without showing it on screen
	 * 
	 * @param context
	 * @param message
	 * @param title
	 * @return
	 */
	public static AlertDialog createAlertDialog(Context context, String message, String title) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		return alertBuilder.setTitle(title).setMessage(message).setIcon(R.drawable.alert_dark_frame)
				.setOnKeyListener(new DialogInterface.OnKeyListener() {

					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						dialog.dismiss();
						return false;
					}
				}).create();
	}

	public static String toHtmlColor(String text, String color) {
		return "<font color=\"" + color + "\">" + text + "</font>";
	}

	/**
	 * Sets flag icon by doing a {@link Flags} lookup.
	 * 
	 * @param imageView
	 * @param code
	 */
	public static void setFlagIcon(ImageView imageView, String code) {
		int imageId = Flags.getResourceFromCode(code);
		if (imageId != -1) {
			imageView.setImageResource(imageId);
		}
	}

	/**
	 * @see #setFlagIcon(ImageView, String)
	 */
	public static void setFlagIcon(View view, int id, String code) {
		ImageView icon = (ImageView) view.findViewById(id);
		setFlagIcon(icon, code);
	}

	/**
	 * 
	 * @param view
	 * @param id
	 * @param text
	 * @param isHtml
	 *            Set to {@code true}, if {@code text} contains html tags.
	 */
	public static void setText(View view, int id, String text, boolean isHtml) {
		TextView textView = (TextView) view.findViewById(id);
		textView.setText(isHtml ? Html.fromHtml(text) : text);
	}

	/**
	 * @see #setText(View, int, String, boolean)
	 */
	public static void setText(View view, int id, CharSequence text, boolean isHtml) {
		setText(view, id, text.toString(), isHtml);
	}

	/**
	 * @see #setText(View, int, CharSequence, boolean)
	 */
	public static void setText(View view, int id, CharSequence text) {
		setText(view, id, text, false);
	}

	/**
	 * @see #setText(View, int, String, boolean)
	 */
	public static void setText(View view, int id, String text) {
		setText(view, id, text, false);
	}

	public static void setTag(View view, int id, Object tag) {
		view.findViewById(id).setTag(tag);
	}
}

package net.vexelon.currencybg.app.common;

import android.content.Context;

import com.google.common.collect.Sets;

import net.vexelon.currencybg.app.R;

import java.util.Set;

/**
 * Available currency sources
 * <p>
 * Should any values be updated here, make sure to also update the
 * {@code currency_sources_ids} array in {@code arrays.xml}.
 */
public enum Sources {

	/**
	 * @deprecated
	 */
	BNB(1),

	FIB(100),
	TAVEX(200),
	POLANA1(300);

	private int id;

	Sources(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	/**
	 *
	 * @param id
	 * @return {@link Sources} or {@code null}.
	 */
	public static Sources valueOf(int id) {
		for (Sources s : Sources.values()) {
			if (s.getID() == id) {
				return s;
			}
		}
		return null;
	}

	public static String getName(int id, Context context) {
		int[] sourceIds = context.getResources().getIntArray(R.array.currency_sources_ids);
		String[] sourceNames = context.getResources().getStringArray(R.array.currency_sources);

		for (int i = 0; i < sourceIds.length; i++) {
			if (sourceIds[i] == id) {
				return sourceNames[i];
			}
		}

		return "";
	}
}

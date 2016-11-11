package net.vexelon.currencybg.app.common;

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
}

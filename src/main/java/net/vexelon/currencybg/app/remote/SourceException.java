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
package net.vexelon.currencybg.app.remote;

@SuppressWarnings("serial")
public class SourceException extends Exception {

	public static final int HTTP_CODE_MAINTENANCE = 503;

	private boolean isMaintenanceError = false;
	private int code;

	public SourceException(int code) {
		super();
		this.code = code;
		this.isMaintenanceError = HTTP_CODE_MAINTENANCE == code;
	}

	public SourceException(String message) {
		super(message);
	}

	public SourceException(Throwable t) {
		super(t);
	}

	public SourceException(String message, Throwable t) {
		super(message, t);
	}

	public int getCode() {
		return code;
	}

	public boolean isMaintenanceError() {
		return isMaintenanceError;
	}
}

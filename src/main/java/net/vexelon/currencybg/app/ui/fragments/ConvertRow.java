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
package net.vexelon.currencybg.app.ui.fragments;

public class ConvertRow {
	
	enum RowType {
		RowBGN,
		RowOthers
	};	
	
	private static int lastId = 0;
	
	private int rowId;
	private int spinnerId;
	private int editTextId;
	private RowType rowType;
	
	public ConvertRow(int spinnerId, int editTextId, RowType rowType) {
		this.spinnerId = spinnerId;
		this.editTextId = editTextId;
		this.rowId = ++ConvertRow.lastId;
		this.rowType = rowType;
	}

	public int getRowId() {
		return rowId;
	}

	public int getSpinnerId() {
		return spinnerId;
	}

	public int getEditTextId() {
		return editTextId;
	}	
	
	public RowType getRowType() {
		return rowType;
	}
}

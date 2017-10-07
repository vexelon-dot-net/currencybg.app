package net.vexelon.currencybg.app.ui.events;

import android.support.annotation.NonNull;

import com.google.common.base.Supplier;

public interface LoadListener<MODEL> {

	void onLoadStart();

	void onLoadSuccessful(@NonNull Supplier<MODEL> newData);

	void onLoadFailed(final int msgId);
}

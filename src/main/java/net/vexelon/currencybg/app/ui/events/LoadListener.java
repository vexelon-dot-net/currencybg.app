package net.vexelon.currencybg.app.ui.events;

import android.support.annotation.NonNull;

import com.google.common.base.Supplier;

public interface LoadListener<T> {

	void onLoadSuccessful(@NonNull Supplier<T> newData);

	void onLoadFailed(final int msgId);
}

package net.vexelon.currencybg.app.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;

import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.ui.components.InfoListAdapter;

import java.util.List;

public class InfoFragment extends AbstractFragment {

	private static final String URL_3RDPARTY = "intr://3rd";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.fragment_info, container, false);
		init(rootView);
		return rootView;
	}

	private void init(View view) {
		final Activity activity = getActivity();
		ListView lvInfo = (ListView) view.findViewById(R.id.list_info);
		final InfoListAdapter adapter = new InfoListAdapter(getActivity(), R.layout.info_row, getInfosList());
		lvInfo.setAdapter(adapter);
		lvInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String url = adapter.getUrl(position);
				if (URL_3RDPARTY.equals(url)) {
					new MaterialDialog.Builder(getActivity()).customView(R.layout.fragment_thirdparty, true)
							.positiveText(R.string.text_ok).build().show();
				} else if (url != null) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(browserIntent);
				}
			}
		});
	}

	private List<InfoListAdapter.InfoItem> getInfosList() {
		List<InfoListAdapter.InfoItem> infoList = Lists.newArrayList();

		PackageInfo packageInfo = null;
		try {
			packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),
					PackageManager.GET_GIDS);
			infoList.add(newInfoRow(getString(R.string.about_version), packageInfo.versionName));
		} catch (Exception e) {
			Log.e(Defs.LOG_TAG, "", e);
		}

		infoList.add(newInfoRow(getString(R.string.about_rates_provider), getString(R.string.about_rates_provider_text),
				"http://www.bnb.bg"));
		infoList.add(newInfoRow(getString(R.string.about_join_appdev), "github.com/vexelon-dot-net/currencybg.app",
				"https://github.com/vexelon-dot-net/currencybg.app"));
		infoList.add(newInfoRow(getString(R.string.about_author), getString(R.string.about_author_text)));
		infoList.add(newInfoRow(getString(R.string.about_logo), getString(R.string.about_logo_text),
				"http://www.stremena.com"));
		infoList.add(newInfoRow(getString(R.string.about_flag_icons),
				"Copyright (c) 2013 Aha-Soft. http://www.aha-soft.com/free-icons/free-yellow-button-icons",
				"http://www.aha-soft.com/free-icons/free-yellow-button-icons"));
		infoList.add(newInfoRow(getString(R.string.about_flag_icons),
				"Copyright (CC BY-ND 3.0) Visual Pharm. http://icons8.com", "http://icons8.com"));
		infoList.add(newInfoRow(getString(R.string.about_3rdparty), "", URL_3RDPARTY));

		return infoList;
	}

	private InfoListAdapter.InfoItem newInfoRow(String name, String value, String url) {
		return new InfoListAdapter.InfoItem(name, value, url);
	}

	private InfoListAdapter.InfoItem newInfoRow(String name, String value) {
		return newInfoRow(name, value, null);
	}
}

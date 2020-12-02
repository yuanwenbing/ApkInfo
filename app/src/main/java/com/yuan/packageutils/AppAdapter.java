package com.yuan.packageutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Yuan on 10/31/15.
 * <p>
 * app info adapter
 */
class AppAdapter extends BaseAdapter {
    private Context mContext;

    private LayoutInflater mInflater;

    private CopyOnWriteArrayList<AppInfo> mDatas = new CopyOnWriteArrayList<>();

    AppAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    void refresh(CopyOnWriteArrayList<AppInfo> datas) {
        mDatas.clear();
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.app_item, viewGroup, false);
            holder.countTv = view.findViewById(R.id.app_item_count);
            holder.imageView = (ImageView) view.findViewById(R.id.app_item_icon);
            holder.nameText = (TextView) view.findViewById(R.id.app_item_name);
            holder.versionName = (TextView) view.findViewById(R.id.app_item_version_name);
            holder.versionCode = (TextView) view.findViewById(R.id.app_item_version_code);
            holder.packageNameText = (TextView) view.findViewById(R.id.app_item_package_name);
            holder.lastInstallText = (TextView) view.findViewById(R.id.app_item_last_install);
            holder.installPathText = (TextView) view.findViewById(R.id.app_item_install_path);
            holder.isSystemText = (TextView) view.findViewById(R.id.app_item_is_system);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        try {
            final AppInfo info = (AppInfo) getItem(i);
            holder.countTv.setText(String.valueOf(i + 1));
            holder.imageView.setImageDrawable(info.getAppIcon());
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    Uri uri = Uri.parse("package:" + info.getPackageName());//获取删除包名的URI
                    intent.setAction(Intent.ACTION_DELETE);//设置我们要执行的卸载动作
                    intent.setData(uri);//设置获取到的URI
                    mContext.startActivity(intent);
                }
            });
            holder.nameText.setText(String.format(mContext.getResources().getString(R.string.apps_name), info.getAppName()));
            holder.packageNameText.setText(String.format(mContext.getResources().getString(R.string.package_name), info.getPackageName()));
            holder.lastInstallText.setText(String.format(mContext.getResources().getString(R.string.last_install), info.getLastInstall()));
            holder.installPathText.setText(String.format(mContext.getResources().getString(R.string.install_path), info.getInstallPath()));
            holder.versionCode.setText(String.format(mContext.getResources().getString(R.string.version_code), info.getVersionCode()));
            holder.versionName.setText(String.format(mContext.getResources().getString(R.string.version_name), info.getVersionName()));

            if (info.isSystem()) {
                holder.isSystemText.setText(String.format(mContext.getString(R.string.is_system), "是"));
            } else {
                holder.isSystemText.setText(String.format(mContext.getString(R.string.is_system), "否"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return view;
    }

    private class ViewHolder {

        TextView countTv;

        ImageView imageView;

        TextView nameText;

        TextView versionName;

        TextView versionCode;

        TextView packageNameText;

        TextView lastInstallText;

        TextView installPathText;

        TextView isSystemText;

    }
}

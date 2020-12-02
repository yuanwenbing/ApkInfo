package com.yuan.packageutils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView mListView;

    private AppAdapter mAdapter;

    private final CopyOnWriteArrayList<AppInfo> mDataList = new CopyOnWriteArrayList<>();

    private NavigationView mNavigationView;

    private final ExecutorService mFixedThreadPool = Executors.newFixedThreadPool(6);

    private AppReceiver mAppStateReceiver;

    @SuppressLint("HandlerLeak")
    Handler appHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 2) { // 刷新
                mAdapter.refresh(mDataList);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        Resources resource = (Resources) getBaseContext().getResources();
        ColorStateList csl = (ColorStateList) resource.getColorStateList(R.color.navigation_menu_item_color);
        mNavigationView.setItemTextColor(csl);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        getSupportActionBar().setTitle("全部应用");
        mNavigationView.setNavigationItemSelectedListener(this);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppInfo info = (AppInfo) mAdapter.getItem(i);
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(info.getPackageName());//获取启动的包名
                if (intent == null) {
                    Snackbar.make(mListView, "启动不了！", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    return;
                }
                startActivity(intent);
            }
        });
        mAdapter = new AppAdapter(this);
        mListView.setAdapter(mAdapter);
        getAppsInfo();

        mAppStateReceiver = new AppReceiver();
        IntentFilter filter = new IntentFilter();

        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");

        this.registerReceiver(mAppStateReceiver, filter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);//在菜单中找到对应控件的item
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                CopyOnWriteArrayList<AppInfo> queryArray = new CopyOnWriteArrayList<AppInfo>();
                for (int i = 0; i < mDataList.size(); i++) {
                    String appName = mDataList.get(i).getAppName();
                    if (appName.toLowerCase().contains(newText.toLowerCase())) {
                        queryArray.add(mDataList.get(i));
                    }
                }
                mAdapter.refresh(queryArray);
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {//设置打开关闭动作监听
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                //                makeText(MainActivity.this, "onExpand", LENGTH_LONG).show();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                //                makeText(MainActivity.this, "Collapse", LENGTH_LONG).show();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.all) {
            mAdapter.refresh(mDataList);
            mListView.smoothScrollToPosition(0);
            Snackbar.make(mListView, "全部应用", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            mNavigationView.getMenu().getItem(0).setChecked(true);
            mNavigationView.getMenu().getItem(1).setChecked(false);
            mNavigationView.getMenu().getItem(2).setChecked(false);
            getSupportActionBar().setTitle("全部应用");


        } else if (id == R.id.system) {
            CopyOnWriteArrayList<AppInfo> list = new CopyOnWriteArrayList<>();
            for (int i = 0; i < mDataList.size(); i++) {
                if (mDataList.get(i).isSystem()) {
                    list.add(mDataList.get(i));
                }
            }
            mAdapter.refresh(list);
            mListView.smoothScrollToPosition(0);
            Snackbar.make(mListView, "系统应用", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            mNavigationView.getMenu().getItem(0).setChecked(false);
            mNavigationView.getMenu().getItem(1).setChecked(true);
            mNavigationView.getMenu().getItem(2).setChecked(false);
            getSupportActionBar().setTitle("系统应用");
        } else if (id == R.id.user) {
            CopyOnWriteArrayList<AppInfo> list = new CopyOnWriteArrayList<>();
            for (int i = 0; i < mDataList.size(); i++) {
                if (!mDataList.get(i).isSystem()) {
                    list.add(mDataList.get(i));
                }
            }
            mAdapter.refresh(list);
            mListView.smoothScrollToPosition(0);
            Snackbar.make(mListView, "安装应用", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            mNavigationView.getMenu().getItem(0).setChecked(false);
            mNavigationView.getMenu().getItem(1).setChecked(true);
            mNavigationView.getMenu().getItem(2).setChecked(true);
            getSupportActionBar().setTitle("安装应用");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getAppsInfo() {
        List<PackageInfo> packageInfoList = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packageInfoList.size(); i++) {
            mFixedThreadPool.execute(new ReadAppThread(packageInfoList, i));
        }
    }


    class ReadAppThread implements Runnable {

        private final List<PackageInfo> packageInfoList;

        private final int i;

        public ReadAppThread(List<PackageInfo> packageInfoList, int i) {
            this.packageInfoList = packageInfoList;
            this.i = i;
        }

        @Override
        public void run() {
            final AppInfo appInfo = readAppInfo(packageInfoList, i);
            mDataList.add(appInfo);
            Log.d("ReadAppThread", appInfo.getAppName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appHandler.sendEmptyMessage(2);
                }
            });
        }
    }

    @NonNull
    private AppInfo readAppInfo(List<PackageInfo> packageInfoList, int i) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        PackageInfo pInfo = packageInfoList.get(i);
        AppInfo appInfo = new AppInfo();
        appInfo.setAppName(pInfo.applicationInfo.loadLabel(getPackageManager()).toString());
        appInfo.setPackageName(pInfo.packageName);
        appInfo.setVersionCode(pInfo.versionCode);
        appInfo.setVersionName(pInfo.versionName);
        appInfo.setLastInstall(sdf.format(pInfo.firstInstallTime));
        appInfo.setInstallPath(pInfo.applicationInfo.sourceDir);
        appInfo.setAppIcon(pInfo.applicationInfo.loadIcon(getPackageManager()));
        appInfo.setSystem((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
        return appInfo;
    }


    public class AppReceiver extends BroadcastReceiver {
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                final String packageName = intent.getData().getSchemeSpecificPart();
                mDataList.forEach(new Consumer<AppInfo>() {
                    @Override
                    public void accept(AppInfo appInfo) {
                        if (appInfo.getPackageName().equals(packageName)) {
                            mDataList.remove(appInfo);
                        }
                    }
                });

                mAdapter.refresh(mDataList);
                Toast.makeText(context, "卸载成功" + packageName, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAppStateReceiver != null) {
            unregisterReceiver(mAppStateReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

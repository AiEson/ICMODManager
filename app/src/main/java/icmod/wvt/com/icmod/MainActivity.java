package icmod.wvt.com.icmod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.cardview.widget.CardView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.*;

import net.lingala.zip4j.ZipFile;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import icmod.wvt.com.icmod.others.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static FloatingActionButton fab;
    public static final String SHARE_APP_TAG = "SHARE_APP_TAG";
    ConstraintLayout constraintLayout;
    TabLayout tabLayout;
    @SuppressLint("StaticFieldLeak")
    static TextView sidebarUserName;
    String path;
    ListView listView;
    @SuppressLint("StaticFieldLeak")
    static ImageView sidebarImageView;
    LocalMODAdapter localMODAdapter;
    LocalMAPAdapter localMAPAdapter;
    LocalResAdapter localResAdapter;
    public static ProgressDialog pDialog;
    @SuppressLint("StaticFieldLeak")
    private static Activity mActivity;
    private LruCacheUtils lruCacheUtils;
    List<OnlineMOD> onlineMODList;
    static Context mContext;
    int type;
    File modP, mapP, choosePath = null;
    static File userData;
    private long firstTime = 0;
    Toolbar toolbar;
    static Boolean haveUserData = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FileDownloader.setup(this);
        type = FinalValuable.MOD;
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showFileChooseDialog());
        constraintLayout = findViewById(R.id.constraintLayout);
        tabLayout = findViewById(R.id.tablayout);
        tabLayout.addTab(tabLayout.newTab().setText("汉化组源").setTag(FinalValuable.OnlineHhz));
        tabLayout.addTab(tabLayout.newTab().setText("官网源").setTag(FinalValuable.OnlineGf));
        tabLayout.setVisibility(View.GONE);
        lruCacheUtils = new LruCacheUtils(this);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        sidebarImageView = headerView.findViewById(R.id.sidebarImageView);
        sidebarImageView.setOnClickListener(view -> {
            if (haveUserData) {
                try {
                    final File userData = new File(FinalValuable.UserInfo);
                    JSONObject json = new JSONObject(Algorithm.readFile(userData));
                    JSONObject userInfo = json.getJSONObject("user_info");
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("用户信息")
                            .setMessage("ID：" + userInfo.getInt("user_id") + "\n名称：" + userInfo.getString("user_name"))
                            .setNegativeButton("返回", (dialog, which) -> {

                            })
                            .setPositiveButton("注销登录", (dialog, which) -> {
                                sidebarImageView.setImageBitmap(Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.baseline_account_circle_black_24dp));
                                sidebarUserName.setText("点击头像以登录");
                                Algorithm.deleteFile(userData);
                                haveUserData = false;
                            })
                            .create();
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Intent loginActivity = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginActivity);
            }
        });
        sidebarUserName = headerView.findViewById(R.id.userName);
        listView = findViewById(R.id.mainListView);
        modP = new File(FinalValuable.MODDir);
        if (!modP.exists()) modP.mkdirs();
        localMODAdapter = new LocalMODAdapter(this, R.layout.mod_item, flashNativeMOD(true));
        listView.setAdapter(localMODAdapter);
        if (!Algorithm.isAvilible(MainActivity.this, "com.mojang.minecraftpe")) {
            AlertDialog alertDialog1 = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("警告")
                    .setMessage("检测到您未安装Minecraft国际版，不能正常进入InnerCore，是否跳转去下载伪装软件？（安装Minecraft原版需要卸载伪装）")
                    .setNegativeButton("不用了", (dialogInterface, i) -> {

                    })
                    .setPositiveButton("去下载", (dialogInterface, i) -> {
                        Intent intent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://adodoz.cn/MinecraftCamouflage.apk")
                        );
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .create();
            alertDialog1.show();
        }
        if (!Algorithm.isAvilible(MainActivity.this, "com.zhekasmirnov.innercore")) {
            AlertDialog alertDialog2 = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("警告")
                    .setMessage("检测到您未安装InnerCore主程序，是否跳转去下载主程序？")
                    .setNegativeButton("不用了", (dialogInterface, i) -> {

                    })
                    .setPositiveButton("去下载", (dialogInterface, i) -> {
                        Intent intent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.coolapk.com/game/com.zhekasmirnov.innercore")
                        );
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .create();
            alertDialog2.show();
            mActivity = MainActivity.this;
        }
        Bitmap bitmap = Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.no_logo);
        lruCacheUtils.savePicToMemory("null", bitmap);
        flashUser();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tag = (int) tab.getTag();
                switch (tag) {
                    case FinalValuable.OnlineGf:
                        type = FinalValuable.OnlineGf;
                        if (userData.exists()) {
                            listView.setVisibility(View.GONE);
                            get_online_json();
                        } else {
                            print("请登录后再试", Snackbar.LENGTH_LONG);
                        }
                        break;
                    case FinalValuable.OnlineHhz:
                        type = FinalValuable.OnlineHhz;
                        if (userData.exists()) {
                            listView.setVisibility(View.GONE);
                            get_online_json();
                        } else {
                            print("请登录后再试", Snackbar.LENGTH_LONG);
                        }

                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        SharedPreferences setting = getSharedPreferences(SHARE_APP_TAG, 0);
        Boolean user_first = setting.getBoolean("FIRST", true);
        mContext = getApplicationContext();

        if (user_first) {//第一次
            setting.edit().putBoolean("FIRST", false).apply();
            drawer.openDrawer(GravityCompat.START);
        }

        if (Algorithm.isNetworkAvailable(MainActivity.this))
            new Thread(() -> {
                try {
                    String ret2 = null;
                    String Resultms = null;
                    String lines;
                    StringBuilder response = new StringBuilder();
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL("https://adodoz.cn/ICMODManagerAPI.php");
                        //连接服务器
                        connection = (HttpURLConnection) url.openConnection();
                        //上传服务器内容
                        connection.setRequestMethod("POST");
                        connection.setConnectTimeout(8000);
                        connection.setDoInput(true);//允许输入
                        connection.setDoOutput(true);//允许输出
                        connection.setUseCaches(false);
                        connection.setRequestProperty("Accept-Charset", "UTF-8");
                        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                        connection.connect();
                        DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                        outStream.writeBytes("order=getupdate");
                        outStream.flush();
                        outStream.close();
                        //读取响应
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        //读数据
                        while ((lines = reader.readLine()) != null) {
                            lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                            response.append(lines);
                        }
                        ret2 = response.toString().trim();
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                    Log.e("TAG", ret2 + "");
                    JSONObject jsonObject = new JSONObject(ret2);
                    if (jsonObject != null) {
                        if (jsonObject.getInt("versioncode") != Algorithm.getVersionCode(MainActivity.this)) {
                            MainActivity.this.runOnUiThread(() -> {
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("更新提醒")
                                        .setMessage("发现新版本，请立即更新")
                                        .setNegativeButton("取消", (dialogInterface, i) -> {

                                        })
                                        .setPositiveButton("去更新", (dialogInterface, i) -> {
                                            Intent intent = new Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://www.coolapk.com/game/icmod.wvt.com.icmod")
                                            );
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }).create();
                                alertDialog.show();
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        flashUser();
    }

    protected void get_online_json() {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("加载中...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final JSONArray[] jsonArray = {null};
        final List<OnlineMOD> onlineMODList = new ArrayList<>();
        try {
            switch (type) {
                case FinalValuable.OnlineGf:
                    if (!new File(FinalValuable.NetModDataGw).exists()) {
                        FileDownloader.getImpl().create("https://adodoz.cn/mods/allmodinfo.json").setPath(FinalValuable.NetModDataGw)
                                .setListener(new FileDownloadListener() {
                                    @Override
                                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                                    }

                                    @Override
                                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                        progressDialog.setMessage("数据加载进度：" + Algorithm.getPercent(soFarBytes, totalBytes));
                                    }

                                    @Override
                                    protected void completed(BaseDownloadTask task) {
                                        File file = new File(FinalValuable.NetModDataGw);
                                        if (file.exists()) {
                                            try {
                                                jsonArray[0] = new JSONArray(Algorithm.readFile(file));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        new Thread(() -> {
                                            List<MOD> modList = flashNativeMOD(false);
                                            for (int i = 0; i < jsonArray[0].length(); i++) {
                                                JSONObject jsonObject = null;
                                                Log.e("TAG", i + "");
                                                final int finalI = i;
                                                final JSONArray finalJsonArray = jsonArray[0];
                                                final int finalI1 = i;
                                                MainActivity.this.runOnUiThread(() -> progressDialog.setMessage("正在比较数据：" + Algorithm.getPercent(finalI1, jsonArray[0].length())));
                                                try {
                                                    jsonObject = jsonArray[0].getJSONObject(i);
                                                    String name = jsonObject.getString("title");
                                                    String description = jsonObject.getString("description");
                                                    OnlineMOD onlineMOD = new OnlineMOD(FinalValuable.ICCNUrl + "mods/" + jsonObject.getInt("id"), FinalValuable.ICCNUrl + "mods/img/" + jsonObject.getString("icon"), name, description, FinalValuable.OnlineGf);
                                                    boolean isHave = false;
                                                    for (int j = 0; j < modList.size(); j++) {
                                                        if (modList.get(j).getName() != null && modList.get(j).getName().equals(name))
                                                            isHave = true;
                                                    }
                                                    if (!isHave)
                                                        onlineMODList.add(onlineMOD);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            MainActivity.this.runOnUiThread(() -> {
                                                MainActivity.this.onlineMODList = onlineMODList;
                                                listView.setAdapter(new OnlineMODAdapter(MainActivity.this, R.layout.mod_item, onlineMODList));
                                                listView.setVisibility(View.VISIBLE);
                                                progressDialog.dismiss();
                                            });
                                        }).start();
                                    }

                                    @Override
                                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                                    }

                                    @Override
                                    protected void error(BaseDownloadTask task, Throwable e) {
                                        print("获取失败，请重试", Snackbar.LENGTH_SHORT);
                                        e.printStackTrace();
                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    protected void warn(BaseDownloadTask task) {

                                    }
                                }).start();
                    } else {
                        new Thread(() -> {
                            List<MOD> modList = flashNativeMOD(false);
                            try {
                                jsonArray[0] = new JSONArray(Algorithm.readFile(new File(FinalValuable.NetModDataGw)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            for (int i = 0; i < jsonArray[0].length(); i++) {
                                JSONObject jsonObject = null;
                                Log.e("TAG", i + "");
                                final int finalI = i;
                                final JSONArray finalJsonArray = jsonArray[0];
                                final int finalI1 = i;
                                MainActivity.this.runOnUiThread(() -> progressDialog.setMessage("正在比较数据：" + Algorithm.getPercent(finalI1, jsonArray[0].length())));
                                try {
                                    jsonObject = jsonArray[0].getJSONObject(i);
                                    String name = jsonObject.getString("title");
                                    String description = jsonObject.getString("description");
                                    OnlineMOD onlineMOD = new OnlineMOD(FinalValuable.ICCNUrl + "mods/" + jsonObject.getInt("id"), FinalValuable.ICCNUrl + "mods/img/" + jsonObject.getString("icon"), name, description, FinalValuable.OnlineGf);
                                    boolean isHave = false;
                                    for (int j = 0; j < modList.size(); j++) {
                                        if (modList.get(j).getName() != null && modList.get(j).getName().equals(name))
                                            isHave = true;
                                    }
                                    if (!isHave)
                                        onlineMODList.add(onlineMOD);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            MainActivity.this.runOnUiThread(() -> {
                                MainActivity.this.onlineMODList = onlineMODList;
                                listView.setAdapter(new OnlineMODAdapter(MainActivity.this, R.layout.mod_item, onlineMODList));
                                listView.setVisibility(View.VISIBLE);
                                progressDialog.dismiss();
                            });
                        }).start();
                    }
                    break;
                case FinalValuable.OnlineHhz:
                    new Thread(() -> {
                        if (!new File(FinalValuable.NetModDataHhz).exists()) {
                            String ret2 = null;
                            ret2 = Algorithm.Post("", "http://www.innercorehhz.cf/alljson.php", MainActivity.this);
                            try {
                                jsonArray[0] = new JSONArray(ret2);
                                Algorithm.writeFile(FinalValuable.NetModDataHhz, ret2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                jsonArray[0] = new JSONArray(Algorithm.readFile(new File(FinalValuable.NetModDataHhz)));
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                        for (int i = 0; i < jsonArray[0].length(); i++) {
                            JSONObject jsonObject = null;
                            Log.e("TAG", i + "");
                            try {
                                jsonObject = jsonArray[0].getJSONObject(i);
                                if (!jsonObject.getString("state").equals("1"))
                                    continue;
                                String name = jsonObject.getString("name");
                                String description = jsonObject.getString("info");
                                OnlineMOD onlineMOD = new OnlineMOD("http://www.innercorehhz.cf/hhz/download.php" + "?id=" + ((jsonObject.getInt("id")) - 5000), FinalValuable.ICHhzUrl + "mods/已通过/icon/" + jsonObject.getString("icon"), name, description, FinalValuable.OnlineHhz);
                                onlineMODList.add(onlineMOD);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        MainActivity.this.onlineMODList = onlineMODList;
                        MainActivity.this.runOnUiThread(() -> {
                            listView.setAdapter(new OnlineMODAdapter(MainActivity.this, R.layout.mod_item, onlineMODList));
                            listView.setVisibility(View.VISIBLE);
                            progressDialog.dismiss();
                        });
                    }).start();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            MainActivity.this.runOnUiThread(() -> print("获取信息失败，点击侧滑栏刷新按钮以重试", Snackbar.LENGTH_LONG));
        }
    }

    /**
     * 支付宝支付
     **/
    private void donateAlipay() {
        boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(this);
        if (hasInstalledAlipayClient) {
            AlipayDonate.startAlipayClient(this, "fkx18184vir1w8crfl6vsa9");
        }
    }

    /**
     * 需要提前准备好 微信收款码 照片，可通过微信客户端生成
     */
    private void donateWeixin() {
        Toast.makeText(MainActivity.this, "付款码图片已保存，请手动打开本地相册扫码", Toast.LENGTH_LONG).show();
        InputStream weixinQrIs = getResources().openRawResource(R.raw.wxfkm);
        String qrPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM" + File.separator + "Donate" + File.separator +
                "Donate.png";
        WeiXinDonate.saveDonateQrImage2SDCard(qrPath, BitmapFactory.decodeStream(weixinQrIs));
        WeiXinDonate.donateViaWeiXin(this, qrPath);
    }

    public static Context getContext() {
        return mContext;
    }

    protected void showFileChooseDialog() {
        AlertDialog alertDialogWindow = null;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setLayoutParams(params);
        layout.setGravity(Gravity.TOP);
        layout.setOrientation(LinearLayout.VERTICAL);
        final ListView listView = new ListView(MainActivity.this);
        final TextView textView = new TextView(MainActivity.this);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int dp = Algorithm.dp2px(MainActivity.this, 16);
        textView.setPadding(Algorithm.dp2px(MainActivity.this, 20), 0, dp, 0);
        listView.setPadding(dp, Algorithm.dp2px(MainActivity.this, 2), dp, 0);
        final File defPath = (choosePath == null) ? new File(Environment.getExternalStorageDirectory().toString()) : choosePath;

        final List<String> fileList = Algorithm.orderByName(defPath.toString());
        listView.setDivider(new ColorDrawable(Color.alpha(0)));
        listView.setDividerHeight(0);
        listView.setAdapter(new FileAdapter(MainActivity.this, R.layout.select_item, fileList, defPath, textView));
        alertDialogWindow = alertDialog.setView(layout)
                .setTitle("请选择一个文件")
                .setNegativeButton("取消", (dialog, which) -> {
                })
                .show();
        final AlertDialog finalAlertDialogWindow = alertDialogWindow;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            File defPath2 = defPath;
            List<String> fileList2 = fileList;

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File onFile = new File(defPath2.toString() + File.separator + fileList2.get(i));
                if (fileList2.get(i).equals("..")) {
                    try {
                        defPath2 = defPath2.getParentFile();
                        if (defPath2.isDirectory()) {
                            fileList2 = Algorithm.orderByName(defPath2.toString());
                            listView.setAdapter(new FileAdapter(MainActivity.this, R.layout.select_item, fileList2, defPath2, textView));
                        } else {
                            Toast.makeText(MainActivity.this, "返回上一级失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "失败，请检查您的文件路径", Toast.LENGTH_SHORT).show();
                    }
                } else if (fileList2.get(i).equals("系统浏览器下载")) {
                    try {
                        defPath2 = new File(FinalValuable.SystemDownload);
                        if (defPath2.isDirectory()) {
                            fileList2 = Algorithm.orderByName(defPath2.toString());
                            listView.setAdapter(new FileAdapter(MainActivity.this, R.layout.select_item, fileList2, defPath2, textView));
                        } else {
                            Toast.makeText(MainActivity.this, "进入特定路径失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "失败，请检查您的文件路径", Toast.LENGTH_SHORT).show();
                    }
                } else if (fileList2.get(i).equals("QQ下载")) {
                    try {
                        defPath2 = new File(FinalValuable.QQDownload);
                        if (defPath2.isDirectory()) {
                            fileList2 = Algorithm.orderByName(defPath2.toString());
                            listView.setAdapter(new FileAdapter(MainActivity.this, R.layout.select_item, fileList2, defPath2, textView));
                        } else {
                            Toast.makeText(MainActivity.this, "进入特定路径失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "失败，请检查您的文件路径", Toast.LENGTH_SHORT).show();
                    }
                } else if (fileList2.get(i).equals("微信下载")) {
                    try {
                        defPath2 = new File(FinalValuable.WeChatDownload);
                        if (defPath2.isDirectory()) {
                            fileList2 = Algorithm.orderByName(defPath2.toString());
                            listView.setAdapter(new FileAdapter(MainActivity.this, R.layout.select_item, fileList2, defPath2, textView));
                        } else {
                            Toast.makeText(MainActivity.this, "进入特定路径失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "失败，请检查您的文件路径", Toast.LENGTH_SHORT).show();
                    }
                } else if (fileList2.get(i).equals("百度网盘下载")) {
                    try {
                        defPath2 = new File(FinalValuable.BaiduNetDiskDownload);
                        if (defPath2.isDirectory()) {
                            fileList2 = Algorithm.orderByName(defPath2.toString());
                            listView.setAdapter(new FileAdapter(MainActivity.this, R.layout.select_item, fileList2, defPath2, textView));
                        } else {
                            Toast.makeText(MainActivity.this, "进入特定路径失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "失败，请检查您的文件路径", Toast.LENGTH_SHORT).show();
                    }
                } else if (onFile.isDirectory()) {
                    defPath2 = onFile;
                    fileList2 = Algorithm.orderByName(defPath2.toString());
                    listView.setAdapter(new FileAdapter(MainActivity.this, R.layout.select_item, fileList2, defPath2, textView));
                } else {
                    try {
                        ZipFile zipFile = new ZipFile(onFile.toString());
                        if (zipFile.isValidZipFile()) {
                            path = onFile.toString();
                            switch (type) {
                                case FinalValuable.MOD:
                                    new Thread(() -> {
                                        handler.sendEmptyMessage(1);
                                        if (Algorithm.installMOD(path)) {
                                            choosePath = defPath2;
                                            handler.sendEmptyMessage(3);
                                        } else handler.sendEmptyMessage(4);
                                        handler.sendEmptyMessage(2);
                                    }
                                    ).start();
                                    break;
                                case FinalValuable.MCMAP:
                                    new Thread(() -> {
                                        handler.sendEmptyMessage(1);
                                        if (Algorithm.installMAP(path, FinalValuable.MCMAPDir)) {
                                            choosePath = defPath2;
                                            handler.sendEmptyMessage(3);
                                        } else handler.sendEmptyMessage(4);
                                        handler.sendEmptyMessage(2);
                                    }
                                    ).start();
                                    break;
                                case FinalValuable.ICMAP:
                                    new Thread(() -> {
                                        handler.sendEmptyMessage(1);
                                        if (Algorithm.installMAP(path, FinalValuable.ICMAPDir)) {
                                            choosePath = defPath2;
                                            handler.sendEmptyMessage(3);
                                        } else {
                                            handler.sendEmptyMessage(4);
                                        }
                                        handler.sendEmptyMessage(2);
                                    }
                                    ).start();
                                    break;
                                case FinalValuable.ICRES:
                                    new Thread(() -> {
                                        handler.sendEmptyMessage(1);
                                        if (Algorithm.installRes(path, FinalValuable.ResDir)) {
                                            choosePath = defPath2;
                                            handler.sendEmptyMessage(3);
                                        } else {
                                            handler.sendEmptyMessage(4);
                                        }
                                        handler.sendEmptyMessage(2);
                                    }
                                    ).start();
                            }
                            finalAlertDialogWindow.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, "所选文件不是压缩文件或已损坏", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        layout.addView(textView);
        layout.addView(listView);
    }

    @SuppressLint("SetTextI18n")
    public static void flashUser() {
        try {
            userData = new File(FinalValuable.UserInfo);
            if (userData.exists()) {
                try {
                    haveUserData = true;
                    JSONObject json = new JSONObject(Algorithm.readFile(userData));
                    JSONObject userInfo = json.getJSONObject("user_info");
                    sidebarUserName.setText("欢迎您，" + userInfo.getString("user_name"));
                    load_avatar loadAvatar = new load_avatar(userInfo.getString("user_avatar"));
                    loadAvatar.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    print("出现未知错误，请注销后再次登录", Snackbar.LENGTH_LONG);
                }
            } else {
                haveUserData = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class load_avatar extends AsyncTask<Void, Void, Bitmap> {
        String url;

        load_avatar(String url) {
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            return Algorithm.getImageBitmapFromUrl(url);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap == null)
                print("头像获取失败，请稍后重试", Snackbar.LENGTH_LONG);
            else
                sidebarImageView.setImageBitmap(bitmap);
        }
    }

    protected static void print(String string, int longg) {
        Snackbar.make(fab, string, longg)
                .setAction("Action", null).show();
    }

    private void showProDialg() {
        if (null == pDialog) {
            pDialog = new ProgressDialog(MainActivity.this);
        }
        pDialog.setMessage("请稍等...");
        pDialog.setCancelable(false);
        pDialog.create();
        pDialog.show();
    }

    class FileAdapter extends ArrayAdapter {
        List<String> fileList;
        private int resourceId;
        private TextView textView;
        File fileDir;

        FileAdapter(Context context, int resource, List<String> fileList, File fileDir, TextView textView2) {
            super(context, resource, fileList);
            this.fileList = fileList;
            this.resourceId = resource;
            this.fileDir = fileDir;
            textView2.setText(fileDir.toString());
        }

        @NotNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            File file = new File(fileDir.toString() + File.separator + getItem(position));
            View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            ImageView image = view.findViewById(R.id.seleteimage);
            TextView textView = view.findViewById(R.id.seletetext);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            Bitmap bitmap = (file.isDirectory() ? Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.round_folder_open_black_36dp) : Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.round_description_black_36dp));
            if (!file.exists())
                bitmap = Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.baseline_open_in_new_black_24dp);
            textView.setText(fileList.get(position));
            image.setImageBitmap(bitmap);
            return view;
        }
    }

    public List<MOD> flashNativeMOD(boolean showNumber) {
        List<MOD> ret = new ArrayList<>();
        File[] modsFIle = modP.listFiles();
        int qy = 0;
        if (modsFIle != null)
            for (int i = 0; i < modsFIle.length; i++) {
                if (modsFIle[i].isDirectory()) {
                    MOD mod = Algorithm.getNativeMODClass(modsFIle[i].toString());
                    if (mod != null) {
                        ret.add(mod);
                        if (mod.getImagePath() != null) {
                            Bitmap bitmap = Algorithm.getBitmap(mod.getImagePath());
                            lruCacheUtils.savePicToMemory(mod.getImagePath(), bitmap);
                        }
                    }
                }
            }
        for (int i = 0; i < ret.size(); i++)
            if (ret.get(i).getEnabled())
                qy += 1;
        if (type == FinalValuable.MOD && showNumber)
            print("加载本地MOD完毕，已启用的共" + qy + "个", Snackbar.LENGTH_SHORT);
        return ret;
    }

    public List<MAP> flashNativeMAP(boolean showNumber) {
        List<MAP> ret = new ArrayList<MAP>();
        File[] mapsFIle = mapP.listFiles();
        if (mapsFIle != null)
            for (int i = 0; i < mapsFIle.length; i++) {
                if (mapsFIle[i].isDirectory()) {
                    MAP map = Algorithm.getNativeMAPClass(mapsFIle[i].toString(), type);
                    if (map != null) {
                        ret.add(map);
                        if (map.getImagePath() != null) {
                            Bitmap bitmap = Algorithm.getBitmap(map.getImagePath());
                            lruCacheUtils.savePicToMemory(map.getImagePath(), bitmap);
                        }
                    }
                }
            }
        if (showNumber)
            print("加载本地地图完毕，共" + ret.size() + "个", Snackbar.LENGTH_SHORT);
        return ret;
    }

    public List<ResPack> flashNativeRes(boolean showNumber) {
        List<ResPack> ret = new ArrayList<>();
        File[] mapsFIle = new File(FinalValuable.ResDir).listFiles();
        if (mapsFIle != null)
            for (int i = 0; i < mapsFIle.length; i++) {
                if (mapsFIle[i].isDirectory()) {
                    ResPack res = Algorithm.getNativeResClass(mapsFIle[i].toString());
                    if (res != null) {
                        ret.add(res);
                        if (res.getImagePath() != null) {
                            Bitmap bitmap = Algorithm.getBitmap(res.getImagePath());
                            lruCacheUtils.savePicToMemory(res.getImagePath(), bitmap);
                        }
                    }
                }
            }
        if (showNumber)
            print("加载本地资源包完毕，共" + ret.size() + "个", Snackbar.LENGTH_SHORT);
        return ret;
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    showProDialg();
                    break;
                case 2:
                    pDialog.dismiss();
                    switch (type) {
                        case FinalValuable.MOD:
                            localMODAdapter = new LocalMODAdapter(MainActivity.this, R.layout.mod_item, flashNativeMOD(false));
                            listView.setAdapter(localMODAdapter);
                            break;
                        case FinalValuable.MCMAP:
                        case FinalValuable.ICMAP:
                            localMAPAdapter = new LocalMAPAdapter(MainActivity.this, R.layout.map_item, flashNativeMAP(false));
                            listView.setAdapter(localMAPAdapter);
                            break;
                        case FinalValuable.OnlineGf:
                            break;
                        case FinalValuable.OnlineHhz:
                            break;
                        case FinalValuable.ICRES:
                            localResAdapter = new LocalResAdapter(MainActivity.this, R.layout.mod_item, flashNativeRes(false));
                            listView.setAdapter(localResAdapter);
                            break;

                    }
                    break;
                case 3:
                    Snackbar.make(fab, "安装成功", Snackbar.LENGTH_SHORT).show();
                    break;
                case 4:
                    Snackbar.make(fab, "安装失败，请检查您的文件是否正确", Snackbar.LENGTH_LONG).show();
                    break;
                case 5:
                    print("移动成功", Snackbar.LENGTH_SHORT);
                    break;
                case 6:
                    print("移动失败", Snackbar.LENGTH_SHORT);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
//            super.onBackPressed();
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
                finish();
            }
        }
    }

    //右上角三点菜单位置
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(() -> {
            switch (type) {
                case FinalValuable.MOD:
                    final List<MOD> list = flashNativeMOD(false);
                    MainActivity.this.runOnUiThread(() -> listView.setAdapter(new LocalMODAdapter(MainActivity.this, R.layout.mod_item, list)));
                    break;
                case FinalValuable.ICMAP:
                case FinalValuable.MCMAP:
                    final List<MAP> list3 = flashNativeMAP(false);
                    MainActivity.this.runOnUiThread(() -> listView.setAdapter(new LocalMAPAdapter(MainActivity.this, R.layout.map_item, list3)));
                    break;
                case FinalValuable.OnlineGf:
                case FinalValuable.OnlineHhz:
                    get_online_json();
                    break;
                case FinalValuable.ICRES:
                    final List<ResPack> list4 = flashNativeRes(false);
                    MainActivity.this.runOnUiThread(() -> listView.setAdapter(new LocalResAdapter(MainActivity.this, R.layout.mod_item, list4)));
                    break;
            }
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("正在搜索...");
                new Thread(() -> {
                    switch (type) {
                        case FinalValuable.MOD:
                            final List<MOD> list = flashNativeMOD(false);
                            final List<MOD> list2 = new ArrayList<>();
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).getName() != null && list.get(i).getName().toLowerCase().indexOf(query.toLowerCase()) != -1)
                                    list2.add(list.get(i));
                            }
                            MainActivity.this.runOnUiThread(() -> {
                                listView.setAdapter(new LocalMODAdapter(MainActivity.this, R.layout.mod_item, list2));
                                progressDialog.dismiss();
                            });
                            break;
                        case FinalValuable.ICMAP:
                        case FinalValuable.MCMAP:
                            final List<MAP> list3 = flashNativeMAP(false);
                            final List<MAP> list4 = new ArrayList<>();
                            for (int i = 0; i < list3.size(); i++) {
                                if (list3.get(i).getName() != null && list3.get(i).getName().toLowerCase().indexOf(query.toLowerCase()) != -1)
                                    list4.add(list3.get(i));
                            }
                            MainActivity.this.runOnUiThread(() -> {
                                listView.setAdapter(new LocalMAPAdapter(MainActivity.this, R.layout.map_item, list4));
                                progressDialog.dismiss();
                            });
                            break;
                        case FinalValuable.OnlineGf:
                        case FinalValuable.OnlineHhz:
                            final List<OnlineMOD> onlineMODList1 = new ArrayList<>();
                            for (int i = 0; i < onlineMODList.size(); i++) {
                                if (onlineMODList.get(i).getName() != null && onlineMODList.get(i).getName().toLowerCase().indexOf(query.toLowerCase()) != -1)
                                    onlineMODList1.add(onlineMODList.get(i));
                            }
                            MainActivity.this.runOnUiThread(() -> {
                                listView.setAdapter(new OnlineMODAdapter(MainActivity.this, R.layout.mod_item, onlineMODList1));
                                listView.setVisibility(View.VISIBLE);
                                progressDialog.dismiss();
                            });
                            break;
                        case FinalValuable.ICRES:
                            final List<ResPack> listres = flashNativeRes(false);
                            final List<ResPack> listres2 = new ArrayList<>();
                            for (int i = 0; i < listres.size(); i++) {
                                if (listres.get(i).getName() != null && listres.get(i).getName().toLowerCase().indexOf(query.toLowerCase()) != -1)
                                    listres2.add(listres.get(i));
                            }
                            MainActivity.this.runOnUiThread(() -> {
                                listView.setAdapter(new LocalResAdapter(MainActivity.this, R.layout.mod_item, listres2));
                                progressDialog.dismiss();
                            });
                            break;
                    }
                }).start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
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
        if (id == R.id.search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.addMod:
                type = FinalValuable.MOD;
                localMODAdapter = new LocalMODAdapter(this, R.layout.mod_item, flashNativeMOD(true));
                listView.setAdapter(localMODAdapter);
                toolbar.setTitle("MOD列表");
                tabLayout.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
                break;
            case R.id.addmcmap:
                type = FinalValuable.MCMAP;
                mapP = new File(FinalValuable.MCMAPDir);
                localMAPAdapter = new LocalMAPAdapter(this, R.layout.map_item, flashNativeMAP(true));
                listView.setAdapter(localMAPAdapter);
                toolbar.setTitle("MC地图列表");
                tabLayout.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
                break;
            case R.id.addicmap:
                type = FinalValuable.ICMAP;
                mapP = new File(FinalValuable.ICMAPDir);
                localMAPAdapter = new LocalMAPAdapter(this, R.layout.map_item, flashNativeMAP(true));
                listView.setAdapter(localMAPAdapter);
                toolbar.setTitle("IC地图列表");
                tabLayout.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
                break;
            case R.id.gotoweb:
                final Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://adodoz.cn")
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.onlineDown:
                if (Algorithm.isNetworkAvailable(MainActivity.this)) {
                    listView.setVisibility(View.GONE);
                    toolbar.setTitle("在线MOD列表");
                    type = FinalValuable.OnlineHhz;
                    tabLayout.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.GONE);
                    if (!userData.exists()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("提示")
                                .setMessage("请登录后再使用在线下载功能！")
                                .setNegativeButton("取消", (dialogInterface, i) -> {

                                })
                                .setPositiveButton("去登录", (dialogInterface, i) -> {
                                    Intent intent2 = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivity(intent2);
                                }).create();
                        alertDialog.show();
                    } else {
                        tabLayout.getTabAt(0).select();
                        get_online_json();
                    }
                } else {
                    print("请连接您的网络再试", Snackbar.LENGTH_LONG);
                }
                break;

            case R.id.addicres:
                type = FinalValuable.ICRES;
                localResAdapter = new LocalResAdapter(this, R.layout.mod_item, flashNativeRes(true));
                listView.setAdapter(localResAdapter);
                toolbar.setTitle("材质包列表");
                tabLayout.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
                break;
            case R.id.reload:
                switch (type) {
                    case FinalValuable.MOD:
                        localMODAdapter = new LocalMODAdapter(MainActivity.this, R.layout.mod_item, flashNativeMOD(true));
                        listView.setAdapter(localMODAdapter);
                        break;
                    case FinalValuable.MCMAP:
                    case FinalValuable.ICMAP:
                        localMAPAdapter = new LocalMAPAdapter(this, R.layout.map_item, flashNativeMAP(true));
                        listView.setAdapter(localMAPAdapter);
                        break;
                    case FinalValuable.OnlineGf:
                    case FinalValuable.OnlineHhz:
                        if (userData.exists())
                            get_online_json();
                        else print("请登录后再试", Snackbar.LENGTH_SHORT);
                }

                break;
            case R.id.openic:
                if (!Algorithm.openApp("com.zhekasmirnov.innercore", MainActivity.this))
                    print("进入失败", Snackbar.LENGTH_SHORT);
                break;
            case R.id.openmc:
                if (!Algorithm.openApp("com.mojang.minecraftpe", MainActivity.this))
                    print("进入失败", Snackbar.LENGTH_SHORT);
                break;
            case R.id.join:
                if (Algorithm.isNetworkAvailable(MainActivity.this)) {
                    String urlStr = "https://adodoz.cn/QQGroup.json";
                    getqqgroup_json();
                } else {
                    print("请连接您的网络再试", Snackbar.LENGTH_LONG);
                }
                break;
            case R.id.donate:
                showDonateDialog();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void showDonateDialog() {
        List<String> nameList = new ArrayList<>();
        nameList.add("支付宝捐赠");
        nameList.add("微信捐赠");

        AlertDialog alertDialogWindow = null;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setLayoutParams(params);
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);
        ListView listView = new ListView(MainActivity.this);
        listView.setPadding(10, 10, 10, 5);
        QQAdapter qqAdapter = new QQAdapter(MainActivity.this, R.layout.qqgroup, nameList);
        listView.setAdapter(qqAdapter);
        listView.setDivider(new ColorDrawable(Color.alpha(0)));
        listView.setDividerHeight(0);
        alertDialogWindow = alertDialog.setView(layout)
                .setTitle("请选择您的捐赠方式")
                .setNegativeButton("捐赠列表", (dialog, which) -> {
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("正在获取捐赠信息...");
                    progressDialog.show();
                    if (Algorithm.isNetworkAvailable(MainActivity.this))
                        new Thread(() -> {
                            try {
                                String ret2 = null;
                                String Resultms = null;
                                String lines;
                                StringBuilder response = new StringBuilder();
                                HttpURLConnection connection = null;
                                try {
                                    URL url = new URL("https://adodoz.cn/ICMODManagerAPI.php");
                                    //连接服务器
                                    connection = (HttpURLConnection) url.openConnection();
                                    //上传服务器内容
                                    connection.setRequestMethod("POST");
                                    connection.setConnectTimeout(8000);
                                    connection.setDoInput(true);//允许输入
                                    connection.setDoOutput(true);//允许输出
                                    connection.setUseCaches(false);
                                    connection.setRequestProperty("Accept-Charset", "UTF-8");
                                    connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                                    connection.connect();
                                    DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                                    outStream.writeBytes("order=donate");
                                    outStream.flush();
                                    outStream.close();
                                    //读取响应
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    //读数据
                                    while ((lines = reader.readLine()) != null) {
                                        lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                                        response.append(lines);
                                    }
                                    ret2 = response.toString().trim();
                                    reader.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    if (connection != null) {
                                        connection.disconnect();
                                    }
                                }
                                Log.e("TAG", ret2 + "");
                                JSONArray jsonArray = new JSONArray(ret2);
                                List<String> donateList = new ArrayList<>();
                                if (jsonArray!=null) {
                                    Log.e("TAG", "aaaaaaa");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Log.e("TAG", i + "");
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        if (i == 0) {
                                            donateList.add(jsonObject.getString("name"));
                                        } else {
                                            donateList.add(jsonObject.getString("name") + " : " + jsonObject.getString("money"));
                                        }
                                    }
                                    MainActivity.this.runOnUiThread(() -> {

                                        LinearLayout layout2 = new LinearLayout(MainActivity.this);
                                        layout2.setLayoutParams(params);
                                        layout2.setGravity(Gravity.CENTER);
                                        layout2.setOrientation(LinearLayout.VERTICAL);
                                        ListView listView2 = new ListView(MainActivity.this);
                                        listView2.setPadding(10, 10, 10, 5);
                                        QQAdapter qqAdapter2 = new QQAdapter(MainActivity.this, R.layout.qqgroup, donateList);
                                        listView2.setAdapter(qqAdapter2);
                                        listView2.setDivider(new ColorDrawable(Color.alpha(0)));
                                        listView2.setDividerHeight(0);
                                        layout2.addView(listView2);
                                        AlertDialog alertDialog2 = new AlertDialog.Builder(MainActivity.this)
                                                .setView(layout2)
                                                .setTitle("感谢下列小伙伴的捐赠")
                                                .setPositiveButton("关闭", (dialogInterface, i) -> {

                                                }).create();
                                        alertDialog2.show();
                                        progressDialog.dismiss();
                                    });
                                } else {
                                    MainActivity.this.runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this, "获取捐赠信息失败", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                })
                .setPositiveButton("为什么？", (dialogInterface, i) -> {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("关于捐赠")
                            .setMessage("本软件开发者为在校学生，无收入来承受高昂的服务器费用\n如果您手头富足且愿意支持ICCN（InnerCore China）的发展，请助我们一臂之力\n毕竟用爱发电不是长久之计");
                    dialog.show();
                })
                .show();
        final AlertDialog finalAlertDialogWindow = alertDialogWindow;
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            switch (i) {
                case 0:
                    donateAlipay();
                    finalAlertDialogWindow.dismiss();
                    break;
                case 1:
                    donateWeixin();
                    finalAlertDialogWindow.dismiss();
                    break;
            }

        });
        layout.addView(listView);

    }

    protected void getqqgroup_json() {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("获取群聊信息中...");
        progressDialog.show();
        FileDownloader.getImpl().create("https://adodoz.cn/QQGroup.json").setPath(FinalValuable.QQGroupJson)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        List<String> nameList = new ArrayList<>();
                        final List<String> urlList = new ArrayList<>();
                        try {
                            String nr = Algorithm.readFile(new File(FinalValuable.QQGroupJson));
                            JSONArray jsonArray = new JSONArray(nr);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                nameList.add(jsonObject.getString("name"));
                                urlList.add(jsonObject.getString("url"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        AlertDialog alertDialogWindow = null;
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        LinearLayout layout = new LinearLayout(MainActivity.this);
                        layout.setLayoutParams(params);
                        layout.setGravity(Gravity.CENTER);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        ListView listView = new ListView(MainActivity.this);
                        listView.setPadding(10, 10, 10, 5);
                        QQAdapter qqAdapter = new QQAdapter(MainActivity.this, R.layout.qqgroup, nameList);
                        listView.setAdapter(qqAdapter);
                        listView.setDivider(new ColorDrawable(Color.alpha(0)));
                        listView.setDividerHeight(0);
                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            String url = urlList.get(i);
                            joinQQGroup(url);
                        });
                        layout.addView(listView);
                        alertDialogWindow = alertDialog.setView(layout)
                                .setTitle("请选择您想加入的群聊")
                                .setNegativeButton("取消", (dialog, which) -> {
                                })
                                .show();
                        progressDialog.dismiss();
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        print("获取失败", Snackbar.LENGTH_LONG);
                        progressDialog.dismiss();
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }

    public boolean joinQQGroup(String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            print("未安装手机QQ或版本不支持", Snackbar.LENGTH_LONG);
            return false;
        }
    }

    class QQAdapter extends ArrayAdapter<String> {

        private int resourceId;

        QQAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;

        }


        @NotNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String fruit = getItem(position); //获取当前项的Fruit实例
            @SuppressLint("ViewHolder") View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            TextView fruitName = view.findViewById(R.id.qqtext);
            fruitName.setText(fruit);
            return view;
        }
    }

    class LocalMAPAdapter extends ArrayAdapter<MAP> {
        private List<MAP> mapList;
        private int resourceID;

        LocalMAPAdapter(@NonNull Context context, int resource, List<MAP> objects) {
            super(context, resource, objects);
            this.resourceID = resource;
            this.mapList = objects;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final MAP map = getItem(position);
            final View view;
            ViewHolderMAP viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(MainActivity.this).inflate(resourceID, parent, false);
                viewHolder = new ViewHolderMAP();
                viewHolder.cardView = view.findViewById(R.id.CardView);
                viewHolder.textView1 = view.findViewById(R.id.itemsettingTextView1);
                viewHolder.imageView = view.findViewById(R.id.itemsettingImageView1);
                viewHolder.button1 = view.findViewById(R.id.itemsettingButton1);
                viewHolder.button2 = view.findViewById(R.id.itemsettingButton2);
                viewHolder.button3 = view.findViewById(R.id.change);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolderMAP) view.getTag();
            }
            viewHolder.textView1.setText(getItem(position) == null ? "未知" : getItem(position).getName());
            if (map.getImagePath() != null) {
                Bitmap bitmap = lruCacheUtils.getPicFromMemory(map.getImagePath());
                if (bitmap != null)
                    viewHolder.imageView.setImageBitmap(bitmap);
                else
                    viewHolder.imageView.setImageBitmap(lruCacheUtils.getPicFromMemory("null"));
            } else viewHolder.imageView.setImageBitmap(lruCacheUtils.getPicFromMemory("null"));
            viewHolder.button1.setOnClickListener(v -> {
                String allInfo = "";
                if (map.getName() != null)
                    allInfo += "名称：" + map.getName() + "\n";
                else allInfo += "名称：未知\n";
                allInfo += "路径：" + map.getMapPath();
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("详细信息")
                        .setMessage(allInfo)
                        .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                alertDialog.show();
            });
            viewHolder.button2.setOnClickListener(v -> {
                final File f = new File(map.getMapPath());
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("将要删除地图：" + map.getName() + "，是否继续（该操作不可撤销）？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Algorithm.deleteFile(f);
                                if (!f.exists()) {
                                    print("已删除地图：" + map.getName(), Snackbar.LENGTH_SHORT);
                                    mapList.remove(position);
                                    notifyDataSetChanged();
                                }
                            }
                        })
                        .create();
                alertDialog.show();
            });
            viewHolder.button3.setOnClickListener(view1 -> {
                AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("是否移动此地图（" + getItem(position).getName() + "）至" + (type == FinalValuable.MCMAP ? "IC地图？" : "MC地图？"))
                        .setNegativeButton("取消", (dialog, which) -> {

                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final File mapFile = new File(getItem(position).getMapPath());
                                final File toFile = new File((type == FinalValuable.MCMAP ? FinalValuable.ICMAPDir : FinalValuable.MCMAPDir) + File.separator + mapFile.getName());
                                if (toFile.exists()) {
                                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("提示")
                                            .setMessage("目标路径已存在存档，是否覆盖？")
                                            .setNegativeButton("取消", (dialog, which) -> {

                                            })
                                            .setPositiveButton("确定", (dialogInterface1, i1) -> new Thread(() -> {
                                                handler.sendEmptyMessage(1);
                                                Algorithm.deleteFile(toFile);
                                                if (Algorithm.copyFolder(mapFile.toString(), toFile.toString()))
                                                    handler.sendEmptyMessage(5);
                                                else handler.sendEmptyMessage(6);
                                                handler.sendEmptyMessage(2);
                                            }).start())
                                            .create();
                                    alertDialog.show();
                                } else {
                                    new Thread(() -> {
                                        handler.sendEmptyMessage(1);
                                        if (Algorithm.copyFolder(mapFile.toString(), toFile.toString()))
                                            handler.sendEmptyMessage(5);
                                        else handler.sendEmptyMessage(6);
                                        handler.sendEmptyMessage(2);
                                    }).start();
                                }
                            }
                        })
                        .create();
                builder.show();
            });

            return view;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Nullable
        @Override
        public MAP getItem(int position) {
            return mapList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    class ViewHolderMAP {
        CardView cardView;
        TextView textView1;
        ImageView imageView;
        Button button1, button2, button3;
    }

    class OnlineMODAdapter extends ArrayAdapter<OnlineMOD> {
        private List<OnlineMOD> modList;
        private int resourceID;

        public OnlineMODAdapter(Context context, int resourcesID, List<OnlineMOD> objects) {
            super(context, resourcesID, objects);
            this.resourceID = resourcesID;
            this.modList = objects;
        }

        @Override
        public int getCount() {
            return modList.size();
        }

        @Nullable
        @Override
        public OnlineMOD getItem(int position) {
            return modList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final OnlineMOD mod = modList.get(position);
            final View view;
            final ViewHolderMOD viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(MainActivity.this).inflate(resourceID, parent, false);
                viewHolder = new ViewHolderMOD();
                viewHolder.cardView = view.findViewById(R.id.CardView);
                viewHolder.textView1 = view.findViewById(R.id.itemsettingTextView1);
                viewHolder.textView2 = view.findViewById(R.id.itemsettingTextView2);
                viewHolder.imageView = view.findViewById(R.id.itemsettingImageView1);
                viewHolder.button1 = view.findViewById(R.id.itemsettingButton1);
                viewHolder.button2 = view.findViewById(R.id.itemsettingButton2);
                viewHolder.aSwitch = view.findViewById(R.id.switch1);
                viewHolder.needInflate = false;
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolderMOD) view.getTag();
            }
            viewHolder.aSwitch.setVisibility(View.GONE);
            viewHolder.button1.setText("下载并安装");
            viewHolder.button2.setText("仅下载");
            viewHolder.textView1.setText(getItem(position).getName());
            viewHolder.textView2.setText(getItem(position).getDescribe());
            viewHolder.imageView.setTag(getItem(position).getImageUrl());
            viewHolder.imageView.setImageBitmap(lruCacheUtils.getPicFromMemory("null"));
            if (lruCacheUtils.getPicFromMemory(getItem(position).getImageUrl()) != null) {
                if (getItem(position).getImageUrl() == viewHolder.imageView.getTag())
                    viewHolder.imageView.setImageBitmap(lruCacheUtils.getPicFromMemory(getItem(position).getImageUrl()));
                else
                    viewHolder.imageView.setImageBitmap(lruCacheUtils.getPicFromMemory("null"));
            } else {
                viewHolder.imageView.setImageBitmap(lruCacheUtils.getPicFromMemory("null"));
                new Thread(() -> {
                    final Bitmap bitmap = Algorithm.getImageBitmapFromUrl(getItem(position).getImageUrl());
                    // 如果本地还没缓存该图片，就缓存
                    if (lruCacheUtils.getPicFromMemory(getItem(position).getImageUrl()) == null) {
                        lruCacheUtils.savePicToMemory(getItem(position).getImageUrl(), bitmap);
                    }

                    MainActivity.this.runOnUiThread(() -> {
                        if (viewHolder.imageView != null && bitmap != null && viewHolder.imageView.getTag().toString() == getItem(position).getImageUrl()) {
                            viewHolder.imageView.setImageBitmap(bitmap);
                        }
                    });

                }).start();
            }

            if (mod.getName() != null)
                viewHolder.textView1.setText(mod.getName());
            viewHolder.button1.setOnClickListener(v -> {
                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("下载正在开始...请稍等");
                progressDialog.setCancelable(false);
                progressDialog.show();
                Log.e("TAG", getItem(position).getModUrl());
                FileDownloader.getImpl().create(getItem(position).getModUrl()).setPath(FinalValuable.DownLoadPath + File.separator + getItem(position).getName() + ".icmod")
                        .setListener(new FileDownloadLargeFileListener() {
                            @Override
                            protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                            }

                            @Override
                            protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                                if (totalBytes == -1)
                                    progressDialog.setMessage("已下载：" + Algorithm.readableFileSize(soFarBytes));
                                else
                                    progressDialog.setMessage("已下载：" + Algorithm.readableFileSize(soFarBytes) + "  " + Algorithm.getPercent(soFarBytes, totalBytes));
                            }

                            @Override
                            protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                            }

                            @Override
                            protected void completed(BaseDownloadTask task) {
                                new Thread(() -> {
                                    if (Algorithm.installMOD(FinalValuable.DownLoadPath + File.separator + getItem(position).getName() + ".icmod")) {
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                print("安装成功", Snackbar.LENGTH_SHORT);
                                                progressDialog.dismiss();
                                                try {
                                                    modList.remove(position);
                                                    notifyDataSetChanged();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        });
                                    } else {
                                        progressDialog.dismiss();
                                        try {
                                            Algorithm.copyFile(FinalValuable.DownLoadPath + File.separator + getItem(position).getName() + ".icmod", Environment.getExternalStorageDirectory().toString() + File.separator + "Download" + File.separator + getItem(position).getName() + ".icmod");
                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    print("安装失败，已保存" + getItem(position).getName() + ".icmod在" + "Download目录下，请尝试手动安装", Snackbar.LENGTH_LONG);
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }).start();

                            }

                            @Override
                            protected void error(BaseDownloadTask task, Throwable e) {
                                progressDialog.dismiss();
                                print("下载失败", Snackbar.LENGTH_SHORT);
                                e.printStackTrace();
                            }

                            @Override
                            protected void warn(BaseDownloadTask task) {

                            }
                        }).start();
            });
            viewHolder.button2.setOnClickListener(v -> {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getItem(position).getModUrl())
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });

            return view;
        }
    }

    class LocalResAdapter extends ArrayAdapter<ResPack> {
        private List<ResPack> resPackList;
        private int resourceID;

        public LocalResAdapter(Context context, int resourcesID, List<ResPack> objects) {
            super(context, resourcesID, objects);
            this.resourceID = resourcesID;
            this.resPackList = objects;
        }

        public List<ResPack> getModList() {
            return resPackList;
        }

        @Override
        public int getCount() {
            return resPackList.size();
        }

        @Override
        public ResPack getItem(int position) {
            return resPackList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ResPack mod = resPackList.get(position);
            final View view;
            ViewHolderMOD viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(MainActivity.this).inflate(resourceID, parent, false);
                viewHolder = new ViewHolderMOD();
                viewHolder.cardView = view.findViewById(R.id.CardView);
                viewHolder.textView1 = view.findViewById(R.id.itemsettingTextView1);
                viewHolder.textView2 = view.findViewById(R.id.itemsettingTextView2);
                viewHolder.imageView = view.findViewById(R.id.itemsettingImageView1);
                viewHolder.button1 = view.findViewById(R.id.itemsettingButton1);
                viewHolder.button2 = view.findViewById(R.id.itemsettingButton2);
                viewHolder.aSwitch = view.findViewById(R.id.switch1);
                viewHolder.needInflate = false;
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolderMOD) view.getTag();
            }
            if (mod.getName() != null)
                viewHolder.textView1.setText(mod.getName());
            else viewHolder.textView1.setText("未知");
            if (mod.getPackId() != null)
                viewHolder.textView2.setText(mod.getPackId());
            else viewHolder.textView2.setText("未知");
            if (mod.getPackVersion() != null)
                viewHolder.textView2.setText(viewHolder.textView2.getText() + "-" + mod.getPackVersion());
            else viewHolder.textView2.setText(viewHolder.textView2.getText() + "-未知");
            if (mod.getImagePath() != null) {
                Bitmap bitmap = lruCacheUtils.getPicFromMemory(mod.getImagePath());
                if (bitmap != null)
                    viewHolder.imageView.setImageBitmap(bitmap);
                else
                    viewHolder.imageView.setImageBitmap(lruCacheUtils.getPicFromMemory("null"));
            } else viewHolder.imageView.setImageBitmap(lruCacheUtils.getPicFromMemory("null"));


            viewHolder.button1.setOnClickListener(v -> {
                String allInfo = "";
                if (mod.getName() != null)
                    allInfo += "名称：" + mod.getName() + "\n";
                else allInfo += "名称：未知\n";
                if (mod.getPackId() != null)
                    allInfo += "资源包ID：" + mod.getPackId() + "\n";
                else allInfo += "资源包ID：未知\n";
                if (mod.getPackVersion() != null)
                    allInfo += "资源包版本：" + mod.getPackVersion() + "\n";
                else allInfo += "资源包版本：未知\n";
                if (mod.getDescribe() != null)
                    allInfo += "资源包描述：" + mod.getDescribe() + "\n";
                else allInfo += "资源包描述：未知" + "\n";
                if (mod.getUuid() != null)
                    allInfo += "资源包UUID：" + mod.getUuid() + "\n";
                else allInfo += "资源包UUID：未知" + "\n";
                if (mod.getModuleType() != null)
                    allInfo += "模块类型：" + mod.getModuleType() + "\n";
                else allInfo += "模块类型：未知" + "\n";
                if (mod.getModuleVersion() != null)
                    allInfo += "模块版本：" + mod.getModuleVersion() + "\n";
                else allInfo += "模块版本：未知" + "\n";
                if (mod.getModuleDes() != null)
                    allInfo += "模块描述：" + mod.getModuleDes() + "\n";
                else allInfo += "模块描述：未知" + "\n";
                if (mod.getModuleUuid() != null)
                    allInfo += "模块UUID：" + mod.getModuleUuid();
                else allInfo += "模块UUID：未知";

                final String finalAllInfo = allInfo;
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("详细信息")
                        .setMessage(allInfo)
                        .setNegativeButton("关闭", (dialog, which) -> {

                        })
                        .setPositiveButton("复制信息", (dialogInterface, i) -> {
                            //获取剪贴板管理器：
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData mClipData = ClipData.newPlainText("Label", finalAllInfo);
                            cm.setPrimaryClip(mClipData);
                            print("已复制到粘贴板", Snackbar.LENGTH_SHORT);
                        })
                        .create();
                alertDialog.show();
            });
            viewHolder.button2.setOnClickListener(v -> {
                final File f = new File(mod.getResPath());
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("将要删除资源包：" + mod.getName() + "，是否继续（该操作不可撤销）？")
                        .setNegativeButton("取消", (dialog, which) -> {

                        })
                        .setPositiveButton("确定", (dialog, which) -> {
                            if (getItem(position).getEnabled()) {
                                    AlertDialog alertDialog2 = new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("警告")
                                            .setMessage("将会关闭所有已启用资源包，是否继续？")
                                            .setPositiveButton("是", (dialogInterface, i) -> {
                                                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                                                progressDialog.setCancelable(false);
                                                progressDialog.show();
                                                new Thread(() -> {
                                                    File icResDir = new File(FinalValuable.ICResDir);
                                                    for (int j = 0; j < resPackList.size(); j++) {
                                                        File file = new File(resPackList.get(j).getResPath() + File.separator + "enabled.txt");
                                                        if (file.exists())
                                                            Algorithm.deleteFile(file);
                                                    }
                                                    if (icResDir.exists())
                                                        Algorithm.deleteFile(icResDir);
                                                    Algorithm.deleteFile(f);
                                                    if (!f.exists()) {
                                                        Snackbar.make(findViewById(R.id.fab), "已删除资源包：" + mod.getName(), Snackbar.LENGTH_SHORT).show();
                                                    }
                                                    MainActivity.this.runOnUiThread(() -> {
                                                        localResAdapter = new LocalResAdapter(MainActivity.this, R.layout.mod_item, flashNativeRes(false));
                                                        listView.setAdapter(localResAdapter);
                                                        print("已禁用所有材质包，请重新手动启用", Snackbar.LENGTH_SHORT);
                                                        progressDialog.dismiss();
                                                    });
                                                }).start();


                                            })
                                            .setNegativeButton("否", (dialogInterface, i) -> {

                                            }).create();
                                    alertDialog2.show();
                            } else {
                                Algorithm.deleteFile(f);
                                if (!f.exists()) {
                                    Snackbar.make(findViewById(R.id.fab), "已删除资源包：" + mod.getName(), Snackbar.LENGTH_SHORT).show();
                                    resPackList.remove(position);
                                    notifyDataSetChanged();
                                }
                            }

                        })
                        .create();
                alertDialog.show();
            });
            viewHolder.aSwitch.setOnCheckedChangeListener(null);
            viewHolder.aSwitch.setChecked(mod.getEnabled());
            viewHolder.aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("请稍等...");
                    progressDialog.show();
                    new Thread(() -> {
                        File[] filelist = new File(getItem(position).getResPath()).listFiles();
                        Algorithm.copyFolder(getItem(position).getResPath(), FinalValuable.ICResDir);
                        getItem(position).setEnabled(true);
                        try {
                            new File(getItem(position).getResPath() + File.separator + "enabled.txt").createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        MainActivity.this.runOnUiThread(() -> {
                            print("已启用材质包：" + getItem(position).getName(), Snackbar.LENGTH_SHORT);
                            progressDialog.dismiss();
                        });
                    }).start();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                            .setMessage("警告")
                            .setMessage("将会关闭所有已启用资源包，是否继续？")
                            .setPositiveButton("是", (dialogInterface, i) -> {
                                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                new Thread(() -> {
                                    File icResDir = new File(FinalValuable.ICResDir);
                                    for (int j = 0; j < resPackList.size(); j++) {
                                        File file = new File(resPackList.get(j).getResPath() + File.separator + "enabled.txt");
                                        if (file.exists())
                                            Algorithm.deleteFile(file);
                                    }
                                    if (icResDir.exists())
                                        Algorithm.deleteFile(icResDir);
                                    MainActivity.this.runOnUiThread(() -> {
                                        localResAdapter = new LocalResAdapter(MainActivity.this, R.layout.mod_item, flashNativeRes(false));
                                        listView.setAdapter(localResAdapter);
                                        print("已禁用所有材质包，请重新手动启用", Snackbar.LENGTH_SHORT);
                                        progressDialog.dismiss();
                                    });
                                }).start();


                            })
                            .setNegativeButton("否", (dialogInterface, i) -> {

                            }).create();
                    alertDialog.show();
                }
            });
            return view;
        }
    }

    class LocalMODAdapter extends ArrayAdapter<MOD> {
        private List<MOD> modList;
        private int resourceID;

        public LocalMODAdapter(Context context, int resourcesID, List<MOD> objects) {
            super(context, resourcesID, objects);
            this.resourceID = resourcesID;
            this.modList = objects;
        }

        public List<MOD> getModList() {
            return modList;
        }

        @Override
        public int getCount() {
            return modList.size();
        }

        @Override
        public MOD getItem(int position) {
            return modList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final MOD mod = modList.get(position);
            final View view;
            ViewHolderMOD viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(MainActivity.this).inflate(resourceID, parent, false);
                viewHolder = new ViewHolderMOD();
                viewHolder.cardView = view.findViewById(R.id.CardView);
                viewHolder.textView1 = view.findViewById(R.id.itemsettingTextView1);
                viewHolder.textView2 = view.findViewById(R.id.itemsettingTextView2);
                viewHolder.imageView = view.findViewById(R.id.itemsettingImageView1);
                viewHolder.button1 = view.findViewById(R.id.itemsettingButton1);
                viewHolder.button2 = view.findViewById(R.id.itemsettingButton2);
                viewHolder.aSwitch = view.findViewById(R.id.switch1);
                viewHolder.needInflate = false;
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolderMOD) view.getTag();
            }
            if (mod.getName() != null)
                viewHolder.textView1.setText(mod.getName());
            else viewHolder.textView1.setText("未知");
            if (mod.getAuthor() != null)
                viewHolder.textView2.setText(mod.getAuthor());
            else viewHolder.textView2.setText("未知");
            if (mod.getVersion() != null)
                viewHolder.textView2.setText(viewHolder.textView2.getText() + "-" + mod.getVersion());
            else viewHolder.textView2.setText(viewHolder.textView2.getText() + "-未知");
            if (mod.getImagePath() != null) {
                Bitmap bitmap = lruCacheUtils.getPicFromMemory(mod.getImagePath());
                if (bitmap != null)
                    viewHolder.imageView.setImageBitmap(bitmap);
                else
                    viewHolder.imageView.setImageBitmap(lruCacheUtils.getPicFromMemory("null"));
            } else viewHolder.imageView.setImageBitmap(lruCacheUtils.getPicFromMemory("null"));
            viewHolder.button1.setOnClickListener(v -> {
                String allInfo = "";
                if (mod.getName() != null)
                    allInfo += "名称：" + mod.getName() + "\n";
                else allInfo += "名称：未知\n";
                if (mod.getAuthor() != null)
                    allInfo += "作者：" + mod.getAuthor() + "\n";
                else allInfo += "作者：未知\n";
                if (mod.getVersion() != null)
                    allInfo += "版本：" + mod.getVersion() + "\n";
                else allInfo += "版本：未知\n";
                allInfo += "路径：" + mod.getModPath() + "\n";
                if (mod.getDescribe() != null)
                    allInfo += "描述：" + mod.getDescribe();
                else allInfo += "描述：未知";

                final String finalAllInfo = allInfo;
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("详细信息")
                        .setMessage(allInfo)
                        .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("复制信息", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //获取剪贴板管理器：
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData mClipData = ClipData.newPlainText("Label", finalAllInfo);
                                cm.setPrimaryClip(mClipData);
                                print("已复制到粘贴板", Snackbar.LENGTH_SHORT);
                            }
                        })
                        .create();
                alertDialog.show();
            });
            viewHolder.button2.setOnClickListener(v -> {
                final File f = new File(mod.getModPath());
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("将要删除MOD：" + mod.getName() + "，是否继续（该操作不可撤销）？")
                        .setNegativeButton("取消", (dialog, which) -> {

                        })
                        .setPositiveButton("确定", (dialog, which) -> {
                            Algorithm.deleteFile(f);
                            if (!f.exists()) {
                                Snackbar.make(findViewById(R.id.fab), "已删除MOD：" + mod.getName(), Snackbar.LENGTH_SHORT).show();
                                modList.remove(position);
                                notifyDataSetChanged();
                            }
                        })
                        .create();
                alertDialog.show();
            });
            viewHolder.aSwitch.setOnCheckedChangeListener(null);
            viewHolder.aSwitch.setChecked(mod.getEnabled());
            viewHolder.aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (mod.changeMOD()) {
                        Snackbar.make(fab, "已启用该MOD", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    if (mod.changeMOD()) {
                        Snackbar.make(fab, "已禁用该MOD", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
            return view;
        }
    }

    class ViewHolderMOD {
        CardView cardView;
        TextView textView1, textView2;
        ImageView imageView;
        Button button1, button2;
        Switch aSwitch;
        boolean needInflate;
    }
}

package icmod.wvt.com.icmod;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.*;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.json.JSONObject;

import icmod.wvt.com.icmod.others.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static FloatingActionButton fab;
    TextView mainText;
    static TextView sidebarUserName;
    String path;
    ListView listView;
    static ImageView sidebarImageView;
    LocalMODAdapter localMODAdapter;
    LocalMAPAdapter localMAPAdapter;
    ProgressDialog pDialog;
    private LruCacheUtils lruCacheUtils;
    int type;
    File modP, mapP;
    Toolbar toolbar;
    static Boolean haveUserData = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        modP = new File(FinalValuable.MODDir);
        if (!modP.exists()) modP.mkdirs();
        super.onCreate(savedInstanceState);
        Intent intentGet = getIntent();
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        type = FinalValuable.MOD;
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                listView.setPadding(10, 10, 10, 5);
                final File defPath = new File(Environment.getExternalStorageDirectory().toString());

                final List<String> fileList = Algorithm.orderByName(defPath.toString());
                listView.setAdapter(new FileAdapter(MainActivity.this, R.layout.select_item, fileList, defPath, textView));
                alertDialogWindow = alertDialog.setView(layout)
                        .setTitle("请选择一个文件")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
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
                                fileList2 = Algorithm.orderByName(defPath2.toString());
                                listView.setAdapter(new FileAdapter(MainActivity.this, R.layout.select_item, fileList2, defPath2, textView));
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
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    handler.sendEmptyMessage(1);
                                                    if (Algorithm.installMOD(path))
                                                        handler.sendEmptyMessage(3);
                                                    else handler.sendEmptyMessage(4);
                                                    handler.sendEmptyMessage(2);
                                                }
                                            }
                                            ).start();
                                            break;
                                        case FinalValuable.MCMAP:
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    handler.sendEmptyMessage(1);
                                                    if (Algorithm.installMAP(path, FinalValuable.MCMAPDir))
                                                        handler.sendEmptyMessage(3);
                                                    else handler.sendEmptyMessage(4);
                                                    handler.sendEmptyMessage(2);
                                                }
                                            }
                                            ).start();
                                            break;
                                        case FinalValuable.ICMAP:
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    handler.sendEmptyMessage(1);
                                                    if (Algorithm.installMAP(path, FinalValuable.ICMAPDir))
                                                        handler.sendEmptyMessage(3);
                                                    else handler.sendEmptyMessage(4);
                                                    handler.sendEmptyMessage(2);
                                                }
                                            }
                                            ).start();
                                            break;
                                    }
                                    finalAlertDialogWindow.dismiss();
                                }
                            } catch (ZipException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                layout.addView(textView);
                layout.addView(listView);
            }
        });
        lruCacheUtils = new LruCacheUtils(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        sidebarImageView = headerView.findViewById(R.id.sidebarImageView);
        sidebarUserName = headerView.findViewById(R.id.userName);
        FileDownloader.setup(this);
        listView = findViewById(R.id.mainListView);
        localMODAdapter = new LocalMODAdapter(this, R.layout.mod_item, flashNativeMOD());
        listView.setAdapter(localMODAdapter);
        if (!Algorithm.isAvilible(MainActivity.this, "com.mojang.minecraftpe")) {
            AlertDialog alertDialog1 = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("警告")
                    .setMessage("检测到您未安装Minecraft国际版，不能正常进入InnerCore，是否跳转去下载伪装软件？（安装Minecraft原版需要卸载伪装）")
                    .setNegativeButton("不用了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setPositiveButton("去下载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://adodoz.cn/MinecraftCamouflage.apk")
                            );
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .create();
            alertDialog1.show();
        }
        if (!Algorithm.isAvilible(MainActivity.this, "com.zhekasmirnov.innercore")) {
            AlertDialog alertDialog2 = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("警告")
                    .setMessage("检测到您未安装InnerCore主程序，是否跳转去下载主程序？")
                    .setNegativeButton("不用了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setPositiveButton("去下载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.coolapk.com/game/com.zhekasmirnov.innercore")
                            );
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .create();
            alertDialog2.show();
        }
        Bitmap bitmap = Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.no_logo);
        lruCacheUtils.savePicToMemory("null", bitmap);
        flashUser();
    }

    public static void flashUser()
    {
        try {
            File userData = new File(FinalValuable.UserInfo);
            if (userData.exists()) {
                haveUserData = true;
                JSONObject json = new JSONObject(Algorithm.readFile(userData));
                JSONObject userInfo = json.getJSONObject("user_info");
                sidebarUserName.setText("欢迎您，" + userInfo.getString("user_name"));
                load_avatar loadAvatar = new load_avatar(userInfo.getString("user_avatar"));
                loadAvatar.execute();
            } else {
                haveUserData = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class load_avatar extends AsyncTask<Void, Void, Bitmap>
    {
        String url;
        load_avatar(String url)
        {
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap ret = getImageBitmap(url);
            return ret;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            sidebarImageView.setImageBitmap(bitmap);
        }

        public Bitmap getImageBitmap(String url) {
            Bitmap bitmap = null;
            URL imgUrl = null;
            try {
                imgUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) imgUrl
                        .openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }


    public void sidebarImageViewClick(View view){
        if (haveUserData) {
            try{
                final File userData = new File(FinalValuable.UserInfo);
                JSONObject json = new JSONObject(Algorithm.readFile(userData));
                JSONObject userInfo = json.getJSONObject("user_info");
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("用户信息")
                        .setMessage("ID：" + userInfo.getInt("user_id") + "\n名称：" + userInfo.getString("user_name"))
                        .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("注销登录", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sidebarImageView.setImageBitmap(Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.baseline_account_circle_black_24dp));
                                sidebarUserName.setText("点击头像以登录");
                                Algorithm.deleteFile(userData);
                                haveUserData = false;
                            }
                        })
                        .create();
                alertDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else {
            Intent loginActivity = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginActivity);
        }
    }

    public static MessageCallback messageCallback = new MessageCallback() {
        @Override
        public void onMessage(String message) {
            if (message.equals("ok")){
                flashUser();
            }
            else {
                print("登陆失败惹！", Snackbar.LENGTH_LONG);
            }
        }
    };
    public interface MessageCallback{
        public void onMessage(String message);
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

        public FileAdapter(Context context, int resource, List<String> fileList, File fileDir, TextView textView2) {
            super(context, resource, fileList);
            this.fileList = fileList;
            this.resourceId = resource;
            this.textView = textView;
            this.fileDir = fileDir;
            textView2.setText(fileDir.toString());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            File file = new File(fileDir.toString() + File.separator + getItem(position));
            View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            ImageView image = view.findViewById(R.id.seleteimage);
            TextView textView = view.findViewById(R.id.seletetext);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            Bitmap bitmap = (file.isDirectory() ? Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.round_folder_open_black_36dp) : Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.round_description_black_36dp));
            textView.setText(fileList.get(position));
            image.setImageBitmap(bitmap);
            return view;
        }
    }

    public List<MOD> flashNativeMOD() {
        List<MOD> ret = new ArrayList<MOD>();
        File modsFIle[] = modP.listFiles();
        if (modsFIle != null)
            for (int i = 0; i < modsFIle.length; i++) {
                if (modsFIle[i].isDirectory()) {
                    MOD mod = Algorithm.getNativeMODClass(modsFIle[i].toString());
                    if (mod != null)
                    {
                        ret.add(mod);
                        if(mod.getImagePath() != null)
                        {
                            Bitmap bitmap = Algorithm.getBitmap(mod.getImagePath());
                            lruCacheUtils.savePicToMemory(mod.getImagePath(), bitmap);
                        }
                    }
                }
            }
        return ret;
    }

    public List<MAP> flashNativeMAP() {
        List<MAP> ret = new ArrayList<MAP>();
        File mapsFIle[] = mapP.listFiles();
        if (mapsFIle != null)
            for (int i = 0; i < mapsFIle.length; i++) {
                if (mapsFIle[i].isDirectory()) {
                    MAP map = Algorithm.getNativeMAPClass(mapsFIle[i].toString(), type);
                    if (map != null)
                    {
                        ret.add(map);
                        if(map.getImagePath() != null)
                        {
                            Bitmap bitmap = Algorithm.getBitmap(map.getImagePath());
                            lruCacheUtils.savePicToMemory(map.getImagePath(), bitmap);
                        }
                    }
                }
            }
        return ret;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
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
                            localMODAdapter = new LocalMODAdapter(MainActivity.this, R.layout.mod_item, flashNativeMOD());
                            listView.setAdapter(localMODAdapter);
                            break;
                        case FinalValuable.MCMAP:
                        case FinalValuable.ICMAP:
                            localMAPAdapter = new LocalMAPAdapter(MainActivity.this, R.layout.map_item, flashNativeMAP());
                            listView.setAdapter(localMAPAdapter);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

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

    private void continueDownLoad(BaseDownloadTask task) {
        while (task.getSmallFileSoFarBytes() != task.getSmallFileTotalBytes()) {
            int percent = (int) ((double) task.getSmallFileSoFarBytes() / (double) task.getSmallFileTotalBytes() * 100);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.addMod:
                type = FinalValuable.MOD;
                localMODAdapter = new LocalMODAdapter(this, R.layout.mod_item, flashNativeMOD());
                listView.setAdapter(localMODAdapter);
                toolbar.setTitle("安装MOD...");
                break;
            case R.id.addmcmap:
                type = FinalValuable.MCMAP;
                mapP = new File(FinalValuable.MCMAPDir);
                localMAPAdapter = new LocalMAPAdapter(this, R.layout.map_item, flashNativeMAP());
                listView.setAdapter(localMAPAdapter);
                toolbar.setTitle("安装MC地图...");
                break;
            case R.id.addicmap:
                type = FinalValuable.ICMAP;
                mapP = new File(FinalValuable.ICMAPDir);
                localMAPAdapter = new LocalMAPAdapter(this, R.layout.map_item, flashNativeMAP());
                listView.setAdapter(localMAPAdapter);
                toolbar.setTitle("安装IC地图...");
                break;
            case R.id.gotoweb:
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://adodoz.cn")
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
//            case R.id.onlineDown:
//
//                break;
            case R.id.reload:
                switch (type) {
                    case FinalValuable.MOD:
                        localMODAdapter = new LocalMODAdapter(MainActivity.this, R.layout.mod_item, flashNativeMOD());
                        listView.setAdapter(localMODAdapter);
                        break;
                    case FinalValuable.MCMAP:
                    case FinalValuable.ICMAP:
                        localMAPAdapter = new LocalMAPAdapter(this, R.layout.map_item, flashNativeMAP());
                        listView.setAdapter(localMAPAdapter);
                        break;
                }

                break;
            case R.id.openic:
                if(!Algorithm.openApp("com.zhekasmirnov.innercore", MainActivity.this))
                    print("进入失败", Snackbar.LENGTH_SHORT);
                break;
            case R.id.openmc:
                if(!Algorithm.openApp("com.mojang.minecraftpe", MainActivity.this))
                    print("进入失败", Snackbar.LENGTH_SHORT);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class LocalMAPAdapter extends ArrayAdapter<MAP> {
        private List<MAP> mapList;
        private int resourceID;

        public LocalMAPAdapter(@NonNull Context context, int resource, List<MAP> objects) {
            super(context, resource, objects);
            this.resourceID = resource;
            this.mapList = objects;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final MAP map = (MAP) getItem(position);
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
            viewHolder.button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                }
            });
            viewHolder.button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
//                                        deletePattern(getView(position, view, parent), position, modList,localMODAdapter);
                                        mapList.remove(position);
                                        localMAPAdapter.notifyDataSetChanged();
//                                        localMODAdapter = new LocalMODAdapter(MainActivity.this, R.layout.mod_item, flashNativeMOD());
//                                        listView.setAdapter(localMODAdapter);
                                    }
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            });
            viewHolder.button3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("是否移动此地图（" + getItem(position).getName() + "）至" + (type == FinalValuable.MCMAP ? "IC地图？" : "MC地图？"))
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
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
                                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                handler.sendEmptyMessage(1);
                                                                Algorithm.deleteFile(toFile);
                                                                if (Algorithm.copyFolder(mapFile.toString(), toFile.toString()))
                                                                    handler.sendEmptyMessage(5);
                                                                else handler.sendEmptyMessage(6);
                                                                handler.sendEmptyMessage(2);
                                                            }
                                                        }).start();


                                                    }
                                                })
                                                .create();
                                        alertDialog.show();
                                    } else {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                handler.sendEmptyMessage(1);
                                                if (Algorithm.copyFolder(mapFile.toString(), toFile.toString()))
                                                    handler.sendEmptyMessage(5);
                                                else handler.sendEmptyMessage(6);
                                                handler.sendEmptyMessage(2);
                                            }
                                        }).start();
                                    }
                                }
                            })
                            .create();
                    builder.show();
                }
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
            viewHolder.button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                }
            });
            viewHolder.button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final File f = new File(mod.getModPath());
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("将要删除MOD：" + mod.getName() + "，是否继续（该操作不可撤销）？")
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
                                        Snackbar.make(findViewById(R.id.fab), "已删除MOD：" + mod.getName(), Snackbar.LENGTH_SHORT).show();
//                                        deletePattern(getView(position, view, parent), position, modList,localMODAdapter);
                                        modList.remove(position);
                                        localMODAdapter.notifyDataSetChanged();
//                                        localMODAdapter = new LocalMODAdapter(MainActivity.this, R.layout.mod_item, flashNativeMOD());
//                                        listView.setAdapter(localMODAdapter);
                                    }
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            });
            viewHolder.aSwitch.setOnCheckedChangeListener(null);
            viewHolder.aSwitch.setChecked(mod.getEnabled());
            viewHolder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (mod.changeMOD()) {
                            Snackbar.make(fab, "已启用该MOD", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        if (mod.changeMOD()) {
                            Snackbar.make(fab, "已禁用该MOD", Snackbar.LENGTH_SHORT).show();
                        }
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

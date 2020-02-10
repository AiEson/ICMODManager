package icmod.wvt.com.icmod.others;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Environment;
import android.util.Log;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icmod.wvt.com.icmod.MainActivity;

import static android.text.TextUtils.isEmpty;

public class Algorithm {

    //字节转换
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static void writeFile(String fp, String nr) throws IOException {
        File wj = new File(fp);
        wj.getParentFile().mkdirs();
        if (!wj.exists())
            wj.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(wj));
        writer.write(nr);
        writer.close();
    }

    public static String getPercent(long y, long z) {
        String baifenbi = "";// 接受百分比的值
        double baiy = y * 1.0;
        double baiz = z * 1.0;
        double fen = baiy / baiz;
// NumberFormat nf = NumberFormat.getPercentInstance();注释掉的也是一种方法
// nf.setMinimumFractionDigits( 2 ); 保留到小数点后几位
        DecimalFormat df1 = new DecimalFormat("##.00%");
// ##.00%
// 百分比格式，后面不足2位的用0补齐
// baifenbi=nf.format(fen);
        baifenbi = df1.format(fen);
        System.out.println(baifenbi);
        return baifenbi;
    }

    public static int getVersionCode(Context context)//获取版本号(内部识别号)
    {
        try {
            PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static String getEncoding(String path) throws Exception {
        String encoding = "GBK";
        ZipFile zipFile = new ZipFile(path);
        zipFile.setCharset(Charset.forName(encoding));
        List<FileHeader> list = zipFile.getFileHeaders();
        for (int i = 0; i < list.size(); i++) {
            FileHeader fileHeader = list.get(i);
            String fileName = fileHeader.getFileName();
            if (isMessyCode(fileName)) {
                encoding = "UTF-8";
                break;
            }
        }
        return encoding;
    }

    private static boolean isMessyCode(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 当从Unicode编码向某个字符集转换时，如果在该字符集中没有对应的编码，则得到0x3f（即问号字符?）
            // 从其他字符集向Unicode编码转换时，如果这个二进制数在该字符集中没有标识任何的字符，则得到的结果是0xfffd
            if ((int) c == 0xfffd) {
                // 存在乱码
                return true;
            }
        }
        return false;
    }

    public static boolean installResPack() {

        return false;
    }

    public static String Post(String string, String get, Context context) {
        String html = "";
        try {
            String urldizhi = get; //请求地址
            URL url = new URL(urldizhi);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(50000);//超时时间
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
//      conn.setRequestProperty("User-Agent", Other.getUserAgent(context));
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(string);
            out.flush();
            out.close();

            InputStream inputStream = conn.getInputStream();
            byte[] data = StreamTool.read(inputStream);
            html = new String(data, StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.out.println("-----" + e);
            String string2 = "{\"success\":-1}";

            return string2;
        }
        return html;
    }

    public static Bitmap getImageBitmapFromUrl(String url) {
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

    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        return networkinfo != null && networkinfo.isAvailable();
    }

    public static boolean emailFormat(String email) {
        boolean tag = true;
        final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        final Pattern pattern = Pattern.compile(pattern1);
        final Matcher mat = pattern.matcher(email);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    public static MAP getNativeMAPClass(String path, int type) {
        MAP ret = null;
        File mappath = new File(path);
        if (mappath.isDirectory()) {
            String retName = null, retImage = null;
            File image = new File(path + File.separator + "world_icon.jpeg");
            File nameFile = new File(path + File.separator + "levelname.txt");
            if (image.exists())
                retImage = image.toString();
            if (nameFile.exists()) {
                try {
                    retName = readFile(nameFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ret = new MAP(retName, path, retImage, type);
        }
        return ret;
    }

    public static ResPack getNativeResClass(String path) {
        ResPack ret = null;
        File mappath = new File(path);
        if (mappath.isDirectory()) {
            boolean enabled = false;
            String name = null, imagePath = null, describe = null, uuid = null, packId = null, resPath = path, packVersion = null, moduleDes = null, moduleVersion = null, moduleUuid = null, moduleType = null;
            JSONObject info = null;
            File image = new File(path + File.separator + "pack_icon.png");
            File infoFile = new File(path + File.separator + "pack_manifest.json");
            if (!infoFile.exists())
                infoFile = new File(path + File.separator + "manifest.json");
            if (image.exists())
                imagePath = image.toString();
            enabled = new File(path + File.separator + "enabled.txt").exists();
            if (infoFile.exists()) {
                try {
                    info = new JSONObject(readFile(infoFile));
                    JSONArray modules = null;
                    JSONObject modulesObj = null;
                    JSONObject header = info.getJSONObject("header");
                    if (!header.isNull("pack_id"))
                        packId = header.getString("pack_id");
                    if (!header.isNull("name"))
                        name = header.getString("name");

                    if (!header.isNull("uuid"))
                        uuid = header.getString("uuid");

                    if (!header.isNull("packs_version"))
                        packVersion = header.getString("packs_version");

                    if (!header.isNull("description"))
                        describe = header.getString("description");

                    if (!header.isNull("modules"))
                        modules = header.getJSONArray("modules");
                    if (modules != null && modules.length() != 0) {
                        modulesObj = modules.getJSONObject(0);
                    }
                    if (modulesObj!= null && !modulesObj.isNull("description"))
                        moduleDes = modulesObj.getString("description");

                    if (modulesObj!= null && !modulesObj.isNull("version"))
                        moduleVersion = modulesObj.getString("version");
                    if (modulesObj!= null && !modulesObj.isNull("uuid"))
                        moduleUuid = modulesObj.getString("uuid");
                    if (modulesObj!= null && !modulesObj.isNull("type"))
                        moduleType = modulesObj.getString("type");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ret = new ResPack(name, imagePath, describe, uuid, packId, resPath, packVersion, moduleDes, moduleVersion, moduleUuid,
                    moduleType, enabled);
        }
        return ret;
    }

    //通过文件路径获取MOD类
    public static MOD getNativeMODClass(String path) {
        MOD ret = null;
        File modpath = new File(path);
        if (modpath.isDirectory()) {
            String retPath = path, retImage = null, retName = null, retVerison = null, retDescribe = null,
                    retAuthor = null;
            Boolean retEnabled = false;
            File image = new File(path + File.separator + "mod_icon.png");
            File modinfo = new File(path + File.separator + "mod.info");
            File config = new File(path + File.separator + "config.json");
            if (image.exists())
                retImage = image.toString();
            if (modinfo.exists()) {
                JSONObject modInfoJson = null;
                try {
                    modInfoJson = new JSONObject(readFile(modinfo));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (modInfoJson != null) {
                        if (!modInfoJson.isNull("name"))
                            retName = modInfoJson.getString("name");
                        else retName = modpath.getName();
                        if (!modInfoJson.isNull("author"))
                            retAuthor = modInfoJson.getString("author");
                        else retAuthor = "未知";
                        if (!modInfoJson.isNull("version"))
                            retVerison = modInfoJson.getString("version");
                        else retVerison = "未知";
                        if (!modInfoJson.isNull("description"))
                            retDescribe = modInfoJson.getString("description");
                        else retDescribe = "未知";
                    } else {
                        retName = modpath.getName();
                        retAuthor = "未知";
                        retVerison = "未知";
                        retDescribe = "未知";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            if (config.exists()) {
                try {
                    JSONObject configJson = new JSONObject(readFile(config));
                    if (!configJson.isNull("enabled"))
                        retEnabled = configJson.getBoolean("enabled");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ret = new MOD(retPath, retName, retImage, retVerison, retDescribe, retAuthor, retEnabled, FinalValuable.MOD);
        }
        return ret;
    }

    //改变MOD的启动状态
    public static boolean changeMOD(String path, boolean zt) {
        boolean ret = false;
        File config = new File(path + File.separator + "config.json");
        if (config.exists()) {
            try {
                JSONObject nr = new JSONObject(getStringNoBlank(readFile(config)));
                nr.put("enabled", zt);
                writeFile(config.toString(), nr.toString());
                ret = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                config.createNewFile();
                JSONObject nr = new JSONObject("{}");
                nr.put("enabled", zt);
                writeFile(config.toString(), nr.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    private static int mFileSize = 0;

    private static boolean sortFolder(String path, List<String> mPathString) {
        if (path == null || isEmpty(path))
            return false;
        File[] fileList = null;
        File file = new File(path);
        if (file.exists() == false) {
            file.mkdir();
        }
        if (!file.exists() || (file.isDirectory() && (file.listFiles().length == 0))) {
            return true;
        } else {
            fileList = file.listFiles();
            mFileSize = file.listFiles().length;
            mPathString.clear();
            if (mFileSize > 0) {
                for (int i = 0; i < mFileSize; i++) {
                    mPathString.add(fileList[i].getAbsolutePath());
                }
                Collections.sort(mPathString);
            }
            return false;
        }
    }

    public static int dp2px(Context context, int value) {
        float v = context.getResources().getDisplayMetrics().density;
        return (int) (v * value + 0.5f);
    }

    public static ArrayList<String> orderByName(String filePath) {
        ArrayList<String> FileNameList = new ArrayList<String>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        List fileList = Arrays.asList(files);
        Collections.sort(fileList, (Comparator<File>) (o1, o2) -> {
            if (o1.isDirectory() && o2.isFile())
                return -1;
            if (o1.isFile() && o2.isDirectory())
                return 1;
            return o1.getName().compareTo(o2.getName());
        });
        if (!filePath.equals(Environment.getExternalStorageDirectory().toString())) {
            FileNameList.add("..");
        } else {
            FileNameList.add("系统浏览器下载");
            if (isAvilible(MainActivity.getContext(), "com.tencent.mobileqq") || isAvilible(MainActivity.getContext(), "com.tencent.qqlite") ||
                    isAvilible(MainActivity.getContext(), "com.tencent.mobileqqi") || isAvilible(MainActivity.getContext(), "com.tencent.minihd.qq"))
                FileNameList.add("QQ下载");
            if (isAvilible(MainActivity.getContext(), "com.tencent.mm"))
                FileNameList.add("微信下载");
            if (isAvilible(MainActivity.getContext(), "com.baidu.netdisk"))
                FileNameList.add("百度网盘下载");
        }
        for (File file1 : files) {
            FileNameList.add(file1.getName());
        }
        return FileNameList;
    }

    public static boolean openApp(String packageName, Context context) {
        boolean ret = false;
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<>();

        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        // 判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }

    public static Bitmap getBitmapFromRes(Context context, int res) {
        Resources r = context.getResources();
        InputStream is = r.openRawResource(res);
        BitmapDrawable bmpDraw = new BitmapDrawable(is);
        Bitmap bmp = bmpDraw.getBitmap();
        return bmp;
    }

    public static String readFile(File f) throws IOException {
        FileReader fre = new FileReader(f);
        BufferedReader bre = new BufferedReader(fre);
        String str = "", Strret = "";
        while ((str = bre.readLine()) != null) {
            Strret += str;
        }
        bre.close();
        fre.close();
        return Strret;
    }

    public static void deleteFile(File file) {
        // 判断传递进来的是文件还是文件夹,如果是文件,直接删除,如果是文件夹,则判断文件夹里面有没有东西
        if (file.isDirectory()) {
            // 如果是目录,就删除目录下所有的文件和文件夹
            File[] files = file.listFiles();
            // 遍历目录下的文件和文件夹
            for (File f : files) {
                // 如果是文件,就删除
                if (f.isFile()) {
//                    System.out.println("已经被删除的文件:" + f);
                    // 删除文件
                    f.delete();
                } else if (file.isDirectory()) {
                    // 如果是文件夹,就递归调用文件夹的方法
                    deleteFile(f);
                }
            }
            // 删除文件夹自己,如果它低下是空的,就会被删除
//            System.out.println("已经被删除的文件夹:" + file);
            file.delete();
            return;// 文件夹被删除后,直接用return语句结束当次递归调用
        }

        // 如果是文件,就直接删除自己
//        System.out.println("已经被删除的文件:" + file);
        file.delete();

    }

    //复制到粘贴板
    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        systemService.setPrimaryClip(ClipData.newPlainText("ICMOD管理器", text));
    }

    //验证某目录和子目录是否存在指定文件
    protected static int isExFile(String path, String filename) {
        int ret = 0;
        File MODPath = new File(path + File.separator + filename);

        if (MODPath.exists()) {
            ret = 1;
        } else if (new File(FinalValuable.MODTestDir + File.separator + filename).exists()) {
            ret = 3;
        } else if (new File(path + File.separator + Objects.requireNonNull(new File(path).list())[0] + File.separator + filename).exists()) {
            ret = 2;
        }
        return ret;
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     * */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    //安装MOD，需要在多线程运行
    public static boolean installMOD(String file) {
        boolean ret = false;
        try {
            unZip(file, FinalValuable.MODTestDir, "");
            int modStatus = isExFile(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0], "build.config");
            if (modStatus == 1) {
                copyFolder(new File(FinalValuable.MODTestDir).toString(), new File(FinalValuable.MODDir).toString());
                ret = true;
            } else if (modStatus == 2) {
                copyFolder(new File(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0]).toString(), new File(FinalValuable.MODDir).toString());
                ret = true;
            } else if (modStatus == 3) {
                String fileName = getFileNameNoEx(new File(file).getName());
                File modTest = new File(FinalValuable.MODTestDir);
                File mbPath = new File(FinalValuable.MODDir + File.separator + fileName);
                mbPath.mkdirs();
                String[] list = modTest.list();
                for (int i = 0; i < list.length; i++) {
                    File file1 = new File(FinalValuable.MODTestDir + File.separator + list[i]);
                    if (file1.isDirectory()) {
                        copyFolder(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    } else {
                        copyFile(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    }
                }
                ret = true;
            } else {
                ret = false;
            }
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File testDir = new File(FinalValuable.MODTestDir);
        deleteFile(testDir);
        testDir.mkdirs();
        return ret;
    }

    public static boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            File newFile = new File(newPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }

            /* 如果不需要打log，可以使用下面的语句
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            */

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean installMAP(String file, String toFile) {
        boolean ret = false;
        try {
            unZip(file, FinalValuable.MODTestDir, "");
        } catch (ZipException e) {
            e.printStackTrace();
        }
        try {
            int modStatus = isExFile(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0], "level.dat");
            if (modStatus == 1) {
                copyFolder(new File(FinalValuable.MODTestDir).toString(), toFile);
                ret = true;
            } else if (modStatus == 2) {
                copyFolder(new File(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0]).toString(), toFile);
                ret = true;
            } else if (modStatus == 3) {
                String fileName = getFileNameNoEx(new File(file).getName());
                File modTest = new File(FinalValuable.MODTestDir);
                File mbPath = new File(toFile + File.separator + fileName);
                mbPath.mkdirs();
                String[] list = modTest.list();
                for (int i = 0; i < list.length; i++) {
                    File file1 = new File(FinalValuable.MODTestDir + File.separator + list[i]);
                    Log.e("TAG", file1.toString() + "  目标：   " + mbPath.toString());
                    if (file1.isDirectory()) {
                        copyFolder(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    } else {
                        copyFile(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    }
                }
                ret = true;
            } else {
                ret = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        File testDir = new File(FinalValuable.MODTestDir);
        deleteFile(testDir);
        testDir.mkdirs();
        return ret;
    }
    public static boolean installRes(String file, String toFile) {
        boolean ret = false;
        try {
            unZip(file, FinalValuable.MODTestDir, "");
        } catch (ZipException e) {
            e.printStackTrace();
        }
        try {
            int modStatus = isExFile(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0], "pack_manifest.json");
            if (modStatus == 0)
                modStatus = isExFile(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0], "manifest.json");
            if (modStatus == 1) {
                copyFolder(new File(FinalValuable.MODTestDir).toString(), toFile);
                ret = true;
            } else if (modStatus == 2) {
                copyFolder(new File(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0]).toString(), toFile);
                ret = true;
            } else if (modStatus == 3) {
                String fileName = getFileNameNoEx(new File(file).getName());
                File modTest = new File(FinalValuable.MODTestDir);
                File mbPath = new File(toFile + File.separator + fileName);
                mbPath.mkdirs();
                String[] list = modTest.list();
                for (int i = 0; i < list.length; i++) {
                    File file1 = new File(FinalValuable.MODTestDir + File.separator + list[i]);
                    Log.e("TAG", file1.toString() + "  目标：   " + mbPath.toString());
                    if (file1.isDirectory()) {
                        copyFolder(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    } else {
                        copyFile(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    }
                }
                ret = true;
            } else {
                ret = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        File testDir = new File(FinalValuable.MODTestDir);
        deleteFile(testDir);
        testDir.mkdirs();
        return ret;
    }

    public static boolean copyFolder(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e("--Method--", "copyFolder: cannot create directory.");
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.");
                    return false;
                } else if (!temp.isFile()) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.");
                    return false;
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.");
                    return false;
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getStringNoBlank(String str) {
        if (str != null && !"".equals(str)) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            String strNoBlank = m.replaceAll("");
            return strNoBlank;
        } else {
            return str;
        }
    }

    public static String getFileLastName(File file) {
        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return suffix;
    }

    public static void unZip(String zipfile, String dest, String passwd) throws ZipException {
        ZipFile zfile = new ZipFile(zipfile);
        try {
            zfile.setCharset(Charset.forName(getEncoding(zipfile)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!zfile.isValidZipFile()) {
            throw new ZipException("压缩文件不合法，可能已经损坏！");
        }

        File file = new File(dest);
        if (file.isDirectory() && !file.exists()) {
            file.mkdirs();
        }

        if (zfile.isEncrypted()) {
            zfile.setPassword(passwd.toCharArray());
        }
        zfile.extractAll(dest);
    }

    public static Bitmap getBitmap(String path) {
        FileInputStream fis;
        Bitmap ret = null;
        try {
            fis = new FileInputStream(path);
            ret = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }
}

package icmod.wvt.com.icmod.others;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icmod.wvt.com.icmod.MainActivity;
import icmod.wvt.com.icmod.R;

import static android.text.TextUtils.isEmpty;

public class Algorithm {
    public static void writeFile(String fp, String nr) throws IOException {
        File wj = new File(fp);
        if (!wj.exists())
            wj.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(wj));
        writer.write(nr);
        writer.close();
    }
    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }

        return true;
    }

    public static MAP getNativeMAPClass(String path, int type) {
        MAP ret = null;
        File mappath = new File(path);
        if (mappath.isDirectory())
        {
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
    //通过文件路径获取MOD类
    public static MOD getNativeMODClass(String path) {
        MOD ret = null;
        File modpath = new File(path);
        if (modpath.isDirectory())
        {
            String retPath = path, retImage = null, retName = null, retVerison = null, retDescribe = null,
                retAuthor = null;
            Boolean retEnabled = false;
            File image = new File(path + File.separator + "mod_icon.png");
            File modinfo = new File(path + File.separator + "mod.info");
            File config = new File(path + File.separator + "config.json");
            if (image.exists())
                retImage = image.toString();
            if (modinfo.exists())
            {
                try {
                    JSONObject modInfoJson = new JSONObject(readFile(modinfo));
                    if (!modInfoJson.isNull("name"))
                        retName = modInfoJson.getString("name");
                    if (!modInfoJson.isNull("author"))
                        retAuthor = modInfoJson.getString("author");
                    if (!modInfoJson.isNull("version"))
                        retVerison = modInfoJson.getString("version");
                    if (!modInfoJson.isNull("description"))
                        retDescribe = modInfoJson.getString("description");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (config.exists())
            {
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
            ret = new MOD(retPath, retName, retImage, retVerison, retDescribe,retAuthor, retEnabled, FinalValuable.MOD);
        }
        return ret;
    }
    //改变MOD的启动状态
    public static boolean changeMOD(String path, boolean zt) {
        boolean ret = false;
        File config = new File(path + File.separator + "config.json");
        if (config.exists())
        {
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
        }
        else
        {
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
    public static ArrayList<String> orderByName(String filePath) {
        ArrayList<String> FileNameList = new ArrayList<String>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        List fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        if (!filePath.equals("/"))
            FileNameList.add("..");
        for (File file1 : files) {
                FileNameList.add(file1.getName());
        }
        return FileNameList;
    }

    public static boolean openApp(String packageName, Context context) {
        boolean ret = false;
        try{
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
            ret = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }
    /**
     * 检查手机上是否安装了指定的软件
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

    public static Bitmap getBitmapFromRes(Context context, int res)
    {
        Resources r = context.getResources();
        InputStream is = r.openRawResource(res);
        BitmapDrawable bmpDraw = new BitmapDrawable(is);
        Bitmap bmp = bmpDraw.getBitmap();
        return bmp;
    }
    public static String readFile(File f) throws IOException {
        FileReader fre=new FileReader(f);
        BufferedReader bre=new BufferedReader(fre);
        String str="", Strret = "";
        while((str=bre.readLine())!=null)
        {
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

        if (MODPath.exists())
        {
            ret =  1;
        }
        else if (new File(path + File.separator +new File(path).list()[0] + File.separator + filename).exists()){
            Log.e("TAG", 2 + "");
                ret =  2;
        }
        return ret;
    }
    //安装MOD，需要在多线程运行
    public static boolean installMOD(String file) {
        boolean ret = false;
        try {
            unZip(file, FinalValuable.MODTestDir, "");
            int modStatus = isExFile(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0], "build.config");
            if (modStatus == 1)
            {
                copyFolder(new File(FinalValuable.MODTestDir).toString(), new File(FinalValuable.MODDir).toString());
                ret = true;
            }
            else if (modStatus == 2)
            {
               copyFolder(new File(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0]).toString(), new File(FinalValuable.MODDir).toString());
                ret = true;
            }
            else{
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
    public static boolean installMAP(String file, String toFile) {
        boolean ret = false;
        try {
            unZip(file, FinalValuable.MODTestDir, "");
            int modStatus = isExFile(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0], "level.dat");
            if (modStatus == 1)
            {
                copyFolder(new File(FinalValuable.MODTestDir).toString(), toFile);
                ret = true;
            }
            else if (modStatus == 2)
            {
                copyFolder(new File(FinalValuable.MODTestDir + File.separator + new File(FinalValuable.MODTestDir).list()[0]).toString(), toFile);
                ret = true;
            }
            else{
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

                /* 如果不需要打log，可以使用下面的语句
                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (temp.exists() && temp.isFile() && temp.canRead()) {
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
                 */
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void copy(File f, File nf, boolean flag) throws Exception {
    // 判断是否存在
    if (f.exists()) {
        // 判断是否是目录
        if (f.isDirectory()) {
            if (flag) {
                // 制定路径，以便原样输出
                nf = new File(nf + "/" + f.getName());
                // 判断文件夹是否存在，不存在就创建
                if (!nf.exists()) {
                    nf.mkdirs();
                }
            }
            flag = true;
            // 获取文件夹下所有的文件及子文件夹
            File[] l = f.listFiles();
            // 判断是否为null
            if (null != l) {
                for (File ll : l) {
                    // 循环递归调用
                    copy(ll, nf, flag);
                }
            }
        } else {
            // 获取输入流
            FileInputStream fis = new FileInputStream(f);
            // 获取输出流
            FileOutputStream fos = new FileOutputStream(nf + "/" + f.getName());
            byte[] b = new byte[1024];
            // 读取文件
            while (fis.read(b) != -1) {
                // 写入文件，复制
                fos.write(b);
            }
            fos.close();
            fis.close();
        }
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
        zfile.setFileNameCharset("UTF-8");//在GBK系统中需要设置
        if (!zfile.isValidZipFile())
        {
            throw new ZipException("压缩文件不合法，可能已经损坏！");
        }

        File file = new File(dest);
        if (file.isDirectory() && !file.exists())
        {
            file.mkdirs();
        }

        if (zfile.isEncrypted())
        {
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

package icmod.wvt.com.icmod;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.MOD;

import static icmod.wvt.com.icmod.others.Algorithm.getFileLastName;

public class OpenActivity extends AppCompatActivity {
    List<MOD> modList = new ArrayList<>();
    Boolean dirSiMOD = false;
    List<File>notModDir = new ArrayList<>();

    private static final int PERMISSION_REQUEST = 0xa00;
    // 声明一个数组，用来存储所有需要动态申请的权限
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE};
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();
    Context mContext;
    boolean mShowRequestPermission = true;//用户是否禁止权限

    class open_load extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                File testDir = new File(FinalValuable.MODTestDir);
                Algorithm.deleteFile(testDir);
                testDir.mkdirs();
                File allMod = new File(FinalValuable.MODDir);
                File allModList[] = allMod.listFiles();
                if (allModList  != null && allModList.length != 0) {
                    for (int i = 0; i < allModList.length; i++){
                        if (allModList[i].isDirectory()) {
                            File modDel = new File(allModList[i].toString() + File.separator + "build.config");
                            if (!modDel.exists())
                            {
                                notModDir.add(allModList[i]);
                            }
                        }
                        else {
                            if (!getFileLastName(allModList[i]).equals("js") || !getFileLastName(allModList[i]).equals("json")) {
                                Algorithm.deleteFile(allModList[i]);
                            }
                        }
                    }
                }

                Thread.sleep(550);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            Intent intent = new Intent(OpenActivity.this, MainActivity.class);
            if (!dirSiMOD)
            {
                intent.putExtra("notFileList", (Serializable) notModDir);
            }
            intent.putExtra("hasNoMod", notModDir.size() == 0);
//            Toast.makeText(OpenActivity.this, dirSiMOD + "", 1).show();
            if (new File(FinalValuable.NetModData).isFile())
                Algorithm.deleteFile(new File(FinalValuable.NetModData));
            startActivity(intent);
            OpenActivity.this.finish();
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_loading);
        mContext = this;
        checkPermission();
    }

    private void checkPermission() {
        mPermissionList.clear();
        /**
         * 判断哪些权限未授予
         * 以便必要的时候重新申请
         */
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
        /**
         * 判断存储委授予权限的集合是否为空
         */
        if (!mPermissionList.isEmpty()) {
            // 后续操作...
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST);
        } else {//未授予的权限为空，表示都授予了
            // 后续操作...
            open_load ol = new open_load();
            ol.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(OpenActivity.this, permissions[i]);
                        if (showRequestPermission) {
                            // 后续操作...
                            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST);
                        } else {
                            // 后续操作...
                            Toast.makeText(mContext, "感谢您的权限哦~", 1).show();

                        }
                    }
                }
                // 授权结束后的后续操作...
                Intent intent = new Intent(OpenActivity.this, MainActivity.class);
                if (!dirSiMOD)
                {
                    intent.putExtra("notFileList", (Serializable) notModDir);
                }
                intent.putExtra("hasNoMod", notModDir.size() == 0);
//            Toast.makeText(OpenActivity.this, dirSiMOD + "", 1).show();
                startActivity(intent);
                OpenActivity.this.finish();
                break;
            default:
                break;
        }
    }
}

package icmod.wvt.com.icmod.others;

import android.os.Environment;

import java.io.File;

public class FinalValuable {
    public static final int MOD = 1, MCMAP = 2,
        ICMAP = 3, TESTMAP = 23, RES = 4, Online = 5;
    public static final String MODDir = Environment.getExternalStorageDirectory().toString() + File.separator + "games" + File.separator + "com.mojang" + File.separator + "mods",
        MCMAPDir = Environment.getExternalStorageDirectory().toString() + File.separator + "games" + File.separator + "com.mojang" + File.separator + "minecraftWorlds",
        ICMAPDir = Environment.getExternalStorageDirectory().toString() + File.separator + "games" + File.separator + "com.mojang" + File.separator + "innercoreWorlds",
        WvTWorkDir = Environment.getExternalStorageDirectory() + File.separator + "WvT",
        MODTestDir = Environment.getExternalStorageDirectory() + File.separator + "WvT" + File.separator + "Test",
        MODDataPath = Environment.getExternalStorageDirectory() + File.separator + "WvT" + File.separator + "AllModInfo.json",
        DownLoadPath = Environment.getExternalStorageDirectory() + File.separator + "WvT" + File.separator + "Download",
        NetModData = DownLoadPath + File.separator + "NetModData.json";
}

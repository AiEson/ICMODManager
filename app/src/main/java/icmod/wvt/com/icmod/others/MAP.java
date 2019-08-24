package icmod.wvt.com.icmod.others;

public class MAP {
    private String name, imagePath, mapPath;
    private int type;
    MAP(String name, String mapPath, String imagePath, int type)
    {
        this.name = name;
        this.mapPath = mapPath;
        this.imagePath = imagePath;
        this.type = type;
    }
    public String getMapPath() {
        return mapPath;
    }
    public String getImagePath() {
        return imagePath;
    }
    public String getName() {
        return name;
    }
    public int getType() {
        return type;
    }
}

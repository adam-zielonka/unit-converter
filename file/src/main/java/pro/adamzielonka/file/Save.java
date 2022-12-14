package pro.adamzielonka.file;

import android.content.Context;
import android.os.Environment;

import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

public class Save {

    //region internal
    public static <T> void saveJSON(Context context, String fileName, T t) throws IOException {
        FileOutputStream out = context.openFileOutput(fileName, MODE_PRIVATE);
        out.write(FileTools.getGson().toJson(t).getBytes());
        out.close();
    }

    private static boolean isFileInternalExist(Context context, String fileName) {
        return context.getFileStreamPath(fileName).exists();
    }

    public static String getNewFileInternalName(Context context, String prefix, String name) {
        String fileName = prefix + name.toUpperCase() + Extension.JSON;
        for (int i = 1; isFileInternalExist(context, fileName); i++) {
            fileName = prefix + name.toUpperCase() + "_" + i + Extension.JSON;
        }
        return fileName;
    }
    //endregion

    //region external
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    //endregion
}

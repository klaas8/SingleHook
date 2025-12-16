package org.lsposed.hijack;

import android.app.Application;
import android.content.Context;
import java.io.File;
import java.util.NavigableSet;
import org.lsposed.hijack.util.AppUtil;
import org.mapdb.DBMaker;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppUtil.db = DBMaker.fileDB(new File(getFilesDir(), "config.db"))
            .fileMmapEnableIfSupported()
            .asyncWriteEnable()
            .asyncWriteFlushDelay(300)
            .asyncWriteQueueSize(100)
            .executorEnable()
            .make();
        AppUtil.CrashHandler.getInstance().registerPart(this);
    }
}
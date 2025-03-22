package uni.dcloud.io.uniplugin_richalert;

import android.app.Application;

import io.dcloud.feature.uniapp.UniAppHookProxy;


public class RichAlert_AppProxy implements UniAppHookProxy {
    @Override
    public void onCreate(Application application) {
        //可写初始化触发逻辑
    }

    @Override
    public void onSubProcessCreate(Application application) {
        //子进程初始化回调
    }
}

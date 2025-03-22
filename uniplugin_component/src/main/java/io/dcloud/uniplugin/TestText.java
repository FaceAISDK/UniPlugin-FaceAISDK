package io.dcloud.uniplugin;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

import io.dcloud.feature.uniapp.UniSDKInstance;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.ui.action.AbsComponentData;
import io.dcloud.feature.uniapp.ui.component.AbsVContainer;
import io.dcloud.feature.uniapp.ui.component.UniComponent;
import io.dcloud.feature.uniapp.ui.component.UniComponentProp;

public class TestText extends UniComponent<TextView> {

    public TestText(UniSDKInstance instance, AbsVContainer parent, AbsComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    @Override
    protected TextView initComponentHostView(Context context) {
        TextView textView = new TextView(context);
        textView.setTextSize(20);
        textView.setTextColor(Color.BLACK);
        return textView;
    }

    @UniComponentProp(name = "tel")
    public void setTel(String telNumber) {
        getHostView().setText("tel: " + telNumber);
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> number = new HashMap<>();
        number.put("tel", telNumber);
        //目前uni限制 参数需要放入到"detail"中 否则会被清理
        params.put("detail", number);
        fireEvent("onTel", params);
    }

    @UniJSMethod
    public void clearTel() {
        getHostView().setText("");
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
    }

    @Override
    public void onActivityPause() {
        super.onActivityPause();
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
    }
}

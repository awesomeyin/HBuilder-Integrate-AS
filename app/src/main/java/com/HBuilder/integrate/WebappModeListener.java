package com.HBuilder.integrate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import io.dcloud.RInformation;
import io.dcloud.common.DHInterface.IApp;
import io.dcloud.common.DHInterface.ICore;
import io.dcloud.common.DHInterface.IOnCreateSplashView;
import io.dcloud.common.DHInterface.IWebview;
import io.dcloud.common.DHInterface.IWebviewStateListener;
import io.dcloud.common.util.ImageLoaderUtil;
import io.dcloud.feature.internal.sdk.SDK;

/**
 * Created by along on 2018/8/1 15:40
 */

public class WebappModeListener implements ICore.ICoreStatusListener, IOnCreateSplashView {

    Activity activity;
    View splashView = null;
    ViewGroup rootView;
    IApp app = null;
    ProgressDialog pd = null;
    private int mSign = 0;

    public WebappModeListener(Activity activity,ViewGroup rootView,int sign) {
        this.activity = activity;
        this.rootView = rootView;
        this.mSign = sign;
    }

    /**
     * 5+内核初始化完成时触发
     */
    @Override
    public void onCoreInitEnd(ICore coreHandler) {

        // 表示Webapp的路径在 file:///android_asset/apps/HelloH5
        String appBasePath = "/apps/HelloH5";
        if (mSign == 0) {
            appBasePath = "/apps/HelloH5";
        } else if (mSign == 1) {
            appBasePath = "/apps/H53B12F37";
        }

        //String appBasePath = "/apps/jubeizf";

        // 设置启动参数,可在页面中通过plus.runtime.arguments;方法获取到传入的参数
        String args = "{url:'http://www.baidu.com'}";

        // 启动启动独立应用的5+ Webapp
        app = SDK.startWebApp(activity,appBasePath,args,new IWebviewStateListener() {
            // 设置Webview事件监听，可在监监听内获取WebIvew加载内容的进度
            @Override
            public Object onCallBack(int pType,Object pArgs) {
                switch (pType) {
                    case IWebviewStateListener.ON_WEBVIEW_READY:
                        // WebApp准备加载事件
                        // 准备完毕之后添加webview到显示父View中，
                        // 设置排版不显示状态，避免显示webview时html内容排版错乱问题
                        View view = ((IWebview) pArgs).obtainApp().obtainWebAppRootView().obtainMainView();
                        view.setVisibility(View.INVISIBLE);

                        if (view.getParent() != null) {
                            ((ViewGroup) view.getParent()).removeView(view);
                        }
                        rootView.addView(view,0);
                        break;
                    case IWebviewStateListener.ON_PAGE_STARTED:
                        // 首页面开始加载事件
                        pd = ProgressDialog.show(activity,"加载中","0/100");
                        break;
                    case IWebviewStateListener.ON_PROGRESS_CHANGED:
                        // WebApp首页面加载进度变化事件
                        if (pd != null) {
                            pd.setMessage(pArgs + "/100");
                        }
                        break;
                    case IWebviewStateListener.ON_PAGE_FINISHED:
                        // WebApp首页面加载完成事件
                        if (pd != null) {
                            pd.dismiss();
                            pd = null;
                        }
                        // 页面加载完毕，设置显示webview
                        // 如果不需要显示spalsh页面将此行代码移动至onCloseSplash事件内
                        app.obtainWebAppRootView().obtainMainView().setVisibility(View.VISIBLE);
                        break;
                }
                return null;
            }
        },this);

        app.setIAppStatusListener(new IApp.IAppStatusListener() {
            // 设置APP运行事件监听
            @Override
            public boolean onStop( ) {
                // 应用运行停止时调用
                rootView.removeView(app.obtainWebAppRootView().obtainMainView());
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onStart( ) {
                // 独立应用启动时触发事件
            }

            @Override
            public void onPause(IApp arg0,IApp arg1) {
                // WebApp暂停运行时触发事件
            }

            @Override
            public String onStoped(boolean arg0,String arg1) {
                // TODO Auto-generated method stub
                return null;
            }
        });
    }

    @Override
    public void onCoreReady(ICore coreHandler) {
        // 初始化5+ SDK，
        // 5+SDK的其他接口需要在SDK初始化后才能調用
        SDK.initSDK(coreHandler);
        // 设置当前应用可使用的5+ API
        SDK.requestAllFeature();
        //调用nativeObj.bitmap时  load之前 需要 onCoreReady中进行 imageloader初始化
        ImageLoaderUtil.initImageLoaderL(activity);
    }

    @Override
    public boolean onCoreStop( ) {
        // 当返回false时候回关闭activity
        return false;
    }

    // 在Widget集成时如果不需要显示splash页面可按照如下步骤操作
    // 1 删除onCreateSplash方法内的代码
    // 2 将5+mainView添加到rootview时将页面设置为不可见
    // 3 在onCloseSplash方法中将5+mainView设置为可见
    // 4 修改androidmanifest.xml文件 将SDK_WebApp的主题设置为透明
    // 注意！
    // 如果不显示splash页面会造成用户点击后页面短时间内会没有变化，
    // 可能会给用户造成程序没响应的错觉，
    // 所以开发者需要对5+内核启动到5+应用页面显示之间的这段事件进行处理

    @Override
    public Object onCreateSplash(Context pContextWrapper) {
        splashView = new FrameLayout(activity);
        splashView.setBackgroundResource(RInformation.DRAWABLE_SPLASH);
        rootView.addView(splashView);
        return null;
    }

    @Override
    public void onCloseSplash( ) {
        rootView.removeView(splashView);
    }
}

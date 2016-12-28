package com.supermap.desktop.core;

import com.supermap.data.CloudLicense;
import com.supermap.desktop.Application;
import com.supermap.desktop.GlobalParameters;
import com.supermap.desktop.exception.SmUncaughtExceptionHandler;
import com.supermap.desktop.icloud.CloudLicenseDialog;
import com.supermap.desktop.icloud.LicenseManager;
import com.supermap.desktop.properties.CoreProperties;
import com.supermap.desktop.utilities.LogUtilities;
import com.supermap.desktop.utilities.SplashScreenUtilities;
import com.supermap.desktop.utilities.StringUtilities;
import org.osgi.framework.*;

import java.io.File;

public class CoreActivator implements BundleActivator {

    static {
        GlobalParameters.initResource();
    }

    //    ServiceRegistration<?> registration;
    CoreServiceTracker serviceTracker;
    private String userName;
    private String passWord;
//    private LicenseService service;
//    private ApplyFormalLicenseResponse formLicenseResponse;
//    private ApplyTrialLicenseResponse trialLicenseResponse;
//    private LicenseId licenseId;

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext )
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        //本机许可是否满足java版本需求
        if (LicenseManager.valiteLicense()) {
            //yes
            startUp(context, "null");
        } else {
            //判断试用许可
            //no
            if (LicenseManager.valiteTrialLicense()) {
                //是否有离线许可
                //yes
                startUp(context, "null");
            } else {
                //没有离线许可，登录云许可
                //no
                loginOnlineLicense(context);
            }
        }

    }

    /**
     * 登录云许可
     *
     * @param bundleContext
     */
    private void loginOnlineLicense(BundleContext bundleContext) {
        try {
//            if (UICommonToolkit.showConfirmDialog(message) == JOptionPane.OK_OPTION) {
            //登录
            CloudLicenseDialog dialog = new CloudLicenseDialog();
            if (dialog.showDialog() == CloudLicenseDialog.DIALOGRESULT_OK) {
                userName = dialog.getUserName();
                passWord = dialog.getUserName();
//                service = dialog.getLicenseService();
//                //许可申请成功
//                licenseId = dialog.getLicenseId();
//                formLicenseResponse = dialog.getFormalLicenseResponse();
//                trialLicenseResponse = dialog.getTrialLicenseResponse();
//                if (null != formLicenseResponse) {
//                    startUp(bundleContext, formLicenseResponse.license);
//                } else if (null != trialLicenseResponse) {
//                    startUp(bundleContext, trialLicenseResponse.license);
//                } else {
//                    //不登陆
//                    stop(bundleContext);
//                    System.exit(1);
//                    return;
//                }
//                }
            } else {
                //不登陆
                stop(bundleContext);
                System.exit(1);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startUp(final BundleContext context, String licenseStr) {
        File licenseFile = LicenseManager.buildLicenseFile(licenseStr);
        if (licenseFile.exists() || "null".equals(licenseStr)) {
            // 设置没有被捕捉的异常的处理方法
            Thread.setDefaultUncaughtExceptionHandler(new SmUncaughtExceptionHandler());
            LogUtilities.outPut(LogUtilities.getSeparator());
            LogUtilities.outPut(CoreProperties.getString("String_DesktopStarting"));
            SplashScreenUtilities splashScreenUtiltiesInstance = SplashScreenUtilities.getSplashScreenUtiltiesInstance();
            if (splashScreenUtiltiesInstance != null) {
                SplashScreenUtilities.setBundleCount(context.getBundles().length);
                SplashScreenUtilities.resetCurrentCount();
                Bundle bundle = context.getBundle();
                String name = bundle.getSymbolicName();
                if (name == null) {
                    name = "unname";
                }
                String info = "Loading " + name + ".jar";
                splashScreenUtiltiesInstance.update(info);
            }


            context.addBundleListener(new SynchronousBundleListener() {
                @Override
                public void bundleChanged(BundleEvent bundleEvent) {
                    if (bundleEvent.getType() != BundleEvent.STARTING) {
                        return;
                    }
                    SplashScreenUtilities splashScreenUtiltiesInstance = SplashScreenUtilities.getSplashScreenUtiltiesInstance();
                    if (splashScreenUtiltiesInstance != null) {
                        Bundle bundle = bundleEvent.getBundle();
                        String name = bundle.getSymbolicName();
                        if (name == null) {
                            name = "unname";
                        }
                        String info = "Loading " + name + ".jar";
                        splashScreenUtiltiesInstance.update(info);
                    }
                    if (bundleEvent.getBundle() == context.getBundles()[context.getBundles().length - 1]) {
                        context.removeBundleListener(this);
                    }
                }
            });
            System.out.println("Hello SuperMap === Core!!");

            // 不知道为什么，core会被加载两次，暂时先这么处理
            if (Application.getActiveApplication() == null || Application.getActiveApplication().getPluginManager().getBundle("SuperMap.Desktop.Core") == null) {
                this.serviceTracker = new CoreServiceTracker(context);
                this.serviceTracker.open();
//			this.registration = context.registerService(Application.class.getName(), new Application(), null);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Goodbye SuperMap === Core!!");
        if (!StringUtilities.isNullOrEmpty(userName) && !StringUtilities.isNullOrEmpty(passWord)) {
            CloudLicense.logout(userName, passWord);
        }
//        if (null != service && null != licenseId && null != formLicenseResponse) {
//            //退出产品时，归还正式许可
//            LicenseManager.returnFormLicense(service, formLicenseResponse, licenseId);
//        }
//        if (null != service && null != trialLicenseResponse) {
//            //退出产品时，归还试用许可
//            LicenseManager.returnTrialLicense(service, trialLicenseResponse);
//        }
        this.serviceTracker.close();
//		this.registration.unregister();
    }
}

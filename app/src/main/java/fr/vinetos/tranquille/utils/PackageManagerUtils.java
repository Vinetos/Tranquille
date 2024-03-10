package fr.vinetos.tranquille.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public class PackageManagerUtils {

    public static boolean isComponentEnabled(Context ctx, Class<?> cls) {
        return isComponentEnabled(ctx, new ComponentName(ctx, cls));
    }

    public static void setComponentEnabledOrDefault(Context ctx, Class<?> cls, boolean enabled) {
        setComponentEnabledOrDefault(ctx, new ComponentName(ctx, cls), enabled);
    }

    public static boolean isComponentEnabled(Context ctx, ComponentName componentName) {
        return getPackageManager(ctx).getComponentEnabledSetting(componentName)
                == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static void setComponentEnabledOrDefault(Context ctx, ComponentName componentName,
                                                    boolean enabled) {
        getPackageManager(ctx).setComponentEnabledSetting(componentName, enabled
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP);
    }

    private static PackageManager getPackageManager(Context ctx) {
        return ctx.getPackageManager();
    }

}

package dev.fluttercommunity.plus.device_info

import android.content.pm.FeatureInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.annotation.RequiresPermission
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import kotlin.collections.HashMap

/**
 * The implementation of [MethodChannel.MethodCallHandler] for the plugin. Responsible for
 * receiving method calls from method channel.
 */
internal class MethodCallHandlerImpl(
    private val packageManager: PackageManager,
    private val windowManager: WindowManager,
    private val packageName: String,
) : MethodCallHandler {

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method.equals("getDeviceInfo")) {
            val build: MutableMap<String, Any> = HashMap()

            build["board"] = Build.BOARD
            build["bootloader"] = Build.BOOTLOADER
            build["brand"] = Build.BRAND
            build["device"] = Build.DEVICE
            build["display"] = Build.DISPLAY
            build["fingerprint"] = Build.FINGERPRINT
            build["hardware"] = Build.HARDWARE
            build["host"] = Build.HOST
            build["id"] = Build.ID
            build["manufacturer"] = Build.MANUFACTURER
            build["model"] = Build.MODEL
            build["product"] = Build.PRODUCT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                build["supported32BitAbis"] = listOf(*Build.SUPPORTED_32_BIT_ABIS)
                build["supported64BitAbis"] = listOf(*Build.SUPPORTED_64_BIT_ABIS)
                build["supportedAbis"] = listOf<String>(*Build.SUPPORTED_ABIS)
            } else {
                build["supported32BitAbis"] = emptyList<String>()
                build["supported64BitAbis"] = emptyList<String>()
                build["supportedAbis"] = emptyList<String>()
            }

            build["tags"] = Build.TAGS
            build["type"] = Build.TYPE
            build["isPhysicalDevice"] = !isEmulator
            build["systemFeatures"] = getSystemFeatures()

            val version: MutableMap<String, Any> = HashMap()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                version["baseOS"] = Build.VERSION.BASE_OS
                version["previewSdkInt"] = Build.VERSION.PREVIEW_SDK_INT
                version["securityPatch"] = Build.VERSION.SECURITY_PATCH
            }
            version["codename"] = Build.VERSION.CODENAME
            version["incremental"] = Build.VERSION.INCREMENTAL
            version["release"] = Build.VERSION.RELEASE
            version["sdkInt"] = Build.VERSION.SDK_INT
            build["version"] = version

            build["displayMetrics"] = getDisplayMetricsData()
            build["serialNumber"] = getSerialNumber()

            result.success(build)
        } else {
            result.notImplemented()
        }
    }

    private fun getDisplayMetricsData(): Map<String, Any> {
        val display: Display = windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)

        val displayData: MutableMap<String, Any> = HashMap()
        displayData["widthPx"] = metrics.widthPixels.toDouble()
        displayData["heightPx"] = metrics.heightPixels.toDouble()
        displayData["xDpi"] = metrics.xdpi
        displayData["yDpi"] = metrics.ydpi

        return displayData
    }

    /**
     * Retrieval of serial number for Android apps that have required permission added
     * Supression of lint is added because all required checks happen inside this function
     */
    @Suppress("MissingPermission", "Deprecation")
    private fun getSerialNumber(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return Build.SERIAL
        } else {
            val packageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
                } else {
                    packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                }
            val hasRequiredPermission = packageInfo.requestedPermissions.contains("android.permission.READ_PRIVILEGED_PHONE_STATE")

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasRequiredPermission) {
                Build.getSerial()
            } else {
                Build.UNKNOWN
            }

        }
    }

    private fun getSystemFeatures(): List<String> {
        val featureInfos: Array<FeatureInfo> = packageManager.systemAvailableFeatures
        return featureInfos
            .filterNot { featureInfo -> featureInfo.name == null }
            .map { featureInfo -> featureInfo.name }
    }

    /**
     * A simple emulator-detection based on the flutter tools detection logic and a couple of legacy
     * detection systems
     */
    private val isEmulator: Boolean
        get() = ((Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator"))
}

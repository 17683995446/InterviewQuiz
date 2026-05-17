package com.interviewquiz.app.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dangerousApps = listOf(
        "com.chelpus.lackypatch",
        "com.forpda.lp",
        "com.dimonvideo.lackypatch",
        "com.android.vending.billing.InAppBillingService.LUCK",
        "com.android.vending.billing.InAppBillingService.CRAC",
        "com.android.vending.billing.InAppBillingService.COIN",
        "com.android.vending.billing.InAppBillingService.LUCK",
        "com.android.vending.billing.InAppBillingService.SLOT",
        "com.android.vending.billing.InAppBillingService.PAY",
        "com.android.vending.billing.InAppBillingService.CARD",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.topjohnwu.magisk",
        "com.kingroot.kinguser",
        "com.kingo.root",
        "com.smedialink.oneclickroot",
        "com.zhiqupk.root.global",
        "com.alephzain.framaroot"
    )

    private val rootBinaryPaths = listOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/su/bin",
        "/system/xbin/daemonsu",
        "/system/bin/ext/su",
        "/system/lib/librs.so",
        "/system/etc/init.d/99SuperSUDaemon",
        "/system/etc/.donttouch",
        "/system/xbin/su"
    )

    private val emulatorFiles = listOf(
        "/system/lib/qemu_props.lua",
        "/system/lib64/qemu_props.lua",
        "/system/build.prop",
        "/sys/qemu_trace",
        "/dev/socket/qemud",
        "/dev/qemu_pipe",
        "/dev/vfio/0",
        "/dev/vfio/1",
        "/dev/vfio/2",
        "/dev/vfio/3",
        "/dev/vfio/4",
        "/dev/vfio/5",
        "/dev/vfio/6",
        "/dev/vfio/7",
        "/proc/self/status",
        "/sys/class/power_supply/",
        "/sys/devices/system/cpu/",
        "/system/bin/androVM-prop",
        "/system/bin/houdini1",
        "/system/bin/houdini2",
        "/system/bin/houdini3",
        "/system/bin/houdini4",
        "/system/bin/houdini5",
        "/system/bin/houdini6",
        "/system/bin/houdini7",
        "/system/bin/houdini8",
        "/system/bin/houdini9",
        "/system/xbin/houdini",
        "/system/lib/libhoudini.so",
        "/system/lib64/libhoudini.so"
    )

    private val emulatorProps = mapOf(
        "ro.kernel.qemu" to "1",
        "ro.product.model" to "sdk_gphone64_arm64",
        "ro.product.name" to "sdk_gphone64_arm64",
        "ro.hardware" to "ranchu",
        "qemu.hw.mainkeys" to "1",
        "qemu.sf.fake_camera" to "1",
        "qemu.sf.lcd_density" to "160",
        "dalvik.vm.dex2opt" to "bash",
        "ro.debuggable" to "1",
        "service.adb.root" to "1"
    )

    fun isRooted(): Boolean {
        return checkRootBinaries() || checkRootManagement() || checkSuCommand()
    }

    private fun checkRootBinaries(): Boolean {
        return rootBinaryPaths.any { path ->
            File(path).exists()
        }
    }

    private fun checkRootManagement(): Boolean {
        val packageManager = context.packageManager
        return dangerousApps.any { packageName ->
            try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun checkSuCommand(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine() != null
        } catch (e: Exception) {
            false
        } finally {
            process?.destroy()
        }
    }

    fun isEmulator(): Boolean {
        return checkEmulatorFiles() || checkEmulatorProps() || checkQemuBranding()
    }

    private fun checkEmulatorFiles(): Boolean {
        return emulatorFiles.any { path ->
            File(path).exists()
        }
    }

    private fun checkEmulatorProps(): Boolean {
        return emulatorProps.entries.any { (key, value) ->
            getSystemProperty(key) == value
        }
    }

    private fun getSystemProperty(key: String): String? {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine()
        } catch (e: Exception) {
            null
        } finally {
            process?.destroy()
        }
    }

    private fun checkQemuBranding(): Boolean {
        val brand = Build.BRAND.lowercase()
        val device = Build.DEVICE.lowercase()
        val hardware = Build.HARDWARE.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val model = Build.MODEL.lowercase()
        val product = Build.PRODUCT.lowercase()

        val qemuIndicators = listOf(
            "generic", "goldfish", "ranchu", "sdk", "emulator",
            "模拟器", "虚拟机", "virtual", "qemu", "bluestacks",
            "nox", "mumu", "ldplayer", "genymotion"
        )

        return qemuIndicators.any { indicator ->
            brand.contains(indicator) ||
            device.contains(indicator) ||
            hardware.contains(indicator) ||
            manufacturer.contains(indicator) ||
            model.contains(indicator) ||
            product.contains(indicator)
        }
    }

    fun isDebuggable(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun isRunningInDebugMode(): Boolean {
        return isDebuggable() || Settings.Secure.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        ) == 1
    }

    fun getSecurityIssues(): List<SecurityIssue> {
        val issues = mutableListOf<SecurityIssue>()

        if (isRooted()) {
            issues.add(
                SecurityIssue(
                    type = SecurityIssueType.ROOT_DETECTED,
                    severity = Severity.HIGH,
                    description = "设备已Root，存在安全风险",
                    recommendation = "建议在安全环境中使用"
                )
            )
        }

        if (isEmulator()) {
            issues.add(
                SecurityIssue(
                    type = SecurityIssueType.EMULATOR_DETECTED,
                    severity = Severity.MEDIUM,
                    description = "检测到模拟器环境",
                    recommendation = "某些功能可能在模拟器中受限"
                )
            )
        }

        if (isDebuggable()) {
            issues.add(
                SecurityIssue(
                    type = SecurityIssueType.DEBUG_MODE,
                    severity = Severity.MEDIUM,
                    description = "应用处于调试模式",
                    recommendation = "发布时应禁用调试模式"
                )
            )
        }

        return issues
    }
}

data class SecurityIssue(
    val type: SecurityIssueType,
    val severity: Severity,
    val description: String,
    val recommendation: String
)

enum class SecurityIssueType {
    ROOT_DETECTED,
    EMULATOR_DETECTED,
    DEBUG_MODE,
    TAMPER_DETECTED,
    HOOK_DETECTED,
    SCREENSHOT_TAKEN
}

enum class Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

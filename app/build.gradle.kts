import java.time.Instant
import java.util.Calendar
import java.io.ByteArrayOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

plugins {
    id("com.android.application")
}

val appName = "SingleHook"
val packageName = "org.lsposed.hijack"
val versionname = "1.0"
// 签名信息
val keyStoreFile = file("app.jks")
val keyAliasValue = "SingleHook"
val keyPasswordValue = "12345678"
val storePasswordValue = "12345678"
val encryptionKey = "SingleHook.SecureEncryptionKey"

android {
    namespace = packageName
    compileSdk = 35
	defaultConfig {
        applicationId = packageName
        minSdk = 23
        targetSdk = 35
        versionCode = calculateVersionCode()
        versionName = versionname
		val currentBuildTime = Instant.now().toEpochMilli().toString()
		resValue("string", "app_name", appName)
        GenerateSignatureInformation()
		buildConfigField("String", "APP_NAME", "\"${appName}\"")
        buildConfigField("long", "BUILD_TIME", currentBuildTime)
        vectorDrawables { 
            useSupportLibrary = true
        }
        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
    }
    
    signingConfigs {
        create("keyStore") {
		    // 签名文件路径
            storeFile = keyStoreFile
			// 密钥别名
            keyAlias = keyAliasValue
			// 密钥密码
            keyPassword = keyPasswordValue
			// 密钥库密码
			storePassword = storePasswordValue
			// 启用V1签名
			enableV1Signing = true
			// 启用V2签名
            enableV2Signing = true
			// 启用V3签名
            enableV3Signing = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
		    signingConfig = signingConfigs.getByName("keyStore")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
		debug {
            signingConfig = signingConfigs.getByName("keyStore")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

fun calculateVersionCode() : Int {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return year * 10000 + month * 100 + day
}

fun encrypt(key: String, plaintext: String): String {
    try {
        val salt = ByteArray(8).also { SecureRandom().nextBytes(it) }
        val secretKey = generateSecretKey(key.toCharArray(), salt)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(plaintext.toByteArray())
        return (salt + encrypted).joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        return plaintext
    }
}

fun generateSecretKey(password: CharArray, salt: ByteArray): SecretKey {
    val spec = PBEKeySpec(password, salt, 128, 128)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val secret = factory.generateSecret(spec)
    return SecretKeySpec(secret.encoded, "AES")
}

fun GenerateSignatureInformation() {
    if (keyStoreFile.exists()) {
        val SHAOutput = ByteArrayOutputStream()
        exec {
            commandLine(
                "keytool",
                "-list",
                "-v",
                "-keystore", keyStoreFile.absolutePath,
                "-alias", keyAliasValue,
                "-storepass", storePasswordValue
            )
            standardOutput = SHAOutput
            isIgnoreExitValue = true
        }
        val output = SHAOutput.toString()
        if (output.isNotBlank()) {
            val sha1 = Regex("SHA1: (.*)").find(output)?.groupValues?.get(1)?.replace(":", "")?.replace(" ", "")?.trim()?.uppercase()?:""
            val sha256 = Regex("SHA256: (.*)").find(output)?.groupValues?.get(1)?.replace(":", "")?.replace(" ", "")?.trim()?.uppercase()?:""
            val encryptedSha1 = encrypt(encryptionKey, sha1)
            val encryptedSha256 = encrypt(encryptionKey, sha256)
            android.defaultConfig.apply {
                resValue("string", "sha_key", encryptionKey)
                buildConfigField("String", "SHA_KEY", "\"12345678\"")
                buildConfigField("String", "SHA1", "\"${encryptedSha1}\"")
                buildConfigField("String", "SHA256", "\"${encryptedSha256}\"")
            }
        }
    }
}

tasks.register("createKeyStore") {
    doLast {
        if (!keyStoreFile.exists()) {
            exec {
                commandLine(
                    "keytool",
                    "-genkey",
                    "-v",
                    "-keystore", keyStoreFile.absolutePath,
                    "-keyalg", "RSA",
                    "-keysize", "2048",
                    "-validity", "3650",
                    "-alias", keyAliasValue,
                    "-keypass", keyPasswordValue,
                    "-storepass", storePasswordValue,
                    "-dname", "CN=Micro Cao, OU=ByteDance, O=ByteDance, L=Beijing, ST=Beijing, C=CN"
                )
            }
        }
    }
    GenerateSignatureInformation()
}

tasks.named("preBuild") {
    dependsOn("createKeyStore")
}

androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.name.equals("Debug", ignoreCase = true)) {
            variantBuilder.enabled = false
        }
    }
}

dependencies {
    // Android 核心基础库，提供核心功能支持，比如资源管理、线程管理等。
    implementation("androidx.core:core:1.3.0")
    // Android 启动画面（Splash Screen）支持库，
    implementation("androidx.core:core-splashscreen:1.0.0")
    // AppCompat 库，提供向后兼容的 UI 组件和主题支持，比如 AppCompatActivity，允许使用 Material Design 在旧版本 Android 上。
    implementation("androidx.appcompat:appcompat:1.6.1")
    // Android Jetpack Navigation 组件中的 Fragment 相关库，用于管理导航图中的 Fragment 导航逻辑。
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    // Android Jetpack Navigation 组件中的 UI 相关库，提供如 NavigationUI 类，用于将导航与 ActionBar/Toolbar 等 UI 控件绑定。
    implementation("androidx.navigation:navigation-ui:2.5.3")
    // Google 官方的 Material Design 组件库，提供符合 Material Design 设计规范的控件，如 Button、TextField、BottomNavigation 等。
    implementation("com.google.android.material:material:1.9.0")
    // ImmersionBar 是一个第三方库，用于快速、简单地实现 Android 状态栏和导航栏沉浸式效果，比如透明状态栏、状态栏字体颜色控制等，简化了原生 API 的复杂操作。
    implementation("com.geyifeng.immersionbar:immersionbar:3.2.2")
    // FastScroll 是一个第三方库，用于为 RecyclerView 或 ListView 添加快速滚动条（Fast Scroller)
    implementation("me.zhanghai.android.fastscroll:library:1.3.0")
    // 引用 Xposed环境
    compileOnly("de.robv.android.xposed:api:82")
    // Xposed dex查找工具
    implementation("org.luckypray:dexkit:2.0.7")
    // 腾讯mmkv存储
    implementation("com.tencent:mmkv:1.3.14")
    // 图片加载库
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    // json处理库
    implementation("com.google.code.gson:gson:2.0")
    // 文件操作库
    implementation("commons-io:commons-io:2.11.0")
    // 二维码库
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    // mapdb
    implementation("org.mapdb:mapdb:2.0-beta13")
}

configurations.all {
    //exclude("com.itsaky.androidide", "gradle-plugin")
}

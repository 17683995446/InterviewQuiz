# InterviewQuiz ProGuard Rules for Security and Optimization

# Keep application class
-keep class com.interviewquiz.app.InterviewQuizApplication { *; }

# Keep security classes
-keep class com.interviewquiz.app.security.** { *; }
-keepclassmembers class com.interviewquiz.app.security.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.android.internal.managers.ComponentSupplier
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.android.internal.managers.FragmentComponentManager
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Navigation
-keep class * extends androidx.navigation.Navigator
-keepnames class * extends androidx.navigation.Navigator

# Data classes - obfuscate
-keepclassmembers class com.interviewquiz.app.data.Question {
    <fields>;
    <init>(...);
    public <methods>;
}
-keepclassmembers class com.interviewquiz.app.ui.screens.HomeUiState {
    <fields>;
    <init>(...);
    public <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# General Android rules
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# Reflection safety
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @javax.inject.* <methods>;
}

# Enum safety
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# R8 full mode optimizations
-if interface * { @dagger.hilt.android.* <methods>; }
-keep,allowobfuscation interface <1>

-if interface * { @javax.inject.* <methods>; }
-keep,allowobfuscation interface <1>

# Prevent stripping of ViewModel factory methods
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

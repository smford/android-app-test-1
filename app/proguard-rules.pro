# Proguard configuration for Credential Manager and Firebase
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

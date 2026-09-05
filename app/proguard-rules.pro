# Stack traces from a privileged root/Shizuku process are the only way to debug
# installation failures on a user's device, so names are kept readable.
-dontobfuscate

# Shizuku instantiates the user service reflectively, preferring a single-Context
# constructor and falling back to a parameterless one. R8 sees neither being called
# from app code and removes both, which makes bindUserService fail with a
# NoSuchMethodException in every minified build.
-keep class vegabobo.dsusideloader.service.PrivilegedService {
    <init>(...);
    public *;
}

# The two binder hosts are only ever named in the manifest or handed to
# RootService.bind(), never constructed directly.
-keep class vegabobo.dsusideloader.service.PrivilegedRootService { *; }
-keep class vegabobo.dsusideloader.service.PrivilegedSystemService { *; }

# Both ends of the AIDL boundary live in this APK, but the transaction codes are
# fixed by the interface, so the generated stub and proxy are kept whole rather
# than relying on R8 to agree with itself across a process boundary.
-keep interface vegabobo.dsusideloader.IPrivilegedService { *; }
-keep class vegabobo.dsusideloader.IPrivilegedService$* { *; }

# Parcelables are reconstructed by the framework through CREATOR.
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Hidden platform APIs reached through HiddenApiBypass and Class.forName. These
# are resolved by name at runtime against the platform, so nothing in the APK
# references them and no keep would apply to them; listed here only to record
# that the reflection in PrivilegedService, DSUInstaller and DevicePropUtils is
# deliberate. The corresponding stubs are compileOnly (:hidden-api-stub) and are
# never packaged.
-dontnote android.os.**
-dontnote android.gsi.**

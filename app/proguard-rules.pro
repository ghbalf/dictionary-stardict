# dictzip
-keep class org.dict.zip.** { *; }

# Apache Commons Compress - keep what we use, ignore optional deps
-keep class org.apache.commons.compress.archivers.tar.** { *; }
-keep class org.apache.commons.compress.compressors.xz.** { *; }
-keep class org.apache.commons.compress.compressors.gzip.** { *; }
-dontwarn com.github.luben.zstd.**
-dontwarn org.brotli.dec.**
-dontwarn org.objectweb.asm.**
-dontwarn java.lang.invoke.MethodHandle

# XZ
-keep class org.tukaani.xz.** { *; }

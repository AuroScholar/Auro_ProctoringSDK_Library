-dontwarn junit.**
-dontwarn org.junit.**

# Make crash call-stacks debuggable.
-keepnames class ** { *; }
-keepattributes SourceFile,LineNumberTable
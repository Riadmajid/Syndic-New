# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Project Data Classes (Required for Firestore serialization)
-keep class com.example.syndic.zaineb4.data.** { *; }

# Compose
-keep class androidx.compose.material.icons.** { *; }
-dontwarn androidx.compose.material.icons.**
# qu_sp_android

Kotlin `SharedPreferences` property delegates with Flow observation.

## Dependency

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

```kotlin
dependencies {
    implementation("com.github.tbtsfastad:qu_sp_android:v1.0.2")
}
```

## Usage

```kotlin
var launchCount by QUSPIntStore("launch_count")
var userName by QUSPStringStore("user_name", "Guest")

private val themeStore = QUSPStringStore("theme").obs
var theme by themeStore

lifecycleScope.launch {
    themeStore.flow.collect { value ->
        println(value)
    }
}
```

Use `scope` to select another `SharedPreferences` file:

```kotlin
var token by QUSPStringStore(
    key = "token",
    scope = "account",
)
```

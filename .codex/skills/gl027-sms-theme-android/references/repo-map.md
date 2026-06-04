# GL027 Repo Map

Use this as a compact map before broad project edits.

## Build

- Root Gradle: `build.gradle.kts`, `settings.gradle.kts`
- App Gradle: `app/build.gradle.kts`
- Versions: `gradle/libs.versions.toml`
- Java/Kotlin target: JVM 17
- ViewBinding enabled: yes
- DI: Hilt with KSP
- Database: Room with `fallbackToDestructiveMigration()` currently enabled

## Existing Source

- `app/src/main/AndroidManifest.xml`: permissions, app components, FileProvider.
- `app/src/main/java/com/example/myapplication/base/activity/BaseActivity.kt`: Activity base, binding, system bars, navigation helpers, loading dialog.
- `app/src/main/java/com/example/myapplication/base/fragment/BaseFragment.kt`: fragment binding lifecycle and activity helper delegation.
- `app/src/main/java/com/example/myapplication/base/fragment/BaseBottomFragment.kt`: bottom sheet base.
- `app/src/main/java/com/example/myapplication/base/adapter`: RecyclerView adapter bases.
- `app/src/main/java/com/example/myapplication/base/dialog/BaseDialog.kt`: dialog base.
- `app/src/main/java/com/example/myapplication/utils/PermissionUtils.kt`: notification, default SMS role, exact alarm, full-screen intent, SMS permission helpers.
- `app/src/main/java/com/example/myapplication/utils/SpManager.kt`: SharedPreferences wrapper, injected by Hilt.
- `app/src/main/java/com/example/myapplication/di/AppModule.kt`: Room/DAO/SpManager providers.
- `app/src/main/java/com/example/myapplication/ui/main/MainActivity.kt`: current placeholder main screen and permission bottom sheet flow.
- `app/src/main/res/values/styles.xml`: existing text styles are `TextView_4`, `TextView_5`, `TextView_6`, `TextView_7`.

## Hard Local Rules

- New Activity classes must extend `BaseActivity<VB>`.
- New Fragment classes must extend `BaseFragment` or `BaseBottomFragment`.
- New RecyclerView adapters must extend the project adapter base classes.
- Any text created by Codex or requested by the user must use `@style/TextView_4`, `@style/TextView_5`, `@style/TextView_6`, or `@style/TextView_7`.

## Current Gaps To Remember

- The app still looks like a template in several places.
- `activity_main.xml` is placeholder content.
- Some layouts still hardcode text color/size despite the rule. Clean touched layout areas.
- Manifest has SMS permissions but not yet all default SMS app components.
- Package is still `com.example.myapplication`; rename only if the user asks or release work requires it.

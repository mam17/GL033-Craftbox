# CLAUDE.md

Tệp này cung cấp hướng dẫn cho Claude Code (claude.ai/code) khi làm việc với mã nguồn trong kho lưu trữ này.

⚠️ **Đây là dự án Template (Mẫu)** - Sử dụng khi bắt đầu một dự án Android mới. Dự án cung cấp cấu trúc Clean Architecture, các lớp base tái sử dụng, và các hàm tiện ích để bạn bắt đầu nhanh chóng với code clean và có cấu trúc tốt.

## 🤖 ClaudeKit Integration

Dự án này tích hợp **ClaudeKit Framework** để hỗ trợ AI-powered development với agent orchestration.

### Workflows & Rules
- **Primary Workflow**: `./.claude/rules/primary-workflow.md`
- **Development Rules**: `./.claude/rules/development-rules.md`
- **Android Rules** (BẮT BUỘC): `./.claude/rules/android-development-rules.md`
- **Orchestration**: `./.claude/rules/orchestration-protocol.md`
- **Documentation**: `./.claude/rules/documentation-management.md`

### ⚠️ CRITICAL RULES - BẮT BUỘC TUÂN THỨ
1. **Đọc** `./.claude/rules/android-development-rules.md` trước khi code
2. **Tuân thủ** tất cả rules trong file đó (Base classes, Layout rules, Ripple, Style)
3. **Agents sẽ enforce** các rules này tự động qua code review
4. **🚨 TUYỆT ĐỐI KHÔNG COMMIT/PUSH** `claudekit-engineer-2.16.0/` folder
   - Đọc: `./.claude/KIT-RULES.md` (Important!)
   - `.gitignore` đã cấu hình để tự động ignore
   - Kiểm tra trước commit: `git status` (không nên thấy claudekit files)

### Quick Commands
```bash
# Lập kế hoạch cho feature mới
/ck:plan "implement [feature_name]"

# Thực thi implementation plan
/ck:cook

# Kiểm thử tự động
/ck:test

# Code review tự động
/ck:code-review

# Cập nhật documentation
/ck:docs

# Kiểm tra project status
/ck:watzup
```

## Bắt Đầu Nhanh

### Build & Chạy
- **Build APK debug**: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`)
- **Build APK release**: `./gradlew assembleRelease`
- **Chạy trên máy giả lập**: `./gradlew installDebug` (cần máy giả lập đang chạy)
- **Xóa build cũ**: `./gradlew clean`

### Kiểm Thử
- **Chạy unit tests**: `./gradlew test`
- **Chạy instrumented tests**: `./gradlew connectedAndroidTest` (cần thiết bị/máy giả lập kết nối)
- **Chạy test cụ thể**: `./gradlew test --tests com.example.myapplication.ClassName`

### Kiểm Tra Chất Lượng Mã
- **Lint check**: `./gradlew lint`
- **Sửa lỗi lint**: Sửa vi phạm theo khuyến nghị

## Kiến Trúc

Ứng dụng Android này sử dụng **Clean Architecture** với ba tầng riên biệt:

### Tầng Dữ Liệu (Data Layer) (`data/`)
- **Lưu trữ cục bộ**: Room database với AppDatabase, DAOs, và Entities
- Nằm trong `data/local/` với các thư mục con `dao/` và `entities/`
- Dependencies: Room ORM, SharedPreferences (qua utility SpManager)

### Tầng Domain (`domain/`)
- **Models**: Các data class đơn giản biểu diễn các thực thể kinh doanh (`domain/layer/`)
- **Use Cases**: Đóng gói logic kinh doanh với lớp base UseCase trừu tượng (`domain/usecase/`)
- Không phụ thuộc Android; chỉ dùng Kotlin thuần

### Tầng UI (`ui/`)
- **Activities & Fragments**: Xây dựng với ViewBinding và androidx.navigation
- **Màn hình**: splash, onboarding, chọn ngôn ngữ, main
- **Dialogs**: Các lớp dialog tái sử dụng (alert, loading)

## Công Nghệ Chủ Yếu

**Ngôn Ngữ Lập Trình**: Kotlin/Java
- Viết code mới ưu tiên sử dụng **Kotlin** (modern, concise, safe)
- Có thể viết Java nếu cần nhưng Kotlin được khuyến khích hơn
- Tính năng Kotlin: Null-safety, Extension functions, Coroutines

## Công Nghệ & Mẫu Chính

### Dependency Injection (Hilt)
- Cấu hình trong `di/AppModule.kt`
- Cung cấp các instance singleton: Room database, DAOs, SpManager
- Tất cả Activities/Fragments được trang trí với `@AndroidEntryPoint`
- Inject qua `@Inject` trong Activities/Fragments

### Cơ Sở Dữ Liệu (Room)
- Tệp cơ sở dữ liệu duy nhất: `app_database`
- Hiện tại định nghĩa: `UserEntity` với `UserDao` tương ứng
- Các thay đổi schema xử lý với `.fallbackToDestructiveMigration()` (chỉ dev)
- Khi migrate, tạo các lớp migration thích hợp cho production

### Lập Trình Reactive
- **RxJava 3**: Cho các tác vụ không đồng bộ và data streams
- **Coroutines**: Cho các tác vụ không đồng bộ trong ViewModel
- Room DAOs hỗ trợ trả về Observable/Flowable

### Thành Phần UI
- **ViewBinding**: Tất cả Activities sử dụng binding thay vì findViewById
- **Navigation**: Android Navigation Component để điều hướng fragment
- **Animations**: Chuyển động slide cho Activities/Fragments (định nghĩa trong `R.anim.*`)
- **Tải hình ảnh**: Glide để cache và hiển thị hình ảnh

### Lớp Base (Các Thành Phần Tái Sử Dụng)
- **BaseActivity**: ViewBinding chung, kiểm soát system bar, giao dịch fragment, dialog loading
- **BaseFragment**: Các lifecycle hook và utility tương tự
- **BaseAdapter/BaseListAdapter/BaseMultiAdapter**: RecyclerView adapters với binding support
- **BaseDialog**: Dialog base với các mẫu thông dụng

## Cấu Trúc Dự Án

```
app/src/main/
├── java/com/example/myapplication/
│   ├── base/                      # Các lớp base tái sử dụng
│   │   ├── activity/BaseActivity.kt
│   │   ├── adapter/               # Lớp base adapter
│   │   ├── dialog/BaseDialog.kt
│   │   └── fragment/              # Lớp base fragment
│   ├── data/                      # Tầng dữ liệu (Room, SharedPrefs)
│   │   └── local/
│   │       ├── AppDatabase.kt
│   │       ├── dao/               # Data Access Objects
│   │       └── entities/          # Room entities
│   ├── di/                        # Dependency Injection (Hilt)
│   │   └── AppModule.kt
│   ├── domain/                    # Logic kinh doanh (không phụ Android)
│   │   ├── layer/                 # Data models
│   │   └── usecase/               # Business use cases
│   ├── ui/                        # Tầng UI (Activities, Fragments)
│   │   ├── dialog/                # Custom dialogs
│   │   ├── language/              # Màn hình chọn ngôn ngữ
│   │   ├── main/                  # Màn hình chính
│   │   ├── onboarding/            # Luồng onboarding
│   │   └── splash/                # Màn hình splash
│   ├── utils/                     # Các lớp utility (SpManager, SystemUtil)
│   ├── views/                     # Custom views
│   └── MyApplication.kt           # Application class với Hilt
└── res/                           # Resources (layouts, drawables, strings)
```

## Quy Tắc Thiết Kế (Design Rules) - BẮT BUỘC

### ✅ Quy Tắc Bắt Buộc
1. **Mọi Activity, Fragment, Adapter PHẢI kế thừa từ lớp Base tương ứng**
   - Activities: phải extends `BaseActivity<VB>`
   - Fragments: phải extends `BaseFragment` hoặc `BaseBottomFragment`
   - Adapters: phải extends một trong các BaseAdapter sau:
     - `BaseAdapter<VH>` - RecyclerView adapter cơ bản
     - `BaseListAdapter<T>` - Cho list đơn giản
     - `BaseMultiAdapter` - Cho nhiều loại item
     - `BaseMultiListAdapter` - Kết hợp multi-type với list

2. **Layout XML cho từng màn hình**
   - Mỗi Activity/Fragment PHẢI có file layout XML riêng
   - Đặt tên: `activity_[name].xml` hoặc `fragment_[name].xml`
   - Sử dụng ViewBinding (không dùng findViewById)

3. **Ripple Effect bắt buộc trên nút bấm**
   - Tất cả nút (Button, ImageButton, v.v.) PHẢI có ripple effect
   - Thêm thuộc tính: `android:foreground="?attr/selectableItemBackground"`
   - Hoặc định nghĩa drawable ripple riêng nếu cần

4. **Các View có chữ PHẢI dùng thuộc tính Style**
   - TextView, Button, EditText: dùng style có sẵn từ `styles.xml`
   - KHÔNG set trực tiếp: `android:textSize`, `android:textColor`, `android:textStyle`
   - Sử dụng: `style="@style/TextStyle_Body"` (ví dụ)

## Các Quy Ước Quan Trọng

### Đặt Tên & Packages
- Cấu trúc package phản ánh kiến trúc: `data`, `domain`, `ui`
- Data models trong `domain/layer/`, use cases trong `domain/usecase/`
- Màn hình UI được tổ chức theo feature trong các thư mục con `ui/`

### Lifecycle Hooks trong BaseActivity
- `initView()`: Khởi tạo các thành phần UI và listeners
- `initData()`: Tải dữ liệu ban đầu, thiết lập ViewModel observers
- `initObserver()`: (tùy chọn) Quan sát LiveData/Flows

### Thêm Một Entity Cơ Sở Dữ Liệu Mới
1. Tạo entity trong `data/local/entities/`
2. Tạo DAO trong `data/local/dao/`
3. Thêm vào danh sách entities của `AppDatabase` và tạo abstract fun
4. Tạo model trong `domain/layer/`
5. Tạo use case trong `domain/usecase/` để xử lý logic kinh doanh

### Thêm Màn Hình Mới (PHẢI TUÂN THỨ QUY TẮC)
1. Tạo Activity hoặc Fragment trong `ui/[feature]/`
   - Activity **PHẢI** extends `BaseActivity<VB>` (VB là ViewBinding class)
   - Fragment **PHẢI** extends `BaseFragment` hoặc `BaseBottomFragment`
2. Tạo layout XML riêng - **BẮT BUỘC**
   - Đặt tên: `activity_[feature_name].xml` hoặc `fragment_[feature_name].xml`
   - Sử dụng ViewBinding trong lớp
3. Thêm ripple effect cho nút bấm - **BẮT BUỘC**
   - Tất cả Button/ImageButton phải có: `android:foreground="?attr/selectableItemBackground"`
4. Dùng Style cho các view có chữ - **BẮT BUỘC**
   - TextView, Button, EditText phải sử dụng `style` attribute
   - Không set trực tiếp textSize, textColor, textStyle
5. Implement các abstract methods từ BaseActivity:
   - `initView()` - Khởi tạo UI
   - `initData()` - Load dữ liệu ban đầu
   - `initObserver()` - (tùy chọn) Subscribe LiveData/Flow
6. Đăng ký trong AndroidManifest.xml (chỉ Activities)

### Chọn BaseAdapter Phù Hợp Theo Ngữ Cảnh
Chọn adapter dựa trên cấu trúc dữ liệu và loại item:

| Loại Adapter | Khi Nào Dùng | Ví Dụ |
|---|---|---|
| `BaseAdapter<VH>` | Adapter cơ bản, 1 loại item | RecyclerView đơn giản |
| `BaseListAdapter<T>` | List với 1 loại item, dùng binding | Danh sách người dùng, bài viết |
| `BaseMultiAdapter` | Nhiều loại item khác nhau | Feed có text, ảnh, video |
| `BaseMultiListAdapter` | Multi-type item + list binding | Chat với message types khác nhau |

**Ví dụ**:
- Danh sách người dùng đơn giản → `BaseListAdapter<UserModel>`
- Trang feed (text, image, video) → `BaseMultiAdapter`
- Chat messages (text, image, audio) → `BaseMultiListAdapter`

## Dependencies (gradle/libs.versions.toml)
- **Android**: AGP 9.1.0, Kotlin 2.3.10, compileSdk 36
- **Core**: androidx.core-ktx, lifecycle, navigation
- **UI**: Material Design 3, ViewBinding, ConstraintLayout
- **Data**: Room 2.8.4
- **DI**: Dagger Hilt 2.59.2
- **Async**: RxJava 3.1.12, Coroutines 1.10.2
- **Image**: Glide 5.0.5
- **JSON**: Gson 2.13.2
- **Testing**: JUnit 4, Espresso 3.7.0

## Các Tác Vụ Phát Triển Thông Dụng

### Debug
- Gắn debugger qua Android Studio / Android Device Monitor
- Lọc Logcat: Sử dụng `adb logcat` với bộ lọc package
- Các vấn đề ViewBinding: Kiểm tra cú pháp file layout và tái sinh

### Truy Cập Dữ Liệu
- Truy cập SharedPreferences qua `SpManager` được inject trong Activities/Fragments
- Truy cập Room DB qua các DAOs được inject
- Sử dụng coroutines hoặc RxJava cho các truy vấn ngoài main thread

### Tùy Chỉnh System Bar (Status Bar, Navigation Bar)
- BaseActivity cung cấp: `setBaseStatusBar()`, `setBaseStatusBarColor()`, `setBaseFullScreen()`, `setBaseHideNavigation()`
- Gọi trong `onCreate()` hoặc ghi đè defaults cho các màn hình cụ thể
- Các comments trong BaseActivity giải thích terminologies UI

### Xử Lý Back Navigation
- Override `onBack()` trong các subclass của BaseActivity
- Sử dụng `onBackPressedDispatcher` callback (đã thiết lập trong BaseActivity)
- Fragment backstack được quản lý qua `replaceFragment()` / `addFragment()` với tag

## Hướng Dẫn XML & Styling - Quy Tắc Thiết Kế

### Layout XML - Cấu Trúc Bắt Buộc
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <!-- Các view ở đây -->

</LinearLayout>
```

### Ripple Effect - BẮT BUỘC cho Nút Bấm
**Cách 1: Dùng Material Button (Khuyến Nghị)**
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/btn_login"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="Đăng Nhập"
    style="@style/Widget.MaterialComponents.Button"/>
```

**Cách 2: Dùng Button + foreground**
```xml
<Button
    android:id="@+id/btn_submit"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="Gửi"
    android:foreground="?attr/selectableItemBackground"
    style="@style/CustomButtonStyle"/>
```

**Cách 3: Drawable Ripple Riêng**
```xml
<!-- res/drawable/ripple_button.xml -->
<ripple xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="@color/ripple_color">
    <item android:id="@android:id/mask">
        <shape android:shape="rectangle">
            <solid android:color="@color/button_bg"/>
        </shape>
    </item>
</ripple>

<!-- Trong layout -->
<Button
    android:id="@+id/btn_action"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:background="@drawable/ripple_button"
    android:text="Hành Động"/>
```

### Style cho Text Views - BẮT BUỘC

**Trong `styles.xml` hoặc `attrs.xml`:**
```xml
<!-- Tiêu đề -->
<style name="TextStyle_Title">
    <item name="android:textSize">24sp</item>
    <item name="android:textColor">@color/text_primary</item>
    <item name="android:textStyle">bold</item>
</style>

<!-- Nội dung chính -->
<style name="TextStyle_Body">
    <item name="android:textSize">16sp</item>
    <item name="android:textColor">@color/text_primary</item>
    <item name="android:textStyle">normal</item>
</style>

<!-- Nhãn phụ -->
<style name="TextStyle_Label">
    <item name="android:textSize">14sp</item>
    <item name="android:textColor">@color/text_secondary</item>
</style>

<!-- Hint text -->
<style name="TextStyle_Hint">
    <item name="android:textSize">14sp</item>
    <item name="android:textColor">@color/text_hint</item>
</style>
```

**Cách dùng trong layout:**
```xml
<!-- ✅ ĐÚNG - Dùng style -->
<TextView
    android:id="@+id/tv_title"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Tiêu Đề"
    style="@style/TextStyle_Title"/>

<!-- ❌ SAI - Set trực tiếp -->
<TextView
    android:id="@+id/tv_title"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Tiêu Đề"
    android:textSize="24sp"
    android:textColor="@color/text_primary"
    android:textStyle="bold"/>
```

**Cho EditText:**
```xml
<EditText
    android:id="@+id/et_email"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:hint="Email"
    android:inputType="textEmailAddress"
    style="@style/TextStyle_Body"
    android:foreground="?attr/selectableItemBackground"/>
```

### Quy Tắc Đặt Tên Resource
- **Layouts**: `activity_login.xml`, `fragment_profile.xml`, `item_user.xml`
- **Colors**: `color_primary.xml`, `color_text.xml`
- **Styles**: `style_text.xml`, `style_button.xml`
- **Drawables**: `drawable_ripple_button.xml`, `drawable_ic_menu.xml`

### Kiểm Tra Khi Tạo Layout
- ✅ Mọi Button/ImageButton có ripple effect
- ✅ Mọi TextView/EditText/Button sử dụng style
- ✅ Layout được apply ViewBinding
- ✅ Không có hardcoded color/size trong layout
- ✅ Tên file layout theo quy ước (activity_*/fragment_*/item_*)

## ClaudeKit Agent Workflows

### Agent Workflow cho Android Development

#### **Workflow 1: Thêm Feature Mới**
```bash
# Bước 1: Lập kế hoạch chi tiết
/ck:plan "implement user profile screen"
# → Planner Agent tạo chi tiết plan

# Bước 2: Thực thi plan
/ck:cook
# → Tự động:
#   - Tạo Activity extends BaseActivity
#   - Tạo layout XML với ripple + style
#   - Tạo DAO/Entity nếu cần database
#   - Tạo ViewModel

# Bước 3: Kiểm thử tự động
/ck:test
# → Tester Agent sinh unit tests

# Bước 4: Code review tự động
/ck:code-review
# → Code Reviewer kiểm tra:
#   - Base classes, ripple, style
#   - Naming conventions, architecture
#   - Security, performance

# Bước 5: Cập nhật documentation
/ck:docs
# → Tự động update README.md, CHANGELOG
```

#### **Workflow 2: Bug Fixing**
```bash
# Phân tích vấn đề
/ck:debug "investigate login crash"
# → Debugger phân tích logs

# Lập kế hoạch fix
/ck:plan "fix authentication issue"

# Thực thi fix
/ck:cook

# Kiểm thử fix
/ck:test

# Code review
/ck:code-review
```

#### **Workflow 3: Refactoring**
```bash
# Lập kế hoạch refactor
/ck:plan "refactor user adapter for better performance"

# Thực thi refactor
/ck:cook

# Kiểm thử + review
/ck:test
/ck:code-review

# Cập nhật docs
/ck:docs
```

### Agents Hữu Ích Nhất Cho Android

| Agent | Công Dụng | Khi Sử Dụng |
|-------|-----------|-----------|
| **Planner** | Lập kế hoạch architecture, feature | Trước khi code feature mới |
| **Tester** | Sinh unit tests, instrumented tests | Sau khi implementation |
| **Code Reviewer** | Enforce rules (ripple, style, base class) | Sau implementation |
| **Debugger** | Phân tích crash logs, logcat | Khi có bug |
| **Docs Manager** | Update README, API docs | Sau feature/bug fix |
| **Git Manager** | Clean commits, branch management | Tự động |

### Rules Agents Sẽ Enforce

#### Code Reviewer Sẽ Kiểm Tra
✅ Tất cả Activity extends `BaseActivity<VB>`  
✅ Tất cả Fragment extends `BaseFragment`  
✅ Tất cả Adapter extends BaseAdapter phù hợp  
✅ Ripple effect trên buttons (bắt buộc)  
✅ Style được dùng cho text views (bắt buộc)  
✅ Layout XML có tên đúng theo quy ước  
✅ ViewBinding được sử dụng  
✅ Naming conventions đúng  

#### Tester Agent Sẽ
✅ Sinh unit tests cho UseCase & ViewModel  
✅ Sinh instrumented tests cho UI  
✅ Xác nhận tests pass  
✅ Report test coverage  

#### Docs Manager Sẽ
✅ Update README.md  
✅ Update CHANGELOG.md  
✅ Sync documentation  

## Hàm Tiện Ích Phổ Biến

### SpManager (SharedPreferences Manager)
Quản lý SharedPreferences - lưu/lấy các cài đặt đơn giản.

```kotlin
// Inject trong Activity/Fragment
@Inject lateinit var spManager: SpManager

// Lưu dữ liệu
spManager.putString("user_name", "John Doe")
spManager.putInt("user_id", 123)
spManager.putBoolean("is_logged_in", true)

// Lấy dữ liệu (với giá trị mặc định)
val userName = spManager.getString("user_name", "Guest")
val userId = spManager.getInt("user_id", 0)
val isLoggedIn = spManager.getBoolean("is_logged_in", false)

// Xóa dữ liệu
spManager.remove("user_name")
spManager.clear() // Xóa tất cả
```

### SystemUtil (Công Cụ Hệ Thống)
Các hàm tiện ích liên quan đến hệ thống.

```kotlin
// Cài đặt ngôn ngữ (Locale)
SystemUtil.setLocale(context) // Dùng trong attachBaseContext
```

### BaseActivity Utilities
Các hàm tiện ích có sẵn trong BaseActivity.

```kotlin
// Chuyển màn hình với animation
startNextActivity(MainActivity::class.java)
startNextActivity(MainActivity::class.java, bundle, isFinish = true)

// Chuyển màn hình và đặt làm task mới
startActivityNewTask(LoginActivity::class.java)

// Quản lý Fragment
replaceFragment(R.id.fragment_container, MyFragment(), backStack = "my_fragment")
addFragment(R.id.fragment_container, MyFragment())
hideFragment(myFragment)
removeFragment(myFragment)

// Dialog Loading
showLoading("Đang tải...")
hideLoading()

// Toast
showToast("Thành công!")
showToast("Lỗi xảy ra", Toast.LENGTH_LONG)

// Điều chỉnh Status Bar
setBaseStatusBar(isVisible = true, isLightIcons = true)
setBaseStatusBarColor(isLightIcons = false)
setBaseFullScreen()
setBaseHideNavigation()
```

### BaseFragment Utilities
Tương tự BaseActivity nhưng cho Fragment.

### Common Patterns

**Hiển thị loading khi load dữ liệu:**
```kotlin
override fun initData() {
    showLoading("Đang tải dữ liệu...")
    
    lifecycleScope.launch {
        try {
            val data = myViewModel.loadData()
            // Xử lý dữ liệu
        } catch (e: Exception) {
            showToast("Lỗi: ${e.message}")
        } finally {
            hideLoading()
        }
    }
}
```

**Observe LiveData:**
```kotlin
override fun initObserver() {
    myViewModel.userLiveData.observe(this) { user ->
        // Cập nhật UI khi dữ liệu thay đổi
        updateUserUI(user)
    }
}
```

**Room Database Query:**
```kotlin
@Inject lateinit var userDao: UserDao

override fun initData() {
    lifecycleScope.launch(Dispatchers.IO) {
        val users = userDao.getAllUsers() // Suspend function
        withContext(Dispatchers.Main) {
            updateList(users)
        }
    }
}
```

## Danh Sách Kiểm Tra (Checklist) Khi Tạo Dự Án Mới

- ✅ Copy project này và đổi package name
- ✅ Update `applicationId` trong `app/build.gradle.kts`
- ✅ Update `namespace` trong `app/build.gradle.kts`
- ✅ Tạo layout XML cho màn hình mới
- ✅ Tất cả Activity extends `BaseActivity<VB>`
- ✅ Tất cả Fragment extends `BaseFragment`/`BaseBottomFragment`
- ✅ Tất cả Adapter extends BaseAdapter phù hợp
- ✅ Thêm ripple effect cho tất cả nút bấm
- ✅ Dùng style cho tất cả text views
- ✅ Đăng ký Activity trong `AndroidManifest.xml`
- ✅ Thêm Entity/DAO/UseCase nếu cần dữ liệu mới
- ✅ Chạy `./gradlew clean && ./gradlew assembleDebug` để test build

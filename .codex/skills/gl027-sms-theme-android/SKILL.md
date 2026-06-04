---
name: gl027-sms-theme-android
description: Senior Android project skill for GL027-SMSColorGreen, an SMS/MMS messaging app with customizable colors, backgrounds, fonts, and themes. Use when modifying this Android repo, adding SMS inbox/conversation/send/media/theme features, touching permissions/default SMS role, Room/Hilt/ViewBinding architecture, XML UI, adapters, styles, or project documentation/rules.
---

# GL027 SMS Theme Android

Use this skill before changing `D:\ProjectByShark\GL027-SMSColorGreen`. Treat the app as a real SMS/MMS client with heavy visual customization, not as the old template app.

## Project Snapshot

- Package/namespace: `com.example.myapplication` for now.
- Android: Kotlin, AGP 9.2.1, Kotlin 2.3.10, compileSdk 36.1, minSdk 24, targetSdk 36.
- Core stack: ViewBinding, Hilt, Room, Lifecycle, WorkManager, RxJava 3, Coroutines, Glide, Retrofit/OkHttp, Firebase Remote Config/Analytics/Crashlytics.
- Existing source shape:
  - `base/`: `BaseActivity`, `BaseFragment`, `BaseBottomFragment`, `BaseDialog`, RecyclerView base adapters.
  - `ui/`: splash, onboarding, language, main, uninstall, alert permission flow.
  - `data/local/`: Room `AppDatabase`, DAO/entity scaffold.
  - `domain/layer` and `domain/usecase`: model/usecase pattern.
  - `utils/`: `PermissionUtils`, `SpManager`, notification/system helpers.
- Existing manifest already requests SMS/MMS-related permissions plus notification/exact alarm/full-screen intent permissions. Do not assume these are sufficient for default SMS behavior; verify Android role and required components for each feature.

## Workflow

1. Read only the local files needed for the task, starting from this skill and `PROJECT_COMMANDMENTS.md`.
2. Identify the touched feature boundary: SMS data, conversation UI, composer, media/MMS, theme customization, onboarding/permissions, storage, or notification.
3. Reuse existing base classes and hooks:
   - Activity: must extend `BaseActivity<VB>(Binding::inflate)` with `@AndroidEntryPoint`.
   - Fragment/bottom sheet: must extend `BaseFragment` or `BaseBottomFragment`.
   - Dialog: `BaseDialog`.
   - RecyclerView adapter: must extend one of the project base adapters. Prefer `BaseListAdapter` with `DiffUtil`; use multi-type base adapters for chat rows.
4. Keep Clean Architecture shape:
   - Device/provider or network/storage access belongs in data layer repositories/datasources.
   - App DB state belongs in Room entities/DAO.
   - UI-ready objects belong in domain models.
   - Business operations belong in use cases.
   - Activities/fragments bind UI and observe state; they do not parse SMS/MMS payloads directly.
5. Before finishing, run the narrowest meaningful verification. Prefer:
   - `gradlew.bat compileDebugKotlin`
   - `gradlew.bat lintDebug`
   - focused unit tests when logic is touched.
6. Follow Git safety strictly: never commit or push unless the user explicitly asks for it. Read-only git commands are allowed when useful.

## SMS/MMS Guardrails

- Check default SMS role before write/send operations. Use `RoleManager.ROLE_SMS` on Android Q+ and `Telephony.Sms.getDefaultSmsPackage` on older APIs, following `PermissionUtils`.
- Runtime permission is separate from default app role. Gate READ/SEND/RECEIVE SMS, POST_NOTIFICATIONS, exact alarm, and full-screen intent independently.
- For full default SMS support, confirm manifest components before coding: SMS deliver receiver, MMS WAP push receiver, SENDTO/SMSTO compose entry point, and any respond-via-message service required by Android for default SMS apps.
- Do not do SMS/MMS provider queries on the main thread. Use coroutines/Rx on IO dispatchers and expose immutable UI state.
- For media messages, handle `content://` URIs with persisted/read grants where appropriate, use `FileProvider` only for app-owned files, and avoid leaking absolute file paths.
- Preserve user privacy: never log message bodies, phone numbers, attachment URIs, or full contact identifiers. Logs may contain counts, IDs masked at the edge, and high-level states.
- Treat MMS as a first-class feature, not just "SMS with image". Model attachments, send status, download status, content type, size, and failure reasons.
- Handle dual-SIM, no telephony hardware, airplane mode, invalid recipients, long SMS segmentation, delivery errors, and revoked default-SMS role.

## Theme Customization Guardrails

- Theme is product data. Model it explicitly: colors, bubble shape, conversation background, font family/size, wallpaper/image source, dark/light mode, and per-conversation overrides if needed.
- Store reusable theme definitions in Room when they need querying/listing/sync; store only simple active selections in `SpManager`.
- Apply themes from a single source of truth. Avoid scattering raw colors/fonts across screens.
- XML text must use styles. If a touched layout has hardcoded `textSize`, `textColor`, or `textStyle`, clean that touched area.
- Clickable rows/buttons need ripple or Material components.
- Keep chat UI performant: use `RecyclerView`, stable IDs where useful, `DiffUtil`, pagination/windowed loading, Glide thumbnails, and background decoding.

## UI Rules

- Every screen gets its own XML layout: `activity_*`, `fragment_*`, `item_*`, `dialog_*`.
- Use ViewBinding only. Do not add `findViewById`.
- Never create a plain Android `Activity`, `Fragment`, or `RecyclerView.Adapter`. New Activities, Fragments, and Adapters must inherit from the project Base classes.
- Prefer `ConstraintLayout` for complex screens and simple `LinearLayout` only when it is truly linear.
- Put user-facing text in `strings.xml`; update existing translations when practical or leave an obvious default fallback.
- Every text view created by Codex or requested by the user must use exactly one of the existing text styles: `@style/TextView_4`, `@style/TextView_5`, `@style/TextView_6`, or `@style/TextView_7`.
- Do not add hardcoded `android:textSize`, `android:textColor`, `android:textStyle`, or `android:fontFamily` to TextView/Button/EditText in new XML. Add or adjust the existing `TextView_4..7` styles only if the user explicitly wants to change global typography.
- Keep dimensions in `dimens.xml` when reused; do not introduce new one-off magic values across many files.
- Do not copy the current `activity_main.xml` placeholder style into production UI.

## Git Rules

- Do not push code to any remote without an explicit user command or approval.
- Do not commit unless the user explicitly asks for a commit.
- Read-only commands such as `git status`, `git diff`, and `git log` are allowed when needed.
- Before committing or pushing, summarize the planned files/actions and wait for approval unless the user already gave the direct command.

## Data Model Hints

Likely future modules:

- `data/sms` or `data/messaging`: provider queries, send operations, MMS helpers.
- `data/local/entities`: app-owned cached threads/messages/themes, not raw provider ownership unless deliberately mirrored.
- `domain/layer`: `ConversationModel`, `MessageModel`, `AttachmentModel`, `ThemeModel`.
- `domain/usecase`: `GetConversationsUseCase`, `GetMessagesUseCase`, `SendTextMessageUseCase`, `SendMediaMessageUseCase`, `ApplyThemeUseCase`.
- `ui/inbox`, `ui/conversation`, `ui/theme`, `ui/composer`: feature folders with their own adapters and view models.

## References

- Read `PROJECT_COMMANDMENTS.md` at repo root for the durable commandments.
- Read `PROJECT_COMMANDMENTS_OVERRIDE.md` for mandatory overrides about `TextView_4..7` styles, Base class inheritance, and Git safety.
- Read `references/repo-map.md` when you need a compact map of files and conventions.

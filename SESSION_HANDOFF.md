# GL027 Session Handoff

Use this file when the user asks "lam toi dau roi?" in a later session.

## Project Rules To Remember

- Do not commit or push unless the user explicitly asks.
- New Activity/Fragment/Adapter must inherit project Base classes.
- Any text created by Codex or requested by the user must use `TextView_4`, `TextView_5`, `TextView_6`, or `TextView_7`.
- No hardcoded text size/color/style/font in new XML.
- Read `.codex/skills/gl027-sms-theme-android/SKILL.md` and `PROJECT_COMMANDMENTS_OVERRIDE.md` before coding.

## Completed Work In This Thread

1. Created local project skill/rules:
   - `.codex/skills/gl027-sms-theme-android/SKILL.md`
   - `.codex/skills/gl027-sms-theme-android/references/repo-map.md`
   - `.codex/skills/gl027-sms-theme-android/agents/openai.yaml`
   - `PROJECT_COMMANDMENTS.md`
   - `PROJECT_COMMANDMENTS_OVERRIDE.md`
   - `AGENTS.md` points to the skill/rules.

2. Main navigation:
   - `MainActivity` uses Material `BottomNavigationView`.
   - Menu items: Home, Themes, Stickers, Setting.
   - Selected color: `R.color.col_main`.
   - Unselected color: `R.color.grey`.
   - Text appearance: `@style/TextView_5`.
   - Added `res/menu/menu_main_bottom_nav.xml`.
   - Added `res/color/bottom_nav_item_color.xml`.

3. Main fragments:
   - Home, Themes, Stickers, Setting fragments exist under `ui/main/func`.
   - They inherit `BaseFragment`.
   - `MainActivity` switches fragments into `frMainContent`.

4. RoundImageView:
   - Removed forced `CENTER_CROP`.
   - If `android:scaleType` is not explicitly set, default is forced to `FIT_CENTER`.
   - If user explicitly sets `centerCrop`, it is respected.

5. `ic_iap.xml` resource linking fix:
   - Replaced unsupported SVG pattern `url(#pattern0_...)` with `@color/transparent`.
   - `rg "url\\(#"` found no more occurrences afterward.

6. SMS realtime flow:
   - Added `sms_helper` package:
     - `SmsMessageModel.kt`
     - `SmsReader.kt`
     - `SmsRepository.kt`
   - `SmsReader` queries `Telephony.Sms.CONTENT_URI` on `Dispatchers.IO`.
   - `SmsRepository` uses `ContentObserver` + `callbackFlow` + `conflate`.
   - `MainActivity` requests `READ_SMS` and `RECEIVE_SMS`.
   - `HomeFragment` collects SMS Flow with `repeatOnLifecycle`.
   - `SmsMessageAdapter` extends `BaseListAdapter`.
   - `item_message.xml` text uses project styles; bound colors are set from resources in adapter.

## Verification Done

- `gradlew.bat compileDebugKotlin`: passed after the latest SMS changes.
- `gradlew.bat lintDebug`: was attempted earlier and failed because of existing MissingTranslation debt, first seen at `txt_select` missing in locale `ae`. Do not treat that as caused by the SMS/navigation work unless rechecked.

## Current Worktree Notes

- No commit or push was done.
- `git status --short` shows many modified/added files, including user-added drawables/layouts and Codex-created SMS/navigation files.
- Be careful not to revert user assets or unrelated user edits.

## Likely Next Steps

- Test SMS permission/runtime behavior on a real device or emulator with SMS provider data.
- Add empty state when SMS permission is denied or the device has no messages.
- Decide whether Home should show all SMS rows or grouped conversations by `threadId`.
- Consider adding a ViewModel/Hilt repository later if SMS logic grows.

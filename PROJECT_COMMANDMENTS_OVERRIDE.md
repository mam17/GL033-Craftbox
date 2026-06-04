# GL027 Mandatory Overrides

These rules override any older template guidance.

## Text Styles

All text created by Codex, or created because of a user request, must use one of the existing XML text styles:

- `@style/TextView_4`
- `@style/TextView_5`
- `@style/TextView_6`
- `@style/TextView_7`

Do not hardcode `android:textSize`, `android:textColor`, `android:textStyle`, or `android:fontFamily` in new TextView/Button/EditText XML.

Only change the global `TextView_4`, `TextView_5`, `TextView_6`, or `TextView_7` style definitions when the user explicitly asks to change global typography.

## Base Classes

All new Android UI classes must inherit from the project base classes:

- Activity: `BaseActivity<VB>`
- Fragment: `BaseFragment` or `BaseBottomFragment`
- Adapter: `BaseAdapter`, `BaseListAdapter`, `BaseMultiAdapter`, or `BaseMultiListAdapter`

Do not create plain Android `Activity`, `Fragment`, or `RecyclerView.Adapter` classes in this project.

## Git Safety

Do not push code to any remote unless the user explicitly asks for it in the current conversation.

Do not commit code unless the user explicitly asks for a commit. It is OK to run read-only git commands such as `git status`, `git diff`, and `git log` when needed.

Before any commit or push, show the planned files/summary and wait for user approval unless the user already gave a direct commit/push command.

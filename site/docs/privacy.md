# privacy

Will is built so that the question `where does my data go` has the same answer for every feature: nowhere.

## what stays on device

- every notification Will captures
- every task you create, complete, or delete
- the per-app filter
- the pinned-apps list
- the focus dashboard data (focus score, time-on-task, context switches)

all of it lives in:

- a Room database stored inside Will's private app sandbox
- a `SharedPreferences` file inside the same sandbox

other apps cannot read these files. nothing is uploaded.

## what Will does not do

- no analytics SDK
- no crash reporting
- no telemetry of any kind
- no network calls — Will does not request `INTERNET` permission

## what Will reads

| source | why |
|-------|-----|
| posted notifications | to turn them into tasks |
| installed apps (PackageManager) | to render the app drawer |
| your tap on a notification card | to fire the original `PendingIntent` and open the source conversation |

## what you can erase

- **delete a task** — removed from the database, no soft-delete
- **delete all tasks** — `PrivacySettings` has a one-tap action that wipes the entire `Task` table
- **uninstall** — Android removes the sandbox, including the database and preferences

selective deletion (for example, "wipe everything older than 30 days") is on the [roadmap](/docs/roadmap).

## what is still pending

the privacy story has gaps that the project is closing:

- on-device guarantees for accessibility-driven features once they ship — they will not be allowed to leave the device
- explicit user consent dialogs before any feature with broader permission scope is enabled

these are tracked under US-07 in `docs/99-TASKS.md`.

## permissions, in plain language

| permission | reads | writes | leaves device |
|------------|-------|--------|---------------|
| BIND_NOTIFICATION_LISTENER_SERVICE | active notifications | local Room database | no |
| SYSTEM_ALERT_WINDOW | nothing | a floating window | no |
| RECEIVE_BOOT_COMPLETED | boot signal | nothing | no |

if a future feature ever requires a new permission or any network access, it will land behind an explicit setting that defaults to off.

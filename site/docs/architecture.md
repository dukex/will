# architecture

Will follows domain / infrastructure separation and avoids dependency injection frameworks. every layer is small enough to reason about by reading the file.

## domain model

`Task` is a pure Kotlin data class — zero Android or Room dependencies. its fields cover the lifecycle (`startedAt`, `completedAt`, `contextSwitchCount`, `status`) and the deep-link payload (`notificationKey`, `conversationKey`, `unreadCount`).

`infrastructure/room/TaskEntity.kt` mirrors the domain class and provides `toTask()` / `Task.toEntity()` mappers. domain code never imports Room.

## layers

| layer | package | responsibility |
|-------|---------|---------------|
| domain | `task/` | Task, TaskRepository interface, TaskViewModel |
| infrastructure | `infrastructure/room/`, `infrastructure/memory/` | Room entity, DAO, database singleton, in-memory test doubles |
| notification | `notification/` | listener, processor, action store |
| launcher | `launcher/` | app list and pinned-apps repositories and ViewModels |
| ui | `ui/` | composable screens and components |
| overlay | `overlay/` | system-alert-window service for the active-task bubble |

## notification pipeline

```
notification posted
  → WillNotificationListenerService.onNotificationPosted()
  → SharedPrefsAppFilterRepository — drop notifications from filtered packages
  → NotificationProcessor.process() — derive Task and conversationKey (packageName + normalized title)
  → TaskDao.upsertByConversationKey() — refresh open task or insert new one
  → TaskFeed recomposes via StateFlow
```

`upsertByConversationKey` is the load-bearing piece for messengers: a unique index on `conversationKey` (filtered to open tasks) collapses repeated WhatsApp-style notifications into a single card with an `unreadCount` counter. tapping the card looks up the latest `notificationKey` and fires the original `contentIntent`, opening the specific conversation rather than the app's home screen.

## deep-link durability

`NotificationActionStore` caches `PendingIntent`s in memory for fast tap response. when the cache misses (after a service restart), Will falls back to `notificationKey` persisted in Room: it scans the active notifications, finds the one matching the key, and uses its `contentIntent`.

## overlay communication

`ActiveTaskOverlayService` runs as a `TYPE_APPLICATION_OVERLAY` window. it talks back to `MainActivity` through a `SharedFlow` (replaced an earlier `LocalBroadcastManager` implementation). the flow emits three actions:

- `ACTION_DONE` — mark the task done
- `ACTION_LATER` — dismiss the overlay, keep the task
- `ACTION_DELETE` — delete the task

## launcher ergonomics

- `excludeFromRecents=true` — Will never appears in the recents screen
- `stateNotNeeded=true` — no `onSaveInstanceState` round-trip
- `screenOrientation=portrait` — portrait only
- `windowSoftInputMode=stateAlwaysHidden` — keyboard never auto-pops on home
- swipe-up to the app drawer is implemented through `nestedScroll.onPostScroll`, not `pointerInput`, because `LazyColumn` consumes the vertical drag first

## pinned apps

`PinnedAppsRepository` stores up to 5 package names as a pipe-separated string in `SharedPreferences`. `PinnedAppsViewModel` joins those names with the installed-apps list and emits a `StateFlow<List<LauncherApp>>` in pin order. the row is rendered through a `LazyListScope` extension consumed by `TaskFeed`'s `header` slot, so pinned apps and tasks share a single scroll surface.

## no Hilt

dependency injection is manual via `ViewModelProvider.Factory`. each ViewModel receives a `CoroutineDispatcher` so tests can inject `UnconfinedTestDispatcher` without mocking a framework.

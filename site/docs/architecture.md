# architecture

Will follows domain/infrastructure separation.

## domain model

`Task` is a pure Kotlin data class — zero Android or Room dependencies.

## layers

| layer | package | responsibility |
|-------|---------|---------------|
| domain | `task/` | Task model, TaskRepository interface, TaskViewModel |
| infrastructure | `infrastructure/room/` | Room entity, DAO, database singleton |
| notification | `notification/` | listener, processor, action store |
| UI | `ui/` | composable screens and components |
| overlay | `overlay/` | system alert window overlay service |
| launcher | `launcher/` | app list repository and ViewModel |

## notification pipeline

```
notification posted
  → WillNotificationListenerService.onNotificationPosted()
  → NotificationProcessor.process() — maps notification to Task (or null)
  → TaskDao.insert() — persists to Room
  → TaskFeed recomposes — StateFlow emits new list
```

## overlay communication

the overlay communicates back to MainActivity via SharedFlow:
- ACTION_DONE — mark task done
- ACTION_LATER — dismiss overlay, keep task
- ACTION_DELETE — delete task

## no Hilt

dependency injection is manual via ViewModelProvider.Factory. each ViewModel receives a CoroutineDispatcher for testability.

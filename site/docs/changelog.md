# changelog

## [unreleased]

### added
- the focus screen now names what pulls you away — the apps you most often jumped to while a task was open, and how many times. Will has been recording this since the task history landed and never showed it to you. Nothing leaves your phone, it is only the app's name and a count, and clearing your data clears it
- the focus screen now shows the week, not just today — a bar for each of the last 7 days, how long you actually spent on task across them, your longest unbroken stretch, and a switch to 30 days. Worked out from the history Will already keeps, so a day you touched a task for twenty minutes reads as twenty minutes, whatever day the task was created on. Two tasks open across the same hour count as one hour, because that is how much of your attention it took, and a stretch that runs past midnight is split between the two days it belongs to
- a quiet day no longer hides your week — the focus screen used to say "nothing yet" whenever nothing happened today, even with a full week behind it
- search by meaning, if you want it — turn it on in settings and typing "banco" finds your bank even though the word is nowhere in its name. It runs Google's model on your phone, only when searching by name finds nothing, and your searches still go nowhere. Off until you ask for it, and settings says plainly what turning it on brings
- a task opens the screen, not just the app — "ligar para Miguel" opens the dialer with Miguel ready, "ir para Pocrane" opens the map, a link opens the browser. Will never places the call itself, it just gets you there
- search finds your tasks too — the search box in the app drawer now looks through what you still have to do as well as your apps, with the tasks first
- find an app by what it is for — type "musica" and get your music apps, "mapa" and get your maps, even when you cannot remember what the app is called
- a long press on the Will icon offers the task you are in the middle of, so what you are working on is one gesture away from anywhere
- Will stops showing the notification you keep throwing away — delete the same one three times and it stops reaching your feed. Finish one and it comes back, because acting on it proves it mattered. You can see everything it silenced, and undo any of it, under data & privacy. It only learns from notifications you deleted without finishing, never from tasks you typed yourself
- Will now speaks Portuguese — the texts the launcher itself writes follow your phone's language, including the new task titles it derives from your notifications
- your feed now says what to do, not who it was from — a WhatsApp message from Maria reads "responder Maria", a missed call reads "ligar de volta para Miguel". Anything that is not a conversation, like a bank alert, arrives exactly as it was. The wording follows your phone's language, and the original title is kept so nothing is lost
- a task you type now opens the app you mentioned — write "mandar o contrato no WhatsApp" and tapping the task opens WhatsApp. Before this, typing a task gave you something that did nothing when tapped. Will only follows an app name you actually wrote; it does not try to guess which app you meant
- Will points out what you have probably already done — a task you opened, spent time on and left alone now reads "looks finished" in the feed, so clearing it is one tap on the checkbox that was already there. It never completes anything by itself, and a notification you never opened is never suggested, however long it has been sitting there. Worked out from timestamps and app names only, nothing from inside your apps
- notifications in Portuguese no longer clutter the feed — "Baixando vídeo", "Carregamento concluído", "Atualização concluída", "Sua hora de dormir começa" and the rest of the housekeeping chatter are now recognised and skipped, in Portuguese as well as English, with or without accents
- the feed says when a task was not yours — a task another app shared into Will reads "shared" next to the app it came from, and one an agent wrote reads "from agent". a notification or something you typed says nothing extra, because that is the ordinary case and naming it would add a word to every row
- share anything into Will — Will now appears in the Android share sheet. share a message, a link or a selected paragraph from any app and it lands on your home screen as a task, with the app you shared from kept as the source, so tapping the task takes you back there. sharing never opens Will, you stay in the app you were in
- create a task from a link — `will://task?title=pay%20the%20bill` puts that task on your home screen, so a script, an automation tool or an agent on your phone can hand you an intention without Will having to talk to anything
- Will remembers how a task went, not just where it ended up — each task now keeps its own history: when you started it, every time you were pulled away and which app pulled you, when you paused, finished or reopened it. coming back to a task reads as resuming it instead of starting over, which is what the focus dashboard needs before it can show trends across days. deleting a task takes its history with it, and clearing your data clears all of it
- first-run onboarding — Will now walks you through making it the default launcher, granting the active-task overlay, and (optionally) enabling the notification listener, instead of dropping you into a wall of system dialogs. you can re-run the flow any time from settings.
- distinctive Will icon — a flat geometric "W" stroke on true black replaces the default platform mark. ships with a monochrome variant so Android 13+ themed icons inherit your system tint.
- pin up to 5 apps to the home screen — long-press an app in the drawer to pin or unpin it; pinned apps live above the task feed for one-tap launch
- visible checkbox to mark tasks as done — no more hidden gestures
- separate "done" screen accessible from top bar — completed tasks live off the main feed
- apps button in top bar — no need to discover the swipe-up gesture
- conversation dedup via database unique index — same conversation never creates duplicate tasks, even after app restart
- unread counter on tasks — when more than one notification piles up for the same conversation, the task shows "N new" so you know how many messages are waiting

### changed
- a task you typed no longer shows the word "manual" where an app name belongs — it just shows the time
- pinned apps and the task feed now share one scroll area — the pinned row no longer holds a fixed slice at the top of the home screen, so the feed reclaims the space when you have messages waiting and pinned apps stay reachable by scrolling up

### fixed
- settings is reachable on a fresh install — data & privacy, re-run onboarding and lifecycle tracking used to be hidden until some app had sent you a notification
- searching the app drawer now works in Portuguese — typing "itau" finds Itaú and "configuracoes" finds Configurações. Accents used to make the app invisible to its own name. You can also type initials ("gd" for Google Drive) or just the consonants ("tlgrm" for Telegram), and the closest match comes first
- only the task you are working on is highlighted as active — every other started-but-not-finished task now shows as paused, with its own muted dot, so the home screen reflects what you are actually doing right now
- uninstalled apps no longer linger in the pinned row — the launcher refreshes its app list every time you return home, so pins for apps you removed disappear automatically
- WhatsApp and other messengers no longer spam the feed — repeated notifications from the same chat collapse into a single task and the tap always opens the most recent message
- tapping a task now opens the specific conversation (WhatsApp, Calendar, etc.) instead of the app's home screen — notification key persisted in database
- notification filter toggle now updates the UI reactively
- replace broadcast with SharedFlow for overlay→activity communication
- active task overlay shows the correct tapped task
- overlay actions (done/delete/later) now reach MainActivity

### removed
- swipe-to-done and swipe-to-delete gestures — caused accidental actions
- long-press to action sheet — hidden gesture replaced by visible controls
- system noise notifications (alarms, connectivity, charging, OTA updates) no longer create tasks

## [0.1.0] — 2026-04-15

### added
- basic launcher (app grid, swipe-up gesture, launch apps)
- notification capture (NotificationListenerService → Task → Room)
- task feed (HomeScreen): tap to open source app, long-press for action sheet
- Room database with TaskEntity, TaskDao, WillDatabase
- active task overlay service (TYPE_APPLICATION_OVERLAY)

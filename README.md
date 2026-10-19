# Reactive Priority To-Do List App (Jetpack Compose & Firebase Realtime Database)

**Mobile Application Development Assignment**  
**Subject Code:** 702AI0E002  
**Institution:** School of Technology Management & Engineering, SVKM's NMIMS, Mumbai  
**Academic Year:** 2024–25  

---

## 1. Project Overview & Aim

The objective of this project is to develop a reactive, priority-based To-Do list Android application using **Kotlin**, **Jetpack Compose (Material 3)**, and **Firebase Realtime Database**. The application implements full **CRUD (Create, Read, Update, Delete)** functionality and dynamically sorts tasks by priority (**High → Medium → Low**), updating in real time across clients through Firebase's reactive streaming.

### Key Features
- **Create Tasks:** Add tasks with a description and priority level (`High`, `Medium`, `Low`).
- **Dynamic Priority Sorting:** Tasks are dynamically sorted as **High → Medium → Low** in a `LazyColumn`, with secondary sorting by newest creation/update timestamp.
- **Real-Time Database Observation:** Powered by Kotlin Coroutines `callbackFlow` and Firebase `ValueEventListener`, reflecting cloud changes immediately without manual polling.
- **Update Tasks:** Edit both description and priority inline via an interactive Material 3 dialog; priority updates immediately re-position the task in the list.
- **Swipe-to-Delete:** Uses Material 3 `SwipeToDismissBox` to dismiss and delete tasks with animated background indicators and undo/status snackbars.
- **Lifecycle-Aware State Management:** State is modeled via a sealed `TodoListUiState` exposed through `StateFlow` and observed in Compose with `collectAsStateWithLifecycle()`.
- **Resilient State Handling:** Dedicated UI presentations for **Loading**, **Empty List**, and **Configuration / Error** states with a retry mechanism and an instant In-Memory Demo mode.

---

## 2. Architecture & Technical Design

The application follows the recommended Android **MVVM (Model-View-ViewModel)** architectural pattern with unidirectional data flow (UDF).

```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│   TodoListScreen (LazyColumn, SwipeToDismissBox, etc.) │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow<TodoListUiState>
                            │ User Actions (CRUD)
┌───────────────────────────┴────────────────────────────┐
│                    TodoListViewModel                   │
│         Manages UI State, Priority Sorting Logic       │
└───────────────────────────▲────────────────────────────┘
                            │ Flow<Result<List<Task>>>
┌───────────────────────────┴────────────────────────────┐
│                     TaskRepository                     │
│        (FirebaseTaskRepository / InMemoryTaskRepo)     │
└───────────────────────────▲────────────────────────────┘
                            │ callbackFlow & ValueEventListener
┌───────────────────────────┴────────────────────────────┐
│               Firebase Realtime Database               │
│                (Cloud NoSQL & Offline)                 │
└────────────────────────────────────────────────────────┘
```

### Core Components
1. **`Priority` (`data.model.Priority`)**:
   - Enum with levels: `HIGH (3)`, `MEDIUM (2)`, `LOW (1)`.
   - Provides safe string parsing and UI color mapping.
2. **`Task` & `TaskDto` (`data.model.Task`)**:
   - Domain model and Firebase DTO with null-safe mapping and Map converters.
3. **`FirebaseTaskRepository` (`data.repository.FirebaseTaskRepository`)**:
   - Implements `TaskRepository`.
   - Enables offline disk persistence via `setPersistenceEnabled(true)`.
   - Transforms Firebase `ValueEventListener` snapshots into a reactive Kotlin Coroutine `Flow<Result<List<Task>>>` using `callbackFlow`.
   - Handles push keys (`push()`), child updates (`updateChildren()`), and deletions (`removeValue()`).
4. **`TodoListViewModel` (`ui.todo.TodoListViewModel`)**:
   - Manages UI state in a `StateFlow<TodoListUiState>`.
   - Handles input validation, error recovery, dialog state, and notifications.
5. **UI Layer (`ui.todo.*`)**:
   - `TodoListScreen`: Root screen hosting `TopAppBar`, priority count banner, `LazyColumn`, `FloatingActionButton`, and `SnackbarHost`.
   - `TaskItem`: Swipeable task card wrapped in `SwipeToDismissBox`.
   - `TaskDialog`: Material 3 `AlertDialog` for Add & Edit operations with single-select priority chips.
   - `PriorityBadge`: Color-coded priority pill indicator.

---

## 3. Firebase Realtime Database Setup Guide

Follow these exact steps to connect the application to your live Firebase project:

### Step 1: Create a Firebase Project
1. Navigate to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** (or create a new project named e.g., `Todo-Realtime-App`).
3. Follow the prompts (Google Analytics is optional) and click **Create Project**.

### Step 2: Register the Android App
1. On your Firebase project overview page, click the **Android** icon to add an Android application.
2. Enter the exact Android package name:
   ```
   com.fahim.geminiApiComposeStarter
   ```
3. (Optional) Provide an App nickname (e.g. `NMIMS ToDo App`).
4. Click **Register app**.

### Step 3: Download & Place `google-services.json`
1. Download the generated `google-services.json` configuration file from Firebase.
2. Place the downloaded `google-services.json` directly into the `app/` folder of this project:
   ```
   GeminiApiComposeStarter/
   └── app/
       └── google-services.json    <-- Place here
   ```
3. *Note on Gradle Automation:* `app/build.gradle.kts` contains conditional plugin detection:
   ```kotlin
   if (file("google-services.json").exists()) {
       apply(plugin = "com.google.gms.google-services")
   }
   ```
   As soon as `google-services.json` is added, Gradle automatically applies Google Services and initializes Firebase on startup.

### Step 4: Create the Realtime Database
1. In the Firebase Console left sidebar, go to **Build** → **Realtime Database**.
2. Click **Create Database**.
3. Choose your nearest database location (e.g., `United States` or `Singapore`).
4. Under Security Rules, select **Start in test mode** for initial assignment testing, or use the security rules below.

### Step 5: Configure Database Security Rules
Navigate to the **Rules** tab in the Firebase Realtime Database console and paste one of the following configurations:

#### Option A: Quick Testing Rules (Development Mode)
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

#### Option B: Validated Production Schema Rules (Recommended)
This enforces data integrity, ensuring only valid priorities and non-empty descriptions can be written:
```json
{
  "rules": {
    "tasks": {
      ".read": true,
      ".write": true,
      "$taskId": {
        ".validate": "newData.hasChildren(['description', 'priority'])",
        "description": {
          ".validate": "newData.isString() && newData.val().length > 0"
        },
        "priority": {
          ".validate": "newData.val() == 'HIGH' || newData.val() == 'MEDIUM' || newData.val() == 'LOW'"
        },
        "timestamp": {
          ".validate": "newData.isNumber()"
        },
        "completed": {
          ".validate": "newData.isBoolean()"
        }
      }
    }
  }
}
```
Click **Publish** to save the rules.

---

## 4. Building, Testing, and Running the App

### Build the Project
To compile and assemble the debug APK:
```bash
./gradlew assembleDebug
```
The output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Run Automated Unit Tests
To execute all local unit tests (Priority sorting, ViewModel CRUD, state flows):
```bash
./gradlew test
```
The HTML test report is generated at:
`app/build/reports/tests/testDebugUnitTest/index.html`

### Install and Launch on an Android Emulator or Physical Device
Ensure ADB is available and run:
```bash
./gradlew installDebug
adb shell am start -n com.fahim.geminiApiComposeStarter/.MainActivity
```

---

## 5. Assignment Deliverables: Conclusion & Answers to Questions

### Section B.2: Conclusion (Learning Outcomes)

Completing this practical assignment provided comprehensive hands-on experience in building a modern, cloud-connected Android application:

1. **Using Firebase Realtime Database for Cloud Synchronization:**
   - Learned how Firebase Realtime Database operates as a NoSQL document/JSON tree store that pushes data changes instantaneously over a persistent WebSocket connection.
   - Mastered the integration of Firebase SDK with modern Kotlin Coroutines by utilizing `callbackFlow`. Wrapping Firebase's event listener (`ValueEventListener`) inside `callbackFlow` with `awaitClose` bridged legacy callback-driven asynchronous APIs with modern, reactive Flow streams.
   - Enabled offline persistence (`setPersistenceEnabled(true)`), which caches data on the client device and guarantees application availability even under intermittent network connectivity.

2. **Performing CRUD Operations and Managing Task Priorities:**
   - Implemented standard CRUD operations:
     - **Create:** Using Firebase `push().setValue()` to generate globally unique keys and insert tasks.
     - **Read:** Streaming data reactively through `addValueEventListener` on the `/tasks` reference.
     - **Update:** Performing partial child modifications via `updateChildren()`.
     - **Delete:** Removing nodes using `removeValue()`.
   - Solved the dynamic priority management challenge by creating a structured `Priority` enum (`HIGH`, `MEDIUM`, `LOW`) with assigned numeric weights. Implemented dynamic sorting in Compose so that any task creation or priority update triggers an immediate, seamless re-ordering of the list (`High → Medium → Low`).

3. **Modern Jetpack Compose UI & Interactive Patterns:**
   - Implemented interactive gesture-based UI components using Material 3 `SwipeToDismissBox`, providing visual swipe feedback and instant deletion.
   - Leveraged `LazyColumn` with stable item keys (`key = { it.id }`) for optimal rendering performance and smooth layout animations.
   - Built a robust UI state machine (`TodoListUiState`) handling all edge cases: loading spinners, empty list illustrations, and configuration banners with graceful fallback to demo mode.

---

### Section B.3: Assignment Viva & Theory Questions

#### Q1 (i): What is Firebase Realtime Database, and why is it suitable for real-time apps?

**Answer:**
**Firebase Realtime Database** is a cloud-hosted, NoSQL database provided by Google that stores data as one large JSON tree and synchronizes data across connected clients in real time.

**Why it is suitable for real-time apps:**
1. **Bi-directional WebSocket Communication:** Unlike traditional REST APIs that require periodic client polling or long polling to fetch updates, Firebase maintains a single, persistent bi-directional WebSocket connection between the client SDK and the cloud backend.
2. **Push-Based Reactive Data Flow:** When any client writes or modifies data, the Firebase server broadcasts the data delta to all actively subscribed clients in milliseconds (`< 100ms` latency).
3. **Local In-Memory and Disk Caching:** The Firebase SDK writes changes to an internal local cache first (providing optimistic UI updates) and synchronizes with the remote server asynchronously in the background.
4. **Ideal Use Cases:** Applications such as collaborative To-Do lists, chat applications, real-time multiplayer scoreboards, and live tracking dashboards benefit directly from this architecture because UI components stay continuously synchronized across all devices without user-initiated refreshes.

---

#### Q1 (ii): Explain how the Firebase Realtime Database manages synchronization between multiple clients. How does it handle offline scenarios?

**Answer:**

**Synchronization Between Multiple Clients:**
- When an Android app attaches a listener (`ValueEventListener` or `ChildEventListener`) to a database reference, the Firebase client establishes a WebSocket connection with the Firebase backend.
- When **Client A** modifies a node (e.g., adds or edits a task):
  1. The local SDK on Client A immediately updates its local cache, triggering a local data change event so the UI updates instantaneously (optimistic UI update).
  2. The SDK sends the write operation payload over the WebSocket connection to the Firebase cloud server.
  3. The Firebase server validates the payload against the project's **Firebase Security Rules**.
  4. Once committed, the server broadcasts the updated JSON delta to **Client B, Client C, etc.**, whose SDKs receive the event and invoke their respective `onDataChange` callbacks.

**Handling Offline Scenarios:**
- **Persistent Disk Caching (`setPersistenceEnabled(true)`):** When enabled, Firebase writes all synced data to SQLite-backed internal storage on the Android device. If the device loses internet connection or restarts offline, the app reads directly from local disk cache.
- **Offline Write Queueing:** Any write operations executed while offline (adding, updating, or deleting tasks) are queued locally in a persistent queue. The local listeners fire immediately so the user experiences zero disruption.
- **Automatic Re-synchronization on Reconnect:** As soon as network connectivity is restored, the Firebase client sends the queued operations to the server in the order they were generated.
- **Conflict Resolution:** If multiple clients modify the exact same node while offline, Firebase resolves conflicts by accepting the **last write committed to the server** ("last write wins"). Developers can also use `ServerValue.TIMESTAMP` or transactional operations (`runTransaction()`) to achieve atomic, conflict-free updates.

---

#### Q1 (iii): Compare Firebase Realtime Database with other database options available for Android. What are the benefits and drawbacks of using Firebase for a To-Do app?

**Answer:**

### Comparison Table

| Feature / Metric | Firebase Realtime Database | Cloud Firestore | Android Room (SQLite) | Cloud Storage |
| :--- | :--- | :--- | :--- | :--- |
| **Database Type** | Cloud NoSQL (JSON Tree) | Cloud NoSQL (Document-Collection) | Local Relational (SQL) | Object / Blob Storage |
| **Data Synchronization** | Automatic Real-Time (WebSocket) | Automatic Real-Time (gRPC stream) | None (Local only, requires custom sync) | Manual upload/download |
| **Offline Support** | Built-in offline cache & queue | Built-in offline cache & queue | Fully offline (local device database) | Manual caching needed |
| **Querying Capabilities** | Limited (Filter/sort on 1 child) | Advanced (Compound queries, indexing) | Extremely rich (Full SQL capabilities) | None (Key-value blob storage) |
| **Scalability** | Up to ~200k concurrent connections | Scales horizontally to millions | Limited to single device storage | High (Unlimited file storage) |
| **Pricing Model** | Based on Bandwidth (GB downloaded) & Storage | Based on Operations (Reads, Writes, Deletes) | Free (Local on-device) | Based on Storage & Bandwidth |

### Benefits of Using Firebase Realtime Database for a To-Do App:
1. **Zero Backend Maintenance:** No need to build, host, or maintain custom REST or WebSocket servers.
2. **Instant Multi-Device Sync:** Adding a task on a phone instantly displays on an emulator or another device in real time.
3. **Seamless Offline UX:** Users can create, edit, and delete tasks on the go without network connectivity; changes sync silently when connection returns.
4. **Simple Data Model:** A To-Do list maps directly to a simple JSON list (`/tasks/{taskId}`).

### Drawbacks of Using Firebase Realtime Database for a To-Do App:
1. **Limited Querying & Sorting:** Firebase Realtime Database can only sort or filter on a single property at a time on the server. Multi-level sorting (e.g., sort by priority first, then by timestamp) must be performed client-side in Kotlin.
2. **Bandwidth-Heavy on Large Trees:** Because data is stored as a single JSON tree, reading a parent node retrieves all child nodes and attributes, which can lead to unnecessary data usage if task attachments or subtasks grow large.
3. **Vendor Lock-in:** The application relies on Google Play Services and proprietary Firebase SDK APIs, making it harder to migrate to generic self-hosted backends compared to standard REST/GraphQL solutions.

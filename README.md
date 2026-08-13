# VibeFinance 📊✨

VibeFinance is a modern, high-fidelity personal finance and daily budgeting Android application. Built using Jetpack Compose and Kotlin, it delivers a state-of-the-art visual experience with interactive animations, custom card layouts, and local data persistence, directly inspired by premium financial tools like Buckwheat.

---

## 🚀 Features

### 📅 Daily Budgeting & Heatmap
- **Whole Budget Card**: Visualizes starting budgets with an elegant range indicator chip showing exact dates and days elapsed.
- **Spent & Remaining Wavy Bento Cell**: A fluid wave progress cell representing your remaining budget, dynamically adjusting wave frequency and amplitude. Tapping the cell toggles between "Remaining" and "Spent".
- **Days Left Bento Cell**: Displays a clean, concentric circular progress ring representing elapsed days of the active budget period.
- **Minimum & Maximum Spent Cards**: Showcases lowest and highest expenses with embedded Bezier spends curve animations and node markers.
- **Period Spends Heatmap Calendar**: Color-codes daily expenditures based on budget utilization, featuring radial glow backdrops ("spread") and overflow layout sizing for overspent days.

### 💳 Assets & Cards Manager
- **Interactive Credit Utilization**: Live progress tracking of credit limits, outstanding balances, and available credit grids.
- **Transparent Default Style**: Renders cards with a clean, fully transparent glass-like backdrop by default, adapting text colors dynamically for optimal readability in both dark and light modes.
- **Custom Card Themes**: Enables customizing cards with vibrant gradients (e.g. Sunset Glow, Ocean Breeze, Emerald Forest) and background patterns (e.g. Cyber Grid).

### 💰 Persistent Cashback Rates
- **Account-level Custom Rates**: Allows setting custom cashback return rates (%) for specific categories (Food, Transport, Shopping, Utilities, Other) directly inside each account's edit dialog.
- **Automated Calculation & Triage**: Maps categories (such as "Food & Drink" or "Groceries") via keyword matching to calculate exact cashback amounts.
- **History Tracker**: Displays green indicators showing calculated cashback directly on individual transaction line items.
- **Summary Tracker**: Aggregates and displays total lifetime cashback earned under the asset card.
- **Persistent Storage**: Utilizes local `SharedPreferences` to persist configured rates across restarts and system upgrades.

### ⚡ Smooth Transitions
- Replaced bouncy spring animations with high-performance ease-in-out page transitions (`tween(350)`), making tab switching feel stable, fluid, and robust.

---

## 🏗️ Architecture

The app is built on a clean **Model-View-ViewModel (MVVM)** architecture with Unidirectional Data Flow (UDF):

```mermaid
graph TD
    UI[Jetpack Compose UI Screen] -->|Send Intent / UI Event| VM[FinanceViewModel]
    VM -->|Updates State| State[FinanceUiState]
    State -->|Triggers Recomposition| UI
    
    VM -->|CRUD Queries| Repo[Repositories]
    Repo -->|Local Cache| Room[Room SQLite Database]
    Repo -->|Memory States| InMemory[InMemoryDatabase]
    InMemory -->|Key Value Store| Prefs[SharedPreferences]
```

### Key Components:
- **`MainActivity.kt`**: App entry point, responsible for requesting runtime notifications and initializing the database connection.
- **`FinanceViewModel.kt`**: Coordinates state across all navigation tabs, executing operations via repository modules.
- **`InMemoryDatabase.kt`**: Handles state caching, category spending limits, and persistent cashback rule tables.
- **`AccountsScreen.kt`**: Layout rules and interactive card canvas drawings for the Assets & Cards section.
- **`HomeScreen.kt`**: Manages the main daily budget dashboard and calendar heatmap grid views.
- **`HistoryScreen.kt`**: Displays transactional breakdowns and dynamic cashback labels.

---

## 🛠️ Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/compose) (Declarative Kotlin UI)
- **Database**: [Room](https://developer.android.com/training/data-storage/room) (SQLite ORM)
- **Background Jobs**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) (Daily credit card deadline checks)
- **Local Settings**: [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences) (Cashback rules and rates)
- **Architecture**: Kotlin Coroutines, Kotlin Flows, Android Arch Components

---

## 📥 Getting Started

### 📋 Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 34+
- Java JDK 17

### 🛠️ Build & Installation
Build the debug APK using the Gradle wrapper:
```bash
./gradlew assembleDebug
```

Install it on a connected emulator or physical device:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 🧪 Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

---

## 🔍 Knowledge Graph (`graphify`)

We use **Graphify** to maintain a persistent knowledge graph of the codebase for navigation and dependency tracking.

### Update the Graph
If you modify code files, keep the graph current with AST-only extraction (free, no API cost):
```bash
graphify update .
```

### Outputs
All outputs are located under `graphify-out/`:
- `graph.html`: Interactive graph visualization.
- `GRAPH_REPORT.md`: Audit report highlighting God Nodes and Surprising Connections.
- `graph.json`: Raw node and edge data.

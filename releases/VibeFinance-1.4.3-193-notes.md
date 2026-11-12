# VibeFinance 1.4.3 (193) Release Notes — 2026-11-12

## 🌟 核心更新與改進 (Core Updates)

### 1. 每日剩餘天數卡片全滿天數 1% 起始標記 (DaysLeftCard Notch Guarantee)
- **痛點解決**：修正新週期剛建立、全滿天數剩餘（如 30 天剩餘 / 共 30 天）時，倒數消耗進度為 `0.0f`（0%）導致圓形進度條完全空白無彩色弧段的問題。
- **1% 保證標記**：
  - 定義 `MIN_DAYS_LEFT_PROGRESS = 0.01f`，當 `daysLeft >= totalDays` 或消耗時間極短時，進度保證至少呈現 1% 的圓潤指針。
  - 搭配 Material 3 Expressive 的 `StrokeCap.Round` 與 4dp 環形間隔（`gapDegrees`），在 12 點鐘位置呈現精巧圓潤的起始指示圓角凸點（Notch / Bead），軌道環（Track）在其後保持優雅留白間距。
  - 在「波浪」與「平面」外觀切換、淺色與深色主題下皆具備鮮明高對比度與流暢動畫。

### 2. Material 3 自適應架構升級 (Adaptive Architecture)
- **導航中樞化 (Navigation Rail Hub)**：
  - 在橫向模式與大螢幕（$\ge 600$dp 橫向 / $\ge 840$dp 展開）採用固定 88dp 寬度 Navigation Rail，徹底移除可展開抽屜機制，消除水平畫布跳動與卡片壓迫問題。
  - 頂部錨定品牌 Logo 與情境感知 FAB，底部整合隱私、深色主題與設定之快捷控制中樞。
- **日常支出 420dp Modal Side Sheet**：
  - 大螢幕下點擊新增交易轉化為右側停靠的 420dp 側邊抽屜面板（`topStart = 28.dp, bottomStart = 28.dp`）。
  - 嚴格局限於內容區域，左側 88dp 導航欄始終固定可見且可互動；支援中央 38% Scrim 遮罩點擊即關閉。
- **定期訂閱 List-Detail 雙欄架構**：
  - 採用 Jetpack Navigation 3 `ListDetailSceneStrategy`，在大螢幕（$\ge 724$dp）下左欄清單與右欄時間軸並列。
  - 點擊新增或編輯訂閱時，直接在右欄原位內嵌 `AddEditSubscriptionSheet`，無需全螢幕奪取視覺焦點。
- **情境感知導航按鈕 (Context-Aware FAB)**：
  - 根據當前所在分頁自動切換動作與無障礙提示（每日/紀錄：新增日常支出 Side Sheet；定期：右欄嵌入訂閱編輯器；資產：資產建立）。

### 3. Mox 銀行與信用卡現金回贈通知智慧攔截 (Cashback & Rebate Notification Interception)
- **回贈關鍵字語意解耦**：將真實撤銷關鍵字與回贈關鍵字（`"cashback"`, `"rebate"`, `"回贈"`, `"返現"`）分離。當通知帶有回贈優惠標語但包含實質消費動詞時，正確辨識為日常支出交易，不被誤判為撤回或作廢。
- **文法引擎優先度優化**：提前執行 `TransactionGrammarEngine.parse()`，避免電話號碼或百分比（如 `28888228`, `1%`）觸發多重金額防衛檢查而丟棄有效通知。
- **Mox Card 帳戶自動媒合**：支出交易優先辨識並指向信用卡帳戶（`MOX card`）。
- **即時快捷記帳通知**：對於未綁定帳戶的即時通知，自動觸發懸浮決策通知，允許使用者在通知中心一鍵核准記帳。

---

## 📦 發行版本產物 (Release Artifacts)

- **Google Play App Bundle (AAB)**：[`VibeFinance-1.4.3-193-release.aab`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.3-193-release.aab)
  - **檔案大小**：`29,471,122` 位元組（約 28.10 MiB）
  - **建置設定**：包含所有資源與最佳化二進位碼，啟用 R8 最佳化與官方正式簽署（`keystore/release.keystore`），供 Google Play Store 進行動態交付與分發。
  - **完整性校驗**：[`VibeFinance-1.4.3-193-release.aab.sha256`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.3-193-release.aab.sha256)
    ```
    22bdce1ab16912f4d6c63dd13ddf65fc3ae6c71ea436f463170dac646cd70d10  VibeFinance-1.4.3-193-release.aab
    ```

- **獨立 Release APK**：[`VibeFinance-1.4.3-193-release.apk`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.3-193-release.apk)
  - **檔案大小**：`54,590,256` 位元組（約 52.06 MiB）
  - **建置設定**：啟用正式版 R8 混淆最佳化（`isMinifyEnabled = true`）、無用資源縮減（`isShrinkResources = true`）與官方發行密鑰簽署（`keystore/release.keystore`）。
  - **完整性校驗**：[`VibeFinance-1.4.3-193-release.apk.sha256`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.3-193-release.apk.sha256)
    ```
    9fcc9410910abf99208baae05441a4c823592d1bc9cdda556f4a38f87efd7d3b  VibeFinance-1.4.3-193-release.apk
    ```

- **R8 映射符號包**：[`VibeFinance-1.4.3-193-release-mapping.zip`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.3-193-release-mapping.zip)（17.4 MiB，包含 `mapping.txt`, `seeds.txt`, `usage.txt`, `configuration.txt` 等崩潰符號還原檔）。

---

## 📱 Google Play Console 發行說明 (What's New)

參見專用檔案：[`releases/VibeFinance-1.4.3-193-play-store-notes.txt`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.3-193-play-store-notes.txt)

### English (en-US)
```
• Material 3 Expressive Adaptive Layout: Navigation Rail and side sheets for large screens and foldables.
• Days Left Indicator: 1% visual notch indicator for full-cycle budget countdown.
• Subscriptions List-Detail View: Smooth in-place management for recurring payments.
• Smart Notification Interception: Improved support for Mox Bank and virtual card transactions with cashback perks.
• Stability & Performance: Fluid spring kinetics, edge-to-edge support, and R8 optimization.
```

### 繁體中文 (zh-HK)
```
• Material 3 Expressive 自適應設計：大螢幕與折疊螢幕專屬 88dp Navigation Rail 與日常支出側邊欄。
• 週期天數進度指示：新增 1% 起始圓角指示標記，週期初日不再空白。
• 定期訂閱雙欄管理：大螢幕支援 List-Detail 並列檢視，原位編輯訂閱免跳轉。
• 智能交易通知攔截強化：完美支援 Mox 銀行及各類信用卡現金回贈、返現簽賬通知。
• 體驗與效能優化：彈簧物理動效、無障礙觸控區與 R8 混淆瘦身。
```

### 繁體中文 (zh-TW)
```
• Material 3 Expressive 自適應架構：大螢幕與折疊螢幕專用 Navigation Rail 與側邊欄。
• 預算天數進度指示：新增 1% 起始圓角指示標記，週期第一天清晰易見。
• 定期訂閱雙欄管理：大螢幕支援清單與時間軸並列，原位編輯訂閱免跳轉。
• 智能通知攔截強化：支援 Mox 銀行及各類信用卡現金回饋通知自動辨識。
• 系統與效能優化：流暢彈簧物理動態、無障礙觸控與 R8 編譯最佳化。
```

---

## ✅ 測試與品質驗證 (Validation)

1. **單元測試全數通過**：
   - `./gradlew :app:testDebugUnitTest` 通過 571 項測試（包含 `HomeScreenBentoTest` 邊界與 1% 進度驗證、`PaymentNotificationListenerTest` 現金回贈測試）。
2. **實機與自適應測試套件**：
   - Waydroid 實機通過 11/11 項自適應整合測試（`AdaptiveMainFrameTest`, `TransactionSideSheetTest`, `AdaptiveRecurringSceneTest`）與 12/12 項 `TransactionEntryHostTest`。
3. **實體裝置相容性**：
   - 經使用者明確授權已成功佈署於 Samsung Galaxy S24 Ultra（`SM-S9280` / `R5CX22YGH7A`），保留 `/data/user/0/com.ricky688.vibefinance` 原始帳目資料，確認 `versionCode=193`, `versionName=1.4.3` 運作正常。

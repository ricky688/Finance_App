# VibeFinance 1.4.4 (194) Release Notes — 2026-11-12

## 🌟 核心更新與改進 (Core Updates)

### 1. 帳戶卡片與圖標非同步後台載入 (Move Account Image Decoding to I/O Thread)
- **痛點分析**：
  - 在三星 Galaxy S24 Ultra 實機上以 Perfetto 效能分析 1.4.3 版本快速滾動「資產（Accounts）」頁面時，發現 377 個影格中發生 7 次 App 主執行緒截止逾期（Deadline Misses），LazyColumn 項目預取（prefetch）峰值耗時達 23.69ms。
  - 其中 8 個最長的 prefetch 切片包含高達 16ms 的同步圖片與圖標解碼工作，導致主執行緒堵塞引發視覺卡頓。
- **架構優化**：
  - 在 [`AccountsScreen.kt`](file:///home/ricky/Antigravity_project/finance_app/app/src/main/java/com/example/vibefinance/ui/accounts/AccountsScreen.kt) 中，將卡片縮圖解碼（`rememberAccountThumbnail`）與已連結 App 圖標讀取（`AccountAppRedirectButton` / `loadAccountAppIcon`）全面遷移至 `Dispatchers.IO`。
  - 透過 `produceState` 搭配 `key(context, filePath)` 達成非同步非阻塞渲染；當連結的應用程式或圖片路徑變更時即時重置，避免殘留前次載入結果。
- **實機效能驗證 (Galaxy S24 Ultra)**：
  - **Perfetto 追蹤比較**：主執行緒預取峰值自 23.69ms 驟降至 **5.45ms**；LazyColumn prefetch 期間的 `ImageDecoder` 堵塞切片自 8 次直接清零（**0 次**）；主執行緒逾期掉幀自 7/377 下降至 **1/388**。

---

## 📦 發行版本產物 (Release Artifacts)

- **Google Play App Bundle (AAB)**：[`VibeFinance-1.4.4-194-release.aab`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.4-194-release.aab)
  - **檔案大小**：`29,474,057` 位元組（約 28.11 MiB）
  - **建置設定**：包含所有資源與最佳化二進位碼，啟用 R8 最佳化與官方正式簽署（`keystore/release.keystore`），供 Google Play Store 進行動態交付與分發。
  - **完整性校驗**：[`VibeFinance-1.4.4-194-release.aab.sha256`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.4-194-release.aab.sha256)
    ```
    97942bf435c6fad2952ef08cba89ca9b6257a148dfef41b66db7b7ceef9c9cdf  VibeFinance-1.4.4-194-release.aab
    ```

- **獨立 Release APK**：[`VibeFinance-1.4.4-194-release.apk`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.4-194-release.apk)
  - **檔案大小**：`54,607,572` 位元組（約 52.08 MiB）
  - **建置設定**：啟用正式版 R8 混淆最佳化（`isMinifyEnabled = true`）、無用資源縮減（`isShrinkResources = true`）與官方發行密鑰簽署（`keystore/release.keystore`）。
  - **完整性校驗**：[`VibeFinance-1.4.4-194-release.apk.sha256`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.4-194-release.apk.sha256)
    ```
    e9c2aa31799edc712d906c5f2a35052aebf042480617bce11f80c33925775651  VibeFinance-1.4.4-194-release.apk
    ```

- **R8 映射符號包**：[`VibeFinance-1.4.4-194-release-mapping.zip`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.4-194-release-mapping.zip)（16.5 MiB，包含 `mapping.txt`, `seeds.txt`, `usage.txt`, `configuration.txt` 等崩潰符號還原檔）。

---

## 📱 Google Play Console 發行說明 (What's New)

參見專用檔案：[`releases/VibeFinance-1.4.4-194-play-store-notes.txt`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.4-194-play-store-notes.txt)

### English (en-US)
```
• Performance Optimization: Offloaded account card images and linked app icons decoding to background threads, significantly boosting scrolling fluidity on the Accounts tab.
• Adaptive UI: Navigation Rail and side sheets for large screens and foldables.
• Days Left Indicator: 1% visual notch indicator for full-cycle budget countdown.
• Subscriptions List-Detail: In-place management for recurring payments.
• Smart Notifications: Robust Mox Bank & cashback alert interception.
```

### 繁體中文 (zh-HK)
```
• 效能與滑動流暢度提升：帳戶卡片自訂圖像與關聯應用圖標全面改為後台非阻塞式載入，大幅改善資產清單快速滾動時的掉幀問題。
• Material 3 Expressive 自適應設計：大螢幕與折疊螢幕專屬 88dp Navigation Rail 與日常支出側邊欄。
• 週期天數進度指示：新增 1% 起始圓角指示標記，週期初日不再空白。
• 定期訂閱雙欄管理：大螢幕支援 List-Detail 並列檢視，原位編輯訂閱免跳轉。
• 智能交易通知攔截：完美支援 Mox 銀行及各類信用卡現金回贈、返現通知。
```

### 繁體中文 (zh-TW)
```
• 效能與滾動順暢度最佳化：帳戶卡片自訂圖片與關聯應用程式圖示改採背景非同步載入，徹底消除資產頁面快速滑動時的卡頓。
• Material 3 Expressive 自適應架構：大螢幕與折疊螢幕專用 Navigation Rail 與側邊欄。
• 預算天數進度指示：新增 1% 起始圓角指示標記，週期第一天清晰易見。
• 定期訂閱雙欄管理：大螢幕支援清單與時間軸並列，原位編輯訂閱免跳轉。
• 智能通知攔截強化：支援 Mox 銀行及各類信用卡現金回饋通知自動辨識。
```

---

## ✅ 測試與品質驗證 (Validation)

1. **單元測試全數通過**：
   - `./gradlew :app:testDebugUnitTest` 通過 576 項測試。
2. **實體旗艦機實機驗證**：
   - 經使用者授權於 Samsung Galaxy S24 Ultra（`SM-S9280` / `R5CX22YGH7A`）完成原位覆蓋安裝（資料完整保留）。
   - gfxinfo 與 Perfetto 效能追蹤確認 LazyColumn prefetch 堵塞完全消除。

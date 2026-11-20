# VibeFinance 1.4.5 (195) Release Notes — 2026-11-12

## 🌟 核心更新與改進 (Core Updates)

### 1. 跨來源數位錢包伴隨通知去重引擎 (Cross-Source Digital Wallet Companion Alert Deduplication)
- **痛點分析**：
  - 當使用者在實體商店使用 Google Wallet（或 Samsung Wallet）感應付款時，Google Wallet 與發卡銀行（如 Mox Bank、HSBC、恒生等）通常會在數秒內相繼發送消費通知。
  - 由於 Google Wallet 與發卡行 App 套件名稱相異（例如 `com.google.android.apps.walletnfcrel` vs `com.mox.app`），系統先前為兩者產生了不同的雜湊指紋，導致同一筆實體感應消費被重複記帳或跳出兩次決策通知。
- **架構設計與三層防護**：
  - **伴隨消費警報比對 (`areCompanionExpenseAlerts`)**：
    - 時間視窗：限制於 90 秒內。
    - 金額精確核對：小數點兩位完全一致（誤差 < 0.01）。
    - 實體卡號相容驗證：若兩者皆包含卡號末四碼（`cardLast4`），必須嚴格一致；卡號相異者判定為不同卡片的獨立交易。
    - 智能商戶映射 (`isMerchantMatch`)：支援通用錢包佔位名稱（如 `Google Pay Merchant`）自動配對具體商戶，並內建中英雙語對照庫（如 `Sushiro HK` ↔ `壽司郎`、`McDonald's` ↔ `麥當勞` 等）。
  - **三層去重防線**：
    1. **第一層（近 120 秒已處理記錄）**：使用者若已手動忽略錢包通知，伴隨的發卡行通知將靜默拋棄，杜絕重複彈窗騷擾；若先前已自動入帳，系統會自動將先前通用商戶名升級為銀行真實商戶名。
    2. **第二層（資料庫已入帳核對）**：直接核對 `InMemoryDatabase` 近 90 秒內的交易，拋棄重複通知並升級商戶名稱與地理座標。
    3. **第三層（待處理隊列無縫合流）**：若伴隨通知在待處理隊列中，取消舊決策通知，優先繼承發卡行卡種名稱與 Google Wallet 之 GPS 經緯度，以最高品質的單一項目提供記帳或決策。

---

## 📦 發行版本產物 (Release Artifacts)

- **Google Play App Bundle (AAB)**：[`VibeFinance-1.4.5-195-release.aab`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.5-195-release.aab)
  - **檔案大小**：`29,485,675` 位元組（約 28.12 MiB）
  - **建置設定**：包含所有資源與最佳化二進位碼，啟用 R8 最佳化與官方正式簽署（`keystore/release.keystore`），供 Google Play Store 進行動態交付與分發。
  - **完整性校驗**：
    ```
    47869a3c181c7dca46dd01fb59c0b941a6a550f9febbbd0ed1b74533c18a6c96  VibeFinance-1.4.5-195-release.aab
    ```

- **獨立 Release APK**：[`VibeFinance-1.4.5-195-release.apk`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.5-195-release.apk)
  - **檔案大小**：`54,607,572` 位元組（約 52.08 MiB）
  - **建置設定**：啟用正式版 R8 混淆最佳化（`isMinifyEnabled = true`）、無用資源縮減（`isShrinkResources = true`）與官方發行密鑰簽署（`keystore/release.keystore`）。
  - **完整性校驗**：
    ```
    01d503066dbcdbe6773c3569d4870d3c0d6c64d3224cecd29117fa8b3681dd52  VibeFinance-1.4.5-195-release.apk
    ```

- **R8 映射符號包**：[`VibeFinance-1.4.5-195-release-mapping.zip`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.5-195-release-mapping.zip)（約 6.62 MiB，包含崩潰還原符號檔 `mapping.txt`）。
  - **完整性校驗**：
    ```
    199d30589c96855eed8123528de03631dad3f0a29f0f2c9c400ab1bbdea35e78  VibeFinance-1.4.5-195-release-mapping.zip
    ```

---

## 📱 Google Play Console 發行說明 (What's New)

參見專用檔案：[`releases/VibeFinance-1.4.5-195-play-store-notes.txt`](file:///home/ricky/Antigravity_project/finance_app/releases/VibeFinance-1.4.5-195-play-store-notes.txt)

### English (en-US)
```
• Google Wallet Deduplication: Seamless cross-source deduplication between Google Wallet / Samsung Wallet and bank card alerts, eliminating duplicate prompts and double logging.
• Merchant Name Upgrade: Automatically upgrades generic Google Pay placeholders to exact merchant names and enriches GPS locations.
• Performance: Background image decoding for asset cards for smoother scrolling.
• Adaptive UI: Navigation Rail and side sheets for large screens and foldables.
```

### Traditional Chinese (zh-HK)
```
• Google Wallet 與數位錢包重複攔截防護：新增 Google Wallet / Samsung Wallet 與發卡銀行（如 Mox、HSBC、恒生等）通知跨來源智能去重，杜絕實體感應付款後的重複記帳與重複彈窗。
• 商戶資訊自動升級：自動將 Google Pay 通用佔位商戶名升級為銀行真實商戶名稱，並無縫繼承 GPS 座標。
• 效能流暢度提升：帳戶卡片自訂圖像非阻塞式背景載入，快速滑動毫無卡頓。
• Material 3 Expressive 自適應設計：大螢幕與折疊螢幕專屬 88dp 導覽列與側邊欄。
```

### Traditional Chinese (zh-TW)
```
• Google Wallet 與數位錢包重複攔截防護：新增 Google Wallet / Samsung Wallet 與發卡銀行通知跨來源智慧去重，杜絕實體感應付款後的重複記帳與重複彈窗。
• 商戶資訊自動升級：自動將 Google Pay 通用佔位商戶名升級為銀行真實商戶名稱，並無縫繼承 GPS 座標。
• 效能順暢度最佳化：帳戶卡片自訂圖片非同步背景載入，快速滑動穩定流暢。
• Material 3 Expressive 自適應架構：大螢幕與折疊螢幕專屬 Navigation Rail 與側邊欄。
```

---

## 🧪 驗證與測試 (Verification)
- 專屬單元測試 [`GoogleWalletDeduplicationTest.kt`](file:///home/ricky/Antigravity_project/finance_app/app/src/test/java/com/example/vibefinance/service/GoogleWalletDeduplicationTest.kt) 包含 6 大測試案例全部通過。
- 專案完整單元測試套件全數通過（`BUILD SUCCESSFUL in 31s`）。
- 代碼格式與檢查：`git diff --check` 通過。

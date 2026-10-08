# Privacy Policy for VibeFinance

*Last updated: October 8, 2026*

This Privacy Policy describes how **VibeFinance** ("we", "us", or "the app") handles your personal and sensitive data. VibeFinance is developed as an open-source, offline-first personal expense tracking and financial rhythm management application.

We are committed to protecting your privacy. **VibeFinance is designed from the ground up to respect user privacy: your financial data belongs exclusively to you and remains entirely on your device.**

---

## 1. Information Collection and Storage

### A. Financial and Transaction Data
- **What is collected**: All accounts, balances, expense and income transactions, categories, budgets, recurrent subscriptions, and notes that you enter or import.
- **Where it is stored**: All financial data is stored **locally on your device** within a private SQLite / Room database.
- **Network Transmission**: **None.** VibeFinance does NOT operate any external servers, cloud databases, or backend tracking infrastructure. Your personal financial data is never transmitted to us or any third parties.

### B. Notification Access (`NotificationListenerService`)
- **Purpose**: VibeFinance includes an optional feature that assists you in logging transactions automatically by reading payment notifications from supported apps (such as Octopus, Google Pay, PayMe, WeChat Pay, and authorized banking applications).
- **How it works**:
  - Notification parsing is executed **entirely on-device in real time**.
  - Only notifications from apps you explicitly designate or authorize are processed.
  - Notification text is examined solely to extract the transaction amount, merchant/counterparty, card suffix, and payment time to generate a local transaction entry.
- **Data Protection**:
  - Notification content is **NEVER transmitted over the network**, NEVER stored beyond local transaction drafting, and NEVER shared with external parties.
  - We do NOT access notifications unrelated to payment transactions.
- **User Control**: Granting notification permission is completely optional. You can enable, customize, or revoke Notification Access at any time in your Android system settings under *Settings > Apps > Special app access > Notification access*.

### C. Backup and Export Files
- If you use the backup or export features (CSV or encrypted ZIP backup), the files are saved exclusively to the local storage location or cloud provider of your choice via Android's Storage Access Framework. We do not have access to these exported files.

### D. Location Data (Optional Map Features)
- If you use the discount radar or shop location features, maps are rendered using the Google Maps SDK. Location information (if enabled) is used solely on-device to center the map and find nearby offers. We do not track, log, or transmit your physical location.

---

## 2. Third-Party Services and SDKs

- **No Third-Party Advertising**: VibeFinance contains no advertisements and integrates no ad networks (such as Google AdMob).
- **No Analytics or Trackers**: VibeFinance contains no telemetry, crash reporting, or user tracking SDKs (such as Google Analytics or Firebase Crashlytics).
- **Google Maps SDK**: Used solely to display maps in the shop/discount radar module, governed by [Google's Privacy Policy](https://policies.google.com/privacy).

---

## 3. Data Retention and Deletion

- Because all data is stored strictly on your local device, you have complete ownership and control over your data.
- You can delete individual transactions, accounts, or budgets directly within the app.
- Clearing the application data or uninstalling VibeFinance from your device permanently and immediately deletes all stored data and preferences.

---

## 4. Children's Privacy

VibeFinance does not address anyone under the age of 13. We do not knowingly collect personal identifiable information from children.

---

## 5. Changes to This Privacy Policy

We may update our Privacy Policy from time to time. Any changes will be posted on this page with an updated revision date.

---

## 6. Contact Information

If you have questions, feedback, or concerns regarding this Privacy Policy, please open an issue or reach out via our GitHub repository:

- **Repository**: [https://github.com/ricky688/Finance_App](https://github.com/ricky688/Finance_App)
- **Developer**: [ricky688](https://github.com/ricky688)

---

# 隱私權政策 (Privacy Policy - 中文說明)

*最後更新日期：2026 年 10 月 8 日*

本隱私權政策說明 **VibeFinance**（以下簡稱「本應用程式」）如何處理您的個人與機密資料。VibeFinance 是一款秉持「離線優先」（Offline-first）理念開發的開源個人財務與記帳管理工具。

我們高度重視您的隱私權。**VibeFinance 的核心設計原則是資料自主：您的所有財務紀錄完全屬於您個人，並僅保存在您的本機裝置中。**

### 1. 資料收集與儲存方式
- **財務與交易紀錄**：您手動輸入或匯入的帳戶、餘額、消費、收入、轉帳、分類、預算及週期設定，均儲存於本機裝置的私有 SQLite / Room 資料庫中。本應用程式未架設任何外部雲端伺服器，**絕不會將您的財務數據上傳至任何伺服器或第三方**。
- **通知監聽權限 (`NotificationListenerService`)**：
  - 本應用程式提供選用的「通知自動記帳」功能，需在取得您的系統授權後，讀取特定支付工具（如八達通、Google Pay、PayMe、銀行通知等）之推播通知。
  - 所有通知內容**僅於本機端即時解析**，用於提取消費金額、商戶與交易時間以協助建立記帳紀錄。
  - 通知內容**絕不進行網路傳輸**、絕不上傳雲端、絕不分享予任何第三方，亦絕不用於廣告追蹤。
  - 您可隨時在 Android 系統「設定 > 應用程式 > 特殊應用程式存取權 > 通知存取權」中開啟或撤銷此權限。
- **備份與匯出**：匯出 CSV 或加密備份檔案皆透過 Android 系統文件選擇器由您親自指定儲存位置，我們無法存取您的備份檔案。

### 2. 第三方服務與追蹤
- 本應用程式**無廣告**（未整合任何廣告 SDK）。
- 本應用程式**無數據追蹤**（未整合任何使用者追蹤或分析統計 SDK）。
- 地圖服務僅透過 Google Maps SDK 於本機端呈現店家優惠資訊。

### 3. 資料保存與刪除
- 您對所有記帳資料擁有 100% 控制權。您可隨時在應用程式中刪除任何交易或帳戶。
- 於系統設定中清除本應用程式資料或解除安裝，即可立即永久刪除本機中的所有資料。

### 4. 聯絡我們
若對本隱私權政策有任何疑問，歡迎透過 GitHub 倉庫提出：
- [https://github.com/ricky688/Finance_App](https://github.com/ricky688/Finance_App)

# NFC field probe（獨立研究工具）

只測試 API36+ 手機是否能收到 `CardEmulation.NfcEventCallback.onRemoteFieldChanged()`。它不是 VibeFinance 功能，也不辨識車站、卡種、交易成功或入／出閘。

## 使用

1. 開啟手機的「NFC 拍卡診斷」。
2. 在即將正常拍卡前按「開始兩分鐘測試」，首次需自行允許通知，以顯示限時前景服務。
3. 照常使用手機拍讀卡器；可測試背景或鎖屏，不需更改預設錢包、重複付款或改系統設定。
4. 回到診斷畫面查看時間及 `FIELD_ON`／`FIELD_OFF`。每次接觸可能有多筆邊緣事件，筆數不是付款次數。
5. 兩分鐘後自動停止；也可按停止。下一次開始會建立新一輪紀錄。最多保留200筆最近事件於本機私有 SharedPreferences，未上傳且不備份。

「已呼叫註冊」不代表一定能收到事件；只有實際 `FIELD_ON`／`FIELD_OFF` 才是正面證據。若0筆，只能說當次測試未捕捉，不可反推使用者沒有拍卡。時間採手機本地時區。

## 權限與邊界

- 只有 NFC、FOREGROUND_SERVICE、POST_NOTIFICATIONS。
- 不請求定位、網絡、通知存取、安全晶片交易權限或錢包角色；不呼叫 Reader Mode／Observe Mode／付款AID註冊，也不接收卡片資料。
- Activity是唯一匯出元件，短時服務未匯出、START_NOT_STICKY，沒有開機自啟動。
- 使用者從前景畫面啟動 `shortService`；自行120秒結束，並處理系統timeout解除監聽。
- 這只驗證事件。GPS、通知配對及完整乘車推算不在探針範圍。
- 尚未在真讀卡器驗證，未做省電／深度待機時限或UI互動回歸。已編譯、簽章驗證及通過有界EventLog檢查，並安裝至指定Samsung但保持未啟動。

## 建置

使用本機既有JDK17+、Android SDK36與build-tools36.0.0，及既有Android debug keystore；不下載依賴、不編譯主App。

```sh
tools/nfc-field-probe/build.sh
```

產物預設在 `/tmp/vibe-nfc-field-probe-build/nfc-field-probe-debug.apk`。工具包名 `hk.vibefinance.nfcprobe`，版本0.1，與主App完全分開。`EventLogCheck.java` 是JVM assert檢查，涵蓋時間／ON-OFF保留與200筆上限，非手機互動測試。

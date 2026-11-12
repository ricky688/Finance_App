# NFC 拍卡時機觸發定位：可行性研究

日期：2026-11-06（香港時間）。範圍：研究 Android 公開 API、AOSP 分派路徑及指定 Samsung 的唯讀能力資訊；未實作、安裝探針或更改錢包設定。

## 最新實機證據：已收到 RF 回呼（2026-11-06）

使用者提供兩張真讀卡器測試截圖，並明確要求ADB核對。只讀探針私有`probe.xml`取得最新19:28場次：19:28:13.925 ON →14.612 OFF（687ms），14.614 ON →14.631 OFF（17ms），OFF空隙2ms，整組跨度706ms。探針狀態已停止，4筆邊緣事件。

18:31場次只見於使用者截圖，未宣稱從ADB恢復：18:31:08.929 ON →08.941 OFF（12ms），08.949 ON →09.520 OFF（571ms），09.520 ON →09.542 OFF（22ms）；OFF空隙8ms／同一毫秒，整組跨度613ms。這是6筆邊緣事件，不是6筆付款或必然6次接觸。

已證明：獨立一般App探針確實在這部手機收到公開RF callback，先前「靜態可行但未驗證事件」的未知項已有正面執行證據。未證明：鎖屏／熄屏是否成功（截圖無法判斷）、最低可偵測停留時間、快速接觸漏失率、卡片種類、交易成功或站點定位。

時間為主執行緒callback收到後以手機wall clock記錄，沒有硬體RF時間戳／elapsed realtime，因此毫秒數是觀測跨度，不等於精密射頻量測。多段短ON/OFF較像一次接觸中的斷續／輪詢，但沒有逐次物理接觸標註，不能斷言實際接觸次數。後續整合應首次ON觸發一次定位、短OFF間隔合併，不要求長ON才接受；例如200ms合併窗僅作待更多樣本調整的起始值。探針本身沒有停留時長過濾，12/17/22ms段也被保存。未改App、探針或權限設定。

## 結論

有條件可行，值得先做限量實機探針；目前不能宣稱已能在 Smart Octopus／港鐵入閘時可靠自動記錄起點。

可用的訊號是「讀卡器 RF 感應場出現」，不是八達通交易內容、車站代碼或入閘成功回執。應在此事件觸發短暫定位，保存附近候選站，待出閘扣款時再比對。這不解決所有同價歧義；沒有足夠證據仍須待確認。

## 已確認的證據

| 項目 | 結果 | 證據界限 |
|---|---|---|
| 指定手機 | SM_S9280；`getprop` 回報 Android17／API37、One UI90000 | 是系統回報值，不代表已驗證 NFC 回呼 |
| NFC 能力 | 已開啟；宣告 NFC、eSE、HCE、HCE-F、UICC | 支援功能不等於 Smart Octopus 會送出所需事件 |
| 公開 API | `CardEmulation.registerNfcEventCallback()`／`onRemoteFieldChanged(boolean)`，API36 起 | 官方說明只在註冊程序存活期間提供回呼 |
| AOSP 系統分派 | `NfcService.onRemoteFieldActivated/Deactivated` → `CardEmulationManager.onRemoteFieldChanged` → 已註冊回呼 | 查閱公開 AOSP main；不是 Samsung 已安裝 NFC 模組的反編譯或實測 |
| 錢包角色 | 查閱的 AOSP 註冊／RF 分派分支沒有要求先成為預設錢包或優先 HCE 服務 | Samsung 行為仍須用一般 App UID 驗證，不能用 shell UID 結果代替 |
| 功能旗標 | AOSP `nfcEventListener` 關閉時註冊可以直接返回、不做事 | 方法存在／沒有拋例外不等於監聽成功 |
| 專案可重用部分 | 已有系統綁定的 `PaymentNotificationListener`、五秒可取消定位流程及本機車站表 | 現有定位資料沒有 accuracy，足跡只在記憶體；未新增 NFC 權限／回呼 |

手機檢查僅讀取指定版本欄位、NFC 功能列表、NFC 開關狀態及 NFC 套件公開路徑；未匯出交易、錢包資料、卡號、完整通知或系統 NFC dump。

## 最小整合方案（待探針結果，並非本次已實作）

1. API36+ 且支援相關 NFC 功能時，提供明確可停用的選項；舊版維持目前行為。
2. 在現有通知監聽服務已連線且使用者啟用此功能時註冊公開 callback；服務斷線／銷毀／停用時解除，綁定可取消 coroutine scope。宣告一般 NFC 權限；定位沿用使用者授予的前景／背景權限。
3. RF 場出現時，使用 elapsed realtime 去除同一次接觸的抖動、避免重複定位及同時請求；RF 場消失只結束接觸，不代表交易已成功。
4. 立即開始最長五秒的定位。不可把現有三分鐘快取當作拍卡點；需要記錄定位原始時間、accuracy、取得延遲與新鮮度。事件時間與定位時間分開保存，不能以新事件時間替舊位置「刷新」。
5. 有足夠精確的位置才產生車站候選；保存候選、距離及誤差，而非立即選最近站。只保留短期、私有、本機紀錄（例如兩小時上限，實際值待量測），關閉功能可清除；不保存 APDU、AID 或卡片內容。
6. 收到已辨識的 MTR 出閘付款通知後，以票價／乘車身分／先前 RF 事件的站點候選／合理行程時間評分。保留多站歧義，不直接按列表順序挑站。
7. 只有本次 NFC 來源的事件才作為新的起點證據，不能把出閘即時定位或一般付款足跡無條件當作上車位置。不可用「奇數次入閘／偶數次出閘」判斷；商店付款、重拍及失敗拍卡都會破壞此假設。

現有通知監聽服務可作初步註冊位置，避免另開永久 GPS 或新增常駐服務。但其程序仍可能斷線或被殺；callback 不是能喚醒死程序的廣播。若此路線穩定性不足，才評估由使用者主動啟動、具可見通知及時限的乘車工作階段；不能假設收到任意 NFC callback 就獲准在背景啟動定位前景服務。

## 補充：付款通知排除與連續 NFC（2026-11-06）

使用者提出「有付款通知就忽略，沒有就保留位置」。建議將其解讀為：只排除能配對到的接觸作為入閘起點，不忽略通知本身，也不刪除整段候選歷史。MTR出閘付款仍須結算／配對先前候選；無通知只代表未分類，不代表入閘成功。網上付款、延遲推播及多個事件同時存在時不能只靠通知到達時間確定配對。

第二次感應不自動刪除：同一RF場持續為ON不建立新事件；數秒內開關抖動／重拍可合併，保留首次時間及最佳新鮮位置；時間分離的接觸分別保留。首輪可試驗3秒去重、約30秒付款通知觀察窗與短期候選過期，但這些是未量測的起始參數，應以實機分佈調整。若通知晚到可撤銷對應候選；無法唯一配對時不廣泛刪除。不要用第二次感應覆蓋第一次，也不要交替判定入／出閘。

手機唯讀補查：僅查指定NFC logcat標籤中明確RF開關行，並在手機端將NFC event dump篩成remote_field_state_change的時間與ON/OFF；兩者均未返回可列出的事件。未匯出完整dump／卡片／AID／路由。這不是「沒有拍卡」的證據，可能是未保存、未輸出或已輪替；目前不能提供使用者今日真實RF時間表。需另做限時、可停用的一般App UID探針，才能量測接觸次數與通知延遲。本次沒有安裝探針或變更手機設定。

## 再核對紀錄保存狀態（2026-11-06）

使用者確認接觸是手機拍港鐵閘機／付款讀卡器，不是手機掃實體標籤。重新以手機端metadata-only篩選核對全部可用logcat緩衝區與NFC保留事件：

- `VendorDebugEnabled=false`。
- NFC保留事件共100筆，時間範圍2026-11-04 15:43:01.112至2026-11-06 14:01:14.183；RF感應場事件0筆。100筆不是100次拍卡。
- 當時可讀的指定NFC標籤logcat共106行，時間11-06 15:13:17.878至15:24:08.462；擴大RF格式匹配仍為0筆。
- AOSP將RF歷史保存放在`nfcPersistLog && NFC_VENDOR_DEBUG_ENABLED`條件內，但即時callback使用另一個`nfcEventListener`條件；因此缺少歷史不等於callback不可用。Samsung具體實作尚未驗證；沒有啟用vendor debug、重啟NFC或改變任何設定。
- 結論限於「現存可讀歷史沒有RF開關時間」，不能宣稱沒有發生拍卡或即時事件。接下來須以限時callback探針觀察真讀卡器；不能補回未保存的先前事件。

## 手機出貨程式的進一步分析與限時探針

2026-11-06追加：先前「未反編譯Samsung套件／未安裝探針」描述是前一輪狀態，本節更新它；仍未做真閘機拍卡實測。

- 從指定手機公開程式路徑複製NFC APK與framework JAR至`/tmp`，未讀取卡片、交易或App私有資料。
- APK SHA256：`13c96c9e3ebb8c57b359801160317f84e699bd02e09a08ceb38b9b22f0838265`；framework-nfc.jar SHA256：`acd3f0f1564306abb234f4462da2bd00fa13cf12ea563b45608c7624f8e5a428`。
- REA `inspect_android_package` 回報缺少REA_JADX_MCP_JAR（capability_unavailable），未修MCP設定／下載分析元件；改用已安裝SDK的`apkanalyzer dex code`，所以沒有成功的REA Evidence ID可引用。
- 實際APK的`NfcService.onRemoteFieldActivated()`分別以`nfcPersistLog && NFC_VENDOR_DEBUG_ENABLED`決定歷史保存、以`nfcEventListener`決定即時分派。
- 實際`CardEmulationManager$CardEmulationInterface.registerNfcEventCallback()`在旗標開啟時將listener放入RemoteCallbackList；該方法沒有預設錢包／安全晶片授權檢查。其分派方法迭代已註冊listeners。這是靜態可見路徑，不能代替一般App UID實測。
- framework JAR確有公開`CardEmulation.registerNfcEventCallback()`。旗標實作正常初始化路徑在載入AconfigPackage後將`nfcEventListener`設為true；若初始化先失敗仍可能保留false，沒有執行期證據就不宣稱已確認可接收。
- 已新增最小獨立工具`tools/nfc-field-probe/`，debug0.1／包名`hk.vibefinance.nfcprobe`，約17KB，NFC/前景服務/通知三種權限，無定位、網絡、付款資訊、AID或錢包操作。由使用者按鈕啟動120秒shortService，結束解除回呼，最多保留200筆時間與FIELD_ON/OFF到私有檔。簽章及JVM EventLog assert檢查通過。
- 已安裝至指定Samsung，沒有啟動監聽、授予通知權限或更改現有App。已核實VibeFinance仍1.3.42/183。使用者回覆「稍後才有讀卡器」，因此等待下一次正常拍卡才可完成事件可靠性驗證。沒有定位整合／正式release。

本機靜態證據：`/tmp/vibe-phone-NfcService-field-on.smali`、`/tmp/vibe-phone-nfc-register.smali`、`/tmp/vibe-phone-nfc-dispatch.smali`、`/tmp/vibe-phone-nfc-api-register.smali`、`/tmp/vibe-phone-nfc-flags-init.smali`。這些檔案來自手機公開出貨程式，不是一般AOSP推測。

## 不採用的方式

- 不呼叫 `enableReaderMode()`：官方文件指出該模式會停用卡片模擬，可能干擾現有錢包拍卡。
- 不切換預設錢包、不變更路由／Observe Mode、不註冊付款 AID 或假 HCE 卡、不嘗試讀取安全晶片內的八達通資料。
- 不依賴一般 App 無法可靠讀取的系統 logcat RF 日誌，也不需要 root／Shizuku 才能運行正式功能。

## 實機探針須證明的事項

先用獨立診斷模式驗證，與帳目寫入及起點自動填入分開；以下為驗證清單，尚非測試結果：

- 一般 App UID、原本錢包角色不變下，能否註冊並收到 RF 場開／關；需用真讀卡器，Waydroid 無實體 NFC，不能代替。
- App 前景、背景、鎖屏、熄屏及服務重新連線後，事件是否到達；註冊失效／程序被殺時是否清楚顯示不可用。
- Smart Octopus 入閘／出閘，以及一般商店拍卡、重拍、失敗拍卡；確認錢包仍正常，不把 RF 當付款成功。
- 取得位置所需時間、位置年齡及精確度；比較九龍灣站內、入口與附近街道。地下無定位／只有舊快取時必須保持未確認。
- NFC 抖動／連續接觸不產生定位風暴；五秒後感測請求確實停止；停用功能與權限拒絕不影響原記帳。
- 九龍灣→兆康 $12.60：有可靠九龍灣起點證據時排序正確；沒有證據或兩候選相近時不再硬選調景嶺。

建議先量測事件捕捉率、定位成功率、入閘事件與第一個定位結果的延遲、誤判率；在未有這些數據前不承諾固定準確率或鎖屏可靠性。NFC 晶片／系統安全策略可能使「電池耗盡仍可拍卡」與「Android App 可收到回呼」兩者不同。

## 來源

- [Android NFC event callback API](https://developer.android.com/reference/android/nfc/cardemulation/CardEmulation.NfcEventCallback)：API版本、RF場事件、程序存活要求。
- [AOSP CardEmulationManager](https://android.googlesource.com/platform/packages/modules/Nfc/+/refs/heads/main/NfcNci/src/com/android/nfc/cardemulation/CardEmulationManager.java)：註冊、功能旗標及 callback 分派。研究時已下載至本機 `/tmp/vibe-nfc-CardEmulationManager.java` 檢視。
- [AOSP NfcService](https://android.googlesource.com/platform/packages/modules/Nfc/+/refs/heads/main/NfcNci/src/com/android/nfc/NfcService.java)：RF場開／關至 Java callback 的路徑。
- [Android NfcAdapter](https://developer.android.com/reference/android/nfc/NfcAdapter)：Reader Mode 會停用 card emulation。
- [Android background location](https://developer.android.com/develop/sensors-and-location/location/permissions/background)：背景定位授權。
- [Foreground-service background-start restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)：背景啟動及 while-in-use 權限限制。
- [LocationManagerCompat](https://developer.android.com/reference/androidx/core/location/LocationManagerCompat)：單次定位可取消，背景取位置仍可能受節流。

限制：未安裝探針、未操作真 NFC 讀卡器／港鐵閘機、未反編譯 Samsung 系統 APK。本次只更新研究文件，不變更183版 App、不製作 release、不修改或安裝手機軟體。

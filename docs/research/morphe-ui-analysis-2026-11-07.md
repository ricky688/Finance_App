# Morphe Manager 視覺與互動分析

分析日期：2026-11-07。來源：本機 `morphe-manager` checkout，HEAD `da8545c9`（release v1.35.0）。

本報告是程式碼與隨附文件截圖分析，沒有啟動或量測 Morphe 的實際裝置版本。「舒服」的原因是設計推論，不是使用者研究或 FPS 測試結果。沒有改動 VibeFinance UI、裝置縮放、金融資料或 Morphe 程式碼；沒有將原始碼交給外部模型。現有 VibeFinance 圖譜不包含 Morphe 節點，因此以本機原始碼為證。

## 1. 結論

它建立了可預測的視覺節奏：有限且反覆使用的間距／圓角；中性色承托彩色重點；單一主導按壓回饋；快速互動與緩慢裝飾分離；內容、按鈕與讀取寬度維持對齊。

不是每個元件都使用24dp以上圓角，也不是所有元件都用彈簧、即時模糊或形狀變化。最值得學習的是元件家族內的一致性。

## 2. 實際尺寸（dp，字體另用sp）

| 元件／層級 | 程式中的值 | 解讀 |
|---|---|---|
| 預設卡片圓角 |16|不要與首頁卡片混同|
| 緊湊圓角 |12|小型圖標底板／資訊盒等|
| 設定項目圓角 |14|較密集的列表|
| Section圓角 |18|共用預設，是否採用視呼叫端|
| 首頁App卡片 |高80、圓角24、圖標60、水平內距16、內容間距16|固定且容易掃讀的兩行資訊列|
| 內容外距 |窄16、中24、寬32|按視窗寬度分級|
| 一般項目間距 |窄12、中16、寬20|Settings Appearance內容另採16間距|
| 設定列內距 |16|文字與邊緣不擠壓|
| 圖標／小圖標 |24／20|共用尺寸|
| Glass控制 |高48、水平內距12、圖文距8、圓角16|清楚的控制家族|
| Dialog操作鈕 |高52、圓角16|重要操作有更充裕高度|
| Pill可見高度 |36／40|不代表每種呼叫方式都已驗證48觸控範圍|
| CompactListCard |圖標36、字形20、水平12／垂直10內距、列表距6|長清單有獨立緊湊密度|
| 一般內容寬度上限 |560|部分首頁／空白狀態／底部操作列採用；橫向佈局有自己的分欄限制|
| 頁面底部浮層側距 |8|預設token，實際依呼叫端|

來源：`SettingComponents.kt:49`、`:364`、`:494`；`HomeAppCards.kt:63`；`CompactListCard.kt`；`WindowSize.kt`；`SettingsTabPadding.kt`；`GlassButtonDefaults.kt:24`。

## 3. 排版與背景

- `Type.kt:19`只覆寫bodyLarge：系統字體、Normal、16sp、24sp行高、0.5sp字距；其餘繼承Material Typography。不是靠特殊字體製造舒適感。
- 設定列標題使用bodyLarge/Medium，副文bodyMedium/onSurfaceVariant；上下文字距2dp。文字區weight(1f)，尾端控制各自占位。
- 首頁標題titleLarge/Bold，副文bodyMedium，副文字預設alpha0.75；不是所有文字同等粗、亮、大。
- 首頁和底部操作列使用共同內容寬度約束。分組視圖預留內容區域以穩定footer位置，平鋪列表則可隨內容置中（HomeSectionsLayout）。

預設主題：淺底 `#FDFCFF`，深底 `#1A1C1E`；淺主色 `#005FAC`、淺主容器 `#D4E3FF`；深主色 `#A4C9FF`、深主容器 `#004884`。動態色、自訂色、monochrome及pure-black會改變結果，不能當作使用者目前畫面的取色值。

普通卡片透過surfaceColorAtElevation、低tonalElevation和shadowElevation=0分層；分隔線預設左右縮排16dp。首頁彩色卡片另用一層斜向漸層（alpha0.70/0.58/0.64）和1.5dp四段邊框（白0.65→中間色0.30→尾色0.15→白0.20）。普通外框主要1dp，不是每張卡同時疊多層陰影。

GlassButton的選取填色透明度：淺0.72／深0.55；未選取淺0.60／深0.35。文字會按實際合成後的底色重新判斷黑／白；`readableOn`預設檢查3:1，不能據此聲稱所有一般文字都達4.5:1。

預設裝飾背景CIRCLES：六個圓、alpha0.035–0.05，半徑60–133dp，位移振幅約視窗寬高2.5–5%，循環6.5–9.5秒；parallax sensitivity0.3。背景層位於NavHost之下，與頁面內容分離。

查看的主要卡片／Glass控制以透明色與漸層營造玻璃感，沒有發現Haze或RenderEffect模糊實作。不要把名稱Glass誤認為必然有背景採樣模糊。

## 4. 動效：節奏而非統一放慢

| 場景 | 明確宣告的參數 |
|---|---|
| 一般dialog／展開／顏色轉換 |220ms|
| 短fade／部分退出 |180ms|
| 頁面進入 |320ms，縮放0.95→1加fade|
| Settings push |320ms，垂直滑入加fade；返回向下|
| Dialog進入 |220ms，0.95→1，縮放FastOutSlowIn；fade為LinearOutSlowIn|
| 一般展開／收合 |220ms，LinearOutSlowIn|
| 計數文字替換 |進200ms／出150ms，上下半字高位移|
| 緊湊計數badge |進150ms／出100ms|
| 清單排列 |spring stiffness400、dampingRatio0.8|
| 清單新增／移除 |fade進220ms／出180ms|
| Chip錯峰 |每項延40ms，上限8步=320ms；動畫本身220ms，scale0.8→1|
| 按壓 |1→0.97→1；MediumBouncy/Medium spring，沒有固定ms|

`Animations.kt`有共用preset，`MainActivity.kt:621`實際將screenEnter/Exit接到NavHost；Settings在`:717`覆寫push進出。實際互動還有局部覆寫，不應聲稱全App只有三種時長。

Spring完成時間取決於參數、距離與收斂閾值，不能換算成固定220ms。原始碼宣告exit亦不代表所有Dialog dismiss路徑都必定完整播放退出；需要執行期量測。

## 5. 按壓回饋不是全面形變

`PressFeedback.kt`讓按壓縮至0.97，放開彈回；數值在graphicsLayer讀取，不改變佈局尺寸。它避免旁邊文字、卡片因按壓重新排版。

首頁AppCard使用combinedClickable(indication=null)：主要是縮放＋VIRTUAL_KEY，長按LONG_PRESS；因此這一類卡片沒有同時再疊普通ripple。Dialog按鈕則是原生Button／OutlinedButton加pressScale，會保留其原生互動效果。Settings SurfaceCard使用clip後clickable，並未接入共用pressScale。GlassButton則clip圓角、使用Surface互動，pressScale與hapticFeedback預設false，呼叫端可以開啟。

所以沒有證據支持「Morphe每張卡都有M3E形狀morph」。它的共同語言是各類控制回饋可預期，而不是所有表面都施加同一個效果。

## 6. 對VibeFinance的可行參考（建議，未實作）

1. 先建立一致間距層級：內容16、卡內16、圖文12、輔助文字2–4；寬畫面逐級增加外距，資料型Bento可保留自己的寬度上限，無需整頁照搬560。
2. 保留現有較大圓角，但同一家族只使用明確的幾個token；不要全App一律改為Morphe的14/16。
3. 為主要財務指標保留彩色表面，其餘分析卡以中性色承托；副文降低層級，金額仍維持可讀性與完整顯示。
4. 維持使用者指定的「記帳按鍵只有形狀變化」及所有非導航卡可有灰色ripple；不直接照抄首頁AppCard的無ripple縮放。
5. 互動時長可參考150–220ms，頁面可參考約320ms；不要讓每次切頁重播整個財務儀表板。Slow background不是必需品；記帳App可先採靜態淡色背景。
6. 模糊只在真正需要邊緣分層的位置，避免多層全螢幕offscreen；原專案drawWithCache與背景遮擋／生命週期暫停值得研究，但不代表已達120fps。

限制：未安裝／操作Morphe，未錄影逐幀量測，未驗證所有字體大小、觸控範圍、Reduced Motion或幀率。本次為分析，沒有改動VibeFinance執行程式或建立release。

# ImageToolbox：原生元件基底與 Expressive 封裝分析

日期：2026-11-07。本機 checkout：`ImageToolbox`，commit `e73e1461e8`（Improved layout for large screens）；Material3依賴為1.5.0-alpha29。此為原始碼分析，沒有啟動App、錄影量測或宣稱實測無障礙／幀率。現有圖譜未包含此clone，直接查閱本機來源；未傳送程式碼到外部AI。

## 1. 原生程度：應按元件查證

- `EnhancedButton`實際委派Material3 `OutlinedButton`，保留ButtonDefaults、contentColorFor及InteractionSource，再加shapeByInteraction、顏色動畫、陰影和觸覺回饋。這是值得借鑑的集中封裝。
- `PreferenceItemOverload`使用Material3 Card，但Card本身shape=RectangleShape、背景透明；真正的外形／填色由container Modifier處理。只看到Card名稱不足以判定是原生預設外觀。
- Theme使用MaterialExpressiveTheme並注入CustomMotionScheme和modifiedShapes；此MotionScheme是專案自訂值，不等於官方M3E預設值。
- `EnhancedSwitch`有多種分支，domain DEFAULT為SwitchType.Compose；MaterialYou分支中的M3Switch混用透明Compose Switch與AndroidView/MaterialSwitch，並轉送觸控事件。不可稱為所有開關都是純Compose原生Switch。
- `EnhancedModalBottomSheet`在LocalInspectionMode預覽分支使用Material3 ModalBottomSheet；正常執行路徑使用com.t8rin.modalsheet狀態與元件。Preview與執行路徑不同，不能只憑預覽認定實機動效。

## 2. 尺寸、間距及形狀

| 來源 | 明確值 |
|---|---|
| ShapeDefaults |2、4、6（pressed）、8、10、12、16（default）、20、24、28dp，以及circle|
| byIndex |外側16dp、內接4dp；逐角動畫；呼叫端可覆寫|
| container |resultPadding預設4dp；非透明autoShadowElevation1dp；clip預設true|
| container陰影 |有正值邊框或陰影停用時elevation降為0；描邊寬度取設定，沒有固定全局1dp|
| PreferenceItem |外部水平12dp、內容16dp；可覆寫modifier|
| PreferenceRow |外部水平16dp（可停用）、內容垂直8dp；圖標版本按尾端控制改變padding|
| SettingGroupItem |外部padding2dp；標題額外start8dp；ExpandableItem header padding8dp|
| ExpandableItem |預設20dp形狀、pressed6dp；展開內容前後各8dp|
| EnhancedButton |原生ButtonDefaults.ContentPadding、AutoCircleShape；pressed採ButtonDefaults.pressedShape|
| 按鈕陰影 |無正值border且enabled時可為0.5dp，另受drawButtonShadows控制|
| EnhancedButtonGroup |預設外padding8dp；內start/end/bottom6dp、top8dp；小接角8dp、外圓角50%|
| Sheet defaults |頂角28dp、scrim alpha0.32、handle高4dp|

不能把各層padding一律相加成固定列高：呼叫端可能覆寫，尾端控制、字體大小亦會改變尺寸。

## 3. 動效及實際速度

CustomMotionScheme的實際回傳值：

| 方法 | 實際規格 |
|---|---|
| defaultSpatialSpec |tween400ms，FancyTransitionEasing|
| fastSpatialSpec |spring damping0.6、stiffness800|
| slowSpatialSpec |spring damping0.8、stiffness200|
| defaultEffectsSpec |spring damping1.0、stiffness1600|
| fastEffectsSpec |tween300ms，FancyTransitionEasing|
| slowEffectsSpec |tween500ms，FancyTransitionEasing|

Motion.kt雖另外宣告default spatial damping0.8/stiffness380，實際defaultSpatialSpec使用的是400ms tween；不能把未使用常數當成實際行為。

- FancyTransitionEasing=CubicBezierEasing(0.48,0.19,0.05,1.03)。AlphaEasing=(0.4,0.4,0.17,0.9)；PointToPointEasing=(0.55,0.55,0,1)。
- shapeByInteraction預設lessSpringySpec：damping0.4、stiffness=Spring.StiffnessLow。監聽Press/Release/Cancel及Focus/Unfocus，非只看選取狀態；domain DEFAULT的shapeByInteractionThrottle=200ms。
- ExpandableItem內容進入：500ms fade+expand；退出：700ms fade+shrink；外框animateContentSizeNoClip另用10ms tween。這是兩層動畫，不可把10ms當整個展開時長。
- fancySlideTransition共用預設600ms水平滑動；fade300ms、delay100ms。返回分支直接用600ms，不跟隨函式duration參數。
- toolboxPredictiveBackAnimation使用原生predictive animatable，fallback分別fade300ms、slide400ms、scale500ms。這是fallback組合，不代表每次返回都按完整固定時長執行。
- Theme顏色切換400ms Fancy easing；部分AnimatedContent/animateDpAsState沿用框架預設，未逐一宣告固定ms。

## 4. Layout與文字層級

AdaptiveBottomScaffoldLayoutScreen使用Scaffold、TopAppBarDefaults.exitUntilCollapsedScrollBehavior、橫向／直向不同內容與操作區安排；部分模式另有BottomSheetScaffold。是因應工具內容而變化的架構，不適合原樣取代四分頁記帳主框架。

PreferenceItemDefaults明確字體：標題16sp/Medium/18sp行高；副文12sp/Normal/14sp；中等副文13.5sp/15sp。PreferenceRow標題／副文距2dp，文字欄weight(1f)；TitleItem尾端控制及圖文一般8dp間距。這些密度適合工具設定，但繁中財務說明不應因此全面縮到12sp。

設定群組以共同容器與內接小圓角顯示關係，比每列都加厚外框更容易掃讀。不存在可從原始碼證實的全App固定留白百分比。

## 5. 色彩與背景

- DynamicTheme支援動態色、AMOLED、色盲模式、對比設定、theme style、反色與字體選擇；MaterialExpressiveTheme共用Shapes/MotionScheme。沒有一套固定hex能代表所有使用者畫面。
- SettingGroupItem顏色為surfaceContainer.blend(surfaceContainerLowest,0.4)。
- mixedContainer=tertiaryContainer.blend(primaryContainer,0.4)。
- container會先把半透明color合成到background；普通容器不必一直透出後面的動畫背景。
- container呼叫outlineVariant(luminance=0.1,onTopOf=containerColor)，其實是onSecondaryContainer的alpha0.1合成，參數名不是WCAG對比度。該helper預設參數另為0.3，不要混淆呼叫值。
- Sheet surfaceContainerLow、操作區surfaceContainerHigh；內容surfaceContainer向surfaceContainerHigh混合0.3；scrim0.32。
- 普通ThemeSurface是填滿視窗的Surface+Box。已查設定群組的層次主要來自語意surface、圖標容器與邊界；這不代表整個大型專案沒有別的blur／shader功能。

## 6. 按壓、Ripple與平台語意

`EnhancedButton`讓shapeByInteraction與OutlinedButton共用同一個MutableInteractionSource；ripple、pressed/focus形狀更容易對齊。ExpandableItem亦將相同animatedShape同時用於外container與header.clip，讓展開狀態的按壓邊界跟隨形狀。

hapticsClickable仍委派foundation.clickable，預設保留LocalIndication，並在click包裝觸覺。沒有證據支持一套全局固定自訂ripple alpha。

值得警惕的具體實作：
- EnhancedButton把LocalMinimumInteractiveComponentSize設為Dp.Unspecified，並以透明Surface覆蓋停用態、底層OutlinedButton仍enabled=true；不能當作無障礙停用狀態的標準範本。
- 長按／release有自己的處理，且某些按鈕會clearFocus；不能直接搬到需保持鍵盤開啟的交易表單。
- EnhancedButtonGroup的press會透過layout或weight增加按鈕寬度；這會重新排版，不是零layout的GPU縮放。VibeFinance既有連接按鈕尺寸與外角規則優先。
- shapeByInteraction200ms節流可能影響快速連點；不能搬到數字鍵盤。
- 原專案haptics實作不取代VibeFinance的強度／關閉設定。

## 7. VibeFinance採用決策

優先順序：原生Material元件與平台語意 → VibeFinance現有共用元件與使用者偏好 → 薄封裝統一tokens／InteractionSource → 有明確需求及測試才自訂手勢或繪製。

採用：原生按鈕／開關基底、theme-level shapes/motion、共享interaction、按壓同形clip、群組外圓內小角、語意色背景、Preview多狀態驗證。

保留本App：右側全螢幕編輯、原生AppModalBottomSheet、48dp觸控區、原生enabled/selected/switch語意、鍵盤持續開啟、VibeHaptics、字體縮放及完整金額。不要全面套用600ms滑頁、500/700ms展開、12sp副文、6dp pressed角或寬度擠壓動畫。

## 來源索引（本機路徑與行號）

原始碼短節錄（完整enabled／長按／顏色處理仍須參照原檔）：

```kotlin
val animatedShape = shapeByInteraction(
    shape = shape,
    pressedShape = pressedShape,
    interactionSource = interactionSource
)
```

這個animatedShape與interactionSource再傳入OutlinedButton。可取的是共用同一個互動來源，不代表應照抄原包裝的停用態覆蓋處理。

```kotlin
inline fun <T> lessSpringySpec() = spring<T>(
    dampingRatio = 0.4f,
    stiffness = Spring.StiffnessLow
)
```

此helper屬專案動畫參數；VibeFinance並不因文件分析便將全部原生動畫改為此彈簧。

以下路徑均相對於ImageToolbox：
- core/ui/src/main/kotlin/com/t8rin/imagetoolbox/core/ui/theme/Theme.kt:47、Motion.kt:30、Color.kt:38
- core/ui/src/main/kotlin/com/t8rin/imagetoolbox/core/ui/utils/animation/Animations.kt:47、Easing.kt:22
- core/ui/src/main/kotlin/com/t8rin/imagetoolbox/core/ui/widget/modifier/ShapeDefaults.kt:62及495、Container.kt:49
- core/ui/src/main/kotlin/com/t8rin/imagetoolbox/core/ui/widget/enhanced/EnhancedButton.kt:60、EnhancedSwitch.kt:61、EnhancedHapticFeedback.kt:143
- core/ui/src/main/kotlin/com/t8rin/imagetoolbox/core/ui/widget/switches/M3Switch.kt:50
- core/ui/src/main/kotlin/com/t8rin/imagetoolbox/core/ui/widget/preferences/PreferenceItem.kt:64及144、PreferenceItemOverload.kt:64
- core/ui/src/main/kotlin/com/t8rin/imagetoolbox/core/ui/widget/other/ExpandableItem.kt:62
- feature/settings/src/main/java/com/t8rin/imagetoolbox/feature/settings/presentation/components/SettingGroupItem.kt:42

僅新增分析／規範文件；沒有改動金融資料、App程式、版本或裝置。未做實機互動與效能量測；主題對比與無障礙必須在未來採用時逐元件驗證。

---
name: material-3-expressive
description: >-
  Actionable design patterns, implementation recipes, and component templates for Material 3
  Expressive Design, spring-based kinetics, and responsive multi-device layouts in Android Jetpack Compose.
  Use when building or refining UI components, animations, Bento grids, or responsive layouts.
---

# Material 3 Expressive & Responsive Design Skill

This skill provides verified, production-ready recipes and guidelines for crafting Material 3 Expressive UI, organic spring kinetics, and responsive layouts in Jetpack Compose.

---

## 1. Liquid Dynamic Progress Bar Recipe

Use this pattern when creating progress indicators with liquid wavy shapes and spring responsiveness (such as the Hero Budget card).

### Implementation

```kotlin
@Composable
fun LiquidProgressBar(
    ratio: Float, // 0f to 1f
    containerColor: Color,
    waveColor: Color,
    modifier: Modifier = Modifier
) {
    // 1. Spring physics for fluid ratio expansion/contraction
    val animatedRatio by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "liquidRatioSpring"
    )

    // 2. Continuous horizontal wave movement
    val infiniteTransition = rememberInfiniteTransition(label = "waveShift")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveShift"
    )

    // 3. Layer hierarchy: Box with matchParentSize containing an inner Box with fillMaxWidth
    Box(
        modifier = modifier
            .background(containerColor)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedRatio.coerceIn(0.001f, 1f))
                    .background(
                        color = waveColor,
                        shape = WavyShape(
                            period = 36.dp,
                            amplitude = 4.dp,
                            shift = shift
                        )
                    )
            )
        }
    }
}
```

> [!IMPORTANT]
> **Modifier Ordering Guardrail:** Never place `.background(...)` directly after `.matchParentSize()`. The inner Box must be bounded by `.fillMaxWidth(fraction)` *before* drawing the background shape.

---

## 2. Spring-Animated Rolling Number Text

Use this pattern for metric amounts, counters, and currency displays to roll each digit with spring physics without recomposition flicker.

```kotlin
@Composable
fun RollingNumberText(
    text: String,
    style: TextStyle,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        text.forEachIndexed { index, char ->
            if (char.isDigit()) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        val digitSpring = spring<IntOffset>(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                        if (targetState >= initialState) {
                            (slideInVertically(animationSpec = digitSpring) { height -> height } + fadeIn())
                                .togetherWith(slideOutVertically(animationSpec = digitSpring) { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically(animationSpec = digitSpring) { height -> -height } + fadeIn())
                                .togetherWith(slideOutVertically(animationSpec = digitSpring) { height -> height } + fadeOut())
                        }
                    },
                    label = "digit_$index"
                ) { animatedChar ->
                    Text(
                        text = animatedChar.toString(),
                        style = style,
                        fontWeight = fontWeight,
                        color = color
                    )
                }
            } else {
                Text(
                    text = char.toString(),
                    style = style,
                    fontWeight = fontWeight,
                    color = color
                )
            }
        }
    }
}
```

> [!TIP]
> Always pass the **discrete formatted target string** (e.g. `String.format("HK$ %,.0f", targetAmount)`) into `RollingNumberText`. Never pass a continuously animated float from `animateFloatAsState`.

---

## 3. Bento Grid Dashboard Scaffolding

Group dashboard metrics into an expressive, asymmetrical Bento Grid matrix:

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    // 60% prominent metric
    Surface(
        modifier = Modifier.weight(0.6f),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Remaining", style = MaterialTheme.typography.labelMedium)
            RollingNumberText("HK$ 1,293", style = MaterialTheme.typography.headlineMedium)
        }
    }

    // 40% secondary gauge or status pill
    Surface(
        modifier = Modifier.weight(0.4f),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Days Left", style = MaterialTheme.typography.labelSmall)
            Text("14", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
```

---

## 4. Edge-Fading Scrollable Preset Chips

Prevent cut-off chips and popular preset rows at screen boundaries:

```kotlin
@Composable
fun EdgeFadingChipRow(
    items: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                // Left fade hint
                drawRect(
                    brush = Brush.horizontalGradient(
                        0.0f to Color.Transparent,
                        0.03f to Color.Black
                    ),
                    blendMode = BlendMode.DstIn
                )
                // Right fade hint
                drawRect(
                    brush = Brush.horizontalGradient(
                        0.97f to Color.Black,
                        1.0f to Color.Transparent
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                SuggestionChip(
                    onClick = { onSelect(item) },
                    label = { Text(item) },
                    shape = CircleShape
                )
            }
        }
    }
}
```

---

## 5. Responsive Layout Adaptation (`WindowWidthSizeClass`)

Adapt screens across phones, foldables, and tablets:

| Window Size Class | Breakpoint | Recommended Component Strategy |
| :--- | :--- | :--- |
| **Compact** | `< 600dp` | Single-column Bento matrix, Bottom Navigation Bar, Modal Bottom Sheet. |
| **Medium** | `600dp - 840dp` | 2-column Bento grid, Navigation Rail, side sheet modals. |
| **Expanded** | `> 840dp` | 3-column / asymmetric dashboard, persistent navigation drawer, dual-pane list-detail. |

```kotlin
@Composable
fun AdaptiveContainer(
    windowSizeClass: WindowWidthSizeClass,
    content: @Composable () -> Unit
) {
    when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Single-column vertical flow
            Column(modifier = Modifier.fillMaxSize()) { content() }
        }
        WindowWidthSizeClass.Medium -> {
            // 2-column adaptive flow
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) { content() }
            }
        }
        WindowWidthSizeClass.Expanded -> {
            // Side navigation + wide multi-pane content
            PermanentNavigationDrawer(...) {
                Box(modifier = Modifier.fillMaxSize()) { content() }
            }
        }
    }
}
```

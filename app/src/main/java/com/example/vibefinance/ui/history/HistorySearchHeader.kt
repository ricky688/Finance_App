@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalLayoutApi::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class,
)

package com.example.vibefinance.ui.history

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.entity.historyAmountFor
import com.example.vibefinance.ui.common.horizontalFadingEdge
import com.example.vibefinance.ui.components.ExpenseEventIcon
import com.example.vibefinance.util.CurrencyEngine
import com.example.vibefinance.util.VibeHaptics
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.launch

/** Recent History searches, newest first, kept on the device only. */
internal class RecentHistorySearches(context: Context) {
    private val prefs = context.getSharedPreferences("history_search", Context.MODE_PRIVATE)
    val items = mutableStateListOf<String>().apply {
        addAll(prefs.getString(KEY, "").orEmpty().split('\n').filter { it.isNotBlank() })
    }

    fun add(term: String) {
        val clean = term.trim()
        if (clean.isEmpty()) return
        items.remove(clean)
        items.add(0, clean)
        while (items.size > MAX) items.removeAt(items.lastIndex)
        save()
    }

    fun clear() { items.clear(); save() }

    private fun save() = prefs.edit().putString(KEY, items.joinToString("\n")).apply()

    private companion object {
        const val KEY = "recent"
        const val MAX = 8
    }
}

/**
 * History search header (M3 Expressive, redesign item 3): a pinned [SearchBar] with filter chips
 * under it. Tapping the bar expands in-place inside a single [SharedTransitionLayout] so the
 * search surface, leading navigation icon, placeholder, and filter chips smoothly morph and move
 * without hopping across separate Popup/Dialog windows.
 */
@Composable
internal fun HistorySearchHeader(
    query: HistoryQuery,
    onQueryChange: (HistoryQuery) -> Unit,
    accounts: List<AccountEntity>,
    categories: List<String>,
    results: List<TransactionEntity>,
    wide: Boolean,
    onOpenMore: () -> Unit,
    onOpenResult: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val recent = remember { RecentHistorySearches(context.applicationContext) }
    val searchBarState = rememberSearchBarState()
    val textState = rememberTextFieldState(query.text)
    val currentQuery by rememberUpdatedState(query)
    val currentOnChange by rememberUpdatedState(onQueryChange)
    val currentOnExpandedChange by rememberUpdatedState(onExpandedChange)
    var userActivatedSearch by remember { mutableStateOf(false) }
    val expandedFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val minimalMotion = com.example.vibefinance.ui.preferences.LocalExperience.current.motion ==
        com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL
    // Typed text drives the shared History query (and so the list behind the search view).
    LaunchedEffect(textState) {
        snapshotFlow { textState.text.toString() }.collect { text ->
            if (text != currentQuery.text) currentOnChange(currentQuery.copy(text = text))
        }
    }
    // External clears (for example "clear search and filters") flow back into the field.
    LaunchedEffect(query.text) {
        if (textState.text.toString() != query.text) textState.setTextAndPlaceCursorAtEnd(query.text)
    }
    val expanded = searchBarState.targetValue == SearchBarValue.Expanded
    LaunchedEffect(expanded) {
        currentOnExpandedChange(expanded)
        if (expanded) {
            runCatching { expandedFocusRequester.requestFocus() }
        } else {
            userActivatedSearch = false
            focusManager.clearFocus(force = true)
        }
    }
    BackHandler(enabled = expanded) {
        scope.launch { searchBarState.animateToCollapsed() }
    }
    val hint = stringResource(R.string.history_search_hint)

    @Composable
    fun SearchInputField(
        isExpandedOverlay: Boolean,
        navBoundsModifier: Modifier,
        navIconModifier: Modifier,
        placeholderBoundsModifier: Modifier,
    ) {
        val fieldModifier = if (isExpandedOverlay) {
            Modifier
                .fillMaxWidth()
                .focusRequester(expandedFocusRequester)
                .testTag("HistorySearch")
        } else {
            Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        userActivatedSearch = true
                    }
                }
                .focusProperties { canFocus = expanded || userActivatedSearch }
                .testTag("HistorySearch")
        }
        SearchBarDefaults.InputField(
            textFieldState = textState,
            searchBarState = searchBarState,
            onSearch = { term ->
                recent.add(term)
                scope.launch { searchBarState.animateToCollapsed() }
            },
            modifier = fieldModifier,
            placeholder = {
                Text(
                    hint,
                    modifier = placeholderBoundsModifier,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                SearchNavigationLeadingElement(
                    showBack = isExpandedOverlay,
                    navBoundsModifier = navBoundsModifier,
                    navIconModifier = navIconModifier,
                    onBack = { scope.launch { searchBarState.animateToCollapsed() } }
                )
            },
            trailingIcon = if (textState.text.isNotEmpty()) {
                {
                    IconButton(
                        onClick = { VibeHaptics.selection(view); textState.setTextAndPlaceCursorAtEnd("") },
                        modifier = Modifier.testTag("HistorySearchClear")
                    ) { Icon(Icons.Default.Close, stringResource(R.string.history_search_clear)) }
                }
            } else null,
        )
    }

    SharedTransitionLayout(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                val enter = fadeIn(
                    if (minimalMotion) snap() else com.example.vibefinance.theme.VibeMotion.defaultEffects()
                ) + expandVertically(
                    animationSpec = if (minimalMotion) snap() else com.example.vibefinance.theme.VibeMotion.defaultSpatial(),
                    expandFrom = Alignment.Top
                )
                val exit = fadeOut(
                    if (minimalMotion) snap() else com.example.vibefinance.theme.VibeMotion.fastEffects()
                ) + shrinkVertically(
                    animationSpec = if (minimalMotion) snap() else com.example.vibefinance.theme.VibeMotion.defaultSpatial(),
                    shrinkTowards = Alignment.Top
                )
                enter togetherWith exit
            },
            label = "HistorySearchInPlaceTransition"
        ) { isExpanded ->
            val navBoundsModifier = if (minimalMotion) {
                Modifier
            } else {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState("history-search-nav-bounds"),
                    animatedVisibilityScope = this,
                    boundsTransform = { _, _ -> com.example.vibefinance.theme.VibeMotion.defaultSpatial() },
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
            }
            val navIconModifier = if (minimalMotion) {
                Modifier
            } else {
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState("history-search-nav-icon"),
                    animatedVisibilityScope = this,
                    boundsTransform = { _, _ -> com.example.vibefinance.theme.VibeMotion.defaultSpatial() }
                )
            }
            val barBoundsModifier = if (minimalMotion) {
                Modifier
            } else {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState("history-search-bar-surface"),
                    animatedVisibilityScope = this,
                    boundsTransform = { _, _ -> com.example.vibefinance.theme.VibeMotion.defaultSpatial() },
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
            }
            val placeholderBoundsModifier = if (minimalMotion) {
                Modifier
            } else {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState("history-search-placeholder"),
                    animatedVisibilityScope = this,
                    boundsTransform = { _, _ -> com.example.vibefinance.theme.VibeMotion.defaultSpatial() }
                )
            }
            val chipsBoundsModifier = if (minimalMotion) {
                Modifier
            } else {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState("history-filter-chips-bar"),
                    animatedVisibilityScope = this,
                    boundsTransform = { _, _ -> com.example.vibefinance.theme.VibeMotion.defaultSpatial() },
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
            }

            if (!isExpanded) {
                Column(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchBar(
                        state = searchBarState,
                        inputField = {
                            SearchInputField(
                                isExpandedOverlay = false,
                                navBoundsModifier = navBoundsModifier,
                                navIconModifier = navIconModifier,
                                placeholderBoundsModifier = placeholderBoundsModifier,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(barBoundsModifier)
                            .testTag("HistorySearchBar")
                    )
                    HistoryFilterChips(
                        query = query,
                        onQueryChange = onQueryChange,
                        accounts = accounts,
                        categories = categories,
                        onOpenMore = onOpenMore,
                        modifier = chipsBoundsModifier
                    )
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("HistorySearchExpandedPanel"),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .then(barBoundsModifier)
                                .testTag("HistorySearchBar")
                        ) {
                            SearchInputField(
                                isExpandedOverlay = true,
                                navBoundsModifier = navBoundsModifier,
                                navIconModifier = navIconModifier,
                                placeholderBoundsModifier = placeholderBoundsModifier,
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HistoryFilterChips(
                                query = query,
                                onQueryChange = onQueryChange,
                                accounts = accounts,
                                categories = categories,
                                onOpenMore = { scope.launch { searchBarState.animateToCollapsed(); onOpenMore() } },
                                modifier = Modifier.padding(horizontal = 16.dp).then(chipsBoundsModifier)
                            )
                            if (textState.text.isEmpty() && recent.items.isNotEmpty()) {
                                Row(
                                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stringResource(R.string.history_recent_searches),
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    com.example.vibefinance.ui.components.CompletePressTextButton(
                                        onClick = { recent.clear() },
                                        shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                                    ) { Text(stringResource(R.string.history_clear_recent)) }
                                }
                                FlowRow(
                                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    recent.items.forEach { term ->
                                        SuggestionChip(
                                            onClick = { VibeHaptics.selection(view); textState.setTextAndPlaceCursorAtEnd(term) },
                                            label = { Text(term, maxLines = 1) },
                                            icon = { Icon(Icons.Default.History, null, Modifier.size(18.dp)) }
                                        )
                                    }
                                }
                            }
                            if (query != HistoryQuery()) {
                                val shownResults = remember(results) { results.take(200) }
                                Text(
                                    stringResource(R.string.history_results_count, results.size),
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 16.dp).testTag("HistoryResultsCount")
                                )
                                LazyColumn(
                                    Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = if (wide) 320.dp else 400.dp)
                                        .padding(horizontal = 12.dp)
                                        .testTag("HistorySearchResults"),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    itemsIndexed(shownResults, key = { _, it -> it.id }) { index, tx ->
                                        SearchResultRow(
                                            tx = tx,
                                            accounts = accounts,
                                            isFirst = index == 0,
                                            isLast = index == shownResults.lastIndex
                                        ) {
                                            VibeHaptics.selection(view)
                                            recent.add(textState.text.toString())
                                            scope.launch { searchBarState.animateToCollapsed() }
                                            onOpenResult(tx)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchNavigationLeadingElement(
    showBack: Boolean,
    navBoundsModifier: Modifier,
    navIconModifier: Modifier,
    onBack: () -> Unit,
) {
    if (showBack) {
        IconButton(
            onClick = onBack,
            modifier = navBoundsModifier.testTag("HistorySearchNavBack")
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.history_search_back),
                modifier = navIconModifier
            )
        }
    } else {
        Box(
            modifier = navBoundsModifier
                .size(24.dp)
                .semantics(mergeDescendants = true) {}
                .testTag("HistorySearchNavSearch"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = navIconModifier
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    tx: TransactionEntity,
    accounts: List<AccountEntity>,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val amount = tx.historyAmountFor()
    val date = remember(tx.timestamp) {
        Instant.ofEpochMilli(tx.timestamp).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
    val account = accounts.firstOrNull { it.id == tx.accountId }?.name
    val hide = com.example.vibefinance.ui.preferences.LocalExperience.current.hideAmounts
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val rowShape = com.example.vibefinance.ui.main.rememberConnectedListRowShape(
        isFirst = isFirst,
        isLast = isLast,
        isSelected = false,
        isPressed = isPressed
    )
    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = rowShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().testTag("HistorySearchResult_${tx.id}")
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                ExpenseEventIcon(
                    customIcon = if (tx.isReimbursement) "icon:reimbursement" else tx.customIcon,
                    category = tx.category, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)
                )
            },
            headlineContent = {
                Text(
                    tx.description.ifBlank { com.example.vibefinance.ui.home.getCategoryDisplayName(tx.category) },
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = { Text(listOfNotNull(date, account).joinToString(" · "), maxLines = 1) },
            trailingContent = {
                Text(
                    if (hide) "••••" else CurrencyEngine.formatSignedAmount(amount),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (amount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        )
    }
}

/**
 * Filter chips replacing the filter sheet for everyday filters. Type chips toggle; account and
 * category open menus; date opens the M3 date range picker; "More" keeps the full sheet for
 * amount ranges and the rarer types. The row scrolls sideways with faded edges.
 */
@Composable
internal fun HistoryFilterChips(
    query: HistoryQuery,
    onQueryChange: (HistoryQuery) -> Unit,
    accounts: List<AccountEntity>,
    categories: List<String>,
    onOpenMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val scroll = rememberScrollState()
    var accountMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var datePicker by rememberSaveable { mutableStateOf(false) }
    fun change(next: HistoryQuery) { VibeHaptics.selection(view); onQueryChange(next) }

    Row(
        modifier
            .fillMaxWidth()
            .horizontalFadingEdge(scroll)
            .horizontalScroll(scroll)
            .testTag("HistoryFilterChips"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            HistoryKind.ALL to R.string.history_filter_all,
            HistoryKind.EXPENSE to R.string.history_kind_expense,
            HistoryKind.INCOME to R.string.history_kind_income,
            HistoryKind.TRANSFER to R.string.history_kind_transfer,
        ).forEach { (kind, label) ->
            val selected = query.kind == kind
            FilterChip(
                selected = selected,
                onClick = { change(query.copy(kind = if (selected && kind != HistoryKind.ALL) HistoryKind.ALL else kind)) },
                label = { Text(stringResource(label), maxLines = 1) },
                leadingIcon = if (selected) {{ Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }} else null,
                modifier = Modifier.testTag("HistoryChip_${kind.name}")
            )
        }
        Box {
            val name = accounts.firstOrNull { it.id == query.account }?.name
            FilterChip(
                selected = query.account != null,
                onClick = { VibeHaptics.selection(view); accountMenu = true },
                label = { Text(name ?: stringResource(R.string.history_filter_account), maxLines = 1) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(FilterChipDefaults.IconSize)) },
                modifier = Modifier.testTag("HistoryChip_Account")
            )
            DropdownMenu(expanded = accountMenu, onDismissRequest = { accountMenu = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.history_filter_all)) },
                    onClick = { accountMenu = false; change(query.copy(account = null)) })
                accounts.forEach { account ->
                    DropdownMenuItem(text = { Text(account.name) },
                        onClick = { accountMenu = false; change(query.copy(account = account.id)) })
                }
            }
        }
        Box {
            FilterChip(
                selected = query.category != null,
                onClick = { VibeHaptics.selection(view); categoryMenu = true },
                label = {
                    Text(query.category?.let { com.example.vibefinance.ui.home.getCategoryDisplayName(it) }
                        ?: stringResource(R.string.history_filter_category), maxLines = 1)
                },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(FilterChipDefaults.IconSize)) },
                modifier = Modifier.testTag("HistoryChip_Category")
            )
            DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.history_filter_all)) },
                    onClick = { categoryMenu = false; change(query.copy(category = null)) })
                categories.forEach { category ->
                    DropdownMenuItem(text = { Text(com.example.vibefinance.ui.home.getCategoryDisplayName(category)) },
                        onClick = { categoryMenu = false; change(query.copy(category = category)) })
                }
            }
        }
        val hasDates = query.from.isNotBlank() || query.through.isNotBlank()
        FilterChip(
            selected = hasDates,
            onClick = { VibeHaptics.selection(view); datePicker = true },
            label = { Text(if (hasDates) dateRangeLabel(query) else stringResource(R.string.history_filter_date), maxLines = 1) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(FilterChipDefaults.IconSize)) },
            modifier = Modifier.testTag("HistoryChip_Date")
        )
        val moreActive = query.minimum.isNotBlank() || query.maximum.isNotBlank() ||
            query.kind == HistoryKind.ADJUSTMENT || query.kind == HistoryKind.REIMBURSEMENT
        FilterChip(
            selected = moreActive,
            onClick = { VibeHaptics.selection(view); onOpenMore() },
            label = { Text(stringResource(R.string.history_filter_more), maxLines = 1) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(FilterChipDefaults.IconSize)) },
            modifier = Modifier.testTag("HistoryChip_More")
        )
        Spacer(Modifier.size(8.dp))
    }

    if (datePicker) {
        val toUtc = { iso: String -> runCatching { LocalDate.parse(iso).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull() }
        var startUtcMillis by rememberSaveable(query.from) { mutableStateOf(toUtc(query.from)) }
        var endUtcMillis by rememberSaveable(query.through) { mutableStateOf(toUtc(query.through)) }
        val toIso = { millis: Long? -> millis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }.orEmpty() }
        com.example.vibefinance.ui.components.VibeDateRangePickerDialog(
            startUtcMillis = startUtcMillis,
            endUtcMillis = endUtcMillis,
            onRangeSelected = { s, e -> startUtcMillis = s; endUtcMillis = e },
            onDismissRequest = { datePicker = false },
            title = {
                Text(
                    stringResource(R.string.history_date_range_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                com.example.vibefinance.ui.components.CompletePressTextButton(
                    enabled = startUtcMillis != null,
                    onClick = {
                        datePicker = false
                        val from = toIso(startUtcMillis)
                        change(query.copy(from = from, through = toIso(endUtcMillis).ifBlank { from }))
                    },
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                    modifier = Modifier.testTag("HistoryDateRangeConfirm")
                ) { Text(stringResource(R.string.btn_confirm)) }
            },
            dismissButton = {
                Row {
                    com.example.vibefinance.ui.components.CompletePressTextButton(
                        onClick = { datePicker = false; change(query.copy(from = "", through = "")) },
                        shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.history_filter_clear))
                    }
                    com.example.vibefinance.ui.components.CompletePressTextButton(
                        onClick = { datePicker = false },
                        shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                    ) { Text(stringResource(R.string.btn_cancel)) }
                }
            }
        )
    }
}

private fun dateRangeLabel(query: HistoryQuery): String {
    val fmt = DateTimeFormatter.ofPattern("M/d")
    val from = runCatching { LocalDate.parse(query.from).format(fmt) }.getOrNull()
    val through = runCatching { LocalDate.parse(query.through).format(fmt) }.getOrNull()
    return when {
        from != null && through != null && from != through -> "$from – $through"
        from != null -> from
        through != null -> through
        else -> ""
    }
}

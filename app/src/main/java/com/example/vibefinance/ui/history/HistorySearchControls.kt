package com.example.vibefinance.ui.history

import com.example.vibefinance.ui.components.AppModalBottomSheet as ModalBottomSheet

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.ui.common.ScrollBlurContainer
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import com.example.vibefinance.ui.preferences.PrivacyText as Text

/** Occupies the first History list slot, below the measured page headings and before the period card. */
@Composable
fun AnimatedHistorySearchBar(visibility: MutableTransitionState<Boolean>, query: HistoryQuery, count: Int,
    onChange: (HistoryQuery) -> Unit, onOpenFilters: () -> Unit) {
    val searchDescription = stringResource(R.string.history_search_hint)
    val minimal = LocalExperience.current.motion == MotionLevel.MINIMAL
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    AnimatedVisibility(visibleState = visibility, modifier = Modifier.clipToBounds().testTag("HistorySearchReveal"),
        enter = if (minimal) EnterTransition.None else
            slideInVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) { -it } +
                expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Top) +
                fadeIn(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
        exit = if (minimal) ExitTransition.None else
            slideOutVertically(tween(300, easing = CubicBezierEasing(.2f, 0f, 0f, 1f))) { -it } +
                shrinkVertically(tween(300, easing = CubicBezierEasing(.2f, 0f, 0f, 1f)), shrinkTowards = Alignment.Top) +
                fadeOut(tween(220))) {
        // Wait for the drop-down to settle before the IME changes the available window bounds.
        LaunchedEffect(visibility.isIdle, visibility.currentState, visibility.targetState) {
            if (visibility.isIdle && visibility.currentState && visibility.targetState) focus.requestFocus()
        }
        Column(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(query.text, { onChange(query.copy(text = it)) },
                modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("HistorySearch").semantics { contentDescription = searchDescription },
                placeholder = { Text(stringResource(R.string.history_search_title), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                singleLine = true, shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); focusManager.clearFocus() }),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    IconButton(onClick = { focusManager.clearFocus(); keyboard?.hide(); onOpenFilters() },
                        modifier = Modifier.testTag("HistoryFiltersToggle")) {
                        Icon(Icons.Default.FilterList, stringResource(R.string.history_filters))
                    }
                })
            Text(stringResource(R.string.history_results_count, count), style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.testTag("HistoryResultsCount"))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFilterSheet(query: HistoryQuery, accounts: List<AccountEntity>, categories: List<String>, count: Int,
    onChange: (HistoryQuery) -> Unit, onClear: () -> Unit, onDismiss: () -> Unit) {
    val scroll = rememberScrollState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag("HistoryFiltersSheet")) {
        Column(Modifier.fillMaxWidth().heightIn(max = (LocalConfiguration.current.screenHeightDp * .85f).dp).imePadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.history_filters), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.history_results_count, count), modifier = Modifier.testTag("HistoryFilterResultsCount"),
                        style = MaterialTheme.typography.labelLarge)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp).testTag("HistoryFiltersClose")) {
                    Icon(Icons.Default.Close, stringResource(R.string.history_filters_done))
                }
            }
            ScrollBlurContainer(scroll, Modifier.fillMaxWidth().weight(1f, fill = false).padding(horizontal = 20.dp),
                vertical = true, tagPrefix = "HistoryFilterScroll") { source ->
                Column(source.verticalScroll(scroll).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HistoryFilterControls(query, accounts, categories, onChange, onClear)
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterControls(query: HistoryQuery, accounts: List<AccountEntity>, categories: List<String>,
    onChange: (HistoryQuery) -> Unit, onClear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterDropdown(stringResource(R.string.history_filter_account), query.account?.let { id -> accounts.firstOrNull { it.id == id }?.let { it.nickname?.takeIf(String::isNotBlank) ?: it.name } }
            ?: stringResource(R.string.history_filter_all), "HistoryAccountFilter", listOf(null to stringResource(R.string.history_filter_all)) + accounts.map { it.id to (it.nickname?.takeIf(String::isNotBlank) ?: it.name) }) {
            onChange(query.copy(account = it))
        }
        FilterDropdown(stringResource(R.string.history_filter_category), query.category ?: stringResource(R.string.history_filter_all),
            "HistoryCategoryFilter", listOf(null to stringResource(R.string.history_filter_all)) + categories.map { it to it }) {
            onChange(query.copy(category = it))
        }
        val scroll = rememberScrollState()
        ScrollBlurContainer(scroll, Modifier.fillMaxWidth(), tagPrefix = "HistoryTypeFilters") { source ->
            Row(source.horizontalScroll(scroll), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HistoryKind.entries.forEach { kind ->
                    FilterChip(query.kind == kind, { onChange(query.copy(kind = kind)) }, label = {
                        Text(stringResource(when (kind) {
                            HistoryKind.ALL -> R.string.history_filter_all; HistoryKind.EXPENSE -> R.string.history_kind_expense
                            HistoryKind.INCOME -> R.string.history_kind_income; HistoryKind.TRANSFER -> R.string.history_kind_transfer
                            HistoryKind.ADJUSTMENT -> R.string.history_kind_adjustment
                        }), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                    }, modifier = Modifier.testTag("HistoryKind_${kind.name}"))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QueryField(query.from, { onChange(query.copy(from = it)) }, R.string.history_filter_from, "HistoryFrom", Modifier.weight(1f))
            QueryField(query.through, { onChange(query.copy(through = it)) }, R.string.history_filter_through, "HistoryThrough", Modifier.weight(1f))
        }
        Text(stringResource(R.string.history_date_hint), style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QueryField(query.minimum, { onChange(query.copy(minimum = it)) }, R.string.history_filter_minimum, "HistoryMinimum", Modifier.weight(1f), true)
            QueryField(query.maximum, { onChange(query.copy(maximum = it)) }, R.string.history_filter_maximum, "HistoryMaximum", Modifier.weight(1f), true)
        }
        if (!query.valid()) Text(stringResource(R.string.history_filter_invalid), color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onClear, modifier = Modifier.testTag("HistoryClearFilters")) {
            Text(stringResource(R.string.history_clear_filters))
        }
    }
}

@Composable
private fun QueryField(value: String, change: (String) -> Unit, label: Int, tag: String, modifier: Modifier, decimal: Boolean = false) {
    OutlinedTextField(value, change, modifier.testTag(tag), label = { Text(stringResource(label)) }, singleLine = true,
        visualTransformation = if (decimal && com.example.vibefinance.ui.preferences.LocalExperience.current.hideAmounts)
            androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Text))
}

@Composable
private fun <T> FilterDropdown(label: String, selected: String, tag: String, choices: List<Pair<T, String>>, select: (T) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth().testTag(tag)) {
            Text("$label: $selected", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(open, { open = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            choices.forEachIndexed { index, (value, title) -> DropdownMenuItem(text = { Text(title) },
                onClick = { select(value); open = false }, modifier = Modifier.testTag("${tag}_$index")) }
        }
    }
}

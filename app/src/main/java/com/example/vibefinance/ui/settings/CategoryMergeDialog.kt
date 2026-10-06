package com.example.vibefinance.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.components.ExpressiveConnectedButtonGroup
import com.example.vibefinance.ui.home.getCategoryDisplayName

/** One selection surface, with explicit impact review before any persisted mutation. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CategoryMergeDialog(
    state: FinanceUiState,
    onMerge: (CategoryKind, Set<String>, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var kind by rememberSaveable { mutableStateOf(CategoryKind.EXPENSE) }
    var selectedList by rememberSaveable { mutableStateOf(listOf<String>()) }
    val selected = selectedList.toSet()
    var target by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var choosingTarget by remember { mutableStateOf(false) }
    var confirming by remember { mutableStateOf(false) }
    val choices = remember(kind, state.transactions, state.subscriptions, state.categoryLimits, state.categoryMergeRules) {
        categoryChoices(kind, state.transactions, state.subscriptions, state.categoryLimits, state.categoryMergeRules)
    }
    LaunchedEffect(choices) {
        selectedList = selectedList.filter { it in choices }
        if (target !in choices) target = null
    }
    val recordCounts = remember(kind, state.transactions, state.categoryMergeRules) {
        state.transactions.filter { !it.isBalanceAdjustment && it.toAccountId == null && it.categoryKind == kind }
            .groupingBy { state.categoryMergeRules.resolve(it.category, kind) }.eachCount()
    }
    val recurringCounts = remember(kind, state.subscriptions, state.categoryMergeRules) {
        state.subscriptions.filter { it.categoryKind == kind }
            .groupingBy { state.categoryMergeRules.resolve(it.category, kind) }.eachCount()
    }
    val canMerge = target in choices && target !in selected && selected.isNotEmpty() && selected.all { it in choices }
    val combinedLimit = ((selected + listOfNotNull(target)).sumOf { state.categoryLimits[it] ?: 0.0 })
    val labels = choices.associateWith { getCategoryDisplayName(it) }
    fun displayLabel(category: String): String {
        val label = labels[category] ?: category
        return if (labels.values.count { it == label } > 1) "$label ($category)" else label
    }
    val compact = LocalConfiguration.current.screenWidthDp < 600
    Dialog(onDismissRequest = { if (!state.isMergingCategories) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        BackHandler(state.isMergingCategories) { }
        Surface(modifier = if (compact) Modifier.fillMaxSize() else Modifier.widthIn(max = 560.dp).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(if (compact) 0.dp else 28.dp), color = MaterialTheme.colorScheme.surface) {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text(stringResource(R.string.category_merge_title)) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, enabled = !state.isMergingCategories) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.nav_back))
                            }
                        })
                },
                bottomBar = {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                            Text(stringResource(R.string.category_merge_future_hint), style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            CompletePressButton(shapes = ButtonDefaults.shapes(), onClick = { confirming = true }, enabled = canMerge && !state.isMergingCategories,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("ReviewCategoryMerge")) {
                                Text(stringResource(if (state.isMergingCategories) R.string.category_merge_busy else R.string.category_merge_review))
                            }
                        }
                    }
                }
            ) { padding ->
                LazyColumn(Modifier.padding(padding).fillMaxSize().imePadding(),
                    contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        ExpressiveConnectedButtonGroup(CategoryKind.entries, kind.ordinal, { index ->
                            if (!state.isMergingCategories) {
                                kind = CategoryKind.entries[index]; selectedList = emptyList(); target = null; query = ""
                            }
                        }, compact = true, labelProvider = {
                            stringResource(if (it == CategoryKind.INCOME) R.string.filter_income else R.string.filter_expense)
                        })
                    }
                    item { Text(stringResource(R.string.category_merge_source_hint), style = MaterialTheme.typography.bodyMedium) }
                    item {
                        OutlinedButton(onClick = { choosingTarget = true }, enabled = !state.isMergingCategories,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("ChooseMergeTarget")) {
                            Text(target?.let { displayLabel(it) } ?: stringResource(R.string.category_merge_choose_target),
                                modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    item {
                        OutlinedTextField(query, { query = it }, enabled = !state.isMergingCategories,
                            label = { Text(stringResource(R.string.category_merge_search)) }, singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("CategoryMergeSearch"), shape = RoundedCornerShape(24.dp))
                    }
                    items(choices, key = { it }) { category ->
                        val display = displayLabel(category)
                        if (query.isBlank() || category.contains(query, true) || display.contains(query, true)) {
                            val count = recordCounts[category] ?: 0
                            Surface(shape = RoundedCornerShape(20.dp), color = if (category in selected)
                                MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.fillMaxWidth().testTag("MergeSource:$category").toggleable(value = category in selected, role = Role.Checkbox, enabled = category != target && !state.isMergingCategories) {
                                    selectedList = if (category in selected) selectedList - category else selectedList + category
                                }.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(category in selected, onCheckedChange = null, enabled = category != target && !state.isMergingCategories)
                                    Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                                        Text(display, style = MaterialTheme.typography.titleMedium)
                                        Text(stringResource(R.string.category_merge_category_count, count),
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (choosingTarget) {
            AlertDialog(onDismissRequest = { choosingTarget = false },
                title = { Text(stringResource(R.string.category_merge_choose_target)) },
                text = {
                    LazyColumn(Modifier.heightIn(max = 360.dp)) {
                        items(choices.filter { it !in selected }, key = { it }) { category ->
                            ListItem(headlineContent = { Text(displayLabel(category)) },
                                supportingContent = null,
                                leadingContent = { RadioButton(category == target, onClick = null) },
                                modifier = Modifier.selectable(selected = category == target, role = Role.RadioButton) { target = category; choosingTarget = false }
                                    .testTag("MergeTarget:$category"))
                        }
                    }
                }, confirmButton = { CompletePressTextButton(shapes = ButtonDefaults.shapes(), onClick = { choosingTarget = false }) { Text(stringResource(R.string.btn_cancel)) } })
        }
        if (confirming && canMerge && target != null) {
            val destination = target!!
            AlertDialog(onDismissRequest = { confirming = false },
                title = { Text(stringResource(R.string.category_merge_review)) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val sourceNames = selected.map { displayLabel(it) }
                        Text(sourceNames.joinToString() + " → " + displayLabel(destination))
                        Text(stringResource(R.string.category_merge_impact, selected.sumOf { recordCounts[it] ?: 0 }, selected.sumOf { recurringCounts[it] ?: 0 }))
                        if (kind == CategoryKind.EXPENSE && selected.any { it in state.categoryLimits }) {
                            Text(stringResource(R.string.category_merge_budget_sum, combinedLimit))
                        }
                        Text(stringResource(R.string.category_merge_confirm_hint))
                    }
                },
                confirmButton = {
                    CompletePressTextButton(shapes = ButtonDefaults.shapes(), onClick = {
                        confirming = false
                        onMerge(kind, selected, destination)
                    }, modifier = Modifier.testTag("ConfirmCategoryMerge")) { Text(stringResource(R.string.category_merge_confirm)) }
                },
                dismissButton = { CompletePressTextButton(shapes = ButtonDefaults.shapes(), onClick = { confirming = false }) { Text(stringResource(R.string.btn_cancel)) } }
            )
        }
    }
}

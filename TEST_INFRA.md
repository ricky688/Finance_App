# E2E Test Infra: VibeFinance Android M3 Expressive Optimization

## Test Philosophy
- Opaque-box & requirement-driven verification across Unit Test Suite, Automated Verification Script, and Live Waydroid Emulator (`-s 192.168.240.112:5555`).
- Methodology: Category-Partition, Boundary Value Analysis, Pairwise Combinations, and Real-World Application Scenarios on compact viewport (392.7dp width).

## Feature Inventory & Test Matrix
| # | Feature | Source | Tier 1 (Feature) | Tier 2 (Boundary) | Tier 3 (Cross-Feature) | Tier 4 (Real-World) |
|---|---------|--------|:----------------:|:-----------------:|:----------------------:|:-------------------:|
| 1 | M3 Expressive Shapes & Typography | R1 | 5 | 5 | ✓ | ✓ |
| 2 | Edge-Fading Modifiers & Chip Rows | R1 | 5 | 5 | ✓ | ✓ |
| 3 | Responsive Compact Layouts | R1 | 5 | 5 | ✓ | ✓ |
| 4 | Hero Daily Budget Card & Wavy Progress | R1, R2 | 5 | 5 | ✓ | ✓ |
| 5 | Bento Matrix (MinMax, Rest/Spent, Days) | R1 | 5 | 5 | ✓ | ✓ |
| 6 | Financial Math (Rollover & Overdraft) | R2 | 5 | 5 | ✓ | ✓ |
| 7 | Accounts & Net Worth (Debt vs Asset) | R2 | 5 | 5 | ✓ | ✓ |
| 8 | Recurring Subscriptions & Auto-Charges | R2 | 5 | 5 | ✓ | ✓ |
| 9 | Transaction History & Swipe Actions | R1, R2 | 5 | 5 | ✓ | ✓ |
| 10 | Action Bottom Sheets & Dialogs | R1, R2 | 5 | 5 | ✓ | ✓ |
| 11 | Waydroid Navigation & Interactivity | R3 | 5 | 5 | ✓ | ✓ |

## Test Architecture
- **Unit Test Runner**: `./gradlew testDebugUnitTest` (executes repository, worker, and notification test suites).
- **Compilation Runner**: `./gradlew assembleDebug` (compiles full APK with 0 errors).
- **Index Update Runner**: `graphify update .` (updates codebase graph without errors).
- **Device Test Runner**: `scripts/verify_waydroid.sh` enforcing `-s 192.168.240.112:5555` to install APK, launch `com.example.vibefinance/.MainActivity`, tap across all 4 tabs, open action sheets, and verify UI hierarchy via XML dump.

## Real-World Application Scenarios (Tier 4)
| # | Scenario | Features Exercised | Complexity |
|---|----------|--------------------|------------|
| 1 | Add Cash Expense & Observe Allowance Recalculation | M2, M4, R1, R2 | Medium |
| 2 | Switch Rollover Mode & Trigger Recalculate Sheet | M2, M4, R2 | Medium |
| 3 | Add Credit Card Account & Check Net Worth Update | M3, R2 | Medium |
| 4 | Add Recurring Subscription & Check Hero Daily Impact | M3, R2 | High |
| 5 | Filter Transactions by Active Period & Swipe to Edit | M3, R1, R2 | High |
| 6 | Rapid Bottom Navigation Tab Switching on Waydroid | M1, M2, M3, R3 | Medium |

## Coverage Thresholds
- Unit Tests: 100% pass (22 existing + new component assertions).
- Zero compilation errors on `assembleDebug`.
- Emulator UI: 0 horizontal clippings or text truncations across all screens on Waydroid compact screen.

package com.mokelab.sisyphus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mokelab.sisyphus.feature.exam.ExamRecordListScreen
import com.mokelab.sisyphus.feature.exam.ExamRecordViewModel
import com.mokelab.sisyphus.feature.home.HomeScreen
import com.mokelab.sisyphus.feature.stats.ExamStatsScreen
import com.mokelab.sisyphus.feature.stats.ExamStatsViewModel
import com.mokelab.sisyphus.feature.pomodoro.PomodoroScreen
import com.mokelab.sisyphus.feature.pomodoro.PomodoroViewModel
import com.mokelab.sisyphus.feature.reading.ReadingRecordListScreen
import com.mokelab.sisyphus.feature.reading.ReadingRecordViewModel
import com.mokelab.sisyphus.feature.review.ReviewCardListScreen
import com.mokelab.sisyphus.feature.review.ReviewCardViewModel
import com.mokelab.sisyphus.feature.achievement.AchievementScreen
import com.mokelab.sisyphus.feature.search.SearchScreen
import com.mokelab.sisyphus.feature.settings.SettingsScreen
import com.mokelab.sisyphus.feature.skilltree.SkillTreeScreen
import com.mokelab.sisyphus.feature.sync.SyncLifecycleObserver
import com.mokelab.sisyphus.feature.sync.SyncSettingsScreen
import com.mokelab.sisyphus.feature.sync.SyncViewModel
import com.mokelab.sisyphus.feature.subject.SubjectDetailScreen
import com.mokelab.sisyphus.feature.subject.SubjectDetailViewModel
import com.mokelab.sisyphus.feature.stats.StatsTabScreen
import com.mokelab.sisyphus.feature.stats.LogScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "首页", Icons.Default.Home)
    data object Insights : Screen("insights", "数据", Icons.Default.Favorite)
    data object Pomodoro : Screen("pomodoro_tab", "番茄", Icons.Default.Star)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
    data object ExamStats : Screen("exam_stats", "考试统计", Icons.Default.Favorite)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Insights,
    Screen.Pomodoro,
    Screen.Settings,
)

@Composable
fun SisyphusApp() {
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val syncLifecycleObserver: SyncLifecycleObserver = koinInject()

    // 注册同步生命周期观察者
    DisposableEffect(lifecycleOwner, syncLifecycleObserver) {
        lifecycleOwner.lifecycle.addObserver(syncLifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(syncLifecycleObserver)
        }
    }

    Scaffold(
        bottomBar = {
            // 悬浮底部导航栏
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Box {
                    NavigationBar(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        modifier = Modifier.height(64.dp)
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        bottomNavItems.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }

                    // 加号按钮，在番茄钟上方
                    FloatingActionButton(
                        onClick = { /* TODO: 弹出数据录入界面 */ },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 0.dp)
                            .size(48.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加记录",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToPomodoro = { navController.navigate("pomodoro") },
                    onNavigateToSubject = { subjectId ->
                        navController.navigate("subject/$subjectId")
                    }
                )
            }
            composable(Screen.Insights.route) {
                StatsTabScreen(
                    onNavigateToExamStats = { navController.navigate(Screen.ExamStats.route) },
                    onNavigateToLog = { navController.navigate("log") },
                    onNavigateToSkillTree = { navController.navigate("skilltree") }
                )
            }
            composable(Screen.Pomodoro.route) {
                val viewModel: PomodoroViewModel = koinViewModel()
                PomodoroScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { navController.navigate("log") }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSync = { navController.navigate("sync") },
                    onNavigateToAchievement = { navController.navigate("achievement") },
                    onNavigateToAbout = { /* TODO: Navigate to about screen */ }
                )
            }

            composable("sync") {
                val viewModel: SyncViewModel = koinViewModel()
                SyncSettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("pomodoro") {
                val viewModel: PomodoroViewModel = koinViewModel()
                PomodoroScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { navController.navigate("log") }
                )
            }
            composable("subject/{subjectId}") { backStackEntry ->
                val viewModel: SubjectDetailViewModel = koinViewModel()
                SubjectDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("review") {
                val viewModel: ReviewCardViewModel = koinViewModel()
                ReviewCardListScreen(viewModel = viewModel)
            }
            composable("exam") {
                val viewModel: ExamRecordViewModel = koinViewModel()
                ExamRecordListScreen(
                    viewModel = viewModel,
                    onNavigateToStats = { navController.navigate("exam_stats") }
                )
            }
            composable(Screen.ExamStats.route) {
                val viewModel: ExamStatsViewModel = koinViewModel()
                ExamStatsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("log") {
                LogScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("reading") {
                val viewModel: ReadingRecordViewModel = koinViewModel()
                ReadingRecordListScreen(viewModel = viewModel)
            }
            composable("achievement") {
                AchievementScreen(navController = navController)
            }
            composable("skilltree") {
                SkillTreeScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

package com.mokelab.sisyphus.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mokelab.sisyphus.feature.exam.ExamRecordListScreen
import com.mokelab.sisyphus.feature.exam.ExamRecordViewModel
import com.mokelab.sisyphus.feature.home.HomeScreen
import com.mokelab.sisyphus.feature.home.HomeViewModel
import com.mokelab.sisyphus.feature.pomodoro.PomodoroScreen
import com.mokelab.sisyphus.feature.pomodoro.PomodoroViewModel
import com.mokelab.sisyphus.feature.reading.ReadingRecordListScreen
import com.mokelab.sisyphus.feature.reading.ReadingRecordViewModel
import com.mokelab.sisyphus.feature.review.ReviewCardListScreen
import com.mokelab.sisyphus.feature.review.ReviewCardViewModel
import com.mokelab.sisyphus.feature.search.SearchScreen
import com.mokelab.sisyphus.feature.settings.SettingsScreen
import com.mokelab.sisyphus.feature.subject.SubjectScreen
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "首页", Icons.Default.Home)
    data object Subject : Screen("subject", "学科", Icons.AutoMirrored.Filled.List)
    data object Search : Screen("search", "搜索", Icons.Default.Search)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Subject,
    Screen.Search,
    Screen.Settings,
)

@Composable
fun SisyphusApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val viewModel: HomeViewModel = koinViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPomodoro = { navController.navigate("pomodoro") },
                    onSubjectClick = { subjectId ->
                        navController.navigate("subject/$subjectId")
                    }
                )
            }
            composable(Screen.Subject.route) {
                SubjectScreen()
            }
            composable(Screen.Search.route) {
                SearchScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable("pomodoro") {
                val viewModel: PomodoroViewModel = koinViewModel()
                PomodoroScreen(viewModel = viewModel)
            }
            composable("subject/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId")?.toLongOrNull() ?: 0L
                SubjectScreen()
            }
            composable("review") {
                val viewModel: ReviewCardViewModel = koinViewModel()
                ReviewCardListScreen(viewModel = viewModel)
            }
            composable("exam") {
                val viewModel: ExamRecordViewModel = koinViewModel()
                ExamRecordListScreen(viewModel = viewModel)
            }
            composable("reading") {
                val viewModel: ReadingRecordViewModel = koinViewModel()
                ReadingRecordListScreen(viewModel = viewModel)
            }
        }
    }
}

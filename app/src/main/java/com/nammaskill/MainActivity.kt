package com.nammaskill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nammaskill.ui.navigation.BottomNavigationBar
import com.nammaskill.ui.navigation.NavGraph
import com.nammaskill.ui.navigation.Screen
import com.nammaskill.ui.theme.NammaskillsTheme
import com.nammaskill.ui.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NammaskillsTheme() {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                
                // Get AuthViewModel to check user role and pass it to NavGraph
                val authViewModel: AuthViewModel = hiltViewModel()
                val userRole by authViewModel.userRole.collectAsState()
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Whitelist approach: Only show the bottom bar for main User screens
                val showBottomBarRoutes = listOf(
                    Screen.AllSkillCenters.route,
                    Screen.CentreMap.route,
                    Screen.SuccessStories.route
                )

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        // Only show Bottom Bar if the logged-in user is a regular 'user'
                        // and we are on one of the primary navigation screens.
                        if (userRole == "user" && currentRoute in showBottomBarRoutes) {
                            BottomNavigationBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        onShowSnackbar = { message ->
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

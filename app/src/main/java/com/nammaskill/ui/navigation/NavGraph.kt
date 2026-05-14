package com.nammaskill.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nammaskill.ui.screens.*
import com.nammaskill.ui.viewmodels.AuthViewModel
import com.nammaskill.ui.viewmodels.CourseDetailViewModel
import com.nammaskill.ui.viewmodels.CourseViewModel
import com.nammaskill.ui.viewmodels.EnrollmentViewModel
import com.nammaskill.ui.viewmodels.GuidanceViewModel
import com.nammaskill.ui.viewmodels.SuccessStoryViewModel
import com.nammaskill.ui.viewmodels.NotificationViewModel

import androidx.navigation.navDeepLink

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel, // Shared state from MainActivity
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val courseViewModel: CourseViewModel = hiltViewModel()
    val enrollmentViewModel: EnrollmentViewModel = hiltViewModel()
    val successStoryViewModel: SuccessStoryViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.AuthChoice.route,
        modifier = modifier
    ) {
        // --- AUTH ROUTES ---
        composable(Screen.AuthChoice.route) {
            AuthChoiceScreen(
                onNavigateToUserLogin = { navController.navigate(Screen.UserLogin.route) },
                onNavigateToAdminLogin = { navController.navigate(Screen.AdminLogin.route) },
                onNavigateToUserSignup = { navController.navigate(Screen.UserSignup.route) },
                onNavigateToAdminSignup = { navController.navigate(Screen.AdminSignup.route) }
            )
        }
        composable(Screen.UserLogin.route) {
            UserLoginScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.AllSkillCenters.route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AdminLogin.route) {
            AdminLoginScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.UserSignup.route) {
            UserSignupScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.AllSkillCenters.route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AdminSignup.route) {
            AdminSignupScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                }
            )
        }

        // --- USER FLOW ---
        composable(Screen.AllSkillCenters.route) {
            AllSkillCentersScreen(
                viewModel = enrollmentViewModel,
                authViewModel = authViewModel,
                notificationViewModel = notificationViewModel,
                onCenterClick = { adminId ->
                    navController.navigate(Screen.SkillCenterCourses.createRoute(adminId))
                },
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onLogout = {
                    navController.navigate(Screen.AuthChoice.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.SkillCenterCourses.route,
            arguments = listOf(navArgument("adminId") { type = NavType.StringType })
        ) { backStackEntry ->
            val adminId = backStackEntry.arguments?.getString("adminId") ?: ""
            SkillCenterCoursesScreen(
                adminId = adminId,
                courseViewModel = courseViewModel,
                enrollmentViewModel = enrollmentViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // --- ADMIN FLOW ---
        composable(Screen.AdminDashboard.route) {
            AdminSkillCenterDashboardScreen(
                authViewModel = authViewModel,
                courseViewModel = courseViewModel,
                enrollmentViewModel = enrollmentViewModel,
                onAddCourseClick = { navController.navigate(Screen.AddCourse.route) },
                onAddSuccessStoryClick = { navController.navigate(Screen.AddSuccessStory.route) },
                onViewStudents = { courseId ->
                    navController.navigate(Screen.EnrolledStudents.createRoute(courseId))
                },
                onLogout = {
                    navController.navigate(Screen.AuthChoice.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AddCourse.route) {
            AddCourseScreen(
                viewModel = courseViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddSuccessStory.route) {
            AddSuccessStoryScreen(
                viewModel = successStoryViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EnrolledStudents.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            EnrolledStudentsScreen(
                courseId = courseId,
                viewModel = enrollmentViewModel,
                onStudentClick = { student ->
                    enrollmentViewModel.setSelectedStudent(student)
                    navController.navigate(Screen.StudentDetail.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.StudentDetail.route) {
            val student by enrollmentViewModel.selectedStudent.collectAsState()
            student?.let {
                StudentDetailScreen(it, onBack = { navController.popBackStack() })
            }
        }

        // --- USER TABS (Hidden for Admin in Bottom Bar) ---
        composable(Screen.CentreMap.route) { MapScreen() }
        composable(Screen.SuccessStories.route) { 
            SuccessStoriesScreen(viewModel = successStoryViewModel) 
        }
        
        // --- OTHER ROUTES ---
        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "namma-skill://course-detail/{courseId}" })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val detailViewModel: CourseDetailViewModel = hiltViewModel()
            CourseDetailScreen(
                courseId = courseId,
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onShowSnackbar = onShowSnackbar
            )
        }
        composable(Screen.Guidance.route) {
            val guidanceViewModel: GuidanceViewModel = hiltViewModel()
            GuidanceScreen(viewModel = guidanceViewModel)
        }
        composable(Screen.Notifications.route) {
            NotificationScreen(
                viewModel = notificationViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

package com.nammaskill.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object AuthChoice : Screen("auth_choice", "Namma Skill")
    object UserLogin : Screen("user_login", "User Login")
    object AdminLogin : Screen("admin_login", "Admin Login")
    object UserSignup : Screen("user_signup", "User Signup")
    object AdminSignup : Screen("admin_signup", "Admin Signup")
    
    // Main User Screens (Show in Bottom Bar)
    object AllSkillCenters : Screen("all_skill_centers", "Find Courses")
    object CentreMap : Screen("centre_map", "Centre Map")
    object SuccessStories : Screen("success_stories", "Success Stories")

    // Detail/Flow Screens
    object SkillCenterCourses : Screen("skill_center_courses/{adminId}", "Center Courses") {
        fun createRoute(adminId: String) = "skill_center_courses/$adminId"
    }
    object AdminDashboard : Screen("admin_dashboard", "Admin Dashboard")
    object AddCourse : Screen("add_course", "Add New Course")
    object AddSuccessStory : Screen("add_success_story", "Add Success Story")
    object EnrolledStudents : Screen("enrolled_students/{courseId}", "Enrolled Students") {
        fun createRoute(courseId: String) = "enrolled_students/$courseId"
    }
    object StudentDetail : Screen("student_detail", "Student Detail")

    // Other screens
    object Guidance : Screen("guidance", "AI Guidance")
    object CourseDetail : Screen("course_detail/{courseId}", "Course Detail") {
        fun createRoute(courseId: String) = "course_detail/$courseId"
    }
    object Notifications : Screen("notifications", "Notifications")
}

package com.nammaskill.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.unit.dp
import com.nammaskill.data.util.Resource
import com.nammaskill.domain.model.Course
import com.nammaskill.ui.viewmodels.CourseViewModel

import com.nammaskill.ui.components.ShimmerLoadingEffect

import androidx.compose.ui.tooling.preview.Preview
import com.nammaskill.ui.theme.NammaskillsTheme

@Composable
fun CourseListContent(
    coursesState: Resource<List<Course>>,
    selectedTrade: String,
    trades: List<String>,
    onTradeSelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedDuration: String,
    durations: List<String>,
    onDurationSelected: (String) -> Unit,
    onCourseClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search courses...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Text(
            text = "Filter by Trade",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        ScrollableFilters(
            items = trades,
            selectedItem = selectedTrade,
            onItemSelected = onTradeSelected
        )

        Text(
            text = "Filter by Duration",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        ScrollableFilters(
            items = durations,
            selectedItem = selectedDuration,
            onItemSelected = onDurationSelected
        )

        when (coursesState) {
            is Resource.Loading -> {
                ShimmerLoadingEffect()
            }
            is Resource.Success -> {
                val courses = coursesState.data ?: emptyList()
                if (courses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No courses found matching your criteria.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(courses) { course ->
                            CourseItem(
                                course = course,
                                onClick = { onCourseClick(course.id) }
                            )
                        }
                    }
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = coursesState.message ?: "An error occurred")
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun CourseListPreview() {
    val sampleCourses = listOf(
        Course("1", "Electrician Basic", "Electrician", "2024-06-01", "6 Months", "10th Pass", true),
        Course("2", "Advanced Welding", "Welding", "2024-07-15", "3 Months", "8th Pass", false)
    )
    NammaskillsTheme() {
        CourseListContent(
            coursesState = Resource.Success(sampleCourses),
            selectedTrade = "All",
            trades = listOf("All", "Electrician", "Sewing", "Coding", "Welding"),
            onTradeSelected = {},
            searchQuery = "",
            onSearchQueryChanged = {},
            selectedDuration = "All",
            durations = listOf("All", "3 Months", "6 Months"),
            onDurationSelected = {},
            onCourseClick = {}
        )
    }
}

@Composable
fun CourseListScreen(
    viewModel: CourseViewModel,
    onCourseClick: (String) -> Unit
) {
    val coursesState by viewModel.coursesState.collectAsState()
    val selectedTrade by viewModel.selectedTrade.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDuration by viewModel.selectedDuration.collectAsState()

    CourseListContent(
        coursesState = coursesState,
        selectedTrade = selectedTrade,
        trades = viewModel.trades,
        onTradeSelected = { viewModel.onTradeSelected(it) },
        searchQuery = searchQuery,
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
        selectedDuration = selectedDuration,
        durations = viewModel.durations,
        onDurationSelected = { viewModel.onDurationSelected(it) },
        onCourseClick = onCourseClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollableFilters(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            FilterChip(
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
                label = { Text(item) }
            )
        }
    }
}

@Composable
fun CourseItem(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Trade: ${course.trade}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Duration: ${course.duration}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Eligibility: ${course.eligibility}",
                style = MaterialTheme.typography.bodySmall
            )
            if (course.hasJobGuarantee) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFE8F5E9), // Light Green background
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓ Verified Job Guarantee",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32), // Dark Green text
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

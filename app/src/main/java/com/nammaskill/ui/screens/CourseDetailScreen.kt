package com.nammaskill.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nammaskill.data.util.Resource
import com.nammaskill.domain.model.Course
import com.nammaskill.ui.screens.CourseDetailContent
import com.nammaskill.ui.viewmodels.CourseDetailViewModel
import com.nammaskill.util.InputValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CourseDetailViewModel,
    onBack: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val courseState by viewModel.course.collectAsState()
    var showApplySheet by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        viewModel.loadCourse(courseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (courseState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(
                    text = courseState.message ?: "Unknown Error",
                    modifier = Modifier.align(Alignment.Center)
                )
                is Resource.Success -> {
                    val course = courseState.data!!
                    CourseDetailContent(
                        course = course,
                        onApplyClick = { showApplySheet = true }
                    )
                }
            }
        }
    }

    if (showApplySheet) {
        ApplyBottomSheet(
            onDismiss = { showApplySheet = false },
            onSubmit = { name, phone ->
                viewModel.applyForCourse(name, phone)
                showApplySheet = false
                onShowSnackbar("Application submitted successfully for ${courseState.data?.title}")
            }
        )
    }
}

@Composable
fun CourseDetailContent(course: Course, onApplyClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = course.title, style = MaterialTheme.typography.headlineMedium)
        Text(text = "Trade: ${course.trade}", style = MaterialTheme.typography.titleLarge)
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailItem(label = "Duration", value = course.duration)
                DetailItem(label = "Eligibility", value = course.eligibility)
                DetailItem(label = "Start Date", value = course.startDate)
                if (course.hasJobGuarantee) {
                    Text(
                        text = "✓ Job Guarantee Included",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onApplyClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Apply Now")
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label: ", style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    val isButtonEnabled = InputValidator.isValidName(name) && InputValidator.isValidPhoneNumber(phone)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Apply for Course", style = MaterialTheme.typography.headlineSmall)
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = phone,
                onValueChange = { input -> 
                    if (input.all { it.isDigit() } && input.length <= 10) {
                        phone = input 
                    }
                },
                label = { Text("Phone Number") },
                placeholder = { Text("10-digit mobile number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                isError = phone.isNotEmpty() && !InputValidator.isValidPhoneNumber(phone)
            )
            if (phone.isNotEmpty() && !InputValidator.isValidPhoneNumber(phone)) {
                Text(
                    text = "Please enter exactly 10 digits",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Button(
                onClick = { onSubmit(name, phone) },
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Text("Submit Application")
            }
        }
    }
}

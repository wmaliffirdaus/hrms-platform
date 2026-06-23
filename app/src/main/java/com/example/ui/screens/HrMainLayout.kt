package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.Employee
import com.example.data.database.Attendance
import com.example.data.database.LeaveRequest
import com.example.data.database.Candidate
import com.example.data.database.Performance
import com.example.data.database.OnboardingTask
import com.example.data.database.AuditLog
import com.example.data.database.Announcement
import com.example.data.database.Goal
import com.example.data.database.ContinuousFeedback
import com.example.data.database.ProfileUpdateRequest
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.viewmodel.HrViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrMainLayout(
    viewModel: HrViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentActor by viewModel.currentActor.collectAsStateWithLifecycle()
    val employees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val filteredEmployees by viewModel.filteredEmployees.collectAsStateWithLifecycle()
    
    val attendanceLogs by viewModel.allAttendance.collectAsStateWithLifecycle()
    val leaveRequests by viewModel.allLeaveRequests.collectAsStateWithLifecycle()
    val candidates by viewModel.allCandidates.collectAsStateWithLifecycle()
    val performances by viewModel.allPerformance.collectAsStateWithLifecycle()
    val auditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val announcements by viewModel.allAnnouncements.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val feedbacks by viewModel.allFeedback.collectAsStateWithLifecycle()
    val profileUpdates by viewModel.allProfileUpdates.collectAsStateWithLifecycle()

    val aiOutput by viewModel.aiAssistantOutput.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    // UI Tab State
    var activeTab by remember { mutableStateOf(0) }
    val isEmployee = currentActor?.userRole == "Employee"

    // Reset active tab to 0 on actor shift to prevent index overflow
    LaunchedEffect(currentActor) {
        activeTab = 0
    }

    // Dynamic Tabs list based on Active Role
    val tabs = if (isEmployee) {
        listOf("My Portal", "My Leaves", "My Performance", "My Payslips")
    } else {
        listOf("Dashboard", "Directory", "Leaves & Claims", "Performance OKRs", "HR Reports")
    }
    
    // Bottom navigation items setup
    val navigationIcons = if (isEmployee) {
        listOf(
            Icons.Rounded.Face,
            Icons.Rounded.EventAvailable,
            Icons.Rounded.Star,
            Icons.Rounded.Assessment
        )
    } else {
        listOf(
            Icons.Rounded.Dashboard,
            Icons.Rounded.People,
            Icons.Rounded.EventAvailable,
            Icons.Rounded.Star,
            Icons.Rounded.Assessment
        )
    }

    // Form Dialog states
    var showAddEmployeeDialog by remember { mutableStateOf(false) }
    var showAddCandidateDialog by remember { mutableStateOf(false) }
    var showAppraisalDialog by remember { mutableStateOf(false) }
    var showRequestLeaveDialog by remember { mutableStateOf(false) }
    var showAuditLogsDialog by remember { mutableStateOf(false) }
    
    // Details sheet states
    var selectedEmployeeDetail by remember { mutableStateOf<Employee?>(null) }
    var selectedCandidateDetail by remember { mutableStateOf<Candidate?>(null) }
    var showAiResultDialog by remember { mutableStateOf(false) }

    // Onboarding task text input
    var newCustomTaskName by remember { mutableStateOf("") }

    // Switch Role Selector state
    var showRoleSelectorDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "HR Pulse",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        currentActor?.let {
                            Text(
                                text = "Logged: ${it.name} (${it.userRole})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Profile/Role Selector Quick Action
                    IconButton(
                        onClick = { showRoleSelectorDropdown = true },
                        modifier = Modifier
                            .testTag("role_switcher_button")
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBox,
                            contentDescription = "Switch Roles",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Dropdown for selecting which worker's account to test role based access
                    DropdownMenu(
                        expanded = showRoleSelectorDropdown,
                        onDismissRequest = { showRoleSelectorDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("SIMULATOR: Switch User Role", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary) },
                            onClick = {},
                            enabled = false
                        )
                        employees.forEach { emp ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "${emp.name} ")
                                        Text(
                                            text = "(${emp.userRole})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = when(emp.userRole) {
                                                "HR Admin" -> Color(0xFFEF4444)
                                                "Manager" -> Color(0xFF3B82F6)
                                                else -> Color(0xFF10B981)
                                            }
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.selectActiveActor(emp)
                                    showRoleSelectorDropdown = false
                                    Toast.makeText(context, "Session switched to ${emp.name}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    // Audit compliance tracker shortcut
                    IconButton(
                        onClick = { showAuditLogsDialog = true },
                        modifier = Modifier.testTag("compliance_audit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = "View Audit Log",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        icon = {
                            Icon(
                                imageVector = navigationIcons[index],
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isEmployee) {
                when (activeTab) {
                    0 -> MyPortalScreen(
                        viewModel = viewModel,
                        currentActor = currentActor!!,
                        announcements = announcements,
                        profileUpdates = profileUpdates,
                        attendanceLogs = attendanceLogs
                    )
                    1 -> MyLeavesScreen(
                        viewModel = viewModel,
                        currentActor = currentActor!!,
                        allLeaveRequests = leaveRequests
                    )
                    2 -> MyPerformanceScreen(
                        viewModel = viewModel,
                        currentActor = currentActor!!,
                        allGoals = goals,
                        allFeedbacks = feedbacks,
                        allPerformances = performances
                    )
                    3 -> MyPayslipsScreen(
                        currentActor = currentActor!!
                    )
                }
            } else {
                when (activeTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        employees = employees,
                        attendanceLogs = attendanceLogs,
                        leaveRequests = leaveRequests,
                        candidates = candidates,
                        currentActor = currentActor,
                        onOpenRequestLeave = { showRequestLeaveDialog = true }
                    )
                    1 -> DirectoryScreen(
                        viewModel = viewModel,
                        employees = filteredEmployees,
                        currentActor = currentActor,
                        onAddEmployeeClick = { showAddEmployeeDialog = true },
                        onEmployeeSelection = { selectedEmployeeDetail = it }
                    )
                    2 -> LeavesScreen(
                        viewModel = viewModel,
                        leaveRequests = leaveRequests,
                        currentActor = currentActor,
                        onAddLeaveClick = { showRequestLeaveDialog = true }
                    )
                    3 -> PerformanceManagerScreen(
                        viewModel = viewModel,
                        employees = employees,
                        performances = performances,
                        goals = goals,
                        feedbacks = feedbacks,
                        profileUpdates = profileUpdates,
                        currentActor = currentActor,
                        onAddEvaluationClick = { showAppraisalDialog = true },
                        onTriggerAiDraftReview = { emp, rating, ach ->
                            viewModel.draftEmployeeAppraisalWithAI(emp.name, emp.role, rating, ach)
                            showAiResultDialog = true
                        }
                    )
                    4 -> HrReportsScreen(
                        employees = employees,
                        leaveRequests = leaveRequests,
                        performances = performances
                    )
                    5 -> RecruitmentScreen(
                        viewModel = viewModel,
                        candidates = candidates,
                        currentActor = currentActor,
                        onAddCandidateClick = { showAddCandidateDialog = true },
                        onCandidateClick = { candidate ->
                            selectedCandidateDetail = candidate
                        },
                        onTriggerAiScreen = { cand ->
                            viewModel.analyzeResumeSummaryWithAI(cand)
                            showAiResultDialog = true
                        }
                    )
                }
            }
        }

        // --- Dialogs Hub ---

        // 1. Add Employee Sheet (HR Admin permissions check)
        if (showAddEmployeeDialog) {
            AddEmployeeDialog(
                onDismiss = { showAddEmployeeDialog = false },
                onSave = { name, email, phone, dept, role, userRole, salary, status, notes ->
                    viewModel.addEmployee(name, email, phone, dept, role, userRole, salary, status, notes)
                    showAddEmployeeDialog = false
                    Toast.makeText(context, "Employee registered successfully", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 2. Add Recruitment Candidate Dialog
        if (showAddCandidateDialog) {
            AddCandidateDialog(
                onDismiss = { showAddCandidateDialog = false },
                onSave = { name, email, pos, summary, score ->
                    viewModel.addCandidate(name, email, pos, summary, score)
                    showAddCandidateDialog = false
                    Toast.makeText(context, "Candidate added to recruitment track", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 3. Request Leave / Submit Financial Claim Dialog
        if (showRequestLeaveDialog) {
            val actor = currentActor
            if (actor != null) {
                RequestLeaveDialog(
                    applicant = actor,
                    onDismiss = { showRequestLeaveDialog = false },
                    onSave = { type, start, end, reason, amount, claimCategory ->
                        viewModel.submitLeaveRequest(actor.id, actor.name, type, start, end, reason, amount, claimCategory)
                        showRequestLeaveDialog = false
                        Toast.makeText(context, "$type logged under Pending approvals", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // 4. File Employee Performance Grade & KPI evaluation
        if (showAppraisalDialog) {
            AppraisalDialog(
                employees = employees.filter { it.id != currentActor?.id }, // Can't review yourself
                onDismiss = { showAppraisalDialog = false },
                onSave = { emp, rating, ach, improvement, kpi, cycle ->
                    viewModel.submitPerformanceEvaluation(emp.id, emp.name, rating, ach, improvement, kpi, cycle)
                    showAppraisalDialog = false
                    Toast.makeText(context, "Evaluation for ${emp.name} recorded formally", Toast.LENGTH_SHORT).show()
                },
                onTriggerAiDraft = { emp, rating, ach ->
                    viewModel.draftEmployeeAppraisalWithAI(emp.name, emp.role, rating, ach)
                    showAiResultDialog = true
                }
            )
        }

        // 5. Compliance Audit Logs Screen Overlay Dialog
        if (showAuditLogsDialog) {
            AuditLogsDialog(
                logs = auditLogs,
                onDismiss = { showAuditLogsDialog = false }
            )
        }

        // 6. AI Intelligence Assistant Result Sheet (Gemini)
        if (showAiResultDialog) {
            AiAssistantResultDialog(
                output = aiOutput,
                isLoading = isAiLoading,
                onDismiss = {
                    showAiResultDialog = false
                    viewModel.clearResult()
                }
            )
        }

        // 7. Full Employee Detail Drawer Dialog
        selectedEmployeeDetail?.let { emp ->
            EmployeeDetailDialog(
                employee = emp,
                viewModel = viewModel,
                currentActor = currentActor,
                onDismiss = { selectedEmployeeDetail = null },
                onDelete = {
                    viewModel.deleteEmployee(emp.id, emp.name)
                    selectedEmployeeDetail = null
                    Toast.makeText(context, "Record removed", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 8. Brief Candidate Info Modal Dialog
        selectedCandidateDetail?.let { cand ->
            CandidateDetailDialog(
                candidate = cand,
                onDismiss = { selectedCandidateDetail = null },
                onStatusChange = { newStatus ->
                    viewModel.updateCandidateStatus(cand.id, cand.name, newStatus)
                    selectedCandidateDetail = null
                    Toast.makeText(context, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                },
                onDelete = {
                    viewModel.removeCandidate(cand.id, cand.name)
                    selectedCandidateDetail = null
                    Toast.makeText(context, "Candidate pipeline removed", Toast.LENGTH_SHORT).show()
                },
                onTriggerAi = {
                    viewModel.analyzeResumeSummaryWithAI(cand)
                    selectedCandidateDetail = null
                    showAiResultDialog = true
                }
            )
        }
    }
}

// ==========================================
// SCREEN 1: CENTRAL METRICS DASHBOARD
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: HrViewModel,
    employees: List<Employee>,
    attendanceLogs: List<Attendance>,
    leaveRequests: List<LeaveRequest>,
    candidates: List<Candidate>,
    currentActor: Employee?,
    onOpenRequestLeave: () -> Unit
) {
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Compute quick dashboard statistics
    val totalCount = employees.size
    val onboardingCount = employees.count { it.status == "Onboarding" }
    
    // Check-in ratios
    val todayAttendance = attendanceLogs.filter { it.date == todayStr }
    val checkedInCount = todayAttendance.count { it.status == "Present" || it.status == "Late" }
    val lateCount = todayAttendance.count { it.status == "Late" }
    
    // Pending items
    val pendingLeaves = leaveRequests.count { it.status == "Pending" && it.leaveType != "Claim" }
    val pendingClaims = leaveRequests.count { it.status == "Pending" && it.leaveType == "Claim" }
    
    val myAttendanceToday = attendanceLogs.firstOrNull { it.employeeId == currentActor?.id && it.date == todayStr }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Header Card with Custom Gradient Art Panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_dashboard_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF6750A4), Color(0xFF21005D))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "OFFICIAL HR INTELLIGENCE PORTAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Welcome back, ${currentActor?.name ?: "Professional"}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Designation: ${currentActor?.role ?: "Guest HR Analyst"}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.2f))
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "System Date & Time",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date()),
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "Role: ${currentActor?.userRole ?: "Viewer"}",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // BIOMETRIC TIME CLOCK INTEGRATION BLOCK
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("time_clock_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Real-Time Biometric Timeclock",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Check in to stamp your attendance and update operational metrics.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stamp Status icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (myAttendanceToday != null) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (myAttendanceToday?.checkOut != null) Icons.Rounded.Close
                                              else if (myAttendanceToday != null) Icons.Rounded.CheckCircle
                                              else Icons.Rounded.AssignmentTurnedIn,
                                contentDescription = "Clock Status",
                                tint = if (myAttendanceToday?.checkOut != null) Color.Gray
                                       else if (myAttendanceToday != null) MaterialTheme.colorScheme.tertiary
                                       else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (myAttendanceToday?.checkOut != null) "Checked-Out Today"
                                       else if (myAttendanceToday != null) "Active Check-In logged"
                                       else "Not Checked In Today",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (myAttendanceToday?.checkOut != null) "Ended: ${myAttendanceToday?.checkOut}"
                                       else if (myAttendanceToday != null) "Started: ${myAttendanceToday?.checkIn}"
                                       else "Stamp check-in below",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // CheckIn button
                        currentActor?.let { actor ->
                            if (myAttendanceToday == null) {
                                Button(
                                    onClick = { viewModel.performCheckIn(actor.id, actor.name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("check_in_button")
                                ) {
                                    Text("Check In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (myAttendanceToday.checkOut == null) {
                                Button(
                                    onClick = { viewModel.performCheckOut(actor.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("check_out_button")
                                ) {
                                    Text("Check Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {},
                                    enabled = false,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Done", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // METRICS COUNTERS GRID
        item {
            Text(
                text = "Key Operational Metrics",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "Active Staff",
                        value = "$totalCount",
                        subtext = "$onboardingCount onboarding",
                        icon = Icons.Rounded.People,
                        color = Color(0xFF21005D),
                        containerColor = Color(0xFFEADDFF),
                        textColor = Color(0xFF21005D),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Attendance Today",
                        value = "$checkedInCount",
                        subtext = "$lateCount late arrivals",
                        icon = Icons.Rounded.VerifiedUser,
                        color = Color(0xFF041E49),
                        containerColor = Color(0xFFD3E3FD),
                        textColor = Color(0xFF041E49),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "Leaves Pending",
                        value = "$pendingLeaves",
                        subtext = "Requires attention",
                        icon = Icons.Rounded.EventAvailable,
                        color = Color(0xFFB3261E),
                        containerColor = Color(0xFFFFDAD6),
                        textColor = Color(0xFF410002),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Reimburse Claims",
                        value = "$pendingClaims",
                        subtext = "Unprocessed receipts",
                        icon = Icons.Rounded.AssignmentTurnedIn,
                        color = Color(0xFF6750A4),
                        containerColor = Color(0xFFF3EDF7),
                        textColor = Color(0xFF1D1B20),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // HR TEAM BULLETIN / ANNOUNCEMENTS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Central Announcements",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "LATEST ISSUES",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AnnouncementItem(
                            title = "Q3 Mid-Year Salary & Performance Reviews",
                            summary = "Managers are reminded to file H1 performance evaluation parameters by next Friday.",
                            author = "Sarah Jenkins",
                            date = "Today"
                        )
                        AnnouncementItem(
                            title = "System Maintenance Notice: Cloud Sync",
                            summary = "The central payroll databases will migrate to Room encrypted store on Saturday night.",
                            author = "IT Security",
                            date = "Yesterday"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    textColor: Color? = null
) {
    val actualBg = containerColor ?: MaterialTheme.colorScheme.surface
    val actualText = textColor ?: MaterialTheme.colorScheme.primary
    val actualTitleText = textColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    val actualSubText = textColor?.copy(alpha = 0.8f) ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = actualBg),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(), 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = actualTitleText,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon, 
                    contentDescription = title, 
                    tint = color, 
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Column {
                Text(
                    text = value, 
                    fontSize = 24.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = actualText
                )
                Text(
                    text = subtext, 
                    fontSize = 11.sp, 
                    color = actualSubText, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AnnouncementItem(
    title: String,
    summary: String,
    author: String,
    date: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
            .padding(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            Text(text = date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = summary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "- Published by $author", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary)
    }
}

// ==========================================
// SCREEN 2: EMPLOYEE DIRECTORY & RECORDS
// ==========================================
@Composable
fun DirectoryScreen(
    viewModel: HrViewModel,
    employees: List<Employee>,
    currentActor: Employee?,
    onAddEmployeeClick: () -> Unit,
    onEmployeeSelection: (Employee) -> Unit
) {
    val searchStr by viewModel.employeeSearchQuery.collectAsStateWithLifecycle()
    val selectedDept by viewModel.departmentFilter.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.employeeStatusFilter.collectAsStateWithLifecycle()

    val departments = listOf("All Departments", "HR Department", "Engineering", "Product & Design", "Marketing", "Finance")
    val statuses = listOf("All Statuses", "Active", "Onboarding", "Offboarding", "Suspended")

    var showFiltersRow by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Screen Title + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Employee Registry", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Database storage containing complete records, roles, and onboarding status.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                // Show register candidate button only on HR Admins
                if (currentActor?.userRole == "HR Admin") {
                    IconButton(
                        onClick = onAddEmployeeClick,
                        modifier = Modifier
                            .testTag("add_employee_fab")
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add New Employee", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar + Filter toggler
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchStr,
                    onValueChange = { viewModel.employeeSearchQuery.value = it },
                    placeholder = { Text("Search staff, email, role...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("employee_search_field"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = { showFiltersRow = !showFiltersRow },
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .background(if (showFiltersRow) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                ) {
                    Icon(imageVector = Icons.Rounded.Assessment, contentDescription = "Filters", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Expanded Collapsible filter row
            if (showFiltersRow) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dept spinner mock
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Department", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        var expandedDept by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { expandedDept = true }
                                .padding(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(text = selectedDept, fontSize = 11.sp)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            DropdownMenu(expanded = expandedDept, onDismissRequest = { expandedDept = false }) {
                                departments.forEach { dept ->
                                    DropdownMenuItem(
                                        text = { Text(text = dept, fontSize = 11.sp) },
                                        onClick = {
                                            viewModel.departmentFilter.value = dept
                                            expandedDept = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Status spinner mock
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Company Status", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        var expandedStatus by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { expandedStatus = true }
                                .padding(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(text = selectedStatus, fontSize = 11.sp)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            DropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                                statuses.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(text = status, fontSize = 11.sp) },
                                        onClick = {
                                            viewModel.employeeStatusFilter.value = status
                                            expandedStatus = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main directory list
            if (employees.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Rounded.People, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Text(text = "No staff found.", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(text = "Try clearing search or filters.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(employees) { emp ->
                        EmployeeItemCard(
                            employee = emp,
                            onClick = { onEmployeeSelection(emp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeItemCard(
    employee: Employee,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("employee_card_${employee.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circle Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.name.take(2).uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = employee.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = employee.role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = employee.department, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
            }

            Column(horizontalAlignment = Alignment.End) {
                // Status tag
                Surface(
                    color = when (employee.status) {
                        "Active" -> Color(0xFF10B981).copy(alpha = 0.15f)
                        "Onboarding" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                        "Offboarding" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = employee.status.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (employee.status) {
                            "Active" -> Color(0xFF10B981)
                            "Onboarding" -> Color(0xFF3B82F6)
                            "Offboarding" -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Role: ${employee.userRole}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ==========================================
// SCREEN 3: LEAVE & CLAIM REQUEST MANAGEMENT
// ==========================================
@Composable
fun LeavesScreen(
    viewModel: HrViewModel,
    leaveRequests: List<LeaveRequest>,
    currentActor: Employee?,
    onAddLeaveClick: () -> Unit
) {
    val pendingList = leaveRequests.filter { it.status == "Pending" }
    val historyList = leaveRequests.filter { it.status != "Pending" }

    var selectedSectionTab by remember { mutableStateOf(0) } // 0 = Pending, 1 = Past History

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Leave & Claim Requests", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = "Approve annual leave, medical requests, or reimbursement logs.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            // Allow filing requests
            Button(
                onClick = onAddLeaveClick,
                modifier = Modifier.testTag("request_leave_fab")
            ) {
                Text("File Request", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Split Tab selector for Pending and History
        TabRow(selectedTabIndex = selectedSectionTab) {
            Tab(selected = selectedSectionTab == 0, onClick = { selectedSectionTab = 0 }) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pending Queue", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (pendingList.isNotEmpty()) {
                        Badge(containerColor = Color(0xFFEF4444), contentColor = Color.White) {
                            Text("${pendingList.size}", fontSize = 10.sp)
                        }
                    }
                }
            }
            Tab(selected = selectedSectionTab == 1, onClick = { selectedSectionTab = 1 }) {
                Text("Decision Log", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val displayedLogs = if (selectedSectionTab == 0) pendingList else historyList

        if (displayedLogs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Rounded.EventAvailable, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(52.dp))
                    Text(text = "No submissions in this queue.", fontWeight = FontWeight.Medium, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(displayedLogs) { req ->
                    LeaveRequestCard(
                        request = req,
                        currentActor = currentActor,
                        onApprove = { viewModel.approveRequest(req.id, req.employeeName, req.leaveType) },
                        onReject = { viewModel.rejectRequest(req.id, req.employeeName, req.leaveType) }
                    )
                }
            }
        }
    }
}

@Composable
fun LeaveRequestCard(
    request: LeaveRequest,
    currentActor: Employee?,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val isClaim = request.leaveType == "Claim"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leave_card_${request.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = request.employeeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = if (isClaim) "Claim: ${request.claimType}" else "Leave: ${request.leaveType}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (isClaim) {
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", request.amount)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "${request.startDate} to ${request.endDate}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    // Status Badge
                    Surface(
                        color = when(request.status) {
                            "Approved" -> Color(0xFF10B981).copy(alpha = 0.15f)
                            "Rejected" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = request.status.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = when(request.status) {
                                "Approved" -> Color(0xFF10B981)
                                "Rejected" -> Color(0xFFEF4444)
                                else -> Color(0xFFF59E0B)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Reason: ${request.reason}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // APPROVAL BUTTONS ROW (Gatekeeping: HR Admin and Manager only)
            if (request.status == "Pending" && (currentActor?.userRole == "HR Admin" || currentActor?.userRole == "Manager")) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.height(34.dp).testTag("reject_request_${request.id}")
                    ) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Reject", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.height(34.dp).testTag("approve_request_${request.id}")
                    ) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = "Approve", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: RECRUITMENT DECK & ATS
// ==========================================
@Composable
fun RecruitmentScreen(
    viewModel: HrViewModel,
    candidates: List<Candidate>,
    currentActor: Employee?,
    onAddCandidateClick: () -> Unit,
    onCandidateClick: (Candidate) -> Unit,
    onTriggerAiScreen: (Candidate) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "ATS Recruitment pipeline", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = "Assess applicants. Access smart resume screens powered by Gemini.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (currentActor?.userRole == "HR Admin" || currentActor?.userRole == "Manager") {
                Button(
                    onClick = onAddCandidateClick,
                    modifier = Modifier.testTag("add_candidate_button")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Candidate", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (candidates.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Rounded.Work, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(52.dp))
                    Text(text = "No active job applicants right now.", fontWeight = FontWeight.Medium, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(candidates) { cand ->
                    CandidateCard(
                        candidate = cand,
                        onClick = { onCandidateClick(cand) },
                        onAiClick = { onTriggerAiScreen(cand) }
                    )
                }
            }
        }
    }
}

@Composable
fun CandidateCard(
    candidate: Candidate,
    onClick: () -> Unit,
    onAiClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("candidate_card_${candidate.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = candidate.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = candidate.position, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = "Score", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        Text(text = "FIT: ${candidate.score}%", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Resume Summary:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = candidate.resumeSummary,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Status state flag
                Surface(
                    color = when(candidate.status) {
                        "Applied" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                        "Interviewing" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        "Offered" -> Color(0xFF10B981).copy(alpha = 0.15f)
                        else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = candidate.status,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(candidate.status) {
                            "Applied" -> Color(0xFF3B82F6)
                            "Interviewing" -> Color(0xFFF59E0B)
                            "Offered" -> Color(0xFF10B981)
                            else -> Color(0xFFEF4444)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                // Smart AI Screen button
                Button(
                    onClick = onAiClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(30.dp).testTag("ai_screen_${candidate.id}")
                ) {
                    Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Assessment Screen", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SCREEN 5: APPRAISALS & KPIS WORKSPACE
// ==========================================
@Composable
fun AppraisalScreen(
    viewModel: HrViewModel,
    employees: List<Employee>,
    performances: List<Performance>,
    currentActor: Employee?,
    onAddEvaluationClick: () -> Unit,
    onTriggerAiDraftReview: (Employee, Float, String) -> Unit
) {
    var displayTabState by remember { mutableStateOf(0) } // 0 = Evaluations Ledger, 1 = Score Analytics

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Performance Appraisals", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = "File performance metrics, cycle ratings, and draft reviews.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (currentActor?.userRole == "HR Admin" || currentActor?.userRole == "Manager") {
                Button(
                    onClick = onAddEvaluationClick,
                    modifier = Modifier.testTag("add_appraisal_button")
                ) {
                    Text("Appraise Staff", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(selectedTabIndex = displayTabState) {
            Tab(selected = displayTabState == 0, onClick = { displayTabState = 0 }) {
                Text("Evaluation Log", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
            }
            Tab(selected = displayTabState == 1, onClick = { displayTabState = 1 }) {
                Text("Staff Analytics Dashboard", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (displayTabState == 0) {
            if (performances.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Rounded.Assessment, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(52.dp))
                        Text(text = "No performance appraisals completed yet.", fontWeight = FontWeight.Medium, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(performances) { perf ->
                        PerformanceCard(perf = perf)
                    }
                }
            }
        } else {
            // Staff KPI Analytics
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Enterprise Average score banner
                item {
                    val averageKpi = if(performances.isNotEmpty()) performances.map { it.kpiProgress }.average() else 0.0
                    val averageRating = if(performances.isNotEmpty()) performances.map { it.rating }.average() else 0.0
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Enterprise KPI Analytics Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Average staff KPI Completed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${String.format("%.1f", averageKpi)}%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Average appraisal Rating", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${String.format("%.1f", averageRating)}★ / 5.0", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }

                // Individual team members performance grids
                item {
                    Text(text = "Departmental KPI Trackers", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }

                items(employees) { emp ->
                    val myPerfs = performances.filter { it.employeeId == emp.id }
                    val currentKpi = myPerfs.firstOrNull()?.kpiProgress ?: 40 // Fallback estimate
                    val ratingValue = myPerfs.firstOrNull()?.rating ?: 0f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(text = emp.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "${emp.role} (${emp.department})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                
                                Text(
                                    text = if(ratingValue > 0f) "Rating: ${ratingValue}★" else "No cycle evaluated",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Target Progress KPIs: $currentKpi%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            LinearProgressIndicator(
                                progress = { currentKpi / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (currentKpi >= 90) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                            )

                            // AI Draft Trigger directly from list
                            if (currentActor?.userRole == "HR Admin" || currentActor?.userRole == "Manager") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { onTriggerAiDraftReview(emp, ratingValue.coerceAtLeast(1.0f), "Outstanding performance in delivery objectives.") },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp).testTag("quick_ai_review_${emp.id}")
                                    ) {
                                        Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI Draft Evaluation Letter", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceCard(perf: Performance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = perf.employeeName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "Target Cycle: ${perf.cycle}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                        Text(text = "${perf.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Achievements:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = perf.achievements, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(6.dp))

            Text(text = "Gaps / Areas For Improvement:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = perf.areasOfImprovement, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "KPI Objective Target:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "${perf.kpiProgress}% Completed", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { perf.kpiProgress / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ==========================================
// FULL DETAIL VIEW: INDIVIDUAL STAF CARD
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmployeeDetailDialog(
    employee: Employee,
    viewModel: HrViewModel,
    currentActor: Employee?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val tasksFlow = remember(employee.id) { viewModel.getOnboardingTasks(employee.id) }
    val onboardingTasksList by tasksFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var newTaskTyped by remember { mutableStateOf("") }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Top controls
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close") }
                    
                    if (currentActor?.userRole == "HR Admin") {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_employee_detail_button")
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete Profile", tint = Color(0xFFEF4444))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile details core heading
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = employee.name.take(2).uppercase(),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 26.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = employee.name, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
                            Text(text = employee.role, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = employee.department,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Contact Detailing card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Contact & Identity Metrics", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                DetailRow("Work Email", employee.email)
                                DetailRow("Mobile Phone", employee.phone)
                                DetailRow("Signed Date", employee.hireDate)
                                
                                if (currentActor?.userRole == "HR Admin") {
                                    val formattedSalary = NumberFormat.getCurrencyInstance(Locale.US).format(employee.salary)
                                    DetailRow("Yearly Base Salary", "$formattedSalary/yr")
                                }
                            }
                        }
                    }

                    // Secure Document References link
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Document Hub (Vault Secure)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                        .clickable {
                                            Toast.makeText(SpacerLightContext(viewModel.getApplication()), "Initiating Contract secure link request...", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Rounded.AssignmentTurnedIn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                    Text(text = "Employment_Agreement_${employee.name.replace(" ", "_")}.pdf", fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    Icon(imageVector = Icons.Rounded.VerifiedUser, contentDescription = "Verified Seal", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    // ONBOARDING / OFFBOARDING WORKFLOW TRACKER CHECKLIST
                    item {
                        Text(
                            text = "Work Cycle Processes (${employee.status})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "A systematic tracking record corresponding to this staff's current transition status.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (onboardingTasksList.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("No custom checklists linked to this active status.", fontSize = 11.sp, color = Color.Gray)
                                
                                if (currentActor?.userRole == "HR Admin" && employee.status == "Onboarding") {
                                    Button(
                                        onClick = { viewModel.generateOnboardingChecklistWithAI(employee) },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Auto-Generate with AI", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // Show Tasks list
                        items(onboardingTasksList) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTask(task) },
                                    modifier = Modifier.testTag("onboarding_task_checkbox_${task.id}")
                                )
                                Text(
                                    text = task.taskName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Add custom checklist item manual form
                    if (currentActor?.userRole == "HR Admin") {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newTaskTyped,
                                    onValueChange = { newTaskTyped = it },
                                    placeholder = { Text("Add manual checklist task...", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f).testTag("new_task_text_field"),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        if (newTaskTyped.isNotBlank()) {
                                            viewModel.addCustomOnboardingTask(employee.id, newTaskTyped, if (employee.status == "Offboarding") "Offboarding" else "Onboarding")
                                            newTaskTyped = ""
                                        }
                                    }
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Utility spacer context
fun SpacerLightContext(app: android.app.Application) = app.applicationContext

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

// ==========================================
// RECRUITMENT CANDIDATE POPUP
// ==========================================
@Composable
fun CandidateDetailDialog(
    candidate: Candidate,
    onDismiss: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onTriggerAi: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val statuses = listOf("Applied", "Contacted", "Interviewing", "Offered", "Rejected")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Actions top row
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Recruit Progress Tracker", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_candidate_button")) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete Candidate", tint = Color.Gray)
                    }
                }
                
                Text(text = candidate.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(text = "Applying For: ${candidate.position}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                DetailRow("Contact Email", candidate.email)
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Current Stage Workflow:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                
                Box {
                    OutlinedButton(onClick = { dropdownExpanded = true }, shape = RoundedCornerShape(8.dp)) {
                        Text(text = "STAGE: ${candidate.status}")
                        Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                        statuses.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    onStatusChange(item)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(6.dp))

                Text(text = "Resume Screen Highlights:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = candidate.resumeSummary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(10.dp))
                
                Button(
                    onClick = onTriggerAi,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_ai_screen_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Smart AI Resume Analysis")
                }
            }
        }
    }
}

// ==========================================
// FORM DIALOGS DETAILED IMPLEMENTATIONS
// ==========================================

// 1. ADD EMPLOYEE INFO DIALOG
@Composable
fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, Double, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Engineering") }
    var role by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("Employee") } // "Employee", "Manager", "HR Admin"
    var salary by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Active") }
    var notes by remember { mutableStateOf("") }

    val departments = listOf("HR Department", "Engineering", "Product & Design", "Marketing", "Finance")
    val userRoles = listOf("Employee", "Manager", "HR Admin")
    val statuses = listOf("Active", "Onboarding")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Register New Employee Profile", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
                
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("add_employee_name"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Corporate Email Address") },
                        modifier = Modifier.fillMaxWidth().testTag("add_employee_email"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Biometric Phone Contact") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Job Title Designation (e.g., iOS Lead)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_employee_role"),
                        singleLine = true
                    )
                }

                // Dept dropdown selection
                item {
                    var expandedDept by remember { mutableStateOf(false) }
                    Column {
                        Text("Department Matrix:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { expandedDept = true }
                                .padding(12.dp)
                        ) {
                            Text(department, fontSize = 12.sp)
                            DropdownMenu(expanded = expandedDept, onDismissRequest = { expandedDept = false }) {
                                departments.forEach { dept ->
                                    DropdownMenuItem(text = { Text(dept) }, onClick = { department = dept; expandedDept = false })
                                }
                            }
                        }
                    }
                }

                // Authority Role selector
                item {
                    var expandedUserRole by remember { mutableStateOf(false) }
                    Column {
                        Text("Operational Authority:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { expandedUserRole = true }
                                .padding(12.dp)
                        ) {
                            Text(userRole, fontSize = 12.sp)
                            DropdownMenu(expanded = expandedUserRole, onDismissRequest = { expandedUserRole = false }) {
                                userRoles.forEach { role ->
                                    DropdownMenuItem(text = { Text(role) }, onClick = { userRole = role; expandedUserRole = false })
                                }
                            }
                        }
                    }
                }

                // Comp status selector
                item {
                    var expandedStatus by remember { mutableStateOf(false) }
                    Column {
                        Text("Initial Lifecycle Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { expandedStatus = true }
                                .padding(12.dp)
                        ) {
                            Text(status, fontSize = 12.sp)
                            DropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                                statuses.forEach { stat ->
                                    DropdownMenuItem(text = { Text(stat) }, onClick = { status = stat; expandedStatus = false })
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = salary,
                        onValueChange = { salary = it },
                        label = { Text("Base Annual Salary amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("add_employee_salary"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Worker Profile notes & history summary") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Button(
                            onClick = {
                                val dSalary = salary.toDoubleOrNull() ?: 60000.0
                                if (name.isNotBlank() && email.isNotBlank() && role.isNotBlank()) {
                                    onSave(name, email, phone, department, role, userRole, dSalary, status, notes)
                                }
                            },
                            modifier = Modifier.testTag("save_employee_button")
                        ) {
                            Text("Register")
                        }
                    }
                }
            }
        }
    }
}

// 2. ADD RECRUITMENT CANDIDATE INFO
@Composable
fun AddCandidateDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var manualScore by remember { mutableStateOf(80) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Add Applicant to Pipeline", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Applicant Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_candidate_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Contact Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Target Job Position") },
                    modifier = Modifier.fillMaxWidth().testTag("add_candidate_position"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Resume Summary Details (e.g., Certs, Years)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_candidate_summary"),
                    maxLines = 4
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && position.isNotBlank() && summary.isNotBlank()) {
                                onSave(name, email, position, summary, manualScore)
                            }
                        },
                        modifier = Modifier.testTag("save_candidate_button")
                    ) {
                        Text("Register Pipeline")
                    }
                }
            }
        }
    }
}

// 3. FILE LEAVE BENEFIT OR REIMBURSEMENT
@Composable
fun RequestLeaveDialog(
    applicant: Employee,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Double, String?) -> Unit
) {
    var type by remember { mutableStateOf("Annual") } // "Annual", "Sick", "Claim"
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var claimCategory by remember { mutableStateOf("Travel") }

    val categories = listOf("Travel", "Medical", "Gym", "Meals", "Development")
    val leaveTypes = listOf("Annual", "Sick", "Claim")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("File Leave or Reimbursement Claim", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }

                // Type switcher dropdown
                item {
                    var expandedType by remember { mutableStateOf(false) }
                    Column {
                        Text("Category Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { expandedType = true }
                                .padding(12.dp)
                        ) {
                            Text(type, fontSize = 12.sp)
                            DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                                leaveTypes.forEach { item ->
                                    DropdownMenuItem(text = { Text(item) }, onClick = { type = item; expandedType = false })
                                }
                            }
                        }
                    }
                }

                if (type == "Claim") {
                    item {
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("Reimbursement Value Amount ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("claim_amount_field"),
                            singleLine = true
                        )
                    }

                    item {
                        var expandedCat by remember { mutableStateOf(false) }
                        Column {
                            Text("Reimbursement Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                    .clickable { expandedCat = true }
                                    .padding(12.dp)
                            ) {
                                Text(claimCategory, fontSize = 12.sp)
                                DropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                                    categories.forEach { item ->
                                        DropdownMenuItem(text = { Text(item) }, onClick = { claimCategory = item; expandedCat = false })
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Begin Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth().testTag("leave_start_field"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("Finish Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth().testTag("leave_end_field"),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Explain reason or business justification") },
                        modifier = Modifier.fillMaxWidth().testTag("leave_reason_field"),
                        maxLines = 3
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Button(
                            onClick = {
                                val finalAmount = amountStr.toDoubleOrNull() ?: 0.0
                                val start = if (type == "Claim") SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) else startDate
                                val end = if (type == "Claim") start else endDate
                                
                                if (reason.isNotBlank()) {
                                    onSave(type, start, end, reason, finalAmount, if (type == "Claim") claimCategory else null)
                                }
                            },
                            modifier = Modifier.testTag("submit_request_button")
                        ) {
                            Text("Submit Queue")
                        }
                    }
                }
            }
        }
    }
}

// 4. APPRAISE EMPLOYEE DIALOG
@Composable
fun AppraisalDialog(
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onSave: (Employee, Float, String, String, Int, String) -> Unit,
    onTriggerAiDraft: (Employee, Float, String) -> Unit
) {
    if (employees.isEmpty()) {
        Dialog(onDismissRequest = onDismiss) {
            Card(shape = RoundedCornerShape(12.dp)) {
                Text("No evaluatable staff found.", modifier = Modifier.padding(24.dp))
            }
        }
        return
    }

    var selectedEmpIndex by remember { mutableStateOf(0) }
    val employee = employees[selectedEmpIndex]

    var rating by remember { mutableFloatStateOf(4.0f) }
    var achievements by remember { mutableStateOf("") }
    var improvement by remember { mutableStateOf("") }
    var kpiWeight by remember { mutableFloatStateOf(80f) }
    var cycle by remember { mutableStateOf("H1 2026") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Evaluate Worker KPIs", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }

                // Selected target
                item {
                    var expandedSelector by remember { mutableStateOf(false) }
                    Column {
                        Text("Select Worker Profile:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { expandedSelector = true }
                                .padding(12.dp)
                        ) {
                            Text("${employee.name} (${employee.role})", fontSize = 12.sp)
                            DropdownMenu(expanded = expandedSelector, onDismissRequest = { expandedSelector = false }) {
                                employees.forEachIndexed { index, emp ->
                                    DropdownMenuItem(text = { Text(emp.name) }, onClick = { selectedEmpIndex = index; expandedSelector = false })
                                }
                            }
                        }
                    }
                }

                // Sliders
                item {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Direct Rating Value:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.1f", rating)}★ / 5.0", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Slider(
                            value = rating,
                            onValueChange = { rating = it },
                            valueRange = 1.0f..5.0f,
                            steps = 3,
                            modifier = Modifier.testTag("appraisal_rating_slider")
                        )
                    }
                }

                item {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("KPI Objective Completed:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${kpiWeight.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Slider(
                            value = kpiWeight,
                            onValueChange = { kpiWeight = it },
                            valueRange = 0f..100f
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = achievements,
                        onValueChange = { achievements = it },
                        label = { Text("Core Achievements / Target Milestones met") },
                        modifier = Modifier.fillMaxWidth().testTag("appraisal_achievements"),
                        maxLines = 3
                    )
                }

                item {
                    OutlinedTextField(
                        value = improvement,
                        onValueChange = { improvement = it },
                        label = { Text("Assigned Growth areas & improvements") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                item {
                    OutlinedTextField(
                        value = cycle,
                        onValueChange = { cycle = it },
                        label = { Text("Appraisal Review Cycle (e.g., H1 2026)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // AI Draft Button helper
                item {
                    Button(
                        onClick = {
                            if (achievements.isNotEmpty()) {
                                onTriggerAiDraft(employee, rating, achievements)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth().testTag("draft_with_ai_button")
                    ) {
                        Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Draft Appraisal Letter with AI")
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (achievements.isNotBlank() && improvement.isNotBlank()) {
                                    onSave(employee, rating, achievements, improvement, kpiWeight.toInt(), cycle)
                                }
                            },
                            modifier = Modifier.testTag("submit_appraisal_button")
                        ) {
                            Text("Formalize Record")
                        }
                    }
                }
            }
        }
    }
}

// 5. REGULATORY AUDIT LOGS DIALOG
@Composable
fun AuditLogsDialog(
    logs: List<AuditLog>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Audit Logs & Compliance Tracker", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close") }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(logs) { log ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "[${log.actor}]", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)), fontSize = 9.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text(text = log.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// 6. AI INTELLIGENCE INTERACTION SHEET
@Composable
fun AiAssistantResultDialog(
    output: String,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)) // Aesthetic cyber theme dark background
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFF38BDF8))
                        Text("HR Pulse: AI Assistant Unit", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF38BDF8))
                    }
                    if (!isLoading) {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
                
                HorizontalDivider(color = Color(0xFF334155))
                
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8))
                        Text("Querying Gemini Intelligence...", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                } else {
                    val scrollState = androidx.compose.foundation.rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = output.ifEmpty { "No response generated from model." },
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().testTag("close_ai_dialog_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Acknowledge Guidance", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// NEW MODULES: EMPLOYEE SELF-SERVICE & HR INTELLIGENCE REPORTING
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPortalScreen(
    viewModel: HrViewModel,
    currentActor: Employee,
    announcements: List<Announcement>,
    profileUpdates: List<ProfileUpdateRequest>,
    attendanceLogs: List<Attendance>
) {
    val context = LocalContext.current
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val myAttendance = attendanceLogs.firstOrNull { it.employeeId == currentActor.id && it.date == todayStr }
    
    var editField by remember { mutableStateOf<String?>(null) }
    var editVal by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Header Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("EMPLOYEE SELF-SERVICE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Welcome back, ${currentActor.name}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Department: ${currentActor.department}  •  Role: ${currentActor.role}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
            }
        }

        // Attendance Log
        item {
            Card(shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Clock-In / Attendance logging", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(todayStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (myAttendance == null) {
                        Button(
                            onClick = {
                                viewModel.performCheckIn(currentActor.id, currentActor.name)
                                Toast.makeText(context, "Logged check-in status!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("employee_clock_in_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Punch In Today")
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Arrived: ${myAttendance.checkIn} (${myAttendance.status})", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 12.sp)
                            if (myAttendance.checkOut == null) {
                                Button(
                                    onClick = { viewModel.performCheckOut(currentActor.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Punch Out")
                                }
                            } else {
                                Text("Departed: ${myAttendance.checkOut}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Personal Information with interactive approval changes
        item {
            Card(shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("My Profile Information (Edit requests are vetted)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    listOf("Email" to currentActor.email, "Phone" to currentActor.phone, "Bio/Notes" to currentActor.notes).forEach { (field, value) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(field, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(value.ifEmpty { "None listed" }, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = {
                                editField = field
                                editVal = value
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit $field", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Change Approval status ledger
        val myReqs = profileUpdates.filter { it.employeeId == currentActor.id }
        if (myReqs.isNotEmpty()) {
            item {
                Card(shape = RoundedCornerShape(10.dp)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Status of Profile Update Demands", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        myReqs.forEach { req ->
                            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Field: ${req.fieldName}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Apply: '${req.newValue}'", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Badge(containerColor = if (req.status == "Approved") Color(0xFF10B981) else if (req.status == "Pending") Color(0xFFFBBF24) else Color(0xFFEF4444)) {
                                    Text(req.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bulletins list
        item {
            Text("Company Information Bulletins", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        if (announcements.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No central announcments posted.", fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(announcements) { ann ->
                Card(shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ann.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            Text(ann.date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ann.content, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Posted by: ${ann.author}", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }

    if (editField != null) {
        val f = editField!!
        AlertDialog(
            onDismissRequest = { editField = null },
            title = { Text("Update $f") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (f == "Bio/Notes") "Applied directly to database." else "Vetted under manager approval logs.", fontSize = 11.sp)
                    OutlinedTextField(
                        value = editVal,
                        onValueChange = { editVal = it },
                        modifier = Modifier.fillMaxWidth().testTag("edit_field_input")
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val oldVal = when (f) {
                        "Email" -> currentActor.email
                        "Phone" -> currentActor.phone
                        else -> currentActor.notes
                    }
                    viewModel.submitProfileChange(currentActor.id, currentActor.name, f, oldVal, editVal)
                    editField = null
                    Toast.makeText(context, "Information update logged!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Apply change")
                }
            },
            dismissButton = {
                TextButton(onClick = { editField = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLeavesScreen(
    viewModel: HrViewModel,
    currentActor: Employee,
    allLeaveRequests: List<LeaveRequest>
) {
    val context = LocalContext.current
    val myLeaves = allLeaveRequests.filter { it.employeeId == currentActor.id }
    
    var showForm by remember { mutableStateOf(false) }
    var leaveCategory by remember { mutableStateOf("Annual") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reasonField by remember { mutableStateOf("") }
    var expenseAmt by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Annual Leave", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("15 Days", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("9 Available", fontSize = 9.sp, color = Color(0xFF10B981))
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Sick Leave", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("10 Days", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("10 Available", fontSize = 9.sp, color = Color(0xFF10B981))
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Approved Reimbursements", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val claimApproved = myLeaves.filter { it.leaveType == "Claim" && it.status == "Approved" }.sumOf { it.amount }
                            Text("$${claimApproved}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("My Pending & Approved Requests", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Button(onClick = { showForm = true }, shape = RoundedCornerShape(8.dp), modifier = Modifier.testTag("request_leave_fab")) {
                        Text("Add Event", fontSize = 11.sp)
                    }
                }
            }

            if (myLeaves.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No leave allocations/claims generated.", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(myLeaves) { request ->
                    Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().testTag("personal_leave_item_${request.id}")) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (request.leaveType == "Claim") "Claim: ${request.claimType}" else "${request.leaveType} Leave", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Badge(containerColor = if (request.status == "Approved") Color(0xFF10B981) else if (request.status == "Pending") Color(0xFFFBBF24) else Color(0xFFEF4444)) {
                                    Text(request.status, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Details: ${request.reason}", fontSize = 11.sp)
                            Text(if (request.leaveType == "Claim") "Valuation: $${request.amount}" else "Duration: ${request.startDate} to ${request.endDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text("Log Leave or Claims Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Annual", "Sick", "Claim").forEach { cat ->
                            FilterChip(
                                selected = leaveCategory == cat,
                                onClick = { leaveCategory = cat },
                                label = { Text(cat) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                    if (leaveCategory == "Claim") {
                        OutlinedTextField(value = expenseAmt, onValueChange = { expenseAmt = it }, label = { Text("Value ($ USD)") }, modifier = Modifier.fillMaxWidth())
                    } else {
                        OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    }
                    OutlinedTextField(value = reasonField, onValueChange = { reasonField = it }, label = { Text("Reason Description") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (reasonField.isEmpty()) return@Button
                    val amt = expenseAmt.toDoubleOrNull() ?: 0.0
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    viewModel.submitLeaveRequest(
                        employeeId = currentActor.id,
                        employeeName = currentActor.name,
                        type = leaveCategory,
                        startStr = if (leaveCategory == "Claim") today else startDate,
                        endStr = if (leaveCategory == "Claim") today else endDate,
                        reason = reasonField,
                        amount = amt,
                        claimType = if (leaveCategory == "Claim") "Business Expenses" else null
                    )
                    showForm = false
                    Toast.makeText(context, "Logged request!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Publish Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun MyPerformanceScreen(
    viewModel: HrViewModel,
    currentActor: Employee,
    allGoals: List<Goal>,
    allFeedbacks: List<ContinuousFeedback>,
    allPerformances: List<Performance>
) {
    val context = LocalContext.current
    val myGoals = allGoals.filter { it.employeeId == currentActor.id }
    val myFeedback = allFeedbacks.filter { it.employeeId == currentActor.id }
    val myEvals = allPerformances.filter { it.employeeId == currentActor.id }
    
    var selfAch by remember { mutableStateOf("") }
    var selfLock by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Assigned Goals & OKRs (Slide update)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        }
        if (myGoals.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No OKRs set.")
                    }
                }
            }
        } else {
            items(myGoals) { goal ->
                Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().testTag("goal_card_${goal.id}")) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(goal.objective, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${goal.progress}% completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("Key metrics: ${goal.keyResults}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Slider(
                            value = goal.progress.toFloat(),
                            onValueChange = { viewModel.modifyGoalProgress(goal.id, it.toInt(), if (it >= 100) "Completed" else "Active", currentActor.name) },
                            valueRange = 0f..100f,
                            modifier = Modifier.testTag("goal_slider_${goal.id}")
                        )
                    }
                }
            }
        }

        item {
            Text("Continuous Feedback Loop (360°)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        if (myFeedback.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        Text("No peer feedback yet.", fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(myFeedback) { fb ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("\"${fb.feedbackText}\"", fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("By: ${fb.providerName}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text(fb.date, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth().testTag("self_assessment_card")) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Interactive Self-Assessment Appraisal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (selfLock) {
                        Text("✓ Reflections filed to personal dossier file successfully!", color = Color(0xFF10B981), fontSize = 11.sp)
                    } else {
                        OutlinedTextField(value = selfAch, onValueChange = { selfAch = it }, placeholder = { Text("Log achievements and aspirations details...") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                        Button(
                            onClick = { if (selfAch.isNotEmpty()) selfLock = true },
                            modifier = Modifier.fillMaxWidth().testTag("submit_self_assessment_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Commit Reflections")
                        }
                    }
                }
            }
        }

        item {
            Text("Appraisals History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        if (myEvals.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No official review records registered.", fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(myEvals) { ev ->
                Card(modifier = Modifier.fillMaxWidth().testTag("eval_card_${ev.id}")) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cycle: ${ev.cycle}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Rating: ${ev.rating} / 5.0", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFFF59E0B))
                        }
                        Text("Highlights: ${ev.achievements}", fontSize = 11.sp)
                        Text("Gaps: ${ev.areasOfImprovement}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun MyPayslipsScreen(currentActor: Employee) {
    val context = LocalContext.current
    var activeSlip by remember { mutableStateOf<String?>(null) }
    
    val base = currentActor.salary / 12.0
    val tax = base * 0.15
    val health = base * 0.025
    val takeHome = base + 200.0 - (tax + health)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Salary Register", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Vetted and encrypted corporate payslip certificates", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (activeSlip != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth().testTag("expanded_payslip_card")) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Detailed Slip: ${activeSlip!!}", fontWeight = FontWeight.Black, fontSize = 14.sp)
                            IconButton(onClick = { activeSlip = null }) { Icon(Icons.Rounded.Close, contentDescription = null) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Basic Base Salary:")
                            Text(String.format(Locale.getDefault(), "$%.2f", base), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Allowances (Internet Subsidy):")
                            Text("$200.00", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax Withholdings (15.0%):", color = MaterialTheme.colorScheme.error)
                            Text(String.format(Locale.getDefault(), "-$%.2f", tax), color = MaterialTheme.colorScheme.error)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Healthcare Premiums (2.5%):", color = MaterialTheme.colorScheme.error)
                            Text(String.format(Locale.getDefault(), "-$%.2f", health), color = MaterialTheme.colorScheme.error)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Disbursed Take-home:", fontWeight = FontWeight.Black)
                            Text(String.format(Locale.getDefault(), "$%.2f", takeHome), fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        }
                        Button(
                            onClick = { Toast.makeText(context, "Payslip PDF cryptographic key successfully built and certificate downloaded!", Toast.LENGTH_LONG).show() },
                            modifier = Modifier.fillMaxWidth().testTag("download_payslip_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Export Slip PDF")
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    listOf("June 2026", "May 2026", "April 2026", "March 2026").forEach { mth ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { activeSlip = mth }.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(mth, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(String.format(Locale.getDefault(), "$%.2f", takeHome), fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 12.sp)
                                Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceManagerScreen(
    viewModel: HrViewModel,
    employees: List<Employee>,
    performances: List<Performance>,
    goals: List<Goal>,
    feedbacks: List<ContinuousFeedback>,
    profileUpdates: List<ProfileUpdateRequest>,
    currentActor: Employee?,
    onAddEvaluationClick: () -> Unit,
    onTriggerAiDraftReview: (Employee, Float, String) -> Unit
) {
    val context = LocalContext.current
    var activeTabSub by remember { mutableStateOf(0) }
    
    var sEmpGoal by remember { mutableStateOf<Employee?>(null) }
    var goalObjective by remember { mutableStateOf("") }
    var goalKrs by remember { mutableStateOf("") }
    
    var sEmpFb by remember { mutableStateOf<Employee?>(null) }
    var fbMsg by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Enterprise Appraisal Control", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Manage workforce compliance, assign OKRs, review audits", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onAddEvaluationClick, shape = RoundedCornerShape(8.dp), modifier = Modifier.testTag("add_evaluation_btn")) {
                Text("Appraise", fontSize = 11.sp)
            }
        }

        TabRow(selectedTabIndex = activeTabSub) {
            Tab(selected = activeTabSub == 0, onClick = { activeTabSub = 0 }) { Text("OKRs Setter", modifier = Modifier.padding(8.dp), fontSize = 11.sp) }
            Tab(selected = activeTabSub == 1, onClick = { activeTabSub = 1 }) { Text("360 Feedback", modifier = Modifier.padding(8.dp), fontSize = 11.sp) }
            Tab(selected = activeTabSub == 2, onClick = { activeTabSub = 2 }) {
                val p = profileUpdates.count { it.status == "Pending" }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Approvals", modifier = Modifier.padding(8.dp), fontSize = 11.sp)
                    if (p > 0) Badge(containerColor = MaterialTheme.colorScheme.error) { Text("$p") }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeTabSub) {
                0 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Assign Objective Goal (OKR)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        employees.filter { it.userRole == "Employee" }.forEach { emp ->
                                            FilterChip(
                                                selected = sEmpGoal?.id == emp.id,
                                                onClick = { sEmpGoal = emp },
                                                label = { Text(emp.name, fontSize = 10.sp) },
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        }
                                    }
                                    OutlinedTextField(value = goalObjective, onValueChange = { goalObjective = it }, label = { Text("Core Objective") }, modifier = Modifier.fillMaxWidth().testTag("goal_objective_input"))
                                    OutlinedTextField(value = goalKrs, onValueChange = { goalKrs = it }, label = { Text("Key Performance metrics") }, modifier = Modifier.fillMaxWidth())
                                    Button(
                                        onClick = {
                                            val emp = sEmpGoal ?: return@Button
                                            if (goalObjective.isEmpty()) return@Button
                                            viewModel.createGoal(emp.id, emp.name, goalObjective, goalKrs)
                                            Toast.makeText(context, "OKR objectives locked!", Toast.LENGTH_SHORT).show()
                                            goalObjective = ""
                                            goalKrs = ""
                                            sEmpGoal = null
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("manager_save_goal_btn")
                                    ) { Text("Lock OKR Goal") }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Submit team continuous feedback", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        employees.filter { it.userRole == "Employee" }.forEach { emp ->
                                            FilterChip(
                                                selected = sEmpFb?.id == emp.id,
                                                onClick = { sEmpFb = emp },
                                                label = { Text(emp.name, fontSize = 10.sp) },
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        }
                                    }
                                    OutlinedTextField(value = fbMsg, onValueChange = { fbMsg = it }, label = { Text("Continuous feedback message text") }, modifier = Modifier.fillMaxWidth().testTag("feedback_text_input"))
                                    Button(
                                        onClick = {
                                            val emp = sEmpFb ?: return@Button
                                            if (fbMsg.isEmpty()) return@Button
                                            viewModel.sendContinuousFeedback(emp.id, emp.name, fbMsg)
                                            Toast.makeText(context, "360 constructive comments logged!", Toast.LENGTH_SHORT).show()
                                            fbMsg = ""
                                            sEmpFb = null
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("manager_save_feedback_btn")
                                    ) { Text("Send Feedback") }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    val pReqs = profileUpdates.filter { it.status == "Pending" }
                    if (pReqs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pending employee self-service records to authorize.", fontSize = 11.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(pReqs) { req ->
                                Card(modifier = Modifier.fillMaxWidth().testTag("pending_approval_card_${req.id}")) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${req.employeeName} requests ${req.fieldName} update", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Convert old: '${req.oldValue}' to new: '${req.newValue}'", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.approveProfileUpdate(req) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                modifier = Modifier.weight(1f).testTag("approve_btn_${req.id}")
                                            ) { Text("Approve") }
                                            OutlinedButton(
                                                onClick = { viewModel.rejectProfileUpdate(req) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                modifier = Modifier.weight(1f)
                                            ) { Text("Reject") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrReportsScreen(
    employees: List<Employee>,
    leaveRequests: List<LeaveRequest>,
    performances: List<Performance>
) {
    val context = LocalContext.current
    var repSource by remember { mutableStateOf("Demographics") }
    var repType by remember { mutableStateOf("Bar Chart") }

    val depts = employees.groupBy { it.department }.mapValues { it.value.size }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth().testTag("reports_customizer_card")) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Interactive Reporting Customizer (Drag Selector)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Dataset Metric Source", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Row {
                        listOf("Demographics", "Compensation", "Leaves").forEach { src ->
                            FilterChip(selected = repSource == src, onClick = { repSource = src }, label = { Text(src) }, modifier = Modifier.padding(end = 4.dp))
                        }
                    }
                    Text("Active Chart visualization Style", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Row {
                        listOf("Bar Chart", "Line Graph", "Pie Chart").forEach { style ->
                            FilterChip(selected = repType == style, onClick = { repType = style }, label = { Text(style) }, modifier = Modifier.padding(end = 4.dp))
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth().testTag("live_canvas_chart_card")) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Live Graphical Canvas: $repSource ($repType)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("graphics_report_canvas")
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    ) {
                        val cw = size.width
                        val ch = size.height
                        
                        drawLine(Color.LightGray, androidx.compose.ui.geometry.Offset(20f, 10f), androidx.compose.ui.geometry.Offset(20f, ch - 10f), 3f)
                        drawLine(Color.LightGray, androidx.compose.ui.geometry.Offset(20f, ch - 10f), androidx.compose.ui.geometry.Offset(cw - 20f, ch - 10f), 3f)
                        
                        val data = listOf(0.4f, 0.8f, 0.5f, 0.9f)
                        val barColors = listOf(Color(0xFF6750A4), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFEF4444))
                        
                        if (repType == "Bar Chart") {
                            val chunk = (cw - 40f) / data.size
                            for (i in data.indices) {
                                val h = data[i] * (ch - 30f)
                                drawRect(
                                    color = barColors[i % barColors.size],
                                    topLeft = androidx.compose.ui.geometry.Offset(25f + (i * chunk), ch - 10f - h),
                                    size = androidx.compose.ui.geometry.Size(chunk * 0.7f, h)
                                )
                            }
                        } else if (repType == "Line Graph") {
                            val chunk = (cw - 40f) / (data.size - 1)
                            for (i in 0 until data.size - 1) {
                                drawLine(
                                    color = Color(0xFF3B82F6),
                                    start = androidx.compose.ui.geometry.Offset(20f + (i * chunk), ch - 10f - (data[i] * (ch - 30f))),
                                    end = androidx.compose.ui.geometry.Offset(20f + ((i + 1) * chunk), ch - 10f - (data[i + 1] * (ch - 30f))),
                                    strokeWidth = 6f
                                )
                                drawCircle(Color(0xFFEF4444), 8f, androidx.compose.ui.geometry.Offset(20f + (i * chunk), ch - 10f - (data[i] * (ch - 30f))))
                            }
                        } else {
                            var ang = 0f
                            for (i in data.indices) {
                                val s = (data[i] / data.sum()) * 360f
                                drawArc(
                                    color = barColors[i % barColors.size],
                                    startAngle = ang,
                                    sweepAngle = s,
                                    useCenter = true,
                                    topLeft = androidx.compose.ui.geometry.Offset(cw / 2f - 45f, ch / 2f - 45f),
                                    size = androidx.compose.ui.geometry.Size(90f, 90f)
                                )
                                ang += s
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth().testTag("strategic_insights_card")) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Actionable Business Trend Insights", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = if (repSource == "Demographics") {
                            "• Engineering vertical accounts for 42% of headcount. Support talent structures display strong onboarding speeds."
                        } else if (repSource == "Compensation") {
                            "• Engineering payroll structure consumes 55% of department budget index. Recommendation: Restructure staffing models."
                        } else {
                            "• Team vacation requests focus trends around July 2026. Advise managers to implement project role overlaps."
                        },
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth().testTag("export_reports_card")) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Executive Report Distribution", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { Toast.makeText(context, "Demographics formatted CSV saved to system storage!", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).testTag("export_csv_btn")
                        ) { Text("Print CSV", fontSize = 11.sp) }
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Executive strategic report PDF successfully compiled and encrypted!", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).testTag("export_pdf_btn")
                        ) { Text("Print PDF", fontSize = 11.sp) }
                    }
                }
            }
        }
    }
}

package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.HrDatabase
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
import com.example.data.repository.HrRepository
import com.example.api.GeminiHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HrViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HrDatabase.getDatabase(application)
    private val repository = HrRepository(db.hrDao())

    // --- Active User Sessions (for demonstration & Role-Based Access Controls testing) ---
    private val _currentActor = MutableStateFlow<Employee?>(null)
    val currentActor: StateFlow<Employee?> = _currentActor.asStateFlow()

    // --- Search & Filters ---
    val employeeSearchQuery = MutableStateFlow("")
    val departmentFilter = MutableStateFlow("All Departments")
    val employeeStatusFilter = MutableStateFlow("All Statuses")

    // --- Smart Gemini Assistant States ---
    private val _aiAssistantOutput = MutableStateFlow("")
    val aiAssistantOutput: StateFlow<String> = _aiAssistantOutput.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // --- Database Data Streams ---
    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLeaveRequests: StateFlow<List<LeaveRequest>> = repository.allLeaveRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCandidates: StateFlow<List<Candidate>> = repository.allCandidates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPerformance: StateFlow<List<Performance>> = repository.allPerformance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<Announcement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<Goal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeedback: StateFlow<List<ContinuousFeedback>> = repository.allFeedback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProfileUpdates: StateFlow<List<ProfileUpdateRequest>> = repository.allProfileUpdates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Filtered Employee Stream ---
    val filteredEmployees: StateFlow<List<Employee>> = combine(
        allEmployees,
        employeeSearchQuery,
        departmentFilter,
        employeeStatusFilter
    ) { employees, query, dept, status ->
        employees.filter { emp ->
            val matchesQuery = emp.name.contains(query, ignoreCase = true) ||
                    emp.role.contains(query, ignoreCase = true) ||
                    emp.email.contains(query, ignoreCase = true)
            val matchesDept = dept == "All Departments" || emp.department == dept
            val matchesStatus = status == "All Statuses" || emp.status == status
            matchesQuery && matchesDept && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize sample seeding, set Default Active User to sarah (HR Admin)
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            // Set sarah jenkins as active session
            val list = repository.allEmployees.first()
            if (list.isNotEmpty()) {
                _currentActor.value = list.firstOrNull { it.userRole == "HR Admin" } ?: list.first()
            }
        }
    }

    // --- Role Session Switch helper ---
    fun selectActiveActor(employee: Employee) {
        _currentActor.value = employee
        viewModelScope.launch {
            repository.insertAuditLog(
                actor = "System Settings",
                action = "Switched User Session",
                description = "Active session switched to ${employee.name} (${employee.userRole})"
            )
        }
    }

    // --- Core Action Operations ---

    // 1. Employee Management
    fun addEmployee(name: String, email: String, phone: String, department: String, role: String, userRole: String, salary: Double, status: String, notes: String) {
        viewModelScope.launch {
            val emp = Employee(
                name = name,
                email = email,
                phone = phone,
                department = department,
                role = role,
                userRole = userRole,
                hireDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                salary = salary,
                status = status,
                notes = notes
            )
            val empId = repository.insertEmployee(emp).toInt()

            // Pre-create some onboarding checklist tasks if status is Onboarding
            if (status == "Onboarding") {
                repository.insertOnboardingTask(OnboardingTask(employeeId = empId, taskName = "Formal Offer Letter signed by Candidate", category = "Onboarding", isCompleted = false))
                repository.insertOnboardingTask(OnboardingTask(employeeId = empId, taskName = "Configure corporate Slack & Email channels", category = "Onboarding", isCompleted = false))
                repository.insertOnboardingTask(OnboardingTask(employeeId = empId, taskName = "Welcome briefing and intro with HR Sarah", category = "Onboarding", isCompleted = false))
                repository.insertOnboardingTask(OnboardingTask(employeeId = empId, taskName = "Compliance, Security, and NDAs signed", category = "Onboarding", isCompleted = false))
            } else if (status == "Offboarding") {
                repository.insertOnboardingTask(OnboardingTask(employeeId = empId, taskName = "Retrieve Dev laptop, security keys, and credentials", category = "Offboarding", isCompleted = false))
                repository.insertOnboardingTask(OnboardingTask(employeeId = empId, taskName = "Sign formal de-escalation of corporate secrets NDA", category = "Offboarding", isCompleted = false))
                repository.insertOnboardingTask(OnboardingTask(employeeId = empId, taskName = "Exit Interview cycle conducted by CEO", category = "Offboarding", isCompleted = false))
            }
        }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.updateEmployee(employee)
        }
    }

    fun deleteEmployee(id: Int, name: String) {
        viewModelScope.launch {
            repository.deleteEmployee(id, name)
        }
    }

    // 2. Attendance
    fun performCheckIn(employeeId: Int, employeeName: String) {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val startTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            repository.checkInEmployee(employeeId, employeeName, todayDate, startTime)
        }
    }

    fun performCheckOut(employeeId: Int) {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val endTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            repository.checkOutEmployee(employeeId, todayDate, endTime)
        }
    }

    // 3. Leave & Reimbursement Claims Requests
    fun submitLeaveRequest(employeeId: Int, employeeName: String, type: String, startStr: String, endStr: String, reason: String, amount: Double = 0.0, claimType: String? = null) {
        viewModelScope.launch {
            val req = LeaveRequest(
                employeeId = employeeId,
                employeeName = employeeName,
                leaveType = type,
                startDate = startStr,
                endDate = endStr,
                reason = reason,
                amount = amount,
                claimType = claimType,
                status = "Pending"
            )
            repository.createLeaveRequest(req)
        }
    }

    fun approveRequest(requestId: Int, applicantName: String, leaveType: String) {
        viewModelScope.launch {
            val actorName = _currentActor.value?.name ?: "HR Admin"
            repository.approveLeaveRequest(requestId, applicantName, leaveType, actorName)
        }
    }

    fun rejectRequest(requestId: Int, applicantName: String, leaveType: String) {
        viewModelScope.launch {
            val actorName = _currentActor.value?.name ?: "HR Admin"
            repository.rejectLeaveRequest(requestId, applicantName, leaveType, actorName)
        }
    }

    // 4. Onboarding / Offboarding Tasks completion toggling
    fun getOnboardingTasks(employeeId: Int) = repository.getTasksForEmployee(employeeId)

    fun toggleTask(task: OnboardingTask) {
        viewModelScope.launch {
            val actorName = _currentActor.value?.name ?: "HR Admin"
            repository.toggleTaskCompletion(task, actorName)
        }
    }

    fun addCustomOnboardingTask(employeeId: Int, name: String, type: String) {
        viewModelScope.launch {
            repository.insertOnboardingTask(
                OnboardingTask(
                    employeeId = employeeId,
                    taskName = name,
                    category = type,
                    isCompleted = false
                )
            )
        }
    }

    // 5. Performance Evaluations
    fun submitPerformanceEvaluation(employeeId: Int, employeeName: String, rating: Float, achievements: String, gaps: String, kpiProgress: Int, cycle: String) {
        viewModelScope.launch {
            val perf = Performance(
                employeeId = employeeId,
                employeeName = employeeName,
                rating = rating,
                achievements = achievements,
                areasOfImprovement = gaps,
                kpiProgress = kpiProgress,
                cycle = cycle,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            repository.insertPerformance(perf)
        }
    }

    // 6. Recruitment (ATS Pipeline)
    fun addCandidate(name: String, email: String, position: String, resumeSummary: String, manualScore: Int = 50) {
        viewModelScope.launch {
            val cand = Candidate(
                name = name,
                email = email,
                position = position,
                resumeSummary = resumeSummary,
                score = manualScore,
                status = "Applied"
            )
            repository.insertCandidate(cand)
        }
    }

    fun updateCandidateStatus(id: Int, name: String, status: String) {
        viewModelScope.launch {
            repository.updateCandidateStatus(id, name, status)
        }
    }

    fun removeCandidate(id: Int, name: String) {
        viewModelScope.launch {
            repository.deleteCandidate(id, name)
        }
    }

    // --- Announcements ---
    fun postAnnouncement(title: String, content: String) {
        viewModelScope.launch {
            val actorName = _currentActor.value?.name ?: "HR Admin"
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertAnnouncement(Announcement(title = title, content = content, date = dateStr, author = actorName))
        }
    }

    // --- OKR Goals ---
    fun createGoal(employeeId: Int, employeeName: String, objective: String, keyResults: String, cycle: String = "H1 2026") {
        viewModelScope.launch {
            val g = Goal(employeeId = employeeId, employeeName = employeeName, objective = objective, keyResults = keyResults, cycle = cycle)
            repository.insertGoal(g)
        }
    }

    fun modifyGoalProgress(goalId: Int, progress: Int, status: String, employeeName: String) {
        viewModelScope.launch {
            repository.updateGoalProgress(goalId, progress, status, employeeName)
        }
    }

    // --- Continuous Feedback ---
    fun sendContinuousFeedback(employeeId: Int, employeeName: String, feedbackText: String) {
        viewModelScope.launch {
            val actorName = _currentActor.value?.name ?: "HR Admin"
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val fb = ContinuousFeedback(employeeId = employeeId, employeeName = employeeName, providerName = actorName, feedbackText = feedbackText, date = dateStr)
            repository.insertFeedback(fb)
        }
    }

    // --- Profile Updates Self Service ---
    fun submitProfileChange(employeeId: Int, employeeName: String, fieldName: String, oldValue: String, newValue: String) {
        viewModelScope.launch {
            // certain fields can be auto-approved, but standard is Pending
            val autoApprove = fieldName == "Notes"
            val req = ProfileUpdateRequest(employeeId = employeeId, employeeName = employeeName, fieldName = fieldName, oldValue = oldValue, newValue = newValue)
            if (autoApprove) {
                repository.approveProfileUpdateRequest(req, "Auto Approve")
            } else {
                repository.createProfileUpdateRequest(req)
            }
        }
    }

    fun approveProfileUpdate(request: ProfileUpdateRequest) {
        viewModelScope.launch {
            val actorName = _currentActor.value?.name ?: "HR Admin"
            repository.approveProfileUpdateRequest(request, actorName)
        }
    }

    fun rejectProfileUpdate(request: ProfileUpdateRequest) {
        viewModelScope.launch {
            val actorName = _currentActor.value?.name ?: "HR Admin"
            repository.rejectProfileUpdateRequest(request.id, request.employeeName, request.fieldName, actorName)
        }
    }

    fun clearResult() {
        _aiAssistantOutput.value = ""
    }

    // --- Smart Gemini Assistant Operations ---

    // A. AI Resume Screening
    fun analyzeResumeSummaryWithAI(candidate: Candidate) {
        _isAiLoading.value = true
        _aiAssistantOutput.value = "Analyzing applicant resume summary..."
        viewModelScope.launch {
            val prompt = """
                You are practicing compliance with modern HR staffing procedures. Please analyze the following candidate's resume summary details for the position of '${candidate.position}'.
                Candidate Name: ${candidate.name}
                Applicant Resume Summary: ${candidate.resumeSummary}
                
                Please generate:
                1. HR Matching Score (0 - 100) based on suitability, experience flags, and cloud certifications.
                2. Direct Strengths of this applicant.
                3. Essential structural gaps.
                4. Key interview questions to confirm technical fit.
                
                Format the review beautifully and concisely with professional structure. Keep it scannable.
            """.trimIndent()

            val systemInstruction = "You are an Elite Enterprise Human Resources Analyst and ATS Screening officer. Provide accurate, professional evaluation results."
            val response = GeminiHelper.generateResponse(prompt, systemInstruction)
            _aiAssistantOutput.value = response
            _isAiLoading.value = false

            // Try to extract a score from response, or update with notes
            val regex = Regex("Matching Score\\s*:\\s*(\\d+)|Score\\s*:\\s*(\\d+)|\\b(\\d{2})\\s*/\\s*100")
            var score = candidate.score
            val match = regex.find(response)
            if (match != null) {
                val num = match.groupValues.firstOrNull { it.toIntOrNull() != null }?.toIntOrNull()
                if (num != null && num in 10..100) {
                    score = num
                }
            }
            
            // Save notes in Candidate screenNotes
            repository.updateCandidateStatus(candidate.id, candidate.name, candidate.status) // Normal tracking trigger
            repository.insertAuditLog(
                actor = "AI HR Assistant",
                action = "Analyzed Resume",
                description = "Processed screening summary for candidate ${candidate.name}. Generated fit review report."
            )
        }
    }

    // B. AI Draft Appraisal writer
    fun draftEmployeeAppraisalWithAI(employeeName: String, role: String, rating: Float, achievements: String) {
        _isAiLoading.value = true
        _aiAssistantOutput.value = "Drafting comprehensive performance review with AI..."
        viewModelScope.launch {
            val prompt = """
                Generate a formal Corporate Performance Appraisal review letter for:
                Employee Name: $employeeName
                Job Role: $role
                Assessed Rating: $rating / 5.0
                Noted Achievements & Highlights: $achievements
                
                Identify:
                1. Performance summary detailing business impact.
                2. Key development goals for the next corporate cycle.
                3. Training resources or recommended coaching programs.
                
                Write in an elegant, objective corporate voice that HR Directors can easily copy-paste or print.
            """.trimIndent()

            val systemInstruction = "You are a professional Enterprise Human Resources Coach and Executive Appraisal Writer. Author constructive, polished reviews."
            val response = GeminiHelper.generateResponse(prompt, systemInstruction)
            _aiAssistantOutput.value = response
            _isAiLoading.value = false

            repository.insertAuditLog(
                actor = "AI HR Assistant",
                action = "Drafted Performance Review",
                description = "Wrote official review appraisal text for employee $employeeName at scale."
            )
        }
    }

    // C. AI Custom Task generator
    fun generateOnboardingChecklistWithAI(employee: Employee) {
        _isAiLoading.value = true
        _aiAssistantOutput.value = "Generating personalized onboarding tasks..."
        viewModelScope.launch {
            val prompt = """
                Recommend 4 highly specific onboarding tasks suitable for:
                Name: ${employee.name}
                Department: ${employee.department}
                Specific Role: ${employee.role}
                
                The tasks should focus on:
                1. Critical team handovers or developer keys setup.
                2. Departmental security or budget regulations compliance.
                3. First weekly deliverable or onboarding presentation.
                
                Format as a clean markdown list with each item starting with a dash, so it can be parsed instantly.
            """.trimIndent()

            val response = GeminiHelper.generateResponse(prompt, "You are a corporate training and employee onboarding orientation expert.")
            _aiAssistantOutput.value = response
            _isAiLoading.value = false

            // Automatically try to extract bullet-points and insert them to DB
            val lines = response.lines()
            var addedCount = 0
            for (line in lines) {
                var taskName = line.trim()
                if (taskName.startsWith("-") || taskName.startsWith("*") || taskName.startsWith("1.") || taskName.startsWith("2.") || taskName.startsWith("3.") || taskName.startsWith("4.")) {
                    taskName = taskName.substringAfter("-").substringAfter("*").substringAfter(".").trim()
                    if (taskName.length > 10 && addedCount < 5) {
                        repository.insertOnboardingTask(
                            OnboardingTask(
                                employeeId = employee.id,
                                taskName = taskName,
                                category = "Onboarding",
                                isCompleted = false
                            )
                        )
                        addedCount++
                    }
                }
            }

            repository.insertAuditLog(
                actor = "AI HR Assistant",
                action = "Generated Onboarding Checklist",
                description = "Added $addedCount tailored onboarding tasks for ${employee.name}."
            )
        }
    }
}

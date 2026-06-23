package com.example.data.repository

import com.example.data.database.HrDao
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

class HrRepository(private val hrDao: HrDao) {

    // --- Observable Flows ---
    val allEmployees: Flow<List<Employee>> = hrDao.getAllEmployees()
    val allAttendance: Flow<List<Attendance>> = hrDao.getAllAttendance()
    val allLeaveRequests: Flow<List<LeaveRequest>> = hrDao.getAllLeaveRequests()
    val allCandidates: Flow<List<Candidate>> = hrDao.getAllCandidates()
    val allPerformance: Flow<List<Performance>> = hrDao.getAllPerformance()
    val allAuditLogs: Flow<List<AuditLog>> = hrDao.getAllAuditLogs()
    val allAnnouncements: Flow<List<Announcement>> = hrDao.getAllAnnouncements()
    val allGoals: Flow<List<Goal>> = hrDao.getAllGoals()
    val allFeedback: Flow<List<ContinuousFeedback>> = hrDao.getAllFeedback()
    val allProfileUpdates: Flow<List<ProfileUpdateRequest>> = hrDao.getAllProfileUpdates()

    // --- Employee Operations ---
    suspend fun getEmployeeById(id: Int): Employee? = hrDao.getEmployeeById(id)
    
    suspend fun insertEmployee(employee: Employee): Long {
        val id = hrDao.insertEmployee(employee)
        insertAuditLog("Employee Registry", "Created Employee Profile", "Added profile for ${employee.name} (${employee.role})")
        return id
    }

    suspend fun updateEmployee(employee: Employee) {
        hrDao.updateEmployee(employee)
        insertAuditLog("Employee Registry", "Updated Employee Profile", "Modified employee data for ${employee.name} (${employee.role})")
    }

    suspend fun deleteEmployee(id: Int, employeeName: String) {
        hrDao.deleteEmployeeById(id)
        insertAuditLog("Employee Registry", "Deleted Employee Profile", "Removed record for employee ID #$id (Name: $employeeName)")
    }

    // --- Attendance Operations ---
    fun getAttendanceForEmployee(employeeId: Int): Flow<List<Attendance>> = hrDao.getAttendanceForEmployee(employeeId)
    
    suspend fun checkInEmployee(employeeId: Int, employeeName: String, date: String, checkInTime: String) {
        val existing = hrDao.getAttendanceForEmployeeToday(employeeId, date)
        if (existing == null) {
            val attendance = Attendance(
                employeeId = employeeId,
                employeeName = employeeName,
                date = date,
                checkIn = checkInTime,
                checkOut = null,
                status = "Present"
            )
            hrDao.insertAttendance(attendance)
            insertAuditLog("Attendance", "Check-In Recorded", "Recorded check-in for $employeeName at $checkInTime")
        }
    }

    suspend fun checkOutEmployee(employeeId: Int, date: String, checkOutTime: String) {
        val existing = hrDao.getAttendanceForEmployeeToday(employeeId, date)
        if (existing != null && existing.checkOut == null) {
            val updated = existing.copy(checkOut = checkOutTime)
            hrDao.updateAttendance(updated)
            insertAuditLog("Attendance", "Check-Out Recorded", "Recorded check-out for ${existing.employeeName} at $checkOutTime")
        }
    }

    // --- Leave / claims Operations ---
    fun getLeaveRequestsForEmployee(employeeId: Int): Flow<List<LeaveRequest>> = hrDao.getLeaveRequestsForEmployee(employeeId)
    
    suspend fun createLeaveRequest(request: LeaveRequest): Long {
        val id = hrDao.insertLeaveRequest(request)
        val description = if (request.leaveType == "Claim") {
            "Submitted reimbursement claim of $${request.amount} for ${request.claimType}"
        } else {
            "Requested leave from ${request.startDate} to ${request.endDate} (Reason: ${request.reason})"
        }
        insertAuditLog("Leave & Claims", "New Request Submitted", "${request.employeeName} submitted a ${request.leaveType} request. Reason: ${request.reason}")
        return id
    }

    suspend fun approveLeaveRequest(requestId: Int, requestName: String, leaveType: String, managerName: String) {
        hrDao.updateLeaveStatus(requestId, "Approved")
        insertAuditLog("Leave & Claims", "Request Approved", "$managerName approved the $leaveType request submitted by $requestName (ID: $requestId)")
    }

    suspend fun rejectLeaveRequest(requestId: Int, requestName: String, leaveType: String, managerName: String) {
        hrDao.updateLeaveStatus(requestId, "Rejected")
        insertAuditLog("Leave & Claims", "Request Rejected", "$managerName rejected the $leaveType request submitted by $requestName (ID: $requestId)")
    }

    // --- Recruitment Operations ---
    suspend fun insertCandidate(candidate: Candidate): Long {
        val id = hrDao.insertCandidate(candidate)
        insertAuditLog("Recruitment", "Candidate Added", "Registered new candidate ${candidate.name} for position ${candidate.position}")
        return id
    }

    suspend fun updateCandidateStatus(id: Int, name: String, status: String) {
        hrDao.updateCandidateStatus(id, status)
        insertAuditLog("Recruitment", "Candidate Status Changed", "Moved candidate $name (ID: $id) status to '$status'")
    }

    suspend fun deleteCandidate(id: Int, name: String) {
        hrDao.deleteCandidateById(id)
        insertAuditLog("Recruitment", "Candidate Removed", "Deleted interview candidate profile for $name")
    }

    // --- Performance Appraisal Operations ---
    fun getPerformanceForEmployee(employeeId: Int): Flow<List<Performance>> = hrDao.getPerformanceForEmployee(employeeId)
    
    suspend fun insertPerformance(performance: Performance): Long {
        val id = hrDao.insertPerformance(performance)
        insertAuditLog("Performance Appraisals", "Evaluation Filed", "Submitted evaluation and KPI score of ${performance.kpiProgress}% for ${performance.employeeName}")
        return id
    }

    // --- Onboarding / Offboarding Tasks ---
    fun getTasksForEmployee(employeeId: Int): Flow<List<OnboardingTask>> = hrDao.getTasksForEmployee(employeeId)
    
    suspend fun insertOnboardingTask(task: OnboardingTask) = hrDao.insertOnboardingTask(task)
    
    suspend fun toggleTaskCompletion(task: OnboardingTask, actorName: String) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        hrDao.updateOnboardingTask(updated)
        val term = if(task.category == "Onboarding") "Onboarding" else "Offboarding"
        insertAuditLog("Onboarding", "$term Task Modified", "$actorName marked task '${task.taskName}' as ${if(updated.isCompleted) "Completed" else "Incomplete"}")
    }

    // --- Audit Logging Operations ---
    suspend fun insertAuditLog(actor: String, action: String, description: String) {
        val log = AuditLog(
            timestamp = System.currentTimeMillis(),
            actor = actor,
            action = action,
            description = description
        )
        hrDao.insertAuditLog(log)
    }

    // --- Announcement Operations ---
    suspend fun insertAnnouncement(announcement: Announcement): Long {
        val id = hrDao.insertAnnouncement(announcement)
        insertAuditLog("Announcements", "Created Announcement", "Added critical company news '${announcement.title}'")
        return id
    }

    // --- Goals / OKR Operations ---
    fun getGoalsForEmployee(employeeId: Int): Flow<List<Goal>> = hrDao.getGoalsForEmployee(employeeId)

    suspend fun insertGoal(goal: Goal): Long {
        val id = hrDao.insertGoal(goal)
        insertAuditLog("OKR Management", "Created Goal", "Created OKR Goal for ${goal.employeeName}: '${goal.objective}'")
        return id
    }

    suspend fun updateGoalProgress(goalId: Int, progress: Int, status: String, employeeName: String) {
        hrDao.updateGoalProgress(goalId, progress, status)
        insertAuditLog("OKR Management", "Updated Goal Progress", "Updated Goal progress to ${progress}% [Status: $status] for $employeeName")
    }

    // --- Continuous Feedback Operations ---
    fun getFeedbackForEmployee(employeeId: Int): Flow<List<ContinuousFeedback>> = hrDao.getFeedbackForEmployee(employeeId)

    suspend fun insertFeedback(feedback: ContinuousFeedback): Long {
        val id = hrDao.insertFeedback(feedback)
        insertAuditLog("Appraisals & Feedback", "Continuous Feedback Received", "Continuous feedback delivered to ${feedback.employeeName} by ${feedback.providerName}")
        return id
    }

    // --- Profile Update Approval Operations ---
    fun getProfileUpdatesForEmployee(employeeId: Int): Flow<List<ProfileUpdateRequest>> = hrDao.getProfileUpdatesForEmployee(employeeId)

    suspend fun createProfileUpdateRequest(request: ProfileUpdateRequest): Long {
        val id = hrDao.insertProfileUpdate(request)
        insertAuditLog("Employee Self-Service", "Profile Update Requested", "${request.employeeName} requested editing '${request.fieldName}' to '${request.newValue}'")
        return id
    }

    suspend fun approveProfileUpdateRequest(request: ProfileUpdateRequest, reviewerName: String) {
        hrDao.updateProfileStatus(request.id, "Approved")
        // Retrieve full employee and update their fields
        val employee = hrDao.getEmployeeById(request.employeeId)
        if (employee != null) {
            val updatedEmployee = when (request.fieldName) {
                "Phone" -> employee.copy(phone = request.newValue)
                "Email" -> employee.copy(email = request.newValue)
                "Department" -> employee.copy(department = request.newValue)
                "Role" -> employee.copy(role = request.newValue)
                "Notes" -> employee.copy(notes = request.newValue)
                else -> employee
            }
            hrDao.updateEmployee(updatedEmployee)
            insertAuditLog("Employee Self-Service", "Profile Update Approved", "Approved '${request.fieldName}' change for ${request.employeeName}. Database updated.")
        }
    }

    suspend fun rejectProfileUpdateRequest(requestId: Int, employeeName: String, fieldName: String, reviewerName: String) {
        hrDao.updateProfileStatus(requestId, "Rejected")
        insertAuditLog("Employee Self-Service", "Profile Update Rejected", "Rejected '${fieldName}' change request for $employeeName")
    }

    // --- Automatic Pre-Seeder ---
    suspend fun checkAndSeedDatabase() {
        val currentEmployees = allEmployees.first()
        if (currentEmployees.isNotEmpty()) return // Already seeded

        // 1. Initial Employees
        val seedEmployees = listOf(
            Employee(
                name = "Sarah Jenkins",
                email = "sarah.jenkins@enterprise.com",
                phone = "+1 (555) 019-2834",
                department = "HR Department",
                role = "HR Director",
                userRole = "HR Admin",
                hireDate = "2021-04-12",
                salary = 125000.0,
                status = "Active",
                notes = "Responsible for HR operations, structural compliance, policy auditing, and talent acquisition strategies."
            ),
            Employee(
                name = "Marcus Chen",
                email = "m.chen@enterprise.com",
                phone = "+1 (555) 019-8877",
                department = "Engineering",
                role = "Senior Engineering Lead",
                userRole = "Manager",
                hireDate = "2022-09-01",
                salary = 145000.0,
                status = "Active",
                notes = "Core architect of cloud services. Manages developers, sprint scheduling, and architectural reviews."
            ),
            Employee(
                name = "Emily Watson",
                email = "emily.w@enterprise.com",
                phone = "+1 (555) 019-3421",
                department = "Engineering",
                role = "Senior Android Developer",
                userRole = "Employee",
                hireDate = "2024-02-15",
                salary = 110000.0,
                status = "Active",
                notes = "Skilled in Jetpack Compose, modular clean-architecture development, and offline databases."
            ),
            Employee(
                name = "David Kojo",
                email = "david.k@enterprise.com",
                phone = "+1 (555) 019-4560",
                department = "Product & Design",
                role = "Product Owner",
                userRole = "Manager",
                hireDate = "2023-01-10",
                salary = 115000.0,
                status = "Active",
                notes = "Directs roadmap targets, customer discovery cycles, wireframes, and design approvals."
            ),
            Employee(
                name = "Clara Dupont",
                email = "clara.dupont@enterprise.com",
                phone = "+1 (555) 019-9921",
                department = "HR Department",
                role = "Onboarding Specialist",
                userRole = "Employee",
                hireDate = "2025-05-18",
                salary = 75000.0,
                status = "Active",
                notes = "Coordinates checklists, documents storage, compliance forms, and laptop provisions."
            ),
            Employee(
                name = "Zoe Henderson",
                email = "zoe.h@enterprise.com",
                phone = "+1 (555) 019-6112",
                department = "Marketing",
                role = "Growth Manager",
                userRole = "Employee",
                hireDate = "2026-06-01",
                salary = 82000.0,
                status = "Onboarding",
                notes = "Ad campaigns coordinator. Currently undergoing safety onboarding and brand orientation."
            )
        )

        val insertedIds = mutableListOf<Int>()
        for (emp in seedEmployees) {
            val id = hrDao.insertEmployee(emp).toInt()
            insertedIds.add(id)
        }

        // 2. Initial Attendance
        val seedAttendance = listOf(
            Attendance(employeeId = insertedIds[0], employeeName = "Sarah Jenkins", date = "2026-06-22", checkIn = "08:45 AM", checkOut = "05:30 PM", status = "Present"),
            Attendance(employeeId = insertedIds[1], employeeName = "Marcus Chen", date = "2026-06-22", checkIn = "09:12 AM", checkOut = "06:00 PM", status = "Late"),
            Attendance(employeeId = insertedIds[2], employeeName = "Emily Watson", date = "2026-06-22", checkIn = "08:58 AM", checkOut = "05:15 PM", status = "Present"),
            Attendance(employeeId = insertedIds[3], employeeName = "David Kojo", date = "2026-06-22", checkIn = "09:00 AM", checkOut = "05:45 PM", status = "Present"),
            Attendance(employeeId = insertedIds[4], employeeName = "Clara Dupont", date = "2026-06-22", checkIn = "08:30 AM", checkOut = "05:00 PM", status = "Present"),
            
            // Today's active stamps (not checked out yet)
            Attendance(employeeId = insertedIds[0], employeeName = "Sarah Jenkins", date = "2026-06-23", checkIn = "08:51 AM", checkOut = null, status = "Present"),
            Attendance(employeeId = insertedIds[2], employeeName = "Emily Watson", date = "2026-06-23", checkIn = "08:55 AM", checkOut = null, status = "Present"),
            Attendance(employeeId = insertedIds[3], employeeName = "David Kojo", date = "2026-06-23", checkIn = "09:02 AM", checkOut = null, status = "Present")
        )
        for (att in seedAttendance) {
            hrDao.insertAttendance(att)
        }

        // 3. Initial Leave Requests & Claims
        val seedRequests = listOf(
            LeaveRequest(employeeId = insertedIds[1], employeeName = "Marcus Chen", leaveType = "Annual", startDate = "2026-07-02", endDate = "2026-07-10", reason = "Summer family trip", status = "Pending"),
            LeaveRequest(employeeId = insertedIds[2], employeeName = "Emily Watson", leaveType = "Sick", startDate = "2026-06-12", endDate = "2026-06-13", reason = "Dental operation", status = "Approved"),
            LeaveRequest(employeeId = insertedIds[3], employeeName = "David Kojo", leaveType = "Claim", startDate = "2026-06-15", endDate = "2026-06-15", reason = "Client networking dinner", amount = 148.50, claimType = "Entertainment", status = "Pending"),
            LeaveRequest(employeeId = insertedIds[2], employeeName = "Emily Watson", leaveType = "Claim", startDate = "2026-06-20", endDate = "2026-06-20", reason = "AWS Associate Exam fee", amount = 150.00, claimType = "Development", status = "Approved")
        )
        for (req in seedRequests) {
            hrDao.insertLeaveRequest(req)
        }

        // 4. Initial Recruitment Candidates
        val seedCandidates = listOf(
            Candidate(
                name = "Julian Alvarez",
                email = "julian.alvarez@gmail.com",
                position = "Lead Cloud Security Specialist",
                status = "Interviewing",
                resumeSummary = "Certified AWS Solutions Architect Pro with 8 years of serverless security experience. Handled SOC2 audits and automated Kubernetes cluster scanning.",
                score = 88,
                screenNotes = "Excellent knowledge in networking IAM. Strong leadership capacity."
            ),
            Candidate(
                name = "Aisha Rahman",
                email = "aisha.rahman@outlook.com",
                position = "UI/UX Designer",
                status = "Applied",
                resumeSummary = "Highly creative Figma designer specializing in interactive prototypes, Design Tokens mapping, dynamic product onboarding, and Material Design 3 guidelines.",
                score = 94,
                screenNotes = "Highly analytical portfolio. Applied M3 styling in detail."
            ),
            Candidate(
                name = "Thomas Mueller",
                email = "t.mueller@techcorp.de",
                position = "Senior Mobile Engineer (Android)",
                status = "Offered",
                resumeSummary = "Expert Android Engineer, expert in Kotlin, Room database design, custom Jetpack Compose canvases, memory leak inspections, and CI/CD automation pipelines.",
                score = 91,
                screenNotes = "Technical fit verified. Offer letter delivered."
            )
        )
        for (cand in seedCandidates) {
            hrDao.insertCandidate(cand)
        }

        // 5. Initial Appraisals (Performance)
        val seedPerformance = listOf(
            Performance(employeeId = insertedIds[1], employeeName = "Marcus Chen", rating = 4.8f, achievements = "Successfully migrated storage system to serverless architecture, improving loading cycles by 40% and cutting operating costs.", areasOfImprovement = "Continue developing junior engineering team leads in system design.", kpiProgress = 95, cycle = "H1 2026", date = "2026-06-15"),
            Performance(employeeId = insertedIds[2], employeeName = "Emily Watson", rating = 4.5f, achievements = "Rebuilt core mobile dashboards with declarative layouts. Fixed 14 legacy performance lag issues.", areasOfImprovement = "Active participation in API architectural design cycles.", kpiProgress = 90, cycle = "H1 2026", date = "2026-06-18")
        )
        for (perf in seedPerformance) {
            hrDao.insertPerformance(perf)
        }

        // 6. Onboarding checklists
        val onboardingEmployeeId = insertedIds[5] // Zoe Henderson is "Onboarding"
        val onboardingTasks = listOf(
            OnboardingTask(employeeId = onboardingEmployeeId, taskName = "Sign Employee Contract & Offer Letter", category = "Onboarding", isCompleted = true),
            OnboardingTask(employeeId = onboardingEmployeeId, taskName = "Submit Medical Insurance Registration Form", category = "Onboarding", isCompleted = true),
            OnboardingTask(employeeId = onboardingEmployeeId, taskName = "Configure Dev Laptop & Install IDE Packages", category = "Onboarding", isCompleted = false),
            OnboardingTask(employeeId = onboardingEmployeeId, taskName = "Schedule Orientation sync with HR Sarah", category = "Onboarding", isCompleted = false),
            OnboardingTask(employeeId = onboardingEmployeeId, taskName = "Review Codebase Security Policies Document", category = "Onboarding", isCompleted = false)
        )
        for (task in onboardingTasks) {
            hrDao.insertOnboardingTask(task)
        }

        // 7. Initial Audit logs
        val sampleAuditLogs = listOf(
            AuditLog(timestamp = System.currentTimeMillis() - 7200000, actor = "System Sync", action = "Database Initialized", description = "Successfully activated tables and generated seed data for first evaluation."),
            AuditLog(timestamp = System.currentTimeMillis() - 3600000, actor = "Sarah Jenkins", action = "Created Employee Profile", description = "Formally added Zoe Henderson to the system under the Marketing department as Onboarding status.")
        )
        for (log in sampleAuditLogs) {
            hrDao.insertAuditLog(log)
        }

        // 8. Seed Announcements
        val seedAnnouncements = listOf(
            Announcement(
                title = "New Employee Self-Service Core Activated",
                content = "We have fully upgraded our HR Pulse corporate workspace! You can now access your encrypted digital payslips, update contact info with manager approval, review OKRs, provide continuous feedback, and check company announcements directly.",
                date = "2026-06-23",
                author = "Sarah Jenkins"
            ),
            Announcement(
                title = "Q3 Strategy Presentation & Virtual Town Hall",
                content = "Join us this Friday at 3:00 PM EST for our hybrid Q3 strategy kick-off. The corporate leadership group will be sharing our upcoming product roadmaps, active revenue scales, and specific department milestones.",
                date = "2026-06-21",
                author = "Sarah Jenkins"
            )
        )
        for (ann in seedAnnouncements) {
            hrDao.insertAnnouncement(ann)
        }

        // 9. Seed Corporate Goals / OKRs
        val seedGoals = listOf(
            Goal(
                employeeId = insertedIds[2], // Emily Watson
                employeeName = "Emily Watson",
                objective = "Launch Android Core Redesign with Jetpack Compose",
                keyResults = "Migrate 100% of user profile pages, maintain recomposition count under 12, achieve 90% code coverage",
                progress = 75,
                cycle = "H1 2026",
                status = "Active"
            ),
            Goal(
                employeeId = insertedIds[2], // Emily Watson
                employeeName = "Emily Watson",
                objective = "Optimize Database Startup Speeds",
                keyResults = "Benchmark cold startup time under 1.2s, structure indexes for Room entities, remove redundant joins",
                progress = 40,
                cycle = "H1 2026",
                status = "Active"
            ),
            Goal(
                employeeId = insertedIds[1], // Marcus Chen
                employeeName = "Marcus Chen",
                objective = "Scale Enterprise API Server Architecture",
                keyResults = "Implement multi-region database replication, guarantee 95th latency under 120ms, execute SOC2 dry audit",
                progress = 100,
                cycle = "H1 2026",
                status = "Completed"
            )
        )
        for (goal in seedGoals) {
            hrDao.insertGoal(goal)
        }

        // 10. Seed Continuous Feedback
        val seedFeedback = listOf(
            ContinuousFeedback(
                employeeId = insertedIds[2], // Emily Watson
                employeeName = "Emily Watson",
                providerName = "Marcus Chen",
                feedbackText = "Emily displayed exceptional focus while developing the Material 3 design template updates. Her work on mobile density scaling and theme integration was outstanding!",
                date = "2026-06-20"
            ),
            ContinuousFeedback(
                employeeId = insertedIds[2], // Emily Watson
                employeeName = "Emily Watson",
                providerName = "David Kojo",
                feedbackText = "Always highly responsive, constructive in team handovers, and reviews codes with meticulous attention. Excellent work leading the current module restructure.",
                date = "2026-06-22"
            )
        )
        for (fb in seedFeedback) {
            hrDao.insertFeedback(fb)
        }

        // 11. Seed Profile Update Requests
        val seedProfileRequests = listOf(
            ProfileUpdateRequest(
                employeeId = insertedIds[2], // Emily Watson
                employeeName = "Emily Watson",
                fieldName = "Phone",
                oldValue = "+1 (555) 019-3421",
                newValue = "+1 (555) 019-7800",
                status = "Pending"
            )
        )
        for (pr in seedProfileRequests) {
            hrDao.insertProfileUpdate(pr)
        }
    }
}

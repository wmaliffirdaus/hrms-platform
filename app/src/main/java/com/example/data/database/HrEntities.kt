package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val department: String,
    val role: String,
    val userRole: String, // "HR Admin", "Manager", "Employee"
    val hireDate: String,
    val salary: Double,
    val status: String, // "Active", "Onboarding", "Offboarding", "Suspended"
    val documentUrl: String = "", // Document reference path
    val notes: String = ""
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val date: String, // YYYY-MM-DD
    val checkIn: String,
    val checkOut: String?,
    val status: String // "Present", "Late", "Absent", "On Leave"
)

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val leaveType: String, // "Annual", "Sick", "Maternity", "Unpaid", "Claim"
    val startDate: String,
    val endDate: String,
    val reason: String,
    val amount: Double = 0.0, // Used for Claims, otherwise 0.0
    val claimType: String? = null, // e.g., "Travel", "Medical", "Entertainment" (if Claim, else null)
    val status: String = "Pending" // "Pending", "Approved", "Rejected"
)

@Entity(tableName = "candidates")
data class Candidate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val position: String,
    val status: String = "Applied", // "Applied", "Contacted", "Interviewing", "Offered", "Rejected"
    val resumeSummary: String,
    val score: Int = 0, // AI rating score or manual metric
    val screenNotes: String = ""
)

@Entity(tableName = "performance")
data class Performance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val rating: Float, // 1.0 - 5.0
    val achievements: String,
    val areasOfImprovement: String,
    val kpiProgress: Int, // 0 - 100
    val cycle: String, // "Q1 2026", "H1 2026", "2026 Annual"
    val date: String
)

@Entity(tableName = "onboarding_tasks")
data class OnboardingTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val taskName: String,
    val category: String, // "Onboarding", "Offboarding"
    val isCompleted: Boolean = false
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val actor: String,
    val action: String,
    val description: String
)

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: String,
    val author: String
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val objective: String,
    val keyResults: String,
    val progress: Int = 0,
    val cycle: String,
    val status: String = "Active" // "Active", "Completed", "Behind"
)

@Entity(tableName = "continuous_feedback")
data class ContinuousFeedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val providerName: String,
    val feedbackText: String,
    val date: String
)

@Entity(tableName = "profile_updates")
data class ProfileUpdateRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val fieldName: String,
    val oldValue: String,
    val newValue: String,
    val status: String = "Pending" // "Pending", "Approved", "Rejected"
)


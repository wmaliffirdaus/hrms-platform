package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HrDao {

    // --- Employee Queries ---
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Int): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: Int)


    // --- Attendance Queries ---
    @Query("SELECT * FROM attendance ORDER BY date DESC, checkIn DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAttendanceForEmployee(employeeId: Int): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId AND date = :date LIMIT 1")
    suspend fun getAttendanceForEmployeeToday(employeeId: Int, date: String): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Update
    suspend fun updateAttendance(attendance: Attendance)


    // --- Leave & Claim Queries ---
    @Query("SELECT * FROM leave_requests ORDER BY id DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getLeaveRequestsForEmployee(employeeId: Int): Flow<List<LeaveRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequest): Long

    @Query("UPDATE leave_requests SET status = :status WHERE id = :requestId")
    suspend fun updateLeaveStatus(requestId: Int, status: String)


    // --- Recruitment (Athlete Tracker System / ATS) ---
    @Query("SELECT * FROM candidates ORDER BY score DESC, name ASC")
    fun getAllCandidates(): Flow<List<Candidate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: Candidate): Long

    @Query("UPDATE candidates SET status = :status WHERE id = :id")
    suspend fun updateCandidateStatus(id: Int, status: String)

    @Query("DELETE FROM candidates WHERE id = :id")
    suspend fun deleteCandidateById(id: Int)


    // --- Performance (Appraisal Tasks) ---
    @Query("SELECT * FROM performance ORDER BY date DESC")
    fun getAllPerformance(): Flow<List<Performance>>

    @Query("SELECT * FROM performance WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getPerformanceForEmployee(employeeId: Int): Flow<List<Performance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformance(performance: Performance): Long


    // --- Onboarding / Offboarding Checklist ---
    @Query("SELECT * FROM onboarding_tasks WHERE employeeId = :employeeId ORDER BY id ASC")
    fun getTasksForEmployee(employeeId: Int): Flow<List<OnboardingTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnboardingTask(task: OnboardingTask): Long

    @Update
    suspend fun updateOnboardingTask(task: OnboardingTask)


    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog): Long


    // --- Announcements ---
    @Query("SELECT * FROM announcements ORDER BY date DESC, id DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement): Long


    // --- Goals / OKRs ---
    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getGoalsForEmployee(employeeId: Int): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Query("UPDATE goals SET progress = :progress, status = :status WHERE id = :goalId")
    suspend fun updateGoalProgress(goalId: Int, progress: Int, status: String)


    // --- Continuous Feedback ---
    @Query("SELECT * FROM continuous_feedback ORDER BY date DESC, id DESC")
    fun getAllFeedback(): Flow<List<ContinuousFeedback>>

    @Query("SELECT * FROM continuous_feedback WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getFeedbackForEmployee(employeeId: Int): Flow<List<ContinuousFeedback>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: ContinuousFeedback): Long


    // --- Profile Update Requests ---
    @Query("SELECT * FROM profile_updates ORDER BY id DESC")
    fun getAllProfileUpdates(): Flow<List<ProfileUpdateRequest>>

    @Query("SELECT * FROM profile_updates WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getProfileUpdatesForEmployee(employeeId: Int): Flow<List<ProfileUpdateRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileUpdate(request: ProfileUpdateRequest): Long

    @Query("UPDATE profile_updates SET status = :status WHERE id = :id")
    suspend fun updateProfileStatus(id: Int, status: String)
}

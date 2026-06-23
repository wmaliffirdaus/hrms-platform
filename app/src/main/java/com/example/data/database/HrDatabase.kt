package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Employee::class,
        Attendance::class,
        LeaveRequest::class,
        Candidate::class,
        Performance::class,
        OnboardingTask::class,
        AuditLog::class,
        Announcement::class,
        Goal::class,
        ContinuousFeedback::class,
        ProfileUpdateRequest::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HrDatabase : RoomDatabase() {
    abstract fun hrDao(): HrDao

    companion object {
        @Volatile
        private var INSTANCE: HrDatabase? = null

        fun getDatabase(context: Context): HrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HrDatabase::class.java,
                    "hr_pulse_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

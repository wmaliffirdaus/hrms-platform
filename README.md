<div align="center">
  <img width="1200" height="475" alt="Smart HRMS Banner" src="https://images.unsplash.com/photo-1552664730-d307ca884978?q=80&w=1200" />

# Smart HRMS

Modern Human Resource Management System (HRMS) designed to streamline employee management, attendance tracking, leave approvals, recruitment, and workforce analytics.

[![Status](https://img.shields.io/badge/Status-Active-success)]()
[![Platform](https://img.shields.io/badge/Platform-Android-green)]()
[![License](https://img.shields.io/badge/License-MIT-blue)]()

</div>

---

## Overview

Smart HRMS is a centralized Human Resource Management System that helps organizations manage their workforce efficiently through a single platform.

The application simplifies HR operations by providing tools for employee record management, attendance monitoring, leave processing, recruitment tracking, performance evaluation, and organizational reporting.

---

## Features

### Employee Management
- Employee profiles
- Employment history
- Document management
- Department & designation assignment

### Attendance Management
- Daily attendance tracking
- Clock-in / Clock-out records
- Attendance reports
- Late arrival monitoring

### Leave Management
- Leave application submission
- Approval workflow
- Leave balance tracking
- Leave history

### Recruitment
- Candidate tracking
- Interview scheduling
- Applicant status management
- Hiring workflow

### Performance Management
- KPI tracking
- Performance reviews
- Employee evaluations
- Goal management

### Analytics Dashboard
- Employee statistics
- Attendance insights
- Leave analytics
- Workforce reports

### Security
- Authentication & Authorization
- Role-Based Access Control (RBAC)
- Audit logs
- Secure employee data handling

---

## Tech Stack

### Frontend
- Android (Kotlin)
- Jetpack Compose
- Material Design 3

### Backend
- REST API
- Firebase / Flask / Spring Boot *(update accordingly)*

### Database
- PostgreSQL / MySQL / Firebase Firestore *(update accordingly)*

### Architecture
- MVVM Architecture
- Repository Pattern
- Clean Architecture Principles

---

## Project Structure

```text
├── app/
│   ├── src/
│   │   ├── androidTest/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── example/
│   │   │   │           ├── api/
│   │   │   │           │   └── GeminiHelper.kt
│   │   │   │           ├── data/
│   │   │   │           │   ├── database/
│   │   │   │           │   │   ├── HrDao.kt
│   │   │   │           │   │   ├── HrDatabase.kt
│   │   │   │           │   │   └── HrEntities.kt
│   │   │   │           │   └── repository/
│   │   │   │           │       └── HrRepository.kt
│   │   │   │           ├── ui/
│   │   │   │           │   ├── screens/
│   │   │   │           │   │   └── HrMainLayout.kt
│   │   │   │           │   ├── theme/
│   │   │   │           │   │   ├── Color.kt
│   │   │   │           │   │   ├── Theme.kt
│   │   │   │           │   │   └── Type.kt
│   │   │   │           │   └── viewmodel/
│   │   │   │           │       └── HrViewModel.kt
│   │   │   │           └── MainActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   ├── mipmap-anydpi-v26/
│   │   │   │   ├── mipmap-hdpi/
│   │   │   │   ├── mipmap-mdpi/
│   │   │   │   ├── mipmap-xhdpi/
│   │   │   │   ├── mipmap-xxhdpi/
│   │   │   │   ├── mipmap-xxxhdpi/
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── example/
│   │                   ├── ExampleRobolectricTest.kt
│   │                   ├── ExampleUnitTest.kt
│   │                   └── GreetingScreenshotTest.kt
│   ├── screenshots/
│   ├── .gitignore
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── assets/
├── gradle/
├── .env.example
├── .gitignore
├── build.gradle.kts
├── gradle.properties
├── local.properties
├── metadata.json
└── settings.gradle.kts

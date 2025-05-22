# 🏥 SmartClinic - Health Appointment Booking System

**SmartClinic** is a robust Spring Boot-based platform that allows patients to book appointments with doctors, receive medical reports, and manage health-related services seamlessly.

---

## 🚀 Features

### 👤 User & Profile Management
- User registration and login with secure password hashing
- Role-based access: Admin, Doctor, Patient
- One-to-one profile with extended info (e.g., phone, gender, photo)

### 🧑‍⚕️ Doctor Module
- Apply as a doctor with specialty and location
- View/manage upcoming appointments
- Set availability (time slots)
- Create medical reports for patients
- Get approved/rejected by admin

### 🧑‍💼 Patient Module
- Browse doctors by specialty/location
- Book, cancel, and view appointment history
- Access medical reports issued by doctors

### 📅 Appointment Module
- Book appointment based on doctor's availability
- Status updates: Pending, Accepted, Rejected, Completed
- Automated appointment lifecycle
- Optional: Email notifications (coming soon)

### 📝 Medical Report Module
- Doctor creates medical reports per appointment
- Patients can download/view their reports
- Stored securely per patient and appointment

### 🗓 Availability Module
- Doctor can manage their open slots
- System prevents double booking
- Used during appointment booking logic

---

## 🧠 Entity Overview

| Entity         | Description                                                |
|----------------|------------------------------------------------------------|
| `User`         | Base entity for all users (email, password, role)          |
| `Doctor`       | Extends User with specialty, location, availability        |
| `Patient`      | Extends User with patient-specific info                    |
| `Appointment`  | Links doctor & patient with booking time/status            |
| `MedicalReport`| Created by doctor for a patient after appointment          |
| `Availability` | Time slots offered by doctor for appointments              |
| `Profile`      | One-to-one extension of User (bio, phone, DOB, etc.)       |
| `Specialty`    | Represents medical specialization (e.g., Cardiology)       |

---

## 🛠️ Tech Stack

| Layer       | Tech                              |
|-------------|------------------------------------|
| Backend     | Java 21, Spring Boot 3, JPA, Hibernate |
| Database    | MySQL / PostgreSQL (Your choice)   |
| Security    | Spring Security with JWT Auth      |
| Build Tool  | Maven or Gradle                    |
| Testing     | JUnit, Mockito                     |
| Optional    | Docker, Swagger, Email Notifications |

---


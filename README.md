# MotorPH Payroll System

This repository contains the Phase 1 implementation of the MotorPH Payroll System. It automates employee attendance tracking and weekly salary computation with mandatory deductions.

---

## MO-IT101-Group7

### Team Members:
- **Deborah Beulah Cruz**
- **Abigail Agnes**
- **Aila May Salasbar**
- **Czarmaine Althea Barcillano**

---
### Team Details
- **Deborah  - Code (Payroll System)**
- **Czarmaine - Code (Deductions and Salary)**
- **Abigail - GitHub**
- **Aila - GitHub**

## Program Details

The MotorPh Payroll System is a Java-based application that manages employee payroll and attendance.

### System Overview

**Data Storage**: Uses CSV files for data persistence
- `employees.csv`: Contains employee information (ID, name, birthday, hourly rate)
- `attendance.csv`: Records daily attendance (employee ID, date, time in/out)

**Core Components**:
- Two Java implementations with different approaches
- Authentication system with username/password validation
- Employee data lookup from CSV files
- Attendance tracking and hours calculation
- Payroll computation based on hourly rates

### Key Features

**Authentication**: Users login with credentials (employee/payroll_staff with password "12345")

**Employee Management**:
- Retrieves employee details by ID from CSV
- Displays employee information including hourly rates

**Attendance Processing**:
- Tracks daily time in/out with 10-minute grace period
- Calculates total weekly hours worked
- Processes attendance data from CSV records

**Payroll Calculation**:
- Computes gross pay based on hours worked × hourly rate
- Handles different employee categories with varying rates

The system demonstrates a basic but functional payroll management approach using file-based data storage and Java console interface for processing employee compensation.

**Project Plan Link**:
- https://docs.google.com/spreadsheets/d/1kEkYppuvYbNMyqkV7KXTGTbvrPSwtjCK/edit?gid=1826980881#gid=1826980881

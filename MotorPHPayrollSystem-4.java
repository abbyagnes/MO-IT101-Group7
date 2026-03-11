package com.mycompany.motorphpayrollsystem;

import java.io.*;
import java.util.*;

public class MotorPHPayrollSystem {

    // Default password for both employee and payroll staff
    static final String PASSWORD = "12345";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Step 1: Login validation
        if (!login(sc)) {
            System.out.println("Incorrect username or password.");
            return;
        }

        // Step 2: Ask for employee ID
        System.out.print("Employee ID: ");
        String id = sc.nextLine().trim();

        // Step 3: Get employee information from CSV
        String[] employeeInfo = getEmployeeInfo(id);

        if (employeeInfo == null) {
            System.out.println("Employee not found.");
            return;
        }

        String name = employeeInfo[0];
        String birthday = employeeInfo[1];
        double rate = Double.parseDouble(employeeInfo[2]);

        // Step 4: Compute working hours
        double hours = computeHours(id);

        // Step 5: Compute payroll values
        double gross = hours * rate;

        double sss = (gross > 0) ? computeSSS(gross) : 0;
        double philhealth = gross * 0.015;
        double pagibig = Math.min(gross * 0.02, 100);
        double tax = computeTax(gross);

        double deductions = sss + philhealth + pagibig + tax;
        double net = gross - deductions;

        // Step 6: Display payroll summary
        displayPayroll(id, name, birthday, hours, gross, sss, philhealth, pagibig, tax, net);

        sc.close();
    }

    // Handles login validation
    public static boolean login(Scanner sc) {

        System.out.print("Username: ");
        String user = sc.nextLine().trim();

        System.out.print("Password: ");
        String pass = sc.nextLine().trim();

        // Only allow employee or payroll_staff accounts
        return (user.equals("employee") || user.equals("payroll_staff")) && pass.equals(PASSWORD);
    }

    // Reads employee information from employees.csv
    public static String[] getEmployeeInfo(String id) {

        try {

            InputStream file = MotorPHPayrollSystem.class
                    .getClassLoader().getResourceAsStream("employees.csv");

            if (file == null) {
                System.out.println("employees.csv not found.");
                return null;
            }

            BufferedReader emp = new BufferedReader(new InputStreamReader(file));

            emp.readLine(); // Skip header line
            String line;

            while ((line = emp.readLine()) != null) {

                // Split CSV row into columns
                String[] data = line.split(",");

                if (data.length < 4) continue;

                // Check if employee ID matches
                if (data[0].trim().equals(id)) {

                    String name = data[1].trim();
                    String birthday = data[2].trim();
                    String rate = data[3].trim();

                    emp.close();
                    return new String[]{name, birthday, rate};
                }
            }

            emp.close();

        } catch (Exception e) {
            System.out.println("Error reading employees file.");
        }

        return null;
    }

    // Computes total working hours for a specific employee
    public static double computeHours(String id) {

        double totalHours = 0;

        try {

            InputStream file = MotorPHPayrollSystem.class
                    .getClassLoader().getResourceAsStream("attendance.csv");

            if (file == null) {
                System.out.println("attendance.csv not found.");
                return 0;
            }

            BufferedReader att = new BufferedReader(new InputStreamReader(file));

            att.readLine(); // Skip header
            String line;

            while ((line = att.readLine()) != null) {

                String[] data;

                // Attendance file may be tab or comma separated
                if (line.contains("\t")) {
                    data = line.split("\t");
                } else {
                    data = line.split(",");
                }

                if (data.length < 4) continue;

                String empId = data[0].trim();
                String timeIn = data[2].trim();
                String timeOut = data[3].trim();

                // Only process records that match the employee ID
                if (!empId.equals(id)) continue;

                // Add daily working hours to total
                totalHours += calculateDailyHours(timeIn, timeOut);
            }

            att.close();

        } catch (Exception e) {
            System.out.println("Error reading attendance file.");
        }

        return totalHours;
    }

    // Calculates the number of hours worked in one day
    public static double calculateDailyHours(String in, String out) {

        double inTime = convertTime(in);
        double outTime = convertTime(out);

        // Adjust time rules
        if (inTime <= 8.0833) inTime = 8.0;
        if (inTime < 8.0) inTime = 8.0;

        // Do not count hours beyond 5:00 PM
        if (outTime > 17.0) outTime = 17.0;

        double hours = outTime - inTime;

        if (hours < 0) hours = 0;

        return hours;
    }

    // Converts time string (e.g., 8:30:00 AM) into decimal hours
    public static double convertTime(String time) {

        String[] periodSplit = time.toLowerCase().split(" ");
        String[] timeSplit = periodSplit[0].split(":");

        int hour = Integer.parseInt(timeSplit[0]);
        int min = Integer.parseInt(timeSplit[1]);
        int sec = Integer.parseInt(timeSplit[2]);

        if (periodSplit[1].equals("pm") && hour != 12) hour += 12;
        if (periodSplit[1].equals("am") && hour == 12) hour = 0;

        return hour + (min / 60.0) + (sec / 3600.0);
    }

    // Computes SSS deduction based on salary range
    public static double computeSSS(double salary) {

        if (salary < 3250) return 135;
        else if (salary < 3750) return 157.5;
        else if (salary < 4250) return 180;
        else if (salary < 4750) return 202.5;
        else if (salary < 5250) return 225;
        else if (salary < 5750) return 247.5;
        else if (salary < 6250) return 270;
        else if (salary < 6750) return 292.5;
        else if (salary < 7250) return 315;
        else if (salary < 7750) return 337.5;
        else if (salary < 8250) return 360;
        else if (salary < 8750) return 382.5;
        else if (salary < 9250) return 405;
        else if (salary < 9750) return 427.5;
        else if (salary < 10250) return 450;
        else return 1125;
    }

    // Computes income tax based on Philippine tax brackets
    public static double computeTax(double salary) {

        if (salary <= 20833) return 0;
        else if (salary <= 33332) return (salary - 20833) * 0.20;
        else if (salary <= 66666) return 2500 + (salary - 33333) * 0.25;
        else if (salary <= 166666) return 10833 + (salary - 66667) * 0.30;
        else return 40833.33 + (salary - 166667) * 0.32;
    }

    // Displays the final payroll output
    public static void displayPayroll(String id, String name, String birthday,
                                      double hours, double gross,
                                      double sss, double philhealth,
                                      double pagibig, double tax,
                                      double net) {

        System.out.println("\n===== MOTORPH PAYROLL =====");
        System.out.println("Employee ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Birthday: " + birthday);
        System.out.println("Total Hours: " + hours);
        System.out.println("Gross Pay: " + gross);
        System.out.println("SSS: " + sss);
        System.out.println("PhilHealth: " + philhealth);
        System.out.println("PagIBIG: " + pagibig);
        System.out.println("Tax: " + tax);
        System.out.println("Net Pay: " + net);
    }
}

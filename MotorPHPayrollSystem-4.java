package com.mycompany.motorphpayrollsystem;

import java.io.*;
import java.util.*;

public class MotorPHPayrollSystem {

    // Default system password
    static final String PASSWORD = "12345";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Ask the user to login and determine their role
        String role = login(scanner);

        if (role == null) {
            System.out.println("Incorrect username or password.");
            return;
        }

        // Show different menus depending on the user role
        if (role.equals("employee")) {
            displayEmployeeMenu(scanner);
        } else {
            displayPayrollMenu(scanner);
        }

        scanner.close();
    }

    // ================= LOGIN =================
    // Handles system login and returns the role of the user
    public static String login(Scanner sc) {

        System.out.print("Username: ");
        String user = sc.nextLine().trim();

        System.out.print("Password: ");
        String pass = sc.nextLine().trim();

        // Only two valid users: employee or payroll_staff
        if ((user.equals("employee") || user.equals("payroll_staff"))
                && pass.equals(PASSWORD)) {
            return user;
        }

        return null;
    }

    // ================= MENUS =================

    // Menu for employees (can only view their own payroll)
    public static void displayEmployeeMenu(Scanner sc) {

        System.out.print("Enter your Employee ID: ");
        String id = sc.nextLine().trim();

        System.out.print("Enter cutoff period (1 = 1-15, 2 = 16-31): ");
        int cutoff = sc.nextInt();

        processOneEmployee(id, cutoff);
    }

    // Menu for payroll staff
    public static void displayPayrollMenu(Scanner sc) {

        System.out.println("\n=== PAYROLL STAFF MENU ===");
        System.out.println("1. Process One Employee");
        System.out.println("2. Process All Employees");
        System.out.print("Choice: ");

        String choice = sc.nextLine();

        if (choice.equals("1")) {

            System.out.print("Employee ID: ");
            String id = sc.nextLine().trim();

            System.out.print("Enter cutoff period (1 = 1-15, 2 = 16-31): ");
            int cutoff = sc.nextInt();

            processOneEmployee(id, cutoff);

        } else if (choice.equals("2")) {

            System.out.print("Enter cutoff period (1 = 1-15, 2 = 16-31): ");
            int cutoff = sc.nextInt();

            processAllEmployees(cutoff);

        } else {
            System.out.println("Invalid choice.");
        }
    }

    // ================= PROCESSING =================

    // Processes payroll for a single employee
    public static void processOneEmployee(String id, int cutoff) {

        // Get employee information from CSV file
        String[] info = getEmployeeInfo(id);

        if (info == null) {
            System.out.println("Employee not found.");
            return;
        }

        String name = info[0];
        String birthday = info[1];
        double rate = Double.parseDouble(info[2]);

        // Compute hours worked for the selected cutoff period
        double hours = computeHours(id, cutoff);

        // Gross pay calculation
        double gross = hours * rate;

        // Calculate deductions and net pay
        PayrollResult result = calculatePayroll(gross, cutoff);

        // Display payroll result
        displayPayroll(id, name, birthday, hours, gross,
                result.sss, result.philhealth,
                result.pagibig, result.tax,
                result.net);
    }

    // Processes payroll for all employees
    public static void processAllEmployees(int cutoff) {

        try {

            InputStream file = MotorPHPayrollSystem.class
                    .getClassLoader().getResourceAsStream("employees.csv");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(file));

            reader.readLine(); // skip header
            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.contains("\t") ? line.split("\t") : line.split(",");

                if (data.length < 4) continue;

                String id = data[0].trim();

                processOneEmployee(id, cutoff);
            }

            reader.close();

        } catch (Exception e) {
            System.out.println("Error processing employees.");
        }
    }

    // ================= PAYROLL =================

    // Class used to store deduction results
    static class PayrollResult {
        double sss, philhealth, pagibig, tax, net;
    }

    // Calculates deductions and net pay
    public static PayrollResult calculatePayroll(double gross, int cutoff) {

        PayrollResult result = new PayrollResult();
        
        // If no salary, do not apply deductions
        if (gross <= 0) {
            result.sss = 0;
            result.philhealth = 0;
            result.pagibig = 0;
            result.tax = 0;
            result.net = 0;
            return result;
        }

        // First cutoff → no deductions
        if (cutoff == 1) {
            result.net = gross;
            return result;
        }

        // Second cutoff → apply deductions
        result.sss = computeSSS(gross);
        result.philhealth = gross * 0.015;
        result.pagibig = Math.min(gross * 0.02, 100);
        result.tax = computeTax(gross);

        double deductions =
                result.sss + result.philhealth +
                result.pagibig + result.tax;

        result.net = gross - deductions;

        return result;
    }

    // ================= FILE READING =================

    // Retrieves employee information from employees.csv
    public static String[] getEmployeeInfo(String id) {

        try {

            InputStream file = MotorPHPayrollSystem.class
                    .getClassLoader().getResourceAsStream("employees.csv");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(file));

            reader.readLine(); // skip header
            String line;

            while ((line = reader.readLine()) != null) {

                // Support both TAB and COMMA separated files
                String[] data = line.contains("\t") ? line.split("\t") : line.split(",");
                

                if (data[0].trim().equals(id)) {
                    reader.close();
                    return new String[]{
                            data[1].trim(), // name
                            data[2].trim(), // birthday
                            data[3].trim() // hourly rate
                    };
                }
            }

            reader.close();

        } catch (Exception e) {
            System.out.println("Error reading employees file.");
        }

        return null;
    }

    // ================= ATTENDANCE =================

    // Computes total hours worked for the selected cutoff period
    public static double computeHours(String id, int cutoff) {

        double totalHours = 0;

        try {

            InputStream file = MotorPHPayrollSystem.class
                    .getClassLoader().getResourceAsStream("attendance.csv");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(file));

            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.contains("\t") ? line.split("\t") : line.split(",");

                if (!data[0].trim().equals(id)) continue;

                String date = data[1].trim();
                int day;

                // Extract the day number from the date
                if (date.contains("/")) {
                    day = Integer.parseInt(date.split("/")[1]);
                } else {
                    day = Integer.parseInt(date.split("-")[2]);
                }

                // Filter based on cutoff period
                if (cutoff == 1 && day > 15) continue;
                if (cutoff == 2 && day <= 15) continue;

                totalHours += calculateDailyHours(
                        data[2].trim(),
                        data[3].trim());
            }

            reader.close();

        } catch (Exception e) {
            System.out.println("Error reading attendance file.");
        }

        return totalHours;
    }

    // Calculates daily work hours based on time in/out
    public static double calculateDailyHours(String in, String out) {

        double inTime = convertTime(in);
        double outTime = convertTime(out);

        // Grace period until 8:05 AM
        if (inTime <= 8.0833) inTime = 8.0;

        // Work hours capped at 5 PM
        if (outTime > 17.0) outTime = 17.0;

        double hours = outTime - inTime;

        return Math.max(hours, 0);
    }

    // Converts AM/PM time format into decimal hours
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

    // ================= DEDUCTIONS =================

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

    public static double computeTax(double salary) {

        if (salary <= 20833) return 0;
        else if (salary <= 33332)
            return (salary - 20833) * 0.20;
        else if (salary <= 66666)
            return 2500 + (salary - 33333) * 0.25;
        else if (salary <= 166666)
            return 10833 + (salary - 66667) * 0.30;
        else
            return 40833.33 + (salary - 166667) * 0.32;
    }

    // ================= DISPLAY =================

    // Displays payroll summary
    public static void displayPayroll(String id, String name,
                                      String birthday, double hours,
                                      double gross, double sss,
                                      double philhealth, double pagibig,
                                      double tax, double net) {

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

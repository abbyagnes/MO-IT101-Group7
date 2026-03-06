package com.mycompany.motorphpayrollsystem;

import java.io.*;
import java.util.*;

public class MotorPHPayrollSystem {

    static final String PASSWORD = "12345";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Username: ");
        String user = sc.nextLine().trim();

        System.out.print("Password: ");
        String pass = sc.nextLine().trim();

        if (!(user.equals("employee") || user.equals("payroll_staff")) || !pass.equals(PASSWORD)) {
            System.out.println("Incorrect username or password.");
            return;
        }

        System.out.print("Employee ID: ");
        String id = sc.nextLine().trim();

        String name = "";
        String birthday = "";
        double rate = 0;

        try {

            InputStream file = MotorPHPayrollSystem.class
                    .getClassLoader().getResourceAsStream("employees.csv");

            if (file == null) {
                System.out.println("employees.csv not found.");
                return;
            }

            BufferedReader emp = new BufferedReader(new InputStreamReader(file));

            emp.readLine(); 
            String line;

            while ((line = emp.readLine()) != null) {

                String[] d = line.split(",");

                if (d.length < 4) continue;

                if (d[0].trim().equals(id)) {

                    name = d[1].trim();
                    birthday = d[2].trim();
                    rate = Double.parseDouble(d[3].trim());
                    break;
                }
            }

            emp.close();

        } catch (Exception e) {
            System.out.println("Error reading employees file.");
            return;
        }

        if (rate == 0) {
            System.out.println("Employee not found.");
            return;
        }

        double hours = computeHours(id);
        double gross = hours * rate;

        double sss = (gross > 0) ? computeSSS(gross) : 0;
        double philhealth = gross * 0.015;
        double pagibig = Math.min(gross * 0.02, 100);
        double tax = computeTax(gross);

        double deductions = sss + philhealth + pagibig + tax;
        double net = gross - deductions;

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

        sc.close();
    }

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

            att.readLine();
            String line;

            while ((line = att.readLine()) != null) {

                String[] d;

                if (line.contains("\t")) {
                    d = line.split("\t");
                } else {
                    d = line.split(",");
                }

                if (d.length < 4) continue;

                String empId = d[0].trim();
                String timeIn = d[2].trim();
                String timeOut = d[3].trim();

                if (!empId.equals(id)) continue;

                totalHours += calculateDailyHours(timeIn, timeOut);
            }

            att.close();

        } catch (Exception e) {
            System.out.println("Error reading attendance file.");
        }

        return totalHours;
    }

    public static double calculateDailyHours(String in, String out) {

        double inTime = convertTime(in);
        double outTime = convertTime(out);

        if (inTime <= 8.0833) inTime = 8.0;
        if (inTime < 8.0) inTime = 8.0;
        if (outTime > 17.0) outTime = 17.0;

        double hours = outTime - inTime;

        if (hours < 0) hours = 0;

        return hours;
    }

    public static double convertTime(String time) {

        String[] p = time.toLowerCase().split(" ");
        String[] t = p[0].split(":");

        int hour = Integer.parseInt(t[0]);
        int min = Integer.parseInt(t[1]);
        int sec = Integer.parseInt(t[2]);

        if (p[1].equals("pm") && hour != 12) hour += 12;
        if (p[1].equals("am") && hour == 12) hour = 0;

        return hour + (min / 60.0) + (sec / 3600.0);
    }

    public static double computeSSS(double s) {

        if (s < 3250) return 135;
        else if (s < 3750) return 157.5;
        else if (s < 4250) return 180;
        else if (s < 4750) return 202.5;
        else if (s < 5250) return 225;
        else if (s < 5750) return 247.5;
        else if (s < 6250) return 270;
        else if (s < 6750) return 292.5;
        else if (s < 7250) return 315;
        else if (s < 7750) return 337.5;
        else if (s < 8250) return 360;
        else if (s < 8750) return 382.5;
        else if (s < 9250) return 405;
        else if (s < 9750) return 427.5;
        else if (s < 10250) return 450;
        else return 1125;
    }

    public static double computeTax(double s) {

        if (s <= 20833) return 0;
        else if (s <= 33332) return (s - 20833) * 0.20;
        else if (s <= 66666) return 2500 + (s - 33333) * 0.25;
        else if (s <= 166666) return 10833 + (s - 66667) * 0.30;
        else return 40833.33 + (s - 166667) * 0.32;
    }
}
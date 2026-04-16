package org.example.constants;

import java.util.HashMap;
import java.util.Map;

public class ActivityPoints {

    // Activity mapping with Sl. No, Activity Name, and Points
    public static final Map<Integer, ActivityMapping> ACTIVITIES = new HashMap<>();

    static {
        // AICTE Based Activity Points Mapping
        ACTIVITIES.put(1,
                new ActivityMapping(1, "Office Bearers of Department Association (For one academic year)", 5));
        ACTIVITIES.put(2, new ActivityMapping(2, "Participation in NSS activities (Approved by Staff in charge)", 10));
        ACTIVITIES.put(3, new ActivityMapping(3, "Participation in NCC activity", 10));
        ACTIVITIES.put(4, new ActivityMapping(4, "Participation in Yakshagana (Approved by Staff in charge)", 10));
        ACTIVITIES.put(5, new ActivityMapping(5, "Participation in Youth Red Cross unit activities", 10));
        ACTIVITIES.put(6, new ActivityMapping(6, "Participation in Sports (University Level)", 10));
        ACTIVITIES.put(7, new ActivityMapping(7, "Class Representative (For one academic year)", 5));
        ACTIVITIES.put(8,
                new ActivityMapping(8, "Office Bearers of student professional bodies (ISTE, IEEE, etc.)", 5));
        ACTIVITIES.put(9,
                new ActivityMapping(9, "Office Bearers of hobby clubs (Kalanjali, Taleem, Aero club, etc.)", 5));
        ACTIVITIES.put(10,
                new ActivityMapping(10, "Winners in college Fest competition & other clubs (Other Colleges)", 10));
        ACTIVITIES.put(11, new ActivityMapping(11, "Participation in outreach programme (Society Connect)", 5));
        ACTIVITIES.put(12, new ActivityMapping(12, "MOOC / NPTEL Course Completion Certificate", 20));
        ACTIVITIES.put(13, new ActivityMapping(13, "Paper presentation in other colleges (In Conferences)", 20));
        ACTIVITIES.put(14, new ActivityMapping(14, "Key roles in INCREDIA (Approved by concerned faculty)", 10));
        ACTIVITIES.put(15, new ActivityMapping(15, "Any activity conducted by the department (Approved by HoD)", 10));
        ACTIVITIES.put(16, new ActivityMapping(16, "Participation in Hackathons (Approved by faculty)", 10));
        ACTIVITIES.put(17, new ActivityMapping(17, "Participation in NUCAT counselling as a volunteer", 20));
        ACTIVITIES.put(18,
                new ActivityMapping(18, "Participation in Swachh Bharath, Digital India, Rural dev, etc.", 10));
        ACTIVITIES.put(19, new ActivityMapping(19, "Value-added course (Not claimed under curriculum credit)", 10));
        ACTIVITIES.put(20, new ActivityMapping(20, "Winners at Incredia (I, II, III)", 10));
        ACTIVITIES.put(21, new ActivityMapping(21, "Volunteering work at college / departmental Programmes", 5));
        ACTIVITIES.put(22,
                new ActivityMapping(22, "Technical internship (Min. 1 month, not under curriculum credit)", 20));
        ACTIVITIES.put(23, new ActivityMapping(23, "Attending conference / Experts' talks, etc.", 5));
        ACTIVITIES.put(24, new ActivityMapping(24, "First-year internship", 25));
        ACTIVITIES.put(25, new ActivityMapping(25, "Internship for lateral entry students (3rd & 4th semesters)", 25));
    }

    /**
     * Get activity details by serial number
     */
    public static ActivityMapping getActivityBySlNo(Integer slNo) {
        return ACTIVITIES.get(slNo);
    }

    /**
     * Get points for an activity by serial number
     */
    public static Integer getPointsBySlNo(Integer slNo) {
        ActivityMapping activity = ACTIVITIES.get(slNo);
        return activity != null ? activity.getPoints() : 0;
    }

    /**
     * Get all activities
     */
    public static Map<Integer, ActivityMapping> getAllActivities() {
        return new HashMap<>(ACTIVITIES);
    }

    /**
     * Inner class to represent Activity Mapping
     */
    public static class ActivityMapping {
        private Integer slNo;
        private String activityDetail;
        private Integer points;

        public ActivityMapping(Integer slNo, String activityDetail, Integer points) {
            this.slNo = slNo;
            this.activityDetail = activityDetail;
            this.points = points;
        }

        public Integer getSlNo() {
            return slNo;
        }

        public String getActivityDetail() {
            return activityDetail;
        }

        public Integer getPoints() {
            return points;
        }

        public String getDisplayText() {
            return slNo + ". " + activityDetail + " (" + points + " pts)";
        }
    }
}

package com.ejang.restaurantwatch;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Eric on 2017-03-23.
 */

public class InspectionResult {

    public Date inspectionDate;
    public String inspectionType;
    public ArrayList<Violation> violations;
    public HazardRating hazardRating;
    public String numCritical;
    public String numNonCritical;

    public InspectionResult(String date, String type, String violationsLump, String hazardRating,
                            String numCritical, String numNonCritical)
    {
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        try {
            inspectionDate = format.parse(date);
        } catch (ParseException e)
        {
            inspectionDate = null;
        }

        inspectionType = type;
        this.numCritical = numCritical;
        this.numNonCritical = numNonCritical;
        this.violations = new ArrayList<>();

        if (violationsLump.length() > 0)
        {
            // TODO: call organizeViolationsLump when it is implemented
        }

        if (hazardRating.equalsIgnoreCase("low"))
        {
            this.hazardRating = HazardRating.SAFE;
        }
        else if (hazardRating.equalsIgnoreCase("moderate"))
        {
            this.hazardRating = HazardRating.MODERATE;
        }
        else if (hazardRating.equalsIgnoreCase("high"))
        {
            this.hazardRating = HazardRating.UNSAFE;
        }
    }

    public void organizeViolationsLump(String violationsLump)
    {
        String[] violations = violationsLump.split("|");
        for (String violation : violations)
        {
            String[] details = violation.split(",");
            String code = details[0];
            String crit = details[1];
            String description = details[2];
            this.violations.add(new Violation(code, crit, description));
        }
    }

}


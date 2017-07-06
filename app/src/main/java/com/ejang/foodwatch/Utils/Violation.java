package com.ejang.foodwatch.Utils;

/**
 * Created by Eric on 2017-03-29.
 */

public class Violation {

    private String violationCode;
    private String violationCrit;
    private String violationDetail;

    public Violation(String code, String crit, String detail)
    {
        this.violationCode = code;
        this.violationCrit = crit;
        this.violationDetail = detail;
    }

    public String getViolationCode()
    {
        return violationCode;
    }

    public String getViolationCrit()
    {
        return violationCrit;
    }

    public String getViolationDetail()
    {
        return violationDetail;
    }

}

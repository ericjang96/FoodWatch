package com.ejang.restaurantwatch;

/**
 * Created by Eric on 2017-03-29.
 */

public class Violation {

    String violationCode;
    String violationCrit;
    String violationDetail;

    public Violation(String code, String crit, String detail)
    {
        this.violationCode = code;
        this.violationCrit = crit;
        this.violationDetail = detail;
    }

}

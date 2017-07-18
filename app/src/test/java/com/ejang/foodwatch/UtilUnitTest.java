package com.ejang.foodwatch;

import com.ejang.foodwatch.Activities.BaseActivity;
import com.ejang.foodwatch.Utils.InspectionResult;
import com.ejang.foodwatch.Utils.Violation;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UtilUnitTest {

    @Test
    public void testOrganizeViolLump()
    {
        String lump = "209,Not Critical,Food not protected from contamination [s. 12(a)],Not Repeat|" +
                "301,Critical,Equipment/utensils/food contact surfaces not maintained in sanitary condition [s. 17(1)],Not Repeat|" +
                "302,Critical,Equipment/utensils/food contact surfaces not properly washed and sanitized [s. 17(2)],Not Repeat|" +
                "306,Not Critical,Food premises not maintained in a sanitary condition [s. 17(1)],Not Repeat|" +
                "401,Critical,Adequate handwashing stations not available for employees [s. 21(4)],Not Repeat";

        ArrayList<Violation> violations = InspectionResult.organizeViolationsLump(lump);

        assertEquals(5, violations.size());
        assertEquals("306", violations.get(3).getViolationCode());
        assertEquals("Not Critical", violations.get(0).getViolationCrit());
        assertEquals("Equipment/utensils/food contact surfaces not maintained in sanitary condition [s. 17(1)]", violations.get(1).getViolationDetail());
    }

    @Test
    public void testOrganizeOneViolation()
    {
        String lump = "209,Not Critical,Food not protected from contamination [s. 12(a)],Not Repeat";

        ArrayList<Violation> violations = InspectionResult.organizeViolationsLump(lump);

        assertEquals(1, violations.size());
        assertEquals("209", violations.get(0).getViolationCode());
        assertEquals("Not Critical", violations.get(0).getViolationCrit());
        assertEquals("Food not protected from contamination [s. 12(a)]", violations.get(0).getViolationDetail());
    }

    @Test
    public void testOrganizeViolLumpEmpty()
    {
        String lump = "";

        ArrayList<Violation> violations = InspectionResult.organizeViolationsLump(lump);
        assertEquals(0, violations.size());
    }

    @Test
    public void testOrganizeBadViolLump()
    {
        String lump = "asdfasdf|,asdfawefawg";

        ArrayList<Violation> violations = InspectionResult.organizeViolationsLump(lump);
        assertEquals(0, violations.size());
    }

    @Test
    public void testVolleyErrorParse()
    {
        String trimmedError = BaseActivity.trimErrorMessage("{\n" +
                "    \"status\": 403,\n" +
                "    \"message\": \"This is a sample volley error\"\n" +
                "}", "message");

        assertEquals("This is a sample volley error", trimmedError);

    }
}
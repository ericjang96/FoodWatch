package com.ejang.foodwatch.Utils;

/**
 * Created by Eric on 2017-06-23.
 */

// This enum is represents different types of filters to perform based on user input. A filter will
// need to do different things based on whether the user is filtering by text, minimum safety
// rating, distance, etc.
public enum FilterType {
    TEXT_SEARCH, SAFETY_RATING
}

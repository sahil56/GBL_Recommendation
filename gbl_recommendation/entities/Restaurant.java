package gbl_recommendation.entities;

import gbl_recommendation.enums.Cuisine;

import java.util.Date;

public class Restaurant {
    public String restaurantId;
    public Cuisine cuisine;
    public String costBracket;
    public float rating;
    public boolean isRecommended;
    public Date onboardedTime;
}

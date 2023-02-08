# GBL_Recommendation

Features Added:
Null User Exception
Handled Null User Preferences
Handled Adding Dupilcate Restaurants
Limiting the result to 100 restaurants
Modular/extendable code and configurable via Constants.java

Features not Added:
Pending error log statements
Can be more efficient in terms of latency (calculations still done once 100 restaurants are populated)
Sorting not tested (ascending/descending)

Assumptions:
availableRestaurants are not null or empty

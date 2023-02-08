package gbl_recommendation;

import gbl_recommendation.entities.Restaurant;
import gbl_recommendation.entities.User;
import gbl_recommendation.enums.Cuisine;
import gbl_recommendation.enums.RestaurantType;

import java.util.*;
import java.util.stream.Collectors;

public class GBLRecommendation {
    public String[] getRestaurantRecommendations(User user, Restaurant[] availableRestaurants) throws Exception {
        List<String> recommendations = new ArrayList<>();

        // user == null -> throw exception
        // assuming availableRestaurants != null or empty
        if (user == null) {
            throw new Exception("User is Null");
        }

        // get user favourite cuisine and cost bracket
        // top 3 in this case | is configurable e,g can be 4, 5 or more...
        List<Cuisine> userFavouriteCuisines = getUserFavouriteCuisine(user);
        List<String> userFavouriteCostBrackets = getUserFavouriteCostBracket(user);

        // split above preferences to primary and secondary
        // again configurable and any number of elements can be added to these sets
        Set<Cuisine> userPrimaryCuisines = getUserPrimaryCuisines(userFavouriteCuisines);
        Set<Cuisine> userSecondaryCuisines = getUserSecondaryCuisines(userFavouriteCuisines);
        Set<String> userPrimaryCostBracket = getUserPrimaryCostBracket(userFavouriteCostBrackets);
        Set<String> userSecondaryCostBracket = getUserSecondaryCostBracket(userFavouriteCostBrackets);

        // add featured restaurants | Order 1
        recommendations.addAll(getRestaurants(RestaurantType.Featured, userPrimaryCuisines, userSecondaryCuisines, userPrimaryCostBracket, userSecondaryCostBracket, availableRestaurants, 0.0f, null, new ArrayList<>(recommendations)));

        // add Order 2 restaurants
        recommendations.addAll(getRestaurants(null, userPrimaryCuisines, null, userPrimaryCostBracket, null, availableRestaurants, Constants.LOW_RATING, Constants.OPERATOR_GREATER_THAN_EQUAL, new ArrayList<>(recommendations)));

        // add Order 3 restaurants
        recommendations.addAll(getRestaurants(null, userPrimaryCuisines, null, null, userSecondaryCostBracket, availableRestaurants, Constants.HIGH_RATING, Constants.OPERATOR_GREATER_THAN_EQUAL, new ArrayList<>(recommendations)));

        // add Order 4 restaurants
        recommendations.addAll(getRestaurants(null, null, userSecondaryCuisines, userPrimaryCostBracket, null, availableRestaurants, Constants.HIGH_RATING, Constants.OPERATOR_GREATER_THAN_EQUAL, new ArrayList<>(recommendations)));

        // add New restaurants | Order 5
        recommendations.addAll(getRestaurants(RestaurantType.New, null, null, null, null, availableRestaurants, 0.0f, null, new ArrayList<>(recommendations)));

        // add Order 6 restaurants
        recommendations.addAll(getRestaurants(null, userPrimaryCuisines, null, userPrimaryCostBracket, null, availableRestaurants, Constants.LOW_RATING, Constants.OPERATOR_LESS_THAN, new ArrayList<>(recommendations)));

        // add Order 7 restaurants
        recommendations.addAll(getRestaurants(null, userPrimaryCuisines, null, null, userSecondaryCostBracket, availableRestaurants, Constants.HIGH_RATING, Constants.OPERATOR_LESS_THAN, new ArrayList<>(recommendations)));

        // add Order 8 restaurants
        recommendations.addAll(getRestaurants(null, null, userSecondaryCuisines, userPrimaryCostBracket, null, availableRestaurants, Constants.HIGH_RATING, Constants.OPERATOR_LESS_THAN, new ArrayList<>(recommendations)));

        // add remaining restaurants | Order 9
        recommendations.addAll(getRestaurants(null, null, null, null, null, availableRestaurants, 0.0f, null, new ArrayList<>(recommendations)));

        return recommendations.subList(0, 100).toArray(new String[0]);
    }

    public List<Cuisine> getUserFavouriteCuisine(User user) {
        if (user.cuisinesTracking == null) return new ArrayList<>();
        return Arrays.stream(user.cuisinesTracking)
                .sorted((o1, o2) -> (o2.noOfOrders > o1.noOfOrders) ? 1 : -1)
                .map(cuisineTracking -> cuisineTracking.cuisine)
                .limit(Constants.FAVOURITE_LIMIT)
                .collect(Collectors.toList());
    }

    public List<String> getUserFavouriteCostBracket(User user) {
        if (user.costTracking == null) return new ArrayList<>();
        return Arrays.stream(user.costTracking)
                .sorted((o1, o2) -> (o2.noOfOrders > o1.noOfOrders) ? 1 : -1)
                .map(costTracking -> costTracking.costType)
                .limit(Constants.FAVOURITE_LIMIT)
                .collect(Collectors.toList());
    }

    public Set<Cuisine> getUserPrimaryCuisines(List<Cuisine> userFavouriteCuisines) {
        return userFavouriteCuisines.isEmpty() ? new HashSet<>() : new HashSet<>(){{add(userFavouriteCuisines.get(0));}};
    }

    public Set<Cuisine> getUserSecondaryCuisines(List<Cuisine> userFavouriteCuisines) {
        return userFavouriteCuisines.isEmpty() ? new HashSet<>() :
               (userFavouriteCuisines.size() == 1 ? new HashSet<>() :
               (userFavouriteCuisines.size() == 2 ? new HashSet<>(){{add(userFavouriteCuisines.get(1));}} :
               (new HashSet<>(){{add(userFavouriteCuisines.get(1)); add(userFavouriteCuisines.get(2));}})));
    }

    public Set<String> getUserPrimaryCostBracket(List<String> userFavouriteCostBrackets) {
        return userFavouriteCostBrackets.isEmpty() ? new HashSet<>() : new HashSet<>(){{add(userFavouriteCostBrackets.get(0));}};
    }

    public Set<String> getUserSecondaryCostBracket(List<String> userFavouriteCostBrackets) {
        return userFavouriteCostBrackets.isEmpty() ? new HashSet<>() :
               (userFavouriteCostBrackets.size() == 1 ? new HashSet<>() :
               (userFavouriteCostBrackets.size() == 2 ? new HashSet<>(){{add(userFavouriteCostBrackets.get(1));}} :
               (new HashSet<>(){{add(userFavouriteCostBrackets.get(1)); add(userFavouriteCostBrackets.get(2));}})));
    }

    // methods takes already populated restaurants so that those are not added again to avoid duplicates
    public List<String> getRestaurants(RestaurantType restaurantType, Set<Cuisine> userPrimaryCuisines, Set<Cuisine> userSecondaryCuisines, Set<String> userPrimaryCostBrackets, Set<String> userSecondaryCostBrackets, Restaurant[] availableRestaurants, float rating, String operator, List<String> populatedRestaurants) {
        List<String> restaurants = new ArrayList<>();

        // Logic | Featured | Order 1
        if (RestaurantType.Featured == restaurantType) {
            restaurants = Arrays.stream(availableRestaurants)
                    .filter(restaurant -> !populatedRestaurants.contains(restaurant.restaurantId))
                    .filter(restaurant -> userPrimaryCuisines.contains(restaurant.cuisine))
                    .filter(restaurant -> userPrimaryCostBrackets.contains(restaurant.costBracket))
                    .filter(restaurant -> restaurant.isRecommended)
                    .map(restaurant -> restaurant.restaurantId)
                    .toList();
            if (restaurants.isEmpty()) {
                restaurants = Arrays.stream(availableRestaurants)
                        .filter(restaurant -> !populatedRestaurants.contains(restaurant.restaurantId))
                        .filter(restaurant -> (userSecondaryCuisines.contains(restaurant.cuisine) && userPrimaryCostBrackets.contains(restaurant.costBracket)) ||
                                              (userPrimaryCuisines.contains(restaurant.cuisine) && userSecondaryCostBrackets.contains(restaurant.costBracket)))
                        .filter(restaurant -> restaurant.isRecommended)
                        .map(restaurant -> restaurant.restaurantId)
                        .toList();
            }
            return restaurants;
        }

        // Logic | New | Order 5
        if (RestaurantType.New == restaurantType) {
            restaurants = Arrays.stream(availableRestaurants)
                    .filter(restaurant -> !populatedRestaurants.contains(restaurant.restaurantId))
                    .filter(restaurant -> restaurant.onboardedTime.getTime() >= System.currentTimeMillis() - Constants.TWO_DAYS)
                    .sorted((r1, r2) -> (r1.onboardedTime.getTime() > r2.onboardedTime.getTime()) ? 1 : -1)
                    .map(restaurant -> restaurant.restaurantId)
                    .toList();
            return restaurants;
        }

        // Logic | Order 2,3,4 && 6,7,8
        if(userPrimaryCuisines!=null || userSecondaryCuisines!=null || userPrimaryCostBrackets!=null || userSecondaryCostBrackets!=null) {
            List<Restaurant> restaurantsList = Arrays.stream(availableRestaurants)
                    .filter(restaurant -> !populatedRestaurants.contains(restaurant.restaurantId))
                    .toList();
            if (userPrimaryCuisines!=null) {
                restaurantsList = Arrays.stream(availableRestaurants)
                        .filter(restaurant -> userPrimaryCuisines.contains(restaurant.cuisine))
                        .toList();
            }
            if (userSecondaryCuisines!=null) {
                restaurantsList = Arrays.stream(availableRestaurants)
                        .filter(restaurant -> userSecondaryCuisines.contains(restaurant.cuisine))
                        .toList();
            }
            if (userPrimaryCostBrackets!=null) {
                restaurantsList = Arrays.stream(availableRestaurants)
                        .filter(restaurant -> userPrimaryCostBrackets.contains(restaurant.costBracket))
                        .toList();
            }
            if (userSecondaryCostBrackets!=null) {
                restaurantsList = Arrays.stream(availableRestaurants)
                        .filter(restaurant -> userSecondaryCostBrackets.contains(restaurant.costBracket))
                        .toList();
            }
            if (Constants.OPERATOR_GREATER_THAN_EQUAL.equals(operator)) {
                restaurantsList = Arrays.stream(availableRestaurants)
                        .filter(restaurant -> restaurant.rating >= rating)
                        .toList();
            }
            if (Constants.OPERATOR_LESS_THAN.equals(operator)) {
                restaurantsList = Arrays.stream(availableRestaurants)
                        .filter(restaurant -> restaurant.rating < rating)
                        .toList();
            }
            restaurants = restaurantsList.stream()
                    .map(restaurant -> restaurant.restaurantId)
                    .toList();
        }

        // Logic | Remaining | Order 9
        else {
            restaurants = Arrays.stream(availableRestaurants)
                    .filter(restaurant -> !populatedRestaurants.contains(restaurant.restaurantId))
                    .map(restaurant -> restaurant.restaurantId)
                    .limit(Constants.MAX_RECOMMENDATIONS - populatedRestaurants.size())
                    .toList();
        }
        return restaurants;
    }
}

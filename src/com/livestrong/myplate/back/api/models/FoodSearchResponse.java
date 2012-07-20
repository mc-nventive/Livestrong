package com.livestrong.myplate.back.api.models;

import java.util.Collection;

import com.livestrong.myplate.back.models.Food;

public class FoodSearchResponse extends AbstractSearchResponse {
	private Collection<Food> foods;
	
	public FoodSearchResponse() {
		super();
	}

	public FoodSearchResponse(String query, int start, int limit, int found, Collection<Food> foods) {
		super(query, start, limit, found);
		this.foods = foods;
	}

	public Collection<Food> getFoods() {
		return foods;
	}
}

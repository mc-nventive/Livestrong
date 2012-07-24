package com.livestrong.myplate.back.api.models;

import java.util.Collection;

import com.livestrong.myplate.back.models.Exercise;
import com.livestrong.myplate.back.api.models.AbstractSearchResponse;

public class ExerciseSearchResponse extends AbstractSearchResponse {
	private Collection<Exercise> exercises;
	
	public ExerciseSearchResponse() {
		super();
	}

	public ExerciseSearchResponse(String query, int start, int limit, int found, Collection<Exercise> exercises) {
		super(query, start, limit, found);
		this.exercises = exercises;
	}

	public Collection<Exercise> getExercises() {
		return exercises;
	}
}

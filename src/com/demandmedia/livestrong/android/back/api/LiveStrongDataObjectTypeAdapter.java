package com.demandmedia.livestrong.android.back.api;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.demandmedia.livestrong.android.back.api.models.AbstractLiveStrongApiObject;
import com.demandmedia.livestrong.android.back.api.models.LiveStrongApiObject;
import com.demandmedia.livestrong.android.back.models.DiaryEntry;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

public class LiveStrongDataObjectTypeAdapter implements JsonDeserializer<LiveStrongApiObject>, JsonSerializer<LiveStrongApiObject> {
	
	private final FieldNamingStrategy fieldNamingPolicy;
	private final static FieldNamingStrategy beanMethodsNamingPolicy = FieldNamingPolicy.UPPER_CAMEL_CASE;
	
	public LiveStrongDataObjectTypeAdapter(FieldNamingStrategy fieldNamingPolicy) {
		this.fieldNamingPolicy = fieldNamingPolicy;
	}

	public static List<Field> getAllFieldsForClass(List<Field> fields, Class<?> type) {
	    for (Field field: type.getDeclaredFields()) {
	        fields.add(field);
	    }

	    if (type.getSuperclass() != null) {
	        fields = getAllFieldsForClass(fields, type.getSuperclass());
	    }

	    return fields;
	}
	
	public LiveStrongApiObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		LiveStrongApiObject lsObject = null;
		Object objectFromJson = null;
		String fieldName = null;
		try {
			JsonObject jobject = (JsonObject) json;

			lsObject = (LiveStrongApiObject) ((Class<?>) typeOfT).newInstance();
			
			List<Field> fields = new ArrayList<Field>();
			fields = getAllFieldsForClass(fields, lsObject.getClass());
			for (Field field : fields) {
				int mods = field.getModifiers();
				if (Modifier.isStatic(mods) || Modifier.isFinal(mods)) {
					// Let's just skip fields that are final or static
					continue;
				}

				String jsonName = getFieldName(field);
				JsonElement jsonValue = jobject.get(jsonName);
				if (jsonValue == null) {
					continue;
				}
				
				try {
					Type fieldGenType = field.getGenericType();
					if (field.getGenericType().equals(boolean.class)) {
						// AbstractLiveStrongApiObject.sanitizeBoolean(int) will take care of converting that number into a boolean
						fieldGenType = int.class;
					}
					objectFromJson = context.deserialize(jsonValue, fieldGenType);
				} catch (ClassCastException e) {
					// Try using a string, in case there's a setter that will handle the conversion to the proper type.
					objectFromJson = jsonValue.getAsJsonPrimitive().getAsString();
				} catch (JsonSyntaxException e) {
					// Try using a string, in case there's a setter that will handle the conversion to the proper type.
					objectFromJson = jsonValue.getAsJsonPrimitive().getAsString();
				} catch (NumberFormatException e) {
					// Try using a string, in case there's a setter that will handle the conversion to the proper type.
					objectFromJson = jsonValue.getAsJsonPrimitive().getAsString();
				}
				
				if (objectFromJson == null) {
					try {
						objectFromJson = jsonValue.getAsJsonPrimitive().getAsString();
					} catch (IllegalStateException e) {
						// Not a JSON primitive; let's just skip it.
						continue;
					}
				}

				String setterName = "set" + beanMethodsNamingPolicy.translateName(field);
				
				if (lsObject instanceof AbstractLiveStrongApiObject) {
					AbstractLiveStrongApiObject alsObject = (AbstractLiveStrongApiObject) lsObject;
					String units = alsObject.getUnits(field);
					if (field.getType().equals(int.class) && objectFromJson instanceof String) {
						objectFromJson = alsObject.sanitizeInteger((String) objectFromJson, units);
					} else if (field.getType().equals(double.class) && objectFromJson instanceof String) {
						objectFromJson = alsObject.sanitizeDouble((String) objectFromJson, units);
					} else if (field.getType().equals(boolean.class) && objectFromJson instanceof Integer) {
						objectFromJson = alsObject.sanitizeBoolean((Integer) objectFromJson);
					} else if (field.getType().equals(Date.class) && objectFromJson instanceof String) {
						objectFromJson = alsObject.sanitizeDate((String) objectFromJson);
					} else if (field.getType().equals(String.class) && objectFromJson instanceof String) {
						objectFromJson = alsObject.sanitizeString((String) objectFromJson);
					}
				}

				if (objectFromJson == null) {
					continue;
				}

				Method setterMethod = null;
				// Let's look for a method that use the declared class, or any or it's superclasses or interfaces as parameter type.
				Class<?> clazz = objectFromJson.getClass();
				while (clazz != null) {
					try {
						setterMethod = lsObject.getClass().getMethod(setterName, clazz);
						// Found a method using this class!
						break;
					} catch (NoSuchMethodException e) { }

					for (Class<?> intf : clazz.getInterfaces()) {
						try {
							setterMethod = lsObject.getClass().getMethod(setterName, intf);
							// Found a method using this interface!
							break;
						} catch (NoSuchMethodException e) { }
					}
					if (setterMethod != null) {
						break;
					}
					clazz = clazz.getSuperclass();
				}

				if (setterMethod != null) {
					//System.out.println("Calling " + typeOfT + "." + setterName + "(" + objectFromJson.getClass() + ")");
					setterMethod.invoke(lsObject, objectFromJson);
				} else {
					fieldName = field.getName();
					//System.out.println(typeOfT + "." + fieldName + " = " + objectFromJson.getClass());
					field.setAccessible(true);
					field.set(lsObject, objectFromJson);
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.err.println(typeOfT + "." + fieldName + " = " + objectFromJson.getClass());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			// Normal...
		} catch (Exception e) {
			e.printStackTrace();
		}

		// DiaryEntry objects coming from the API should never be dirty!
		if (lsObject instanceof DiaryEntry) {
			((DiaryEntry) lsObject).setDirty(false);
		}

		return lsObject;
	}
	
	private String getFieldName(Field f) {
		SerializedName serializedName = f.getAnnotation(SerializedName.class);
		return serializedName == null ? fieldNamingPolicy.translateName(f) : serializedName.value();
	}

	@Override
	public JsonElement serialize(LiveStrongApiObject lsObject, Type typeOfT, JsonSerializationContext context) {
		JsonObject json = null;

		try {
			json = new JsonObject();
			
			List<Field> fields = new ArrayList<Field>();
			fields = getAllFieldsForClass(fields, lsObject.getClass());
			for (Field field : fields) {
				int mods = field.getModifiers();
				if (Modifier.isStatic(mods) || Modifier.isFinal(mods) || Modifier.isTransient(mods)) {
					// Let's just skip fields that are final or static or transient
					continue;
				}

				String getterName = "getSerializable" + beanMethodsNamingPolicy.translateName(field);
				
				Object dataMemberValue = null;
				try {
					Method getterMethod = lsObject.getClass().getMethod(getterName);

					//System.out.println("Calling " + typeOfT + "." + getterName + "()");
					dataMemberValue = getterMethod.invoke(lsObject);
				} catch (NoSuchMethodException e) {
					//fieldName = field.getName();
					//System.out.println(typeOfT + "." + fieldName);
					field.setAccessible(true);
					dataMemberValue = field.get(lsObject);
				}
				
				if (dataMemberValue == null) {
					continue;
				}
				
				String jsonName = getFieldName(field);
				json.add(jsonName, context.serialize(dataMemberValue));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}
}

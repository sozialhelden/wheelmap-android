package org.wheelmap.android.model;

import com.google.gson.TypeAdapterFactory;
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory;

@GsonTypeAdapterFactory
abstract class AutoValueAdapterFactory implements TypeAdapterFactory {

  // Static factory method to access the package
  // private generated implementation
  public static TypeAdapterFactory create() {
    return new AutoValueGson_AutoValueAdapterFactory();
  }

}
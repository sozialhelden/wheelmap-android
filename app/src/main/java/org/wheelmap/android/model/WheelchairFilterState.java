/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wheelmap.android.model;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public enum WheelchairFilterState {
    NO_PREFERENCE(0),
    UNKNOWN(1), YES(2), LIMITED(3), NO(4),
    TOILET_UNKNOWN(5), TOILET_YES(6), TOILET_NO(7);

    public static final WheelchairFilterState DEFAULT;

    private final int id;

    private static Map<Integer, WheelchairFilterState> id2State;

    private static Map<String, WheelchairFilterState> string2State;


    private WheelchairFilterState(int id) {
        this.id = id;
        register();
    }

    public int getId() {
        return id;
    }

    public static WheelchairFilterState valueOf(int id) {
        return id2State.get(id);
    }

    public static WheelchairFilterState myValueOf(String string, String postfixKey) {
        if(!TextUtils.isEmpty(postfixKey)){
            string = postfixKey + string;
        }
        return string2State.get(string.toLowerCase());
    }

    private void register() {
        if (id2State == null) {
            id2State = new HashMap<Integer, WheelchairFilterState>();
        }

        id2State.put(id, this);

        if (string2State == null) {
            string2State = new HashMap<String, WheelchairFilterState>();
        }

        string2State.put(this.toString().toLowerCase(), this);
    }

    public String asRequestParameter() {
        if (this == NO_PREFERENCE) {
            return "";
        }

        switch (this){
            case TOILET_UNKNOWN:
                return UNKNOWN.name().toLowerCase();
            case TOILET_YES:
                return YES.name().toLowerCase();
            case TOILET_NO:
                return NO.name().toLowerCase();

            default:
                return this.name().toLowerCase();
        }
    }

    static {
        DEFAULT = NO_PREFERENCE;
    }
}

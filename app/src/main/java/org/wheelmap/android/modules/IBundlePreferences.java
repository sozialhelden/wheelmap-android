package org.wheelmap.android.modules;

import android.os.Bundle;

/**
 * Created with IntelliJ IDEA. User: mso Date: 11.05.13 Time: 20:31:16 To change this template use
 * File | Settings | File Templates.
 */
public interface IBundlePreferences {

    void store(String id, Bundle bundle);

    boolean contains(String id);

    Bundle retrieve(String id);
}

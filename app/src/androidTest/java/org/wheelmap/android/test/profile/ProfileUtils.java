package org.wheelmap.android.test.profile;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.wheelmap.android.modules.UserCredentials;

/**
 *
 * Created by timfreiheit on 27.11.15.
 */
public class ProfileUtils {

    public static final String TEST_USER = "wheelmapTestUser";
    public static final String TEST_USER_PASSWORD = "12345678";

    public static void logout(){
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        new UserCredentials(context).logout();
    }


    public static void fakeLogin(){
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        new UserCredentials(context).save("","");
    }

    public static boolean isLoggedIn(){
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return new UserCredentials(context).isLoggedIn();
    }

}

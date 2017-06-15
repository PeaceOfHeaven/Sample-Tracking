package com.example.nhat.mapexample.data.remote.fusiontable;

import android.content.Context;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.fusiontables.FusiontablesScopes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;

/**
 * Created by Nhat on 11/12/2016.
 */

public final class FusionTableApiUtil {

    private FusionTableApiUtil() {
    }

    /**
     * Authorizes the installed application to access user's protected data.
     */
    public static Credential authorize(Context context, HttpTransport httpTransport, JsonFactory jsonFactory) throws Exception {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId("ai2-fustiontable-demo@fustiontabledemo.iam.gserviceaccount.com")
                .setServiceAccountPrivateKeyFromP12File(copyP12FileToCacheDir(context))
                .setServiceAccountScopes(Collections.singleton(FusiontablesScopes.FUSIONTABLES))
                .build();
        return credential;
    }

    private static File copyP12FileToCacheDir(Context context) {
        File f = new File(context.getCacheDir()+"/FustionTableDemo-f7418dd6defc.p12");
        if (!f.exists()) try {
            InputStream is = context.getAssets().open("FustionTableDemo-f7418dd6defc.p12");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) { throw new RuntimeException(e); }
        return f;
    }
}

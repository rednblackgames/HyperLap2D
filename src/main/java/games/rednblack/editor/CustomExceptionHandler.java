/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor;

import java.awt.EventQueue;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpParametersUtils;
import games.rednblack.editor.utils.AppConfig;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class CustomExceptionHandler implements UncaughtExceptionHandler {

    //private UncaughtExceptionHandler defaultUEH;
    private final static String sendURL = "https://hyperlap2d.rednblack.games/error_report";

    /* 
     * if any of the parameters is null, the respective functionality 
     * will not be used 
     */
    public CustomExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        e.printStackTrace();
        String stacktrace = result.toString();
        writeToFile(stacktrace);
        printWriter.close();

        //sendError(stacktrace);

        showErrorDialog(stacktrace);
    }

    public static void showErrorDialog(String stacktrace) {
        File localPath = Gdx.files.internal("crash/java-hyperlog.txt").file();
        stacktrace = stacktrace.replace("<", "");
        stacktrace = stacktrace.replace(">", "");
        stacktrace = stacktrace.replace("$", "");

        TinyFileDialogs.tinyfd_messageBox("Oops! Something went wrong",
                "HyperLap2D just crashed, stacktrace saved in: " + localPath.getAbsolutePath()
                        + "\n\n" + stacktrace,
                "ok", "error", true);
        System.exit(-1);
    }

    public static void sendError(String stacktrace) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("error", stacktrace);
        parameters.put("system", SystemUtils.OS_NAME + " " + SystemUtils.OS_VERSION);
        parameters.put("version", AppConfig.getInstance().version);
        HttpRequest httpGet = new HttpRequest(HttpMethods.GET);
        httpGet.setUrl(sendURL);
        httpGet.setContent(HttpParametersUtils.convertHttpParameters(parameters));
        Gdx.net.sendHttpRequest(httpGet, new HttpResponseListener() {
            public void handleHttpResponse(HttpResponse httpResponse) {
                //showErrorDialog();
            }

            public void failed(Throwable t) {

            }

            @Override
            public void cancelled() {

            }
        });


    }

    private void writeToFile(String stacktrace) {
        try {
            File localPath = Gdx.files.internal("crash/java-hyperlog.txt").file();
            System.out.println(localPath.getAbsolutePath());
            FileUtils.writeStringToFile(localPath, stacktrace, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

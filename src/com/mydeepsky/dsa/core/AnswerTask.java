package com.mydeepsky.dsa.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.mydeepsky.android.task.Task;
import com.mydeepsky.android.task.TaskContext;
import com.mydeepsky.android.task.TaskResult;
import com.mydeepsky.android.task.TaskResult.TaskStatus;
import com.mydeepsky.android.util.StringUtil;

/**
 * Answer task, use to handler the search task
 * 
 */
public class AnswerTask extends Task {
    public final static String REQUEST = "answer_task_request";
    public final static String URL = "answer_task_url";
    public final static String RESULT = "answer_task_result";

    private final String ID = StringUtil.newGuid();
    private final String NAME = "Answer";

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public void pause() {
        // Do nothing
    }

    @Override
    public void resume() {
        // Do nothing
    }

    @Override
    protected TaskResult doInBackground(TaskContext... params) {
        if (params == null || params.length <= 0) {
            return new TaskResult(TaskStatus.Failed, "TaskContext is null");
        }

        TaskContext context = params[0];

        HttpClient client = new DefaultHttpClient();
        HttpPost postMethod = new HttpPost((String) context.get(URL));
        HttpResponse response;
        try {
            postMethod.setEntity(new ByteArrayEntity((byte[]) context.get(REQUEST)));
            response = client.execute(postMethod);
        } catch (IOException e) {
            return new TaskResult(TaskStatus.Failed, "Request error");
        }

        TaskResult result = new TaskResult(TaskStatus.Failed);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity()
                        .getContent()));
                StringBuilder sb = new StringBuilder();
                String res = br.readLine();
                while (res != null) {
                    sb.append(res);
                    res = br.readLine();
                }
                context.set(RESULT, sb.toString());
                result.setStatus(TaskStatus.Finished);
                result.setContext(context);
            } catch (IOException e) {
                result.setStatus(TaskStatus.Failed);
                result.setMessage("Content error");
            }
        } else {
            result.setStatus(TaskStatus.Failed);
            result.setMessage("Http status code " + statusCode);
        }

        if (isCancelled()) {
            result.setStatus(TaskStatus.Cancel);
            result.setMessage("Task is canceled");
        }
        return result;
    }

    @Override
    public String getType() {
        return NAME;
    }
}

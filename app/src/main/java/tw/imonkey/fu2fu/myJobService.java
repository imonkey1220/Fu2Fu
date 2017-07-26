package tw.imonkey.fu2fu;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;

public class myJobService extends com.firebase.jobdispatcher.JobService {

    private static final String TAG = "JobService";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Performing long running task in scheduled job");
        // TODO(developer): add long running task here.
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
package com.squarepolka.readyci.tasks.app.ios;

import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.tasks.Task;
import org.springframework.stereotype.Component;

@Component
public class IOSBuildExport extends Task {

    public static final String TASK_IOS_EXPORT = "ios_export";

    @Override
    public String taskIdentifier() {
        return TASK_IOS_EXPORT;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) throws Exception {
        String archivePath = String.format("%s/app.xcarchive", buildEnvironment.buildPath);
        String exportOptionsPath = String.format("%s/exportOptions.plist", buildEnvironment.projectPath);
        String exportPath = buildEnvironment.buildPath;

        executeCommand(new String[] {"/usr/bin/xcodebuild",
                "-exportArchive",
                "-archivePath", archivePath,
                "-exportOptionsPlist", exportOptionsPath,
                "-exportPath", exportPath}, buildEnvironment.projectPath);

    }
}

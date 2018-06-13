package com.squarepolka.readyci.tasks.app.ios;

import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.tasks.Task;
import org.springframework.stereotype.Component;

@Component
public class IOSArchive extends Task {

    public static final String TASK_IOS_ARCHIVE = "ios_archive";

    @Override
    public String taskIdentifier() {
        return TASK_IOS_ARCHIVE;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) throws Exception {
        String workspace = String.format("%s.xcworkspace", buildEnvironment.buildParameters.get("workspace"));
        String scheme = buildEnvironment.buildParameters.get("target");
        String configuration = buildEnvironment.buildParameters.get("configuration");
        String devTeam = buildEnvironment.buildParameters.get(IOSProvisioningProfileRead.BUILD_PROP_DEV_TEAM);
        String provisioningProfile = buildEnvironment.buildParameters.get(IOSProvisioningProfileRead.BUILD_PROP_PROVISIONING_PROFILE);
        String archivePath = String.format("%s/app.xcarchive", buildEnvironment.buildPath);

        executeCommand(new String[] {"/usr/bin/xcodebuild",
                "DEVELOPMENT_TEAM=" + devTeam,
                "PROVISIONING_PROFILE=" + provisioningProfile,
                "-workspace", workspace,
                "-scheme", scheme,
                "-sdk", "iphoneos",
                "-configuration", configuration,
                "-archivePath", archivePath,
                "archive"}, buildEnvironment.projectPath);
    }
}

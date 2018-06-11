package com.squarepolka.readyci.tasks.app.ios;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;


@Component
public class IOSProvisioningProfileRead extends Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOSProvisioningProfileRead.class);
    public static final String BUILD_PROP_APP_NAME = "appName";
    public static final String BUILD_PROP_ORGANISATION_NAME = "organisationName";
    public static final String BUILD_PROP_DEV_TEAM = "devTeam";
    public static final String BUILD_PROP_PROVISIONING_PROFILE = "provisioningProfile";
    public static final String BUILD_PROP_BUNDLE_ID = "bundleId";

    public String taskIdentifier() {
        return "ios_provisioning_profile_read";
    }

    public void performTask(BuildEnvironment buildEnvironment) throws Exception {

        String relativeProfilePath = buildEnvironment.buildParameters.get("profilePath");
        String profilePath = String.format("%s/%s", buildEnvironment.buildPath, relativeProfilePath);

        InputStream provisioningFileInputStream = decryptProvisioningFile(profilePath);
        readProvisioningInputStream(provisioningFileInputStream, buildEnvironment);

        LOGGER.info(String.format("BUILDING %s for %s in team %s with identifier %s and profile %s",
                buildEnvironment.buildParameters.get(BUILD_PROP_APP_NAME),
                buildEnvironment.buildParameters.get(BUILD_PROP_ORGANISATION_NAME),
                buildEnvironment.buildParameters.get(BUILD_PROP_DEV_TEAM),
                buildEnvironment.buildParameters.get(BUILD_PROP_BUNDLE_ID),
                relativeProfilePath));
    }



    private InputStream decryptProvisioningFile(String profilePath) {
        LOGGER.debug(String.format("Parsing the provisioning profile %s", profilePath));
        return executeCommand(String.format("/usr/bin/security cms -D -i %s", profilePath));
    }

    private void readProvisioningInputStream(InputStream processInputSteam, BuildEnvironment buildEnvironment) throws Exception {
        NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(processInputSteam);
        String appName = rootDict.objectForKey("Name").toString();
        String organisationName = rootDict.objectForKey("TeamName").toString();
        NSArray appIdPrefixs = (NSArray) rootDict.objectForKey("ApplicationIdentifierPrefix");
        String devTeam = appIdPrefixs.lastObject().toString();
        String provisioningProfile = rootDict.objectForKey("UUID").toString();
        NSDictionary entitlementsDict = (NSDictionary) rootDict.objectForKey("Entitlements");
        String fullBundleId = entitlementsDict.objectForKey("application-identifier").toString();
        String bundleId = removeTeamFromBundleId(fullBundleId, devTeam);

        buildEnvironment.buildParameters.put(BUILD_PROP_APP_NAME, appName);
        buildEnvironment.buildParameters.put(BUILD_PROP_ORGANISATION_NAME, organisationName);
        buildEnvironment.buildParameters.put(BUILD_PROP_DEV_TEAM, devTeam);
        buildEnvironment.buildParameters.put(BUILD_PROP_PROVISIONING_PROFILE, provisioningProfile);
        buildEnvironment.buildParameters.put(BUILD_PROP_BUNDLE_ID, bundleId);
    }

    private String removeTeamFromBundleId(String bundleId, String teamId) {
        return bundleId.replace(String.format("%s.", teamId), "");
    }


}
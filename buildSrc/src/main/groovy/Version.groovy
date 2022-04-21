/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

import org.gradle.api.GradleException

/**
 * Class for easier generation of versionCode and versionName attributes of Android builds.
 * Represents a specific version with major, minor, patch version, and optionally a release candidate version or is development version,
 * in accordance with semantic versioning principles.
 * the dailyNumber only suit for daily builds, and only affect to version name.
 */
class Version {

    static final int NO_RC = 0
    static final int INIT_DAILY_NUMBER = 0
    static final boolean IS_DEV = false
    static final boolean IS_DAILY = false

    final int major
    final int minor
    final int patch
    final int releaseCandidate
    final boolean isDevelopment

    final boolean isDaily
    final int dailyNumber

    Version(int major, int minor, int patch, int releaseCandidate = NO_RC, boolean isDevelopment = IS_DEV,
            boolean isDaily = IS_DAILY, int dailyNumber = INIT_DAILY_NUMBER) {
        verifyVersionCorrectness(major, minor, patch, releaseCandidate)

        this.major = major
        this.minor = minor
        this.patch = patch
        this.releaseCandidate = releaseCandidate
        this.isDevelopment = isDevelopment

        this.isDaily = isDaily
        this.dailyNumber = dailyNumber
    }

    /**
     * AABBCCD format, where version is AA.BB.CCrc(D+1), e.g. 1.3.11rc5 is represented as 0103114
     * Versions without RC designation are represented as AABBCC9
     */
    def versionCode() {
        def releaseCandidateDigit
        if (releaseCandidate == NO_RC) releaseCandidateDigit = 9
        else releaseCandidateDigit = releaseCandidate - 1
        major * 100000 + minor * 1000 + patch * 10 + releaseCandidateDigit
    }

    def versionName() {
        switch (releaseCandidate) {
            case NO_RC:
                if (isDevelopment) String.format("%d.%d.%ddev", major, minor, patch)
                else if (isDaily) {
                    String.format("%d.%d.%d.%d", major, minor, patch, dailyNumber)
                } else
                    String.format("%d.%d.%d", major, minor, patch)
                break
            default:
                if (isDevelopment) String.format("%d.%d.%drc%ddev", major, minor, patch, releaseCandidate)
                else String.format("%d.%d.%drc%d", major, minor, patch, releaseCandidate)
                break
        }
    }

    private static def verifyVersionCorrectness(int major, int minor, int patch, int releaseCandidate) {
        if (![major, minor, patch].every { it >= 0 && it < 100 }) {
            throw new GradleException("Major, minor, and patch versions should be in [0, 100) range!")
        }
        if (releaseCandidate < 0 || releaseCandidate >= 10) {
            throw new GradleException("Release candidate version should be in [0, 9) range!")
        }
    }
}

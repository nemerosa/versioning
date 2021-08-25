rootProject.name = "versioning"

plugins {
    id("com.gradle.enterprise") version ("3.6.4")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

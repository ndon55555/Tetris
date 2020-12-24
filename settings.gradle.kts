rootProject.name = "tetris"

include(
    ":browser",
    ":core",
    ":desktop"
)

enableFeaturePreview("ONE_LOCKFILE_PER_PROJECT")

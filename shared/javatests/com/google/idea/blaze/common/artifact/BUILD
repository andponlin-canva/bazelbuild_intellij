load("@rules_java//java:defs.bzl", "java_library", "java_test")

package(
    default_visibility = [
        "//aswb:__subpackages__",
        "//base:__subpackages__",
        "//querysync:__subpackages__",
        "//shared:__subpackages__",
    ],
)

java_test(
    name = "BuildArtifactCacheTest",
    size = "small",
    srcs = ["BuildArtifactCacheTest.java"],
    deps = [
        ":test_utils",
        "//shared/java/com/google/idea/blaze/common/artifact",
        "//shared/javatests/com/google/idea/blaze/common:test_utils",
        "//third_party/java/junit",
        "//third_party/java/truth",
        "@com_google_guava_guava//jar",
    ],
)

java_library(
    name = "test_utils",
    srcs = [
        "MockArtifact.java",
        "TestArtifactFetcher.java",
        "TestOutputArtifact.java",
    ],
    deps = [
        "//shared/java/com/google/idea/blaze/common",
        "//shared/java/com/google/idea/blaze/common/artifact",
        "//shared/java/com/google/idea/blaze/exception",
        "//third_party/java/auto_value",
        "@com_google_guava_guava//jar",
        "@jsr305_annotations//jar",
    ],
)

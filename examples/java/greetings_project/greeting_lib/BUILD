load("@contrib_rules_jvm//java:defs.bzl", "java_junit5_test")
load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_java//java:java_test.bzl", "java_test")
load("//:junit5_deps.bzl", "JUNIT5_DEPS")

java_library(
    name = "greeting_lib",
    srcs = ["src/com/example/Greeting.java"],
    visibility = ["//visibility:public"],
)

java_test(
    name = "greeting_test_junit4",
    srcs = [
        "tests/com/example/junit4/GreetingTest.java",
    ],
    test_class = "com.example.junit4.GreetingTest",
    deps = [
        ":greeting_lib",
        "@junit//jar",
    ],
)

java_test(
    name = "parameterized_greeting_test_junit4",
    srcs = [
        "tests/com/example/junit4/ParameterizedGreetingTest.java",
    ],
    test_class = "com.example.junit4.ParameterizedGreetingTest",
    deps = [
        ":greeting_lib",
        "@junit//jar",
    ],
)

java_junit5_test(
    name = "greeting_test_junit5",
    size = "small",
    srcs = [
        "tests/com/example/junit5/GreetingTest.java",
    ],
    test_class = "com.example.junit5.GreetingTest",
    deps = [
        ":greeting_lib",
    ] + JUNIT5_DEPS,
)

java_junit5_test(
    name = "parameterized_greeting_test_junit5",
    size = "small",
    srcs = [
        "tests/com/example/junit5/ParameterizedGreetingTest.java",
    ],
    test_class = "com.example.junit5.ParameterizedGreetingTest",
    deps = [
        ":greeting_lib",
    ] + JUNIT5_DEPS,
)

java_test(
    name = "env_vars_test",
    srcs = ["tests/com/example/EnvVarsTest.java"],
    test_class = "com.example.EnvVarsTest",
    deps = [
        "@junit//jar",
    ],
)

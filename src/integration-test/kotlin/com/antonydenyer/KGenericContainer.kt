package com.antonydenyer

import org.testcontainers.containers.GenericContainer

// https://github.com/testcontainers/testcontainers-java/issues/318
class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

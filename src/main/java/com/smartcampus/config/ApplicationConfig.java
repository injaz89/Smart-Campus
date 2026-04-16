package com.smartcampus.config;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Configure the base path for all JAX-RS resources.
 */
@ApplicationPath("/api/v1")
public class ApplicationConfig extends Application {
}

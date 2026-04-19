package com.smartcampus.config;

import com.smartcampus.model.errors.SensorUnavailableException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        return Response.status(Response.Status.FORBIDDEN) // 403 Forbidden
                .entity("{\"error\": \"" + exception.getMessage() + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

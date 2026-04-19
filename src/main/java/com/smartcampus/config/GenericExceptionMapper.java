package com.smartcampus.config;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        // If it's already a WebApplicationException (like 404 from Jersey), don't catch it as 500
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            return Response.status(webEx.getResponse().getStatus())
                    .entity("{\"error\": \"" + webEx.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"An unexpected internal server error occurred.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

package com.smartcampus.config;

import com.smartcampus.model.errors.RoomNotEmptyException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity("{\"error\": \"" + exception.getMessage() + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

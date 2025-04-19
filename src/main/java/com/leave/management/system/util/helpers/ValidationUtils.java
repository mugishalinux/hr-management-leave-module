package com.leave.management.system.util.helpers;

import com.leave.management.system.exceptions.ApiRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.Supplier;
@Component
public class ValidationUtils {

    public static void validateAndThrow(Supplier<String> validationFunction) {
        String validationError = validationFunction.get();
        if (validationError != null) {
            throw new ApiRequestException(validationError);
        }
    }
}